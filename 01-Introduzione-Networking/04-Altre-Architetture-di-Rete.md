# Architetture di Rete 

## Introduzione

Un'architettura di rete definisce come i nodi (computer, server, dispositivi) comunicano e interagiscono tra loro. Le architetture principali sono:

1. **Client-Server** - Modello centralizzato
2. **Peer-to-Peer (P2P)** - Modello decentralizzato
3. **Hybrid** - Combinazione di entrambi
4. **Publish-Subscribe** - Modello event-driven
5. **Master-Slave** - Sincronizzazione gerarchica

---

## Capitolo 1: Architettura Client-Server

### 1.1 Caratteristiche

```
┌──────────┐
│  Client  │
└─────┬────┘
      │ Richiesta
      ▼
┌──────────┐
│  Server  │
└─────┬────┘
      │ Risposta
      ▼
┌──────────┐
│  Client  │
└──────────┘
```

**Vantaggi:**
- ✅ Gestione centralizzata
- ✅ Controllo e sicurezza facili
- ✅ Manutenzione semplice
- ✅ Logica concentrata sul server

**Svantaggi:**
- ❌ Single point of failure (il server)
- ❌ Bottleneck sul server
- ❌ Scalabilità limitata
- ❌ Costi elevati di infrastruttura

### 1.2 Modello Classico

```java
// SERVER
ServerSocket server = new ServerSocket(5000);
while (true) {
    Socket client = server.accept();
    new Thread(new Handler(client)).start();
}

// CLIENT
Socket socket = new Socket("localhost", 5000);
PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
out.println("Richiesta");
```

### 1.3 Varianti

**Request-Reply (RPC)**
```
Client → Richiesta → Server
         ← Risposta ←
```

**Publish-Subscribe (Pull)**
```
Client → Chiede aggiornamenti → Server
```

---

## Capitolo 2: Architettura Peer-to-Peer (P2P)

### 2.1 Concetto Fondamentale

```
┌─────────┐     ┌─────────┐
│ Peer A  │────→│ Peer B  │
│ (S+C)   │←────│ (S+C)   │
└─────────┘     └─────────┘
     ↓              ↑
     └──────┬───────┘
            ↓
       ┌─────────┐
       │ Peer C  │
       │ (S+C)   │
       └─────────┘
```

Ogni nodo è **contemporaneamente server e client**.

### 2.2 Caratteristiche

**Vantaggi:**
- ✅ No single point of failure
- ✅ Scalabilità orizzontale
- ✅ Distribuzione del carico
- ✅ Resilienza (tolleranza ai guasti)
- ✅ Autonomia dei nodi

**Svantaggi:**
- ❌ Complessità maggiore
- ❌ Difficile sincronizzazione
- ❌ Scoperta dei nodi complessa
- ❌ Sicurezza più difficile
- ❌ Performance variabile

### 2.3 Implementazione P2P Base

#### PeerNode.java

