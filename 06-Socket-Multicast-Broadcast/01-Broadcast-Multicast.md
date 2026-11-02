# Broadcast e multicast IPv4

Prerequisiti: [UDP](../05-Socket-UDP/README.md), indirizzi IPv4 e prefissi di rete. Broadcast e multicast cambiano i destinatari del datagramma; non aggiungono affidabilità, ordinamento o autenticazione a UDP.

## Unicast, broadcast e multicast

| Modello | Destinatari | Invio | Ricezione |
| --- | --- | --- | --- |
| Unicast | Un endpoint | IP e porta del destinatario | Socket associato alla porta |
| Broadcast IPv4 | Host nel dominio di broadcast che accettano il traffico | Indirizzo di broadcast e porta | Processo in ascolto sulla porta, se rete e firewall lo consentono |
| Multicast IP | Membri di un gruppo | Gruppo multicast e porta | Join del gruppo su un'interfaccia e bind sulla porta |

I router normalmente non inoltrano il broadcast limitato. Anche il broadcast diretto a una subnet può essere filtrato: conoscere l'indirizzo non garantisce l'attraversamento di un router. Non tutti i dispositivi rispondono a un messaggio applicativo arbitrario.

## Broadcast

`255.255.255.255` è il broadcast limitato alla rete locale. Il broadcast di una subnet si ottiene impostando a 1 i bit host del prefisso: per `192.168.10.20/25` è `192.168.10.127`, non sempre `.255`. I prefissi /31 e /32 non vengono trattati come reti broadcast nell'esempio di discovery.

In Java abilita `socket.setBroadcast(true)`. Il ricevitore resta un DatagramSocket associato alla porta; non esiste join per il broadcast. In Node.js usa `setBroadcast(true)` dopo il bind: non impostare le opzioni di un socket che non è ancora pronto.

Gli [esempi broadcast](esempi/README.md) inviano `BROADCAST|Messaggio #N|Timestamp: T`. Java e Node.js usano righe di testo all'interno del datagramma, codificate UTF-8; il confine del messaggio è il datagramma stesso, senza necessità di newline. Un ricevitore per host e porta evita di confondere la distribuzione dei datagrammi unicast fra socket locali che condividono una porta con la consegna multicast a più membri.

Per una diagnosi iniziale puoi impostare come destinazione 127.0.0.1, poi passare al vero broadcast della tua LAN. Il primo caso è una prova unicast dello stesso codice, non una prova di propagazione broadcast.

## Multicast e indirizzi

Gli indirizzi multicast IPv4 sono nell'intervallo 224.0.0.0–239.255.255.255. Alcuni intervalli e indirizzi sono riservati a protocolli specifici: non scegliere un indirizzo casuale in 224.0.0.0/24 o un gruppo di servizio noto per un esperimento didattico.

