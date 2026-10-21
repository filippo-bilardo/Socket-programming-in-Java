# Esercizio c: monitorare un server Java dalla CLI

## Obiettivo

Osserva un server Java prima, durante e dopo una connessione TCP. Dovrai
riconoscere il socket di ascolto, i due endpoint di una connessione locale,
la porta effimera del client e il processo proprietario della porta del server.
Provoca infine un conflitto di porta e spiegalo usando i dati osservati.

La guida di riferimento è [Monitorare i socket dalla CLI](../04-Monitorare-i-Socket-dalla-CLI).
Puoi svolgere l'attività su **Windows con PowerShell** oppure sul server
**Linux Web4Student con Bash**, con il normale account studente. I comandi
dei due ambienti sono messi a confronto nei passaggi dell'esercizio.

## Prerequisiti e organizzazione

Completa l'[esercizio 02 su Java e VS Code](02-Java_e_VSCode_senza_amministratore.md).
Verifica che `java` e `javac` siano disponibili nel terminale in cui lavori.

Apri **tre terminali sullo stesso computer**:

| Terminale | Compito |
|---|---|
| A | Eseguire il server Java |
| B | Collegare il client e inviare messaggi |
| C | Osservare socket e processi; provare un secondo avvio del server |

### Aprire le sessioni sul server Web4Student

Per il percorso Linux apri tre finestre del terminale sul PC. In **ciascuna**,
da PowerShell, collegati al server sostituendo `username` con il tuo account:

```powershell
ssh -p 2222 username@w4s.filippobilardo.it
```

Dopo l'accesso i comandi vengono eseguiti da **Bash sul server Linux**. Usa le
tre sessioni SSH come A, B e C e segui la colonna Web4Student. Il client loopback
va eseguito nella sessione SSH B: `127.0.0.1` deve indicare lo stesso server
remoto in tutte e tre le sessioni.

### Preparare la cartella e verificare gli strumenti

| Operazione | PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|---|
| Creare la cartella in A | `New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\socket\es03-monitoraggio-cli"` | `mkdir -p ~/socket/es03-monitoraggio-cli` |
| Entrare nella cartella, in A, B e C | `Set-Location "$env:USERPROFILE\socket\es03-monitoraggio-cli"` | `cd ~/socket/es03-monitoraggio-cli` |
| Controllare il percorso | `Get-Location` | `pwd` |
| Verificare Java | `java -version` | `java -version` |
| Verificare il compilatore | `javac -version` | `javac -version` |
| Verificare lo strumento di osservazione | `Get-Command Get-NetTCPConnection` | `command -v ss` |
| Verificare il client TCP | Il client .NET è integrato in PowerShell | `command -v nc` |

Su Windows apri la cartella in VS Code. Se devi applicare la configurazione
del Path della sessione spiegata nell'esercizio 02, ripetila nei terminali
che eseguono Java. Su Web4Student usa il JDK disponibile sul server: per il
codice seguente serve Java 11 o successivo. Se mancano `javac`, `ss` o `nc`,
segnalalo al docente senza tentare installazioni di sistema.

Useremo **TCP, IPv4, indirizzo `127.0.0.1`, porta `5000`**. Se un altro utente
usa già questa porta, scegline una libera, ad esempio `5001`, e sostituiscila
in tutti i comandi e nei filtri. Non interrompere processi di altri utenti.

## Passo 1 — Fotografare la situazione iniziale

Nel terminale **C**, usa il comando del tuo ambiente:

| PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|
| `Get-NetTCPConnection \| Where-Object { $_.LocalPort -eq 5000 -or $_.RemotePort -eq 5000 } \| Select-Object LocalAddress, LocalPort, RemoteAddress, RemotePort, State, OwningProcess \| Format-Table -AutoSize` | `ss -tanp '( sport = :5000 or dport = :5000 )'` |

La versione PowerShell su più righe, equivalente alla tabella, è:

