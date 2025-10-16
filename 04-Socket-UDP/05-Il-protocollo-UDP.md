# 📡 Guida 05: Il Protocollo UDP

> *User Datagram Protocol - Velocità e Semplicità*

---

## 📋 **Indice**

1. [Introduzione al UDP](#-introduzione-al-udp)
2. [Caratteristiche del UDP](#-caratteristiche-del-udp)
3. [Architettura UDP](#️-architettura-udp)
4. [UDP vs TCP](#⚡-udp-vs-tcp)
5. [UDP in Java](#☕-udp-in-java)
6. [Esempi Pratici](#-esempi-pratici)
7. [Multicast e Broadcast](#-multicast-e-broadcast)
8. [Performance e Ottimizzazione](#-performance-e-ottimizzazione)
9. [Troubleshooting](#-troubleshooting)
10. [Casi d'Uso Reali](#-casi-duso-reali)

---

## 🎯 **Introduzione al UDP**

### 📖 **Cos'è il UDP**

Il **User Datagram Protocol (UDP)** è un protocollo di trasporto della suite TCP/IP che fornisce:

- ⚡ **Comunicazione veloce** e a basso overhead
- 📦 **Invio immediato** senza setup connessione
- 🎯 **Semplicità** nell'implementazione
- 🔀 **Supporto broadcast/multicast**
- 🚫 **Nessuna garanzia** di consegna

### 🌐 **Posizione nel Stack TCP/IP**

```
┌─────────────────────────┐
│    Applicazione         │ ← DNS, DHCP, SNMP, Games
├─────────────────────────┤
│    Trasporto            │ ← UDP (porta 53, 67, 161)
├─────────────────────────┤
│    Rete (Internet)      │ ← IP (IPv4, IPv6)
├─────────────────────────┤
│    Collegamento         │ ← Ethernet, WiFi
├─────────────────────────┤
│    Fisico               │ ← Cavi, onde radio
└─────────────────────────┘
```

### 🎯 **Quando Utilizzare UDP**

**✅ Usa UDP quando serve:**
- Velocità più importante dell'affidabilità
- Comunicazione broadcast/multicast
- Basso overhead di rete
- Applicazioni real-time
- Tolleranza alla perdita di pacchetti

**Esempi pratici:**
- 🎮 **Gaming online** (posizione, movimento)
- 📹 **Streaming video/audio** (YouTube, Netflix)
- 🌐 **DNS queries** (risoluzione nomi)
- 📡 **IoT sensors** (telemetria)
- 🔄 **DHCP** (configurazione rete)
- 📊 **Monitoring** (SNMP)

---

## ⚡ **Caratteristiche del UDP**

### 🚫 **1. Connectionless**

UDP **NON** stabilisce connessioni:

```
Client                    Server
   │                        │
   │──── DATAGRAM ─────────►│  Invio diretto (no handshake)
   │──── DATAGRAM ─────────►│  Ogni pacchetto indipendente  
   │──── DATAGRAM ─────────►│  Nessun stato connessione
   │                        │
```

**Confronto con TCP:**
```java
// TCP - Richiede connessione
Socket socket = new Socket("host", 8080);  // 3-way handshake
socket.getOutputStream().write(data);       // Poi invia dati

// UDP - Invio diretto
DatagramSocket socket = new DatagramSocket();
DatagramPacket packet = new DatagramPacket(data, data.length, address, 8080);
socket.send(packet);  // Invio immediato, no setup
```

### 📦 **2. Best Effort Delivery**

UDP **non garantisce** consegna:

```
Invio:    [A] [B] [C] [D] [E]
Rete:     [A] [X] [C] [X] [E]  ← [B] e [D] persi
Ricezione:[A]     [C]     [E]  ← UDP non rileva/corregge
```

**Responsabilità applicazione:**
- Rilevare pacchetti persi
- Riordinare se necessario  
- Implementare retry logic
- Gestire duplicati

### ⚡ **3. Velocità e Efficienza**

```
UDP Header: 8 byte
TCP Header: 20-60 byte  ← Molto più pesante

UDP: Nessun ACK, controllo flusso, ritrasmissione
TCP: ACK + controllo flusso + ritrasmissione + riordinamento
```

### 🔢 **4. No Ordinamento**

```java
// Invio sequenziale
send("Messaggio 1");
send("Messaggio 2");  
send("Messaggio 3");

// Possibile ricezione (disordinata)
ricevi("Messaggio 3");
ricevi("Messaggio 1");  
ricevi("Messaggio 2");
```

### 🎯 **5. Message Boundary**

UDP preserva i **confini dei messaggi**:

```java
// Invio
send("Hello");
send("World");

// Ricezione - Due pacchetti separati
String msg1 = receive(); // "Hello"
String msg2 = receive(); // "World"

// TCP invece potrebbe ricevere: "HelloWorld" in un unico read()
```

---

## 🏗️ **Architettura UDP**

### 📋 **Header UDP**

```
 0                   1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
├─────────────────────────────────────────────────────────────────┤
│          Source Port          │       Destination Port          │
├─────────────────────────────────────────────────────────────────┤
│            Length             │           Checksum              │
├─────────────────────────────────────────────────────────────────┤
│                           Data                                  │
│                            ...                                  │
└─────────────────────────────────────────────────────────────────┘
```

#### **Campi Header:**

| Campo | Dimensione | Descrizione |
|-------|------------|-------------|
| **Source Port** | 16 bit | Porta mittente (0-65535) |
| **Destination Port** | 16 bit | Porta destinatario |
| **Length** | 16 bit | Lunghezza UDP header + data |
| **Checksum** | 16 bit | Controllo errori (opzionale IPv4) |

#### **Confronto Header:**
```java
UDP Header = 8 byte fissi
TCP Header = 20-60 byte variabili

Overhead per 1 byte dati:
UDP: 8 byte header / 1 byte data = 800% overhead
TCP: 20+ byte header / 1 byte data = 2000%+ overhead
```

### 🔍 **Checksum UDP**

```java
// Pseudo-header per calcolo checksum
┌─────────────────┬─────────────────┐
│   Source IP     │  Destination IP │  ← Da IP header
├─────────────────┼─────────────────┤
│ 0x00 │ Protocol │     UDP Length  │  ← Protocol = 17
├──────┴──────────┴─────────────────┤
│        UDP Header + Data          │  ← UDP reale
└───────────────────────────────────┘

// In Java il checksum è calcolato automaticamente
```

---

## ⚖️ **UDP vs TCP**

### 📊 **Confronto Dettagliato**

| Caratteristica | UDP | TCP |
|----------------|-----|-----|
| **Connessione** | ❌ Connectionless | ✅ Connection-oriented |
| **Affidabilità** | ❌ Best effort | ✅ Reliable delivery |
| **Ordinamento** | ❌ No ordering | ✅ Ordered delivery |
| **Controllo flusso** | ❌ No flow control | ✅ Flow control |
| **Controllo errori** | ⚠️ Checksum only | ✅ Full error recovery |
| **Velocità** | ⚡ Molto veloce | 🐌 Più lento |
| **Overhead** | 📦 8 byte header | 📦 20+ byte header |
| **Broadcast** | ✅ Supportato | ❌ Non supportato |
| **Multicast** | ✅ Supportato | ❌ Non supportato |
| **Applicazioni** | Gaming, Streaming | Web, Email, File |

### 🎯 **Decision Matrix**

```java
Scegli UDP se:
✅ Velocità > Affidabilità
✅ Real-time applications  
✅ Broadcast/Multicast
✅ Tolleranza perdite dati
✅ Overhead minimo

Scegli TCP se:  
✅ Affidabilità > Velocità
✅ Consegna garantita
✅ Ordinamento dati
✅ Grandi trasferimenti
✅ Applicazioni business-critical
```

### 📈 **Performance Comparison**

```java
// Benchmark semplificato
Test: Invio 10.000 messaggi "Hello World"

UDP Results:
- Tempo: 150ms
- Throughput: 66,666 msg/s
- Banda: ~800 KB/s  
- CPU: 2%

TCP Results:  
- Tempo: 450ms  
- Throughput: 22,222 msg/s
- Banda: ~266 KB/s
- CPU: 8%

UDP è ~3x più veloce per piccoli messaggi
```

---

## ☕ **UDP in Java**

### 🖥️ **Server UDP**

```java
import java.io.*;
import java.net.*;

public class UDPServer {
    private static final int PORT = 8080;
    private static final int BUFFER_SIZE = 1024;
    
    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            System.out.println("Server UDP avviato sulla porta " + PORT);
            
            byte[] buffer = new byte[BUFFER_SIZE];
            
            while (true) {
                // Prepara packet per ricezione
                DatagramPacket receivePacket = new DatagramPacket(
                    buffer, buffer.length);
                
                // Ricevi datagram (BLOCCA fino ad arrivo packet)
                socket.receive(receivePacket);
                
                // Estrai dati ricevuti
                String receivedData = new String(
                    receivePacket.getData(), 0, receivePacket.getLength());
                
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                
                System.out.printf("Ricevuto da %s:%d → %s\n", 
                    clientAddress.getHostAddress(), clientPort, receivedData);
                
                // Prepara risposta (Echo)
                String response = "Echo: " + receivedData;
                byte[] responseData = response.getBytes();
                
                DatagramPacket sendPacket = new DatagramPacket(
                    responseData, responseData.length, 
                    clientAddress, clientPort);
                
                // Invia risposta
                socket.send(sendPacket);
            }
            
        } catch (IOException e) {
            System.err.println("Errore server UDP: " + e.getMessage());
        }
    }
}
```

### 💻 **Client UDP**

```java
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class UDPClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private static final int BUFFER_SIZE = 1024;
    
    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {
            
            InetAddress serverAddress = InetAddress.getByName(HOST);
            System.out.println("Client UDP connesso a " + HOST + ":" + PORT);
            
            String userInput;
            while (true) {
                System.out.print("Messaggio (o 'quit'): ");
                userInput = scanner.nextLine();
                
                if ("quit".equalsIgnoreCase(userInput)) {
                    break;
                }
                
                // Prepara e invia packet
                byte[] sendData = userInput.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length, serverAddress, PORT);
                
                socket.send(sendPacket);
                
                // Prepara buffer per risposta
                byte[] receiveBuffer = new byte[BUFFER_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(
                    receiveBuffer, receiveBuffer.length);
                
                // Ricevi risposta con timeout
                socket.setSoTimeout(5000); // 5 secondi timeout
                socket.receive(receivePacket);
                
                String response = new String(
                    receivePacket.getData(), 0, receivePacket.getLength());
                
                System.out.println("Server: " + response);
            }
            
        } catch (SocketTimeoutException e) {
            System.err.println("Timeout: nessuna risposta dal server");
        } catch (IOException e) {
            System.err.println("Errore client UDP: " + e.getMessage());
        }
        
        System.out.println("Client UDP terminato.");
    }
}
```

### ⚙️ **Configurazioni Socket UDP**

```java
public class UDPConfiguration {
    
    public static void configureUDPSocket(DatagramSocket socket) 
            throws SocketException {
        
        // Buffer dimensioni
        socket.setReceiveBufferSize(64 * 1024);  // 64KB ricezione
        socket.setSendBufferSize(64 * 1024);     // 64KB invio
        
        // Timeout operazioni (importante per UDP!)
        socket.setSoTimeout(10000);  // 10 secondi timeout
        
        // Riuso indirizzo (utile per server)
        socket.setReuseAddress(true);
        
        // Traffic Class (QoS)
        socket.setTrafficClass(0x04); // High throughput
        // socket.setTrafficClass(0x10); // Low delay
        // socket.setTrafficClass(0x08); // High reliability
        
        System.out.println("=== UDP SOCKET CONFIG ===");
        System.out.println("Local port: " + socket.getLocalPort());
        System.out.println("Receive buffer: " + socket.getReceiveBufferSize());
        System.out.println("Send buffer: " + socket.getSendBufferSize());
        System.out.println("SO Timeout: " + socket.getSoTimeout());
    }
}
```

---

## 💡 **Esempi Pratici**

### 🎮 **Game Position Updates**

```java
// Server gioco che gestisce posizioni player
public class GameServer {
    private static Map<String, PlayerPosition> players = new ConcurrentHashMap<>();
    
    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(9999)) {
            System.out.println("Game Server UDP avviato sulla porta 9999");
            
            byte[] buffer = new byte[256];
            
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                String data = new String(packet.getData(), 0, packet.getLength());
                String[] parts = data.split(",");
                
                if (parts.length == 4) {
                    String playerId = parts[0];
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    long timestamp = Long.parseLong(parts[3]);
                    
                    // Aggiorna posizione player
                    players.put(playerId, new PlayerPosition(x, y, timestamp));
                    
                    // Broadcast a tutti gli altri player
                    broadcastPosition(socket, playerId, x, y, timestamp);
                }
            }
        } catch (IOException e) {
            System.err.println("Errore game server: " + e.getMessage());
        }
    }
    
    private static void broadcastPosition(DatagramSocket socket, 
            String playerId, float x, float y, long timestamp) {
        // Implementazione broadcast...
    }
    
    static class PlayerPosition {
        final float x, y;
        final long timestamp;
        
        PlayerPosition(float x, float y, long timestamp) {
            this.x = x; this.y = y; this.timestamp = timestamp;
        }
    }
}

// Client gioco
public class GameClient {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private String playerId;
    
    public void sendPosition(float x, float y) {
        try {
            String data = String.format("%s,%.2f,%.2f,%d", 
                playerId, x, y, System.currentTimeMillis());
            
            byte[] buffer = data.getBytes();
            DatagramPacket packet = new DatagramPacket(
                buffer, buffer.length, serverAddress, 9999);
            
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Errore invio posizione: " + e.getMessage());
        }
    }
}
```

### 📹 **Video Streaming Simulator**

```java
// Simulatore streaming video UDP
public class VideoStreamer {
    private DatagramSocket socket;
    private InetAddress clientAddress;
    private int clientPort;
    private boolean streaming = false;
    
    public void startStreaming() {
        streaming = true;
        new Thread(this::streamVideo).start();
    }
    
    private void streamVideo() {
        int frameNumber = 0;
        
        try {
            while (streaming) {
                // Simula frame video (1KB per frame)
                byte[] frameData = generateVideoFrame(frameNumber++);
                
                // Aggiungi header frame
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);
                
                dos.writeInt(frameNumber);           // Frame ID
                dos.writeLong(System.currentTimeMillis()); // Timestamp
                dos.writeInt(frameData.length);      // Frame size
                dos.write(frameData);                // Frame data
                
                byte[] packet = baos.toByteArray();
                
                DatagramPacket udpPacket = new DatagramPacket(
                    packet, packet.length, clientAddress, clientPort);
                
                socket.send(udpPacket);
                
                // 30 FPS = 33ms tra frame
                Thread.sleep(33);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Errore streaming: " + e.getMessage());
        }
    }
    
    private byte[] generateVideoFrame(int frameNumber) {
        // Simula dati frame video
        byte[] frame = new byte[1024];
        Arrays.fill(frame, (byte) (frameNumber % 256));
        return frame;
    }
}

// Client video receiver
public class VideoReceiver {
    private DatagramSocket socket;
    private Map<Integer, VideoFrame> receivedFrames = new ConcurrentHashMap<>();
    
    public void startReceiving() {
        try {
            byte[] buffer = new byte[2048];
            
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                // Parse frame
                ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
                DataInputStream dis = new DataInputStream(bais);
                
                int frameId = dis.readInt();
                long timestamp = dis.readLong();
                int frameSize = dis.readInt();
                
                byte[] frameData = new byte[frameSize];
                dis.read(frameData);
                
                VideoFrame frame = new VideoFrame(frameId, timestamp, frameData);
                receivedFrames.put(frameId, frame);
                
                // Calcola statistiche
                calculateStats(frame);
            }
        } catch (IOException e) {
            System.err.println("Errore ricezione video: " + e.getMessage());
        }
    }
    
    private void calculateStats(VideoFrame frame) {
        long latency = System.currentTimeMillis() - frame.timestamp;
        System.out.printf("Frame %d ricevuto - Latency: %dms\n", 
            frame.id, latency);
    }
    
    static class VideoFrame {
        final int id;
        final long timestamp;
        final byte[] data;
        
        VideoFrame(int id, long timestamp, byte[] data) {
            this.id = id; this.timestamp = timestamp; this.data = data;
        }
    }
}
```

### 🌐 **DNS Resolver Semplificato**

```java
// Simulatore DNS usando UDP
public class SimpleDNSResolver {
    private static final String DNS_SERVER = "8.8.8.8"; // Google DNS
    private static final int DNS_PORT = 53;
    
    public static String resolveDomain(String domain) {
        try (DatagramSocket socket = new DatagramSocket()) {
            
            // Crea query DNS semplificata (normalmente molto più complessa)
            String query = "QUERY:" + domain;
            byte[] queryData = query.getBytes();
            
            InetAddress dnsServer = InetAddress.getByName(DNS_SERVER);
            DatagramPacket queryPacket = new DatagramPacket(
                queryData, queryData.length, dnsServer, DNS_PORT);
            
            // Invia query
            socket.send(queryPacket);
            
            // Ricevi risposta con timeout
            byte[] responseBuffer = new byte[512];
            DatagramPacket responsePacket = new DatagramPacket(
                responseBuffer, responseBuffer.length);
            
            socket.setSoTimeout(5000); // 5 secondi timeout
            socket.receive(responsePacket);
            
            String response = new String(responsePacket.getData(), 
                0, responsePacket.getLength());
            
            return response;
            
        } catch (IOException e) {
            return "Errore DNS: " + e.getMessage();
        }
    }
    
    public static void main(String[] args) {
        String[] domains = {"google.com", "github.com", "stackoverflow.com"};
        
        for (String domain : domains) {
            System.out.println("Risoluzione " + domain + "...");
            String result = resolveDomain(domain);
            System.out.println("Risultato: " + result);
            System.out.println();
        }
    }
}
```

---

## 🔄 **Multicast e Broadcast**

### 📡 **Multicast UDP**

```java
// Server Multicast
public class MulticastServer {
    private static final String MULTICAST_ADDRESS = "230.0.0.1";
    private static final int MULTICAST_PORT = 8888;
    
    public static void main(String[] args) {
        try (MulticastSocket socket = new MulticastSocket()) {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            
            String[] messages = {
                "Notizia 1: Nuova versione software disponibile",
                "Notizia 2: Manutenzione programmata alle 02:00",  
                "Notizia 3: Nuovo corso Java disponibile"
            };
            
            for (String message : messages) {
                byte[] data = message.getBytes();
                DatagramPacket packet = new DatagramPacket(
                    data, data.length, group, MULTICAST_PORT);
                
                socket.send(packet);
                System.out.println("Inviato: " + message);
                
                Thread.sleep(2000); // Pausa 2 secondi
            }
            
        } catch (IOException | InterruptedException e) {
            System.err.println("Errore multicast server: " + e.getMessage());
        }
    }
}

// Client Multicast  
public class MulticastClient {
    private static final String MULTICAST_ADDRESS = "230.0.0.1";
    private static final int MULTICAST_PORT = 8888;
    
    public static void main(String[] args) {
        try (MulticastSocket socket = new MulticastSocket(MULTICAST_PORT)) {
            
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            
            // Unisciti al gruppo multicast
            SocketAddress groupAddress = new InetSocketAddress(group, MULTICAST_PORT);
            NetworkInterface netInterface = NetworkInterface.getByName("eth0");
            socket.joinGroup(groupAddress, netInterface);
            
            System.out.println("Client unito al gruppo multicast " + MULTICAST_ADDRESS);
            
            byte[] buffer = new byte[1024];
            
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Ricevuto: " + message);
            }
            
        } catch (IOException e) {
            System.err.println("Errore multicast client: " + e.getMessage());
        }
    }
}
```

### 📢 **Broadcast UDP**

```java
// Service Discovery tramite Broadcast
public class ServiceDiscovery {
    private static final int BROADCAST_PORT = 9999;
    
    // Server che risponde a discovery
    public static class ServiceServer {
        public static void main(String[] args) {
            try (DatagramSocket socket = new DatagramSocket(BROADCAST_PORT)) {
                socket.setBroadcast(true);
                
                byte[] buffer = new byte[256];
                
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    
                    String request = new String(packet.getData(), 0, packet.getLength());
                    
                    if ("DISCOVER_SERVICE".equals(request)) {
                        String response = "SERVICE_AVAILABLE:FileServer:localhost:8080";
                        byte[] responseData = response.getBytes();
                        
                        DatagramPacket responsePacket = new DatagramPacket(
                            responseData, responseData.length,
                            packet.getAddress(), packet.getPort());
                        
                        socket.send(responsePacket);
                        System.out.println("Risposto a discovery da: " + 
                            packet.getAddress());
                    }
                }
            } catch (IOException e) {
                System.err.println("Errore service server: " + e.getMessage());
            }
        }
    }
    
    // Client che cerca servizi
    public static class ServiceClient {
        public static void main(String[] args) {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);
                
                // Invia broadcast discovery
                String request = "DISCOVER_SERVICE";
                byte[] requestData = request.getBytes();
                
                InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
                DatagramPacket packet = new DatagramPacket(
                    requestData, requestData.length, 
                    broadcastAddress, BROADCAST_PORT);
                
                socket.send(packet);
                System.out.println("Discovery inviato in broadcast");
                
                // Ascolta risposte
                byte[] buffer = new byte[256];
                socket.setSoTimeout(5000); // 5 secondi timeout
                
                while (true) {
                    try {
                        DatagramPacket responsePacket = new DatagramPacket(
                            buffer, buffer.length);
                        socket.receive(responsePacket);
                        
                        String response = new String(responsePacket.getData(), 
                            0, responsePacket.getLength());
                        
                        System.out.println("Servizio trovato: " + response + 
                            " da " + responsePacket.getAddress());
                        
                    } catch (SocketTimeoutException e) {
                        System.out.println("Timeout - ricerca terminata");
                        break;
                    }
                }
                
            } catch (IOException e) {
                System.err.println("Errore service client: " + e.getMessage());
            }
        }
    }
}
```

---

## 🚀 **Performance e Ottimizzazione**

### 📊 **Benchmark UDP Performance**

```java
public class UDPBenchmark {
    
    public static void benchmarkThroughput() {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress address = InetAddress.getByName("localhost");
            
            // Test diversi payload sizes
            int[] payloadSizes = {64, 512, 1024, 8192};
            int iterations = 10000;
            
            for (int payloadSize : payloadSizes) {
                byte[] data = new byte[payloadSize];
                Arrays.fill(data, (byte) 'A');
                
                long startTime = System.nanoTime();
                
                for (int i = 0; i < iterations; i++) {
                    DatagramPacket packet = new DatagramPacket(
                        data, data.length, address, 8080);
                    socket.send(packet);
                }
                
                long endTime = System.nanoTime();
                long durationMs = (endTime - startTime) / 1_000_000;
                
                double throughputMBps = (payloadSize * iterations / 1024.0 / 1024.0) / 
                                      (durationMs / 1000.0);
                
                System.out.printf("Payload %d byte: %.2f MB/s (%d ms)\n", 
                    payloadSize, throughputMBps, durationMs);
            }
            
        } catch (IOException e) {
            System.err.println("Errore benchmark: " + e.getMessage());
        }
    }
    
    public static void benchmarkLatency() {
        try (DatagramSocket socket = new DatagramSocket(8080)) {
            
            // Avvia server echo in thread separato
            new Thread(() -> {
                try {
                    byte[] buffer = new byte[1024];
                    while (true) {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);
                        socket.send(packet); // Echo back
                    }
                } catch (IOException e) {
                    // Server terminato
                }
            }).start();
            
            Thread.sleep(100); // Aspetta avvio server
            
            // Test latency
            try (DatagramSocket clientSocket = new DatagramSocket()) {
                InetAddress address = InetAddress.getByName("localhost");
                byte[] data = "ping".getBytes();
                
                long totalLatency = 0;
                int iterations = 1000;
                
                for (int i = 0; i < iterations; i++) {
                    long startTime = System.nanoTime();
                    
                    // Invia ping
                    DatagramPacket sendPacket = new DatagramPacket(
                        data, data.length, address, 8080);
                    clientSocket.send(sendPacket);
                    
                    // Ricevi pong
                    byte[] buffer = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                    clientSocket.receive(receivePacket);
                    
                    long endTime = System.nanoTime();
                    long latencyNs = endTime - startTime;
                    totalLatency += latencyNs;
                }
                
                double avgLatencyMs = (totalLatency / iterations) / 1_000_000.0;
                System.out.printf("Latenza media UDP: %.3f ms\n", avgLatencyMs);
            }
            
        } catch (IOException | InterruptedException e) {
            System.err.println("Errore latency test: " + e.getMessage());
        }
    }
}
```

### ⚡ **Ottimizzazioni UDP**

```java
public class UDPOptimization {
    
    // 1. Buffer Pool per ridurre GC
    public static class BufferPool {
        private final Queue<byte[]> pool = new ConcurrentLinkedQueue<>();
        private final int bufferSize;
        
        public BufferPool(int bufferSize, int initialSize) {
            this.bufferSize = bufferSize;
            for (int i = 0; i < initialSize; i++) {
                pool.offer(new byte[bufferSize]);
            }
        }
        
        public byte[] acquire() {
            byte[] buffer = pool.poll();
            return buffer != null ? buffer : new byte[bufferSize];
        }
        
        public void release(byte[] buffer) {
            if (buffer.length == bufferSize) {
                pool.offer(buffer);
            }
        }
    }
    
    // 2. Batch Processing
    public static class BatchUDPSender {
        private DatagramSocket socket;
        private Queue<DatagramPacket> batchQueue = new ConcurrentLinkedQueue<>();
        private ScheduledExecutorService scheduler;
        
        public void startBatching() {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(this::flushBatch, 0, 10, TimeUnit.MILLISECONDS);
        }
        
        public void sendAsync(DatagramPacket packet) {
            batchQueue.offer(packet);
        }
        
        private void flushBatch() {
            List<DatagramPacket> batch = new ArrayList<>();
            DatagramPacket packet;
            
            while ((packet = batchQueue.poll()) != null && batch.size() < 100) {
                batch.add(packet);
            }
            
            for (DatagramPacket p : batch) {
                try {
                    socket.send(p);
                } catch (IOException e) {
                    System.err.println("Errore invio batch: " + e.getMessage());
                }
            }
        }
    }
    
    // 3. Zero-Copy usando NIO
    public static class NIOUDPServer {
        private DatagramChannel channel;
        private ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        
        public void startServer(int port) throws IOException {
            channel = DatagramChannel.open();
            channel.bind(new InetSocketAddress(port));
            channel.configureBlocking(false);
            
            Selector selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ);
            
            while (true) {
                if (selector.select(1000) > 0) {
                    Set<SelectionKey> keys = selector.selectedKeys();
                    
                    for (SelectionKey key : keys) {
                        if (key.isReadable()) {
                            buffer.clear();
                            SocketAddress clientAddress = channel.receive(buffer);
                            
                            if (clientAddress != null) {
                                buffer.flip();
                                channel.send(buffer, clientAddress); // Echo
                            }
                        }
                    }
                    keys.clear();
                }
            }
        }
    }
}
```

---

## 🔧 **Troubleshooting**

### 🕵️ **Diagnosi Problemi UDP**

```java
public class UDPDiagnostics {
    
    public static void diagnoseUDPConnection(String host, int port) {
        System.out.println("=== DIAGNOSI UDP ===");
        
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress address = InetAddress.getByName(host);
            
            // 1. Test basic connectivity
            String testMessage = "UDP_TEST";
            byte[] data = testMessage.getBytes();
            
            DatagramPacket packet = new DatagramPacket(
                data, data.length, address, port);
            
            long startTime = System.currentTimeMillis();
            socket.send(packet);
            System.out.println("✅ Packet inviato a " + host + ":" + port);
            
            // 2. Test ricezione (se c'è un echo server)
            byte[] buffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            
            try {
                socket.setSoTimeout(2000); // 2 secondi timeout
                socket.receive(receivePacket);
                
                long rtt = System.currentTimeMillis() - startTime;
                String response = new String(receivePacket.getData(), 
                    0, receivePacket.getLength());
                
                System.out.println("✅ Risposta ricevuta: " + response);
                System.out.println("⏱️  RTT: " + rtt + "ms");
                
            } catch (SocketTimeoutException e) {
                System.out.println("⚠️  Nessuna risposta (timeout)");
                System.out.println("   → Il server potrebbe non implementare echo");
                System.out.println("   → Firewall potrebbe bloccare risposta");
            }
            
            // 3. Test configurazioni socket
            System.out.println("\n=== CONFIGURAZIONI SOCKET ===");
            System.out.println("Local port: " + socket.getLocalPort());
            System.out.println("Receive buffer: " + socket.getReceiveBufferSize());
            System.out.println("Send buffer: " + socket.getSendBufferSize());
            System.out.println("SO Timeout: " + socket.getSoTimeout());
            System.out.println("Traffic class: " + socket.getTrafficClass());
            
        } catch (IOException e) {
            System.err.println("❌ Errore UDP: " + e.getMessage());
            
            if (e instanceof PortUnreachableException) {
                System.err.println("   → Porta non raggiungibile sul server");
            } else if (e instanceof NoRouteToHostException) {
                System.err.println("   → Host non raggiungibile");
            }
        }
    }
    
    public static void testPacketLoss(String host, int port, int packets) {
        System.out.println("\n=== TEST PACKET LOSS ===");
        
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress address = InetAddress.getByName(host);
            socket.setSoTimeout(1000); // 1 secondo timeout
            
            int sent = 0, received = 0;
            
            for (int i = 0; i < packets; i++) {
                String message = "PING_" + i;
                byte[] data = message.getBytes();
                
                DatagramPacket sendPacket = new DatagramPacket(
                    data, data.length, address, port);
                
                socket.send(sendPacket);
                sent++;
                
                byte[] buffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                
                try {
                    socket.receive(receivePacket);
                    received++;
                } catch (SocketTimeoutException e) {
                    System.out.print("X");
                }
                
                if (i % 50 == 49) {
                    System.out.println(); // New line ogni 50 pacchetti
                }
            }
            
            double lossPercent = ((sent - received) / (double) sent) * 100;
            System.out.printf("\nInviati: %d, Ricevuti: %d, Loss: %.1f%%\n", 
                sent, received, lossPercent);
            
        } catch (IOException e) {
            System.err.println("Errore test packet loss: " + e.getMessage());
        }
    }
}
```

### ⚠️ **Errori Comuni UDP**

| Errore | Causa | Soluzione |
|--------|-------|-----------|
| **PortUnreachableException** | Server non in ascolto | Verificare server avviato |
| **SocketTimeoutException** | Nessuna risposta entro timeout | Aumentare timeout o controllare rete |
| **PacketTooBigException** | Packet > MTU | Ridurre dimensione payload |
| **NoRouteToHostException** | Host irraggiungibile | Verificare routing/firewall |
| **SecurityException** | Permissions mancanti | Verificare policy security |

### 🛠️ **Tools per Debug UDP**

```bash
# Monitor traffico UDP
netstat -u
ss -u

# Cattura pacchetti UDP specifici
tcpdump -i any udp port 8080
wireshark -f "udp port 8080"

# Test connectivity UDP
nc -u localhost 8080      # Netcat UDP mode
socat - UDP4:localhost:8080  # Alternative a netcat

# Statistiche UDP sistema
cat /proc/net/udp
ss -s | grep -i udp

# Genera traffico UDP per test
hping3 -2 -p 8080 -c 10 localhost  # 10 pacchetti UDP
```

---

## 🎯 **Casi d'Uso Reali**

### 🎮 **Gaming Online**

```java
// Architettura tipica gioco multiplayer
public class GameArchitecture {
    
    // Client invia input frequenti
    public class GameClient {
        private DatagramSocket socket;
        private InetAddress serverAddress;
        
        // Invia input giocatore 60 volte/secondo
        public void sendPlayerInput(PlayerInput input) {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(32);
                buffer.putInt(input.playerId);
                buffer.putLong(System.currentTimeMillis());
                buffer.putFloat(input.x);
                buffer.putFloat(input.y);
                buffer.put(input.actions);
                
                DatagramPacket packet = new DatagramPacket(
                    buffer.array(), buffer.position(), serverAddress, 7777);
                
                socket.send(packet);
            } catch (IOException e) {
                // Ignore - UDP best effort
            }
        }
    }
    
    // Server processa input e invia state updates
    public class GameServer {
        private DatagramSocket socket;
        private Map<Integer, PlayerState> players = new ConcurrentHashMap<>();
        
        // Ricevi input da tutti i client
        public void processInputs() {
            try {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                
                socket.receive(packet);
                
                ByteBuffer bb = ByteBuffer.wrap(packet.getData());
                int playerId = bb.getInt();
                long timestamp = bb.getLong();
                float x = bb.getFloat();
                float y = bb.getFloat();
                
                // Aggiorna stato gioco
                updatePlayerState(playerId, x, y, timestamp);
                
            } catch (IOException e) {
                // Log ma continua
            }
        }
        
        // Invia aggiornamenti stato 30 volte/secondo
        public void broadcastGameState() {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            
            // Serializza stato tutti i giocatori
            for (PlayerState player : players.values()) {
                buffer.putInt(player.id);
                buffer.putFloat(player.x);
                buffer.putFloat(player.y);
                buffer.putInt(player.health);
            }
            
            // Invia a tutti i client connessi
            // (lista client mantenuta separatamente)
        }
    }
}
```

### 📹 **Live Streaming**

```java
public class LiveStreaming {
    
    // Encoder video che invia stream UDP
    public class VideoEncoder {
        private DatagramSocket socket;
        private List<InetSocketAddress> viewers = new ArrayList<>();
        
        public void streamVideo() {
            try {
                while (streaming) {
                    // Cattura frame (simulato)
                    VideoFrame frame = captureFrame();
                    
                    // Comprimi frame
                    byte[] compressedData = compressFrame(frame);
                    
                    // Crea RTP packet header
                    ByteBuffer packet = ByteBuffer.allocate(compressedData.length + 12);
                    packet.put((byte) 0x80); // Version + flags  
                    packet.put((byte) 96);   // Payload type
                    packet.putShort((short) sequenceNumber++);
                    packet.putInt((int) System.currentTimeMillis());
                    packet.putInt(ssrc); // Source identifier
                    packet.put(compressedData);
                    
                    // Invia a tutti i viewer
                    DatagramPacket udpPacket = new DatagramPacket(
                        packet.array(), packet.position());
                    
                    for (InetSocketAddress viewer : viewers) {
                        udpPacket.setSocketAddress(viewer);
                        socket.send(udpPacket);
                    }
                    
                    // 30 FPS timing
                    Thread.sleep(33);
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Errore streaming: " + e.getMessage());
            }
        }
    }
    
    // Decoder che riceve stream
    public class VideoDecoder {
        private DatagramSocket socket;
        private TreeMap<Integer, VideoFrame> frameBuffer = new TreeMap<>();
        
        public void receiveStream() {
            try {
                byte[] buffer = new byte[2048];
                
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    
                    // Parse RTP header
                    ByteBuffer bb = ByteBuffer.wrap(packet.getData());
                    bb.get(); // Skip version/flags
                    bb.get(); // Skip payload type
                    int seqNum = bb.getShort() & 0xFFFF;
                    int timestamp = bb.getInt();
                    bb.getInt(); // Skip SSRC
                    
                    // Extract video data
                    byte[] frameData = new byte[packet.getLength() - 12];
                    bb.get(frameData);
                    
                    VideoFrame frame = new VideoFrame(seqNum, timestamp, frameData);
                    frameBuffer.put(seqNum, frame);
                    
                    // Riproduci frame in ordine (con tolleranza perdite)
                    playOrderedFrames();
                }
            } catch (IOException e) {
                System.err.println("Errore ricezione: " + e.getMessage());
            }
        }
        
        private void playOrderedFrames() {
            // Implementa logica replay con buffer jitter
        }
    }
}
```

### 🌐 **IoT Sensor Network**

```java
public class IoTSensorNetwork {
    
    // Sensore IoT che invia telemetria
    public class IoTSensor {
        private DatagramSocket socket;
        private String sensorId;
        private InetAddress gatewayAddress;
        
        public void sendTelemetry() {
            try {
                SensorReading reading = readSensors();
                
                // Formato compatto per IoT
                ByteBuffer data = ByteBuffer.allocate(64);
                data.put(sensorId.getBytes(), 0, 16);        // 16 byte ID
                data.putLong(System.currentTimeMillis());     // 8 byte timestamp
                data.putFloat(reading.temperature);           // 4 byte temp
                data.putFloat(reading.humidity);              // 4 byte humidity  
                data.putShort((short) reading.batteryLevel);  // 2 byte battery
                data.putShort((short) reading.signalStrength); // 2 byte signal
                
                DatagramPacket packet = new DatagramPacket(
                    data.array(), data.position(), gatewayAddress, 5683); // CoAP port
                
                socket.send(packet);
                
                // IoT sensors tipicamente inviano ogni 30-60 secondi
                
            } catch (IOException e) {
                // IoT deve essere robusto - riprova o memorizza per dopo
                System.err.println("Errore invio telemetria: " + e.getMessage());
            }
        }
    }
    
    // Gateway IoT che raccoglie dati
    public class IoTGateway {
        private DatagramSocket socket;
        private Map<String, SensorData> sensorDatabase = new ConcurrentHashMap<>();
        
        public void collectTelemetry() {
            try {
                byte[] buffer = new byte[256];
                
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    
                    ByteBuffer bb = ByteBuffer.wrap(packet.getData());
                    
                    // Parse sensor data
                    byte[] idBytes = new byte[16];
                    bb.get(idBytes);
                    String sensorId = new String(idBytes).trim();
                    
                    long timestamp = bb.getLong();
                    float temperature = bb.getFloat();
                    float humidity = bb.getFloat();
                    short battery = bb.getShort();
                    short signal = bb.getShort();
                    
                    SensorData data = new SensorData(sensorId, timestamp, 
                        temperature, humidity, battery, signal);
                    
                    // Aggiorna database locale
                    sensorDatabase.put(sensorId, data);
                    
                    // Invia a cloud (HTTP/MQTT) se necessario
                    if (shouldForwardToCloud(data)) {
                        forwardToCloud(data);
                    }
                    
                    System.out.printf("Sensore %s: %.1f°C, %.1f%% RH\n", 
                        sensorId, temperature, humidity);
                }
                
            } catch (IOException e) {
                System.err.println("Errore gateway IoT: " + e.getMessage());
            }
        }
    }
}
```

---

## 📚 **Riassunto e Best Practices**

### ✅ **Vantaggi UDP**
- ⚡ **Velocità estrema** - Overhead minimo
- 🔀 **Broadcast/Multicast** - Comunicazione 1-to-many
- 🎯 **Real-time** - Ideale per applicazioni time-sensitive
- 📦 **Message boundaries** - Preserva confini pacchetti
- 🚫 **No connection state** - Scalabilità elevata

### ⚠️ **Svantaggi UDP**
- 📉 **No affidabilità** - Pacchetti possono perdersi
- 🔀 **No ordinamento** - Pacchetti possono arrivare disordinati
- 🚫 **No controllo flusso** - Rischio overflow buffer
- 🛠️ **Complessità applicativa** - Devi gestire affidabilità tu

### 🎯 **Best Practices UDP**

```java
// 1. Sempre impostare timeout
socket.setSoTimeout(5000); // Evita blocking infinito

// 2. Gestire eccezioni gracefully  
try {
    socket.receive(packet);
} catch (SocketTimeoutException e) {
    // Timeout normale, continua
} catch (IOException e) {
    // Errore serio, log e gestisci
}

// 3. Limitare dimensione pacchetti
int MAX_UDP_SIZE = 508; // Safe size per evitare fragmentation

// 4. Implementare retry per operazioni critiche
for (int retry = 0; retry < 3; retry++) {
    try {
        socket.send(packet);
        break; // Successo
    } catch (IOException e) {
        if (retry == 2) throw e; // Ultimo tentativo
        Thread.sleep(100); // Backoff
    }
}

// 5. Usare sequence numbers per rilevare perdite
ByteBuffer buffer = ByteBuffer.allocate(1024);
buffer.putInt(sequenceNumber++);
buffer.put(actualData);
```

### 🚫 **Errori da Evitare**

```java
// ❌ ERRORE: Non impostare timeout
socket.receive(packet); // Può bloccare per sempre

// ✅ CORRETTO: Sempre impostare timeout  
socket.setSoTimeout(5000);
socket.receive(packet);

// ❌ ERRORE: Ignorare completamente gli errori
try {
    socket.send(packet);
} catch (IOException e) {
    // Ignore - SBAGLIATO!
}

// ✅ CORRETTO: Loggare almeno gli errori
try {
    socket.send(packet);  
} catch (IOException e) {
    logger.warn("Errore invio UDP: " + e.getMessage());
}

// ❌ ERRORE: Pacchetti troppo grandi
byte[] hugeData = new byte[65000]; // Fragmentation!

// ✅ CORRETTO: Limitare dimensioni
byte[] data = new byte[512]; // Safe size
```

---

## 🔗 **Risorse e Approfondimenti**

### 📖 **RFC e Specifiche**
- **RFC 768** - User Datagram Protocol
- **RFC 1112** - Host Extensions for IP Multicasting  
- **RFC 3828** - UDP-Lite Protocol
- **RFC 4566** - SDP: Session Description Protocol

### 🔗 **Link Utili**
- [Java DatagramSocket API](https://docs.oracle.com/javase/8/docs/api/java/net/DatagramSocket.html)
- [UDP vs TCP Comparison](https://www.cloudflare.com/learning/ddos/glossary/user-datagram-protocol-udp/)
- [Multicast Programming HOWTO](http://www.tldp.org/HOWTO/Multicast-HOWTO.html)

### 💻 **Tools Raccomandati**
- **Wireshark** - Analisi pacchetti UDP
- **nc (netcat)** - Test UDP connections
- **iperf3** - Benchmark throughput UDP
- **hping3** - Generazione traffico UDP custom
- **JProfiler** - Profiling applicazioni Java UDP

---

## Navigazione del Corso
- [📑 Torna all'Indice del Corso](../README.md)
- [⬅️ Guida Precedente](04-Il-protocollo-TCP.md)
- [➡️ Guida Successiva](06-JavaNetworking.md)

---

*Corso "Socket Programming in Java" - ITCS Cannizzaro"*  
*Versione 1.0 - Ottobre 2025*