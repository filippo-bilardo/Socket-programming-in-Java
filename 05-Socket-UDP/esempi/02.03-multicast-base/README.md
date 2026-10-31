# 📡 Multicast UDP Base - Analisi Approfondita

Questo esempio dimostra l'implementazione base della **comunicazione multicast UDP** sia in **Java** che in **JavaScript/Node.js**. Il multicast permette di inviare un singolo messaggio che può essere ricevuto da più destinatari che si sono "iscritti" a un gruppo multicast specifico.

## 📁 File dell'esempio

### Versione Java
- **Sender.java** - Invia messaggi al gruppo multicast
- **Receiver.java** - Riceve messaggi dal gruppo multicast

### Versione JavaScript
- **sender.js** - Invia messaggi al gruppo multicast
- **receiver.js** - Riceve messaggi dal gruppo multicast
- **package.json** - Configurazione progetto Node.js

## 📚 Concetti Fondamentali


### MulticastSocket vs DatagramSocket

```java
DatagramSocket     → Unicast (punto-a-punto)
MulticastSocket    → Multicast (punto-a-multipunto)
                     Estende DatagramSocket con funzionalità di gruppo
```

**Nota importante**: Per **inviare** messaggi multicast è tecnicamente sufficiente un `DatagramSocket`, ma questo esempio usa `MulticastSocket` per entrambi a scopo didattico.

---

## 🚀 Analisi Sender.java

### Struttura del Codice

```java
public class Sender {
    private static final String SENDER_IP = "127.0.0.1";
    private static final String MULTICAST_IP = "230.0.0.1";
    private static final int MULTICAST_PORT = 19876;
```

#### 📌 Costanti di Configurazione

| Costante | Valore | Descrizione |
|----------|--------|-------------|
| `SENDER_IP` | 127.0.0.1 | Interfaccia di rete locale (loopback) |
| `MULTICAST_IP` | 230.0.0.1 | Indirizzo del gruppo multicast |
| `MULTICAST_PORT` | 19876 | Porta UDP per la comunicazione |

**💡 Nota**: `127.0.0.1` (localhost) funziona solo per test sulla stessa macchina. Per comunicazione tra macchine diverse, usa l'IP reale della tua interfaccia di rete.

### Analisi Step-by-Step

#### 1️⃣ Creazione del Socket

```java
MulticastSocket socket = new MulticastSocket();
```

**Cosa succede:**
- Crea un socket multicast senza binding a una porta specifica
- Il sistema operativo assegna automaticamente una porta disponibile
- Il socket è pronto per inviare/ricevere dati multicast

**Perché senza porta?**
- Il sender non ha bisogno di una porta fissa per inviare
- Solo i receiver devono "ascoltare" su una porta nota

#### 2️⃣ Configurazione dell'Indirizzo Gruppo

```java
InetAddress group = InetAddress.getByName(MULTICAST_IP);
```

**Cosa succede:**
- Converte la stringa IP `"230.0.0.1"` in un oggetto `InetAddress`
- Questo rappresenta il gruppo multicast a cui inviare i messaggi
- `InetAddress.getByName()` può sollevare `UnknownHostException`

#### 3️⃣ Configurazione dell'Interfaccia di Rete

```java
socket.setInterface(InetAddress.getByName(SENDER_IP));
```

**Cosa succede:**
- Specifica quale interfaccia di rete usare per inviare pacchetti multicast
- Importante su sistemi con **multiple interfacce di rete** (WiFi, Ethernet, VPN, etc.)

**Quando è necessario:**
```
Sistema con WiFi + Ethernet:
├── wlan0: 192.168.1.100
└── eth0:  192.168.0.50

Senza setInterface() → usa interfaccia di default
Con setInterface() → forza l'uso di un'interfaccia specifica
```

#### 4️⃣ Unione al Gruppo Multicast (Opzionale per il Sender)

```java
socket.joinGroup(group);
```

**⚠️ Nota Importante:**
- **Per il sender, `joinGroup()` NON è tecnicamente necessario**
- Un `DatagramSocket` normale può inviare a un gruppo multicast
- In questo esempio è usato per:
  - Scopo didattico (mostrare la sintassi completa)
  - Coerenza con il receiver
  - Possibilità di ricevere feedback dal gruppo (se necessario)

**Differenza chiave:**
```java
// Opzione 1: Sender minimale (sufficiente)
DatagramSocket socket = new DatagramSocket();
// ... invia direttamente al gruppo

// Opzione 2: Sender con MulticastSocket (questo esempio)
MulticastSocket socket = new MulticastSocket();
socket.joinGroup(group);  // Non strettamente necessario
```

#### 5️⃣ Preparazione del Messaggio

```java
String message = "Messaggio da Sender";
DatagramPacket packet = new DatagramPacket(
    message.getBytes(),      // Dati in byte array
    message.length(),        // Lunghezza del messaggio
    group,                   // Indirizzo destinazione (gruppo multicast)
    MULTICAST_PORT          // Porta destinazione
);
```

