# Esempio 02.03 - Multicast UDP Sender (Java)

Questo esempio dimostra come inviare pacchetti UDP in modalità **multicast** a un gruppo specifico di destinatari.

## Concetti Chiave

- **Multicast UDP**: Invio di dati a un gruppo selezionato di destinatari
- **Indirizzi Multicast**: Range 224.0.0.0 - 239.255.255.255
- **TTL (Time To Live)**: Controlla quanti router può attraversare un pacchetto
- **MulticastSocket**: Socket specializzato per operazioni multicast

## Caratteristiche

✅ Invio periodico di messaggi multicast  
✅ Configurazione indirizzo multicast, porta, intervallo e TTL  
✅ Validazione automatica dell'indirizzo multicast  
✅ Descrizione automatica del TTL  

## Indirizzi Multicast

### Range Principali
- **224.0.0.0/24**: Riservato per protocolli di routing (OSPF, RIP, ecc.)
- **224.0.1.0/24**: Assegnabili dall'IANA per applicazioni specifiche
- **239.0.0.0/8**: Uso privato/organizzazioni (raccomandato per test)

### Esempi Comuni
- `224.0.0.1`: Tutti i sistemi sulla subnet locale
- `224.0.0.2`: Tutti i router sulla subnet locale
- `224.0.0.251`: mDNS (Multicast DNS)
- `239.255.0.1`: Uso privato (ottimo per test)

## TTL (Time To Live)

| TTL | Scope | Descrizione |
|-----|-------|-------------|
| 0 | Host | Solo questo computer |
| 1 | Subnet | Rete locale (non attraversa router) |
| 2-32 | Sito | Stessa organizzazione |
| 33-64 | Regione | Stessa area geografica |
| 65-128 | Continente | Stesso continente |
| 129-255 | Globale | Illimitato |

**Raccomandazione**: Usa TTL=1 per test locali

## Compilazione

```bash
javac MulticastSender.java
```

## Esecuzione

### Uso Base
```bash
java MulticastSender
```
Usa valori di default (239.255.0.1, porta 5000, intervallo 2000ms, TTL 1)

### Con Parametri
```bash
java MulticastSender <indirizzo_multicast> <porta> <intervallo_ms> <ttl>
```

### Esempi

```bash
# Configurazione di default
java MulticastSender

# Indirizzo multicast personalizzato
java MulticastSender 239.1.1.1 5000 2000 1

# Porta diversa
java MulticastSender 239.255.0.1 6000 2000 1

# Intervallo 1 secondo
java MulticastSender 239.255.0.1 5000 1000 1

# TTL più alto (attraversa router)
java MulticastSender 239.255.0.1 5000 2000 32

# Uso di indirizzo mDNS
java MulticastSender 224.0.0.251 5353 2000 1
```

## Output Atteso

```
═══════════════════════════════════════════════════
📡 MULTICAST UDP SENDER
═══════════════════════════════════════════════════
🔌 Porta: 5000
📡 Indirizzo Multicast: 239.255.0.1
⏱️  Intervallo: 2000ms
🌐 TTL: 1 (Rete locale)
═══════════════════════════════════════════════════

✅ MulticastSocket creato
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

Esempio:
```
MULTICAST|Messaggio #1|Timestamp: 1705420800000|TTL: 1
```

## Test con Receiver

Per testare questo sender, esegui il receiver nell'esempio 02.04:

**Terminale 1 (Receiver):**
```bash
cd ../es02.04-multicast-receiver-java
javac MulticastReceiver.java
java MulticastReceiver 239.255.0.1 5000
```

**Terminale 2 (Sender):**
```bash
java MulticastSender 239.255.0.1 5000 2000 1
```

## Multipli Receiver

Il multicast permette multipli receiver sul gruppo:

**Terminale 1:**
```bash
cd ../es02.04-multicast-receiver-java
java MulticastReceiver 239.255.0.1 5000
```

**Terminale 2:**
```bash
cd ../es02.04-multicast-receiver-java
java MulticastReceiver 239.255.0.1 5000
```

**Terminale 3 (Sender):**
```bash
java MulticastSender 239.255.0.1 5000 2000 1
```

Tutti i receiver riceveranno i messaggi!

## Differenze Broadcast vs Multicast

| Aspetto | Broadcast | Multicast |
|---------|-----------|-----------|
| **Destinatari** | Tutti sulla rete | Solo membri del gruppo |
| **Indirizzo** | 255.255.255.255 | 224.0.0.0 - 239.255.255.255 |
| **Socket** | DatagramSocket | MulticastSocket |
| **Join** | Non richiesto | Richiesto joinGroup() |
| **TTL** | Non applicabile | Configurabile |
| **Router** | Non attraversa | Può attraversare (se TTL > 1) |
| **Efficienza** | Bassa | Alta |

## Codice Rilevante

### Creazione MulticastSocket
```java
MulticastSocket socket = new MulticastSocket();
```

### Configurazione TTL
```java
socket.setTimeToLive(1); // Solo rete locale
```

### Invio Pacchetto Multicast
```java
InetAddress group = InetAddress.getByName("239.255.0.1");
byte[] buffer = message.getBytes();

