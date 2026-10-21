# Esercizio d: Nmap e Wireshark

## Obiettivo

Usa **Nmap** per verificare quali porte rispondono e **Wireshark** per osservare
come si apre una connessione TCP, quali dati scambia e come si chiude.
Riutilizzerai `ServerMonitor.java` dell'[esercizio c](03-Monitorare_i_socket_dalla_CLI.md).

Al termine dovrai collegare tre punti di vista:

| Strumento | Domanda a cui risponde |
|---|---|
| `ss` / `Get-NetTCPConnection` | Quali socket vede il sistema operativo e quali processi li possiedono? |
| Nmap | Quali porte risultano aperte o chiuse dal punto di osservazione della scansione? |
| Wireshark / TShark | Quali pacchetti e dati sono stati catturati durante la comunicazione? |

La guida di riferimento è [Nmap e TShark](../05-Nmap-e-TShark.md).
I comandi sono confrontati per **PowerShell sul PC Windows** e **Bash sul server
Linux Web4Student**. Le prove usano soltanto `127.0.0.1` e le due porte del
laboratorio: `5000` e `5001`.

## Prerequisiti e organizzazione

Completa l'esercizio c e prepara quattro terminali sul sistema scelto:

| Terminale | Attività |
|---|---|
| A | Server Java |
| B | Client che invia messaggi |
| C | Comandi Nmap e osservazione dei socket |
| D | Cattura con TShark; su Windows puoi usare invece la finestra di Wireshark |

Per Web4Student apri quattro finestre del terminale del PC ed esegui in ciascuna:

```powershell
ssh -p 2222 username@w4s.filippobilardo.it
```

Sostituisci `username` con il tuo account. Dopo l'accesso segui la colonna
**Bash**: server, client, scansione e cattura devono trovarsi sullo stesso server
remoto. Wireshark sul PC non vede il loopback di Web4Student: analizzerai sul PC
il file catturato sul server e scaricato via SCP.

### Strumenti e account senza privilegi amministrativi

Nmap, Wireshark e TShark devono essere già disponibili nell'ambiente del laboratorio.
Su Windows la cattura loopback richiede **Npcap** configurato per il tuo account;
su Linux la cattura richiede i permessi predisposti per `dumpcap`.
La preparazione dei driver e dei permessi compete al docente o al tecnico.

