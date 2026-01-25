# Echo UDP - Server e Client

Questo esempio dimostra la comunicazione UDP bidirezionale con un server echo che rimanda indietro i messaggi ricevuti.

## Contenuto

- **EchoUDP.java** - Implementazione Java con server e client
- **echo-udp.js** - Implementazione JavaScript/Node.js con server e client
- **package.json** - Configurazione NPM per la versione JavaScript

## Caratteristiche

✅ Server UDP che fa echo dei messaggi ricevuti  
✅ Client UDP con modalità interattiva  
✅ Gestione timeout e pacchetti persi  
✅ Aggiunta timestamp alle risposte  
✅ Contatore messaggi  

## Versione Java

### Compilazione
```bash
javac EchoUDP.java
```

### Esecuzione Server
```bash
java EchoUDP server 9999
```

### Esecuzione Client
```bash
java EchoUDP client localhost 9999
```

### Output Server
```
🚀 Server UDP Echo avviato su porta 9999
📦 Dimensione buffer: 1024 byte
🛑 Premi Ctrl+C per fermare
==================================================
📨 Ricevuto da /127.0.0.1:54321 → Test di connettività
📤 Risposta inviata: ECHO: Test di connettività [1737820800000]
```

### Output Client
```
🔗 Client UDP Echo
Target: localhost:9999
==============================
📤 Invio: Test di connettività
📨 Risposta: ECHO: Test di connettività [1737820800000]
✅ Connessione OK

💬 Modalità interattiva avviata
💡 Digita 'quit' per uscire
------------------------------
Tu: Ciao server!
📤 Invio: [#1] Ciao server!
📨 Risposta: ECHO: [#1] Ciao server! [1737820801234]

Tu: Come va?
📤 Invio: [#2] Come va?
📨 Risposta: ECHO: [#2] Come va? [1737820802345]

Tu: quit
👋 Disconnessione...
```

## Versione JavaScript/Node.js

### Requisiti
- Node.js 12.x o superiore

### Installazione
```bash
npm install  # Non necessario, usa solo moduli built-in
```

### Esecuzione Server
```bash
node echo-udp.js server 9999
# Oppure con NPM script
npm run server
```

### Esecuzione Client
```bash
node echo-udp.js client localhost 9999
# Oppure con NPM script
npm run client
```

### Output Identico alla Versione Java
Il comportamento è identico alla versione Java.

## Test Completo

Per testare l'esempio, apri due terminali:

**Terminale 1 (Server):**
```bash
# Java
javac EchoUDP.java
java EchoUDP server 9999

# Oppure JavaScript
node echo-udp.js server 9999
```

**Terminale 2 (Client):**
```bash
# Java
java EchoUDP client localhost 9999

# Oppure JavaScript
node echo-udp.js client localhost 9999
```

## Comandi Client Interattivo

Una volta avviato il client:

- Digita qualsiasi messaggio e premi INVIO per inviarlo
- Riceverai l'echo dal server
- Digita `quit` per uscire

## Gestione Timeout

Il client attende la risposta per 5 secondi. Se il server non risponde:

```
Tu: test
📤 Invio: [#1] test
⏰ Timeout - nessuna risposta in 5000ms
⚠️ Messaggio perso (timeout)
```

## Formato Risposta

Il server aggiunge un prefisso "ECHO:" e un timestamp:

```
Messaggio inviato: Ciao
Risposta ricevuta: ECHO: Ciao [1737820800000]
```

## Differenze Java vs JavaScript

| Aspetto | Java | JavaScript |
|---------|------|------------|
| **Socket** | DatagramSocket | dgram.createSocket |
| **Timeout** | socket.setSoTimeout() | setTimeout() + Promise |
| **Invio** | socket.send(packet) | socket.send(buffer, ..., callback) |
| **Ricezione** | socket.receive(packet) | socket.on('message', ...) |
| **Chiusura** | socket.close() | socket.close() |
| **Input utente** | BufferedReader | readline.createInterface |

## Codice Rilevante

### Java - Server Echo
```java
// Riceve pacchetto
DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
socket.receive(packet);

// Prepara risposta
String response = "ECHO: " + received + " [" + System.currentTimeMillis() + "]";
byte[] responseData = response.getBytes();

// Invia risposta
DatagramPacket responsePacket = new DatagramPacket(
    responseData, responseData.length, clientAddress, clientPort);
socket.send(responsePacket);
```

### JavaScript - Server Echo
```javascript
socket.on('message', (msg, remote) => {
    const received = msg.toString();
    const response = `ECHO: ${received} [${Date.now()}]`;
    const responseBuffer = Buffer.from(response);
    
    socket.send(responseBuffer, 0, responseBuffer.length, 
                remote.port, remote.address);
});
```

### Java - Client con Timeout
```java
socket = new DatagramSocket();
socket.setSoTimeout(timeoutMs);

try {
    socket.send(packet);
    socket.receive(responsePacket);
} catch (SocketTimeoutException e) {
    System.err.println("⏰ Timeout");
}
```

### JavaScript - Client con Timeout
```javascript
const timeoutPromise = new Promise((_, reject) => {
    setTimeout(() => reject(new Error('Timeout')), timeoutMs);
});

const messagePromise = new Promise((resolve) => {
    socket.once('message', (msg) => resolve(msg.toString()));
    socket.send(buffer, ...);
});

const response = await Promise.race([messagePromise, timeoutPromise]);
```

## Simulazione Perdita Pacchetti

Per testare la gestione timeout, ferma il server mentre il client è in modalità interattiva:

1. Avvia server e client
2. Ferma il server (Ctrl+C)
3. Invia un messaggio dal client
4. Vedrai il timeout dopo 5 secondi

## Possibili Miglioramenti

- Aggiungere crittografia ai messaggi
- Implementare retry automatico
- Aggiungere statistiche (pacchetti persi, latenza)
- Supportare invio di file
- Aggiungere logging su file

## Riferimenti

- Guida: [01-Socket-UDP-Base.md](../../01-Socket-UDP-Fondamenti.md)
- RFC 768: User Datagram Protocol
