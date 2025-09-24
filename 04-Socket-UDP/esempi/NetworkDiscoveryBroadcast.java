/**
 * Nome dell'Esempio: Network Discovery Broadcast
 * Guida di Riferimento: 02-Broadcast-Multicast.md
 * 
 * Obiettivo: Sistema di discovery di dispositivi sulla rete locale tramite broadcast UDP.
 * 
 * Spiegazione:
 * 1. Scanner che invia broadcast per scoprire dispositivi
 * 2. Responder che risponde ai ping di discovery
 * 3. Mappa automatica della topologia di rete
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

public class NetworkDiscoveryBroadcast {
    
    // Configurazione discovery
    private static final int DISCOVERY_PORT = 8891;
    private static final int SCANNER_TIMEOUT = 2000; // 2 secondi
    private static final int RESPONDER_TIMEOUT = 30000; // 30 secondi
    private static final String DISCOVERY_MESSAGE = "DISCOVER";
    private static final String RESPONSE_PREFIX = "DEVICE";
    
    /**
     * Informazioni su un dispositivo scoperto
     */
    public static class DeviceInfo {
        private final InetAddress address;
        private final String hostname;
        private final String osName;
        private final String javaVersion;
        private final long discoveryTime;
        private final long responseTimeMs;
        private final Map<String, String> properties;
        
        public DeviceInfo(InetAddress address, String hostname, String osName, 
                         String javaVersion, long responseTimeMs) {
            this.address = address;
            this.hostname = hostname;
            this.osName = osName;
            this.javaVersion = javaVersion;
            this.responseTimeMs = responseTimeMs;
            this.discoveryTime = System.currentTimeMillis();
            this.properties = new HashMap<>();
        }
        
        public void addProperty(String key, String value) {
            properties.put(key, value);
        }
        
        // Getters
        public InetAddress getAddress() { return address; }
        public String getHostname() { return hostname; }
        public String getOsName() { return osName; }
        public String getJavaVersion() { return javaVersion; }
        public long getResponseTimeMs() { return responseTimeMs; }
        public long getDiscoveryTime() { return discoveryTime; }
        public Map<String, String> getProperties() { return new HashMap<>(properties); }
        
        // Serializzazione per risposta
        public String toResponseString() {
            StringBuilder sb = new StringBuilder();
            sb.append(RESPONSE_PREFIX).append("|")
              .append(hostname).append("|")
              .append(osName).append("|")
              .append(javaVersion);
            
            // Aggiungi proprietà custom
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                sb.append("|").append(entry.getKey()).append("=").append(entry.getValue());
            }
            
            return sb.toString();
        }
        
        public static DeviceInfo fromResponseString(String response, InetAddress address, long responseTime) {
            String[] parts = response.split("\\|");
            if (parts.length < 4 || !RESPONSE_PREFIX.equals(parts[0])) {
                return null;
            }
            
            try {
                String hostname = parts[1];
                String osName = parts[2];
                String javaVersion = parts[3];
                
                DeviceInfo device = new DeviceInfo(address, hostname, osName, javaVersion, responseTime);
                
                // Parse proprietà aggiuntive
                for (int i = 4; i < parts.length; i++) {
                    String[] prop = parts[i].split("=", 2);
                    if (prop.length == 2) {
                        device.addProperty(prop[0], prop[1]);
                    }
                }
                
                return device;
                
            } catch (Exception e) {
                System.err.println("⚠️ Errore parsing risposta: " + response);
                return null;
            }
        }
        
        @Override
        public String toString() {
            return String.format("%s (%s) - %s - Java %s - %dms", 
                               hostname, address.getHostAddress(), 
                               osName, javaVersion, responseTimeMs);
        }
        
        public String toDetailedString() {
            StringBuilder sb = new StringBuilder();
            sb.append("🖥️ ").append(hostname).append(" (").append(address.getHostAddress()).append(")\n");
            sb.append("   OS: ").append(osName).append("\n");
            sb.append("   Java: ").append(javaVersion).append("\n");
            sb.append("   Latenza: ").append(responseTimeMs).append(" ms\n");
            sb.append("   Scoperto: ").append(
                LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(discoveryTime), 
                                      java.time.ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            
            if (!properties.isEmpty()) {
                sb.append("\n   Proprietà:");
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    sb.append("\n     ").append(entry.getKey()).append(": ").append(entry.getValue());
                }
            }
            
            return sb.toString();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof DeviceInfo)) return false;
            DeviceInfo other = (DeviceInfo) obj;
            return Objects.equals(address, other.address);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(address);
        }
    }
    
    /**
     * Scanner che invia broadcast per scoprire dispositivi
     */
    public static class NetworkScanner {
        private final Map<InetAddress, DeviceInfo> discoveredDevices = new ConcurrentHashMap<>();
        
        public List<DeviceInfo> scanNetwork() throws IOException {
            return scanNetwork(null);
        }
        
        public List<DeviceInfo> scanNetwork(String targetNetwork) throws IOException {
            discoveredDevices.clear();
            
            System.out.println("🔍 Scansione rete in corso...");
            if (targetNetwork != null) {
                System.out.println("   Target network: " + targetNetwork);
            } else {
                System.out.println("   Broadcast globale (255.255.255.255)");
            }
            
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);
                socket.setSoTimeout(SCANNER_TIMEOUT);
                
                // Invia broadcast discovery
                sendDiscoveryBroadcast(socket, targetNetwork);
                
                // Ricevi risposte
                receiveResponses(socket);
                
                System.out.println("✅ Scansione completata - " + discoveredDevices.size() + " dispositivi trovati");
                
                return new ArrayList<>(discoveredDevices.values());
            }
        }
        
        private void sendDiscoveryBroadcast(DatagramSocket socket, String targetNetwork) throws IOException {
            List<InetAddress> broadcastAddresses = new ArrayList<>();
            
            if (targetNetwork != null) {
                // Broadcast diretto alla subnet specifica
                broadcastAddresses.add(InetAddress.getByName(calculateBroadcastAddress(targetNetwork)));
            } else {
                // Broadcast su tutte le interfaccie locali
                broadcastAddresses.addAll(getAllBroadcastAddresses());
                // Aggiungi anche broadcast limitato
                broadcastAddresses.add(InetAddress.getByName("255.255.255.255"));
            }
            
            byte[] discoveryData = DISCOVERY_MESSAGE.getBytes();
            
            for (InetAddress broadcastAddr : broadcastAddresses) {
                try {
                    DatagramPacket packet = new DatagramPacket(
                        discoveryData, discoveryData.length, broadcastAddr, DISCOVERY_PORT);
                    
                    socket.send(packet);
                    System.out.println("📡 Broadcast inviato a: " + broadcastAddr.getHostAddress());
                    
                } catch (IOException e) {
                    System.err.println("⚠️ Errore invio a " + broadcastAddr + ": " + e.getMessage());
                }
            }
        }
        
        private void receiveResponses(DatagramSocket socket) {
            byte[] buffer = new byte[2048];
            long scanStartTime = System.currentTimeMillis();
            
            System.out.println("👂 In ascolto di risposte...");
            
            while (System.currentTimeMillis() - scanStartTime < SCANNER_TIMEOUT) {
                try {
                    DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                    long sendTime = System.currentTimeMillis();
                    
                    socket.receive(responsePacket);
                    long receiveTime = System.currentTimeMillis();
                    long responseTime = receiveTime - sendTime;
                    
                    String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
                    InetAddress deviceAddress = responsePacket.getAddress();
                    
                    DeviceInfo device = DeviceInfo.fromResponseString(response, deviceAddress, responseTime);
                    if (device != null) {
                        discoveredDevices.put(deviceAddress, device);
                        System.out.println("🎯 Dispositivo trovato: " + device.getHostname() + 
                                         " (" + deviceAddress.getHostAddress() + ") - " + responseTime + "ms");
                    }
                    
                } catch (SocketTimeoutException e) {
                    // Normale - continua ad ascoltare fino al timeout totale
                } catch (IOException e) {
                    System.err.println("💥 Errore ricezione: " + e.getMessage());
                }
            }
        }
        
        private List<InetAddress> getAllBroadcastAddresses() {
            List<InetAddress> broadcastAddresses = new ArrayList<>();
            
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                
                while (interfaces.hasMoreElements()) {
                    NetworkInterface ni = interfaces.nextElement();
                    
                    if (ni.isUp() && !ni.isLoopback()) {
                        for (InterfaceAddress addr : ni.getInterfaceAddresses()) {
                            InetAddress broadcast = addr.getBroadcast();
                            if (broadcast != null) {
                                broadcastAddresses.add(broadcast);
                            }
                        }
                    }
                }
                
            } catch (SocketException e) {
                System.err.println("⚠️ Errore enumerazione interfacce: " + e.getMessage());
            }
            
            return broadcastAddresses;
        }
        
        private String calculateBroadcastAddress(String network) {
            // Semplificazione per subnet /24
            String[] parts = network.split("\\.");
            if (parts.length >= 3) {
                return parts[0] + "." + parts[1] + "." + parts[2] + ".255";
            }
            return "255.255.255.255";
        }
        
        public void printDiscoveredDevices() {
            Collection<DeviceInfo> devices = discoveredDevices.values();
            
            System.out.println("\n📋 DISPOSITIVI SCOPERTI (" + devices.size() + "):");
            if (devices.isEmpty()) {
                System.out.println("   Nessun dispositivo trovato");
            } else {
                List<DeviceInfo> sortedDevices = new ArrayList<>(devices);
                sortedDevices.sort(Comparator.comparing(d -> d.getAddress().getHostAddress()));
                
                for (DeviceInfo device : sortedDevices) {
                    System.out.println("   " + device);
                }
            }
            System.out.println();
        }
        
        public void printDetailedDeviceInfo() {
            Collection<DeviceInfo> devices = discoveredDevices.values();
            
            System.out.println("\n📋 INFORMAZIONI DETTAGLIATE:");
            if (devices.isEmpty()) {
                System.out.println("   Nessun dispositivo trovato");
            } else {
                List<DeviceInfo> sortedDevices = new ArrayList<>(devices);
                sortedDevices.sort(Comparator.comparing(d -> d.getAddress().getHostAddress()));
                
                for (DeviceInfo device : sortedDevices) {
                    System.out.println(device.toDetailedString());
                    System.out.println();
                }
            }
        }
    }
    
    /**
     * Responder che ascolta broadcast e risponde con info del dispositivo
     */
    public static class NetworkResponder {
        private volatile boolean running = false;
        private DatagramSocket socket;
        private final DeviceInfo localDeviceInfo;
        
        public NetworkResponder() {
            // Crea informazioni del dispositivo locale
            Map<String, String> properties = new HashMap<>();
            properties.put("availableProcessors", String.valueOf(Runtime.getRuntime().availableProcessors()));
            properties.put("maxMemory", String.valueOf(Runtime.getRuntime().maxMemory() / (1024*1024)) + "MB");
            properties.put("timestamp", String.valueOf(System.currentTimeMillis()));
            
            try {
                properties.put("localHostname", InetAddress.getLocalHost().getHostName());
            } catch (UnknownHostException e) {
                properties.put("localHostname", "unknown");
            }
            
            this.localDeviceInfo = new DeviceInfo(
                null, // Address will be set per response
                getSystemProperty("user.name", "unknown-user"),
                System.getProperty("os.name", "Unknown OS") + " " + System.getProperty("os.version", ""),
                System.getProperty("java.version", "Unknown Java"),
                0 // Response time not applicable for local device
            );
            
            // Aggiungi proprietà custom
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                localDeviceInfo.addProperty(entry.getKey(), entry.getValue());
            }
        }
        
        public void start() throws IOException {
            socket = new DatagramSocket(DISCOVERY_PORT);
            socket.setBroadcast(true);
            socket.setSoTimeout(RESPONDER_TIMEOUT);
            
            running = true;
            
            System.out.println("👂 Network Responder avviato");
            System.out.println("   Porta: " + DISCOVERY_PORT);
            System.out.println("   Device: " + localDeviceInfo.getHostname());
            System.out.println("   OS: " + localDeviceInfo.getOsName());
            System.out.println("🛑 Premi Ctrl+C per fermare");
            System.out.println("=" .repeat(50));
            
            listenForDiscovery();
        }
        
        public void stop() {
            running = false;
            
            if (socket != null) {
                socket.close();
            }
            
            System.out.println("✅ Network Responder arrestato");
        }
        
        private void listenForDiscovery() {
            byte[] buffer = new byte[1024];
            
            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    
                    String message = new String(packet.getData(), 0, packet.getLength());
                    
                    if (DISCOVERY_MESSAGE.equals(message.trim())) {
                        InetAddress requester = packet.getAddress();
                        int requesterPort = packet.getPort();
                        
                        System.out.println("📡 Discovery request da: " + requester.getHostAddress() + 
                                         ":" + requesterPort);
                        
                        // Invia risposta
                        sendResponse(requester, requesterPort);
                    }
                    
                } catch (SocketTimeoutException e) {
                    // Normale - continua ad ascoltare
                } catch (IOException e) {
                    if (running) {
                        System.err.println("💥 Errore ricezione: " + e.getMessage());
                    }
                }
            }
        }
        
        private void sendResponse(InetAddress requester, int requesterPort) throws IOException {
            String response = localDeviceInfo.toResponseString();
            byte[] responseData = response.getBytes();
            
            DatagramPacket responsePacket = new DatagramPacket(
                responseData, responseData.length, requester, requesterPort);
            
            socket.send(responsePacket);
            
            String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            System.out.println("[" + timeStr + "] 📤 Risposta inviata a: " + 
                             requester.getHostAddress());
        }
        
        private String getSystemProperty(String key, String defaultValue) {
            try {
                return System.getProperty(key, defaultValue);
            } catch (Exception e) {
                return defaultValue;
            }
        }
    }
    
    /**
     * Continuous scanner per monitoring rete
     */
    public static class ContinuousScanner {
        private final NetworkScanner scanner = new NetworkScanner();
        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        private final Map<InetAddress, DeviceInfo> lastSeen = new ConcurrentHashMap<>();
        private volatile boolean running = false;
        
        public void startContinuousScanning(int intervalSeconds) {
            running = true;
            
            System.out.println("🔄 Continuous Network Scanning avviato");
            System.out.println("   Intervallo: " + intervalSeconds + " secondi");
            System.out.println("=" .repeat(40));
            
            scheduler.scheduleAtFixedRate(this::performScan, 0, intervalSeconds, TimeUnit.SECONDS);
        }
        
        public void stop() {
            running = false;
            scheduler.shutdown();
            
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            System.out.println("✅ Continuous scanning arrestato");
        }
        
        private void performScan() {
            if (!running) return;
            
            try {
                List<DeviceInfo> currentDevices = scanner.scanNetwork();
                
                // Rileva nuovi dispositivi
                Set<InetAddress> newDevices = new HashSet<>();
                for (DeviceInfo device : currentDevices) {
                    if (!lastSeen.containsKey(device.getAddress())) {
                        newDevices.add(device.getAddress());
                    }
                }
                
                // Rileva dispositivi scomparsi
                Set<InetAddress> lostDevices = new HashSet<>(lastSeen.keySet());
                for (DeviceInfo device : currentDevices) {
                    lostDevices.remove(device.getAddress());
                }
                
                // Aggiorna mappa
                lastSeen.clear();
                for (DeviceInfo device : currentDevices) {
                    lastSeen.put(device.getAddress(), device);
                }
                
                // Notifiche cambiamenti
                for (InetAddress addr : newDevices) {
                    DeviceInfo device = lastSeen.get(addr);
                    System.out.println("🟢 Nuovo dispositivo: " + device.getHostname() + 
                                     " (" + addr.getHostAddress() + ")");
                }
                
                for (InetAddress addr : lostDevices) {
                    System.out.println("🔴 Dispositivo disconnesso: " + addr.getHostAddress());
                }
                
                if (newDevices.isEmpty() && lostDevices.isEmpty()) {
                    String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    System.out.println("[" + timeStr + "] 📊 Scan completato - " + 
                                     currentDevices.size() + " dispositivi attivi");
                }
                
            } catch (IOException e) {
                System.err.println("💥 Errore scan: " + e.getMessage());
            }
        }
        
        public Collection<DeviceInfo> getCurrentDevices() {
            return new ArrayList<>(lastSeen.values());
        }
    }
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("🔍 Network Discovery Broadcast");
            System.out.println("Utilizzo:");
            System.out.println("  java NetworkDiscoveryBroadcast scan [network]");
            System.out.println("  java NetworkDiscoveryBroadcast respond");
            System.out.println("  java NetworkDiscoveryBroadcast monitor <interval-seconds>");
            System.out.println();
            System.out.println("Esempi:");
            System.out.println("  java NetworkDiscoveryBroadcast scan");
            System.out.println("  java NetworkDiscoveryBroadcast scan 192.168.1.0");
            System.out.println("  java NetworkDiscoveryBroadcast respond");
            System.out.println("  java NetworkDiscoveryBroadcast monitor 30");
            return;
        }
        
        String mode = args[0].toLowerCase();
        
        try {
            if ("scan".equals(mode)) {
                NetworkScanner scanner = new NetworkScanner();
                
                String targetNetwork = args.length > 1 ? args[1] : null;
                List<DeviceInfo> devices = scanner.scanNetwork(targetNetwork);
                
                scanner.printDiscoveredDevices();
                
                // Opzione per info dettagliate
                if (!devices.isEmpty()) {
                    System.out.print("Mostrare informazioni dettagliate? (y/N): ");
                    try (Scanner input = new Scanner(System.in)) {
                        String response = input.nextLine().trim().toLowerCase();
                        
                        if ("y".equals(response) || "yes".equals(response)) {
                            scanner.printDetailedDeviceInfo();
                        }
                    }
                }
                
            } else if ("respond".equals(mode)) {
                NetworkResponder responder = new NetworkResponder();
                
                // Shutdown hook
                Runtime.getRuntime().addShutdownHook(new Thread(responder::stop));
                
                responder.start();
                
            } else if ("monitor".equals(mode)) {
                if (args.length < 2) {
                    System.err.println("❌ Intervallo richiesto per monitoring");
                    return;
                }
                
                int interval = Integer.parseInt(args[1]);
                ContinuousScanner monitor = new ContinuousScanner();
                
                // Shutdown hook
                Runtime.getRuntime().addShutdownHook(new Thread(monitor::stop));
                
                monitor.startContinuousScanning(interval);
                
                // Keep alive
                Thread.currentThread().join();
                
            } else {
                System.err.println("❌ Modalità non riconosciuta: " + mode);
            }
            
        } catch (NumberFormatException e) {
            System.err.println("❌ Parametro numerico non valido");
        } catch (Exception e) {
            System.err.println("💥 Errore: " + e.getMessage());
            e.printStackTrace();
        }
    }
}