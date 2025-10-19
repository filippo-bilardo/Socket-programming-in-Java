# Nmap e Wireshark da Riga di Comando - Guida Completa

## Introduzione

Quando stai sviluppando applicazioni che comunicano via socket, ci sono momenti in cui i comandi di base come netstat e lsof non sono sufficienti. Devi sapere quali host sono raggiungibili sulla tua rete, quali porte stanno rispondendo, e come il traffico effettivo fluisce bit per bit attraverso la rete. È qui che strumenti più sofisticati come Nmap e Wireshark diventano indispensabili. Nmap ti permette di eseguire una ricognizione completa della rete, scoprendo host, porte aperte e servizi in esecuzione. Wireshark, da riga di comando tramite TShark, fornisce un'analisi profonda del traffico di rete permettendoti di vedere esattamente cosa sta avvenendo a livello di pacchetto. In questa guida, imparerai come usare entrambi gli strumenti per diagnosticare e comprendere profondamente i tuoi problemi di connessione.

---

## Capitolo 1: Nmap - Network Mapper

### 1.1 Concetti Fondamentali di Nmap

Nmap è uno strumento straordinariamente potente che fa qualcosa di concettualmente semplice: invia pacchetti a un host o a una gamma di host e osserva come rispondono. Basandosi su come rispondono, Nmap deduce quali porte stanno ascoltando, quale versione di quale servizio è in esecuzione, e persino quale sistema operativo sta usando l'host. La ragione per cui è così prezioso nella sviluppo di applicazioni socket è che ti permette di vedere l'intera immagine della tua rete infrastruttura: non solo le tue applicazioni, ma anche tutti i server, firewall e dispositivi di rete che le circondano.

Quando esegui una scansione Nmap, di solito fornisci un target (un indirizzo IP, un nome di host, o un intervallo di indirizzi) e Nmap ti restituisce informazioni sul target. Le informazioni predefinite includono quali porte sono aperte, quali potrebbero essere filtrate da un firewall, e quali sono completamente chiuse. Se lo desideri, Nmap può andare molto più in profondità, effettuando rilevamento del sistema operativo, versioning del servizio, persino script personalizzati per verificare vulnerabilità specifiche.

### 1.2 Scansioni Base - Scoprire Quali Porte Sono Aperte

La forma più semplice di scansione Nmap è chiedere semplicemente: "Quali porte su questo host stanno ascoltando?" Per fare questo, esegui:

```bash
# Scansione di base di localhost
nmap localhost

# Output tipico:
# Starting Nmap 7.92 ( https://nmap.org )
# Nmap scan report for localhost (127.0.0.1)
# Host is up (0.00018s latency).
# 
# PORT      STATE SERVICE
# 22/tcp    open  ssh
# 80/tcp    open  http
# 443/tcp   open  https
# 5000/tcp  open  upnp
# 3001/tcp  open  nessus
```

Quello che stai vedendo è una lista di porte comuni su cui Nmap ha provato a connettersi. Quando ottiene una risposta positiva, mostra lo stato come "open", indicando che c'è un servizio in ascolto su quella porta. Questo è essenziale quando stai sviluppando un'applicazione server e vuoi verificare che effettivamente sta ascoltando dove pensi che stia ascoltando.

Per impostazione predefinita, Nmap scansiona solo le 1000 porte più comuni, il che è veloce ma non completo. Se hai un'applicazione in ascolto su una porta meno comune, Nmap potrebbe non rilevarla. Per fare una scansione di tutte le 65535 porte, usi il flag `-p-`:

```bash
# Scansiona tutte le porte (attenzione: più lento!)
nmap -p- localhost

# Scansiona un intervallo specifico di porte
nmap -p 5000-6000 localhost

# Scansiona porte specifiche
nmap -p 22,80,443,5000,3001 localhost
```

### 1.3 Tipi di Scansione - SYN vs Connect

Nmap supporta diversi tipi di scansione, ognuno con caratteristiche diverse. Il tipo di scansione predefinito è `-sS` (scansione SYN), che è efficiente e spesso non genera log (motivo per cui è popolare negli ambienti di penetration testing). Una scansione SYN funziona inviando pacchetti SYN al target e osservando le risposte: se riceve SYN-ACK, sa che la porta è aperta; se riceve RST (reset), sa che è chiusa.

