# 2. Introduzione ai Socket

## Introduzione
I socket rappresentano l'interfaccia di programmazione fondamentale per la comunicazione di rete. Comprendere cosa sono e come funzionano è essenziale per qualsiasi sviluppatore che voglia creare applicazioni di rete in Java.

## Teoria

### Cos'è un Socket

Un **Socket** è un **endpoint di comunicazione** che permette a due processi di scambiarsi dati attraverso una rete. È l'astrazione che nasconde i dettagli complessi della comunicazione di rete.

**Analogia**: Pensa al socket come a una "presa elettrica" per la rete - fornisce un punto di connessione standardizzato tra due applicazioni.

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