```powershell
Get-NetTCPConnection |
    Where-Object { $_.LocalPort -eq 5000 -or $_.RemotePort -eq 5000 } |
    Select-Object LocalAddress, LocalPort, RemoteAddress, RemotePort, State, OwningProcess |
    Format-Table -AutoSize
```

Controlla in particolare che non ci sia già un ascolto sulla porta scelta.
Eventuali record `TimeWait` di prove precedenti non sono ascolti attivi.

Se lavori su macOS, usa come alternativa `lsof -nP -iTCP:5000`.

Annota quale comando hai usato e l'esito. Un errore di accesso o un comando
mancante non equivalgono all'assenza di socket.

## Passo 2 — Preparare il server Java

Crea **`ServerMonitor.java`** nella cartella dell'esercizio:

| PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|
| Crea il file `ServerMonitor.java` in VS Code | In A esegui `nano ServerMonitor.java`; incolla il codice, salva con Ctrl+O e Invio, poi esci con Ctrl+X |

```java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServerMonitor {
    public static void main(String[] args) throws IOException {
        int porta = args.length == 0 ? 5000 : Integer.parseInt(args[0]);
        InetAddress indirizzo = InetAddress.getByName("127.0.0.1");

        try (ServerSocket server = new ServerSocket(porta, 50, indirizzo)) {
            System.out.println("PID server: " + ProcessHandle.current().pid());
            System.out.println("In ascolto su 127.0.0.1:" + porta);

            while (true) {
                try (Socket client = server.accept();
                     BufferedReader input = new BufferedReader(new InputStreamReader(
                             client.getInputStream(), StandardCharsets.UTF_8));
                     PrintWriter output = new PrintWriter(
                             client.getOutputStream(), true, StandardCharsets.UTF_8)) {
                    System.out.println("Client: " + client.getRemoteSocketAddress());
                    String riga;
                    while ((riga = input.readLine()) != null) {
                        System.out.println("Ricevuto: " + riga);
                        if (riga.equals("fine")) {
                            output.println("Arrivederci");
                            break;
                        }
                        output.println("Echo: " + riga);
                    }
                    System.out.println("Chiusura connessione client");
                } catch (IOException errore) {
                    System.out.println("Errore client: " + errore.getMessage());
                }
            }
        } catch (BindException errore) {
            System.err.println("BindException: impossibile usare 127.0.0.1:" + porta);
            System.err.println(errore.getMessage());
        }
    }
}
```

Questo server risponde a **righe di testo**, non a richieste HTTP. Il comando
`fine` chiude soltanto la connessione del client; il server continua ad ascoltare.
Se il client chiude senza inviare righe, il server torna ad aspettare un client.

Il programma gestisce un client per volta. Il `println` con autoflush invia
subito la risposta e `try-with-resources` chiude le risorse della connessione.
I log applicativi aiutano a collegare i comandi del sistema operativo al codice.

Nel terminale **A** compila e avvia; i comandi Java sono uguali:

| Operazione | PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|---|
| Compilare | `javac -encoding UTF-8 ServerMonitor.java` | `javac -encoding UTF-8 ServerMonitor.java` |
| Avviare | `java -cp . ServerMonitor 5000` | `java -cp . ServerMonitor 5000` |

**Verifica:** il terminale stampa il PID e l'indirizzo di ascolto, poi rimane
occupato. Annota il PID reale: sarà diverso da quello degli esempi della guida.

## Passo 3 — Riconoscere LISTEN e il processo proprietario

In C ripeti il comando del passo 1. Devi vedere un socket in ascolto con porta
locale `5000` e indirizzo `127.0.0.1`.

Nel terminale **C** confronta il PID con quello stampato dal server:

| Operazione | PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|---|
| Trovare l'ascolto | `Get-NetTCPConnection \| Where-Object { $_.LocalPort -eq 5000 -and $_.State -eq 'Listen' }` | `ss -ltnp 'sport = :5000'` |
| Cercare il processo Java da confrontare col PID | `Get-Process -Name java` per i processi Java visibili | `ps -u "$USER" -o pid,comm,args` e individua `ServerMonitor` |
| Ispezionare il PID, qui di esempio 8420 | `Get-Process -Id 8420` | `ps -p 8420 -o pid,user,comm,args` |

