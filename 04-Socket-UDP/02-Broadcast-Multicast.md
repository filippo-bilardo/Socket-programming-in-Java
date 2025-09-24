# 2. Broadcast e Multicast UDP

## Introduzione
Una delle caratteristiche più potenti di UDP è la capacità di inviare dati a **multipli destinatari contemporaneamente**. Questo modulo esplora le tecniche di broadcast e multicast per comunicazioni efficienti uno-a-molti.

## Teoria

### Tipi di Comunicazione di Rete

#### Unicast (1:1)
```java
// Comunicazione tradizionale punto-a-punto
// Un mittente → Un destinatario
InetAddress target = InetAddress.getByName("192.168.1.100");
DatagramPacket packet = new DatagramPacket(data, data.length, target, port);
```

#### Broadcast (1:Tutti nella subnet)
```java
// Un mittente → Tutti i dispositivi nella rete locale
// Utilizza l'indirizzo di broadcast della subnet
InetAddress broadcast = InetAddress.getByName("192.168.1.255");
DatagramPacket packet = new DatagramPacket(data, data.length, broadcast, port);
```

#### Multicast (1:Gruppo)
```java
// Un mittente → Gruppo specifico di destinatari
// Utilizza indirizzi IP multicast (224.0.0.0 - 239.255.255.255)
InetAddress multicast = InetAddress.getByName("224.0.0.1");
DatagramPacket packet = new DatagramPacket(data, data.length, multicast, port);
```

### Broadcast UDP

#### Broadcast Limitato
```java
// 255.255.255.255 - Broadcast a tutta la rete locale
// Non attraversa router (TTL = 1)

public class BroadcastSender {
    public static void sendBroadcast(String message, int port) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            // Abilita broadcast
            socket.setBroadcast(true);
            
            // Indirizzo broadcast limitato
            InetAddress broadcast = InetAddress.getByName("255.255.255.255");
            
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(
                data, data.length, broadcast, port);
            
            socket.send(packet);
            System.out.println("📡 Broadcast inviato: " + message);
        }
    }
}
```

#### Broadcast Diretto
```java
// Broadcast alla specifica subnet (es. 192.168.1.255)
// Può attraversare router se configurato

public class DirectedBroadcast {
    public static void sendToSubnet(String message, String network, int port) 
            throws IOException {
        
        // Calcola indirizzo broadcast per la subnet
        String broadcastAddress = calculateBroadcastAddress(network);
        
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            
            InetAddress broadcast = InetAddress.getByName(broadcastAddress);
            byte[] data = message.getBytes();
            
            DatagramPacket packet = new DatagramPacket(
                data, data.length, broadcast, port);
            
            socket.send(packet);
            System.out.println("📡 Directed broadcast a " + broadcastAddress);
        }
    }
    
    private static String calculateBroadcastAddress(String network) {
        // Semplificazione per subnet /24
        String[] parts = network.split("\\.");
        return parts[0] + "." + parts[1] + "." + parts[2] + ".255";
    }
}
```

### Multicast UDP

#### Gruppi Multicast
```java
// Indirizzi multicast riservati (RFC 3171):
// 224.0.0.0 - 224.0.0.255    : Controllo di rete locale
// 224.0.1.0 - 238.255.255.255: Multicast globale
// 239.0.0.0 - 239.255.255.255: Multicast amministrativo locale

public class MulticastGroups {
    // Gruppi standard
    public static final String ALL_SYSTEMS = "224.0.0.1";        // Tutti i sistemi
    public static final String ALL_ROUTERS = "224.0.0.2";        // Tutti i router
    public static final String OSPF_ROUTERS = "224.0.0.5";       // Router OSPF
    
    // Gruppi applicativi comuni
    public static final String SDP_SESSION = "224.2.127.254";    // Session Description Protocol
    public static final String UPNP_MULTICAST = "239.255.255.250"; // UPnP Discovery
    
    // Gruppo personalizzato per la tua applicazione
    public static final String CUSTOM_GROUP = "239.1.2.3";
}
```