```bash
# Scansione SYN (predefinita, richiede privilegi root su Linux)
nmap -sS localhost

# Scansione Connect (funziona anche senza privilegi)
nmap -sT localhost

# Scansione UDP (scopre porte UDP)
nmap -sU localhost

# Scansione PING (verifica solo se l'host è attivo)
nmap -sn 192.168.1.0/24
```

La scansione Connect (`-sT`) è utile quando non hai privilegi root, perché usa la chiamata di sistema connect() standard invece di craft dei pacchetti raw. La scansione UDP (`-sU`) è importante se il tuo servizio usa UDP anziché TCP.

### 1.4 Scansione di Reti Intere - Scoprire Tutti gli Host Attivi

Uno dei compiti più comuni nella manutenzione di un'infrastruttura è scoprire quali host sono attivi su una rete. Se il tuo data center ha una sottorete 192.168.1.0/24 (che significa 256 indirizzi IP da 192.168.1.0 a 192.168.1.255), puoi usare Nmap per scoprire rapidamente quali di questi host sono attivi:

```bash
# Scansiona una sottorete intera per host attivi
nmap -sn 192.168.1.0/24

# Output tipico:
# Nmap scan report for 192.168.1.1
# Host is up (0.0012s latency).
# Nmap scan report for 192.168.1.50
# Host is up (0.0034s latency).
# Nmap scan report for 192.168.1.100
# Host is up (0.0028s latency).
#
# Nmap done: 256 IP addresses (3 hosts up) scanned in 5.32 seconds

# Scansiona e salva in un file per analisi successiva
nmap -sn 192.168.1.0/24 -oN network_hosts.txt

# Scansiona in formato greppable (facile da parsare)
nmap -sn 192.168.1.0/24 -oG network_hosts.grep

# Estrai solo gli indirizzi IP attivi
nmap -sn 192.168.1.0/24 -oG - | grep "Status: Up" | awk '{print $2}'
```

Questo tipo di scansione è cruciale quando stai configurando un ambiente di test e hai bisogno di sapere quali server sono effettivamente online prima di tentare di connettervi.

### 1.5 Rilevamento di Versione e Servizio

Una caratteristica potente di Nmap è il rilevamento della versione, che ti permette di capire non solo che una porta è aperta, ma anche quale servizio specifico sta rispondendo e persino quale versione di quel servizio. Questo è utile quando hai bisogno di sapere se un server sta eseguendo una versione di Apache vulnerabile o se il tuo servizio Java sta rispondendo correttamente ai probe di versione.

```bash
# Rilevamento di versione dei servizi
nmap -sV localhost

# Output tipico:
# PORT      STATE SERVICE    VERSION
# 22/tcp    open  ssh        OpenSSH 7.4 (protocol 2.0)
# 80/tcp    open  http       Apache httpd 2.4.6
# 5000/tcp  open  upnp       Custom Java Service 1.0

# Rilevamento del sistema operativo (richiede privilegi)
nmap -O localhost

# Combinare rilevamento di versione e SO
nmap -A localhost

# Scansione aggressiva con timing predefinito
nmap -T4 -A localhost
```

Il flag `-A` è una scorciatoia che abilita il rilevamento della versione, il rilevamento del sistema operativo, il traccia di rotte, e gli script. È molto più lento ma fornisce informazioni molto più ricche. Il flag `-T4` imposta il timing su "aggressive", il che significa che Nmap invierà più pacchetti simultaneamente e sarà più veloce, ma userà più larghezza di banda.

### 1.6 Scripting con Nmap-Script Engine (NSE)

Una caratteristica avanzata di Nmap è la capacità di eseguire script personalizzati tramite il Nmap Scripting Engine. Ci sono centinaia di script disponibili che possono fare cose come enumerare gli utenti SMB, trovare vulnerabilità note, o testare configurazioni specifiche del servizio. Quando stai sviluppando un'applicazione di rete, gli script NSE possono aiutarti a verificare che il tuo servizio stia respondendo correttamente.

```bash
# Esegui tutti gli script di default
nmap -sC localhost

# Esegui uno script specifico
nmap --script http-methods localhost

# Esegui un set di script
nmap --script "http-*" localhost

# Esegui script e mostra l'output dettagliato
nmap --script ssl-cert -p 443 localhost

# Elenca tutti gli script disponibili
nmap --script-help

# Ricerca script per parola chiave
nmap --script-help | grep -i vuln
```

