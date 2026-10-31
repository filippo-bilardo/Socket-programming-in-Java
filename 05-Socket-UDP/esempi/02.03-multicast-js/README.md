# Esempio 02.07 - Multicast UDP Sender (JavaScript/Node.js)

Questo esempio dimostra come inviare pacchetti UDP in modalità **multicast** usando Node.js.

## Requisiti

- Node.js 12.x o superiore
- Nessuna dipendenza esterna (usa modulo built-in `dgram`)

## Esecuzione

### Uso Base
```bash
node multicast-sender.js
```
Usa valori di default (239.255.0.1, porta 5000, intervallo 2000ms, TTL 1)

### Con NPM Scripts
```bash
npm start
# oppure
npm run dev
```

### Con Parametri
```bash
node multicast-sender.js <indirizzo> <porta> <intervallo_ms> <ttl>
```

### Esempi

```bash
# Configurazione di default
node multicast-sender.js

# Indirizzo multicast personalizzato
node multicast-sender.js 239.1.1.1 5000 2000 1

# Porta diversa
node multicast-sender.js 239.255.0.1 6000 2000 1

# Intervallo 1 secondo
node multicast-sender.js 239.255.0.1 5000 1000 1

# TTL più alto
node multicast-sender.js 239.255.0.1 5000 2000 32
```

## Output Atteso

```
═══════════════════════════════════════════════════════
📡 MULTICAST UDP SENDER (Node.js)
═══════════════════════════════════════════════════════
🔌 Porta: 5000
📡 Indirizzo Multicast: 239.255.0.1
⏱️  Intervallo: 2000ms
🌐 TTL: 1 (Rete locale)
═══════════════════════════════════════════════════════

✅ Socket creato e configurato per multicast
🚀 Inizio invio messaggi multicast...

Premi Ctrl+C per terminare

📤 [14:30:15] Inviato messaggio #1 a 239.255.0.1:5000 (62 bytes)
📤 [14:30:17] Inviato messaggio #2 a 239.255.0.1:5000 (62 bytes)
📤 [14:30:19] Inviato messaggio #3 a 239.255.0.1:5000 (62 bytes)
```

## Formato Messaggio

```
MULTICAST|Messaggio #<numero>|Timestamp: <timestamp_ms>|TTL: <ttl>
```

## Test con Receiver

**Terminale 1 (Receiver):**
```bash
cd ../es02.08-multicast-receiver-js
node multicast-receiver.js 239.255.0.1 5000
```

**Terminale 2 (Sender):**
```bash
node multicast-sender.js 239.255.0.1 5000 2000 1
```

## Codice Rilevante

### Creazione Socket
```javascript
const dgram = require('dgram');
const socket = dgram.createSocket({ type: 'udp4', reuseAddr: true });
```

### Configurazione Multicast
```javascript
socket.bind(() => {
    // Imposta TTL
    socket.setMulticastTTL(1);
    
    // Join al gruppo (opzionale per sender)
    socket.addMembership('239.255.0.1');
});
```

### Invio Messaggio
```javascript
const message = "Hello Multicast!";
const buffer = Buffer.from(message);

socket.send(
    buffer,
    0,
    buffer.length,
    port,
    multicastAddress,
    (err) => {
        if (err) console.error('Errore:', err);
    }
);
```

## Indirizzi Multicast Comuni

- `224.0.0.1`: Tutti i sistemi sulla subnet
- `224.0.0.251`: mDNS
- `239.255.0.1`: Uso privato (raccomandato per test)

## TTL Values

| TTL | Scope |
|-----|-------|
| 0 | Solo questo host |
| 1 | Rete locale |
| 32 | Stessa organizzazione |
| 64 | Stessa regione |
| 128 | Stesso continente |
| 255 | Globale |

## Esempi Correlati

- [es02.03-multicast-sender-java](../es02.03-multicast-sender-java/README.md) - Versione Java
- [es02.08-multicast-receiver-js](../es02.08-multicast-receiver-js/README.md) - Receiver JavaScript