**Anatomia del DatagramPacket:**

```
┌─────────────────────────────────────┐
│   DatagramPacket                    │
├─────────────────────────────────────┤
│ Data:    "Messaggio da Sender"      │ → byte[] dei dati
│ Length:  20 bytes                   │ → lunghezza effettiva
│ Address: 230.0.0.1                  │ → gruppo multicast
│ Port:    19876                      │ → porta destinazione
└─────────────────────────────────────┘
```

**⚙️ Conversione Stringa → Bytes:**
```java
"Messaggio da Sender".getBytes()
→ [77, 101, 115, 115, 97, 103, ...]  // Array di byte UTF-8
```

#### 6️⃣ Invio del Pacchetto

```java
socket.send(packet);
```

**Cosa succede a livello di rete:**

```
1. Il pacchetto viene inviato all'indirizzo multicast 230.0.0.1:19876
2. I router di rete replicano il pacchetto verso tutti i membri del gruppo
3. Tutti i receiver che hanno fatto joinGroup(230.0.0.1) ricevono il pacchetto
4. I receiver NON sul gruppo non vedono il traffico
```

**Flusso del pacchetto:**
```
Sender (230.0.0.1:19876)
    ↓
[Router/Switch con IGMP]
    ↓ ↓ ↓ ↓ ↓
    ↓ ↓ ↓ ↓ └──→ Receiver 5
    ↓ ↓ ↓ └────→ Receiver 4
    ↓ ↓ └──────→ Receiver 3
    ↓ └────────→ Receiver 2
    └──────────→ Receiver 1
```

#### 7️⃣ Cleanup - Uscita dal Gruppo

```java
socket.leaveGroup(group);
```

**Cosa succede:**
- Invia un messaggio IGMP (Internet Group Management Protocol) al router
- Comunica che non vuole più ricevere messaggi per quel gruppo
- Libera risorse associate all'appartenenza al gruppo

**IGMP Leave Process:**
```
1. Application: socket.leaveGroup(group)
2. OS: Invia IGMP Leave Message
3. Router: Rimuove interfaccia dalla tabella multicast
4. Traffico: Non viene più inoltrato verso questa interfaccia
```

#### 8️⃣ Chiusura del Socket

```java
socket.close();
```

**Cosa succede:**
- Rilascia il socket e tutte le risorse associate
- Chiude la porta UDP allocata dal sistema
- Dopo `close()`, il socket non può più essere usato

---

## 📥 Analisi Receiver.java

### Struttura del Codice

```java
public class Receiver {
    private static final String RECEIVER_IP = "127.0.0.1";
    private static final String MULTICAST_IP = "230.0.0.1";
    private static final int MULTICAST_PORT = 19876;
```

Le costanti sono identiche al sender per garantire la comunicazione sullo stesso gruppo.

### Analisi Step-by-Step

#### 1️⃣ Creazione del Socket con Binding

```java
MulticastSocket socket = new MulticastSocket(MULTICAST_PORT);
```

**Differenza critica con il Sender:**
```java
// Sender (porta casuale)
MulticastSocket socket = new MulticastSocket();

// Receiver (porta fissa 19876)
MulticastSocket socket = new MulticastSocket(19876);
```

**Perché il receiver ha bisogno di una porta specifica?**
- Deve **ascoltare** sulla stessa porta su cui il sender invia
- Il sender invia a `230.0.0.1:19876`
- Il receiver deve quindi essere in ascolto su porta `19876`

**⚠️ Cosa succede se la porta è occupata:**
```java
Exception in thread "main" java.net.BindException: Address already in use
```
Soluzione: Chiudere il processo che usa la porta o scegliere una porta diversa.

#### 2️⃣ Configurazione dell'Interfaccia

```java
InetAddress group = InetAddress.getByName(MULTICAST_IP);
socket.setInterface(InetAddress.getByName(RECEIVER_IP));
```

Stesso concetto del sender: specifica su quale interfaccia di rete ascoltare i pacchetti multicast.

#### 3️⃣ Unione al Gruppo Multicast ⭐ FONDAMENTALE

```java
socket.joinGroup(group);
```

**🔴 ATTENZIONE: Questo è OBBLIGATORIO per il receiver!**

**Cosa succede internamente:**

1. **Livello Applicazione:**
   ```java
   socket.joinGroup(InetAddress.getByName("230.0.0.1"));
   ```

2. **Livello Sistema Operativo:**
   - Aggiorna la tabella degli indirizzi multicast
   - Configura la scheda di rete per accettare pacchetti destinati a 230.0.0.1

3. **Livello Rete (IGMP - Internet Group Management Protocol):**
   - Invia un messaggio IGMP "Join Group" al router locale
   - Router aggiorna la sua tabella di forwarding multicast
   - Router inizia a inoltrare traffico per 230.0.0.1 verso questa interfaccia

