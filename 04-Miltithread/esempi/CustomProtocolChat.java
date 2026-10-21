/**
 * Nome dell'Esempio: Custom Protocol Chat
 * Guida di Riferimento: 02-Comunicazione-Dati-TCP.md
 * 
 * Obiettivo: Dimostrare protocollo personalizzato per chat multi-client.
 * 
 * Spiegazione:
 * 1. Protocollo a messaggi con header strutturato
 * 2. Server chat con broadcasting
 * 3. Client con interfaccia interattiva
 * 
 * @author Socket Programming Course
 * @version 1.0
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomProtocolChat {
    
    // Protocollo messaggi
    public enum MessageType {
        CONNECT(1),
        DISCONNECT(2),
        CHAT_MESSAGE(3),
        USER_LIST(4),
        SYSTEM_MESSAGE(5),
        HEARTBEAT(6);
        
        private final int code;
        
        MessageType(int code) {
            this.code = code;
        }
        
        public int getCode() {
            return code;
        }
        
        public static MessageType fromCode(int code) {
            for (MessageType type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Codice messaggio non valido: " + code);
        }
    }
    
    public static class Message {
        private final MessageType type;
        private final String sender;
        private final String content;
        private final LocalDateTime timestamp;
        
        public Message(MessageType type, String sender, String content) {
            this.type = type;
            this.sender = sender;
            this.content = content;
            this.timestamp = LocalDateTime.now();
        }
        
        // Getters
        public MessageType getType() { return type; }
        public String getSender() { return sender; }
        public String getContent() { return content; }
        public LocalDateTime getTimestamp() { return timestamp; }
        
        // Serializzazione binaria
        public void writeTo(DataOutputStream out) throws IOException {
            out.writeInt(type.getCode());
            out.writeUTF(sender != null ? sender : "");
            out.writeUTF(content != null ? content : "");
            out.writeLong(timestamp.toEpochSecond(java.time.ZoneOffset.UTC));
        }
        
        public static Message readFrom(DataInputStream in) throws IOException {
            int typeCode = in.readInt();
            MessageType type = MessageType.fromCode(typeCode);
            String sender = in.readUTF();
            String content = in.readUTF();
            // timestamp viene impostato automaticamente
            return new Message(type, sender.isEmpty() ? null : sender, content.isEmpty() ? null : content);
        }
        
        @Override
        public String toString() {
            String timeStr = timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            return String.format("[%s] %s: %s", timeStr, sender, content);
        }
    }
    
    /**
     * Server Chat
     */
    public static class ChatServer {
        private final int port;
        private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
        private final ExecutorService threadPool = Executors.newCachedThreadPool();
        private volatile boolean running = false;
        
        public ChatServer(int port) {
            this.port = port;
        }
        
        public void start() throws IOException {
            System.out.println("💬 Chat Server Custom Protocol");
            System.out.println("   Porta: " + port);
            System.out.println("🛑 Premi Ctrl+C per fermare");
            System.out.println("=" .repeat(50));
            
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
            
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                serverSocket.setReuseAddress(true);
                running = true;
                
                System.out.println("✅ Server avviato, in attesa di connessioni...\n");
                
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("🔗 Nuova connessione: " + clientSocket.getRemoteSocketAddress());
                    
                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    threadPool.execute(handler);
                }
            }
        }
        
        public void addClient(String username, ClientHandler handler) {
            clients.put(username, handler);
            System.out.println("👤 Utente connesso: " + username + " (totale: " + clients.size() + ")");
            
            // Notifica altri utenti
            broadcastMessage(new Message(MessageType.SYSTEM_MESSAGE, "Server", 
                           username + " si è unito alla chat"));
            
            // Invia lista utenti al nuovo client
            sendUserList(handler);
        }
        
        public void removeClient(String username) {
            ClientHandler removed = clients.remove(username);
            if (removed != null) {
                System.out.println("👤 Utente disconnesso: " + username + " (totale: " + clients.size() + ")");
                
                // Notifica altri utenti
                broadcastMessage(new Message(MessageType.SYSTEM_MESSAGE, "Server", 
                               username + " ha lasciato la chat"));
            }
        }
        
        public void broadcastMessage(Message message) {
            List<String> disconnected = new ArrayList<>();
            
            for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
                try {
                    entry.getValue().sendMessage(message);
                } catch (IOException e) {
                    disconnected.add(entry.getKey());
                }
            }
            
            // Rimuove client disconnessi
            for (String username : disconnected) {
                removeClient(username);
            }
        }
        
        public void sendUserList(ClientHandler handler) {
            String userListContent = String.join(",", clients.keySet());
            Message userListMessage = new Message(MessageType.USER_LIST, "Server", userListContent);
            try {
                handler.sendMessage(userListMessage);
            } catch (IOException e) {
                // Gestito dal chiamante
            }
        }
        
        public void shutdown() {
            running = false;
            
            // Notifica disconnessione a tutti i client
            Message shutdownMessage = new Message(MessageType.SYSTEM_MESSAGE, "Server", 
                                                "Server in arresto...");
            broadcastMessage(shutdownMessage);
            
            threadPool.shutdown();
            System.out.println("✅ Chat server arrestato");
        }
        
        private class ClientHandler implements Runnable {
            private final Socket socket;
            private final ChatServer server;
            private DataInputStream input;
            private DataOutputStream output;
            private String username;
            
            public ClientHandler(Socket socket, ChatServer server) {
                this.socket = socket;
                this.server = server;
            }
            
            @Override
            public void run() {
                try {
                    input = new DataInputStream(socket.getInputStream());
                    output = new DataOutputStream(socket.getOutputStream());
                    
                    // Attende messaggio di connessione
                    Message connectMessage = Message.readFrom(input);
                    if (connectMessage.getType() == MessageType.CONNECT) {
                        username = connectMessage.getContent();
                        
                        if (username == null || username.trim().isEmpty()) {
                            sendMessage(new Message(MessageType.SYSTEM_MESSAGE, "Server", 
                                      "Username non valido"));
                            return;
                        }
                        
                        if (clients.containsKey(username)) {
                            sendMessage(new Message(MessageType.SYSTEM_MESSAGE, "Server", 
                                      "Username già in uso"));
                            return;
                        }
                        
                        server.addClient(username, this);
                        
                        // Loop messaggi
                        Message message;
                        while ((message = Message.readFrom(input)) != null) {
                            if (message.getType() == MessageType.DISCONNECT) {
                                break;
                            }
                            
                            if (message.getType() == MessageType.CHAT_MESSAGE) {
                                // Ribroadcast con sender corretto
                                Message chatMessage = new Message(MessageType.CHAT_MESSAGE, 
                                                                username, message.getContent());
                                server.broadcastMessage(chatMessage);
                                System.out.println("💬 " + username + ": " + message.getContent());
                            }
                        }
                    }
                    
                } catch (EOFException e) {
                    // Connessione chiusa normalmente
                } catch (IOException e) {
                    System.err.println("💥 Errore client " + username + ": " + e.getMessage());
                } finally {
                    if (username != null) {
                        server.removeClient(username);
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // Ignora
                    }
                }
            }
            
            public void sendMessage(Message message) throws IOException {
                synchronized (output) {
                    message.writeTo(output);
                    output.flush();
                }
            }
        }
    }
    
    /**
     * Client Chat
     */
    public static class ChatClient {
        private final String serverHost;
        private final int serverPort;
        private final String username;
        private Socket socket;
        private DataInputStream input;
        private DataOutputStream output;
        private volatile boolean connected = false;
        
        public ChatClient(String serverHost, int serverPort, String username) {
            this.serverHost = serverHost;
            this.serverPort = serverPort;
            this.username = username;
        }
        
        public void connect() throws IOException {
            socket = new Socket(serverHost, serverPort);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            
            // Invia messaggio di connessione
            Message connectMessage = new Message(MessageType.CONNECT, username, username);
            connectMessage.writeTo(output);
            output.flush();
            
            connected = true;
            
            // Avvia thread per ricezione messaggi
            Thread readerThread = new Thread(this::readMessages);
            readerThread.setDaemon(true);
            readerThread.start();
            
            System.out.println("✅ Connesso al server come: " + username);
            System.out.println("Digita i tuoi messaggi (quit per uscire):");
            System.out.println("-" .repeat(40));
        }
        
        public void sendMessage(String content) throws IOException {
            if (!connected) return;
            
            Message message = new Message(MessageType.CHAT_MESSAGE, username, content);
            synchronized (output) {
                message.writeTo(output);
                output.flush();
            }
        }
        
        public void disconnect() throws IOException {
            if (!connected) return;
            
            connected = false;
            
            Message disconnectMessage = new Message(MessageType.DISCONNECT, username, "");
            disconnectMessage.writeTo(output);
            output.flush();
            
            socket.close();
            System.out.println("👋 Disconnesso dal server");
        }
        
        private void readMessages() {
            try {
                while (connected) {
                    Message message = Message.readFrom(input);
                    
                    switch (message.getType()) {
                        case CHAT_MESSAGE:
                            if (!username.equals(message.getSender())) {
                                System.out.println("💬 " + message);
                            }
                            break;
                            
                        case SYSTEM_MESSAGE:
                            System.out.println("🔔 " + message.getContent());
                            break;
                            
                        case USER_LIST:
                            String[] users = message.getContent().split(",");
                            System.out.println("👥 Utenti online: " + String.join(", ", users));
                            break;
                            
                        case CONNECT:
                        case DISCONNECT:
                        case HEARTBEAT:
                            // Ignorati nel client
                            break;
                    }
                }
            } catch (EOFException e) {
                // Server disconnesso
                System.out.println("🔌 Server disconnesso");
                connected = false;
            } catch (IOException e) {
                if (connected) {
                    System.err.println("💥 Errore ricezione: " + e.getMessage());
                    connected = false;
                }
            }
        }
        
        public void startInteractiveSession() throws IOException {
            try (Scanner scanner = new Scanner(System.in)) {
                while (connected) {
                    String line = scanner.nextLine();
                    
                    if ("quit".equalsIgnoreCase(line)) {
                        break;
                    }
                    
                    if (!line.trim().isEmpty()) {
                        sendMessage(line);
                    }
                }
            }
            
            disconnect();
        }
    }
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("💬 Custom Protocol Chat");
            System.out.println("Utilizzo:");
            System.out.println("  java CustomProtocolChat server <porta>");
            System.out.println("  java CustomProtocolChat client <host> <porta> <username>");
            System.out.println();
            System.out.println("Esempi:");
            System.out.println("  java CustomProtocolChat server 8080");
            System.out.println("  java CustomProtocolChat client localhost 8080 Mario");
            return;
        }
        
        String mode = args[0].toLowerCase();
        
        if ("server".equals(mode)) {
            if (args.length < 2) {
                System.err.println("❌ Porta richiesta per il server");
                return;
            }
            
            try {
                int port = Integer.parseInt(args[1]);
                ChatServer server = new ChatServer(port);
                server.start();
                
            } catch (NumberFormatException e) {
                System.err.println("❌ Porta non valida: " + args[1]);
            } catch (IOException e) {
                System.err.println("💥 Errore server: " + e.getMessage());
            }
            
        } else if ("client".equals(mode)) {
            if (args.length < 4) {
                System.err.println("❌ Host, porta e username richiesti per il client");
                return;
            }
            
            try {
                String host = args[1];
                int port = Integer.parseInt(args[2]);
                String username = args[3];
                
                ChatClient client = new ChatClient(host, port, username);
                client.connect();
                client.startInteractiveSession();
                
            } catch (NumberFormatException e) {
                System.err.println("❌ Porta non valida: " + args[2]);
            } catch (IOException e) {
                System.err.println("💥 Errore client: " + e.getMessage());
            }
            
        } else {
            System.err.println("❌ Modalità non riconosciuta: " + mode);
        }
    }
}