```java
import java.io.*;
import java.net.*;
import java.util.*;

public class PeerNode {
    private int nodeId;
    private int port;
    private ServerSocket serverSocket;
    private List<PrintWriter> connectedPeers = 
        Collections.synchronizedList(new ArrayList<>());

    public PeerNode(int nodeId, int port) {
        this.nodeId = nodeId;
        this.port = port;
    }

    /**
     * Avvia il peer come server (ascolta connessioni)
     */
    public void startServer() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("🔵 Peer #" + nodeId + " ascolta su porta " + port);

        new Thread(() -> {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("✅ Peer #" + nodeId + " accetta connessione");
                    
                    new Thread(new PeerHandler(socket, this)).start();
                } catch (IOException e) {
                    System.err.println("❌ Errore: " + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Si connette a un altro peer come client
     */
    public void connectToPeer(String host, int peerPort) throws IOException {
        Socket socket = new Socket(host, peerPort);
        PrintWriter out = new PrintWriter(
            socket.getOutputStream(), true);
        
        connectedPeers.add(out);
        System.out.println("🟢 Peer #" + nodeId + " connesso a " + host + ":" + peerPort);
    }

    /**
     * Invia messaggio a tutti i peer connessi
     */
    public void broadcastMessage(String message) {
        System.out.println("[Peer #" + nodeId + "] " + message);
        
        for (PrintWriter peer : connectedPeers) {
            peer.println("[Peer #" + nodeId + "] " + message);
            peer.flush();
        }
    }

    /**
     * Riceve messaggio da un altro peer
     */
    public void receiveMessage(String message) {
        System.out.println(message);
        broadcastMessage("Forward: " + message);
    }

    public int getNodeId() {
        return nodeId;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // Crea 3 peer
        PeerNode peer1 = new PeerNode(1, 5001);
        PeerNode peer2 = new PeerNode(2, 5002);
        PeerNode peer3 = new PeerNode(3, 5003);

        // Avvia i server
        peer1.startServer();
        peer2.startServer();
        peer3.startServer();

        Thread.sleep(1000);

        // Connessioni: P2 e P3 si connettono a P1
        peer2.connectToPeer("localhost", 5001);
        peer3.connectToPeer("localhost", 5001);

        Thread.sleep(1000);

        // Comunica
        peer1.broadcastMessage("Ciao da Peer 1");
        
        Thread.sleep(500);
        
        peer2.broadcastMessage("Risposta da Peer 2");
    }
}

class PeerHandler implements Runnable {
    private Socket socket;
    private PeerNode peer;

    public PeerHandler(Socket socket, PeerNode peer) {
        this.socket = socket;
        this.peer = peer;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            
            String message;
            while ((message = in.readLine()) != null) {
                peer.receiveMessage(message);
            }
        } catch (IOException e) {
            System.err.println("❌ Errore: " + e.getMessage());
        }
    }
}
```

### 2.4 Scoperta dei Nodi (Node Discovery)

Un aspetto critico della P2P è scoprire nuovi nodi.

#### Metodo 1: Hardcoded

```java
// Elenco statico di nodi noti
List<String> knownPeers = Arrays.asList(
    "192.168.1.10:5000",
    "192.168.1.11:5000",
    "192.168.1.12:5000"
);

for (String peer : knownPeers) {
    String[] parts = peer.split(":");
    connectToPeer(parts[0], Integer.parseInt(parts[1]));
}
```

#### Metodo 2: Broadcast Discovery

```java
public void discoverPeers() throws IOException {
    DatagramSocket socket = new DatagramSocket();
    socket.setBroadcast(true);

    // Invia pacchetto broadcast
    byte[] buffer = "PEER_DISCOVERY".getBytes();
    DatagramPacket packet = new DatagramPacket(
        buffer, buffer.length, 
        InetAddress.getByName("255.255.255.255"), 5000);

    socket.send(packet);
    socket.close();
    
    System.out.println("📡 Discovery packet inviato");
}
```

#### Metodo 3: DHT (Distributed Hash Table)

```java
// Ogni peer conosce alcuni altri peer
// Usa una hash table distribuita per trovare risorse
Map<String, String> dht = new ConcurrentHashMap<>();

public void registerResource(String key, String value) {
    dht.put(key, value);
    // Replica su altri peer
}

public String lookupResource(String key) {
    if (dht.containsKey(key)) {
        return dht.get(key);
    }
    // Chiedi ai peer vicini
    return queryOtherPeers(key);
}
```

---

## Capitolo 3: Architettura Hybrid (Client-Server + P2P)

### 3.1 Concetto

```
    ┌─────────────┐
    │   Server    │
    │  Centrale   │
    └──────┬──────┘
           │
    ┌──────┼───────┐
    ▼      ▼       ▼
  ┌────┐ ┌────┐ ┌────┐
  │ P1 │ │ P2 │ │ P3 │
  └─┬─ ┘ └─┬──┘ └─┬──┘
    └──────┼──────┘
           ▼
      P2P Network
```