**Visualizzazione IGMP:**
```
Receiver                 Router                    Network
   |                        |                          |
   |-- IGMP Join 230.0.0.1 →|                          |
   |                        |-- Update table           |
   |                        |   Add: eth0 → 230.0.0.1 |
   |                        |                          |
   |                     [Sender invia a 230.0.0.1]    |
   |                        |                          |
   |← Forward packet -------|                          |
   |                        |                          |
```

**Senza `joinGroup()`:**
- Il receiver NON riceverà i pacchetti multicast
- La scheda di rete scarta i pacchetti destinati al gruppo
- `socket.receive()` rimarrà bloccato indefinitamente

#### 4️⃣ Preparazione del Buffer di Ricezione

```java
byte[] buffer = new byte[1024];
DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
```

**Anatomia del buffer:**

```
Buffer di ricezione (1024 bytes):
┌────────────────────────────────────────┐
│ [0][1][2]...[1023]                    │ ← Array inizialmente vuoto
└────────────────────────────────────────┘
         ↑
DatagramPacket punta a questo buffer
```

**Dimensione del buffer:**
- `1024 bytes` = 1 KB
- Dimensione tipica per messaggi UDP piccoli
- Se il messaggio è > 1024 bytes, viene troncato

**💡 Best Practice:**
```java
// Per messaggi piccoli
byte[] buffer = new byte[1024];

// Per messaggi medi
byte[] buffer = new byte[8192];  // 8 KB

// Per file o dati grandi
byte[] buffer = new byte[65507]; // Max UDP payload
```

#### 5️⃣ Loop di Ricezione

```java
String message = "";
while (!message.equals("bye")) {
    socket.receive(packet);
    message = new String(packet.getData(), 0, packet.getLength());
    System.out.println("Messaggio ricevuto: " + message);
}
```

**Analisi dettagliata:**

##### A. `socket.receive(packet)` - CHIAMATA BLOCCANTE ⏸️

```java
socket.receive(packet);  // ← Il programma si ferma qui fino all'arrivo di un pacchetto
```

**Comportamento:**
- Il thread si **blocca** (wait) fino all'arrivo di dati
- Quando arriva un pacchetto, il metodo ritorna
- I dati vengono scritti nel buffer del `packet`

**Timeline:**
```
T0: socket.receive(packet) chiamato
    ↓ [thread in attesa] ⏳
    ↓ [thread in attesa] ⏳
    ↓ [thread in attesa] ⏳
T5: Pacchetto arriva dalla rete
    ↓ [copia dati nel buffer]
T6: receive() ritorna
    ↓ [continua esecuzione]
```

##### B. Estrazione del Messaggio

```java
message = new String(packet.getData(), 0, packet.getLength());
```

**⚠️ ATTENZIONE: Usa `getLength()`, non `getData().length`!**

**Esempio:**
```java
// Buffer da 1024 bytes
byte[] buffer = new byte[1024];

// Riceviamo "Ciao" (4 bytes)
socket.receive(packet);

// SBAGLIATO ❌
String msg = new String(packet.getData());
// → "Ciao" + 1020 caratteri spazzatura (null, dati vecchi)

// CORRETTO ✅
String msg = new String(packet.getData(), 0, packet.getLength());
// → "Ciao" (solo i 4 bytes effettivi)
```

**Visualizzazione del buffer:**
```
Buffer dopo ricezione di "Ciao":
┌────────────────────────────────────────┐
│ C | i | a | o | ? | ? | ? | ... | ?   │
└────────────────────────────────────────┘
  0   1   2   3   4   5   6  ...  1023
  
getData()       → tutto l'array [0..1023]
getLength()     → 4 (bytes effettivi)
getData(0, 4)   → solo [C,i,a,o]
```

##### C. Condizione di Uscita

```java
while (!message.equals("bye")) {
```

**⚠️ BUG RISOLTO nella versione 1.10:**

```java
// VERSIONE PRECEDENTE (ERRATA) ❌
while (message != "bye") {
    // Confronto con == non funziona per String!
    // Confronta i riferimenti, non il contenuto
}

// VERSIONE CORRETTA ✅
while (!message.equals("bye")) {
    // Confronta il contenuto effettivo della stringa
}
```

**Spiegazione del bug:**
```java
String a = "bye";
String b = new String("bye");

a == b           // false (riferimenti diversi) ❌
a.equals(b)      // true (stesso contenuto)     ✅
```

#### 6️⃣ Informazioni sul Mittente

Il `DatagramPacket` contiene anche informazioni sul mittente:

```java
InetAddress senderAddress = packet.getAddress();
int senderPort = packet.getPort();
byte[] data = packet.getData();
int length = packet.getLength();
```

**Esempio di output:**
```
Da: 192.168.1.100:54321
Dati: "Messaggio da Sender"
Lunghezza: 20 bytes
```

#### 7️⃣ Cleanup

```java
socket.leaveGroup(group);
socket.close();
```

