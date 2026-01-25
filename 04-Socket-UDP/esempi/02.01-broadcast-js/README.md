# Esempio 02.05 - Broadcast UDP Sender (JavaScript/Node.js)

Questo esempio dimostra come inviare pacchetti UDP in modalità **broadcast** usando Node.js.

## Requisiti

- Node.js 12.x o superiore
- Nessuna dipendenza esterna (usa modulo built-in `dgram`)

## Installazione

```bash
npm install  # Non necessario, usa solo moduli built-in
```

## Esecuzione

### Uso Base
```bash
node broadcast-sender.js
```
Usa valori di default (porta 5000, intervallo 2000ms)

### Con NPM Scripts
```bash
npm start
# oppure
npm run dev
```

### Con Parametri
```bash
node broadcast-sender.js <porta> <intervallo_ms>
```

### Esempi

```bash
# Porta 5000, intervallo 2 secondi
node broadcast-sender.js 5000 2000

# Porta 6000, intervallo 1 secondo
node broadcast-sender.js 6000 1000

# Porta 8888, intervallo 5 secondi
node broadcast-sender.js 8888 5000
```

## Output Atteso

```
═══════════════════════════════════════════════════════
📡 BROADCAST UDP SENDER (Node.js)
═══════════════════════════════════════════════════════
🔌 Porta: 5000
📡 Indirizzo Broadcast: 255.255.255.255
⏱️  Intervallo: 2000ms
═══════════════════════════════════════════════════════

✅ Socket creato e configurato per broadcast
🚀 Inizio invio messaggi broadcast...

Premi Ctrl+C per terminare

📤 [14:30:15] Inviato messaggio #1 (52 bytes)
📤 [14:30:17] Inviato messaggio #2 (52 bytes)
📤 [14:30:19] Inviato messaggio #3 (52 bytes)
```

## Formato Messaggio

```
BROADCAST|Messaggio #<numero>|Timestamp: <timestamp_ms>
```

## Test con Receiver

**Terminale 1 (Receiver):**
```bash
cd ../es02.06-broadcast-receiver-js
node broadcast-receiver.js 5000
```

**Terminale 2 (Sender):**
```bash
node broadcast-sender.js 5000 2000
```

## Codice Rilevante

### Creazione Socket
```javascript
const dgram = require('dgram');
const socket = dgram.createSocket('udp4');
```

### Abilitazione Broadcast
```javascript
socket.bind(() => {
    // NECESSARIO per il broadcast!
    socket.setBroadcast(true);
});
```

### Invio Messaggio
```javascript
const message = "Hello Broadcast!";
const buffer = Buffer.from(message);

socket.send(
    buffer,
    0,
    buffer.length,
    port,
    '255.255.255.255',
    (err) => {
        if (err) console.error('Errore:', err);
    }
);
```

## Differenze con Java

| Aspetto | Java | JavaScript (Node.js) |
|---------|------|---------------------|
| Socket | DatagramSocket | dgram.createSocket |
| Abilita Broadcast | setBroadcast(true) | setBroadcast(true) |
| Invio | socket.send(packet) | socket.send(buffer, ..., callback) |
| Binding | Opzionale | Richiesto per setBroadcast |
| Chiusura | socket.close() | socket.close() |


---

# Broadcast UDP Receiver (JavaScript/Node.js)

Questo esempio dimostra come ricevere pacchetti UDP in modalità **broadcast** usando Node.js.

## Requisiti

- Node.js 12.x o superiore
- Nessuna dipendenza esterna (usa modulo built-in `dgram`)

## Esecuzione

### Uso Base
```bash
node broadcast-receiver.js
```
Usa porta di default 5000

### Con NPM Scripts
```bash
npm start
# oppure
npm run dev
```

### Con Porta Personalizzata
```bash
node broadcast-receiver.js <porta>
```

### Esempi

```bash
# Ascolto sulla porta 5000
node broadcast-receiver.js 5000

# Ascolto sulla porta 6000
node broadcast-receiver.js 6000
```

## Output Atteso

```
═══════════════════════════════════════════════════════
📡 BROADCAST UDP RECEIVER (Node.js)
═══════════════════════════════════════════════════════
🔌 Porta: 5000
═══════════════════════════════════════════════════════

✅ Socket in ascolto su 0.0.0.0:5000
👂 In attesa di messaggi broadcast...

Premi Ctrl+C per terminare

╔═════════════════════════════════════════════════════
║ 📥 MESSAGGIO #1 RICEVUTO
╠═════════════════════════════════════════════════════
║ ⏰ Ora: 14:30:15
║ 👤 Mittente: 192.168.1.100:54321
║ 📦 Dimensione: 52 bytes
╠═════════════════════════════════════════════════════
║ 📝 Tipo: BROADCAST
║ 💬 Contenuto: Messaggio #1
║ 🕐 Timestamp: 1705420800000
╚═════════════════════════════════════════════════════
```

## Test Completo

**Terminale 1 (Receiver):**
```bash
node broadcast-receiver.js 5000
```

**Terminale 2 (Sender):**
```bash
cd ../es02.05-broadcast-sender-js
node broadcast-sender.js 5000 2000
```

## Multipli Receiver

**Terminale 1:**
```bash
node broadcast-receiver.js 5000
```

**Terminale 2:**
```bash
node broadcast-receiver.js 5000
```

**Terminale 3 (Sender):**
```bash
cd ../es02.05-broadcast-sender-js
node broadcast-sender.js 5000 2000
```

Entrambi i receiver riceveranno i messaggi!

## Codice Rilevante

### Creazione Socket
```javascript
const dgram = require('dgram');
const socket = dgram.createSocket('udp4');
```

### Eventi Socket
```javascript
socket.on('listening', () => {
    const address = socket.address();
    console.log(`Socket in ascolto su ${address.address}:${address.port}`);
});

socket.on('message', (msg, remote) => {
    console.log(`Ricevuto: ${msg}`);
    console.log(`Da: ${remote.address}:${remote.port}`);
});

socket.on('error', (err) => {
    console.error(`Errore: ${err.message}`);
});
```

### Binding
```javascript
// Per broadcast NON serve setBroadcast(true) sul receiver
socket.bind(port);
```

## Risoluzione Problemi

### Porta già in uso
```
❌ Errore socket: bind EADDRINUSE
   Verifica che la porta non sia già in uso
```

**Soluzione:**
```bash
# Usa porta diversa
node broadcast-receiver.js 5001

# Oppure trova processo che usa la porta
# Linux/Mac
lsof -i :5000
kill -9 <PID>

# Windows
netstat -ano | findstr :5000
taskkill /PID <PID> /F
```

### Non riceve messaggi

1. Verifica stesso numero di porta sender/receiver
2. Controlla firewall
3. Verifica stessa rete locale