Gli script NSE sono particolarmente utili quando stai verificando che il tuo servizio sia sicuro. Ad esempio, se stai eseguendo HTTPS, puoi verificare il certificato SSL, la versione del protocollo supportata, e le cipher suite utilizzate, il tutto automaticamente da Nmap.

### 1.7 Output di Nmap - Formati e Parsing

Nmap può salvare i risultati in vari formati, il che è utile quando vuoi automatizzare il parsing dei risultati. I tre principali formati di output sono il formato normale (human-readable), il formato XML (strutturato), e il formato greppable (facile da parsare).

```bash
# Output normale (schermo)
nmap localhost

# Salva in formato normale
nmap localhost -oN scan.nmap

# Salva in formato XML (utile per parsing automatico)
nmap localhost -oX scan.xml

# Salva in formato greppable
nmap localhost -oG scan.grep

# Salva in tutti i formati
nmap localhost -oA scan

# Leggi il risultato da un file precedente
nmap -p 80,443 --script http-title -oX - < /dev/null > /dev/null
```

Il formato XML è particolarmente utile perché puoi parsarlo facilmente con qualsiasi linguaggio di programmazione. Ad esempio, in un script di automazione, potresti eseguire Nmap, salvare l'output in XML, e poi processarlo per estrarre i servizi specifici che ti interessano.

### 1.8 Scenario Pratico: Scoprire Conflitti di Porta

Immagina che stai cercando di avviare il tuo servizio Java sulla porta 5000, ma ricevi "Address already in use". Vuoi scoprire quale host è usando quella porta e quale versione di quale servizio sta eseguendo:

```bash
# Scansiona il tuo host per scoprire cosa sta usando la porta 5000
nmap -p 5000 -sV localhost

# Se il primo tentativo non mostra il servizio, prova una scansione più aggressiva
nmap -p 5000 -sV -A -T4 localhost

# Se vuoi sapere tutto quello che sta ascoltando sul tuo host
nmap -p- -sV localhost

# Se sospetti che un firewall stia filtrando, prova diversi tipi di scansione
nmap -sS localhost  # SYN scan
nmap -sT localhost  # Connect scan
nmap -sA localhost  # ACK scan (per rilevare firewall)
```

---

## Capitolo 2: TShark - Wireshark da Riga di Comando

### 2.1 Cosa è TShark e Perché Usarlo

TShark è la versione da riga di comando di Wireshark, il noto strumento di analisi del traffico di rete. Mentre Wireshark ha un'interfaccia grafica incredibilmente potente, TShark permette di catturare e analizzare il traffico di rete da uno script o da uno shell, il che lo rende perfetto per l'automazione e il debugging remoto. TShark fa quello che tcpdump fa, ma con più opzioni di parsing e filtraggio incorporate. Dove tcpdump mostra principalmente i raw packet, TShark può interpretare automaticamente i protocolli e mostrare le informazioni in un formato più leggibile.

Quando stai sviluppando un'applicazione socket e qualcosa non funziona, TShark ti permette di vedere esattamente quali pacchetti vengono inviati, in quale ordine, e con quale contenuto. Questo è il livello di visibilità più profondo possibile nella comunicazione di rete.

### 2.2 Installazione e Verificazione

Prima di usare TShark, devi verificare che sia installato sul tuo sistema. Poiché TShark è la versione CLI di Wireshark, se hai Wireshark installato, probabilmente hai anche TShark:

```bash
# Verifica se TShark è installato
which tshark

# Verifica la versione
tshark -v

# Se non è installato, installalo:
# Su Ubuntu/Debian:
sudo apt-get install tshark

# Su macOS:
brew install wireshark

# Su Windows (con Chocolatey):
choco install wireshark
```

### 2.3 Cattura Base di Traffico

La forma più semplice di usare TShark è semplicemente catturare tutto il traffico su un'interfaccia di rete. Questo è utile quando vuoi vedere tutto quello che sta accadendo sulla tua rete:

```bash
# Cattura il traffico sull'interfaccia predefinita
tshark

# Cattura sull'interfaccia specifica (es. eth0)
tshark -i eth0

# Cattura e ferma dopo 10 pacchetti
tshark -c 10

# Cattura per 30 secondi e poi fermati
tshark -a duration:30

# Elenca le interfacce disponibili
tshark -D

# Output tipico:
# 1. eth0
# 2. lo
# 3. docker0
# 4. veth1234567
```

Quando esegui TShark senza filtri, vedrai pacchetti da TUTTO il traffico sulla tua interfaccia. Questo può essere sopraffatto peramente. Generalmente, vorrai filtrare per vedere solo il traffico di cui ti importa.