Stesso processo del sender: comunica al router di non inoltrare più traffico multicast e libera le risorse.

---

## 🔄 Differenze tra Unicast, Broadcast e Multicast

### Tabella Comparativa

| Caratteristica | Unicast | Broadcast | Multicast |
|----------------|---------|-----------|-----------|
| **Destinatari** | Uno | Tutti nella rete | Gruppo specifico |
| **Indirizzo IP** | IP normale (es. 192.168.1.10) | 255.255.255.255 | 224.0.0.0 - 239.255.255.255 |
| **Efficienza** | Alta per 1 destinatario | Bassa (congestiona la rete) | Alta per N destinatari |
| **Utilizzo banda** | N pacchetti per N destinatari | 1 pacchetto a tutti | 1 pacchetto al gruppo |
| **Scalabilità** | Bassa | Molto bassa | Alta |
| **Esempio** | Email, Web browsing | ARP, DHCP discovery | Video streaming, conferenze |

### Visualizzazione

```
UNICAST (1 → 1)
Sender → → → Receiver1

BROADCAST (1 → ALL)
        ┌→ Device1
        ├→ Device2
Sender ─┼→ Device3
        ├→ Device4
        └→ Device5

MULTICAST (1 → GROUP)
        ┌→ Receiver1 (nel gruppo)
Sender ─┼→ Receiver2 (nel gruppo)
        └→ Receiver3 (nel gruppo)
        
   Device4 (non nel gruppo) ← non riceve nulla
   Device5 (non nel gruppo) ← non riceve nulla
```

### Quando usare Multicast?

✅ **Usa Multicast per:**
- Streaming video/audio a più client
- Distribuzione di aggiornamenti software
- Videoconferenze
- Gaming multiplayer
- Distribuzione dati finanziari in tempo reale

❌ **Non usare Multicast per:**
- Comunicazioni uno-a-uno (usa unicast)
- Comunicazioni che richiedono acknowledgment (UDP è inaffidabile)
- Reti senza supporto IGMP
- Quando serve garantire la consegna (usa TCP unicast)

---

## 🚀 Esecuzione

### Prerequisiti

```bash
# Verifica di avere Java installato
java -version

# Verifica supporto multicast sul sistema
# Linux/Mac:
ifconfig | grep -i multicast

# Windows:
ipconfig /all | findstr "Multicast"
```

### Compilazione

```bash
# Compila entrambi i file
javac Sender.java Receiver.java

# Verifica la compilazione
ls -la *.class
# Output atteso:
# Receiver.class
# Sender.class
```

### Esecuzione Passo-Passo

#### Scenario 1: Singolo Receiver

**Terminale 1 - Receiver:**
```bash
java Receiver
```
Output:
```
Receiver in ascolto. Ctrl+C per terminare.
```

**Terminale 2 - Sender:**
```bash
java Sender
```
Output:
```
Messaggio inviato al gruppo di multicast.
```

**Terminale 1 - Receiver riceve:**
```
Messaggio ricevuto: Messaggio da Sender
```

#### Scenario 2: Multipli Receiver

**Terminale 1 - Receiver 1:**
```bash
java Receiver
```

**Terminale 2 - Receiver 2:**
```bash
java Receiver
```

**Terminale 3 - Receiver 3:**
```bash
java Receiver
```

**Terminale 4 - Sender:**
```bash
java Sender
```

**Risultato:**
TUTTI e tre i receiver ricevono simultaneamente lo stesso messaggio!

```
Receiver 1: Messaggio ricevuto: Messaggio da Sender
Receiver 2: Messaggio ricevuto: Messaggio da Sender
Receiver 3: Messaggio ricevuto: Messaggio da Sender
```

#### Scenario 3: Test Terminazione

**Receiver in ascolto:**
```bash
java Receiver
```

**Invia messaggio "bye":**

Modifica temporaneamente il Sender:
```java
String message = "bye";  // Cambia da "Messaggio da Sender"
```

Ricompila ed esegui:
```bash
javac Sender.java
java Sender
```

Il Receiver terminerà automaticamente.

---

## 🔄 Flusso di Comunicazione

### Diagramma di Sequenza Completo

```
Receiver                  Network/Router              Sender
   |                            |                        |
   |-- new MulticastSocket(19876)                       |
   |                            |                        |
   |-- joinGroup(230.0.0.1) --->|                        |
   |                            |-- IGMP Join            |
   |                            |   (registra receiver)  |
   |                            |                        |
   |-- receive() [BLOCKING] --->|                        |
   |   [thread in attesa]       |                        |
   |                            |                        |-- new MulticastSocket()
   |                            |                        |
   |                            |                        |-- joinGroup(230.0.0.1)
   |                            |<-- IGMP Join -----------|
   |                            |                        |
   |                            |                        |-- send(packet)
   |                            |<-- UDP Packet ----------|
   |                            |   (dest: 230.0.0.1:19876)
   |                            |                        |
   |<-- Forward packet ---------|                        |
   |   [receive() ritorna]      |                        |
   |                            |                        |
   |-- print("Messaggio...")    |                        |-- leaveGroup()
   |                            |                        |
   |-- check if "bye"           |                        |-- close()
   |   [continua loop]          |                        |
   |                            |                        |
```