#### MulticastSocket - Ricezione
```java
import java.net.MulticastSocket;
import java.net.InetAddress;

public class MulticastReceiver {
    private final String groupAddress;
    private final int port;
    private MulticastSocket socket;
    private InetAddress group;
    
    public MulticastReceiver(String groupAddress, int port) {
        this.groupAddress = groupAddress;
        this.port = port;
    }
    
    public void start() throws IOException {
        // Crea socket multicast
        socket = new MulticastSocket(port);
        group = InetAddress.getByName(groupAddress);
        
        // Unisciti al gruppo
        socket.joinGroup(group);
        
        System.out.println("🔗 Unito al gruppo multicast " + groupAddress + 
                          " sulla porta " + port);
        
        byte[] buffer = new byte[1024];
        
        try {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("📨 Multicast ricevuto da " + 
                                 packet.getAddress() + ": " + message);
            }
        } finally {
            stop();
        }
    }
    
    public void stop() throws IOException {
        if (socket != null && group != null) {
            // Lascia il gruppo
            socket.leaveGroup(group);
            socket.close();
            System.out.println("👋 Lasciato gruppo multicast");
        }
    }
}
```

#### MulticastSocket - Invio
```java
public class MulticastSender {
    
    public static void sendToGroup(String message, String groupAddress, int port) 
            throws IOException {
        
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress group = InetAddress.getByName(groupAddress);
            
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(
                data, data.length, group, port);
            
            socket.send(packet);
            System.out.println("📡 Multicast inviato al gruppo " + groupAddress);
        }
    }
    
    // Invio con TTL personalizzato
    public static void sendWithTTL(String message, String groupAddress, 
                                  int port, int ttl) throws IOException {
        
        try (MulticastSocket socket = new MulticastSocket()) {
            // Imposta Time To Live (quanti hop può attraversare)
            socket.setTimeToLive(ttl);
            
            InetAddress group = InetAddress.getByName(groupAddress);
            byte[] data = message.getBytes();
            
            DatagramPacket packet = new DatagramPacket(
                data, data.length, group, port);
            
            socket.send(packet);
            System.out.println("📡 Multicast inviato (TTL=" + ttl + ")");
        }
    }
}
```

## Implementazioni Avanzate