### 2.4 Filtraggio - Focalizzarsi sul Traffico Rilevante

Il vero potere di TShark sta nella sua capacità di filtrare il traffico usando criteri complessi. I filtri in TShark sono espressi in un linguaggio specifico che ti permette di selezionare pacchetti basati su una varietà di criteri.

```bash
# Filtra per traffico sulla porta 5000 (TCP e UDP)
tshark -i any -f "port 5000"

# Filtra solo per TCP sulla porta 5000
tshark -i any -f "tcp port 5000"

# Filtra per traffico tra due host
tshark -i any -f "host 192.168.1.100"

# Filtra per traffico da o verso un host
tshark -i any -f "src host 192.168.1.100"
tshark -i any -f "dst host 192.168.1.100"

# Filtra per un intervallo di porte
tshark -i any -f "portrange 5000-5100"

# Combina filtri con AND/OR
tshark -i any -f "tcp and port 5000"
tshark -i any -f "tcp port 5000 or tcp port 8080"

# Filtra escludendo il traffico specifico
tshark -i any -f "tcp port 5000 and not dst 127.0.0.1"
```

Nota che i filtri nel flag `-f` sono filtri di cattura, che vengono applicati a livello di kernel prima che i pacchetti vengano nemmeno catturati. Questo è molto efficiente perché riduce la mole di dati che deve passare da kernel a userspace.

### 2.5 Visualizzazione e Analisi dei Pacchetti

Una volta catturati i pacchetti, TShark può mostrarteli in vari formati. Per impostazione predefinita, TShark mostra una riga per pacchetto con un summary delle informazioni principali. Se vuoi analizzare i dettagli di un pacchetto specifico, puoi chiedere a TShark di mostrarti la dissecazione completa:

```bash
# Cattura e mostra un summary su una riga per pacchetto
tshark -i any -f "tcp port 5000" -c 10

# Output tipico:
# 1    0.000000 127.0.0.1 → 127.0.0.1 TCP 54 54321 → 5000 [SYN] Seq=0 Win=65535
# 2    0.000012 127.0.0.1 → 127.0.0.1 TCP 54 5000 → 54321 [SYN, ACK] Seq=0 Ack=1
# 3    0.000025 127.0.0.1 → 127.0.0.1 TCP 54 54321 → 5000 [ACK] Seq=1 Ack=1

# Mostra la dissecazione completa di un pacchetto
tshark -i any -f "tcp port 5000" -V

# Output mostra:
# Frame 1: 66 bytes on wire (528 bits), 66 bytes captured (528 bits)
# Ethernet II, Src: 00:00:00:00:00:00, Dst: 00:00:00:00:00:00
# Internet Protocol Version 4, Src: 127.0.0.1, Dst: 127.0.0.1
# Transmission Control Protocol, Src Port: 54321, Dst Port: 5000, Seq: 0, Ack: 0
```

### 2.6 Mostrare il Payload dei Pacchetti

Se vuoi vedere il contenuto effettivo dei dati che vengono trasmessi, non solo gli header, puoi chiedere a TShark di mostrare il payload in formato hex e ASCII:

```bash
# Mostra il payload in formato hex e ASCII
tshark -i any -f "tcp port 5000" -x

# Mostra solo il payload senza gli header
tshark -i any -f "tcp port 5000" -O "Colinfo" -e "data"

# Mostra il payload dei pacchetti HTTP in formato ASCII
tshark -i any -f "tcp port 80" -Y "http" -x
```

Questo è particolarmente utile quando stai debuggando il protocollo della tua applicazione. Se il tuo servizio Java dovrebbe inviare "OK\n" dopo aver ricevuto una connessione, puoi usare TShark per verificare che stai effettivamente inviando esattamente quel contenuto.

### 2.7 Salvataggio e Riesamina dei Catturamenti

Proprio come con tcpdump, puoi salvare i pacchetti catturati in un file per analisi successiva. TShark salva nel formato pcap, che è uno standard di industria e può essere aperto con Wireshark, tcpdump, e molti altri strumenti.

```bash
# Cattura e salva in un file pcap
tshark -i any -f "tcp port 5000" -w capture.pcap

# Leggi un file pcap precedentemente salvato
tshark -r capture.pcap

# Analizza un file pcap con dissecazione completa
tshark -r capture.pcap -V

# Filtra un file pcap per mostrare solo i pacchetti che ti interessano
tshark -r capture.pcap -Y "http.request"

# Estrai il payload di un pacchetto da un file catturato
tshark -r capture.pcap -x
```

