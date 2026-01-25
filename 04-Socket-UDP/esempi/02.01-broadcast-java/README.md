# Esempio 02.01 - Broadcast UDP Sender (Java)

Questo esempio dimostra come inviare pacchetti UDP in modalità **broadcast** a tutti i dispositivi della rete locale.

## Concetti Chiave

- **Broadcast UDP**: Invio di dati a tutti i dispositivi della rete locale
- **Indirizzo 255.255.255.255**: Indirizzo broadcast limitato (limited broadcast)
- **setBroadcast(true)**: Metodo necessario per abilitare il broadcast sul socket

## Caratteristiche

✅ Invio periodico di messaggi broadcast  
✅ Configurazione porta e intervallo personalizzabili  
✅ Timestamp in ogni messaggio  
✅ Output formattato e leggibile  

## Compilazione

```bash
javac BroadcastSender.java
```

## Esecuzione

### Uso Base
```bash
java BroadcastSender
```
Usa valori di default (porta 5000, intervallo 2000ms)

### Con Parametri
```bash
java BroadcastSender <porta> <intervallo_ms>
```

### Esempi
```bash
# Porta 5000, intervallo 2 secondi
java BroadcastSender 5000 2000

# Porta 6000, intervallo 1 secondo
java BroadcastSender 6000 1000

# Porta 8888, intervallo 5 secondi
java BroadcastSender 8888 5000
```

## Output Atteso

```
═══════════════════════════════════════════════════
📡 BROADCAST UDP SENDER
═══════════════════════════════════════════════════
🔌 Porta: 5000
📡 Indirizzo Broadcast: 255.255.255.255
⏱️  Intervallo: 2000ms
═══════════════════════════════════════════════════

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

Esempio:
```
BROADCAST|Messaggio #1|Timestamp: 1705420800000
```

## Test con Receiver

Per testare questo sender, esegui il receiver nell'esempio 02.02:

**Terminale 1 (Receiver):**
```bash
cd ../es02.02-broadcast-receiver-java
javac BroadcastReceiver.java
java BroadcastReceiver 5000
```

**Terminale 2 (Sender):**
```bash
java BroadcastSender 5000 2000
```

## Considerazioni

### Limitazioni del Broadcast
- ⚠️ Il broadcast **non attraversa i router** - funziona solo sulla rete locale
- ⚠️ Tutti i dispositivi della rete ricevono il pacchetto
- ⚠️ Può causare congestione se usato eccessivamente

### Sicurezza
- Non inviare dati sensibili via broadcast
- Validare sempre i dati ricevuti da broadcast
- Implementare rate limiting per evitare flooding

### Performance
- L'intervallo di default (2 secondi) è adeguato per la maggior parte dei casi
- Intervalli troppo brevi possono sovraccaricare la rete
- Considera l'uso di multicast per gruppi specifici di destinatari

## Codice Rilevante

### Abilitazione Broadcast
```java
DatagramSocket socket = new DatagramSocket();
socket.setBroadcast(true); // NECESSARIO per il broadcast!
```

### Indirizzo Broadcast
```java
String BROADCAST_ADDRESS = "255.255.255.255";
InetAddress broadcastAddr = InetAddress.getByName(BROADCAST_ADDRESS);
```

### Invio Pacchetto
```java
byte[] buffer = message.getBytes();
DatagramPacket packet = new DatagramPacket(
    buffer,
    buffer.length,
    broadcastAddr,
    port
);
socket.send(packet);
```

## Risoluzione Problemi

### "Permission denied" su Linux
Su alcune configurazioni Linux potrebbe essere necessario:
```bash
sudo sysctl -w net.ipv4.ip_forward=1
```

### Firewall
Assicurati che il firewall non blocchi il traffico UDP:
```bash
# Linux (ufw)
sudo ufw allow 5000/udp

# Windows
Aggiungi regola in Windows Firewall per la porta UDP
```

# Esempio 02.02 - Broadcast UDP Receiver (Java)

Questo esempio dimostra come ricevere pacchetti UDP in modalità **broadcast** da tutti i sender della rete locale.

## Concetti Chiave

- **Ricezione Broadcast**: Ascolto di messaggi inviati all'indirizzo broadcast
- **DatagramSocket**: Socket UDP standard sufficiente per ricevere broadcast
- **Binding sulla porta**: Il socket deve essere in ascolto sulla porta corretta

## Caratteristiche

✅ Ricezione di tutti i messaggi broadcast sulla porta specificata  
✅ Visualizzazione informazioni dettagliate sul mittente  
✅ Parsing e validazione dei messaggi  
✅ Output formattato e leggibile  

## Compilazione

```bash
javac BroadcastReceiver.java
```

## Esecuzione

### Uso Base
```bash
java BroadcastReceiver
```
Usa porta di default 5000

### Con Porta Personalizzata
```bash
java BroadcastReceiver <porta>
```

### Esempi
```bash
# Ascolto sulla porta 5000
java BroadcastReceiver 5000