Sostituisci `8420` con il PID reale. L'output di `ss -p` può contenere una
sezione come `users:(("java",pid=8420,fd=4))`.

Su Windows puoi ricavare il processo direttamente dall'ascolto:

```powershell
$ascolti = @(Get-NetTCPConnection |
    Where-Object { $_.LocalPort -eq 5000 -and $_.State -eq 'Listen' })
$ascolti | Select-Object LocalAddress, LocalPort, State, OwningProcess
$ascolti | Select-Object -ExpandProperty OwningProcess -Unique |
    ForEach-Object { Get-Process -Id $_ } |
    Select-Object Id, ProcessName
```

Su macOS usa `lsof -nP -iTCP:5000 -sTCP:LISTEN`.

**Annota:** nome del processo, PID, indirizzo e porta. Spiega perché il solo
socket di ascolto non rappresenta una connessione con un client.

## Passo 4 — Aprire una connessione e mantenerla visibile

Nel terminale **B**, i comandi a confronto sono:

| Operazione | PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|---|
| Collegarsi | Blocco `TcpClient` riportato sotto | `nc 127.0.0.1 5000` |
| Inviare una riga | `$scrittore.WriteLine('ciao')` | Dentro `nc`, digita `ciao` e premi Invio |
| Leggere la risposta | `$lettore.ReadLine()` | La risposta viene visualizzata da `nc` |
| Osservare gli endpoint in C | `Get-NetTCPConnection -State Established \| Where-Object { $_.LocalPort -eq 5000 -or $_.RemotePort -eq 5000 }` | `ss -tnp state established '( sport = :5000 or dport = :5000 )'` |

### Windows: client PowerShell

Nel terminale B esegui il blocco. Gli oggetti rimangono disponibili nella
sessione: non chiudere il terminale dopo averlo eseguito.

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

$client.Client.LocalEndPoint
$client.Client.RemoteEndPoint
```

La risposta deve essere `Echo: ciao`. Il server deve stampare la riga ricevuta.
La codifica UTF-8 è condivisa dai due programmi e non aggiunge un BOM al messaggio.

### Linux Web4Student: client netcat

Se `nc` è già disponibile, nel terminale B:

```bash
nc 127.0.0.1 5000
```

Digita `ciao` e premi Invio. Devi ricevere `Echo: ciao`. Lascia aperta la sessione.
`ciao` è un messaggio da scrivere **dentro `nc`**, non un comando Bash. Se `nc`
manca sul laboratorio remoto, chiedi al docente quale client è disponibile.
Lo stesso comando può essere usato su macOS.

### Osservazione nel terminale C

Ripeti il comando del passo 1. Con client e server sullo stesso sistema,
normalmente vedrai:

- Una riga `LISTEN` / `Listen` del server.
- Una riga `ESTABLISHED` / `Established` del lato server.
- Una riga `ESTABLISHED` / `Established` del lato client.

Riempi questa tabella con i valori reali:

| Lato | Indirizzo e porta locali | Indirizzo e porta remoti | Stato | PID |
|---|---|---|---|---|
| Ascolto server | | | | |
| Connessione lato server | | | | |
| Connessione lato client | | | | |

Confronta la porta effimera del client con il log stampato da Java. Spiega perché
le due righe `ESTABLISHED` descrivono una sola connessione TCP.

## Passo 5 — Distinguere il test TCP dalla risposta del programma

Lascia aperto il client B e verifica che i socket restino `ESTABLISHED` anche
quando non stai scrivendo messaggi. Lo stato non implica un flusso continuo di dati.

| Operazione nel terminale B | PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|---|
| Inviare un'altra riga | `$scrittore.WriteLine('seconda prova')` | Dentro `nc`, digita `seconda prova` e premi Invio |
| Leggere la risposta | `$lettore.ReadLine()` | Leggi la risposta visualizzata da `nc` |

La risposta attesa è `Echo: seconda prova`.

**Annota:** la risposta applicativa prova qualcosa in più rispetto allo stato
`ESTABLISHED`? Perché `curl http://127.0.0.1:5000/` non è il test giusto per
questo protocollo?