La scansione **TCP connect `-sT`** usa le normali connessioni TCP e permette di
lavorare senza pacchetti raw. I permessi necessari alla cattura sono distinti.
Se non puoi catturare, svolgi le prove Nmap e analizza il file della stessa
attività fornito dal docente, indicando questa modalità nella relazione.
Aprire un file di cattura già disponibile non richiede i permessi di cattura.
Riferimenti: [TCP connect scan](https://nmap.org/book/scan-methods-connect-scan.html)
e [cattura loopback con Wireshark](https://wiki.wireshark.org/CaptureSetup/Loopback).

## Passo 1 — Preparare la cartella dell'esercizio

In A crea la cartella, copia il sorgente dell'esercizio c e compilalo:

| Operazione | PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|---|
| Creare la cartella | `New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\socket\es04-nmap-wireshark"` | `mkdir -p ~/socket/es04-nmap-wireshark` |
| Entrare nella cartella | `Set-Location "$env:USERPROFILE\socket\es04-nmap-wireshark"` | `cd ~/socket/es04-nmap-wireshark` |
| Copiare il sorgente | `Copy-Item ..\es03-monitoraggio-cli\ServerMonitor.java .` | `cp ../es03-monitoraggio-cli/ServerMonitor.java .` |
| Compilare | `javac -encoding UTF-8 ServerMonitor.java` | `javac -encoding UTF-8 ServerMonitor.java` |

Entra nella cartella dell'esercizio anche in B, C e D. Il server verrà avviato
al passo 4: per ora deve essere spento.

Su Windows, se i programmi non sono nel Path, definisci i percorsi seguenti,
adattandoli all'installazione reale del laboratorio:

```powershell
# Nel terminale C
$nmap = 'C:\Program Files (x86)\Nmap\nmap.exe'

# Nel terminale D, se usi TShark
$tshark = 'C:\Program Files\Wireshark\tshark.exe'
```

Il simbolo `&` nei comandi successivi esegue il programma indicato dalla variabile.
Se Nmap è già nel Path, puoi impostare `$nmap = 'nmap'`; analogamente puoi usare
`$tshark = 'tshark'` se TShark è nel Path.

## Passo 2 — Verificare strumenti e porte

| Operazione | PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|---|
| Versione Nmap, in C | `& $nmap --version` | `nmap --version` |
| Versione TShark, in D | `& $tshark --version` | `tshark --version` |
| Interfacce di cattura, in D | `& $tshark -D` | `tshark -D` |
| Ascolti sulle due porte, in C | `Get-NetTCPConnection -State Listen \| Where-Object { $_.LocalPort -in 5000,5001 }` | `ss -ltnp '( sport = :5000 or sport = :5001 )'` |

Entrambe le porte devono essere libere all'inizio. Se sono usate da altri
programmi, scegli due porte disponibili e sostituiscile in **tutti** i comandi,
nei filtri e nella relazione. Non interrompere processi di altri utenti.

Se una versione o l'elenco delle interfacce restituisce un errore, annotalo:
non equivale all'assenza di traffico. La cattura sul loopback Linux usa normalmente
`lo`; su Windows cerca **Adapter for loopback traffic capture**, fornito da Npcap.
Il suo numero nell'elenco non è uguale su tutti i PC.

## Passo 3 — Scansionare prima dell'avvio del server

Nel terminale C:

| PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|
| `& $nmap --unprivileged -sT -Pn -n -p 5000,5001 --reason -oN 01-prima.nmap 127.0.0.1` | `nmap --unprivileged -sT -Pn -n -p 5000,5001 --reason -oN 01-prima.nmap 127.0.0.1` |

Le opzioni hanno questi compiti:

| Opzione | Significato |
|---|---|
| `--unprivileged` | Non presumere di avere privilegi per operazioni raw |
| `-sT` | Esegui una scansione TCP connect |
| `-Pn` | Salta la fase di scoperta dell'host; non dipendere dal ping |
| `-n` | Evita la risoluzione DNS |
| `-p 5000,5001` | Prova soltanto le due porte indicate |
| `--reason` | Mostra la ragione dello stato rilevato |
| `-oN` | Salva anche un rapporto testuale nel file indicato |

Con le porte libere sul loopback, normalmente entrambe risultano **closed**.
Se l'output riassume le porte chiuse, leggi anche la riga `Not shown`.
`Filtered` indica invece che Nmap non riesce a stabilire se la porta sia aperta:
non è un sinonimo di `closed`. Riporta sempre il risultato reale.
Vedi gli [stati delle porte Nmap](https://nmap.org/book/man-port-scanning-basics.html).

## Passo 4 — Avviare il server e ripetere la scansione

In A:

| PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|
| `java -cp . ServerMonitor 5000` | `java -cp . ServerMonitor 5000` |

In C confronta ascolto locale e risultato Nmap:

| Operazione | PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|---|
| Verificare l'ascolto | `Get-NetTCPConnection -State Listen \| Where-Object { $_.LocalPort -eq 5000 }` | `ss -ltnp 'sport = :5000'` |
| Scansionare e salvare | `& $nmap --unprivileged -sT -Pn -n -p 5000,5001 --reason -oN 02-attivo.nmap 127.0.0.1` | `nmap --unprivileged -sT -Pn -n -p 5000,5001 --reason -oN 02-attivo.nmap 127.0.0.1` |

**Risultato atteso:** `5000/tcp open`, mentre `5001/tcp` rimane chiusa.
Il server può registrare una connessione senza messaggi o una disconnessione
con errore: Nmap apre TCP, ma non svolge il dialogo `ciao` / `Echo: ciao`.

La colonna **SERVICE** può riportare un nome come `upnp`, ricavato dalla porta:
non dimostra che il tuo server Java implementi quel protocollo. In questa prova
non usiamo il rilevamento di versione `-sV`.
Riferimento: [nomi dei servizi e rilevamento delle versioni](https://nmap.org/book/man-version-detection.html).

## Passo 5 — Avviare la cattura prima di generare traffico

Scegli **una** delle modalità seguenti. Il server A deve rimanere attivo e non
deve esserci un client persistente già collegato.

### Modalità Windows: interfaccia grafica Wireshark

1. Apri Wireshark e seleziona **Adapter for loopback traffic capture**.
2. In **Capture → Options** imposta come filtro di cattura:

   ```text
   tcp port 5000 or tcp port 5001
   ```

3. Avvia la cattura prima di proseguire al passo 6.
4. Dopo le prove del passo 7, ferma la cattura e usa **File → Save As** per salvare
   `sessione.pcapng` nella cartella dell'esercizio.

### Modalità CLI: TShark su Windows o Web4Student

Usa il terminale D. Su Windows sostituisci **NUMERO_LOOPBACK** con il numero reale
mostrato da `& $tshark -D` per l'interfaccia loopback; su Web4Student usa `lo`:

| PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|
| `& $tshark -i NUMERO_LOOPBACK -f 'tcp port 5000 or tcp port 5001' -F pcapng -w sessione.pcapng` | `tshark -i lo -f 'tcp port 5000 or tcp port 5001' -F pcapng -w sessione.pcapng` |

Attendi la conferma di avvio della cattura. Il terminale rimarrà occupato:
continua le prove negli altri terminali. Ferma con Ctrl+C solo dopo il passo 7.
Non cambiare i permessi di sistema se la cattura viene rifiutata: usa la modalità
con file del docente descritta nei prerequisiti.

Il filtro **di cattura** usa la sintassi `tcp port 5000`. Un filtro **di
visualizzazione** usa invece `tcp.port == 5000`: le due sintassi non sono
intercambiabili. TShark usa rispettivamente `-f` e `-Y`.
Riferimento: [manuale TShark](https://www.wireshark.org/docs/man-pages/tshark.html).

## Passo 6 — Catturare una scansione e una conversazione

Prima, in C, ripeti la scansione e salva un nuovo rapporto:

| PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|
| `& $nmap --unprivileged -sT -Pn -n -p 5000,5001 --reason -oN 03-durante-cattura.nmap 127.0.0.1` | `nmap --unprivileged -sT -Pn -n -p 5000,5001 --reason -oN 03-durante-cattura.nmap 127.0.0.1` |

La cattura conterrà tentativi sulle porte aperta e chiusa: serviranno a
confrontarli con il dialogo applicativo.

Poi apri un client persistente nel terminale B.

**PowerShell — Windows locale:**

```powershell
$client = [System.Net.Sockets.TcpClient]::new('127.0.0.1', 5000)
$flusso = $client.GetStream()
$flusso.ReadTimeout = 5000
$codifica = [System.Text.UTF8Encoding]::new($false)
$lettore = [System.IO.StreamReader]::new($flusso, $codifica)
$scrittore = [System.IO.StreamWriter]::new($flusso, $codifica)
$scrittore.AutoFlush = $true
$scrittore.WriteLine('ciao')
$lettore.ReadLine()
$scrittore.WriteLine('prova wireshark')
$lettore.ReadLine()
```

**Bash — Linux Web4Student:**

```bash
nc 127.0.0.1 5000
```

Dentro `nc`, digita `ciao`, premi Invio, poi digita `prova wireshark` e premi
ancora Invio. Queste righe sono messaggi del protocollo, non comandi Bash.

In entrambi i casi devi ricevere `Echo: ciao` ed `Echo: prova wireshark`.
Poi chiudi ordinatamente la conversazione:

| Operazione in B | PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|---|
| Inviare la richiesta di chiusura | `$scrittore.WriteLine('fine')` | Dentro `nc`, digita `fine` e premi Invio |
| Leggere il saluto | `$lettore.ReadLine()` | Leggi `Arrivederci` nel terminale |
| Chiudere il client | `$scrittore.Dispose()` poi `$lettore.Dispose()` poi `$client.Dispose()` | Se `nc` resta aperto dopo il saluto, premi Ctrl+C |

## Passo 7 — Arrestare il server e verificare la porta chiusa

Con la cattura ancora attiva, premi Ctrl+C nel terminale A. In C esegui:

| PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|
| `& $nmap --unprivileged -sT -Pn -n -p 5000,5001 --reason -oN 04-spento.nmap 127.0.0.1` | `nmap --unprivileged -sT -Pn -n -p 5000,5001 --reason -oN 04-spento.nmap 127.0.0.1` |

La porta `5000` deve tornare chiusa. Controlla il risultato reale: un altro
processo potrebbe avere occupato la porta nel frattempo.

Ora ferma la cattura in Wireshark oppure premi Ctrl+C in D e verifica che
`sessione.pcapng` esista e contenga pacchetti. Non confondere il file binario di
cattura con i rapporti testuali `.nmap`.

## Passo 8 — Scaricare la cattura da Web4Student

Se hai lavorato su Windows, il file è già sul PC e puoi passare al passo 9.
Se hai catturato su Web4Student, apri una **nuova PowerShell locale**, fuori dalle
sessioni SSH, e scarica il file nella cartella Download:

```powershell
Set-Location "$env:USERPROFILE\Downloads"
scp -P 2222 username@w4s.filippobilardo.it:socket/es04-nmap-wireshark/sessione.pcapng .
```

Il percorso remoto relativo parte dalla home dello studente. Nota la **`-P`
maiuscola** di `scp`, mentre `ssh` usa `-p`. Apri il file scaricato con Wireshark
tramite **File → Open**. Leggerlo non richiede accesso alle interfacce remote.

## Passo 9 — Analizzare handshake, dati e chiusura in Wireshark

Apri `sessione.pcapng` e applica, uno per volta, questi **filtri di visualizzazione**:

| Obiettivo | Filtro Wireshark |
|---|---|
| Tutto il traffico sulla porta del server | `tcp.port == 5000` |
| Richieste iniziali di apertura | `tcp.port == 5000 && tcp.flags.syn == 1 && tcp.flags.ack == 0` |
| Risposte SYN-ACK | `tcp.port == 5000 && tcp.flags.syn == 1 && tcp.flags.ack == 1` |
| Pacchetti con dati applicativi | `tcp.port == 5000 && tcp.len > 0` |
| Chiusura con FIN | `tcp.port == 5000 && tcp.flags.fin == 1` |
| Reset sulle due porte | `(tcp.port == 5000 \|\| tcp.port == 5001) && tcp.flags.reset == 1` |

Seleziona un pacchetto con il messaggio `ciao`, poi scegli
**Analyze → Follow → TCP Stream**. Se necessario passa al formato ASCII o UTF-8.
Devi riconoscere le richieste e le risposte della conversazione, compreso `fine`
e `Arrivederci`. Annota l'indice reale **N** del flusso e usa `tcp.stream == N`
per isolarlo: la scansione Nmap ha creato altre connessioni, quindi non presumere
che la conversazione sia il flusso numero 0.
Riferimento: [Follow TCP Stream](https://www.wireshark.org/docs/wsug_html_chunked/ChAdvFollowStreamSection.html).

Per quel flusso individua SYN, SYN-ACK e ACK, le porte client/server e la fase di
chiusura. I flag di chiusura e dati possono essere combinati negli stessi
segmenti: non imporre un numero fisso di pacchetti. Un messaggio può occupare
più segmenti e un segmento può contenere più messaggi.

Confronta infine un tentativo verso una porta chiusa con la conversazione:
normalmente vedrai SYN seguito da RST/ACK nel primo caso. Anche una scansione
connect su una porta aperta può terminare con un reset: un RST isolato non basta
per concludere che quella porta fosse chiusa.

## Passo 10 — Ripetere l'analisi dalla CLI con TShark

Usa D sul sistema dove si trova il file; su Windows definisci `$tshark` come
nel passo 1 se hai aperto un nuovo terminale. Confronta i comandi:

| Operazione | PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|---|
| Riepilogo del file | `& $tshark -r sessione.pcapng` | `tshark -r sessione.pcapng` |
| Pacchetti con dati | `& $tshark -r sessione.pcapng -Y 'tcp.port == 5000 && tcp.len > 0' -x` | `tshark -r sessione.pcapng -Y 'tcp.port == 5000 && tcp.len > 0' -x` |
| Campi essenziali | `& $tshark -r sessione.pcapng -Y 'tcp.port == 5000' -T fields -e frame.number -e tcp.stream -e ip.src -e tcp.srcport -e ip.dst -e tcp.dstport -e tcp.flags -e tcp.len` | `tshark -r sessione.pcapng -Y 'tcp.port == 5000' -T fields -e frame.number -e tcp.stream -e ip.src -e tcp.srcport -e ip.dst -e tcp.dstport -e tcp.flags -e tcp.len` |
| Ricostruire il flusso scelto | `& $tshark -r sessione.pcapng -q -z 'follow,tcp,ascii,N'` | `tshark -r sessione.pcapng -q -z 'follow,tcp,ascii,N'` |

Sostituisci **N** con l'indice reale annotato al passo 9, per esempio
`'follow,tcp,ascii,2'`. `-T fields` abilita l'estrazione dei campi richiesti da
`-e`; `-x` mostra i byte dei pacchetti, mentre Follow ricostruisce il flusso.

## Consegna e autovalutazione

Nella cartella dell'esercizio prepara `relazione.md` con:

1. Ambiente usato, versioni degli strumenti, porte scelte e interfaccia di cattura.
2. I rapporti Nmap e una tabella degli stati prima dell'avvio, durante l'ascolto
   e dopo l'arresto del server.
3. Una schermata del three-way handshake con porte ed endpoint evidenziati.
4. Una schermata di Follow TCP Stream con richieste e risposte, e il suo indice.
5. Un tentativo verso una porta chiusa e una spiegazione dei flag osservati.
6. La differenza tra filtro di cattura, filtro di visualizzazione e scansione Nmap.
7. La spiegazione del perché SERVICE non certifica il protocollo del tuo server
   e del perché un RST non significa sempre porta chiusa.

Consegna anche i quattro file `.nmap` e `sessione.pcapng`, contenente soltanto
il traffico delle due porte di questa prova. Se usi la cattura del docente,
indicalo e separa le osservazioni sul file dalle prove Nmap eseguite da te.

Nel repository **Socket-Programming** registra relazione e rapporti Nmap. Allega
la cattura alla consegna secondo le indicazioni del docente; evita di versionare
catture ripetute o di grandi dimensioni. Su Web4Student:

```bash
cd ~/socket
git add -- es04-nmap-wireshark/relazione.md es04-nmap-wireshark/*.nmap
git diff --cached
git commit -m "Completa l'esercizio su Nmap e Wireshark"
git push
```

Gli stessi comandi Git funzionano in PowerShell nella tua copia del repository;
usa `Set-Location "$env:USERPROFILE\socket"` per raggiungerla.

---

[Esercizio precedente: monitorare i socket dalla CLI](03-Monitorare_i_socket_dalla_CLI.md) · [Guida: Nmap e TShark](../05-Nmap-e-TShark.md) · [Indice del capitolo](../README.md)
