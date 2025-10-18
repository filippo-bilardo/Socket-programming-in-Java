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
// Chiude solo l'output stream (invio)
socket.shutdownOutput();

// Chiude solo l'input stream (ricezione) 
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

### Gestione Risorse con Pool

```java
public class ConnectionPool {
    private final Queue<Socket> availableConnections = new ConcurrentLinkedQueue<>();
    private final Set<Socket> usedConnections = ConcurrentHashMap.newKeySet();
    
    public Socket borrowConnection() throws IOException {
        Socket socket = availableConnections.poll();
        if (socket == null || socket.isClosed()) {
            socket = new Socket("localhost", 8080);
        }
        usedConnections.add(socket);
        return socket;
    }
    
    public void returnConnection(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            usedConnections.remove(socket);
            availableConnections.offer(socket);
        }
    }
    
    public void close() {
        // Chiude tutte le connessioni
        Stream.concat(availableConnections.stream(), usedConnections.stream())
              .forEach(socket -> {
                  try { socket.close(); } catch (IOException e) { }
              });
    }
}
```

## 🔗 Esempi di Riferimento

- [Esempio 01: Gestione Risorse](./esempi/GestioneRisorse.java) - Pattern corretti per cleanup
- [Esempio 02: Connection Pool](./esempi/ConnectionPool.java) - Pool di connessioni riutilizzabili

## 💡 Best Practices

- **Try-with-resources**: Sempre quando possibile
- **Cleanup in finally**: Se non usi try-with-resources
- **Shutdown graceful**: Usa shutdownOutput() prima della chiusura
- **Timeout per close**: Evita attese infinite durante chiusura
- **Pool per performance**: Riusa connessioni quando appropriato

⚠️ **Errori Comuni**:
- Non chiudere stream prima del socket
- Ignorare eccezioni durante la chiusura
- Non verificare se socket è già chiuso
- Memory leak per connessioni non chiuse

## Navigazione del Corso
- [📑 Torna all'Indice del Corso](../README.md)
- [⬅️ Guida Precedente](02-Comunicazione-Dati-TCP.md)
- [➡️ Esercitazione Successiva](../04-Socket-UDP-Fondamenti/README.md)