## Passo 6 — Provocare un conflitto di porta

Con il server A ancora attivo, nel terminale **C** esegui:

| Operazione | PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|---|
| Avviare una seconda istanza | `java -cp . ServerMonitor 5000` | `java -cp . ServerMonitor 5000` |
| Ricontrollare chi possiede l'ascolto | `Get-NetTCPConnection \| Where-Object { $_.LocalPort -eq 5000 -and $_.State -eq 'Listen' }` | `ss -ltnp 'sport = :5000'` |

La seconda istanza deve segnalare `BindException`: indirizzo e porta richiesti
sono già usati dalla prima istanza.

Ripeti il comando di osservazione e verifica che il PID in ascolto rimanga quello
di A. Risolvi il conflitto scegliendo la porta **5001** per la seconda istanza:

| PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|
| `java -cp . ServerMonitor 5001` | `java -cp . ServerMonitor 5001` |

Dopo aver verificato la stampa di avvio, termina **questa seconda istanza**
con Ctrl+C nel terminale C. Il server originale sulla porta `5000` resta attivo.
Se `5001` è già occupata, scegline un'altra libera e annotala.

**Spiega:** perché chiudere il client B non libererebbe il socket di ascolto
posseduto dal server A?

## Passo 7 — Chiudere la connessione e osservare gli stati residui

Nel terminale **B**:

| Operazione | PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|---|
| Richiedere la chiusura | `$scrittore.WriteLine('fine')` | Dentro `nc`, digita `fine` e premi Invio |
| Leggere il saluto e chiudere il client | `$lettore.ReadLine()`, poi i tre `Dispose()` del blocco seguente | Attendi `Arrivederci`; se `nc` resta aperto, premi Ctrl+C |

Sequenza completa PowerShell:

```powershell
$scrittore.WriteLine('fine')
$lettore.ReadLine()
$scrittore.Dispose()
$lettore.Dispose()
$client.Dispose()
```

Con `nc`, digita `fine` e premi Invio; se il terminale non torna al prompt dopo
la risposta, interrompi il client con Ctrl+C.

La risposta è `Arrivederci`. In C ripeti subito il comando del passo 1.
Le due righe `ESTABLISHED` devono sparire; l'ascolto del server deve rimanere.
Può comparire `TIME_WAIT` / `TimeWait`, anche senza un PID applicativo valido.
Il lato su cui compare e gli stati intermedi visibili dipendono dall'ordine
e dalla rapidità della chiusura. Non è obbligatorio riuscire a fotografarli tutti.
Su macOS `lsof` può non mostrare i record rimasti dopo la chiusura dei processi:
per cercare `TIME_WAIT` confronta anche `netstat -an -p tcp`, leggendo le colonne
degli indirizzi e delle porte.

Nel terminale **C** confronta gli stati e, senza un client persistente collegato,
prova ad aprire una nuova connessione:

| Operazione | PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|---|
| Mostrare tutti gli stati | `Get-NetTCPConnection \| Where-Object { $_.LocalPort -eq 5000 -or $_.RemotePort -eq 5000 }` | `ss -tanp '( sport = :5000 or dport = :5000 )'` |
| Cercare TIME_WAIT | `Get-NetTCPConnection \| Where-Object { $_.State -eq 'TimeWait' -and ($_.LocalPort -eq 5000 -or $_.RemotePort -eq 5000) }` | `ss -tan state time-wait '( sport = :5000 or dport = :5000 )'` |
| Testare TCP | `Test-NetConnection -ComputerName 127.0.0.1 -Port 5000` | `nc -zv -w 3 127.0.0.1 5000` |

Su Windows leggi `TcpTestSucceeded`; su Linux/macOS leggi l'esito di `nc`.
Il test dovrebbe riuscire. Java può registrare un nuovo client senza stampare
`Ricevuto`: il test apre e chiude TCP senza inviare una riga del nostro protocollo.
La connessione può essere troppo breve per comparire nel monitor.