### 2.8 Filtri Display avanzati

Oltre ai filtri di cattura (che operano a livello di kernel), TShark supporta anche filtri di visualizzazione, che operano sui pacchetti già catturati. I filtri di visualizzazione sono espressi con il flag `-Y`:

```bash
# Filtra i pacchetti per mostrare solo quelli con il flag TCP SYN
tshark -i any -f "tcp port 5000" -Y "tcp.flags.syn==1"

# Mostra solo i pacchetti con una dimensione di payload maggiore di 100 byte
tshark -i any -Y "tcp.len > 100"

# Mostra i pacchetti che contengono una stringa specifica
tshark -i any -f "tcp port 5000" -Y "data contains \"Hello\""

# Filtra i pacchetti in base al protocollo di applicazione
tshark -i any -Y "http"
tshark -i any -Y "dns"
tshark -i any -Y "ssh"
```

### 2.9 Estrazione di Campi Specifici

Quando vuoi automatizzare l'analisi del traffico, spesso hai bisogno di estrarre campi specifici da ogni pacchetto. TShark permette di selezionare quali campi visualizzare usando il flag `-e`:

```bash
# Estrai solo gli indirizzi IP sorgente e destinazione
tshark -i any -f "tcp port 5000" -e "ip.src" -e "ip.dst" -e "tcp.port"

# Estrai timestamp, sorgente, destinazione e lunghezza del payload
tshark -i any -f "tcp port 5000" -e "frame.time" -e "ip.src" -e "ip.dst" -e "tcp.len"

# Output CSV per facilità di parsing
tshark -i any -f "tcp port 5000" -e "ip.src" -e "ip.dst" -e "tcp.sport" -e "tcp.dport" -T fields -E separator=","

# Converte il CSV in un file che puoi importare in Excel
tshark -r capture.pcap -e "ip.src" -e "ip.dst" -e "tcp.sport" -e "tcp.dport" -T fields -E separator="," > analysis.csv
```

Questo output CSV è particolarmente utile quando hai catturato una grande quantità di traffico e vuoi analizzarlo con Excel o uno strumento di data analysis.

### 2.10 Scenario Pratico: Debuggare la Comunicazione Socket

Immagina che il tuo client Java dovrebbe connettersi al server sulla porta 5000, inviare una richiesta JSON, ricevere una risposta, e poi disconnettersi. Ma qualcosa non funziona. Ecco come useresti TShark per debuggare:

```bash
# Passo 1: Cattura il traffico sulla porta 5000
tshark -i any -f "tcp port 5000" -w debug.pcap

# (In un altro terminale, esegui il tuo client Java)

# Passo 2: Analizza la cattura
tshark -r debug.pcap

# Vedrai il three-way handshake, gli scambi di dati, e la chiusura della connessione

# Passo 3: Guarda i dati effettivi inviati/ricevuti
tshark -r debug.pcap -x

# Passo 4: Filtra per mostrare solo i pacchetti con payload
tshark -r debug.pcap -Y "tcp.len > 0" -x

# Passo 5: Se il tuo protocollo è HTTP, analizza come HTTP
tshark -r debug.pcap -Y "http" -V
```

---

## Capitolo 3: Combinazione di Nmap e TShark

### 3.1 Workflow Completo di Troubleshooting

In un scenario reale di troubleshooting, spesso usi Nmap e TShark insieme. Nmap ti aiuta a scoprire quali servizi stanno ascoltando, e TShark ti aiuta a capire il traffico effettivo di quei servizi. Ecco un workflow tipico:

```bash
# Passo 1: Scopri quali porte stanno ascoltando sul tuo host
nmap -sV localhost

# Output potrebbe mostrare:
# 5000/tcp open  upnp    Custom Java Service 1.0
# 8080/tcp open  http    Apache httpd 2.4.6

# Passo 2: Inizia a catturare il traffico sulla porta di interesse
tshark -i any -f "tcp port 5000" -w capture.pcap

# Passo 3: In un altro terminale, attiva il tuo client e genera traffico
java MyClient

# Passo 4: Analizza la cattura per vedere cosa è effettivamente stato trasmesso
tshark -r capture.pcap -V

# Passo 5: Se il traffico sembra corretto, ma il servizio non funziona,
# prova una scansione Nmap più aggressiva per scoprire dettagli di versione
nmap -sV -A -p 5000 localhost
```

