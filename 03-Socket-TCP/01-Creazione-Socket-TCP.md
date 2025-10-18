# 1. Creazione Socket TCP

## Introduzione
I socket TCP sono la base della comunicazione affidabile in rete. Comprendere come crearli, configurarli e gestirli correttamente è fondamentale per sviluppare applicazioni robuste.

## Teoria

### Classe Socket (Client TCP)

La classe `Socket` rappresenta un endpoint client per la comunicazione TCP:

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
socket.setSoTimeout(30000);          // Timeout read/write (30s)
socket.setReceiveBufferSize(64 * 1024); // Buffer ricezione 64KB
socket.setSendBufferSize(64 * 1024);    // Buffer invio 64KB
```

### Classe ServerSocket (Server TCP)

La classe `ServerSocket` gestisce l'ascolto e l'accettazione di connessioni:

```java
// Costruttori
ServerSocket serverSocket = new ServerSocket(8080);           // Porta specifica
ServerSocket serverSocket = new ServerSocket(8080, 50);      // Con backlog
ServerSocket serverSocket = new ServerSocket(8080, 50, 
    InetAddress.getByName("192.168.1.100"));                // IP specifico
```

#### Configurazione ServerSocket
```java
serverSocket.setReuseAddress(true);     // Riusa indirizzo
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

### Gestione delle Eccezioni

#### Eccezioni Comuni
- **ConnectException**: Server non raggiungibile
- **SocketTimeoutException**: Timeout operazione
- **BindException**: Porta già in uso
- **UnknownHostException**: Host non risoluto

```java
try {
    Socket socket = new Socket("localhost", 8080);
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

- [Esempio 01: Client TCP Base](./esempi/es01/ClientTCPBase.java) - Client TCP con gestione errori
- [Esempio 02: Server TCP Base](./esempi/es01/ServerTCPBase.java) - Server TCP configurabile

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