### Timeline Dettagliata

```
T=0s    Receiver: Avvio e join al gruppo 230.0.0.1
T=0.1s  Receiver: Invio IGMP Join al router
T=0.2s  Receiver: Entra in receive() bloccante
T=1s    Sender: Avvio applicazione
T=1.1s  Sender: Join al gruppo 230.0.0.1
T=1.2s  Sender: Invio pacchetto al gruppo
T=1.3s  Router: Riceve pacchetto e lo replica verso i membri
T=1.4s  Receiver: Riceve pacchetto, receive() ritorna
T=1.5s  Receiver: Stampa messaggio
T=1.6s  Receiver: Controlla if "bye" → false → continua loop
T=1.7s  Receiver: Torna in receive() bloccante
T=2s    Sender: leaveGroup() e close()
```

---

## 🔧 Considerazioni Tecniche

### 1. TTL (Time To Live) Multicast

Anche se non impostato esplicitamente in questo esempio, il TTL è importante:

```java
// Esempio con TTL
MulticastSocket socket = new MulticastSocket();
socket.setTimeToLive(1);  // Solo rete locale
```

**Valori TTL comuni:**

| TTL | Scopo |
|-----|-------|
| **0** | Stesso host (loopback) |
| **1** | Stessa subnet (default) ✅ Questo esempio |
| **32** | Stesso sito |
| **64** | Stessa regione |
| **128** | Stesso continente |
| **255** | Globale (unrestricted) |

### 2. Dimensioni Pacchetti UDP

**Limiti UDP:**
```
Max UDP Datagram: 65,535 bytes (teorico)
Max UDP Payload:   65,507 bytes (pratico, 65535 - 8 byte header - 20 byte IP)

Raccomandazione: ≤ 1472 bytes per evitare frammentazione IP
MTU Ethernet: 1500 bytes
  - 20 bytes IP header
  - 8 bytes UDP header
  = 1472 bytes payload sicuro
```

**In questo esempio:**
```java
byte[] buffer = new byte[1024];  // OK per messaggi piccoli
```

### 3. Affidabilità UDP

**⚠️ UDP è un protocollo INAFFIDABILE:**

```
Problemi possibili:
├── Pacchetti persi (no acknowledgment)
├── Pacchetti duplicati
├── Pacchetti fuori ordine
└── Pacchetti corrotti (checksum opzionale)
```

**Soluzioni se serve affidabilità:**
- Implementare ACK a livello applicazione
- Numeri di sequenza
- Timeout e ritrasmissione
- Oppure usare TCP (ma perde efficienza multicast)

### 4. Supporto Multicast sulla Rete

**Verifica supporto multicast:**

```bash
# Linux
ip maddr show

# Windows
netsh interface ipv4 show joins

# Mac
netstat -g
```

**Router Configuration:**
- Il router deve supportare **IGMP (Internet Group Management Protocol)**
- Alcuni router consumer disabilitano multicast per default
- Su reti aziendali, potrebbe essere bloccato dal firewall

### 5. Firewall e Sicurezza

**Configurazione firewall per multicast:**

```bash
# Linux (iptables)
sudo iptables -A INPUT -p udp -d 230.0.0.1 --dport 19876 -j ACCEPT

# Linux (firewalld)
sudo firewall-cmd --add-port=19876/udp --permanent

# Windows Firewall
netsh advfirewall firewall add rule name="Multicast Test" dir=in action=allow protocol=UDP localport=19876
```

### 6. Performance e Scalabilità

**Multicast vs Unicast - Comparazione pratica:**

```
Scenario: Inviare 1 MB a 100 destinatari

Unicast:
- Pacchetti inviati: 100
- Banda usata: 100 MB
- Tempo: ~10 secondi (su rete 100 Mbps)

Multicast:
- Pacchetti inviati: 1
- Banda usata: 1 MB
- Tempo: ~0.1 secondi
- Risparmio: 99% di banda! 🎉
```

### 7. Loopback Mode

Per default, il mittente multicast riceve le proprie trasmissioni:

```java
MulticastSocket socket = new MulticastSocket();
socket.setLoopbackMode(false); // Riceve i propri messaggi (default)
socket.setLoopbackMode(true);  // NON riceve i propri messaggi
```

---

## 🐛 Troubleshooting

### Problema: "Receiver non riceve messaggi"

#### Diagnosi 1: Verifica gruppo multicast

```bash
# Il receiver ha fatto joinGroup()?
# Controlla nel codice:
socket.joinGroup(group);  // Deve essere presente!
```

#### Diagnosi 2: Verifica porta

