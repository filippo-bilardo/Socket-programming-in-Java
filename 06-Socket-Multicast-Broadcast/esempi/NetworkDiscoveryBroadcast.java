import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Discovery applicativo: trova soltanto i dispositivi che eseguono il responder. */
public class NetworkDiscoveryBroadcast {
    private static final int PORT = 8891;
    public static String broadcastAddress(String cidr) throws IOException {
        String[] parts = cidr.split("/", -1);
        if (parts.length != 2) throw new IOException("Richiesto IPv4/prefisso");
        String[] octets = parts[0].split("\\.", -1);
        if (octets.length != 4) throw new IOException("IPv4 non valido");
        int ip = 0;
        try {
            for (String octet : octets) {
                int n = Integer.parseInt(octet);
                if (n < 0 || n > 255) throw new NumberFormatException();
                ip = (ip << 8) | n;
            }
            int prefix = Integer.parseInt(parts[1]);
            if (prefix < 0 || prefix > 30) throw new NumberFormatException();
            int mask = prefix == 0 ? 0 : -1 << (32 - prefix);
            int value = ip | ~mask;
            return InetAddress.getByAddress(new byte[]{(byte)(value >>> 24), (byte)(value >>> 16), (byte)(value >>> 8), (byte)value}).getHostAddress();
        } catch (NumberFormatException e) { throw new IOException("IPv4/prefisso non valido (0..30)", e); }
    }
    public static class NetworkResponder {
        private final int port;
        private volatile boolean running = true;
        private volatile DatagramSocket socket;
        public NetworkResponder() { this(PORT); }
        public NetworkResponder(int port) { this.port = port; }
        public void start() throws IOException {
            try (DatagramSocket owned = new DatagramSocket(port)) {
                socket = owned;
                if (!running) return;
                System.out.println("PRONTO responder " + port);
                byte[] data = new byte[1401];
                while (running) {
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    try { owned.receive(packet); }
                    catch (SocketException e) { if (!running) break; throw e; }
                    if (packet.getLength() > 1400) continue;
                    String request = new String(data, 0, packet.getLength(), StandardCharsets.UTF_8);
                    String[] fields = request.split("\\|", -1);
                    if (fields.length != 2 || !fields[0].equals("DISCOVER")) continue;
                    try { UUID.fromString(fields[1]); } catch (IllegalArgumentException e) { continue; }
                    String name = URLEncoder.encode(InetAddress.getLocalHost().getHostName(), StandardCharsets.UTF_8);
                    String os = URLEncoder.encode(System.getProperty("os.name", "unknown"), StandardCharsets.UTF_8);
                    byte[] reply = ("DEVICE|" + fields[1] + "|" + name + "|" + os).getBytes(StandardCharsets.UTF_8);
                    owned.send(new DatagramPacket(reply, reply.length, packet.getSocketAddress()));
                }
            } finally { socket = null; }
        }
        public void stop() { running = false; DatagramSocket current = socket; if (current != null) current.close(); }
    }
    public static class NetworkScanner {
        public List<String> scanNetwork(String target, int port) throws IOException {
            String destination = target.contains("/") ? broadcastAddress(target) : target;
            InetAddress address = InetAddress.getByName(destination);
            if (!(address instanceof Inet4Address)) throw new IOException("Richiesto IPv4");
            String nonce = UUID.randomUUID().toString();
            byte[] query = ("DISCOVER|" + nonce).getBytes(StandardCharsets.UTF_8);
            Map<String, String> found = new TreeMap<>();
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);
                long start = System.nanoTime(), deadline = start + TimeUnit.SECONDS.toNanos(2);
                socket.send(new DatagramPacket(query, query.length, address, port));
                byte[] data = new byte[1401];
                while (System.nanoTime() < deadline) {
                    int remainingMs = (int)Math.max(1, TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime()));
                    socket.setSoTimeout(remainingMs);
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    try { socket.receive(packet); } catch (SocketTimeoutException e) { break; }
                    if (packet.getPort() != port || packet.getLength() > 1400) continue;
                    String[] parts = new String(data, 0, packet.getLength(), StandardCharsets.UTF_8).split("\\|", -1);
                    if (parts.length != 4 || !parts[0].equals("DEVICE") || !parts[1].equals(nonce)) continue;
                    try {
                        String host = URLDecoder.decode(parts[2], StandardCharsets.UTF_8);
                        String os = URLDecoder.decode(parts[3], StandardCharsets.UTF_8);
                        long rtt = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                        String ip = packet.getAddress().getHostAddress();
                        found.put(ip, ip + " nome=" + host + " os=" + os + " risposta_ms=" + rtt);
                    } catch (IllegalArgumentException ignored) { }
                }
            }
            List<String> result = new ArrayList<>(found.values());
            for (String device : result) System.out.println("TROVATO " + device);
            System.out.println("TOTALE " + result.size());
            return result;
        }
    }
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("respond")) {
            NetworkResponder responder = new NetworkResponder(args.length > 1 ? Integer.parseInt(args[1]) : PORT);
            Runtime.getRuntime().addShutdownHook(new Thread(responder::stop)); responder.start();
        } else if (args.length > 0 && args[0].equals("scan")) {
            new NetworkScanner().scanNetwork(args.length > 1 ? args[1] : "255.255.255.255", args.length > 2 ? Integer.parseInt(args[2]) : PORT);
        } else System.out.println("java NetworkDiscoveryBroadcast respond [porta] | scan [broadcast-IPv4|CIDR] [porta]");
    }
}
