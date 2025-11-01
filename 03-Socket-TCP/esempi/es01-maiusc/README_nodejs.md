# Server TCP in Node.js - Conversione in Maiuscolo

Questo esempio mostra l'implementazione equivalente in Node.js del server TCP Java che converte messaggi in maiuscolo.

## File

- **tcpServer.js** - Server TCP che riceve messaggi e risponde in maiuscolo
- **tcpClient.js** - Client TCP che invia messaggi al server

## Prerequisiti

- Node.js installato (versione 12 o superiore)

## Esecuzione

### 1. Avviare il server

In un terminale:

```bash
node tcpServer.js
```

Output atteso:
```
Server in ascolto sulla porta 8765...
```

### 2. Avviare il client

In un altro terminale:

```bash
node tcpClient.js
```

Output client:
```
Connesso al server localhost:8765
Invio messaggio: Ciao dal client Node.js!
Risposta ricevuta: Risposta dal server: CIAO DAL CLIENT NODE.JS!
Connessione chiusa
```

Output server:
```
Connessione stabilita con il client!
Client: ::1:xxxxx
Messaggio ricevuto: Ciao dal client Node.js!
Client disconnesso
```

## Test con telnet o netcat

Puoi anche testare il server usando telnet o netcat:

```bash
# Con telnet
telnet localhost 8765
> hello world
Risposta dal server: HELLO WORLD

# Con netcat
echo "hello world" | nc localhost 8765
```

## Differenze Java vs Node.js

### Java (TcpServer.java)
```java
ServerSocket serverSocket = new ServerSocket(8765);
Socket clientSocket = serverSocket.accept();
BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
String message = in.readLine();
out.println("Risposta: " + message.toUpperCase());
```

### Node.js (tcpServer.js)
```javascript
const server = net.createServer((socket) => {
    socket.on('data', (data) => {
        const message = data.toString().trim();
        socket.write('Risposta: ' + message.toUpperCase() + '\n');
        socket.end();
    });
});
server.listen(8765);
```

## Caratteristiche

### Versione Java
- ✅ Usa `try-with-resources` per gestione automatica risorse
- ✅ Gestisce una connessione alla volta (single-threaded)
- ✅ Chiusura automatica delle risorse

### Versione Node.js
- ✅ Event-driven (listeners per eventi)
- ✅ Asincrono e non-bloccante
- ✅ Può gestire multiple connessioni concorrenti
- ✅ Gestione eventi: `data`, `end`, `error`, `close`

## Estensioni

### Server Multi-Client (Node.js)

Il server Node.js può facilmente gestire più client contemporaneamente:

```javascript
const net = require('net');
const clients = new Set();

const server = net.createServer((socket) => {
    clients.add(socket);
    console.log(`Connessioni attive: ${clients.size}`);
    
    socket.on('data', (data) => {
        const message = data.toString().trim();
        socket.write('Risposta: ' + message.toUpperCase() + '\n');
    });
    
    socket.on('end', () => {
        clients.delete(socket);
        console.log(`Connessioni attive: ${clients.size}`);
    });
});

server.listen(8765);
```

### Versione con readline (input interattivo)

```javascript
// tcpClientInteractive.js
const net = require('net');
const readline = require('readline');

const client = new net.Socket();
const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

client.connect(8765, 'localhost', () => {
    console.log('Connesso! Scrivi un messaggio:');
    
    rl.on('line', (input) => {
        if (input.toLowerCase() === 'quit') {
            client.end();
            rl.close();
            return;
        }
        client.write(input + '\n');
    });
});

client.on('data', (data) => {
    console.log(data.toString().trim());
    rl.prompt();
});

client.on('close', () => {
    console.log('Disconnesso');
    process.exit(0);
});
```

## Confronto Performance

| Caratteristica | Java | Node.js |
|---------------|------|---------|
| Modello | Thread-based | Event-loop |
| Concorrenza | Thread per client | Single-thread event-driven |
| I/O | Blocking | Non-blocking |
| Memoria | Maggiore (thread stack) | Minore (event-loop) |
| Scalabilità | Limitata da thread | Alta (migliaia di connessioni) |

## Note

- La versione Node.js è naturalmente **asincrona** e **non-bloccante**
- Java richiede **multi-threading** per gestire più client (non mostrato nell'esempio base)
- Node.js gestisce facilmente **migliaia di connessioni** concorrenti grazie all'event-loop
- Entrambe le versioni usano la porta **8765** per compatibilità

## Prossimi Passi

1. Implementare un server multi-threaded in Java
2. Aggiungere persistenza dei messaggi
3. Implementare un protocollo di comunicazione più complesso
4. Aggiungere autenticazione client
5. Implementare broadcast a tutti i client connessi
