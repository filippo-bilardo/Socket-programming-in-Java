# 1. Creazione Socket TCP

## Introduzione
I socket TCP sono la base della comunicazione affidabile in rete. Comprendere come crearli, configurarli e gestirli correttamente è fondamentale per sviluppare applicazioni robuste.

## Teoria

### Classe Socket (Client TCP)

La classe `Socket` rappresenta un endpoint connesso TCP; viene usata dal client
e rappresenta anche la connessione accettata dal server. I costruttori seguenti
sono alternative: scegli una sola dichiarazione di `socket`.

```java
// Costruttori principali
Socket socket = new Socket();                    // Socket non connesso
Socket socket = new Socket("localhost", 8080);   // Connessione immediata
Socket socket = new Socket(InetAddress.getByName("192.168.1.1"), 8080);
```

#### Connessione Manuale
```java
Socket socket = new Socket();
socket.connect(new InetSocketAddress("localhost", 8080), 5000); // timeout 5s
```

#### Configurazione Socket Client
```java
socket.setTcpNoDelay(true);          // Disabilita algoritmo Nagle
socket.setKeepAlive(true);           // Abilita keep-alive
socket.setSoTimeout(30000);          // Timeout delle letture (30s); non delle scritture
socket.setReceiveBufferSize(64 * 1024); // Buffer ricezione 64KB
socket.setSendBufferSize(64 * 1024);    // Buffer invio 64KB
```

### Classe ServerSocket (Server TCP)

La classe `ServerSocket` gestisce l'ascolto e l'accettazione di connessioni.
Anche i seguenti costruttori sono alternative, non istruzioni da eseguire insieme:

```java
// Costruttori
ServerSocket serverSocket = new ServerSocket(8080);           // Porta specifica
ServerSocket serverSocket = new ServerSocket(8080, 50);      // Con backlog
// Il backlog richiede una coda di connessioni in attesa: il limite effettivo dipende dal sistema
ServerSocket serverSocket = new ServerSocket(8080, 50, 
    InetAddress.getByName("192.168.1.100"));                // IP specifico
```

#### Configurazione ServerSocket

Questo blocco è un’alternativa ai costruttori che eseguono subito il binding:

```java
// Se vuoi configurare il riuso, fallo prima del bind:
ServerSocket serverSocket = new ServerSocket();
serverSocket.setReuseAddress(true);
serverSocket.bind(new InetSocketAddress(8080));
serverSocket.setSoTimeout(10000);       // Timeout accept (10s)
serverSocket.setReceiveBufferSize(128 * 1024); // Buffer ricezione
```

#### Accettazione Connessioni
```java
while (true) {
    try {
        Socket clientSocket = serverSocket.accept(); // Bloccante
        // Gestisci client...
    } catch (SocketTimeoutException e) {
        // Timeout accept - continua il loop
    }
}
```

`connect(..., timeout)` limita l’apertura; `Socket.setSoTimeout` limita le letture.
Il timeout impostato sul `ServerSocket` riguarda invece `accept()`, non i client
accettati. Configura il socket del client per il suo timeout di lettura.

### Gestione delle Eccezioni

#### Eccezioni Comuni
- **ConnectException**: Server non raggiungibile
- **SocketTimeoutException**: Timeout operazione
- **BindException**: Impossibile associare l’indirizzo/porta richiesti, per esempio perché occupati
- **UnknownHostException**: Host non risoluto

```java
try (Socket socket = new Socket()) {
    socket.connect(new InetSocketAddress("localhost", 8080), 5000);
    // Operazioni...
} catch (ConnectException e) {
    System.err.println("Server non disponibile: " + e.getMessage());
} catch (SocketTimeoutException e) {
    System.err.println("Timeout connessione: " + e.getMessage());
} catch (IOException e) {
    System.err.println("Errore I/O: " + e.getMessage());
}
```

## 🔗 Esempi di Riferimento

- [Esempio a: client EchoService](esercizi/a-EchoService/TcpClient.java) - Client TCP con gestione errori
- [Esempio a: server EchoService](esercizi/a-EchoService/TcpServer.java) - Server TCP configurabile

## 💡 Best Practices, Tips & Tricks

- **Try-with-resources**: Sempre per gestione automatica risorse
- **Timeout appropriati**: Evita attese infinite
- **Configurazione buffer**: Ottimizza per il tipo di applicazione
- **Gestione eccezioni**: Specifica per ogni tipo di errore
- **Riuso indirizzi**: `setReuseAddress(true)` per server

⚠️ **Errori Comuni**:
- Non chiudere i socket correttamente
- Timeout troppo corti o assenti
- Ignorare le eccezioni specifiche
- Buffer size inadeguati

## Navigazione del Corso
- [📑 Torna all'Indice del Corso](../README.md)
- [➡️ Guida Successiva](02-Comunicazione-Dati-TCP.md)