Server centrale per:
- Coordinamento
- Registrazione nodi
- Sincronizzazione

Peer-to-Peer per:
- Trasferimento dati diretto
- Ridondanza
- Scalabilità

### 3.2 Implementazione

#### CentralServer.java

```java
import java.io.*;
import java.net.*;
import java.util.*;

public class CentralServer {
    private ServerSocket serverSocket;
    private Map<String, PeerInfo> registeredPeers = 
        Collections.synchronizedMap(new HashMap<>());

    public CentralServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("🟡 Server Centrale avviato porta " + port);
    }

    public void start() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                new Thread(new CentralHandler(socket, this)).start();
            } catch (IOException e) {
                System.err.println("❌ Errore: " + e.getMessage());
            }
        }
    }

    /**
     * Registra un peer
     */
    public synchronized void registerPeer(String peerId, String host, int port) {
        registeredPeers.put(peerId, new PeerInfo(peerId, host, port));
        System.out.println("📝 Peer registrato: " + peerId + " (" + host + ":" + port + ")");
        broadcastPeerList();
    }

    /**
     * Invia lista di tutti i peer a tutti i client
     */
    private void broadcastPeerList() {
        // Notifica tutti i client connessi della lista aggiornata
    }

    public Map<String, PeerInfo> getPeers() {
        return new HashMap<>(registeredPeers);
    }

    public static void main(String[] args) throws IOException {
        new CentralServer(6000).start();
    }
}

class PeerInfo {
    public String id;
    public String host;
    public int port;

    public PeerInfo(String id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
    }

    @Override
    public String toString() {
        return id + " (" + host + ":" + port + ")";
    }
}

class CentralHandler implements Runnable {
    private Socket socket;
    private CentralServer server;

    public CentralHandler(Socket socket, CentralServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);

            String request;
            while ((request = in.readLine()) != null) {
                // Formato: REGISTER|peerId|host|port
                if (request.startsWith("REGISTER")) {
                    String[] parts = request.split("\\|");
                    String peerId = parts[1];
                    String host = parts[2];
                    int port = Integer.parseInt(parts[3]);

                    server.registerPeer(peerId, host, port);
                    out.println("OK");
                }
                // Formato: LIST
                else if (request.equals("LIST")) {
                    for (PeerInfo peer : server.getPeers().values()) {
                        out.println(peer);
                    }
                    out.println("END");
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Errore: " + e.getMessage());
        }
    }
}
```

#### HybridPeer.java

```java
import java.io.*;
import java.net.*;
import java.util.*;

public class HybridPeer {
    private String peerId;
    private int port;
    private String centralServerHost;
    private int centralServerPort;

    public HybridPeer(String peerId, int port, 
                      String centralHost, int centralPort) {
        this.peerId = peerId;
        this.port = port;
        this.centralServerHost = centralHost;
        this.centralServerPort = centralPort;
    }

    /**
     * Registra questo peer al server centrale
     */
    public void registerWithCentral() throws IOException {
        Socket socket = new Socket(centralServerHost, centralServerPort);
        PrintWriter out = new PrintWriter(
            socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));

        // Invia registrazione
        String localHost = InetAddress.getLocalHost().getHostAddress();
        out.println("REGISTER|" + peerId + "|" + localHost + "|" + port);

        String response = in.readLine();
        if ("OK".equals(response)) {
            System.out.println("✅ " + peerId + " registrato al server centrale");
        }

        // Chiedi lista peer
        out.println("LIST");
        String peerLine;
        while ((peerLine = in.readLine()) != null && !peerLine.equals("END")) {
            System.out.println("🔷 Peer scoperto: " + peerLine);
        }

        socket.close();
    }

    /**
     * Avvia il server locale per comunicare con altri peer
     */
    public void startLocalServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("🔵 " + peerId + " ascolta su porta " + port);

        new Thread(() -> {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                    
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("📬 " + peerId + " ricevuto: " + message);
                    }
                } catch (IOException e) {
                    System.err.println("❌ Errore: " + e.getMessage());
                }
            }
        }).start();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // Crea tre peer hybrid
        HybridPeer peer1 = new HybridPeer("Peer1", 5001, "localhost", 6000);
        HybridPeer peer2 = new HybridPeer("Peer2", 5002, "localhost", 6000);
        HybridPeer peer3 = new HybridPeer("Peer3", 5003, "localhost", 6000);

        // Avvia server locali
        peer1.startLocalServer();
        peer2.startLocalServer();
        peer3.startLocalServer();

        Thread.sleep(500);

        // Registra al server centrale
        peer1.registerWithCentral();
        peer2.registerWithCentral();
        peer3.registerWithCentral();
    }
}
```

