# Broadcast e Multicast UDP

## Introduzione
Una delle caratteristiche più potenti di UDP è la capacità di inviare dati a **multipli destinatari contemporaneamente**. Questo modulo esplora le tecniche di broadcast e multicast per comunicazioni efficienti uno-a-molti.

### Tipi di Comunicazione di Rete

Per comunicazione **unicast** (si veda il modulo precedente), un singolo mittente invia dati a un singolo destinatario utilizzando l’indirizzo IP del destinatario.

Quando si parla di **broadcast UDP**, ci si riferisce all’invio di pacchetti **UDP** (User Datagram Protocol) a **tutti i dispositivi** all’interno di una **rete locale** (LAN) utilizzando un **indirizzo broadcast**. Questo meccanismo è utile per scopi come la scoperta automatica di dispositivi, l’invio di messaggi a tutti i nodi di una rete o la configurazione di servizi di rete.

Quando si parla di **multicast UDP**, invece, si fa riferimento all’invio di pacchetti UDP a un **gruppo selezionato di destinatari** che hanno espresso interesse a ricevere tali dati. Gli indirizzi multicast sono specifici e consentono una comunicazione più efficiente rispetto al broadcast, poiché solo i dispositivi iscritti al gruppo riceveranno i pacchetti.

#### Unicast (1:1)
```java
// Comunicazione tradizionale punto-a-punto
// Un mittente → Un destinatario
InetAddress target = InetAddress.getByName("192.168.1.100");
DatagramPacket packet = new DatagramPacket(data, data.length, target, port);
// Utilizza un DatagramSocket standard
DatagramSocket socket = new DatagramSocket();
// Invia pacchetto unicast
socket.send(packet);
```

#### Broadcast (1:Tutti nella subnet)
```java
// Un mittente → Tutti i dispositivi nella rete locale
// Utilizza l'indirizzo di broadcast della subnet
InetAddress broadcast = InetAddress.getByName("192.168.1.255");
DatagramPacket packet = new DatagramPacket(data, data.length, broadcast, port);
// Abilita il broadcast sul socket
DatagramSocket socket = new DatagramSocket();
socket.setBroadcast(true);
// Invia pacchetto broadcast
socket.send(packet);
```

#### Multicast (1:Gruppo)
```java
// Un mittente → Gruppo specifico di destinatari
// Utilizza indirizzi IP multicast (224.0.0.0 - 239.255.255.255)
InetAddress multicast = InetAddress.getByName("224.0.0.1");
DatagramPacket packet = new DatagramPacket(data, data.length, multicast, port);
// Join multicast group to receive
MulticastSocket socket = new MulticastSocket(port);
socket.joinGroup(multicast);
// Invia pacchetto multicast
socket.send(packet);
```

## **Broadcast UDP**

Il **broadcast** è un metodo di trasmissione in cui un pacchetto viene inviato a **tutti i dispositivi** connessi a una rete locale. A differenza del **multicast** (che invia dati solo ai dispositivi che hanno espresso interesse) o dell’**unicast** (che invia dati a un singolo destinatario), il broadcast raggiunge **tutti i nodi** nella sottorete.

### **2. Indirizzi Broadcast in IPv4**
In IPv4, un indirizzo broadcast è identificato da:
- **Indirizzo di rete con tutti gli host bit impostati a 1**.
  - Esempio: Se la rete è **192.168.1.0/24**, l’indirizzo broadcast è **192.168.1.255**.
- **Indirizzo limitato (limited broadcast)**: **255.255.255.255**, che raggiunge tutti i dispositivi nella rete locale, indipendentemente dalla sottorete.

### **3. UDP e Broadcast**
Il protocollo **UDP** è spesso utilizzato per il broadcast perché:
- È **connectionless**: Non richiede una connessione stabilita tra mittente e destinatario.
- È **leggero**: Non ha il sovraccarico del controllo di flusso o della consegna garantita (a differenza di TCP).
- È **adatto a messaggi "one-to-all"**: Ideale per applicazioni come la scoperta di servizi (es. DHCP, ARP, mDNS).

### **4. Come Funziona il Broadcast UDP?**
1. **Invio del pacchetto**: Un’applicazione invia un pacchetto UDP all’indirizzo broadcast della rete (es. **192.168.1.255**).
2. **Propagazione**: Il pacchetto viene recapitato a **tutti i dispositivi** nella stessa rete locale.
3. **Ricezione**: Ogni dispositivo nella rete riceve il pacchetto, ma solo le applicazioni configurate per ascoltare la porta UDP specificata lo elaboreranno.