DatagramPacket packet = new DatagramPacket(
    buffer,
    buffer.length,
    group,
    port
);

socket.send(packet);
```

## Configurazione di Rete

### Linux
Verifica che il multicast sia abilitato:
```bash
# Verifica interfaccia di rete
ip link show

# Verifica routing multicast
ip mroute show

# Abilita multicast su interfaccia
sudo ip link set eth0 multicast on
```

### Firewall
```bash
# Linux (ufw)
sudo ufw allow 5000/udp

# Iptables - permetti multicast
sudo iptables -A INPUT -p udp -d 224.0.0.0/4 -j ACCEPT
```

## Risoluzione Problemi

### "Network is unreachable"
Il multicast potrebbe non essere supportato/abilitato sulla tua interfaccia di rete.

### Non attraversa router
Con TTL=1, i pacchetti non attraversano router. Aumenta il TTL se necessario:
```bash
java MulticastSender 239.255.0.1 5000 2000 32
```

### Indirizzo multicast non valido
```
❌ Indirizzo multicast non valido: 192.168.1.100
   Deve essere nel range 224.0.0.0 - 239.255.255.255
```

Usa un indirizzo valido nel range multicast.

---

# Esempio 02.04 - Multicast UDP Receiver (Java)

Questo esempio dimostra come ricevere pacchetti UDP in modalità **multicast** da un gruppo specifico.

## Concetti Chiave

- **Join al Gruppo**: Il receiver deve unirsi esplicitamente al gruppo multicast
- **MulticastSocket**: Socket specializzato per operazioni multicast
- **Leave dal Gruppo**: Importante lasciare il gruppo alla chiusura
- **Selettività**: Solo i membri del gruppo ricevono i messaggi

## Caratteristiche

✅ Join automatico al gruppo multicast  
✅ Ricezione solo messaggi del gruppo  
✅ Leave pulito alla chiusura  
✅ Validazione indirizzo multicast  
✅ Output formattato e dettagliato  

## Compilazione

```bash
javac MulticastReceiver.java
```

## Esecuzione

### Uso Base
```bash
java MulticastReceiver
```
Usa indirizzo e porta di default (239.255.0.1:5000)

### Con Parametri
```bash
java MulticastReceiver <indirizzo_multicast> <porta>
```

### Esempi

```bash
# Gruppo di default
java MulticastReceiver

# Gruppo personalizzato
java MulticastReceiver 239.1.1.1 5000

# Porta diversa
java MulticastReceiver 239.255.0.1 6000

# Gruppo mDNS
java MulticastReceiver 224.0.0.251 5353
```

## Output Atteso

```
═══════════════════════════════════════════════════
📡 MULTICAST UDP RECEIVER
═══════════════════════════════════════════════════
🔌 Porta: 5000
📡 Gruppo Multicast: 239.255.0.1
═══════════════════════════════════════════════════

✅ MulticastSocket creato sulla porta 5000
🤝 Unito al gruppo multicast 239.255.0.1
👂 In attesa di messaggi multicast...

Premi Ctrl+C per terminare

╔═══════════════════════════════════════════════════
║ 📥 MESSAGGIO MULTICAST #1 RICEVUTO
╠═══════════════════════════════════════════════════
║ ⏰ Ora: 14:30:15
║ 👤 Mittente: 192.168.1.100:54321
║ 📦 Dimensione: 62 bytes
╠═══════════════════════════════════════════════════
║ 📝 Tipo: MULTICAST
║ 💬 Contenuto: Messaggio #1
║ 🕐 Timestamp: 1705420800000
║ 🌐 TTL: 1
╚═══════════════════════════════════════════════════
```

## Test Completo

**Terminale 1 (Receiver):**
```bash
javac MulticastReceiver.java
java MulticastReceiver 239.255.0.1 5000
```

**Terminale 2 (Sender):**
```bash
cd ../es02.03-multicast-sender-java
java MulticastSender 239.255.0.1 5000 2000 1
```

## Multipli Receiver sullo Stesso Gruppo

Il multicast permette multipli receiver:

**Terminale 1:**
```bash
java MulticastReceiver 239.255.0.1 5000
```

**Terminale 2:**
```bash
java MulticastReceiver 239.255.0.1 5000
```

**Terminale 3:**
```bash
java MulticastReceiver 239.255.0.1 5000
```

**Terminale 4 (Sender):**
```bash
cd ../es02.03-multicast-sender-java
java MulticastSender 239.255.0.1 5000 2000 1
```

Tutti e tre i receiver riceveranno i messaggi!

## Gruppi Multicast Diversi

I receiver su gruppi diversi NON ricevono i messaggi degli altri:

**Terminale 1 (Gruppo A):**
```bash
java MulticastReceiver 239.255.0.1 5000
```

**Terminale 2 (Gruppo B):**
```bash
java MulticastReceiver 239.255.0.2 5000
```

**Terminale 3 (Sender su Gruppo A):**
```bash
cd ../es02.03-multicast-sender-java
java MulticastSender 239.255.0.1 5000 2000 1
```

Solo il Terminale 1 riceverà i messaggi!

## Codice Rilevante

### Creazione e Join
```java
// Crea socket sulla porta specifica
MulticastSocket socket = new MulticastSocket(port);

