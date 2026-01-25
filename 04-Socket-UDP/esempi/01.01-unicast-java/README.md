# Esempi UDP Unicast - Java e JavaScript

Esempi base di comunicazione UDP unicast (punto-a-punto) in Java e JavaScript/Node.js.

## Contenuto

### Versione Java
- **UdpSender.java** - Mittente UDP che invia un messaggio
- **UdpReceiver.java** - Ricevitore UDP che riceve un messaggio

### Versione JavaScript/Node.js
- **udp-sender.js** - Mittente UDP che invia un messaggio
- **udp-receiver.js** - Ricevitore UDP che riceve un messaggio
- **package.json** - Configurazione NPM

## Versione Java

### Compilazione
```bash
javac UdpSender.java
javac UdpReceiver.java
```

### Esecuzione

**Terminale 1 - Receiver:**
```bash
java UdpReceiver
```

Output:
```
In attesa di messaggi...
```

**Terminale 2 - Sender:**
```bash
java UdpSender
```

Output:
```
Messaggio inviato!
```

**Output Receiver dopo ricezione:**
```
In attesa di messaggi...
Messaggio ricevuto: Hello, UDP!
```

### Compilazione ed Esecuzione Rapida
```bash
# Receiver
javac UdpReceiver.java && java UdpReceiver

# Sender (in altro terminale)
javac UdpSender.java && java UdpSender
```

## Versione JavaScript/Node.js

### Requisiti
- Node.js 12.x o superiore

### Esecuzione

**Terminale 1 - Receiver:**
```bash
node udp-receiver.js
# Oppure con NPM
npm run receiver
```

Output:
```
In attesa di messaggi su 0.0.0.0:9876...
```

**Terminale 2 - Sender:**
```bash
node udp-sender.js
# Oppure con NPM
npm run sender
```

Output:
```
Messaggio inviato!
```

**Output Receiver dopo ricezione:**
```
In attesa di messaggi su 0.0.0.0:9876...
Messaggio ricevuto: Hello, UDP!
Da: 127.0.0.1:xxxxx
```

## Caratteristiche

✅ Comunicazione UDP unicast semplice  
✅ Invio di un singolo messaggio  
✅ Chiusura automatica dopo ricezione  
✅ Gestione errori base  

## Differenze tra le Versioni

| Aspetto | Java | JavaScript |
|---------|------|------------|
| **Moduli** | java.net.* | dgram (built-in) |
| **Socket** | DatagramSocket | dgram.createSocket |
| **Invio** | socket.send(packet) | socket.send(buffer, ..., callback) |
| **Ricezione** | socket.receive(packet) | socket.on('message', ...) |
| **Binding** | Automatico | socket.bind(port) |
| **Eventi** | Bloccante | Event-driven |

## Concetti Chiave

### UDP (User Datagram Protocol)
- Protocollo connectionless (senza connessione)
- Non garantisce la consegna
- Non garantisce l'ordine
- Più veloce di TCP
- Overhead minimo

### Unicast
- Comunicazione uno-a-uno
- Mittente → Destinatario specifico
- Usa indirizzo IP e porta del destinatario

### Porta 9876
- Porta UDP usata in questi esempi
- Può essere cambiata modificando il codice
- Porte < 1024 richiedono privilegi amministrativi

## Codice Rilevante

### Java - Invio
```java
DatagramSocket socket = new DatagramSocket();
String message = "Hello, UDP!";
byte[] buffer = message.getBytes();
InetAddress address = InetAddress.getByName("localhost");

DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 9876);
socket.send(packet);
socket.close();
```

### Java - Ricezione
```java
DatagramSocket socket = new DatagramSocket(9876);
byte[] buffer = new byte[1024];
DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

socket.receive(packet);
String message = new String(packet.getData(), 0, packet.getLength());
socket.close();
```

### JavaScript - Invio
```javascript
const socket = dgram.createSocket('udp4');
const message = 'Hello, UDP!';
const buffer = Buffer.from(message);

socket.send(buffer, 0, buffer.length, 9876, 'localhost', (err) => {
    console.log('Messaggio inviato!');
    socket.close();
});
```

### JavaScript - Ricezione
```javascript
const socket = dgram.createSocket('udp4');

socket.on('message', (msg, remote) => {
    const message = msg.toString();
    console.log(`Messaggio ricevuto: ${message}`);
    socket.close();
});

socket.bind(9876);
```

## Personalizzazione

### Cambiare il Messaggio

**Java - UdpSender.java:**
```java
String message = "Il tuo messaggio personalizzato";
```

**JavaScript - udp-sender.js:**
```javascript
const message = 'Il tuo messaggio personalizzato';
```

### Cambiare la Porta

**Java:**
```java
// Sender
DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 8888);

// Receiver
DatagramSocket socket = new DatagramSocket(8888);
```

**JavaScript:**
```javascript
// Sender
const port = 8888;

// Receiver
socket.bind(8888);
```

### Cambiare l'Host Destinatario

**Java:**
```java
InetAddress address = InetAddress.getByName("192.168.1.100");
```

**JavaScript:**
```javascript
const host = '192.168.1.100';
```

## Risoluzione Problemi

### Porta già in uso
```
Error: bind EADDRINUSE (JavaScript)
BindException: Address already in use (Java)
```

**Soluzione:** Cambia porta o chiudi il processo che la usa.

### Connection refused
Se il receiver non è in esecuzione, il sender non riceverà errori (caratteristica di UDP).

### Firewall
Assicurati che il firewall permetta il traffico UDP sulla porta 9876:

```bash
# Linux (ufw)
sudo ufw allow 9876/udp

# Verifica porta in ascolto
sudo netstat -ulnp | grep 9876
```

## Test su Reti Diverse

Per testare tra computer diversi:

1. Trova l'IP del computer receiver:
   ```bash
   # Linux/Mac
   ifconfig
   
   # Windows
   ipconfig
   ```

2. Modifica il sender per usare l'IP trovato:
   ```java
   // Java
   InetAddress.getByName("192.168.1.100")
   ```
   ```javascript
   // JavaScript
   const host = '192.168.1.100';
   ```

3. Assicurati che il firewall permetta la connessione

## Limitazioni

⚠️ Questi esempi sono minimalisti:
- Inviano/ricevono un solo messaggio
- Non gestiscono pacchetti persi
- Non hanno timeout
- Non validano i dati ricevuti

Per esempi più completi, vedi:
- [01.02-EchoUdp](../01.02-EchoUdp/README.md) - Echo server con timeout
- [es02.01-broadcast-sender-java](../es02.01-broadcast-sender-java/README.md) - Broadcast UDP

## Riferimenti

- RFC 768: User Datagram Protocol
- Guida: [01-Socket-UDP-Fondamenti.md](../../01-Socket-UDP-Fondamenti.md)
