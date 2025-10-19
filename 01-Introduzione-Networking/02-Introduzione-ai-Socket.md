# 2. Introduzione ai Socket

## Introduzione
I socket rappresentano l'interfaccia di programmazione fondamentale per la comunicazione di rete. Comprendere cosa sono e come funzionano è essenziale per qualsiasi sviluppatore che voglia creare applicazioni di rete in Java.

## Teoria

### Cos'è un Socket

Un **Socket** è un **endpoint di comunicazione** che permette a due processi di scambiarsi dati attraverso una rete. È l'astrazione che nasconde i dettagli complessi della comunicazione di rete.

**Analogia**: Pensa al socket come a una "presa elettrica" per la rete - fornisce un punto di connessione standardizzato tra due applicazioni.

### Identificazione di un servizio tramite Socket
Ecco sintetizzato il concetto di **identificazione** di un host e di un servizio mostrato nell’immagine:

```markdown
| Identificazione Host                     | Identificazione Servizio                     |
|------------------------------------------|----------------------------------------------|
| **nome**                                 | **processo**                                 |
| ↓                                        | ↓                                            |
| **indirizzo IP**                         | **protocollo**      **porta**                |
| ↓                                        | ↓                                            |
| **host**                                 | **servizio**                                 |
| (globally unique)                        | (specifico dell’host)                        |
```

Un numero di porta non può essere associato a più processi (server o client) ma un server può offrire più 
servizi, ciascuno su un’apposita porta.

La socket permette di identificare in modo preciso un servizio su un host in una rete, consentendo alle applicazioni di comunicare tra loro in modo affidabile e ordinato.

### Comunicazione client-server tramite Socket

1. **Socket Client**
   - Si trova nel processo client.
   - Viene utilizzato per avviare la connessione verso il server.

2. **Socket di ascolto (Welcome Socket, Listening Socket)**
   - Si trova nel processo server.
   - È in ascolto su una porta specifica, pronto a ricevere richieste di connessione dai client.

3. **Socket di Connessione**
   - Viene creato dal server una volta che il socket di ascolto accetta una richiesta di connessione dal client.
   - Questo socket gestisce la comunicazione effettiva tra client e server.

Fasi della Comunicazione

1. **Handshaking**
   - Il client invia una richiesta di connessione al socket di ascolto del server.
   - Il server accetta la richiesta e crea un socket di connessione dedicato per comunicare con quel client.

2. **Scambio di Dati**
   - Una volta stabilita la connessione, i byte possono essere scambiati tra il socket client e il socket di connessione del server.
   - Questo scambio avviene in entrambi i sensi: il client può inviare dati al server e viceversa.

Esempio Pratico

- **Client**: Un browser web che tenta di connettersi a un sito web.
- **Server**: Un server web che ospita il sito.
- **Socket di Ascolto**: In ascolto sulla porta 80 (HTTP).
- **Socket di Connessione**: Creato dal server per gestire la connessione con il browser.

Il browser (client) invia una richiesta di connessione al server sulla porta 80. Il server accetta la richiesta e crea un socket di connessione per gestire la comunicazione con il browser. Ora, il browser e il server possono scambiarsi dati (pagine web, immagini, ecc.).


### Storia e Evoluzione dei Socket

I socket furono introdotti per la prima volta nel 1983 con **Berkeley Software Distribution (BSD) 4.2**. L'API Berkeley Sockets è diventata lo standard de facto per la programmazione di rete e influenza ancora oggi le implementazioni moderne.

**Evoluzione temporale:**
- **1981**: Prima implementazione in BSD 4.1c
- **1983**: BSD Sockets (Berkeley)
- **1991**: Windows Sockets (Winsock)
- **1995**: Java Socket API
- **2008**: HTML5 WebSockets
- **Oggi**: Supportati in tutti i linguaggi moderni (Socket.io, WebRTC, QUIC, ecc.) 

### Tipologie di Socket

#### 1. Socket TCP (SOCK_STREAM, stream socket)
```java
// Socket orientato alla connessione
Socket clientSocket = new Socket("localhost", 8080);
ServerSocket serverSocket = new ServerSocket(8080);
```

**Caratteristiche**:
- Connessione affidabile
- Flusso continuo di dati  
- Controllo errori automatico
- ⚠️ Overhead maggiore

#### 2. Socket UDP (SOCK_DGRAM, datagram socket)
```java
// Socket senza connessione
DatagramSocket socket = new DatagramSocket(8080);
```

**Caratteristiche**:
- ⚡ Comunicazione veloce
- 📦 Messaggi discreti (datagrammi)
- 🚫 Nessuna garanzia di consegna
- ⚡ Overhead minimo