---

## Capitolo 4: Comparazione Architetture

### 4.1 Tabella Comparativa

| Aspetto | Client-Server | P2P | Hybrid |
|---------|---------------|-----|--------|
| **Scalabilità** | Media | Ottima | Ottima |
| **Resilienza** | Scarsa | Eccellente | Buona |
| **Semplicità** | Alta | Bassa | Media |
| **Sicurezza** | Buona | Difficile | Buona |
| **Latenza** | Bassa | Media | Bassa |
| **Costi** | Alti | Bassi | Medi |
| **Sincronizzazione** | Facile | Difficile | Media |
| **Point of Failure** | Server | Nessuno | Server |

### 4.2 Quando Usare Cosa?

**Client-Server:**
- ✅ Applicazioni aziendali
- ✅ Banche e finanza
- ✅ E-commerce
- ✅ Quando serve forte controllo

**P2P:**
- ✅ File sharing (Torrent)
- ✅ Criptovalute (Bitcoin)
- ✅ Mesh networks
- ✅ Quando serve distribuzione

**Hybrid:**
- ✅ Social media
- ✅ Giochi multiplayer
- ✅ VoIP (Skype)
- ✅ CDN (Content Delivery)

---

## Capitolo 5: Altre Architetture

### 5.1 Master-Slave (Replication)

```
        ┌────────────┐
        │   Master   │
        │(write+read)│
        └──────┬─────┘
               │
        ┌──────┼──────┐
        ▼      ▼      ▼
     ┌─────┐┌─────┐┌─────┐
     │ S1  ││ S2  ││ S3  │
     │(ro) ││(ro) ││(ro) │
     └─────┘└─────┘└─────┘
```

Usa: Database replication, Backup

### 5.2 Publish-Subscribe (Event-Driven)

```
  ┌────────────────┐
  │   Publisher    │
  │   (Sensore)    │
  └────────┬───────┘
           │ Evento
           ▼
  ┌────────────────┐
  │    Broker      │
  │ (Message Queue)│
  └────────┬───────┘
           │
    ┌──────┼──────┐
    ▼      ▼      ▼
  Sub1    Sub2   Sub3
```

Usa: Kafka, RabbitMQ, Event streaming

### 5.3 Serverless (Cloud Functions)

```
  Client → API Gateway → Function1
                      → Function2
                      → Function3
                      ↓
                    Database
```

Usa: AWS Lambda, Google Cloud Functions

---

## Capitolo 6: Esercizi Proposti

### Esercizio 1: Client-Server Esteso
Estendi il modello client-server con:
- Autenticazione
- Database di utenti
- Logging delle richieste
- Rate limiting

### Esercizio 2: P2P File Sharing
Crea un file sharing P2P dove:
- Ogni peer può caricare file
- Gli altri peer possono scaricare
- Usa DHT per scoperta file
- Implementa chunking e multipart download