### **5. Esempi di Utilizzo del Broadcast UDP**
- **DHCP (Dynamic Host Configuration Protocol)**: Un client invia un messaggio **DHCPDISCOVER** a **255.255.255.255:67** per trovare un server DHCP.
- **ARP (Address Resolution Protocol)**: Utilizzato per risolvere un indirizzo IP in un indirizzo MAC.
- **mDNS (Multicast DNS)**: Anche se il nome suggerisce "multicast", utilizza anche il broadcast per la scoperta di servizi locali (es. **224.0.0.251** per Bonjour/Apple).
- **Wake-on-LAN**: Invia un "magic packet" UDP a un indirizzo broadcast per svegliare un dispositivo in standby.
- **Scoperta di servizi**: Applicazioni che cercano dispositivi o servizi nella rete locale (es. stampanti, server).
- **Notifiche di rete**: Invio di messaggi di stato o aggiornamenti a tutti i nodi della rete.
- **Giochi multiplayer locali**: Invio di aggiornamenti di stato a tutti i giocatori nella stessa rete.
 

### **6. Limitazioni del Broadcast UDP**
- **Non instradabile**: I router **bloccano** il traffico broadcast per evitare congestioni su Internet. Il broadcast è limitato alla **rete locale**.
- **Sicurezza**: Poiché tutti i dispositivi ricevono il pacchetto, può essere sfruttato per attacchi **DoS** (Denial of Service) o scansioni di rete non autorizzate.
- **Prestazioni**: L’invio eccessivo di pacchetti broadcast può causare **congestione** nella rete locale.

### **7. Esempio di Codice (Javascript)**
Ecco un esempio di come inviare un pacchetto UDP broadcast in JavaScript utilizzando il modulo `dgram` di Node.js:

```javascript
const dgram = require('dgram');
const socket = dgram.createSocket('udp4');

socket.bind(() => {
  // Abilita il broadcast
  socket.setBroadcast(true);

  const message = Buffer.from("Hello, this is a broadcast message!");
  const broadcastAddress = '192.168.1.255';
  const port = 5000;

  socket.send(message, 0, message.length, port, broadcastAddress, (err) => {
    if (err) {
      console.error('Errore durante l\'invio del messaggio broadcast:', err);
    } else {
      console.log('Messaggio broadcast UDP inviato!');
    }
    socket.close();
  });
});
```

Ecco un esempio di come ricevere pacchetti UDP broadcast in JavaScript:

```javascript
const dgram = require('dgram');
const socket = dgram.createSocket('udp4');
const port = 5000;

socket.on('listening', () => {
  const address = socket.address();
  console.log(`Socket in ascolto su ${address.address}:${address.port}`);
}); 

socket.on('message', (message, remote) => {
  console.log(`Messaggio ricevuto da ${remote.address}:${remote.port} - ${message}`);
});

// Ricezione dei messaggi broadcast sulla porta specificata
socket.bind(port);
```

Salva i due script in file separati (es. mittente.js e ricevitore.js).
Esegui prima il ricevitore e poi il mittente:
```bash
node ricevitore.js
node mittente.js
```

### **8. Differenze tra Broadcast e Multicast**
| Caratteristica       | Broadcast                          | Multicast                               |
|----------------------|------------------------------------|-----------------------------------------|
| **Destinatari**      | Tutti i dispositivi nella rete     | Solo i dispositivi iscritti al gruppo   |
| **Instradamento**    | Limitato alla rete locale          | Può attraversare router (se configurato)|
| **Efficienza**       | Bassa (tutti ricevono il pacchetto)| Alta (solo i destinatari interessati)   |
| **Utilizzo tipico**  | Scoperta di servizi locali         | Streaming video, videoconferenze        |



## **Multicast IPv4**

Il protocollo **multicast** rappresenta una modalità di comunicazione di rete che consente l’invio simultaneo di dati da un mittente a un gruppo selezionato di destinatari, ottimizzando l’utilizzo della banda rispetto alla trasmissione unicast. Gli indirizzi multicast IPv4 sono definiti nello spazio di indirizzamento **224.0.0.0/4** (da 224.0.0.0 a 239.255.255.255), riservato esclusivamente a questo scopo. La loro gestione è regolamentata da standard IETF, tra cui la **RFC 5771** (che sostituisce la precedente RFC 3171) e la **RFC 2365**, che ne definiscono l’allocazione e l’utilizzo.