```bash
# Sender e Receiver usano la stessa porta?
# Sender:
DatagramPacket packet = new DatagramPacket(..., group, 19876);

# Receiver:
MulticastSocket socket = new MulticastSocket(19876);

# Le porte devono corrispondere!
```

#### Diagnosi 3: Verifica indirizzo gruppo

```bash
# Entrambi usano lo stesso MULTICAST_IP?
# Sender:   MULTICAST_IP = "230.0.0.1"
# Receiver: MULTICAST_IP = "230.0.0.1"
# Devono essere identici!
```

#### Diagnosi 4: Firewall

```bash
# Verifica che la porta UDP non sia bloccata
# Linux:
sudo netstat -ulnp | grep 19876

# Windows:
netstat -ano | findstr 19876
```

### Problema: "BindException: Address already in use"

**Causa:** Un altro processo sta usando la porta 19876.

**Soluzione 1: Trova e chiudi il processo**
```bash
# Linux/Mac
sudo lsof -i :19876
kill -9 <PID>

# Windows
netstat -ano | findstr 19876
taskkill /PID <PID> /F
```

**Soluzione 2: Cambia porta**
```java
private static final int MULTICAST_PORT = 19877;  // Usa porta diversa
```

### Problema: "Receiver riceve messaggi corrotti"

**Causa:** Buffer troppo piccolo o conversione errata.

**Soluzione:**
```java
// SBAGLIATO ❌
message = new String(packet.getData());

// CORRETTO ✅
message = new String(packet.getData(), 0, packet.getLength());
```

### Problema: "Multicast funziona in locale ma non tra macchine"

**Causa:** SENDER_IP/RECEIVER_IP = "127.0.0.1" (loopback).

**Soluzione:**
```java
// Trova il tuo IP reale
// Linux/Mac:   ifconfig
// Windows:     ipconfig

// Cambia da:
private static final String SENDER_IP = "127.0.0.1";

// A:
private static final String SENDER_IP = "192.168.1.100";  // IP reale
```

### Problema: "UnknownHostException"

**Causa:** Indirizzo IP malformato.

**Soluzione:**
```java
// Verifica formato IP
MULTICAST_IP = "230.0.0.1"     // ✅ Corretto
MULTICAST_IP = "230.0.0.999"   // ❌ Errore (999 > 255)
MULTICAST_IP = "230.0.0"       // ❌ Errore (incompleto)
```

### Test di Connettività

**Script di test:**

```bash
#!/bin/bash
# test-multicast.sh

echo "Test 1: Verifica interfaccia multicast"
ip link show | grep -i multicast

echo "Test 2: Verifica gruppi multicast"
ip maddr show

echo "Test 3: Test ping multicast"
ping -c 3 230.0.0.1

echo "Test 4: Verifica porta UDP aperta"
nc -ulv 19876
```

---

## 📖 Concetti Avanzati

### IGMP (Internet Group Management Protocol)

IGMP è il protocollo che gestisce l'appartenenza ai gruppi multicast.

**Versioni IGMP:**
- **IGMPv1**: Base (1989)
- **IGMPv2**: Aggiunge Leave Group (1997)
- **IGMPv3**: Supporta source filtering (2002)

**Messaggi IGMP principali:**

| Tipo | Direzione | Scopo |
|------|-----------|-------|
| **Membership Report** | Host → Router | "Voglio unirmi al gruppo X" |
| **Leave Group** | Host → Router | "Lascio il gruppo X" |
| **Membership Query** | Router → Host | "Chi è nel gruppo X?" |

### Source-Specific Multicast (SSM)

Versione avanzata che filtra anche per sorgente:

```java
// Riceve solo da mittenti specifici
NetworkInterface ni = NetworkInterface.getByName("eth0");
InetAddress group = InetAddress.getByName("232.0.0.1");
InetAddress source = InetAddress.getByName("192.168.1.10");

socket.joinGroup(new InetSocketAddress(group, MULTICAST_PORT), ni);
```

---

## 🎓 Conclusioni

Questo esempio dimostra i concetti fondamentali della comunicazione multicast UDP in **Java** e **JavaScript**:

✅ **Hai imparato:**
- Differenza tra unicast, broadcast e multicast
- Come creare e configurare MulticastSocket (Java) e dgram socket (JavaScript)
- Il ruolo di `joinGroup()`/`addMembership()` e `leaveGroup()`/`dropMembership()`
- La gestione dei pacchetti UDP in entrambi i linguaggi
- L'importanza del protocollo IGMP
- Pattern event-driven in JavaScript vs try-catch in Java

🚀 **Prossimi passi:**
1. Modifica il codice per supportare messaggi multipli
2. Aggiungi un timestamp ai messaggi
3. Implementa un chat multicast completo
4. Sperimenta con TTL diversi
5. Crea un sistema di discovery dei nodi
6. Confronta le performance tra Java e JavaScript

---

## 🔄 Equivalenze Java ↔ JavaScript

### Tabella di Corrispondenza API