### Esercizio 3: Hybrid Chat
Sviluppa una chat hybrid dove:
- Server centrale gestisce utenti
- P2P per messaggi diretti tra peer
- Fallback a server se P2P non disponibile

### Esercizio 4: Master-Slave Database
Realizza un database master-slave dove:
- Master accetta write
- Slave replica i dati
- Failover automatico se master cade

---

## Capitolo 7: Domande di Autovalutazione

### Domanda 1
Qual è la differenza principale tra Client-Server e P2P?

A) Client-Server è più veloce  
B) Client-Server ha un server centrale, P2P è decentralizzato  
C) P2P richiede internet, Client-Server no  
D) Non c'è differenza  

**Risposta corretta: B**

Client-Server ha un server centralizzato, P2P non ha punto centrale - ogni nodo è sia client che server.

---

### Domanda 2
Quale architettura è più scalabile?

A) Client-Server  
B) P2P  
C) Hybrid  
D) Tutte uguali  

**Risposta corretta: B**

P2P scala orizzontalmente: più nodi aggiungi, più capacità aggiungi. Client-Server ha bottleneck sul server.

---

### Domanda 3
Perché P2P è difficile da sincronizzare?

A) Troppi nodi  
B) Nessun punto di controllo centrale  
C) Sempre lento  
D) Problema di rete  

**Risposta corretta: B**

Senza punto centrale, è difficile coordinare, ordinare e sincronizzare lo stato tra nodi.

---

### Domanda 4
Qual è il vantaggio principale di Hybrid?

A) Più semplice di Client-Server  
B) Combina vantaggi di entrambi  
C) Più veloce di P2P  
D) Nessun failover  

**Risposta corretta: B**

Hybrid ha controllo centrale del server + scalabilità e resilienza di P2P.

---

### Domanda 5
Bitcoin usa quale architettura?

A) Client-Server  
B) P2P pura  
C) Hybrid  
D) Master-Slave  

**Risposta corretta: B**

Bitcoin è P2P pura decentralizzata - nessun server centrale, ogni nodo è indipendente.

---

## Risposte Corrette

| Q | Risposta | Spiegazione |
|---|----------|-------------|
| 1 | B | Client-Server centralizzato, P2P decentralizzato |
| 2 | B | P2P scala orizzontalmente |
| 3 | B | Nessun punto di controllo centrale in P2P |
| 4 | B | Hybrid combina vantaggi di entrambi |
| 5 | B | Bitcoin è P2P decentralizzata pura |

---

## Applicazioni Reali

| Applicazione | Architettura | Perché |
|-------------|------------|--------|
| Facebook | Hybrid | Server centrale + CDN P2P |
| Torrent | P2P | Distribuzione decentralizzata |
| Email | Client-Server | Controllo centralizzato |
| Skype | Hybrid | Server + P2P peer-to-peer |
| Bitcoin | P2P | Decentralizzazione assoluta |
| Netflix | Hybrid | Server + Cache distribuita |
| WhatsApp | Hybrid | Server + P2P end-to-end |
| DNS | Client-Server | Gerarchia centralizzata |
| IPFS | P2P | Content-addressed P2P |

---

## Conclusione

Le architetture di rete fondamentali sono:

1. **Client-Server** - Semplice, centralizzato, controllato
2. **P2P** - Scalabile, distribuito, resiliente
3. **Hybrid** - Equilibrio tra i due

La scelta dipende dai requisiti:
- **Controllo e semplicità** → Client-Server
- **Scalabilità e resilienza** → P2P
- **Equilibrio** → Hybrid
- **Event-driven** → Publish-Subscribe
- **Replicazione** → Master-Slave

Non esiste una "migliore": ogni architettura ha il suo caso d'uso ideale.

---

## Navigazione del Corso
- [📑 Torna all'Indice del Corso](../README.md)
- [⬅️ Guida Precedente](03-Architetture-Client-Server.md)
- [➡️ Guida Successiva](05-Tipologie-di-Applicazioni.md)