### **1. Spazio di Indirizzamento Multicast IPv4**
Gli indirizzi multicast IPv4 occupano il range **224.0.0.0 – 239.255.255.255**, suddiviso in categorie funzionali:

- **224.0.0.0/24**: Indirizzi riservati per protocolli di routing e applicazioni di controllo (es. OSPF, RIP, IGMP).
- **224.0.1.0/24**: Indirizzi assegnabili su richiesta all’**IANA** per applicazioni specifiche.
- **239.0.0.0/8**: Indirizzi *administratively scoped*, destinati a reti private e non instradabili su Internet pubblico.

### **2. Indirizzi Multicast Riservati (RFC 5771)**
Gli indirizzi nel range **224.0.0.0/24** sono preassegnati a protocolli di rete critici. Di seguito una tabella riassuntiva:

Indirizzi Multicast IPv4 Riservati

| **Indirizzo**       | **Descrizione**                                      |
|---------------------|------------------------------------------------------|
| 224.0.0.0           | Riservato (non assegnato).                           |
| 224.0.0.1           | Tutti i sistemi multicast in una sottorete locale.   |
| 224.0.0.2           | Tutti i router multicast in una sottorete locale.    |
| 224.0.0.4           | Router DVMRP (Distance Vector Multicast Routing).    |
| 224.0.0.5           | Router OSPF (Open Shortest Path First).              |
| 224.0.0.6           | Router OSPF Designated.                              |
| 224.0.0.9           | Router RIPv2 (Routing Information Protocol).         |
| 224.0.0.10          | Router EIGRP (Enhanced Interior Gateway Routing).    |
| 224.0.0.12          | Server e relay agent DHCPv6.                         |
| 224.0.0.18          | VRRP (Virtual Router Redundancy Protocol).           |
| 224.0.0.22          | IGMPv3 (Internet Group Management Protocol).         |
| 224.0.0.251         | mDNS (Multicast DNS).                                |
| 224.0.0.252         | LLDP (Link Layer Discovery Protocol).                |


### **3. Indirizzi Assegnabili**
- **224.0.1.0/24**: Questo range è destinato a applicazioni multicast personalizzate. L’assegnazione avviene tramite richiesta all’**IANA**, che garantisce l’unicità globale dell’indirizzo. Gli indirizzi multicast **assegnati dall'IANA** (Internet Assigned Numbers Authority) per applicazioni specifiche sono standardizzati e riservati per garantire l'interoperabilità tra dispositivi e servizi in tutto il mondo. Questi indirizzi sono utilizzati in protocolli di rete, applicazioni multimediali, servizi di scoperta e altre funzionalità critiche.

Alcuni esempi noti includono:
- **224.0.1.1**: Utilizzato dal protocollo **NTP** (Network Time Protocol) per la sincronizzazione dell'ora tra dispositivi.
- **224.0.1.2**: Riservato per **SGI-Dogfight**, un'applicazione storica per la simulazione di volo.
- **224.0.1.3**: Utilizzato da **Rwhod**, un protocollo per il monitoraggio delle risorse di sistema.
- **224.0.1.4**: Riservato per **VNP** (Virtual Network Protocol).
- **224.0.1.68**: Utilizzato da **mDNS** (Multicast DNS) per la scoperta di servizi locali.
- **224.0.1.75**: Riservato per **PVL** (Packet Video Protocol).
- **224.0.1.129**: Utilizzato da **PTP** (Precision Time Protocol) per la sincronizzazione di alta precisione.

### **4. Indirizzi Administratively Scoped**
Il range **239.0.0.0/8** rappresenta un blocco di indirizzi multicast IPv4 riservato esclusivamente per uso locale all’interno di reti private, come reti aziendali, universitarie o domestiche. Questo range non è instradabile su Internet pubblico, il che garantisce che il traffico multicast rimanga confinato all’interno della rete locale, migliorando sicurezza e prestazioni.


#### **Caratteristiche principali**
Il range **239.0.0.0/8** comprende tutti gli indirizzi da **239.0.0.0** a **239.255.255.255**, per un totale di 16.777.216 indirizzi multicast. Questi indirizzi sono ideali per applicazioni interne che richiedono la distribuzione di dati a più destinatari all’interno di una stessa organizzazione, senza la necessità di richiedere indirizzi multicast globali all’IANA.

