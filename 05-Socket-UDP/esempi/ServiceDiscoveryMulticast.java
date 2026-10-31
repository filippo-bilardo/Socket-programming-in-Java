/**
 * Nome dell'Esempio: Service Discovery Multicast
 * Guida di Riferimento: 02-Broadcast-Multicast.md
 * 
 * Obiettivo: Implementare un sistema di service discovery usando multicast UDP.
 * 
 * Spiegazione:
 * 1. Server che annuncia periodicamente i propri servizi
 * 2. Client che scoprono automaticamente servizi disponibili
 * 3. Protocollo strutturato per annunci e query
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

public class ServiceDiscoveryMulticast {
    
    // Configurazione multicast
    private static final String MULTICAST_GROUP = "239.1.2.3";
    private static final int MULTICAST_PORT = 8890;
    private static final int ANNOUNCEMENT_INTERVAL = 5000; // 5 secondi
    private static final int SERVICE_TIMEOUT = 15000; // 15 secondi
    
    /**
     * Informazioni su un servizio
     */
    public static class ServiceInfo {
        private final String serviceName;
        private final String serviceType;
        private final InetAddress address;
        private final int port;
        private final Map<String, String> properties;
        private final long timestamp;
        
        public ServiceInfo(String serviceName, String serviceType, 
                          InetAddress address, int port, Map<String, String> properties) {
            this.serviceName = serviceName;
            this.serviceType = serviceType;
            this.address = address;
            this.port = port;
            this.properties = new HashMap<>(properties);
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters
        public String getServiceName() { return serviceName; }
        public String getServiceType() { return serviceType; }
        public InetAddress getAddress() { return address; }
        public int getPort() { return port; }
        public Map<String, String> getProperties() { return new HashMap<>(properties); }
        public long getTimestamp() { return timestamp; }
        
        public boolean isExpired() {
            return (System.currentTimeMillis() - timestamp) > SERVICE_TIMEOUT;
        }
        
        // Serializzazione per multicast
        public String toAnnouncementString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ANNOUNCE|")
              .append(serviceName).append("|")
              .append(serviceType).append("|")
              .append(address.getHostAddress()).append("|")
              .append(port).append("|")
              .append(timestamp);
            
            // Aggiungi proprietà
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                sb.append("|").append(entry.getKey()).append("=").append(entry.getValue());
            }
            
            return sb.toString();
        }
        
        public static ServiceInfo fromAnnouncementString(String announcement) {
            String[] parts = announcement.split("\\|");
            if (parts.length < 6 || !"ANNOUNCE".equals(parts[0])) {
                return null;
            }
            
            try {
                String serviceName = parts[1];
                String serviceType = parts[2];
                InetAddress address = InetAddress.getByName(parts[3]);
                int port = Integer.parseInt(parts[4]);
                
                Map<String, String> properties = new HashMap<>();
                for (int i = 6; i < parts.length; i++) {
                    String[] prop = parts[i].split("=", 2);
                    if (prop.length == 2) {
                        properties.put(prop[0], prop[1]);
                    }
                }
                
                return new ServiceInfo(serviceName, serviceType, address, port, properties);
                
            } catch (Exception e) {
                System.err.println("⚠️ Errore parsing annuncio: " + announcement);
                return null;
            }
        }
        
        @Override
        public String toString() {
            return String.format("%s (%s) @ %s:%d - %s", 
                               serviceName, serviceType, 
                               address.getHostAddress(), port, 
                               properties);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ServiceInfo)) return false;
            
            ServiceInfo other = (ServiceInfo) obj;
            return Objects.equals(serviceName, other.serviceName) &&
                   Objects.equals(address, other.address) &&
                   port == other.port;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(serviceName, address, port);
        }
    }
    
    /**
     * Server che annuncia i propri servizi
     */
    public static class ServiceAnnouncer {
        private final List<ServiceInfo> services = new ArrayList<>();
        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private volatile boolean running = false;
        private MulticastSocket socket;
        private InetAddress group;
        
        public void addService(ServiceInfo service) {
            synchronized (services) {
                services.add(service);
            }
            System.out.println("📢 Servizio aggiunto: " + service.getServiceName());
        }
        
        public void removeService(String serviceName) {
            synchronized (services) {
                services.removeIf(s -> s.getServiceName().equals(serviceName));
            }
            System.out.println("🗑️ Servizio rimosso: " + serviceName);
        }
        
        public void start() throws IOException {
            socket = new MulticastSocket();
            group = InetAddress.getByName(MULTICAST_GROUP);
            
            // Configura multicast
            socket.setTimeToLive(32); // Propagazione locale/regionale
            socket.setLoopbackMode(false); // Ricevi i tuoi annunci (per test)
            
            running = true;
            
            System.out.println("📡 Service Announcer avviato");
            System.out.println("   Gruppo multicast: " + MULTICAST_GROUP + ":" + MULTICAST_PORT);
            System.out.println("   Intervallo annunci: " + (ANNOUNCEMENT_INTERVAL/1000) + "s");
            System.out.println("=" .repeat(50));
            
            // Avvia thread per annunci periodici
            executor.submit(this::announcementLoop);
        }
        
        public void stop() {
            running = false;
            
            if (socket != null) {
                socket.close();
            }
            
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            System.out.println("✅ Service Announcer arrestato");
        }
        
        private void announcementLoop() {
            while (running) {
                try {
                    synchronized (services) {
                        for (ServiceInfo service : services) {
                            sendAnnouncement(service);
                        }
                    }
                    
                    Thread.sleep(ANNOUNCEMENT_INTERVAL);
                    
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    System.err.println("💥 Errore invio annunci: " + e.getMessage());
                    try {
                        Thread.sleep(1000); // Pausa prima di retry
                    } catch (InterruptedException ie) {
                        break;
                    }
                }
            }
        }
        
        private void sendAnnouncement(ServiceInfo service) throws IOException {
            String announcement = service.toAnnouncementString();
            byte[] data = announcement.getBytes();
            
            DatagramPacket packet = new DatagramPacket(
                data, data.length, group, MULTICAST_PORT);
            
            socket.send(packet);
            
            String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            System.out.println("[" + timeStr + "] 📢 Annunciato: " + service.getServiceName());
        }
    }
    
    /**
     * Client che scopre servizi sulla rete
     */
    public static class ServiceDiscoverer {
        private final Map<String, ServiceInfo> discoveredServices = new ConcurrentHashMap<>();
        private final ExecutorService executor = Executors.newFixedThreadPool(2);
        private volatile boolean running = false;
        private MulticastSocket socket;
        
        public void start() throws IOException {
            socket = new MulticastSocket(MULTICAST_PORT);
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            socket.joinGroup(group);
            
            running = true;
            
            System.out.println("🔍 Service Discoverer avviato");
            System.out.println("   Ascolto gruppo: " + MULTICAST_GROUP + ":" + MULTICAST_PORT);
            System.out.println("   Timeout servizi: " + (SERVICE_TIMEOUT/1000) + "s");
            System.out.println("=" .repeat(50));
            
            // Thread per ricezione annunci
            executor.submit(this::discoveryLoop);
            
            // Thread per pulizia servizi scaduti
            executor.submit(this::cleanupLoop);
        }
        
        public void stop() throws IOException {
            running = false;
            
            if (socket != null) {
                InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
                socket.leaveGroup(group);
                socket.close();
            }
            
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            System.out.println("✅ Service Discoverer arrestato");
        }
        
        public Collection<ServiceInfo> getDiscoveredServices() {
            return new ArrayList<>(discoveredServices.values());
        }
        
        public List<ServiceInfo> findServicesByType(String serviceType) {
            return discoveredServices.values().stream()
                    .filter(s -> serviceType.equals(s.getServiceType()))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        public ServiceInfo findServiceByName(String serviceName) {
            return discoveredServices.get(serviceName);
        }
        
        private void discoveryLoop() {
            byte[] buffer = new byte[2048];
            
            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    
                    String message = new String(packet.getData(), 0, packet.getLength());
                    processAnnouncement(message, packet.getAddress());
                    
                } catch (IOException e) {
                    if (running) {
                        System.err.println("💥 Errore ricezione: " + e.getMessage());
                    }
                }
            }
        }
        
        private void processAnnouncement(String announcement, InetAddress sender) {
            ServiceInfo service = ServiceInfo.fromAnnouncementString(announcement);
            
            if (service != null) {
                String key = service.getServiceName();
                ServiceInfo existing = discoveredServices.get(key);
                
                if (existing == null) {
                    // Nuovo servizio
                    discoveredServices.put(key, service);
                    String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    System.out.println("[" + timeStr + "] 🎯 Nuovo servizio: " + service.getServiceName() + 
                                     " (" + service.getServiceType() + ") @ " + 
                                     service.getAddress().getHostAddress() + ":" + service.getPort());
                } else {
                    // Aggiorna timestamp del servizio esistente
                    discoveredServices.put(key, service);
                }
            }
        }
        
        private void cleanupLoop() {
            while (running) {
                try {
                    // Ogni 10 secondi pulisci servizi scaduti
                    Thread.sleep(10000);
                    
                    Iterator<Map.Entry<String, ServiceInfo>> iterator = 
                        discoveredServices.entrySet().iterator();
                    
                    while (iterator.hasNext()) {
                        Map.Entry<String, ServiceInfo> entry = iterator.next();
                        if (entry.getValue().isExpired()) {
                            iterator.remove();
                            System.out.println("⏰ Servizio scaduto: " + entry.getKey());
                        }
                    }
                    
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
        
        public void printDiscoveredServices() {
            Collection<ServiceInfo> services = getDiscoveredServices();
            
            System.out.println("\n📋 SERVIZI SCOPERTI (" + services.size() + "):");
            if (services.isEmpty()) {
                System.out.println("   Nessun servizio trovato");
            } else {
                for (ServiceInfo service : services) {
                    long age = (System.currentTimeMillis() - service.getTimestamp()) / 1000;
                    System.out.println("   • " + service.getServiceName() + 
                                     " (" + service.getServiceType() + ") @ " +
                                     service.getAddress().getHostAddress() + ":" + service.getPort() +
                                     " - età: " + age + "s");
                }
            }
            System.out.println();
        }
    }
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("🔍 Service Discovery Multicast");
            System.out.println("Utilizzo:");
            System.out.println("  java ServiceDiscoveryMulticast announcer <service-name> <service-type> <port> [properties...]");
            System.out.println("  java ServiceDiscoveryMulticast discoverer");
            System.out.println("  java ServiceDiscoveryMulticast both <service-name> <service-type> <port>");
            System.out.println();
            System.out.println("Esempi:");
            System.out.println("  java ServiceDiscoveryMulticast announcer WebServer HTTP 8080 version=1.0 ssl=true");
            System.out.println("  java ServiceDiscoveryMulticast discoverer");
            System.out.println("  java ServiceDiscoveryMulticast both ChatServer TCP 9999");
            return;
        }
        
        String mode = args[0].toLowerCase();
        
        try {
            if ("announcer".equals(mode)) {
                if (args.length < 4) {
                    System.err.println("❌ Parametri insufficienti per announcer");
                    return;
                }
                
                String serviceName = args[1];
                String serviceType = args[2];
                int port = Integer.parseInt(args[3]);
                
                // Parse proprietà opzionali
                Map<String, String> properties = new HashMap<>();
                for (int i = 4; i < args.length; i++) {
                    String[] prop = args[i].split("=", 2);
                    if (prop.length == 2) {
                        properties.put(prop[0], prop[1]);
                    }
                }
                
                ServiceInfo service = new ServiceInfo(serviceName, serviceType, 
                                                    InetAddress.getLocalHost(), port, properties);
                
                ServiceAnnouncer announcer = new ServiceAnnouncer();
                announcer.addService(service);
                announcer.start();
                
                // Shutdown hook
                Runtime.getRuntime().addShutdownHook(new Thread(announcer::stop));
                
                // Keep alive
                Thread.currentThread().join();
                
            } else if ("discoverer".equals(mode)) {
                ServiceDiscoverer discoverer = new ServiceDiscoverer();
                discoverer.start();
                
                // Shutdown hook
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        discoverer.stop();
                    } catch (IOException e) {
                        System.err.println("Errore chiusura discoverer: " + e.getMessage());
                    }
                }));
                
                // Interfaccia interattiva
                Scanner scanner = new Scanner(System.in);
                System.out.println("Comandi: list, find <type>, quit");
                
                String command;
                while (!(command = scanner.nextLine().trim()).equals("quit")) {
                    if ("list".equals(command)) {
                        discoverer.printDiscoveredServices();
                    } else if (command.startsWith("find ")) {
                        String type = command.substring(5);
                        List<ServiceInfo> services = discoverer.findServicesByType(type);
                        System.out.println("Servizi tipo '" + type + "': " + services.size());
                        for (ServiceInfo service : services) {
                            System.out.println("  " + service);
                        }
                    } else {
                        System.out.println("Comando non riconosciuto. Usa: list, find <type>, quit");
                    }
                }
                
            } else if ("both".equals(mode)) {
                if (args.length < 4) {
                    System.err.println("❌ Parametri insufficienti");
                    return;
                }
                
                String serviceName = args[1];
                String serviceType = args[2];
                int port = Integer.parseInt(args[3]);
                
                // Avvia sia announcer che discoverer
                ServiceInfo service = new ServiceInfo(serviceName, serviceType, 
                                                    InetAddress.getLocalHost(), port, 
                                                    Collections.singletonMap("mode", "both"));
                
                ServiceAnnouncer announcer = new ServiceAnnouncer();
                ServiceDiscoverer discoverer = new ServiceDiscoverer();
                
                announcer.addService(service);
                announcer.start();
                discoverer.start();
                
                // Shutdown hooks
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    announcer.stop();
                    try {
                        discoverer.stop();
                    } catch (IOException e) {
                        System.err.println("Errore chiusura: " + e.getMessage());
                    }
                }));
                
                // Stats periodiche
                while (true) {
                    Thread.sleep(30000); // Ogni 30 secondi
                    discoverer.printDiscoveredServices();
                }
                
            } else {
                System.err.println("❌ Modalità non riconosciuta: " + mode);
            }
            
        } catch (Exception e) {
            System.err.println("💥 Errore: " + e.getMessage());
            e.printStackTrace();
        }
    }
}