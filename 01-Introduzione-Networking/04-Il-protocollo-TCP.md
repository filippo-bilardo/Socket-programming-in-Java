# 📡 Guida 04: Il Protocollo TCP

> *Transmission Control Protocol - Fondamenti e Implementazione Java*

---

## 📋 **Indice**

1. [Introduzione al TCP](#-introduzione-al-tcp)
2. [Caratteristiche del TCP](#-caratteristiche-del-tcp)
3. [Architettura TCP](#️-architettura-tcp)
4. [Connessioni TCP](#-connessioni-tcp)
5. [Gestione Flusso Dati](#-gestione-flusso-dati)
6. [Controllo Errori](#️-controllo-errori)
7. [TCP in Java](#☕-tcp-in-java)
8. [Esempi Pratici](#-esempi-pratici)
9. [Performance e Ottimizzazione](#-performance-e-ottimizzazione)
10. [Troubleshooting](#-troubleshooting)

---

## 🎯 **Introduzione al TCP**

### 📖 **Cos'è il TCP**

Il **Transmission Control Protocol (TCP)** è un protocollo di trasporto della suite TCP/IP che fornisce:

- **Connessione affidabile** tra due host
- **Consegna garantita** dei dati
- **Ordinamento** dei pacchetti
- **Controllo di flusso** e congestione
- **Rilevamento e correzione errori**

### 🌐 **Posizione nel Stack TCP/IP**

```
┌─────────────────────────┐
│    Applicazione         │ ← HTTP, FTP, SSH, SMTP
├─────────────────────────┤
│    Trasporto            │ ← TCP (porta 80, 443, 21, 22)
├─────────────────────────┤
│    Rete (Internet)      │ ← IP (IPv4, IPv6)
├─────────────────────────┤
│    Collegamento         │ ← Ethernet, WiFi
├─────────────────────────┤
│    Fisico               │ ← Cavi, onde radio
└─────────────────────────┘
```

### 🎯 **Quando Utilizzare TCP**

**Usa TCP quando serve:**
- Affidabilità nella consegna dati
- Ordine corretto dei messaggi
- Controllo automatico degli errori
- Applicazioni mission-critical

**Esempi pratici:**
- 🌐 **Web browsing** (HTTP/HTTPS)
- 📧 **Email** (SMTP, IMAP, POP3)
- 📁 **Trasferimento file** (FTP, SFTP)
- 🔒 **SSH** e connessioni remote
- 💰 **Transazioni bancarie**
- 🎮 **Chat e messaging**

---

## ⚙️ **Caratteristiche del TCP**

### 🔗 **1. Connection-Oriented**

TCP stabilisce una **connessione** prima di trasmettere dati:

```
Client                    Server
    │                        │
    │──── SYN ──────────────►│  1. Richiesta connessione
    │◄─── SYN+ACK ───────────│  2. Accettazione + conferma
    │──── ACK ──────────────►│  3. Conferma finale
    │                        │
    │═══ CONNESSIONE ════════│  ← Dati possono fluire
    │                        │
    │──── FIN ──────────────►│  4. Richiesta chiusura
    │◄──── ACK ──────────────│  5. Conferma chiusura
    │◄──── FIN ──────────────│  6. Chiusura dal server
    │──── ACK ──────────────►│  7. Conferma finale
    │                        │
    │════ DISCONNESSO ═══════│  ← Connessione chiusa
```

### 📦 **2. Affidabilità**

#### **A) Acknowledgment (ACK)**
Ogni segmento ricevuto viene confermato:

```java
// Esempio concettuale
Client invia: "Hello" (seq=100)
Server risponde: ACK=105 (conferma ricezione di 5 byte)
```

#### **B) Ritrasmissione Automatica**
Se l'ACK non arriva entro il timeout:

```java
Client invia: "World" (seq=105)
// Timeout scaduto, nessun ACK ricevuto
Client ri-invia: "World" (seq=105) // Stesso numero sequenza
```

#### **C) Rilevamento Duplicati**
Il numero di sequenza previene duplicati:

```java
Server riceve: "World" (seq=105) → Nuovo, accetta
Server riceve: "World" (seq=105) → Duplicato, scarta
```

### 🔢 **3. Ordinamento**

TCP garantisce che i dati arrivino nell'ordine corretto:

```
Invio:     [1] [2] [3] [4] [5]
Rete:      [1] [4] [2] [5] [3]  ← Disordinati
Ricezione: [1] [2] [3] [4] [5]  ← Riordinati da TCP
```

### ⚡ **4. Controllo di Flusso**

**Sliding Window Protocol** previene overflow del buffer:

```
Sender Buffer:    [████████░░░░] Window size = 8
Receiver Buffer:  [██░░░░░░░░░░] Available = 2

TCP Header: Window = 2 ← Receiver comunica spazio disponibile
```

### 🚥 **5. Controllo Congestione**

TCP rileva e gestisce la congestione di rete:

```java
// Algoritmi principali
1. Slow Start     → Crescita esponenziale velocità
2. Congestion Avoidance → Crescita lineare  
3. Fast Retransmit → Ritrasmissione rapida
4. Fast Recovery  → Recupero senza slow start
```

---

## 🏗️ **Architettura TCP**

### 📋 **Header TCP**

```
 0                   1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
├─────────────────────────────────────────────────────────────────┤
│          Source Port          │       Destination Port          │
├─────────────────────────────────────────────────────────────────┤
│                        Sequence Number                          │
├─────────────────────────────────────────────────────────────────┤
│                    Acknowledgment Number                        │
├─────────────────────────────────────────────────────────────────┤
│  Data │           │U│A│P│R│S│F│                                 │
│ Offset│  Reserved │R│C│S│S│Y│I│            Window Size          │
│       │           │G│K│H│T│N│N│                                 │
├─────────────────────────────────────────────────────────────────┤
│           Checksum            │         Urgent Pointer          │
├─────────────────────────────────────────────────────────────────┤
│                    Options                    │    Padding      │
├─────────────────────────────────────────────────────────────────┤
```

#### **Campi Principali:**

| Campo | Dimensione | Descrizione |
|-------|------------|-------------|
| **Source Port** | 16 bit | Porta mittente (0-65535) |
| **Destination Port** | 16 bit | Porta destinatario |
| **Sequence Number** | 32 bit | Numero sequenza per ordinamento |
| **ACK Number** | 32 bit | Prossimo byte atteso |
| **Window Size** | 16 bit | Spazio buffer disponibile |
| **Flags** | 6 bit | SYN, ACK, FIN, RST, PSH, URG |
| **Checksum** | 16 bit | Controllo integrità |

### 🚩 **Flag TCP**

```java
SYN = 1  → Stabilire connessione
ACK = 1  → Acknowledgment valido  
FIN = 1  → Chiudere connessione
RST = 1  → Reset connessione (errore)
PSH = 1  → Push immediato ai livelli superiori
URG = 1  → Dati urgenti (raramente usato)
```

---

## 🔗 **Connessioni TCP**

### 🤝 **Three-Way Handshake (Apertura)**

```
Client                           Server
   │                                │
   │──── SYN seq=100 ──────────────►│  1. Client → Server: "Voglio connettermi"
   │                                │     SYN=1, seq=100
   │                                │
   │◄── SYN+ACK seq=200,ack=101 ────│  2. Server → Client: "OK, anche io"  
   │                                │     SYN=1, ACK=1, seq=200, ack=101
   │                                │
   │──── ACK seq=101,ack=201 ──────►│  3. Client → Server: "Perfetto!"
   │                                │     ACK=1, seq=101, ack=201
   │                                │
   │════════ CONNESSO ══════════════│     Connessione stabilita
```

#### **Implementazione Java:**
```java
// Client side
Socket socket = new Socket("localhost", 8080); 
// ↑ Questo comando esegue automaticamente il 3-way handshake

// Server side  
ServerSocket serverSocket = new ServerSocket(8080);
Socket client = serverSocket.accept(); 
// ↑ accept() completa il 3-way handshake
```

### 👋 **Four-Way Handshake (Chiusura)**

```
Client                           Server
   │                                │
   │──── FIN seq=300 ──────────────►│  1. Client: "Ho finito di inviare"
   │                                │     FIN=1, seq=300
   │                                │
   │◄──── ACK ack=301 ──────────────│  2. Server: "Ho ricevuto il tuo FIN"
   │                                │     ACK=1, ack=301
   │                                │
   │◄──── FIN seq=400 ──────────────│  3. Server: "Anch'io ho finito"  
   │                                │     FIN=1, seq=400
   │                                │
   │──── ACK ack=401 ──────────────►│  4. Client: "Chiusura confermata"
   │                                │     ACK=1, ack=401
   │                                │
   │══════ DISCONNESSO ═════════════│     Connessione chiusa
```

#### **Implementazione Java:**
```java
// Chiusura esplicita
socket.close(); // Invia FIN e gestisce handshake

// Chiusura con try-with-resources (automatica)
try (Socket socket = new Socket("localhost", 8080)) {
    // Operazioni...
} // close() chiamato automaticamente
```

### 🔄 **Stati Connessione TCP**

```
CLOSED → LISTEN → SYN_RCVD → ESTABLISHED → FIN_WAIT_1 → CLOSED
   ↑         ↓                      ↓              ↓
   └─────────┴──────────────────────┴──────────────┘
   
Stati principali:
• CLOSED      → Connessione chiusa
• LISTEN      → Server in ascolto  
• SYN_SENT    → Client ha inviato SYN
• ESTABLISHED → Connessione attiva
• FIN_WAIT_1  → Inizio chiusura
• TIME_WAIT   → Attesa finale prima chiusura
```

---

## 🌊 **Gestione Flusso Dati**

### 📏 **Sliding Window**

Il **controllo di flusso** impedisce al mittente di sovraccaricare il ricevitore:

```java
// Esempio semplificato
Sender Window:   [1][2][3][4]░░░░  ← Può inviare 4 segmenti
Receiver Buffer: ██░░░░░░░░░░░░░░  ← Spazio per 2 segmenti

TCP Header Window = 2  ← Receiver dice: "Ho spazio per 2"
```

#### **Algoritmo Sliding Window:**

```java
public class SlidingWindow {
    private int windowSize;     // Dimensione finestra
    private int base;           // Primo byte non confermato  
    private int nextSeq;        // Prossimo numero sequenza
    
    public boolean canSend() {
        return (nextSeq - base) < windowSize;
    }
    
    public void sendData(byte[] data) {
        if (canSend()) {
            // Invia segmento con seq = nextSeq
            nextSeq += data.length;
        }
    }
    
    public void receiveAck(int ackNum) {
        if (ackNum > base) {
            base = ackNum;  // Sposta finestra in avanti
        }
    }
}
```

### ⚡ **Algoritmo di Nagle**

Ottimizza l'efficienza combinando piccoli segmenti:

```java
// SENZA Nagle (inefficiente)
send("H");    // 1 byte + 40 byte header = 41 byte
send("e");    // 1 byte + 40 byte header = 41 byte  
send("l");    // 1 byte + 40 byte header = 41 byte
send("l");    // 1 byte + 40 byte header = 41 byte
send("o");    // 1 byte + 40 byte header = 41 byte
// Totale: 205 byte per inviare "Hello"

// CON Nagle (efficiente)  
buffer = "Hello";  // Accumula
send("Hello");     // 5 byte + 40 byte header = 45 byte
// Totale: 45 byte per inviare "Hello"
```

#### **Controllo Nagle in Java:**
```java
Socket socket = new Socket("localhost", 8080);
socket.setTcpNoDelay(true);  // Disabilita Nagle per bassa latenza
socket.setTcpNoDelay(false); // Abilita Nagle per efficienza
```

---

## 🛡️ **Controllo Errori**

### 🔍 **Checksum**

TCP calcola un checksum per rilevare errori utilizzando la somma in complemento a uno dei dati:

**Formula matematica:**
\[
\text{Checksum} = \sim \left( \sum_{i=0}^{n-1} w_i \right) \mod 2^{16}
\]
Dove:
- \( w_i \) sono le parole a 16 bit del segmento TCP (inclusi pseudo-header, header TCP e dati)
- \( \sim \) è il complemento a uno (NOT bit a bit)
- Il risultato è un valore a 16 bit

```java
// Pseudo-codice calcolo checksum
public int calculateChecksum(byte[] data) {
    int sum = 0;
    for (int i = 0; i < data.length - 1; i += 2) {
        sum += ((data[i] << 8) | data[i + 1]);
    }
    return ~sum & 0xFFFF;  // Complemento a 1
}
```

### ⏰ **Timeout e Ritrasmissione**

TCP usa **Adaptive Timeout** basato su RTT (Round Trip Time):

```java
// Calcolo timeout adattivo
public class AdaptiveTimeout {
    private double rtt;          // Round Trip Time misurato
    private double srtt;         // Smoothed RTT
    private double rttvar;       // RTT variance
    
    public void updateRTT(double measuredRTT) {
        if (srtt == 0) {
            srtt = measuredRTT;
            rttvar = measuredRTT / 2;
        } else {
            rttvar = 0.75 * rttvar + 0.25 * Math.abs(srtt - measuredRTT);
            srtt = 0.875 * srtt + 0.125 * measuredRTT;
        }
    }
    
    public double getTimeout() {
        return srtt + 4 * rttvar;  // RFC formula
    }
}
```

### 🚀 **Fast Retransmit**

Ritrasmissione rapida senza aspettare timeout:

```
Sender invia: [1] [2] [3] [4] [5]
Receiver riceve: [1] [2] [4] [5]  ← [3] perso

ACK ricevuti dal Sender:
ACK=2  ← OK
ACK=2  ← Duplicate (manca [3])  
ACK=2  ← Duplicate (manca [3])
ACK=2  ← Duplicate (manca [3]) ← 3° duplicato!

→ Fast Retransmit di [3] senza aspettare timeout
```

---

## ☕ **TCP in Java**

### 🖥️ **Server TCP**

```java
import java.io.*;
import java.net.*;

public class TCPServer {
    private static final int PORT = 8080;
    
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server TCP avviato sulla porta " + PORT);
            
            while (true) {
                // Accept() esegue 3-way handshake automaticamente
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connesso: " + 
                    clientSocket.getRemoteSocketAddress());
                
                // Gestisci client (in questo esempio: thread separato)
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Errore server: " + e.getMessage());
        }
    }
    
    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(
                clientSocket.getOutputStream(), true)) {
            
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Ricevuto: " + inputLine);
                out.println("Echo: " + inputLine);  // Echo back
            }
        } catch (IOException e) {
            System.err.println("Errore gestione client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close(); // 4-way handshake automatico
            } catch (IOException e) {
                System.err.println("Errore chiusura: " + e.getMessage());
            }
        }
    }
}
```

### 💻 **Client TCP**

```java
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TCPClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    
    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);  // 3-way handshake
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {
            
            System.out.println("Connesso al server " + HOST + ":" + PORT);
            
            String userInput;
            while (true) {
                System.out.print("Messaggio (o 'quit'): ");
                userInput = scanner.nextLine();
                
                if ("quit".equalsIgnoreCase(userInput)) {
                    break;
                }
                
                out.println(userInput);           // Invia al server
                String response = in.readLine();  // Ricevi risposta
                System.out.println("Server: " + response);
            }
            
        } catch (IOException e) {
            System.err.println("Errore client: " + e.getMessage());
        } // close() automatico → 4-way handshake
        
        System.out.println("Client disconnesso.");
    }
}
```

### ⚙️ **Configurazioni Socket TCP**

```java
public class TCPConfiguration {
    public static void configureTCPSocket(Socket socket) throws IOException {
        // Controllo di flusso
        socket.setReceiveBufferSize(64 * 1024);  // Buffer ricezione 64KB
        socket.setSendBufferSize(64 * 1024);     // Buffer invio 64KB
        
        // Algoritmo di Nagle
        socket.setTcpNoDelay(false);  // Abilita Nagle (default)
        // socket.setTcpNoDelay(true);   // Disabilita per bassa latenza
        
        // Keep-Alive (heartbeat)
        socket.setKeepAlive(true);    // Rileva connessioni morte
        
        // Timeout operazioni
        socket.setSoTimeout(30000);   // 30 secondi timeout lettura
        
        // Linger (comportamento chiusura)
        socket.setSoLinger(true, 10); // Aspetta 10 sec prima chiusura forzata
        
        // Riuso indirizzo (utile per server)
        if (socket instanceof ServerSocket) {
            ((ServerSocket) socket).setReuseAddress(true);
        }
    }
}
```

---

## 💡 **Esempi Pratici**

### 📂 **File Transfer TCP**

```java
// Server per ricevere file
public class FileServer {
    public static void receiveFile(Socket socket, String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName);
             InputStream in = socket.getInputStream()) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            
            System.out.println("File ricevuto: " + fileName);
        } catch (IOException e) {
            System.err.println("Errore ricezione file: " + e.getMessage());
        }
    }
}

// Client per inviare file  
public class FileClient {
    public static void sendFile(Socket socket, String fileName) {
        try (FileInputStream fis = new FileInputStream(fileName);
             OutputStream out = socket.getOutputStream()) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            
            out.flush();
            System.out.println("File inviato: " + fileName);
        } catch (IOException e) {
            System.err.println("Errore invio file: " + e.getMessage());
        }
    }
}
```

### 💬 **Chat TCP Multi-Client**

```java
// Server chat con thread pool
public class ChatServer {
    private static Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    
    public static void broadcast(String message, ClientHandler sender) {
        clients.parallelStream()
               .filter(client -> client != sender)
               .forEach(client -> client.sendMessage(message));
    }
    
    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private String username;
        
        // Implementazione gestione client...
    }
}
```

### 🔐 **HTTP Server Semplificato**

```java
public class SimpleHTTPServer {
    public static void handleHTTPRequest(Socket client) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(client.getInputStream()));
             PrintWriter out = new PrintWriter(client.getOutputStream())) {
            
            // Leggi richiesta HTTP
            String requestLine = in.readLine();
            System.out.println("Request: " + requestLine);
            
            // Salta gli header
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                // Skip headers
            }
            
            // Risposta HTTP semplice
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/html");
            out.println("Connection: close");
            out.println(); // Linea vuota obbligatoria
            out.println("<html><body><h1>Hello from TCP Server!</h1></body></html>");
            out.flush();
            
        } catch (IOException e) {
            System.err.println("Errore HTTP: " + e.getMessage());
        }
    }
}
```

---

## 🚀 **Performance e Ottimizzazione**

### 📊 **Metriche Performance TCP**

```java
public class TCPMetrics {
    public static void measureTCPPerformance(Socket socket) {
        try {
            // Informazioni socket
            System.out.println("=== TCP SOCKET INFO ===");
            System.out.println("Local: " + socket.getLocalSocketAddress());
            System.out.println("Remote: " + socket.getRemoteSocketAddress());
            System.out.println("Send Buffer: " + socket.getSendBufferSize());
            System.out.println("Receive Buffer: " + socket.getReceiveBufferSize());
            System.out.println("TCP No Delay: " + socket.getTcpNoDelay());
            System.out.println("Keep Alive: " + socket.getKeepAlive());
            
            // Test throughput
            measureThroughput(socket);
            
        } catch (IOException e) {
            System.err.println("Errore metrics: " + e.getMessage());
        }
    }
    
    private static void measureThroughput(Socket socket) throws IOException {
        byte[] data = new byte[1024 * 1024]; // 1MB
        Arrays.fill(data, (byte) 'A');
        
        long startTime = System.currentTimeMillis();
        
        OutputStream out = socket.getOutputStream();
        for (int i = 0; i < 100; i++) { // Invia 100MB
            out.write(data);
        }
        out.flush();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        double throughputMBps = (100.0 * 1000) / duration; // MB/s
        System.out.printf("Throughput: %.2f MB/s\n", throughputMBps);
    }
}
```

### ⚡ **Ottimizzazioni TCP**

```java
public class TCPOptimization {
    
    // 1. Buffer Size Tuning
    public static void optimizeBuffers(Socket socket) throws IOException {
        // Calcola buffer ottimale: Bandwidth × RTT
        int bandwidth = 100_000_000; // 100 Mbps in bit/s
        int rtt = 50;               // 50ms RTT
        int optimalBuffer = (bandwidth / 8) * (rtt / 1000); // Bytes
        
        socket.setReceiveBufferSize(optimalBuffer);
        socket.setSendBufferSize(optimalBuffer);
    }
    
    // 2. Disable Nagle per bassa latenza
    public static void optimizeLatency(Socket socket) throws IOException {
        socket.setTcpNoDelay(true);  // Disable Nagle
        socket.setSoTimeout(1000);   // 1s timeout
    }
    
    // 3. Enable Keep-Alive per connessioni lunghe
    public static void optimizeLongConnections(Socket socket) throws IOException {
        socket.setKeepAlive(true);
        socket.setSoLinger(false, 0); // Chiusura immediata
    }
    
    // 4. Thread Pool per server scalabili
    public static ExecutorService createOptimizedThreadPool() {
        int cores = Runtime.getRuntime().availableProcessors();
        return Executors.newFixedThreadPool(cores * 2);
    }
}
```

### 📈 **Benchmark TCP vs UDP**

```java
public class TCPvsUDPBenchmark {
    
    public static void benchmarkTCP() {
        long startTime = System.currentTimeMillis();
        
        try (Socket socket = new Socket("localhost", 8080)) {
            OutputStream out = socket.getOutputStream();
            
            for (int i = 0; i < 10000; i++) {
                out.write("Hello TCP".getBytes());
                out.flush();
            }
            
        } catch (IOException e) {
            System.err.println("TCP Error: " + e.getMessage());
        }
        
        long tcpTime = System.currentTimeMillis() - startTime;
        System.out.println("TCP Time: " + tcpTime + "ms");
    }
    
    public static void benchmarkUDP() {
        long startTime = System.currentTimeMillis();
        
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress address = InetAddress.getByName("localhost");
            
            for (int i = 0; i < 10000; i++) {
                byte[] data = "Hello UDP".getBytes();
                DatagramPacket packet = new DatagramPacket(
                    data, data.length, address, 8080);
                socket.send(packet);
            }
            
        } catch (IOException e) {
            System.err.println("UDP Error: " + e.getMessage());
        }
        
        long udpTime = System.currentTimeMillis() - startTime;
        System.out.println("UDP Time: " + udpTime + "ms");
    }
}
```

---

## 🔧 **Troubleshooting**

### 🕵️ **Diagnosi Problemi TCP**

```java
public class TCPDiagnostics {
    
    public static void diagnoseConnection(String host, int port) {
        System.out.println("=== DIAGNOSI TCP ===");
        
        // 1. Test raggiungibilità host
        try {
            InetAddress address = InetAddress.getByName(host);
            boolean reachable = address.isReachable(5000);
            System.out.println("Host raggiungibile: " + reachable);
        } catch (IOException e) {
            System.out.println("Host non raggiungibile: " + e.getMessage());
        }
        
        // 2. Test connessione TCP
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 5000);
            System.out.println("Connessione TCP: OK");
            
            // 3. Test velocità connessione
            long startTime = System.currentTimeMillis();
            socket.getOutputStream().write(1);
            socket.getInputStream().read();
            long rtt = System.currentTimeMillis() - startTime;
            System.out.println("RTT: " + rtt + "ms");
            
        } catch (IOException e) {
            System.out.println("Connessione TCP fallita: " + e.getMessage());
        }
    }
}
```

### ⚠️ **Errori Comuni TCP**

| Errore | Causa | Soluzione |
|--------|-------|-----------|
| **Connection refused** | Server non in ascolto | Verificare che il server sia avviato |
| **Connection timeout** | Firewall o rete lenta | Aumentare timeout, verificare firewall |
| **Connection reset** | Server chiude improvvisamente | Gestire eccezione, implementare retry |
| **Broken pipe** | Client disconnesso | Verificare connessione prima di scrivere |
| **Address already in use** | Porta già occupata | Usare porta diversa o SO_REUSEADDR |

### 🛠️ **Comandi Sistema per Debug TCP**

```bash
# Monitor connessioni TCP
netstat -an | grep :8080
ss -tuln | grep :8080

# Cattura pacchetti TCP  
tcpdump -i any port 8080
wireshark  # GUI per analisi pacchetti

# Statistiche TCP sistema
cat /proc/net/tcp
ss -s  # Statistiche socket

# Test connessione manuale
telnet localhost 8080
nc -v localhost 8080
```

---

## 📚 **Risorse e Approfondimenti**

### 📖 **RFC e Specifiche**
- **RFC 793** - Transmission Control Protocol
- **RFC 1122** - Requirements for Internet Hosts  
- **RFC 2581** - TCP Congestion Control
- **RFC 3390** - Increasing TCP's Initial Window

### 🔗 **Link Utili**
- [Oracle Socket Programming](https://docs.oracle.com/javase/tutorial/networking/sockets/)
- [TCP/IP Illustrated](https://www.amazon.com/TCP-Illustrated-Volume-Implementation/dp/0321336313)
- [Wireshark TCP Analysis](https://www.wireshark.org/docs/wsug_html_chunked/ChAdvTCPAnalysis.html)

### 💻 **Tools Raccomandati**
- **Wireshark** - Analisi pacchetti
- **iperf3** - Benchmark throughput  
- **tcpdump** - Cattura pacchetti CLI
- **nmap** - Port scanning
- **JProfiler** - Profiling applicazioni Java

---

## 🎯 **Riassunto Chiave**

### **Vantaggi TCP**
- 🛡️ **Affidabilità** - Consegna garantita
- 📊 **Ordinamento** - Dati nell'ordine corretto  
- 🔄 **Controllo flusso** - Previene overflow
- 🚥 **Controllo congestione** - Ottimizza utilizzo rete
- 🔧 **Facile utilizzo** - API semplici in Java

### ⚠️ **Svantaggi TCP**
- 🐌 **Overhead** - Header + acknowledgment
- ⏱️ **Latenza** - 3-way handshake iniziale
- 💾 **Memoria** - Buffer per riordinamento
- 🔄 **Complessità** - Algoritmi controllo interno

### 🎯 **Quando Usare TCP**
- **Web applications** (HTTP/HTTPS)
- **File transfer** (FTP, SFTP)  
- **Email** (SMTP, IMAP)
- **Database** connections
- **Chat applications**
- **API REST**

### 🚫 **Quando NON Usare TCP**
- ❌ **Gaming real-time** (meglio UDP)
- ❌ **Streaming video** (tolleranza perdite)
- ❌ **DNS queries** (singola richiesta/risposta)
- ❌ **IoT sensors** (overhead eccessivo)
- ❌ **Broadcast/multicast** (non supportati)

---

*Guida creata per il corso "Socket Programming in Java" - ITCS Cannizzaro"*  
*Versione 1.0 - Ottobre 2025*