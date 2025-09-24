# 1. Socket UDP - Fondamenti

## Introduzione
I socket UDP (User Datagram Protocol) rappresentano una delle modalità di comunicazione di rete più veloci disponibili in Java. A differenza dei socket TCP, UDP è un protocollo **connectionless** che privilegia la velocità rispetto all'affidabilità.

## Teoria

### Caratteristiche del Protocollo UDP

#### Vantaggi di UDP
- **Velocità**: Nessun handshake o controllo di connessione
- **Overhead ridotto**: Header minimale (8 byte)
- **Broadcast/Multicast**: Supporto nativo per comunicazioni uno-a-molti
- **Semplicità**: API più diretta e immediata

#### Svantaggi di UDP
- **Nessuna garanzia di consegna**: I pacchetti possono essere persi
- **Nessun controllo dell'ordine**: I pacchetti possono arrivare in ordine diverso
- **Nessun controllo di flusso**: Rischio di overflow dei buffer
- **Nessuna verifica di integrità**: Controllo checksum opzionale

### Casi d'Uso Ideali per UDP

```java
// Esempi di applicazioni che beneficiano di UDP:

// 1. Streaming multimediale in tempo reale
// - Video conferenze
// - Gaming online
// - Live streaming

// 2. Servizi di discovery
// - DNS (Domain Name System)
// - DHCP (Dynamic Host Configuration Protocol)
// - mDNS (Multicast DNS)

// 3. Monitoring e telemetria
// - Invio metriche di sistema
// - Logging distribuito
// - IoT sensor data

// 4. Applicazioni time-sensitive
// - Trading ad alta frequenza
// - Controllo industriale
// - Sincronizzazione orologi
```

### Architettura Socket UDP in Java

#### Classi Principali
```java
// DatagramSocket - Socket per invio/ricezione
DatagramSocket socket = new DatagramSocket();

// DatagramPacket - Contenitore per dati + indirizzamento
DatagramPacket packet = new DatagramPacket(buffer, buffer.length, 
                                          address, port);

// InetAddress - Rappresentazione indirizzi IP
InetAddress address = InetAddress.getByName("192.168.1.100");
```

#### Flusso di Comunicazione
```java
/*
 * SERVER UDP:
 * 1. Crea DatagramSocket sulla porta desiderata
 * 2. Crea buffer per ricezione dati
 * 3. Loop: riceve pacchetti e processa richieste
 * 4. Invia risposte ai client (opzionale)
 */

/*
 * CLIENT UDP:
 * 1. Crea DatagramSocket (porta automatica)
 * 2. Prepara dati da inviare in DatagramPacket
 * 3. Invia pacchetto al server
 * 4. Riceve risposta (se prevista)
 */
```

## Implementazione Base

### Server UDP Semplice
```java
import java.net.*;
import java.io.IOException;

public class SimpleUDPServer {
    private static final int BUFFER_SIZE = 1024;
    
    public static void main(String[] args) throws IOException {
        int port = 8888;
        
        // Crea socket legato alla porta
        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("🚀 Server UDP avviato sulla porta " + port);
            
            byte[] buffer = new byte[BUFFER_SIZE];
            
            while (true) {
                // Prepara pacchetto per ricezione
                DatagramPacket receivePacket = new DatagramPacket(
                    buffer, buffer.length);
                
                // Riceve dati (operazione bloccante)
                socket.receive(receivePacket);
                
                // Estrae informazioni
                String message = new String(receivePacket.getData(), 
                                          0, receivePacket.getLength());
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                
                System.out.printf("📨 Ricevuto da %s:%d - %s%n", 
                                clientAddress, clientPort, message);
                
                // Echo back al client
                String response = "Echo: " + message;
                byte[] responseData = response.getBytes();
                
                DatagramPacket sendPacket = new DatagramPacket(
                    responseData, responseData.length, 
                    clientAddress, clientPort);
                
                socket.send(sendPacket);
            }
        }
    }
}
```

### Client UDP Semplice
```java
import java.net.*;
import java.io.IOException;
import java.util.Scanner;

public class SimpleUDPClient {
    public static void main(String[] args) throws IOException {
        String serverHost = "localhost";
        int serverPort = 8888;
        
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(serverHost);
            Scanner scanner = new Scanner(System.in);
            
            System.out.println("💬 Client UDP - digita messaggi (quit per uscire)");
            
            String message;
            while (!(message = scanner.nextLine()).equals("quit")) {
                // Prepara e invia messaggio
                byte[] sendData = message.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length, serverAddress, serverPort);
                
                socket.send(sendPacket);
                
                // Riceve risposta
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(
                    receiveBuffer, receiveBuffer.length);
                
                socket.receive(receivePacket);
                
                String response = new String(receivePacket.getData(), 
                                           0, receivePacket.getLength());
                System.out.println("📨 Risposta: " + response);
            }
        }
    }
}
```