## Passo 8 — Arrestare il server e confrontare i risultati

In A premi Ctrl+C. Ripeti il comando di osservazione in C: l'ascolto sulla
porta `5000` deve scomparire, pur potendo restare record di chiusura.

| Operazione nel terminale C | PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|---|
| Cercare l'ascolto dopo l'arresto | `Get-NetTCPConnection \| Where-Object { $_.LocalPort -eq 5000 -and $_.State -eq 'Listen' }` | `ss -ltnp 'sport = :5000'` |
| Ripetere il test TCP | `Test-NetConnection -ComputerName 127.0.0.1 -Port 5000` | `nc -zv -w 3 127.0.0.1 5000` |

Il test ora deve fallire, a meno che un altro processo
abbia nel frattempo occupato la porta. In quel caso controlla il nuovo PID.

Riassumi le osservazioni:

| Fase | LISTEN presente? | ESTABLISHED presenti? | Altri stati osservati | Esito test TCP, se eseguito |
|---|---|---|---|---|
| Prima dell'avvio | | | | |
| Server senza client | | | | |
| Client collegato | | | | |
| Client chiuso, server attivo | | | | |
| Server arrestato | | | | |

## Estensione facoltativa — Monitoraggio ripetuto

Usa lo script PowerShell o Bash della [guida](../04-Monitorare-i-Socket-dalla-CLI)
nel terminale C e ripeti apertura, scambio di righe e chiusura del client.
Controlla che il filtro includa soltanto la porta scelta.

| PowerShell — Windows locale | Bash — Linux Web4Student |
|---|---|
| Ciclo `while ($true)` del paragrafo 7 della guida | In C: `watch -n 1 "ss -tanp '( sport = :5000 or dport = :5000 )'"` |

Se `watch` non è disponibile su Web4Student, usa il ciclo Bash della guida
oppure ripeti manualmente il comando `ss`.

Se gli script PowerShell sono bloccati, puoi incollare il ciclo nel terminale;
non è necessario modificare l'execution policy. Ferma il monitor con Ctrl+C.

## Consegna e autovalutazione

Prepara un file **`relazione.md`** nella cartella dell'esercizio con:

1. Sistema usato, cartella di lavoro e comandi realmente eseguiti.
2. Tabella degli endpoint del passo 4 e tabella delle fasi del passo 8.
3. Output o schermate di ascolto, connessione stabilita, conflitto di porta e arresto.
4. Spiegazione della differenza tra PID, porta del server e porta effimera del client.
5. Risposte alle domande dei passi 3, 4, 5 e 6.
6. Spiegazione del perché un record `TIME_WAIT` non dimostra una perdita di socket.

Consegna `ServerMonitor.java` e `relazione.md`. Se lavori nel repository
**Socket-Programming** in `~/socket` su Web4Student, registra sorgente e relazione
con Git e invia il commit a GitHub. Se lavori sul PC Windows, trasferiscili nella
cartella del repository prima del commit; i file `.class` restano esclusi dal
`.gitignore` preparato nell'esercizio 01.

Su Web4Student puoi scrivere la relazione con `nano relazione.md` dalla cartella
dell'esercizio. Per registrare la consegna nel repository, esegui poi:

```bash
cd ~/socket
git status
git add -- es03-monitoraggio-cli/ServerMonitor.java es03-monitoraggio-cli/relazione.md
git diff --cached
git commit -m "Completa l'esercizio di monitoraggio dei socket"
git push
git log --oneline -1
```

Controlla i file preparati prima del commit. Questi comandi Git funzionano anche
in PowerShell: sul PC Windows sostituisci `cd ~/socket` con
`Set-Location "$env:USERPROFILE\socket"` e usali soltanto nella copia del
repository già configurata con il suo remoto GitHub.

---

[Esercizio precedente: Java e VS Code](02-Java_e_VSCode_senza_amministratore.md) · [Guida: monitorare i socket](../04-Monitorare-i-Socket-dalla-CLI) · [Indice del capitolo](../README.md)