| Concetto | Java | JavaScript (Node.js) |
|----------|------|----------------------|
| **Importazione** | `import java.net.*` | `const dgram = require('dgram')` |
| **Creazione socket** | `new MulticastSocket()` | `dgram.createSocket('udp4')` |
| **Binding porta** | `new MulticastSocket(port)` | `socket.bind(port)` |
| **Unione gruppo** | `socket.joinGroup(group)` | `socket.addMembership(ip)` |
| **Uscita gruppo** | `socket.leaveGroup(group)` | `socket.dropMembership(ip)` |
| **Invio pacchetto** | `socket.send(packet)` | `socket.send(buffer, offset, length, port, ip, callback)` |
| **Ricezione** | `socket.receive(packet)` [bloccante] | `socket.on('message', callback)` [event-driven] |
| **Impostazione TTL** | `socket.setTimeToLive(ttl)` | `socket.setMulticastTTL(ttl)` |
| **Chiusura socket** | `socket.close()` | `socket.close()` |
| **Gestione errori** | `try-catch` | `socket.on('error', callback)` |

### Differenze Architetturali

#### Java: Approccio Bloccante (Blocking)
```java
// Il thread si blocca fino alla ricezione
while (!message.equals("bye")) {
    socket.receive(packet);  // ← BLOCCA qui
    message = new String(packet.getData(), 0, packet.getLength());
    System.out.println(message);
}
```

#### JavaScript: Approccio Event-Driven
```javascript
// Event loop non bloccante
socket.on('message', (msg, rinfo) => {
    // ← Callback asincrono
    const message = msg.toString('utf8');
    console.log(message);
    if (message === 'bye') cleanup();
});
```

### Confronto Gestione Risorse

| Aspetto | Java | JavaScript |
|---------|------|------------|
| **Gestione memoria** | Try-with-resources automatico | Event listeners da rimuovere manualmente |
| **Cleanup** | `finally` block | `process.on('SIGINT')` per Ctrl+C |
| **Conversione dati** | `message.getBytes()` / `new String(bytes)` | `Buffer.from()` / `buffer.toString()` |
| **Informazioni sender** | `packet.getAddress()`, `packet.getPort()` | `rinfo.address`, `rinfo.port` |

---

## 🚀 Esecuzione Versione JavaScript

### Prerequisiti

```bash
# Verifica Node.js installato (richiesto >= 12.0.0)
node --version

# Esempio output:
# v18.17.0
```

### Installazione (opzionale)

```bash
# Non ci sono dipendenze esterne
# Il modulo dgram è built-in in Node.js
```

### Esecuzione

#### Scenario 1: Singolo Receiver

**Terminale 1 - Receiver:**
```bash
node receiver.js
```
Output:
```
═══════════════════════════════════════════════════
📡 MULTICAST UDP RECEIVER (JavaScript)
═══════════════════════════════════════════════════
🌐 Gruppo Multicast: 239.0.0.1
🔌 Porta: 9876
═══════════════════════════════════════════════════

✅ Socket in ascolto su 0.0.0.0:9876
🤝 Unito al gruppo multicast: 239.0.0.1

👂 Receiver in ascolto...
💡 Invia "bye" per terminare la comunicazione
Ctrl+C per forzare la chiusura
```

**Terminale 2 - Sender:**
```bash
node sender.js
```
Output:
```
═══════════════════════════════════════════════════
📡 MULTICAST UDP SENDER (JavaScript)
═══════════════════════════════════════════════════
🌐 Gruppo Multicast: 239.0.0.1
🔌 Porta: 9876
🌍 TTL: 1 (rete locale)
═══════════════════════════════════════════════════

⏱️  TTL impostato a: 1

📦 Preparato pacchetto:
   └─ Messaggio: "Messaggio da Sender"
   └─ Dimensione: 20 bytes
   └─ Destinazione: 239.0.0.1:9876

📤 Invio messaggio al gruppo multicast...
✅ Messaggio inviato con successo!
   Tutti i membri del gruppo 239.0.0.1 riceveranno il messaggio

🔒 Socket chiuso

👋 Sender terminato
```

**Terminale 1 - Receiver riceve:**
```
╔═══════════════════════════════════════════════════
║ 📥 MESSAGGIO #1 RICEVUTO
╠═══════════════════════════════════════════════════
║ 👤 Da: 127.0.0.1:54321
║ 📦 Dimensione: 20 bytes
║ 💬 Contenuto: Messaggio da Sender
╚═══════════════════════════════════════════════════
```

#### Scenario 2: Usando npm scripts

```bash
# Terminale 1
npm run receiver

# Terminale 2
npm run sender
```

#### Scenario 3: Multipli Receiver

```bash
# Terminale 1
node receiver.js

# Terminale 2
node receiver.js

# Terminale 3
node receiver.js

# Terminale 4
node sender.js
```

Tutti e tre i receiver ricevono simultaneamente!

---

## 🔍 Differenze Implementative Dettagliate

