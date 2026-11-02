import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
/** Echo esatto di un datagramma (max 1024 byte). JDK 11+. */
public class EchoUDP {
    public static final int MAX_MESSAGE = 1024;
    public static class Server {
        private final int port;
        private volatile boolean running = true;
        private volatile DatagramSocket socket;
        public Server(int port) { this.port = port; }
        public void start() throws IOException {
            try (DatagramSocket owned = new DatagramSocket(port)) {
                socket = owned;
                if (!running) return;
                System.out.println("PRONTO " + owned.getLocalPort());
                byte[] buffer = new byte[MAX_MESSAGE + 1];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                while (running) {
                    packet.setLength(buffer.length); // receive modifica la lunghezza del pacchetto.
                    try { owned.receive(packet); }
                    catch (SocketException e) { if (!running) break; throw e; }
                    if (packet.getLength() > MAX_MESSAGE) {
                        System.err.println("SCARTATO datagramma oltre " + MAX_MESSAGE + " byte");
                        continue;
                    }
                    owned.send(new DatagramPacket(packet.getData(), packet.getOffset(), packet.getLength(), packet.getSocketAddress()));
                }
            } finally { socket = null; }
        }
        public void stop() {
            running = false;
            DatagramSocket current = socket;
            if (current != null) current.close();
        }
    }
    public static class Client implements AutoCloseable {
        private final DatagramSocket socket;
        private final InetSocketAddress peer;
        public Client(String host, int port) throws IOException {
            peer = new InetSocketAddress(InetAddress.getByName(host), port);
            socket = new DatagramSocket();
        }
        public String sendMessage(String message, int timeoutMs) throws IOException {
            byte[] data = message.getBytes(StandardCharsets.UTF_8);
            if (data.length > MAX_MESSAGE || timeoutMs <= 0) throw new IllegalArgumentException("Max 1024 byte; timeout positivo");
            socket.send(new DatagramPacket(data, data.length, peer));
            long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(timeoutMs);
            byte[] buffer = new byte[MAX_MESSAGE + 1];
            while (true) {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) throw new SocketTimeoutException("Timeout risposta");
                socket.setSoTimeout((int)Math.max(1, java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(remaining)));
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                socket.receive(reply);
                if (!reply.getSocketAddress().equals(peer)) continue;
                if (reply.getLength() > MAX_MESSAGE) throw new IOException("Risposta troppo grande");
                return new String(reply.getData(), reply.getOffset(), reply.getLength(), StandardCharsets.UTF_8);
            }
        }
        public void interactive() throws IOException {
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = console.readLine()) != null && !line.equals("quit")) {
                try { System.out.println("ECHO " + sendMessage(line, 2000)); }
                catch (SocketTimeoutException e) { System.err.println("TIMEOUT: esito non noto; termina la sessione senza riusare il peer"); break; }
            }
        }
        @Override public void close() { socket.close(); }
    }
    public static void main(String[] args) throws Exception {
        if (args.length == 2 && args[0].equals("server")) {
            Server server = new Server(Integer.parseInt(args[1]));
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
            server.start();
        } else if (args.length >= 3 && args[0].equals("client")) {
            try (Client client = new Client(args[1], Integer.parseInt(args[2]))) {
                if (args.length > 3) System.out.println("ECHO " + client.sendMessage(args[3], 2000));
                else client.interactive();
            }
        } else System.out.println("java EchoUDP server <porta> | client <host> <porta> [messaggio]");
    }
}