// Indirizzo gruppo multicast
InetAddress group = InetAddress.getByName("239.255.0.1");

// Join al gruppo (NECESSARIO!)
socket.joinGroup(group);
```

### Ricezione
```java
byte[] buffer = new byte[BUFFER_SIZE];
DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

// Riceve pacchetto
socket.receive(packet);

// Estrae dati
String message = new String(packet.getData(), 0, packet.getLength());
```

### Leave e Cleanup
```java
try {
    // Lascia il gruppo multicast
    socket.leaveGroup(group);
    
    // Chiude il socket
    socket.close();
} catch (IOException e) {
    // Gestione errore
}
```

## Differenze con Broadcast Receiver

| Aspetto | Broadcast Receiver | Multicast Receiver |
|---------|-------------------|-------------------|
| **Socket** | DatagramSocket | MulticastSocket |
| **Join** | Non richiesto | RICHIESTO joinGroup() |
| **Indirizzo** | 255.255.255.255 | 224.0.0.0 - 239.255.255.255 |
| **Selettività** | Tutti ricevono | Solo membri gruppo |
| **Leave** | Non applicabile | NECESSARIO leaveGroup() |
| **Routing** | Solo LAN | Può attraversare router |

## Gestione Interfacce Multiple

Su sistemi con multiple interfacce di rete, specifica quale usare:

```java
MulticastSocket socket = new MulticastSocket(port);

// Ottieni interfaccia specifica
NetworkInterface ni = NetworkInterface.getByName("eth0");

// Usa questa interfaccia per multicast
socket.setNetworkInterface(ni);

// Join al gruppo
InetAddress group = InetAddress.getByName("239.255.0.1");
socket.joinGroup(group);
```

Oppure per indirizzo IP:

```java
socket.setInterface(InetAddress.getByName("192.168.1.100"));
```

## Loopback Mode

Controlla se ricevi i tuoi stessi messaggi:

```java
// Disabilita ricezione dei propri pacchetti
socket.setLoopbackMode(true);  // true = disabilita loopback

// Abilita ricezione dei propri pacchetti (default)
socket.setLoopbackMode(false); // false = abilita loopback
```

## Configurazione di Rete

### Linux

```bash
# Verifica supporto multicast
ip maddr show

# Abilita multicast su interfaccia
sudo ip link set eth0 multicast on

# Aggiungi route multicast
sudo route add -net 224.0.0.0 netmask 240.0.0.0 dev eth0
```

### Firewall

```bash
# Linux (ufw)
sudo ufw allow 5000/udp

# Iptables - permetti traffico multicast in ingresso
sudo iptables -A INPUT -p udp -d 224.0.0.0/4 -j ACCEPT
```

## Risoluzione Problemi

### Non riceve messaggi

1. **Verifica join al gruppo**:
   ```java
   socket.joinGroup(group); // Non dimenticarlo!
   ```

2. **Stesso indirizzo e porta**:
   - Sender: `239.255.0.1:5000`
   - Receiver: `239.255.0.1:5000`

3. **Firewall**: Verifica che non blocchi UDP

4. **Interfaccia di rete**: Specifica l'interfaccia corretta se hai multiple NIC

### "Address already in use"

La porta è già occupata. Opzioni:

```bash
# Usa porta diversa
java MulticastReceiver 239.255.0.1 5001

# Oppure trova e termina il processo che usa la porta
# Linux
sudo netstat -tulpn | grep :5000
sudo kill -9 <PID>

# Windows
netstat -ano | findstr :5000
taskkill /PID <PID> /F
```

### "Network is unreachable"

Il multicast potrebbe non essere supportato:

```bash
# Linux - verifica routing multicast
ip mroute show

# Verifica interfaccia supporti multicast
ip link show | grep MULTICAST
```

## Best Practices

### ✅ Raccomandazioni

1. **Sempre fare join**: `socket.joinGroup(group)`
2. **Sempre fare leave**: `socket.leaveGroup(group)` nel finally
3. **Gestire eccezioni**: Try-catch-finally per cleanup
4. **Validare indirizzo**: Verificare sia multicast valido
5. **Specificare interfaccia**: Su sistemi multi-NIC

### ❌ Errori Comuni

1. Dimenticare `joinGroup()`
2. Non fare `leaveGroup()` alla chiusura
3. Usare DatagramSocket invece di MulticastSocket
4. Indirizzo multicast non valido
5. Firewall che blocca UDP

## Esempi Correlati

- [es02.03-multicast-sender-java](../es02.03-multicast-sender-java/README.md) - Sender multicast
- [es02.01-broadcast-sender-java](../es02.01-broadcast-sender-java/README.md) - Sender broadcast
- [es02.08-multicast-receiver-js](../es02.08-multicast-receiver-js/README.md) - Receiver multicast in JavaScript