#### 3. **Socket Raw**
Permettono l'accesso diretto ai protocolli di livello inferiore (IP, ICMP). Richiedono privilegi di amministratore e sono usati per:
- Implementare protocolli personalizzati
- Network monitoring e debugging
- Security tools (port scanning, packet crafting)

#### 4. **Socket Unix Domain (UDS)**
Per comunicazione **inter-process** sulla stessa macchina:
```python
# Esempio Socket Unix
import socket

sock = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
sock.bind("/tmp/my_socket")
```

### Architettura dei Socket nel Sistema

```
┌─────────────────┐    ┌─────────────────┐
│   Applicazione  │    │   Applicazione  │
├─────────────────┤    ├─────────────────┤
│   Socket API    │ ←→ │   Socket API    │
├─────────────────┤    ├─────────────────┤
│  TCP/UDP Layer  │    │  TCP/UDP Layer  │
├─────────────────┤    ├─────────────────┤
│    IP Layer     │    │    IP Layer     │
├─────────────────┤    ├─────────────────┤
│ Physical Network│ ←→ │ Physical Network│
└─────────────────┘    └─────────────────┘
    Client Side            Server Side
```

### Socket API POSIX

L'API Socket segue un pattern comune in tutti i sistemi operativi:

```
socket()  → Crea il socket
bind()    → Associa a un indirizzo
listen()  → Mette in ascolto (solo server)
accept()  → Accetta connessioni (solo server)
connect() → Si connette (solo client)
send()    → Invia dati
recv()    → Riceve dati
close()   → Chiude la connessione
```

### Socket in Java: java.net

Java fornisce un'API ad alto livello che semplifica l'uso dei socket:

| **Classe Java** | **Tipo** | **Uso** |
|----------------|----------|---------|
| `Socket` | TCP Client | Connessione a un server |
| `ServerSocket` | TCP Server | Accetta connessioni client |
| `DatagramSocket` | UDP | Invio/ricezione datagrammi |
| `MulticastSocket` | UDP Multicast | Comunicazione multicast |

## 🔗 Esempi di Riferimento

- [Esempio 01: Verifica Connettività](./esempi/TestConnettivita.java) - Testa se una porta è aperta
- [Esempio 02: Socket Info](./esempi/SocketInfo.java) - Mostra informazioni sui socket

## 💡 Best Practices, Tips & Tricks

- **Gestisci sempre le eccezioni**: Le operazioni di rete possono fallire
- **Chiudi le risorse**: Usa try-with-resources per garantire la chiusura
- **Scegli il tipo giusto**: TCP per affidabilità, UDP per velocità
- **Considera i timeout**: Evita attese infinite
- **Gestisci gli indirizzi**: Usa InetAddress per la risoluzione DNS

⚠️ **Errori Comuni da Evitare**:
- Non chiudere i socket (memory leak)
- Ignorare le eccezioni di rete
- Usare thread bloccanti senza timeout
- Non gestire la riconnessione automatica

## 🧠 Verifica dell'Apprendimento

### Domande a Scelta Multipla

1. **Cosa rappresenta un socket in Java?**  
    a) Un'interfaccia grafica per l'utente  
    b) Un endpoint per la comunicazione di rete  
    c) Un tipo di database relazionale

2. **Quale classe Java viene utilizzata per creare un socket client TCP?**  
    a) ServerSocket  
    b) DatagramSocket  
    c) Socket

3. **Qual è la differenza principale tra TCP e UDP?**  
    a) TCP è orientato alla connessione, UDP no  
    b) UDP supporta solo connessioni locali  
    c) TCP non gestisce errori di trasmissione

### Risposte alle Domande
1. **Risposta corretta: b)** Un socket è un endpoint che permette la comunicazione tra processi su una rete.
2. **Risposta corretta: c)** La classe Socket viene utilizzata per creare un client TCP.
3. **Risposta corretta: a)** TCP garantisce connessione affidabile e ordinata, mentre UDP è senza connessione.

### Proposte di Esercizi
- **Esercizio 1 (Facile)**: Scrivi un programma che verifica se una porta specifica è aperta su localhost.
- **Esercizio 2 (Intermedio)**: Crea un'applicazione che mostra tutte le informazioni disponibili su un socket.
- **Esercizio 3 (Avanzato)**: Implementa un semplice port scanner che testa la connettività su un range di porte.

## Navigazione del Corso
- [📑 Torna all'Indice del Corso](../README.md)
- [⬅️ Guida Precedente](01-Concetti-Fondamentali-Networking.md)
- [➡️ Guida Successiva](03-Architetture-Client-Server.md)