#### **Utilizzi tipici**
Questo range è comunemente impiegato per applicazioni come lo **streaming video interno**, le **videoconferenze**, i **sistemi di notifica aziendale** e i **giochi multiplayer locali**. Ad esempio, un’azienda potrebbe utilizzare l’indirizzo **239.1.1.1** per trasmettere un evento live ai dipendenti, mentre un’università potrebbe usare **239.255.0.1** per inviare notifiche di emergenza a tutti i dispositivi connessi alla rete locale.

#### **Configurazione di base**
Per utilizzare un indirizzo nel range **239.0.0.0/8**, è necessario configurare i dispositivi di rete, come router e switch, per supportare il multicast. Questo include l’abilitazione del protocollo **IGMP** per IPv4, che consente ai dispositivi di unirsi a gruppi multicast, e la configurazione di **PIM** se il traffico deve attraversare più sottoreti.

Inoltre, è importante impostare un **TTL (Time To Live) basso**, tipicamente 1, per garantire che i pacchetti non escano dalla rete locale.

#### **Esempio pratico in Node.js**
Ecco un esempio di come inviare un messaggio multicast a un indirizzo nel range **239.0.0.0/8** utilizzando Node.js:

```javascript
const dgram = require('dgram');
const socket = dgram.createSocket('udp4');

const MULTICAST_ADDRESS = '239.1.1.1'; // Indirizzo nel range 239.0.0.0/8
const PORT = 5000;

socket.bind(() => {
  socket.setMulticastTTL(1); // TTL limitato alla rete locale
  socket.addMembership(MULTICAST_ADDRESS); // Unione al gruppo multicast

  const message = "Messaggio multicast locale!";
  socket.send(message, PORT, MULTICAST_ADDRESS, () => {
    console.log(`Messaggio inviato a ${MULTICAST_ADDRESS}:${PORT}`);
    socket.close();
  });
});
```

#### **Considerazioni sulla sicurezza**
È fondamentale assicurarsi che il firewall non blocchi il traffico UDP sulla porta utilizzata per il multicast. Inoltre, poiché il traffico è confinato alla rete locale, non ci sono rischi di esposizione su Internet. Tuttavia, è buona pratica utilizzare indirizzi specifici per ogni applicazione, per evitare conflitti e garantire una gestione ordinata del traffico multicast.

### **5. Protocolli di Supporto**
Per il corretto funzionamento del multicast, sono necessari protocolli di gestione e routing:
- **IGMP (Internet Group Management Protocol)**: Utilizzato dagli host per segnalare l’adesione a un gruppo multicast. Le versioni **IGMPv2** e **IGMPv3** supportano funzionalità avanzate come la gestione delle sorgenti.
- **PIM (Protocol Independent Multicast)**: Protocollo di routing multicast che opera indipendentemente dal protocollo di routing unicast sottostante. Le varianti **PIM-DM** (Dense Mode) e **PIM-SM** (Sparse Mode) sono le più diffuse.
- **MSDP (Multicast Source Discovery Protocol)**: Utilizzato per lo scambio di informazioni sulle sorgenti multicast tra domini PIM-SM.

### **6. Applicazioni Pratiche**
Il multicast IPv4 trova applicazione in scenari dove è richiesta la distribuzione efficiente di dati a più destinatari:
- **Streaming multimediale**: Trasmissione di eventi live (es. IPTV, webinar).
- **Videoconferenza**: Distribuzione di flussi audio/video a gruppi di partecipanti.
- **Distribuzione di dati finanziari**: Aggiornamenti in tempo reale per applicazioni di trading.
- **Giochi online multiplayer**: Sincronizzazione dello stato di gioco tra più client.

### **7. Considerazioni di Sicurezza**
- **Controllo degli accessi**: Limitare l’adesione ai gruppi multicast tramite **IGMP snooping** sugli switch.
- **Filtraggio del traffico**: Utilizzare **ACL (Access Control List)** per bloccare indirizzi multicast non autorizzati.
- **Monitoraggio**: Strumenti come **MRT (Multicast Routing Monitor)** consentono di analizzare il traffico multicast in tempo reale.

### **8. Esempio di Codice (Javascript)**
Ecco un esempio di come inviare un pacchetto UDP multicast in JavaScript utilizzando il modulo `dgram` di Node.js:

```javascript
const dgram = require('dgram');
const socket = dgram.createSocket('udp4');

// Indirizzo multicast e porta
const MULTICAST_ADDRESS = '239.255.0.1'; // Indirizzo multicast valido
const PORT = 5000;

// Abilita il multicast e invia il messaggio
socket.bind(() => {
  socket.setMulticastTTL(128); // Imposta il TTL per il multicast
  socket.addMembership(MULTICAST_ADDRESS); // Unisci il gruppo multicast

  const message = "Ciao, questo è un messaggio UDP multicast!";
  socket.send(
    message,
    PORT,
    MULTICAST_ADDRESS,
    () => {
      console.log(`Messaggio multicast inviato a ${MULTICAST_ADDRESS}:${PORT}`);
      socket.close();
    }
  );
});
```

Ecco un esempio di come ricevere pacchetti UDP multicast in JavaScript:

```javascript
const dgram = require('dgram');
// Crea un socket UDP con l'opzione reuseAddr per permettere più socket sulla stessa porta
const socket = dgram.createSocket({ type: 'udp4', reuseAddr: true });

// Indirizzo multicast e porta
const MULTICAST_ADDRESS = '239.255.0.1';
const PORT = 5000;

socket.on('listening', () => {
  socket.addMembership(MULTICAST_ADDRESS);
  const address = socket.address();
  console.log(`Socket in ascolto su ${address.address}:${address.port}`);
});

socket.on('message', (message, remote) => {
  console.log(`Messaggio ricevuto da ${remote.address}:${remote.port} - ${message}`);
});

// Ricezione dei messaggi multicast sulla porta specificata
socket.bind(PORT);
```

## Configurazione e Performance

### TTL (Time To Live)
```java
// Controlla quanti router può attraversare un pacchetto multicast
MulticastSocket socket = new MulticastSocket();

// TTL = 1: Solo rete locale
socket.setTimeToLive(1);

// TTL = 32: Raggiunge altre subnet nella stessa organizzazione  
socket.setTimeToLive(32);

// TTL = 255: Massima propagazione (uso raramente consigliato)
socket.setTimeToLive(255);
```

### Interfacce di Rete Multiple
```java
// Specifica interfaccia per multicast
NetworkInterface networkInterface = NetworkInterface.getByName("eth0");
MulticastSocket socket = new MulticastSocket();
socket.setNetworkInterface(networkInterface);

// Oppure per indirizzo specifico
InetAddress localAddress = InetAddress.getByName("192.168.1.100");
socket.setInterface(localAddress);
```

### Controllo Loop-back
```java
// Disabilita ricezione dei propri pacchetti multicast
MulticastSocket socket = new MulticastSocket();
socket.setLoopbackMode(true); // true = disabilita loopback
```

## Sicurezza e Considerazioni

### Validazione Sorgente
```java
// Sempre validare mittenti nei sistemi broadcast/multicast
public boolean isValidSender(InetAddress sender) {
    // Verifica che sia nella tua subnet
    if (!isInLocalSubnet(sender)) {
        System.err.println("⚠️ Pacchetto da subnet non autorizzata: " + sender);
        return false;
    }
    
    // Altre verifiche di sicurezza...
    return true;
}
```

### Rate Limiting
```java
// Previeni flood di pacchetti broadcast/multicast
public class RateLimiter {
    private final Map<InetAddress, Long> lastMessageTime = new ConcurrentHashMap<>();
    private final long minimumInterval; // ms
    
    public RateLimiter(long minimumIntervalMs) {
        this.minimumInterval = minimumIntervalMs;
    }
    
    public boolean allowMessage(InetAddress sender) {
        long now = System.currentTimeMillis();
        Long lastTime = lastMessageTime.get(sender);
        
        if (lastTime == null || (now - lastTime) >= minimumInterval) {
            lastMessageTime.put(sender, now);
            return true;
        }
        
        return false; // Rate limit exceeded
    }
}
```

## Best Practices

### ✅ Raccomandazioni
1. **Usa TTL appropriato** per limitare propagazione
2. **Implementa timeout** per discovery operations
3. **Valida sempre mittenti** per sicurezza
4. **Limita rate** per prevenire flooding
5. **Gestisci interfacce multiple** correttamente

### ❌ Errori Comuni
1. **Non abilitare broadcast** con `setBroadcast(true)`
2. **TTL troppo alto** che causa traffico eccessivo
3. **Non gestire loop-back** nei test locali
4. **Assumere consegna garantita** anche per broadcast
5. **Non filtrare mittenti** in reti non sicure


---
[🏠 Torna al Modulo](../README.md) | [⬅️ Lezione Precedente](01-Socket-UDP-Fondamenti.md) | [➡️ Prossima Lezione](03-UDP-Performance.md)