### Service Discovery con Multicast
```java
public class ServiceDiscovery {
    private static final String DISCOVERY_GROUP = "239.1.1.1";
    private static final int DISCOVERY_PORT = 8889;
    
    // Server che annuncia il proprio servizio
    public static class ServiceAnnouncer {
        private final String serviceName;
        private final int servicePort;
        private volatile boolean running = false;
        
        public ServiceAnnouncer(String serviceName, int servicePort) {
            this.serviceName = serviceName;
            this.servicePort = servicePort;
        }
        
        public void startAnnouncing() throws IOException {
            running = true;
            
            try (DatagramSocket socket = new DatagramSocket()) {
                InetAddress group = InetAddress.getByName(DISCOVERY_GROUP);
                
                while (running) {
                    // Annuncio formato: SERVICE:nome:porta:timestamp
                    String announcement = String.format("SERVICE:%s:%d:%d", 
                                                      serviceName, servicePort, 
                                                      System.currentTimeMillis());
                    
                    byte[] data = announcement.getBytes();
                    DatagramPacket packet = new DatagramPacket(
                        data, data.length, group, DISCOVERY_PORT);
                    
                    socket.send(packet);
                    System.out.println("📢 Annunciato: " + serviceName);
                    
                    // Attende 5 secondi prima del prossimo annuncio
                    Thread.sleep(5000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        public void stop() {
            running = false;
        }
    }
    
    // Client che scopre servizi disponibili
    public static class ServiceDiscoverer {
        private final Map<String, ServiceInfo> discoveredServices = 
            new ConcurrentHashMap<>();
        
        public void startDiscovery() throws IOException {
            MulticastSocket socket = new MulticastSocket(DISCOVERY_PORT);
            InetAddress group = InetAddress.getByName(DISCOVERY_GROUP);
            socket.joinGroup(group);
            
            System.out.println("🔍 Ricerca servizi in corso...");
            
            byte[] buffer = new byte[1024];
            
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                String message = new String(packet.getData(), 0, packet.getLength());
                parseServiceAnnouncement(message, packet.getAddress());
            }
        }
        
        private void parseServiceAnnouncement(String message, InetAddress source) {
            try {
                // Parse: SERVICE:nome:porta:timestamp
                String[] parts = message.split(":");
                if (parts.length == 4 && "SERVICE".equals(parts[0])) {
                    String serviceName = parts[1];
                    int port = Integer.parseInt(parts[2]);
                    long timestamp = Long.parseLong(parts[3]);
                    
                    ServiceInfo service = new ServiceInfo(serviceName, source, port, timestamp);
                    discoveredServices.put(serviceName, service);
                    
                    System.out.println("🎯 Servizio scoperto: " + service);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Errore parsing annuncio: " + message);
            }
        }
        
        public Collection<ServiceInfo> getDiscoveredServices() {
            // Rimuovi servizi obsoleti (>30 secondi)
            long cutoff = System.currentTimeMillis() - 30000;
            discoveredServices.entrySet().removeIf(
                entry -> entry.getValue().getTimestamp() < cutoff);
            
            return new ArrayList<>(discoveredServices.values());
        }
    }
    
    public static class ServiceInfo {
        private final String name;
        private final InetAddress address;
        private final int port;
        private final long timestamp;
        
        public ServiceInfo(String name, InetAddress address, int port, long timestamp) {
            this.name = name;
            this.address = address;
            this.port = port;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getName() { return name; }
        public InetAddress getAddress() { return address; }
        public int getPort() { return port; }
        public long getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format("%s @ %s:%d", name, address.getHostAddress(), port);
        }
    }
}
```

### Broadcast Listener per Network Discovery
```java
public class NetworkDiscovery {
    
    public static class BroadcastListener {
        private final int port;
        private volatile boolean running = false;
        
        public BroadcastListener(int port) {
            this.port = port;
        }
        
        public void startListening() throws IOException {
            running = true;
            
            try (DatagramSocket socket = new DatagramSocket(port)) {
                // Abilita ricezione broadcast
                socket.setBroadcast(true);
                
                System.out.println("👂 In ascolto di broadcast sulla porta " + port);
                
                byte[] buffer = new byte[1024];
                
                while (running) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    
                    try {
                        socket.receive(packet);
                        
                        String message = new String(packet.getData(), 0, packet.getLength());
                        InetAddress sender = packet.getAddress();
                        
                        System.out.println("📡 Broadcast da " + sender.getHostAddress() + 
                                         ": " + message);
                        
                        // Rispondi al mittente con le tue informazioni
                        sendResponse(socket, packet);
                        
                    } catch (IOException e) {
                        if (running) {
                            System.err.println("⚠️ Errore ricezione: " + e.getMessage());
                        }
                    }
                }
            }
        }
        
        private void sendResponse(DatagramSocket socket, DatagramPacket originalPacket) 
                throws IOException {
            
            // Risposta con informazioni locali
            String response = "RESPONSE:" + InetAddress.getLocalHost().getHostName() + 
                            ":" + System.currentTimeMillis();
            
            byte[] responseData = response.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(
                responseData, responseData.length,
                originalPacket.getAddress(), originalPacket.getPort());
            
            socket.send(responsePacket);
            System.out.println("📤 Risposta inviata a " + originalPacket.getAddress());
        }
        
        public void stop() {
            running = false;
        }
    }
}
```

## Configurazione e Performance

