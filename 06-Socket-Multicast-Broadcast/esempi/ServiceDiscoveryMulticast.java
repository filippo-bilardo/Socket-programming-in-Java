import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/** Annunci multicast periodici, cache locale e scadenza degli annunci mancanti. */
public class ServiceDiscoveryMulticast {
    private static final String GROUP = "239.255.0.2";
    private static final int PORT = 8890;
    private static final long EXPIRY_NANOS = TimeUnit.SECONDS.toNanos(15);
    public static class ServiceInfo {
        private final String name, type;
        private final InetAddress address;
        private final int port;
        // Il mittente non può scegliere l'età nella cache del ricevitore.
        private final long receivedAt = System.nanoTime();
        public ServiceInfo(String name, String type, InetAddress address, int port) {
            if (name.isBlank() || type.isBlank() || name.length() > 64 || type.length() > 64 || port < 1 || port > 65535)
                throw new IllegalArgumentException("Nome, tipo o porta servizio non validi");
            this.name = name; this.type = type; this.address = address; this.port = port;
        }
        public String getServiceName() { return name; }
        public String getServiceType() { return type; }
        public InetAddress getAddress() { return address; }
        public int getPort() { return port; }
        public boolean isExpired() { return System.nanoTime() - receivedAt > EXPIRY_NANOS; }
        public String key() { return name.length() + ":" + name + type.length() + ":" + type + "|" + address.getHostAddress() + "|" + port; }
        public String toAnnouncementString() {
            return "ANNOUNCE|" + URLEncoder.encode(name, StandardCharsets.UTF_8) + "|" + URLEncoder.encode(type, StandardCharsets.UTF_8) + "|" + port;
        }
        public static ServiceInfo parse(String text, InetAddress sender) {
            String[] parts = text.split("\\|", -1);
            if (parts.length != 4 || !parts[0].equals("ANNOUNCE")) return null;
            try { return new ServiceInfo(URLDecoder.decode(parts[1], StandardCharsets.UTF_8), URLDecoder.decode(parts[2], StandardCharsets.UTF_8), sender, Integer.parseInt(parts[3])); }
            catch (IllegalArgumentException e) { return null; }
        }
        @Override public String toString() { return name + " (" + type + ") @ " + address.getHostAddress() + ":" + port; }
    }
    public static class ServiceAnnouncer implements AutoCloseable {
        private final List<ServiceInfo> services = new CopyOnWriteArrayList<>();
        private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        private MulticastSocket socket;
        public void addService(ServiceInfo service) { services.add(service); }
        public void start() throws IOException {
            socket = new MulticastSocket();
            try {
                socket.setNetworkInterface(MulticastSupport.networkInterface());
                socket.setTimeToLive(1); socket.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
                InetAddress group = MulticastSupport.group(GROUP);
                executor.scheduleAtFixedRate(() -> {
                    for (ServiceInfo service : services) {
                        byte[] data = service.toAnnouncementString().getBytes(StandardCharsets.UTF_8);
                        try { socket.send(new DatagramPacket(data, data.length, group, PORT)); }
                        catch (IOException e) { if (!socket.isClosed()) System.err.println(e.getMessage()); }
                    }
                }, 0, 5, TimeUnit.SECONDS);
                System.out.println("PRONTO announcer " + GROUP + ":" + PORT);
            } catch (IOException | RuntimeException e) { close(); throw e; }
        }
        public void stop() { close(); }
        @Override public void close() { executor.shutdownNow(); if (socket != null) socket.close(); }
    }
    public static class ServiceDiscoverer implements AutoCloseable {
        private final ConcurrentMap<String, ServiceInfo> services = new ConcurrentHashMap<>();
        private final ScheduledExecutorService cleanup = Executors.newSingleThreadScheduledExecutor();
        private volatile boolean running = true;
        private MulticastSocket socket;
        private Thread receiver;
        public void start() throws IOException {
            InetAddress group = MulticastSupport.group(GROUP);
            NetworkInterface ni = MulticastSupport.networkInterface();
            socket = MulticastSupport.receiver(group, PORT, ni);
            receiver = new Thread(() -> {
                byte[] data = new byte[1401];
                try {
                    while (running) {
                        DatagramPacket packet = new DatagramPacket(data, data.length);
                        socket.receive(packet);
                        if (packet.getLength() > 1400) continue;
                        ServiceInfo service = ServiceInfo.parse(new String(data, 0, packet.getLength(), StandardCharsets.UTF_8), packet.getAddress());
                        if (service != null) {
                            services.put(service.key(), service);
                            System.out.println("SERVIZIO " + service);
                        }
                    }
                } catch (IOException e) { if (running) System.err.println(e.getMessage()); }
                finally { socket.close(); }
            }, "MulticastDiscovery");
            receiver.start();
            cleanup.scheduleAtFixedRate(() -> {
                for (Map.Entry<String, ServiceInfo> entry : services.entrySet()) {
                    if (entry.getValue().isExpired() && services.remove(entry.getKey(), entry.getValue()))
                        System.out.println("SCADUTO " + entry.getValue());
                }
            }, 1, 1, TimeUnit.SECONDS);
            System.out.println("PRONTO discoverer " + GROUP + ":" + PORT + " interfaccia=" + ni.getName());
        }
        public Collection<ServiceInfo> getDiscoveredServices() { return new ArrayList<>(services.values()); }
        public void stop() { close(); }
        @Override public void close() {
            running = false; cleanup.shutdownNow();
            if (socket != null) socket.close(); // Chiudere il socket sblocca receive.
            if (receiver != null && receiver != Thread.currentThread()) try { receiver.join(1500); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("java ServiceDiscoveryMulticast discoverer [secondi] | announcer <nome> <tipo> <porta> [secondi] | both <nome> <tipo> <porta> [secondi]");
            return;
        }
        String mode = args[0];
        if (!mode.equals("discoverer") && !mode.equals("announcer") && !mode.equals("both")) throw new IllegalArgumentException("Modalità non valida");
        if (!mode.equals("discoverer") && args.length < 4) throw new IllegalArgumentException("Nome, tipo e porta richiesti");
        long seconds = mode.equals("discoverer") ? (args.length > 1 ? Long.parseLong(args[1]) : 0) : (args.length > 4 ? Long.parseLong(args[4]) : 0);
        if (seconds < 0) throw new IllegalArgumentException("Durata non negativa");
        try (ServiceDiscoverer discoverer = new ServiceDiscoverer(); ServiceAnnouncer announcer = new ServiceAnnouncer()) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> { discoverer.close(); announcer.close(); }));
            if (!mode.equals("announcer")) discoverer.start();
            if (!mode.equals("discoverer")) {
                announcer.addService(new ServiceInfo(args[1], args[2], InetAddress.getLoopbackAddress(), Integer.parseInt(args[3])));
                announcer.start(); // L'indirizzo usato nella cache è quello sorgente del datagramma.
            }
            if (seconds == 0) new CountDownLatch(1).await();
            else TimeUnit.SECONDS.sleep(seconds);
        }
    }
}
