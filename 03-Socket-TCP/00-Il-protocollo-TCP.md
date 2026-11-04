# 📡 0. Il Protocollo TCP

> *Transmission Control Protocol - Fondamenti e Implementazione Java*

---

## 📋 **Indice**

1. [Introduzione al TCP](#introduzione-al-tcp)
2. [Caratteristiche del TCP](#caratteristiche-del-tcp)
3. [Architettura TCP](#architettura-tcp)
4. [Connessioni TCP](#connessioni-tcp)
5. [Gestione Flusso Dati](#gestione-flusso-dati)
6. [Controllo Errori](#controllo-errori)
7. [TCP in Java](#tcp-in-java)
8. [Esempi Pratici](#esempi-pratici)
9. [Performance e Ottimizzazione](#performance-e-ottimizzazione)
10. [Troubleshooting](#troubleshooting)

---

<a id="introduzione-al-tcp"></a>

## 🎯 **Introduzione al TCP**

### 📖 **Cos'è il TCP**

Il **Transmission Control Protocol (TCP)** è un protocollo di trasporto della suite TCP/IP che fornisce:

- **Connessione affidabile** tra due host
- **Trasferimento affidabile e ordinato** dei byte, oppure segnalazione di un errore
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

### 🏗️ **Modello ISO/OSI**

```
┌─────────────────────────┐
│    Applicazione (7)     │ ← HTTP, FTP, SSH, SMTP
├─────────────────────────┤
│    Presentazione (6)    │ ← Codifica dati (ASCII, JPEG)
├─────────────────────────┤
│    Sessione (5)         │ ← Gestione sessioni
├─────────────────────────┤
│    Trasporto (4)        │ ← TCP/UDP (affidabilità)
├─────────────────────────┤
│    Rete (3)             │ ← IP (routing)
├─────────────────────────┤
│    Collegamento (2)     │ ← Ethernet, WiFi (frame)
├─────────────────────────┤
│    Fisico (1)           │ ← Cavi, onde radio (bit)
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

<a id="caratteristiche-del-tcp"></a>

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

```text
// Esempio concettuale
Client invia: "Hello" (seq=100)
Server risponde: ACK=105 (conferma ricezione di 5 byte)
```

#### **B) Ritrasmissione Automatica**
Se l'ACK non arriva entro il timeout:

```text
Client invia: "World" (seq=105)
// Timeout scaduto, nessun ACK ricevuto
Client ri-invia: "World" (seq=105) // Stesso numero sequenza
```

#### **C) Rilevamento Duplicati**
Il numero di sequenza previene duplicati:

```text
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
Sender Buffer:    [████████░░░░] Window size = 8 byte
Receiver Buffer:  [██░░░░░░░░░░] Available = 2 byte

TCP Header: Window = 2 ← Receiver comunica spazio disponibile
```

### 🚥 **5. Controllo Congestione**

TCP rileva e gestisce la congestione di rete:

```text
// Algoritmi principali
1. Slow Start     → Crescita esponenziale velocità
2. Congestion Avoidance → Crescita lineare  
3. Fast Retransmit → Ritrasmissione rapida
4. Fast Recovery  → Recupero senza slow start
```

---

<a id="architettura-tcp"></a>

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
| **Flags** | 8 bit nel formato base della RFC 9293 | Include SYN, ACK, FIN, RST, PSH, URG, ECE e CWR |
| **Checksum** | 16 bit | Controllo integrità |

### 🚩 **Flag TCP**

```text
SYN = 1  → Stabilire connessione
ACK = 1  → Acknowledgment valido  
FIN = 1  → Chiudere connessione
RST = 1  → Reset connessione (errore)
PSH = 1  → Push immediato ai livelli superiori
URG = 1  → Dati urgenti (raramente usato)
```

---

<a id="connessioni-tcp"></a>

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
// ↑ accept() preleva una connessione dalla coda del sistema operativo
```

### 👋 **Chiusura della connessione**

Il diagramma mostra una chiusura ordinata tipica. FIN e ACK possono essere
combinati, quindi non servono sempre quattro pacchetti distinti; un reset può
interrompere la connessione. La chiusura di un socket non attesta che
l'applicazione remota abbia elaborato tutti i dati.

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
socket.close(); // Chiude il socket: il sistema gestisce la chiusura TCP

// Chiusura con try-with-resources (automatica)
try (Socket socket = new Socket("localhost", 8080)) {
    // Operazioni...
} // close() chiamato automaticamente
```

### 🔄 **Stati Connessione TCP**

```
Socket di ascolto del server: CLOSED → LISTEN
Apertura lato client: CLOSED → SYN_SENT → ESTABLISHED
Apertura lato server, connessione accettata: SYN_RCVD → ESTABLISHED

Chiusura attiva tipica:
ESTABLISHED → FIN_WAIT_1 → FIN_WAIT_2 → TIME_WAIT → CLOSED

Chiusura passiva tipica:
ESTABLISHED → CLOSE_WAIT → LAST_ACK → CLOSED

Il socket di ascolto è distinto dai socket delle connessioni accettate.
Le transizioni possono variare per chiusura simultanea o reset.
```

---

<a id="gestione-flusso-dati"></a>

## 🌊 **Gestione Flusso Dati**

TCP è un **flusso di byte**, non una sequenza di messaggi preservati. Le letture
possono restituire porzioni diverse dalle scritture. Definisci un framing nel
protocollo applicativo: riga, lunghezza o EOF. Un ACK conferma dati al livello
TCP, non l'esito dell'elaborazione applicativa.

### 📏 **Sliding Window**

Il **controllo di flusso** impedisce al mittente di sovraccaricare il ricevitore:

```text
// Esempio semplificato
Sender Window:   [1][2][3][4]░░░░  ← Esempio: quattro byte disponibili
Receiver Buffer: ██░░░░░░░░░░░░░░  ← Esempio: spazio per due byte

TCP Header Window = 2  ← Due byte, prima dell’eventuale window scaling
```

#### **Algoritmo Sliding Window:**

```java
public class SlidingWindow {
    private int windowSize;     // Dimensione finestra
    private int base;           // Primo byte non confermato  
    private int nextSeq;        // Prossimo numero sequenza
    
    public SlidingWindow(int windowSize) {
        if (windowSize <= 0) throw new IllegalArgumentException("Finestra non positiva");
        this.windowSize = windowSize;
    }

    public boolean canSend(int byteCount) {
        return byteCount >= 0 && byteCount <= windowSize - (nextSeq - base);
    }
    
    public void sendData(byte[] data) {
        if (canSend(data.length)) {
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

```text
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

<a id="controllo-errori"></a>

## 🛡️ **Controllo Errori**

### 🔍 **Checksum**

TCP calcola un checksum per rilevare errori utilizzando la somma in complemento a uno dei dati:

```java
// Esempio della somma su parole a 16 bit, non un checksum TCP completo.
// Per TCP occorrono anche pseudo-header IP, header TCP e campo checksum a zero.
public int calculateChecksum(byte[] data) {
    int sum = 0;
    for (int i = 0; i < data.length; i += 2) {
        int word = (data[i] & 0xFF) << 8;
        if (i + 1 < data.length) word |= data[i + 1] & 0xFF;
        sum += word;
        sum = (sum & 0xFFFF) + (sum >>> 16);
    }
    return ~sum & 0xFFFF;
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
        return Math.max(1000, srtt + 4 * rttvar); // Esempio con misure in ms e minimo 1 s
    }
}
```

### 🚀 **Fast Retransmit**

Ritrasmissione rapida senza aspettare timeout:

```
Esempio con segmenti di un byte: seq=100,101,102,103,104,105.
Il byte seq=102 viene perso.
Ricezione di 100 → ACK=101
Ricezione di 101 → ACK=102 (prossimo byte atteso)
Ricezione di 103 → ACK=102 (primo duplicato)
Ricezione di 104 → ACK=102 (secondo duplicato)
Ricezione di 105 → ACK=102 (terzo duplicato)
→ Ritrasmissione del byte seq=102 senza attendere il timeout.
```

---

<a id="tcp-in-java"></a>

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
                // Il sistema gestisce l’handshake; accept() preleva una connessione in coda
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connesso: " + 
                    clientSocket.getRemoteSocketAddress());
                
                // Un client per volta; la concorrenza è trattata nel modulo successivo.
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            System.err.println("Errore server: " + e.getMessage());
        }
    }
    
    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                clientSocket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8)) {
            
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Ricevuto: " + inputLine);
                out.println("Echo: " + inputLine);  // Echo back
            }
        } catch (IOException e) {
            System.err.println("Errore gestione client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close(); // Rilascia il socket e avvia la chiusura TCP
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
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8);
             BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
             Scanner scanner = new Scanner(System.in)) {
            
            System.out.println("Connesso al server " + HOST + ":" + PORT);
            
            String userInput;
            while (true) {
                System.out.print("Messaggio (o 'quit'): ");
                if (!scanner.hasNextLine()) break;
                userInput = scanner.nextLine();
                
                if ("quit".equalsIgnoreCase(userInput)) {
                    break;
                }
                
                out.println(userInput);           // Invia al server
                String response = in.readLine();  // Ricevi risposta
                if (response == null) break;
                System.out.println("Server: " + response);
            }
            
        } catch (IOException e) {
            System.err.println("Errore client: " + e.getMessage());
        } // close() automatico: il sistema gestisce la chiusura TCP
        
        System.out.println("Client disconnesso.");
    }
}
```

### ⚙️ **Configurazioni Socket TCP**

`Socket` e `ServerSocket` sono classi distinte. Configura le opzioni del socket
di ascolto prima del binding; i timeout di lettura si impostano invece sui
socket connessi. Linger è un'opzione avanzata e non sostituisce un protocollo
di chiusura concordato.

```java
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

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
        socket.setSoLinger(true, 10); // Limita l’attesa di close con dati pendenti; opzione avanzata
        
    }

    public static ServerSocket createServerSocket(int port) throws IOException {
        ServerSocket server = new ServerSocket();
        try {
            server.setReuseAddress(true); // Configurare prima del binding.
            server.bind(new InetSocketAddress(port));
            return server;
        } catch (IOException | RuntimeException error) {
            server.close();
            throw error;
        }
    }
}
```

---

<a id="esempi-pratici"></a>

## 💡 **Esempi Pratici**

### 📂 **File Transfer TCP**

Gli helper seguenti vanno salvati in file distinti, aggiungendo gli import
indicati, e chiamati da un client/server. EOF delimita la fine del file: la
chiusura dello stream termina anche la connessione, che non è riutilizzabile
per un altro file. Per un protocollo riutilizzabile occorre dichiarare la
lunghezza del file o un altro framing.

```java
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

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

// Salvare la classe seguente in FileClient.java, con import java.io.* e java.net.*.
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

Il codice completo della [chat con protocollo personalizzato](../04-Multithread/esempi/CustomProtocolChat.java)
è nel modulo multithreading. Prima di introdurre thread e broadcast, verifica
un singolo dialogo request/response con i programmi TCP di questa guida.

### 🔐 **HTTP Server Semplificato**

```java
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class SimpleHTTPServer {
    public static void handleHTTPRequest(Socket client) throws IOException {
        client.setSoTimeout(5000);
        BufferedReader in = new BufferedReader(new InputStreamReader(
                client.getInputStream(), StandardCharsets.ISO_8859_1));
        String requestLine = in.readLine();
        if (requestLine == null) return;
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            // La riga vuota termina gli header, non serve attendere EOF.
        }
        if (line == null) return;
        byte[] body = "Hello from TCP Server!\n".getBytes(StandardCharsets.UTF_8);
        String headers = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/plain; charset=utf-8\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "Connection: close\r\n\r\n";
        OutputStream out = client.getOutputStream();
        out.write(headers.getBytes(StandardCharsets.US_ASCII));
        out.write(body);
        out.flush();
    }
}
```

Questo helper illustra una risposta a una GET e viene chiamato da un server
che chiude il client dopo la risposta. Per il programma eseguibile con gestione
GET/HEAD e rifiuto degli altri metodi vedi [HelloServer](esercizi/b-HelloServer/README.md).

---

<a id="performance-e-ottimizzazione"></a>

## 🚀 **Performance e Ottimizzazione**

### 📊 **Metriche Performance TCP**

```java
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

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
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class TCPOptimization {
    
    // 1. Buffer Size Tuning
    public static void optimizeBuffers(Socket socket) throws IOException {
        // Calcola buffer ottimale: Bandwidth × RTT
        int bandwidth = 100_000_000; // 100 Mbps in bit/s
        int rtt = 50;               // 50ms RTT
        long bufferBytes = (bandwidth / 8L) * rtt / 1000L;
        if (bufferBytes <= 0 || bufferBytes > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Dimensione buffer non valida");
        }
        int optimalBuffer = (int) bufferBytes; // 625000 byte con i valori proposti
        
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
        socket.setSoLinger(false, 0); // Disabilita linger: non richiede un reset immediato
    }
    
    // 4. Thread Pool per server scalabili
    public static ExecutorService createOptimizedThreadPool() {
        int cores = Runtime.getRuntime().availableProcessors();
        return Executors.newFixedThreadPool(cores * 2);
    }
}
```

### 📈 **Benchmark TCP vs UDP — esempio da completare con un ricevitore**

Il tempo di invio locale non è un confronto completo tra TCP e UDP. Avvia un
ricevitore per ciascun protocollo, misura i byte realmente ricevuti e le perdite
UDP, usa lo stesso carico e riporta le condizioni della prova. Il loop di invio
seguente, da solo, non dimostra prestazioni equivalenti o superiori.

```java
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

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

<a id="troubleshooting"></a>

## 🔧 **Troubleshooting**

### 🕵️ **Diagnosi Problemi TCP**

Il metodo seguente è destinato al server echo **a righe** della sezione TCP in
Java, non a EchoService con `readUTF` né a un servizio arbitrario. Il successo di
`isReachable()` dipende dai meccanismi consentiti dal sistema e non è necessario
perché una porta TCP sia raggiungibile.

```java
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

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
            
            // 3. Prova applicativa: il peer deve implementare l’echo a righe UTF-8.
            socket.setSoTimeout(3000);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true,
                    java.nio.charset.StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            long startTime = System.nanoTime();
            out.println("PING");
            String response = in.readLine();
            if (response == null) throw new EOFException("Peer disconnesso");
            long elapsedMillis = (System.nanoTime() - startTime) / 1_000_000;
            System.out.println("Risposta: " + response);
            System.out.println("Tempo richiesta/risposta applicativo: " + elapsedMillis + " ms");
            
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
ss -tanp '( sport = :8080 or dport = :8080 )'

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
- [RFC 9293](https://www.rfc-editor.org/rfc/rfc9293.html) - Specifica TCP corrente, sostituisce RFC 793
- **RFC 1122** - Requirements for Internet Hosts  
- [RFC 5681](https://www.rfc-editor.org/rfc/rfc5681.html) - TCP Congestion Control, sostituisce RFC 2581
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
- 🛡️ **Affidabilità** - Trasferimento ordinato dei byte e rilevamento degli errori
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
- **Streaming video**: la scelta dipende dal protocollo; anche TCP e QUIC sono usati
- **DNS**: usa sia UDP sia TCP; TCP è necessario in diversi scenari
- ❌ **IoT sensors** (overhead eccessivo)
- ❌ **Broadcast/multicast** (non supportati)

---

## Navigazione del Corso
- [📑 Torna all'Indice del Corso](../README.md)
- [⬅️ Modulo precedente](../02-Ambiente-Sviluppo/README.md)
- [➡️ Guida successiva](01-Creazione-Socket-TCP.md)

---

*Corso "Socket Programming in Java" - ITCS Cannizzaro"*  
*Versione 1.0 - Ottobre 2025*
