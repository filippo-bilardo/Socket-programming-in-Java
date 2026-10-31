/** Chat multiclient con messaggi binari: int tipo, UTF mittente, UTF contenuto,
 * long timestamp UTC (secondi). Ogni campo scritto deve essere letto nello stesso ordine.
 * JDK 11+. Vedere README.md per comandi e limiti didattici.
 */
import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class CustomProtocolChat {
    public enum MessageType {
        CONNECT(1), DISCONNECT(2), CHAT_MESSAGE(3), USER_LIST(4), SYSTEM_MESSAGE(5), HEARTBEAT(6);
        private final int code;
        MessageType(int code) { this.code = code; }
        public int getCode() { return code; }
        public static MessageType fromCode(int code) {
            for (MessageType type : values()) if (type.code == code) return type;
            throw new IllegalArgumentException("Codice messaggio non valido: " + code);
        }
    }
    public static class Message {
        private final MessageType type;
        private final String sender, content;
        private final LocalDateTime timestamp;
        public Message(MessageType type, String sender, String content) {
            this(type, sender, content, LocalDateTime.now(ZoneOffset.UTC));
        }
        private Message(MessageType type, String sender, String content, LocalDateTime timestamp) {
            this.type = type; this.sender = sender; this.content = content; this.timestamp = timestamp;
        }
        public MessageType getType() { return type; }
        public String getSender() { return sender; }
        public String getContent() { return content; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void writeTo(DataOutputStream out) throws IOException {
            out.writeInt(type.getCode());
            out.writeUTF(sender == null ? "" : sender);
            out.writeUTF(content == null ? "" : content);
            out.writeLong(timestamp.toEpochSecond(ZoneOffset.UTC));
        }
        public static Message readFrom(DataInputStream in) throws IOException {
            MessageType type;
            try { type = MessageType.fromCode(in.readInt()); }
            catch (IllegalArgumentException e) { throw new IOException(e.getMessage(), e); }
            String sender = in.readUTF();
            String content = in.readUTF();
            LocalDateTime time;
            try { time = LocalDateTime.ofEpochSecond(in.readLong(), 0, ZoneOffset.UTC); }
            catch (DateTimeException e) { throw new IOException("Timestamp non valido", e); }
            return new Message(type, sender, content, time);
        }
        @Override public String toString() {
            return "[" + timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " UTC] " + sender + ": " + content;
        }
    }
    public static class ChatServer {
        private final int port;
        private final ConcurrentMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
        private final Set<Socket> sockets = ConcurrentHashMap.newKeySet();
        // Nessuna coda di socket: oltre 50 gestori la connessione viene chiusa.
        private final ExecutorService pool = new ThreadPoolExecutor(0, 50, 60,
                TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
        private final Object lifecycle = new Object();
        private volatile boolean running = true;
        private ServerSocket listener;
        public ChatServer(int port) { this.port = port; }
        public void start() throws IOException {
            synchronized (lifecycle) {
                if (!running) return;
                listener = new ServerSocket(port);
            }
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
            System.out.println("Chat server in ascolto sulla porta " + port);
            try {
                while (running) {
                    Socket socket = listener.accept();
                    synchronized (lifecycle) {
                        if (!running) { close(socket); break; }
                        sockets.add(socket);
                        try { pool.execute(new ClientHandler(socket)); }
                        catch (RejectedExecutionException e) { sockets.remove(socket); close(socket); }
                    }
                }
            } catch (SocketException e) {
                if (running) throw e;
            } finally { shutdown(); }
        }
        public void broadcastMessage(Message message) {
            for (ClientHandler client : clients.values()) {
                try { client.sendMessage(message); }
                catch (IOException e) { close(client.socket); }
            }
        }
        public void shutdown() {
            synchronized (lifecycle) {
                if (!running) return;
                running = false;
                if (listener != null) try { listener.close(); } catch (IOException ignored) { }
                for (Socket socket : sockets) close(socket);
                pool.shutdownNow();
            }
        }
        private static void close(Socket socket) {
            try { socket.close(); } catch (IOException ignored) { }
        }
        private class ClientHandler implements Runnable {
            private final Socket socket;
            private DataOutputStream out;
            private String username;
            private boolean registered;
            ClientHandler(Socket socket) { this.socket = socket; }
            public void sendMessage(Message message) throws IOException {
                synchronized (out) { message.writeTo(out); out.flush(); }
            }
            @Override public void run() {
                try (Socket owned = socket;
                     DataInputStream in = new DataInputStream(socket.getInputStream())) {
                    out = new DataOutputStream(socket.getOutputStream());
                    socket.setSoTimeout(10000); // Identificazione entro 10 secondi.
                    Message first = Message.readFrom(in);
                    if (first.getType() != MessageType.CONNECT) throw new IOException("CONNECT richiesto");
                    username = first.getContent().trim();
                    if (username.isEmpty() || username.length() > 64 || username.contains(",")) {
                        sendMessage(new Message(MessageType.SYSTEM_MESSAGE, "Server", "Username non valido"));
                        return;
                    }
                    registered = clients.putIfAbsent(username, this) == null;
                    if (!registered) {
                        sendMessage(new Message(MessageType.SYSTEM_MESSAGE, "Server", "Username già in uso"));
                        return;
                    }
                    socket.setSoTimeout(300000); // Inattività massima: 5 minuti.
                    broadcastMessage(new Message(MessageType.SYSTEM_MESSAGE, "Server", username + " si è unito alla chat"));
                    sendMessage(new Message(MessageType.USER_LIST, "Server", String.join(",", clients.keySet())));
                    while (running) {
                        Message message = Message.readFrom(in);
                        if (message.getType() == MessageType.DISCONNECT) break;
                        if (message.getType() == MessageType.CHAT_MESSAGE)
                            broadcastMessage(new Message(MessageType.CHAT_MESSAGE, username, message.getContent()));
                    }
                } catch (EOFException ignored) {
                } catch (IOException e) {
                    if (running) System.err.println("Client " + username + ": " + e.getMessage());
                } finally {
                    sockets.remove(socket);
                    if (registered && clients.remove(username, this) && running)
                        broadcastMessage(new Message(MessageType.SYSTEM_MESSAGE, "Server", username + " ha lasciato la chat"));
                }
            }
        }
    }
    public static class ChatClient {
        private final String host, username;
        private final int port;
        private Socket socket;
        private DataOutputStream out;
        private volatile boolean connected;
        public ChatClient(String host, int port, String username) {
            this.host = host; this.port = port; this.username = username;
        }
        public void connect() throws IOException {
            socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(host, port), 5000);
                out = new DataOutputStream(socket.getOutputStream());
                send(new Message(MessageType.CONNECT, username, username));
                connected = true;
                Thread receiver = new Thread(() -> {
                    try {
                        DataInputStream in = new DataInputStream(socket.getInputStream());
                        while (connected) System.out.println(Message.readFrom(in));
                    } catch (IOException e) {
                        if (connected) System.out.println("Connessione terminata: " + e.getMessage());
                    } finally {
                        connected = false;
                        try { socket.close(); } catch (IOException ignored) { }
                    }
                });
                receiver.setDaemon(true); receiver.start();
            } catch (IOException e) { socket.close(); throw e; }
        }
        private void send(Message message) throws IOException {
            synchronized (out) { message.writeTo(out); out.flush(); }
        }
        public void sendMessage(String content) throws IOException {
            if (connected) send(new Message(MessageType.CHAT_MESSAGE, username, content));
        }
        public void disconnect() throws IOException {
            try { if (connected) send(new Message(MessageType.DISCONNECT, username, "")); }
            finally { connected = false; if (socket != null) socket.close(); }
        }
        public void startInteractiveSession() throws IOException {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Scrivi un messaggio; quit per uscire.");
            try {
                while (connected && scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.equalsIgnoreCase("quit")) break;
                    if (!line.isBlank()) sendMessage(line);
                }
            } finally { disconnect(); }
        }
    }
    public static void main(String[] args) {
        try {
            if (args.length == 2 && args[0].equalsIgnoreCase("server"))
                new ChatServer(Integer.parseInt(args[1])).start();
            else if (args.length == 4 && args[0].equalsIgnoreCase("client")) {
                ChatClient client = new ChatClient(args[1], Integer.parseInt(args[2]), args[3]);
                client.connect(); client.startInteractiveSession();
            } else System.out.println("java CustomProtocolChat server <porta>\njava CustomProtocolChat client <host> <porta> <nome>");
        } catch (IOException | IllegalArgumentException e) { System.err.println(e.getMessage()); }
    }
}