## Gestione Errori e Timeout

### Configurazione Timeout
```java
// Imposta timeout per evitare blocchi indefiniti
socket.setSoTimeout(5000); // 5 secondi

try {
    socket.receive(packet);
} catch (SocketTimeoutException e) {
    System.out.println("⏰ Timeout - nessun pacchetto ricevuto");
} catch (IOException e) {
    System.err.println("💥 Errore I/O: " + e.getMessage());
}
```

### Gestione Packet Loss
```java
// Meccanismo di retry con backoff exponenziale
int maxRetries = 3;
int baseTimeout = 1000;

for (int retry = 0; retry < maxRetries; retry++) {
    try {
        socket.setSoTimeout(baseTimeout * (1 << retry)); // 1s, 2s, 4s
        socket.send(packet);
        socket.receive(responsePacket);
        break; // Successo
        
    } catch (SocketTimeoutException e) {
        System.out.println("⚠️ Tentativo " + (retry + 1) + " fallito");
        if (retry == maxRetries - 1) {
            throw new IOException("Massimo numero di tentativi raggiunto");
        }
    }
}
```

## Buffer Management

### Dimensionamento Buffer
```java
// Considera la MTU (Maximum Transmission Unit)
// Ethernet standard: 1500 bytes
// UDP header: 8 bytes
// IP header: 20 bytes (IPv4) / 40 bytes (IPv6)
// Payload utile: ~1472 bytes per evitare frammentazione

private static final int SAFE_UDP_SIZE = 1400; // Margine di sicurezza
private static final int MAX_UDP_SIZE = 65507;  // Massimo teorico UDP
```

### Buffer Riutilizzabili
```java
// Evita allocazioni continue in loop
byte[] receiveBuffer = new byte[BUFFER_SIZE];
DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

while (running) {
    // Reset lunghezza per riutilizzo
    receivePacket.setLength(receiveBuffer.length);
    
    socket.receive(receivePacket);
    
    // Processa dati...
    processData(receivePacket.getData(), receivePacket.getLength());
}
```

## Considerazioni di Performance

### Thread Safety
```java
// DatagramSocket NON è thread-safe
// Sincronizza accesso concorrente o usa socket separati

// Opzione 1: Sincronizzazione
synchronized(socket) {
    socket.send(packet);
}

// Opzione 2: Socket dedicati per thread
DatagramSocket sendSocket = new DatagramSocket();
DatagramSocket receiveSocket = new DatagramSocket(port);
```

### Buffering del Sistema Operativo
```java
// Aumenta buffer SO per applicazioni ad alto throughput
socket.setReceiveBufferSize(64 * 1024);  // 64KB
socket.setSendBufferSize(64 * 1024);     // 64KB

// Verifica dimensioni effettive (SO può limitare)
System.out.println("Buffer ricezione: " + socket.getReceiveBufferSize());
System.out.println("Buffer invio: " + socket.getSendBufferSize());
```

## Best Practices

### ✅ Raccomandazioni
1. **Usa timeout appropriati** per evitare blocchi
2. **Dimensiona buffer correttamente** per evitare frammentazione
3. **Implementa retry logic** per gestire packet loss
4. **Valida sempre i dati ricevuti** (dimensione e contenuto)
5. **Usa pool di buffer** per applicazioni ad alte performance

### ❌ Errori Comuni
1. **Non gestire SocketTimeoutException**
2. **Buffer troppo piccoli** che causano perdita dati
3. **Assumere consegna garantita** senza verifiche
4. **Non validare indirizzi sorgente** (security risk)
5. **Creare socket in loop** invece di riutilizzarli

## Confronto UDP vs TCP

| Caratteristica | UDP | TCP |
|---|---|---|
| **Connessione** | Connectionless | Connection-oriented |
| **Affidabilità** | Best effort | Garantita |
| **Ordinamento** | Non garantito | Garantito |
| **Velocità** | Molto veloce | Più lento |
| **Overhead** | Minimo (8 byte) | Maggiore (20+ byte) |
| **Broadcast** | Supportato | Non supportato |
| **Controllo flusso** | Assente | Presente |
| **Casi d'uso** | Streaming, gaming, DNS | Web, email, file transfer |

## Prossimi Passi
- **Broadcast e Multicast**: Comunicazione uno-a-molti
- **UDP con reliability**: Implementare ACK e retry
- **Performance tuning**: Ottimizzazioni avanzate
- **Security**: Autenticazione e crittografia UDP

---
[🏠 Torna al Modulo](../README.md) | [➡️ Prossima Lezione](02-Broadcast-Multicast.md)