### 3.2 Scenario: Il Servizio Dice che Sta Ascoltando, ma Non Puoi Connetterti

Questo è uno scenario classico: il tuo programma Java stampa "Server listening on port 5000" ma quando provi a connetterti, la connessione viene rifiutata. Ecco come diagnosticare:

```bash
# Passo 1: Verifica che il processo sia effettivamente in esecuzione
ps aux | grep java

# Passo 2: Verifica che Nmap veda la porta aperta
nmap -p 5000 localhost

# Se Nmap NON vede la porta aperta, ma il processo è in esecuzione,
# allora il processo non sta effettivamente ascoltando

# Passo 3: Verifica con netstat
netstat -tlnp | grep 5000

# Se non vedi nulla, il process non sta ascoltando

# Passo 4: Se il processo dice che sta ascoltando, ma netstat/nmap non lo vedono,
# cattura il traffico per vedere cosa succede quando tenti di connettersi
tshark -i any -f "tcp port 5000" -V

# (In un altro terminale, tenta di connettersi)
nc -zv localhost 5000

# Se vedi pacchetti SYN ma nessun SYN-ACK di ritorno,
# significa che il kernel non sta instradando i pacchetti al processo,
# suggerendo un problema di binding (ad es. il processo sta ascoltando su 127.0.0.1 invece che su 0.0.0.0)
```

---

## Capitolo 4: Tecniche Avanzate

### 4.1 Monitoraggio Continuo con Nmap

Se stai gestendo un servizio e vuoi verificare continuamente che le porte di interesse rimangono aperte, puoi creare uno script di monitoraggio:

```bash
#!/bin/bash
# monitor_ports.sh - Monitora le porte con Nmap

PORTS="22,80,443,5000,8080"
HOST="localhost"
CHECK_INTERVAL=60

while true; do
  echo "=== $(date) ==="
  
  # Esegui scansione Nmap
  nmap -p $PORTS $HOST 2>/dev/null | grep -E "PORT|tcp"
  
  # Se una porta si chiude, generi un allarme
  open_count=$(nmap -p $PORTS $HOST 2>/dev/null | grep -c "open")
  echo "Porte aperte: $open_count / $(echo $PORTS | tr ',' '\n' | wc -l)"
  
  sleep $CHECK_INTERVAL
done
```

### 4.2 Analisi di TShark con Statistiche

TShark può anche fornire statistiche aggregate su vari aspetti del traffico:

```bash
# Conta il numero di pacchetti per host
tshark -r capture.pcap -q -z io,stat,0,"COUNT(frame)frame.src_dst"

# Mostra i protocolli di livello superiore utilizzati
tshark -r capture.pcap -q -z io,prot,0

# Analizza le porte TCP utilizzate
tshark -r capture.pcap -q -z endpoints,tcp
```

### 4.3 Correlazione tra Nmap e TShark

Un approccio sofisticato è usare Nmap per generare una lista di host interessanti, e poi usare TShark per monitorare il traffico verso quegli host:

```bash
#!/bin/bash
# discover_and_monitor.sh - Scopri host con Nmap, monitora con TShark

NETWORK="192.168.1.0/24"

# Scopri tutti gli host attivi
echo "Scoprendo host attivi..."
hosts=$(nmap -sn $NETWORK -oG - 2>/dev/null | grep "Status: Up" | awk '{print $2}')

echo "Host trovati: $hosts"

# Crea un filtro TShark per monitorare il traffico verso questi host
for host in $hosts; do
  echo "host $host or" >> /tmp/tshark_filter.txt
done

# Rimuovi l'ultimo "or"
sed -i '$ s/ or$//' /tmp/tshark_filter.txt

# Monitora il traffico verso questi host
tshark -i any -f "$(cat /tmp/tshark_filter.txt)" -V
```

---

## Capitolo 5: Domande di Autovalutazione

### Domanda 1
Quale comando Nmap scopre quali porte sono aperte su localhost?

A) nmap -sU localhost  
B) nmap -sP localhost  
C) nmap localhost  
D) nmap -sV localhost  

**Risposta corretta: C**

Il comando di base `nmap localhost` effettua una scansione delle porte più comuni e mostra quali sono aperte. `-sU` è per UDP, `-sP` è per PING, e `-sV` aggiunge il rilevamento della versione, quindi C è la risposta più