### 1. Creazione e Binding del Socket

**Java:**
```java
// Receiver: binding automatico alla porta
MulticastSocket socket = new MulticastSocket(MULTICAST_PORT);

// Sender: porta casuale
MulticastSocket socket = new MulticastSocket();
```

**JavaScript:**
```javascript
// Receiver: creazione + binding separati
const socket = dgram.createSocket({ type: 'udp4', reuseAddr: true });
socket.bind(MULTICAST_PORT);

// Sender: creazione semplice
const socket = dgram.createSocket('udp4');
```

**Nota**: `reuseAddr: true` permette a multipli receiver sulla stessa porta.

### 2. Unione al Gruppo Multicast

**Java:**
```java
InetAddress group = InetAddress.getByName(MULTICAST_IP);
socket.joinGroup(group);
```

**JavaScript:**
```javascript
// Chiamato DOPO il binding, nell'event 'listening'
socket.on('listening', () => {
    socket.addMembership(MULTICAST_IP);
});
```

**⚠️ Importante**: In JavaScript, `addMembership()` deve essere chiamato DOPO che il socket è in ascolto!

### 3. Ricezione Messaggi

**Java - Approccio Sincrono:**
```java
// Loop bloccante
while (!message.equals("bye")) {
    socket.receive(packet);  // Thread si blocca qui
    message = new String(packet.getData(), 0, packet.getLength());
    System.out.println(message);
}
```

**JavaScript - Approccio Asincrono:**
```javascript
// Event listener non bloccante
socket.on('message', (msg, rinfo) => {
    const message = msg.toString('utf8');
    console.log(message);
    
    if (message === EXIT_MESSAGE) {
        cleanup();
    }
});
```

### 4. Invio Messaggi

**Java:**
```java
String message = "Messaggio da Sender";
DatagramPacket packet = new DatagramPacket(
    message.getBytes(),
    message.length(),
    group,
    MULTICAST_PORT
);
socket.send(packet);
```

**JavaScript:**
```javascript
const message = 'Messaggio da Sender';
const buffer = Buffer.from(message, 'utf8');

socket.send(
    buffer,
    0,
    buffer.length,
    MULTICAST_PORT,
    MULTICAST_IP,
    (err) => {
        if (err) console.error(err);
        else console.log('Inviato!');
    }
);
```

### 5. Gestione Cleanup

**Java:**
```java
try (MulticastSocket socket = new MulticastSocket(port)) {
    // ... codice ...
} catch (Exception e) {
    e.printStackTrace();
} finally {
    socket.leaveGroup(group);
    socket.close();
}
```

**JavaScript:**
```javascript
function cleanup() {
    socket.dropMembership(MULTICAST_IP);
    socket.close();
}

process.on('SIGINT', () => {
    cleanup();
});
```

### 6. Conversione Byte ↔ Stringa

**Java:**
```java
// String → byte[]
byte[] bytes = message.getBytes();

// byte[] → String (con lunghezza corretta!)
String msg = new String(packet.getData(), 0, packet.getLength());
```

**JavaScript:**
```javascript
// String → Buffer
const buffer = Buffer.from(message, 'utf8');

// Buffer → String
const msg = buffer.toString('utf8');
```

### 7. Informazioni sul Mittente

**Java:**
```java
InetAddress senderAddress = packet.getAddress();
int senderPort = packet.getPort();
int size = packet.getLength();
```

**JavaScript:**
```javascript
socket.on('message', (msg, rinfo) => {
    const address = rinfo.address;  // "192.168.1.10"
    const port = rinfo.port;        // 54321
    const size = rinfo.size;        // 20
});
```

---

## 🧪 Test di Interoperabilità

### Java Sender → JavaScript Receiver

```bash
# Terminale 1
node receiver.js

# Terminale 2
java Sender
```

✅ **Funziona!** I due linguaggi sono completamente interoperabili.

### JavaScript Sender → Java Receiver

```bash
# Terminale 1
java Receiver

# Terminale 2
node sender.js
```

✅ **Funziona!** UDP multicast è indipendente dal linguaggio.

### Mix di Receiver

```bash
# Terminale 1
java Receiver

# Terminale 2
node receiver.js

# Terminale 3
java Receiver

# Terminale 4 - invia da uno qualsiasi
node sender.js
# OPPURE
java Sender
```

✅ **Funziona!** Tutti i receiver (Java e JavaScript) ricevono il messaggio!

---

## 📚 Riferimenti

- [RFC 1112 - Host Extensions for IP Multicasting](https://tools.ietf.org/html/rfc1112)
- [Java MulticastSocket Documentation](https://docs.oracle.com/javase/8/docs/api/java/net/MulticastSocket.html)
- [IGMP Protocol Specification](https://tools.ietf.org/html/rfc2236)
- [IP Multicast Address Allocation](https://www.iana.org/assignments/multicast-addresses)

---

**© 2026 Filippo Bilardo - Socket Programming Course**