### TTL (Time To Live)
```java
// Controlla quanti router può attraversare un pacchetto multicast
MulticastSocket socket = new MulticastSocket();

// TTL = 1: Solo rete locale
socket.setTimeToLive(1);

// TTL = 32: Raggiunge altre subnet nella stessa organizzazione  
socket.setTimeToLive(32);

// TTL = 255: Massima propagazione (uso raramente consigliato)
socket.setTimeToLive(255);
```

### Interfacce di Rete Multiple
```java
// Specifica interfaccia per multicast
NetworkInterface networkInterface = NetworkInterface.getByName("eth0");
MulticastSocket socket = new MulticastSocket();
socket.setNetworkInterface(networkInterface);

// Oppure per indirizzo specifico
InetAddress localAddress = InetAddress.getByName("192.168.1.100");
socket.setInterface(localAddress);
```

### Controllo Loop-back
```java
// Disabilita ricezione dei propri pacchetti multicast
MulticastSocket socket = new MulticastSocket();
socket.setLoopbackMode(true); // true = disabilita loopback
```

## Sicurezza e Considerazioni

### Validazione Sorgente
```java
// Sempre validare mittenti nei sistemi broadcast/multicast
public boolean isValidSender(InetAddress sender) {
    // Verifica che sia nella tua subnet
    if (!isInLocalSubnet(sender)) {
        System.err.println("⚠️ Pacchetto da subnet non autorizzata: " + sender);
        return false;
    }
    
    // Altre verifiche di sicurezza...
    return true;
}
```

### Rate Limiting
```java
// Previeni flood di pacchetti broadcast/multicast
public class RateLimiter {
    private final Map<InetAddress, Long> lastMessageTime = new ConcurrentHashMap<>();
    private final long minimumInterval; // ms
    
    public RateLimiter(long minimumIntervalMs) {
        this.minimumInterval = minimumIntervalMs;
    }
    
    public boolean allowMessage(InetAddress sender) {
        long now = System.currentTimeMillis();
        Long lastTime = lastMessageTime.get(sender);
        
        if (lastTime == null || (now - lastTime) >= minimumInterval) {
            lastMessageTime.put(sender, now);
            return true;
        }
        
        return false; // Rate limit exceeded
    }
}
```

## Best Practices

### ✅ Raccomandazioni
1. **Usa TTL appropriato** per limitare propagazione
2. **Implementa timeout** per discovery operations
3. **Valida sempre mittenti** per sicurezza
4. **Limita rate** per prevenire flooding
5. **Gestisci interfacce multiple** correttamente

### ❌ Errori Comuni
1. **Non abilitare broadcast** con `setBroadcast(true)`
2. **TTL troppo alto** che causa traffico eccessivo
3. **Non gestire loop-back** nei test locali
4. **Assumere consegna garantita** anche per broadcast
5. **Non filtrare mittenti** in reti non sicure

## Esempi di Debugging

### Monitor Network Traffic
```java
// Utility per monitorare tutto il traffico multicast
public class MulticastMonitor {
    public static void monitorGroup(String groupAddress, int port) throws IOException {
        MulticastSocket socket = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName(groupAddress);
        socket.joinGroup(group);
        
        System.out.println("🔍 Monitoring gruppo " + groupAddress + ":" + port);
        
        byte[] buffer = new byte[2048];
        
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            
            System.out.printf("[%s] Da %s:%d (%d bytes)%n",
                new java.util.Date(),
                packet.getAddress().getHostAddress(),
                packet.getPort(),
                packet.getLength());
            
            // Mostra contenuto se è testo
            String content = new String(packet.getData(), 0, packet.getLength());
            if (content.matches("[\\x20-\\x7E\\s]*")) { // ASCII stampabile
                System.out.println("  Contenuto: " + content);
            }
        }
    }
}
```

---
[🏠 Torna al Modulo](../README.md) | [⬅️ Lezione Precedente](01-Socket-UDP-Fondamenti.md) | [➡️ Prossima Lezione](03-UDP-Performance.md)