Per il laboratorio usiamo `239.255.0.1`, nello spazio local scope di 239.255.0.0/16. Gli annunci di discovery usano `239.255.0.2`. Lo spazio amministrativamente limitato complessivo è 239.0.0.0/8; i confini effettivi dipendono dalla configurazione dei router, secondo la [RFC 2365](https://www.rfc-editor.org/rfc/rfc2365.html).

Il gruppo identifica i destinatari a livello IP, la porta identifica il servizio UDP. Due programmi che usano la stessa porta ma gruppi differenti non sono una coppia coerente. Su alcuni sistemi più gruppi sulla stessa porta possono avere comportamenti di consegna dipendenti dall'OS: per servizi distinti usa anche porte distinte e verifica la cattura effettiva.

## Interfaccia, join e leave

La scelta dell'interfaccia è essenziale se il PC ha Ethernet, Wi-Fi, VPN o adattatori virtuali. Un indirizzo multicast non è l'indirizzo dell'interfaccia locale: sono parametri diversi.

| Passo | Java | Node.js |
| --- | --- | --- |
| Prepara più ricevitori locali | reuseAddress prima del bind | `dgram.createSocket({type: 'udp4', reuseAddr: true})` |
| Seleziona interfaccia | `setNetworkInterface(ni)` | `setMulticastInterface(ipLocale)` per l'invio |
| Unisciti al gruppo | `joinGroup(new InetSocketAddress(group, port), ni)` | `addMembership(group, ipLocale)` dopo il bind |
| Lascia il gruppo | `leaveGroup(groupAddress, ni)` | `dropMembership(group, ipLocale)` oppure close del socket |
| Imposta i salti massimi | `setTimeToLive(ttl)` | `setMulticastTTL(ttl)` |
| Abilita la consegna locale | `IP_MULTICAST_LOOP = true` | `setMulticastLoopback(true)` |

Il mittente non deve fare join per inviare. Usare MulticastSocket facilita la configurazione di interfaccia e TTL; un DatagramSocket può comunque spedire a un gruppo multicast con la configurazione opportuna.

Il numero TTL è un limite di hop, non una distanza geografica: 32 non significa “stessa organizzazione”, 64 non significa “stessa regione”. Con TTL 1 i datagrammi non vengono inoltrati oltre il segmento IP locale; TTL 0 serve a prove sul solo host dove supportate. Aumentare il TTL non crea il routing multicast né supera filtri o confini amministrativi.

Il loopback multicast consente di osservare sullo stesso host i datagrammi spediti dal mittente. Non significa che il gruppo sia 127.0.0.1. Una VPN o l'assenza di una vera interfaccia multicast può rendere necessaria una prova su un altro collegamento.

## Selezionare l'interfaccia nel laboratorio

Scegli l'indirizzo IPv4 locale dell'adattatore attivo, ottenuto da `ip -4 addr` su Linux o `Get-NetIPAddress -AddressFamily IPv4` su PowerShell. Nei programmi Java MulticastSupport seleziona automaticamente un'interfaccia IPv4 attiva che supporta multicast; puoi forzarla con la stessa variabile usata dai programmi Node.js.

PowerShell, sostituendo l'indirizzo con quello del PC:

```powershell
$env:MULTICAST_IFACE = "192.168.1.20"
```

Linux:

```sh
export MULTICAST_IFACE=192.168.1.20
```

Entrambi i terminali devono indicare la stessa interfaccia se sender e receiver sono sullo stesso PC. Nei test fra host ciascuno usa il proprio indirizzo locale. Node.js, se la variabile non è impostata, usa la scelta dell'OS: su un PC con più adattatori è preferibile specificarla. Non usare gli IP di esempio alla lettera.

Per sole prove sullo stesso host, se la scheda di rete è filtrata, puoi impostare esplicitamente MULTICAST_IFACE a 127.0.0.1 dove l'OS consente multicast sull'interfaccia di loopback. Le utility Java accettano questa scelta esplicita ma non la selezionano automaticamente. Non è una prova sulla LAN. Analogamente, il broadcast 127.255.255.255 è limitato al loopback dove supportato e non sostituisce il broadcast reale della subnet.

## Riutilizzare un DatagramPacket

Il ricevitore ripristina `packet.setLength(buffer.length)` prima di ogni receive. Senza questo passaggio, dopo un datagramma di 1 byte quello successivo può essere limitato a 1 byte. UTF-8 richiede di inviare `data.length`, non il numero di caratteri. I programmi testuali accettano al massimo 1024 byte e usano un buffer con un byte aggiuntivo per rifiutare i messaggi oltre il limite.

## Prove

1. Avvia due ricevitori multicast prima del mittente e verifica che entrambi osservino lo stesso payload.
2. Invia in successione “a”, un messaggio lungo con accenti e “bye”; controlla che il secondo non sia troncato.
3. Incrocia mittente Java e ricevitore Node.js, poi l'inverso.
4. Ripeti fra due PC della stessa LAN e confronta con il test sul solo host.
5. Cambia gruppo o porta su un lato e osserva l'assenza di messaggi, senza interpretarla subito come un guasto hardware.
6. Confronta broadcast e multicast in Wireshark: `udp.port == 5000`; per il gruppo, `ip.dst == 239.255.0.1`.

UDP può perdere datagrammi anche in multicast. Non è corretto scrivere “tutti riceveranno certamente il messaggio” dopo una send riuscita. Il servizio applicativo deve decidere se tollerare perdite, mantenere una cache o aggiungere identificativi e recupero selettivo.

Riferimenti: [MulticastSocket](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/MulticastSocket.html), [Node dgram](https://nodejs.org/api/dgram.html), [RFC 1112](https://www.rfc-editor.org/rfc/rfc1112.html), [RFC 2365](https://www.rfc-editor.org/rfc/rfc2365.html).

[Indice modulo](README.md) · [Discovery](02-Discovery.md) · [Esercizi](esercizi/README.md)