# Ascolto sulla porta 6000
java BroadcastReceiver 6000

# Ascolto sulla porta 8888
java BroadcastReceiver 8888
```

## Output Atteso

```
═══════════════════════════════════════════════════
📡 BROADCAST UDP RECEIVER
═══════════════════════════════════════════════════
🔌 Porta: 5000
═══════════════════════════════════════════════════

✅ Socket in ascolto sulla porta 5000
👂 In attesa di messaggi broadcast...

Premi Ctrl+C per terminare

╔═══════════════════════════════════════════════════
║ 📥 MESSAGGIO #1 RICEVUTO
╠═══════════════════════════════════════════════════
║ ⏰ Ora: 14:30:15
║ 👤 Mittente: 192.168.1.100:54321
║ 📦 Dimensione: 52 bytes
╠═══════════════════════════════════════════════════
║ 📝 Tipo: BROADCAST
║ 💬 Contenuto: Messaggio #1
║ 🕐 Timestamp: 1705420800000
╚═══════════════════════════════════════════════════
```

## Test Completo

Per testare questo receiver con il sender dell'esempio 02.01:

**Terminale 1 (Receiver):**
```bash
javac BroadcastReceiver.java
java BroadcastReceiver 5000
```

**Terminale 2 (Sender):**
```bash
cd ../es02.01-broadcast-sender-java
java BroadcastSender 5000 2000
```

## Test con Multipli Receiver

Il broadcast permette di avere più receiver contemporaneamente:

**Terminale 1:**
```bash
java BroadcastReceiver 5000
```

**Terminale 2:**
```bash
java BroadcastReceiver 5000
```

**Terminale 3 (Sender):**
```bash
cd ../es02.01-broadcast-sender-java
java BroadcastSender 5000 2000
```

Entrambi i receiver riceveranno gli stessi messaggi!

## Differenze con Multicast

| Aspetto | Broadcast | Multicast |
|---------|-----------|-----------|
| Destinatari | Tutti sulla rete | Solo membri del gruppo |
| Indirizzo | 255.255.255.255 | 224.0.0.0 - 239.255.255.255 |
| Socket | DatagramSocket | MulticastSocket |
| Join | Non necessario | Necessario joinGroup() |
| Instradabile | No (solo LAN) | Sì (se configurato) |

## Codice Rilevante

### Creazione Socket
```java
// Per broadcast è sufficiente un DatagramSocket normale
DatagramSocket socket = new DatagramSocket(port);
// Non serve setBroadcast(true) sul receiver
```

### Ricezione Pacchetto
```java
byte[] buffer = new byte[BUFFER_SIZE];
DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

// Riceve (bloccante fino all'arrivo di un pacchetto)
socket.receive(packet);

// Estrae i dati
String message = new String(packet.getData(), 0, packet.getLength());
InetAddress sender = packet.getAddress();
int senderPort = packet.getPort();
```

## Sicurezza

### Validazione Mittente
È importante validare l'origine dei messaggi broadcast:

```java
private boolean isValidSender(InetAddress sender) {
    // Verifica che sia nella tua subnet
    String senderIP = sender.getHostAddress();
    
    // Esempio: accetta solo dalla subnet 192.168.1.0/24
    if (!senderIP.startsWith("192.168.1.")) {
        System.err.println("⚠️ Pacchetto da subnet non autorizzata: " + senderIP);
        return false;
    }
    
    return true;
}
```

### Rate Limiting
Previeni flooding da mittenti malevoli:

```java
Map<InetAddress, Long> lastMessageTime = new HashMap<>();

private boolean checkRateLimit(InetAddress sender) {
    long now = System.currentTimeMillis();
    Long lastTime = lastMessageTime.get(sender);
    
    if (lastTime != null && (now - lastTime) < 100) { // Min 100ms tra messaggi
        return false; // Rate limit superato
    }
    
    lastMessageTime.put(sender, now);
    return true;
}
```

## Risoluzione Problemi

### Porta già in uso
```
❌ Errore creazione socket: Address already in use
```

**Soluzione**: Chiudi altre applicazioni sulla stessa porta o usa una porta diversa:
```bash
java BroadcastReceiver 5001
```

### Non riceve messaggi
1. Verifica che sender e receiver siano sulla stessa rete
2. Controlla che usino la stessa porta
3. Verifica il firewall (potrebbe bloccare UDP)
4. Su Windows: controlla Windows Defender Firewall

### Firewall
```bash
# Linux (ufw)
sudo ufw allow 5000/udp

# Verifica se la porta è in ascolto
sudo netstat -ulnp | grep 5000
```

## Esempi Correlati

- [es02.01-broadcast-sender-java](../es02.01-broadcast-sender-java/README.md) - Sender broadcast
- [es02.03-multicast-sender-java](../es02.03-multicast-sender-java/README.md) - Sender multicast
- [es02.04-multicast-receiver-java](../es02.04-multicast-receiver-java/README.md) - Receiver multicast
