# 3. Chiusura Connessioni

## Introduzione
La corretta gestione della chiusura delle connessioni TCP è cruciale per prevenire memory leak e garantire che le risorse vengano rilasciate appropriatamente.

## Teoria

### Chiusura Corretta dei Socket

#### Try-with-resources (Raccomandato)
```java
try (Socket socket = new Socket("localhost", 8080);
     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
    
    // Comunicazione...
    out.println("Hello");
    String response = in.readLine();
    
} catch (IOException e) {
    System.err.println("Errore: " + e.getMessage());
}
// Socket e stream chiusi automaticamente
```

#### Chiusura Manuale
```java
Socket socket = null;
BufferedReader in = null;
PrintWriter out = null;

try {
    socket = new Socket("localhost", 8080);
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    out = new PrintWriter(socket.getOutputStream(), true);
    
    // Comunicazione...
    
} catch (IOException e) {
    System.err.println("Errore: " + e.getMessage());
} finally {
    // Chiusura in ordine inverso
    if (out != null) out.close();
    if (in != null) try { in.close(); } catch (IOException e) { }
    if (socket != null) try { socket.close(); } catch (IOException e) { }
}
```

### Shutdown Graceful

#### Chiusura Parziale
```java
// Termina soltanto la direzione di invio, senza chiudere tutto il socket
socket.shutdownOutput();

// Termina soltanto la direzione di ricezione
socket.shutdownInput();

// Verifica stato
boolean inputShutdown = socket.isInputShutdown();
boolean outputShutdown = socket.isOutputShutdown();
```

#### Pattern Shutdown Protocollo
```java
// Client indica fine trasmissione
out.println("QUIT");
socket.shutdownOutput(); // Invia FIN

// Legge eventuali dati rimanenti dal server
while (in.readLine() != null) {
    // Consuma dati rimanenti
}

socket.close(); // Chiusura completa
```

### Gestione risorse con pool — approfondimento

Questo helper illustra il ciclo di prestito/restituzione; non è un pool completo
per produzione. Il servizio deve prevedere sessioni riutilizzabili e il chiamante
deve aver letto tutta la risposta prima di restituire la connessione. `isClosed()`
indica la chiusura locale: non verifica lo stato del peer. Una connessione che ha
dato un errore va chiusa, poi restituita per rimuoverla dal set dei prestiti.

```java
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class ConnectionPool implements AutoCloseable {
    private final Queue<Socket> availableConnections = new ArrayDeque<>();
    private final Set<Socket> usedConnections = new HashSet<>();
    private boolean closed;

    public synchronized Socket borrowConnection() throws IOException {
        if (closed) {
            throw new IOException("Pool chiuso");
        }
        Socket socket;
        do {
            socket = availableConnections.poll();
        } while (socket != null && socket.isClosed());
        if (socket == null) {
            socket = new Socket("localhost", 8080);
        }
        usedConnections.add(socket);
        return socket;
    }

    public synchronized void returnConnection(Socket socket) {
        if (socket == null || !usedConnections.remove(socket)) {
            return;
        }
        if (!closed && !socket.isClosed()) {
            availableConnections.offer(socket);
        } else {
            try { socket.close(); } catch (IOException error) { /* già in chiusura */ }
        }
    }

    @Override
    public synchronized void close() {
        closed = true;
        for (Socket socket : availableConnections) {
            try { socket.close(); } catch (IOException error) { /* prosegui il cleanup */ }
        }
        for (Socket socket : usedConnections) {
            try { socket.close(); } catch (IOException error) { /* prosegui il cleanup */ }
        }
        availableConnections.clear();
        usedConnections.clear();
    }
}
```

## 🔗 Esempi di Riferimento

- [Esempio a: chiusura di client e stream](esercizi/a-EchoService/TcpClient.java) - Pattern corretti per cleanup
- [Approfondimento: pool di thread del server](../04-Miltithread/esempi/ConnectionPoolServer.java) - Pool di connessioni riutilizzabili

## 💡 Best Practices

- **Try-with-resources**: Sempre quando possibile
- **Cleanup in finally**: Se non usi try-with-resources
- **Half-close**: Usa `shutdownOutput()` quando il protocollo usa EOF per terminare la richiesta e devi ancora leggere la risposta
- **Timeout**: Imposta un timeout sulle letture che attendono la risposta o la chiusura del peer; `SO_TIMEOUT` non limita `close()`
- **Pool per performance**: Riusa connessioni quando appropriato

⚠️ **Errori Comuni**:
- Credere che gli stream rimangano utilizzabili dopo `socket.close()`: la chiusura del socket chiude anche gli stream associati
- Ignorare eccezioni durante la chiusura
- Non verificare se socket è già chiuso
- Memory leak per connessioni non chiuse

## Navigazione del Corso
- [📑 Torna all'Indice del Corso](../README.md)
- [⬅️ Guida Precedente](02-Comunicazione-Dati-TCP.md)
- [➡️ Esercitazione Successiva](../04-Miltithread/04-Multithreading.md)
