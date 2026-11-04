# 🎓 ESERCITAZIONE: Socket TCP "Dispenser Numeri con Stato Condiviso"

> *Verifica delle competenze sui socket TCP multithread e sulla sincronizzazione — Sistemi e Reti 3*

---

> **Protocollo:** richieste e risposte sono righe UTF-8 sulla porta TCP `7777`. Ogni comando produce una risposta. `PRENOTA` assegna un numero e conclude la connessione; la sessione del negozio resta aperta per `NEGOZIO → STATO/SERVI → QUIT`. `QUIT` riceve `BYE` e chiude.

Il codice di riferimento comprende tre classi complete: `DispenserServer`, `DispenserClient` e `NegozioClient`. Prima progetta la tua soluzione, poi confrontala con il riferimento e spiega le scelte di sincronizzazione. Lo stato è conservato in memoria: riavviare il server azzera i contatori.

## 📋 **INFORMAZIONI GENERALI**

**Materia:** Sistemi e Reti\
**Argomento:** Socket TCP in Java, server multithread e contatori condivisi\
**Tempo stimato:** 3-4 ore\
**Difficoltà:** ⭐⭐⭐ (Intermedia)\
**Modalità:** Individuale o a coppie

---

## 🎯 **OBIETTIVI DIDATTICI**

Al termine dell'esercitazione lo studente sarà in grado di:

- ✅ **Implementare** un server TCP che gestisce più connessioni contemporaneamente.
- ✅ **Assegnare** numeri progressivi senza duplicati anche con richieste concorrenti.
- ✅ **Proteggere** un'operazione composta con `synchronized`.
- ✅ **Definire e interpretare** un protocollo testuale a righe.
- ✅ **Distinguere** una richiesta singola da una sessione persistente.
- ✅ **Gestire** coda vuota, comandi non validi e disconnessioni.
- ✅ **Verificare** gli invarianti dello stato condiviso con prove ripetibili.
- ✅ **Documentare** architettura, codice, test e risultati con screenshot commentati.

---

## 📚 **PREREQUISITI**

### Conoscenze Teoriche Richieste

- 🔌 **Socket TCP:** `Socket`, `ServerSocket`, connessione client-server ed EOF.
- 🧵 **Multithreading:** `Runnable`, lambda e avvio di un thread per connessione.
- 🔒 **Sincronizzazione:** sezioni critiche e monitor condiviso.
- ☕ **Java I/O:** `BufferedReader`, `PrintWriter`, UTF-8 e `try-with-resources`.
- 📝 **Gestione stringhe:** `split`, parsing di coppie chiave-valore e formattazione.

Ripassa le guide su [server multithread](../../02-Server-Multithreaded.md) e [sincronizzazione e risorse condivise](../../03-Sincronizzazione-Risorse-Condivise.md).

### Strumenti Necessari

- ☕ **JDK 11 o superiore**, con `java` e `javac` disponibili nel terminale.
- 💻 **VS Code** o un altro editor Java.
- 🖥️ **Almeno tre terminali:** server, client negozio e client che richiede il numero.
- 📸 **Strumento di cattura schermo**, per esempio lo Strumento di cattura di Windows.
- 📁 **Cartella del progetto** `~/socket/ES04b-Dispenser`, nel repository personale del corso.

Non sono richiesti privilegi di amministratore né librerie esterne. L'[estensione web PHP](README_avanzato.md) è facoltativa e richiede l'ambiente PHP indicato nella relativa guida.

---

## 📖 **TRACCIA DELL'ESERCIZIO**

### 🚀 **Applicazione: "Dispenser Numeri"**

Simula il distributore di numeri di un negozio. Ogni cliente richiede un numero progressivo; l'operatore completa il servizio dei numeri nell'ordine di assegnazione. Tutti i client devono osservare lo stesso stato del server.

1. **Il SERVER (`DispenserServer`):**
   - Ascolta sulla porta `7777`.
   - Gestisce ogni connessione in un thread separato.
   - Mantiene due contatori condivisi, inizialmente a zero.
   - Assegna il prossimo numero con `PRENOTA`.
   - Restituisce lo stato con `STATO`.
   - Completa il prossimo numero già distribuito con `SERVI`.
   - Rifiuta `SERVI` se non ci sono numeri in attesa.
   - Identifica la sessione del negozio con `NEGOZIO` e gestisce `QUIT`.
   - Chiude il socket alla fine della sessione, anche in caso di errore.

2. **Il CLIENT PER IL PUBBLICO (`DispenserClient`):**
   - Si connette a `localhost:7777`.
   - Invia `PRENOTA` e legge una sola risposta.
   - Visualizza numero assegnato, ultimo numero completato e persone davanti al cliente.
   - Chiude la connessione. Una nuova esecuzione richiede un nuovo numero.

3. **Il CLIENT NEGOZIO (`NegozioClient`):**
   - Apre una connessione e invia `NEGOZIO`.
   - Verifica la risposta `NEGOZIO_OK`.
   - Mantiene la stessa connessione per più comandi `SERVI` e `STATO`.
   - Mostra conferme ed errori senza confondere una coda vuota con un servizio completato.
   - Termina con `QUIT` oppure EOF della console.

`NEGOZIO` è un'identificazione didattica, senza password. Il riferimento non applica autorizzazioni ai comandi: anche un client TCP manuale può inviare `STATO` o `SERVI`. Un controllo dei ruoli è un'estensione distinta.

### 📐 **Esempi di Interazione**

Lo scenario seguente parte da un server appena avviato. Le righe dei vari thread possono comparire in un ordine diverso nei log; le risposte devono rispettare gli aggiornamenti effettivamente eseguiti.

**Terminale server — estratto dei log:**

```text
=== SERVER DISPENSER NUMERI ===
Server avviato sulla porta 7777
In attesa di connessioni...

[PRENOTA] Assegnato numero: 1
[PRENOTA] Assegnato numero: 2
[SERVI] Numero servito aggiornato a: 1
[SERVI] Numero servito aggiornato a: 2
```

**Primo cliente — dati visualizzati:**

```text
IL TUO NUMERO È: 1
Ultimo numero completato: 0
Persone davanti a te: 0
```

**Secondo cliente, dopo il primo e prima di SERVI:**

```text
IL TUO NUMERO È: 2
Ultimo numero completato: 0
Persone davanti a te: 1
```

**Sessione del negozio — scambio applicativo sulla stessa connessione:**

```text
Client → Server: NEGOZIO
Server → Client: NEGOZIO_OK

Client → Server: STATO
Server → Client: SERVIZIO:0|DISTRIBUITI:2|ATTESA:2

Client → Server: SERVI
Server → Client: SERVITO:1|ATTESA:1

Client → Server: STATO
Server → Client: SERVIZIO:1|DISTRIBUITI:2|ATTESA:1

Client → Server: SERVI
Server → Client: SERVITO:2|ATTESA:0

Client → Server: SERVI
Server → Client: ERRORE:Nessun numero in attesa

Client → Server: QUIT
Server → Client: BYE
```

Dopo l'ultimo `SERVI` fallito i contatori restano `ultimoNumeroAssegnato = 2` e `numeroInServizio = 2`. Il comando errato non deve consumare un numero futuro.

---

## 🛠️ **PASSAGGI DA SEGUIRE**

### 📝 **STEP 1: Analisi e Progettazione (20-30 minuti)**

#### 1.1 Analizza il Problema

Identifica responsabilità e dati delle tre classi:

| Classe | Responsabilità | Dati condivisi con altri gestori |
| --- | --- | --- |
| `DispenserServer` | Accettare connessioni, elaborare comandi e mantenere lo stato | I due contatori |
| `DispenserClient` | Richiedere e visualizzare il proprio numero | Nessuno nel processo client |
| `NegozioClient` | Inviare comandi sulla sessione dell'operatore | Nessuno nel processo client |

Nel riferimento il gestore è il metodo `handleClient`, eseguito da una lambda. Non serve un quarto file `ClientHandler.java`: estrarre quel metodo in una classe `Runnable` separata è una possibile riorganizzazione.

Prima di scrivere il codice rispondi a queste domande:

1. Quali operazioni modificano i contatori? Quali li leggono soltanto?
2. Perché due `PRENOTA` simultanei non devono restituire lo stesso numero?
3. Quale stato si deve osservare quando `SERVI` arriva insieme a `PRENOTA`?
4. Quale thread possiede ciascun socket e quando lo chiude?
5. Perché leggere dalla rete dentro un lock globale bloccherebbe anche gli altri client?

#### 1.2 Schema dell'Architettura

```text
                       DispenserServer
                   ServerSocket porta 7777
                              |
                 accept() e avvio dei gestori
                  /           |           \
       Gestore cliente 1  Gestore cliente 2  Gestore negozio
               |                  |                |
        PRENOTA e chiusura  PRENOTA e chiusura  NEGOZIO
                                                STATO/SERVI
                                                QUIT
                  \           |           /
                   Stato condiviso del server
                   - ultimoNumeroAssegnato
                   - numeroInServizio
                   - monitor DispenserServer.class
```

Ogni gestore ha un socket distinto. I contatori, invece, appartengono alla classe del server e sono condivisi da tutti i gestori.

#### 1.3 Protocollo di Comunicazione

Ogni messaggio termina con newline. I comandi di rete sono in maiuscolo; `NegozioClient` converte l'input dell'utente prima di inviarlo. I separatori `|` dividono i campi e `:` separa ciascuna chiave dal valore.

| Richiesta | Risposta | Effetto sulla connessione |
| --- | --- | --- |
| `PRENOTA` | `NUMERO:n\|SERVIZIO:s\|ATTESA:a` | Chiude dopo la risposta |
| `STATO` | `SERVIZIO:s\|DISTRIBUITI:d\|ATTESA:a` | Resta aperta |
| `NEGOZIO` | `NEGOZIO_OK` | Resta aperta |
| `SERVI` | `SERVITO:s\|ATTESA:a` | Resta aperta |
| `SERVI` senza numeri in attesa | `ERRORE:Nessun numero in attesa` | Resta aperta, stato invariato |
| `QUIT` | `BYE` | Chiude dopo la risposta |
| Comando non riconosciuto | `ERRORE: Comando non riconosciuto` | Resta aperta |

TCP è uno stream: una lettura non corrisponde necessariamente a una scrittura. `readLine()` ricostruisce la riga e restituisce `null` a EOF. Qui le risposte sono tutte su una riga: non servono il terminatore `END` o una lettura fino alla chiusura per ogni comando.

### ⚙️ **STEP 2: Definizione dello Stato Condiviso (20-30 minuti)**

#### 2.1 Significato dei Contatori

| Variabile Java | Valore iniziale | Significato |
| --- | --- | --- |
| `ultimoNumeroAssegnato` | 0 | Ultimo numero distribuito a un cliente |
| `numeroInServizio` | 0 | **Ultimo numero completato**, non il prossimo da servire |

Il nome storico `numeroInServizio` va interpretato secondo questa definizione. Il campo di protocollo `SERVIZIO` contiene lo stesso valore. Dopo aver completato il numero 1, il valore è 1; il successivo da completare, se già distribuito, è 2.

L'invariante da mantenere è:

```text
0 <= numeroInServizio <= ultimoNumeroAssegnato
```

#### 2.2 Calcolo delle Persone in Attesa

- Nella risposta a **PRENOTA**, `ATTESA = max(0, numeroAssegnato - numeroInServizio - 1)`: conta le persone **davanti al nuovo cliente**, escludendo lui stesso.
- Nelle risposte a **STATO** e **SERVI**, `ATTESA = ultimoNumeroAssegnato - numeroInServizio`: conta **tutti i numeri ancora da completare**.

Esempio: distribuiti 5, completati 2. STATO restituisce ATTESA 3. Se un nuovo cliente ottiene il numero 6, vede 3 persone davanti a sé, mentre il totale da completare diventa 4.

#### 2.3 Pianifica la Sincronizzazione

Nel riferimento `processaRichiesta` è `static synchronized`: tutti i gestori acquisiscono il monitor di `DispenserServer.class` per controllo, aggiornamento e costruzione della risposta. I metodi di supporto `prenota`, `getStato`, `serviCliente` e `stampaStatistiche` vengono chiamati soltanto da quel percorso protetto.

Non basta rendere atomico il solo incremento: anche la verifica della coda vuota e la lettura dell'altro contatore devono osservare uno stato coerente. Due contatori `volatile` o due operazioni atomiche indipendenti non rendono indivisibile l'intera richiesta.

Lettura e invio sul socket avvengono fuori dal monitor globale. La risposta fotografa lo stato al momento dell'elaborazione: quando arriva al client, un altro gestore potrebbe avere già completato una nuova operazione.

### ⚙️ **STEP 3: Implementazione Server (45-60 minuti)**

#### 3.1 Crea `DispenserServer.java`

Salva la classe completa nel file omonimo. Il listener esegue `accept()` e affida ogni socket a un nuovo thread; il gestore usa `try-with-resources` per chiuderlo anche in caso di eccezione.

```java
/** Stato condiviso del dispenser: contatori e risposta devono cambiare sotto lo stesso monitor. */
import java.io.*;
import java.net.*;

public class DispenserServer {
    private static final int PORTA = 7777;
    
    // Variabili condivise - devono essere sincronizzate
    private static int ultimoNumeroAssegnato = 0;
    private static int numeroInServizio = 0;
    
    public static void main(String[] args) {
        System.out.println("=== SERVER DISPENSER NUMERI ===");
        System.out.println("Server avviato sulla porta " + PORTA);
        System.out.println("In attesa di connessioni...\n");
        
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Gestisci client in thread separato per supporto multi-client
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Errore server: " + e.getMessage());
        }
    }
    
    private static void handleClient(Socket socket) {
        try (
            Socket owned = socket;
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8)
        ) {
            String clientIP = socket.getInetAddress().getHostAddress();
            System.out.println("[CONNESSIONE] Client da " + clientIP);
            
            String richiesta;
            while ((richiesta = in.readLine()) != null) {
                String risposta = processaRichiesta(richiesta);
                out.println(risposta);
                
                // PRENOTA conclude la richiesta; il negozio resta connesso fino a QUIT/EOF.
                if (richiesta.equals("PRENOTA") || richiesta.equals("QUIT")) {
                    break;
                }
            }
            
            System.out.println("[DISCONNESSIONE] Client disconnesso");
            
        } catch (IOException e) {
            System.err.println("Errore gestione client: " + e.getMessage());
        }
    }
    
    // Il monitor della classe protegge controllo, aggiornamento e costruzione della risposta.
    private static synchronized String processaRichiesta(String richiesta) {
        switch (richiesta) {
            case "PRENOTA":
                return prenota();
                
            case "STATO":
                return getStato();
                
            case "NEGOZIO":
                return "NEGOZIO_OK";
                
            case "SERVI":
                return serviCliente();
            case "QUIT":
                return "BYE";
                
            default:
                return "ERRORE: Comando non riconosciuto";
        }
    }
    
    private static String prenota() {
        ultimoNumeroAssegnato++;
        int numeroAssegnato = ultimoNumeroAssegnato;
        
        System.out.println("[PRENOTA] Assegnato numero: " + numeroAssegnato);
        
        int attesa = Math.max(0, numeroAssegnato - numeroInServizio - 1);
        
        // Formato: NUMERO:XX|SERVIZIO:YY|ATTESA:ZZ
        return String.format("NUMERO:%d|SERVIZIO:%d|ATTESA:%d", 
                           numeroAssegnato, numeroInServizio, attesa);
    }
    
    private static String getStato() {
        int attesa = Math.max(0, ultimoNumeroAssegnato - numeroInServizio);
        return String.format("SERVIZIO:%d|DISTRIBUITI:%d|ATTESA:%d",
                           numeroInServizio, ultimoNumeroAssegnato, attesa);
    }
    
    private static String serviCliente() {
        if (numeroInServizio >= ultimoNumeroAssegnato) return "ERRORE:Nessun numero in attesa";
        numeroInServizio++;
        System.out.println("[SERVI] Numero servito aggiornato a: " + numeroInServizio);
        
        int attesa = Math.max(0, ultimoNumeroAssegnato - numeroInServizio);
        
        stampaStatistiche();
        
        return String.format("SERVITO:%d|ATTESA:%d", numeroInServizio, attesa);
    }
    
    private static void stampaStatistiche() {
        System.out.println("\n--- STATISTICHE ---");
        System.out.println("Numeri distribuiti: " + ultimoNumeroAssegnato);
        System.out.println("Ultimo numero completato: " + numeroInServizio);
        System.out.println("Persone in attesa: " + 
                         (ultimoNumeroAssegnato - numeroInServizio));
        System.out.println("-------------------\n");
    }
}
```

#### 3.2 Verifica il Comportamento del Gestore

Segui il percorso di una richiesta nel codice:

1. `handleClient` legge una riga UTF-8 dal proprio socket.
2. `processaRichiesta` acquisisce il monitor della classe, interpreta il comando e costruisce la risposta.
3. `handleClient` invia la risposta con `println` e autoflush.
4. Per `PRENOTA` o `QUIT`, il ciclo termina e il socket viene chiuso; per gli altri comandi il gestore attende un'altra riga.

Il riferimento è introduttivo: crea un thread per connessione e non impone un limite globale o un timeout di lettura. La [guida avanzata](../../04-Multithreading-Avanzato.md) mostra come aggiungere limiti, timeout e arresto coordinato. Non aggiungere notifiche spontanee alla versione base: cambierebbero il protocollo e il modo in cui i client leggono le risposte.

### 💻 **STEP 4: Implementazione Client (45-60 minuti)**

#### 4.1 Crea `DispenserClient.java`

Il client invia una sola richiesta `PRENOTA`, interpreta i campi della risposta e chiude. Non offre un menu per STATO: quel comando si prova dal negozio o con un client TCP manuale.

```java
/** Richiede un numero con PRENOTA e legge una sola risposta prima della chiusura. */
import java.io.*;
import java.net.*;

public class DispenserClient {
    private static final String HOST = "localhost";
    private static final int PORTA = 7777;
    
    public static void main(String[] args) {
        System.out.println("=== CLIENT DISPENSER ===");
        
        try (
            Socket socket = new Socket(HOST, PORTA);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))
        ) {
            System.out.println("Connessione al server riuscita!");
            System.out.println("Richiesta numero in corso...\n");
            
            // Invia richiesta prenota
            out.println("PRENOTA");
            
            // Ricevi risposta
            String risposta = in.readLine();
            
            if (risposta != null) {
                visualizzaRisposta(risposta);
            }
            
            System.out.println("\nGrazie per aver utilizzato il servizio!");
            
        } catch (IOException e) {
            System.err.println("Errore connessione: " + e.getMessage());
        }
    }
    
    private static void visualizzaRisposta(String risposta) {
        // Formato: NUMERO:XX|SERVIZIO:YY|ATTESA:ZZ
        String[] parti = risposta.split("\\|");
        
        for (String parte : parti) {
            String[] keyValue = parte.split(":");
            if (keyValue.length == 2) {
                String chiave = keyValue[0];
                String valore = keyValue[1];
                
                switch (chiave) {
                    case "NUMERO":
                        System.out.println("╔════════════════════════════╗");
                        System.out.println("║   IL TUO NUMERO È: " + 
                                         String.format("%3s", valore) + "   ║");
                        System.out.println("╚════════════════════════════╝");
                        break;
                        
                    case "SERVIZIO":
                        System.out.println("\nUltimo numero completato: " + valore);
                        break;
                        
                    case "ATTESA":
                        System.out.println("Persone davanti a te: " + valore);
                        break;
                }
            }
        }
    }
}
```

#### 4.2 Crea `NegozioClient.java`

Il negozio si identifica una sola volta sulla connessione, poi esegue un ciclo richiesta-risposta. Dopo `QUIT` legge la conferma; EOF della console permette di uscire rilasciando le risorse.

```java
/** Mantiene la sessione NEGOZIO e legge una risposta per ogni comando. */
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class NegozioClient {
    private static final String HOST = "localhost";
    private static final int PORTA = 7777;
    
    public static void main(String[] args) {
        System.out.println("=== NEGOZIO - GESTIONE SERVIZIO ===");
        
        try (
            Socket socket = new Socket(HOST, PORTA);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Connessione al server come Negozio...\n");
            
            // Identifica come negozio
            out.println("NEGOZIO");
            String conferma = in.readLine();
            
            if (!"NEGOZIO_OK".equals(conferma)) {
                System.err.println("Errore identificazione negozio");
                return;
            }
            
            System.out.println("Identificazione riuscita!");
            mostraMenu();
            
            // Loop comandi
            while (true) {
                System.out.print("\n> ");
                if (!scanner.hasNextLine()) break; // EOF console: il try chiude la sessione.
                String comando = scanner.nextLine().trim().toUpperCase(java.util.Locale.ROOT);
                
                if (comando.equals("QUIT")) {
                    out.println("QUIT");
                    in.readLine();
                    System.out.println("Disconnessione...");
                    break;
                }
                
                if (comando.equals("SERVI") || comando.equals("STATO")) {
                    out.println(comando);
                    String risposta = in.readLine();
                    
                    if (risposta != null) {
                        if (risposta.startsWith("ERRORE:")) System.out.println(risposta);
                        else visualizzaRisposta(comando, risposta);
                    }
                } else {
                    System.out.println("Comando non riconosciuto. Usa SERVI, STATO o QUIT");
                }
            }
            
        } catch (IOException e) {
            System.err.println("Errore connessione: " + e.getMessage());
        }
    }
    
    private static void mostraMenu() {
        System.out.println("\nComandi disponibili:");
        System.out.println("  SERVI - Segna come servito e passa al prossimo");
        System.out.println("  STATO - Visualizza stato corrente");
        System.out.println("  QUIT  - Disconnetti");
    }
    
    private static void visualizzaRisposta(String comando, String risposta) {
        String[] parti = risposta.split("\\|");
        
        if (comando.equals("SERVI")) {
            System.out.println("\n✓ Cliente servito!");
            for (String parte : parti) {
                String[] kv = parte.split(":");
                if (kv.length == 2) {
                    if (kv[0].equals("SERVITO")) {
                        System.out.println("  Numero servito: " + kv[1]);
                    } else if (kv[0].equals("ATTESA")) {
                        System.out.println("  Persone in attesa: " + kv[1]);
                    }
                }
            }
        } else if (comando.equals("STATO")) {
            System.out.println("\n--- STATO SISTEMA ---");
            for (String parte : parti) {
                String[] kv = parte.split(":");
                if (kv.length == 2) {
                    switch (kv[0]) {
                        case "SERVIZIO":
                            System.out.println("Ultimo numero completato: " + kv[1]);
                            break;
                        case "DISTRIBUITI":
                            System.out.println("Numeri distribuiti: " + kv[1]);
                            break;
                        case "ATTESA":
                            System.out.println("Persone in attesa: " + kv[1]);
                            break;
                    }
                }
            }
            System.out.println("--------------------");
        }
    }
}
```

#### 4.3 Controlli Prima dell'Avvio

- Salva ciascuna classe nel proprio file, senza aggiungere dichiarazioni `package` al riferimento.
- Usa lo stesso host, la stessa porta e UTF-8 sui due lati.
- Per un server remoto cambia `HOST` in entrambi i client: `localhost` indica il computer che esegue quel client.
- Non leggere risposte con più reader sullo stesso input.
- Distinguere `ERRORE:...` da una conferma è parte del protocollo, non un dettaglio grafico del client.

### 🧪 **STEP 5: Testing e Documentazione (45-60 minuti)**

#### 5.1 Test di Compilazione

Apri il terminale nella cartella che contiene i tre sorgenti:

```sh
javac -encoding UTF-8 DispenserServer.java DispenserClient.java NegozioClient.java
```

Il comando vale sia su Linux sia in PowerShell. Verifica che vengano generati i tre file `.class` e risolvi gli errori prima delle prove di rete.

#### 5.2 Test Funzionale Multi-Client

**Terminale 1 — server:**

```sh
java DispenserServer
```

**Terminale 2 — negozio:**

```sh
java NegozioClient
```

Lascia aperta la sessione del negozio. Esegui STATO, poi avvia un cliente in un altro terminale:

```sh
java DispenserClient
```

Ripeti il comando per richiedere un secondo numero. Torna al negozio ed esegui `STATO`, `SERVI`, `STATO`, `SERVI`, `SERVI`, `QUIT`. Confronta risposte e contatori con l'esempio di interazione. Arresta il server con Ctrl+C solo alla fine delle prove della sessione.

#### 5.3 Scenari di Test Completi

I casi 1-10 seguono l'ordine della tabella, partendo da contatori a zero. I casi 11-15 sono prove aggiuntive: annota lo stato iniziale o riavvia il server e riconnetti i client. Le risposte indicate sono **attese**; compila una colonna separata con l'esito effettivamente osservato.

| # | Scenario | Azione | Risultato Atteso | Evidenza |
| --- | --- | --- | --- | --- |
| 1 | Avvio server | Avvia `DispenserServer` | Ascolto sulla porta 7777 | SS01 |
| 2 | Sessione negozio | Avvia `NegozioClient` | `NEGOZIO_OK`, connessione aperta | SS02 |
| 3 | Stato iniziale | Invia STATO dal negozio | SERVIZIO 0, DISTRIBUITI 0, ATTESA 0 | SS03 |
| 4 | Primo cliente | Esegui `DispenserClient` | NUMERO 1, SERVIZIO 0, ATTESA 0 | SS04 |
| 5 | Secondo cliente | Esegui di nuovo il client | NUMERO 2, SERVIZIO 0, ATTESA 1 | SS05 |
| 6 | Stato condiviso | Invia STATO dalla sessione ancora aperta | DISTRIBUITI 2, ATTESA 2 | SS06 |
| 7 | Primo servizio | Invia SERVI | SERVITO 1, ATTESA 1 | SS07 |
| 8 | Stato aggiornato | Invia STATO | SERVIZIO 1, DISTRIBUITI 2, ATTESA 1 | SS08 |
| 9 | Secondo servizio | Invia SERVI | SERVITO 2, ATTESA 0 | SS09 |
| 10 | Coda vuota | Invia ancora SERVI e poi STATO | ERRORE; contatori invariati | SS10 |
| 11 | Richieste concorrenti | Avvia almeno due client insieme | Numeri distinti e consecutivi, senza imporre quale client ottenga il primo | SS11 |
| 12 | Uscita ed EOF | Invia QUIT; in una prova separata chiudi il client improvvisamente | BYE per QUIT; gli altri client continuano a funzionare | SS12 |
| 13 | Comando non valido | Da un client TCP manuale invia `SCONOSCIUTO`, poi STATO | Una riga ERRORE, poi risposta STATO senza riconnettersi | SS13 |
| 14 | Confini dei messaggi | Invia una richiesta in più scritture e più comandi in una scrittura | Una risposta per ogni riga completa | SS14 |
| 15 | Verifica contatori | Esegui PRENOTA e SERVI concorrenti, poi STATO | Nessun duplicato e `0 <= SERVIZIO <= DISTRIBUITI` | SS15 |

Per i casi 13-14 serve un client TCP di prova: `NegozioClient` filtra localmente i comandi non riconosciuti. Un comando digitato e rifiutato dal suo menu non dimostra che il server abbia gestito l'errore. Il client di prova deve inviare le righe con `flush` e leggere una risposta per riga; senza newline il server può attendere fino a EOF.

Per verificare la concorrenza lascia inoltre un client TCP connesso senza inviare e controlla che un secondo client ottenga un numero. Un esito corretto in una sola esecuzione non basta a giustificare la sincronizzazione: descrivi quali dati protegge il monitor e perché l'invariante resta valida.

#### 5.4 Creazione Documento Google

Crea una relazione seguendo questo modello. Inserisci almeno **15 screenshot**, associati ai casi SS01-SS15, con didascalie che indichino comando, stato iniziale e risultato. Per una prova che richiede più passaggi puoi usare una cattura con più terminali leggibili.

```text
DISPENSER NUMERI — Esercitazione Socket TCP
=========================================

Studente/i: [Nome Cognome]
Classe: [Classe]
Data: [Data]

1. INTRODUZIONE
   - Problema simulato: distribuzione numeri e completamento del servizio.
   - Funzionalità realizzate e limiti della versione base.

2. ARCHITETTURA
   - Schema server, gestori concorrenti, clienti e negozio.
   - Ruolo di DispenserServer, DispenserClient e NegozioClient.
   - Dati condivisi e proprietà dei socket.

3. STATO CONDIVISO E SINCRONIZZAZIONE
   - Significato dei due contatori e valori iniziali.
   - Invariante: 0 <= numeroInServizio <= ultimoNumeroAssegnato.
   - Monitor usato e operazioni incluse nella sezione critica.
   - Differenza fra i due significati di ATTESA.

4. PROTOCOLLO DI COMUNICAZIONE
   - PRENOTA, STATO, NEGOZIO, SERVI, QUIT e risposte.
   - UTF-8, newline, errori ed EOF.
   - Richiesta singola e sessione persistente del negozio.

5. SCREENSHOT DI FUNZIONAMENTO
   - SS01-SS15, ciascuno con didascalia e risultato osservato.
   - Evidenza di almeno due richieste concorrenti.

6. CODICE SORGENTE
   - Repository: <URL_DEL_TUO_REPOSITORY>
   - Oppure cartella di consegna: <URL_CARTELLA_CONSEGNA>
   - Comandi di compilazione e avvio.

7. FUNZIONALITÀ IMPLEMENTATE
   - [Da compilare con quanto effettivamente realizzato.]
   - Eventuali estensioni separate dalla versione base.

8. PROBLEMI RISCONTRATI E SOLUZIONI
   - [Problema, causa individuata, modifica e nuova verifica.]
   - Motivazione della protezione di controllo, aggiornamento e risposta.

9. TESTING
   - Tabella con atteso, osservato, esito ed evidenza.
   - Numeri ottenuti nelle prove concorrenti.
   - Coda vuota, errore di comando e disconnessione improvvisa.

10. CONCLUSIONI E POSSIBILI ESTENSIONI
    - Competenze acquisite e limiti rimasti.
    - Pool, timeout, persistenza, ruoli o interfaccia web.
```

Non compilare preventivamente tutti gli esiti come riusciti: documenta anche gli errori trovati e le prove ripetute dopo la correzione.

---

## 📝 **DELIVERABLE RICHIESTI**

### 📁 File da Consegnare

1. **Codice sorgente Java:** `DispenserServer.java`, `DispenserClient.java`, `NegozioClient.java` ed eventuali classi aggiuntive della tua soluzione.
2. **Documento Google:** link alla relazione, descrizione del protocollo, spiegazione della sincronizzazione, tabella dei risultati e almeno 15 screenshot commentati.
3. **Repository Git:** cartella `ES04b-Dispenser` nella tua homepage `~/socket`, README con istruzioni e `.gitignore` per i file generati. Inserisci nella relazione il collegamento alla cartella dell'esercizio.

Non consegnare soltanto i `.class`: il docente deve poter leggere, compilare e avviare i sorgenti. Le estensioni facoltative devono avere istruzioni proprie e non sostituire il protocollo base senza spiegazione.

---

## 🏆 **CRITERI DI VALUTAZIONE**

### 📊 Griglia di Valutazione (Totale: 100 punti)

| Criterio | Punti | Descrizione |
| --- | --- | --- |
| **Server multi-client** | 15 | Listener operativo e gestori indipendenti |
| **Protocollo** | 15 | Comandi, campi, newline, durata delle connessioni e risposte corretti |
| **Stato condiviso e sincronizzazione** | 30 | Numeri univoci, controllo della coda vuota e risposta coerente con gli aggiornamenti |
| **Client, risorse ed errori** | 15 | Sessione negozio persistente, messaggi comprensibili e socket chiusi anche su errore |
| **Testing e documentazione** | 20 | Prove concorrenti ripetibili, risultati effettivi e 15 screenshot commentati |
| **Qualità del codice** | 5 | Commenti utili, nomi comprensibili e organizzazione |

### 🎯 Livelli di Competenza

- **90-100 — Eccellente:** requisiti completi, sincronizzazione motivata e prove documentate.
- **80-89 — Buono:** comportamento corretto con poche carenze nella spiegazione o nelle evidenze.
- **70-79 — Sufficiente:** funzionalità principali presenti, con alcune verifiche da completare.
- **Sotto 70 — Da consolidare:** requisiti o verifiche ancora incompleti.

Le estensioni sono facoltative e vanno concordate con il docente; non compensano errori nel protocollo o nella sincronizzazione della versione base.

---

## 💡 **SUGGERIMENTI E TRUCCHI**

### 🔧 Best Practices

- Scrivi prima i significati dei contatori e gli invarianti, poi il codice.
- Usa lo stesso monitor per letture e scritture concorrenti dei dati condivisi.
- Mantieni nella sezione critica controllo, aggiornamento e costruzione della risposta.
- Leggi e invia sulla rete fuori dal lock globale: un client lento non deve bloccare gli altri gestori.
- Usa un solo reader per input e una risposta completa per comando.
- Confronta le risposte del protocollo con ciò che il client mostra, soprattutto per ATTESA ed ERRORE.
- Prova EOF e chiusura improvvisa oltre alla sequenza di comandi valida.

Il riferimento stampa alcuni log nella sezione sincronizzata: è una semplificazione di laboratorio. Per ridurre la contesa, una versione più evoluta può costruire un record coerente sotto lock ed emetterlo dopo il rilascio, senza pretendere che l'ordine di arrivo sulla console coincida sempre con quello dei contatori.

### 🚨 Errori Comuni da Evitare

- ❌ Incrementare fuori dal lock o usare un lock diverso per ogni gestore.
- ❌ Eseguire SERVI senza controllare prima che esista un numero distribuito da completare.
- ❌ Interpretare `numeroInServizio` come il prossimo numero da servire.
- ❌ Contare il nuovo cliente fra le persone davanti a lui nella risposta PRENOTA.
- ❌ Chiudere la sessione del negozio dopo NEGOZIO o dopo il primo STATO.
- ❌ Aspettare EOF per leggere STATO: la connessione resta aperta.
- ❌ Usare un browser o curl HTTP direttamente sulla porta del protocollo Dispenser.
- ❌ Scambiare l'identificazione NEGOZIO per un'autenticazione.

### 📸 Screenshot Leggibili

Crea una cartella `screenshot` nel progetto e usa nomi coerenti con la tabella:

```text
screenshot/
├── SS01-server-avviato.png
├── SS02-sessione-negozio.png
├── SS03-stato-iniziale.png
├── SS04-primo-numero.png
├── SS05-secondo-numero.png
├── SS10-coda-vuota.png
├── SS11-richieste-concorrenti.png
└── ...altre evidenze fino a SS15
```

Mostra comando e risposta nella stessa cattura. Per le prove concorrenti disponi i terminali affiancati; accompagna l'immagine con i numeri ottenuti e lo stato finale. La sola presenza di più finestre non dimostra che le richieste siano state concorrenti.

### 🎯 Estensioni Avanzate (Extra)

1. **Persistenza:** salva i contatori e ripristinali all'avvio, mantenendo l'invariante. Definisci cosa accade se il salvataggio fallisce e quando viene confermata una richiesta.
2. **Pool e timeout:** limita worker e coda, chiudi i socket rifiutati e quelli cancellati durante l'arresto. Distingui timeout di lettura e durata complessiva della sessione.
3. **Ruoli:** riserva SERVI agli operatori attraverso una vera identificazione/autenticazione e controlla i permessi sul server.
4. **Interfaccia web PHP:** segui l'[estensione già predisposta](README_avanzato.md), riusando lo stesso server e lo stesso significato dei campi.
5. **Notifiche:** progetta un nuovo protocollo per avvisare i clienti in attesa. Occorrono connessioni persistenti e un modo per distinguere eventi e risposte: i client base non leggono notifiche spontanee.

Non reinviare automaticamente PRENOTA dopo la perdita della risposta: ogni nuova richiesta assegna un nuovo numero. Per riconoscere i tentativi ripetuti occorre aggiungere un identificativo di richiesta e una politica di deduplicazione.

---

## 📚 **RISORSE UTILI**

### 📖 Documentazione del Corso

- [Server TCP multithread](../../02-Server-Multithreaded.md).
- [Sincronizzazione e risorse condivise](../../03-Sincronizzazione-Risorse-Condivise.md).
- [Pool, timeout e arresto](../../04-Multithreading-Avanzato.md).
- [Monitorare i socket dalla CLI](../../../02-Ambiente-Sviluppo/04-Monitorare-i-Socket-dalla-CLI).
- [Dispenser con interfaccia web PHP](README_avanzato.md).

### 🔍 Comandi Debug

| Obiettivo | Linux / web4student | Windows PowerShell |
| --- | --- | --- |
| Versioni Java | `java -version` e `javac -version` | `java -version` e `javac -version` |
| Listener sulla porta 7777 | `ss -ltnp 'sport = :7777'` | `Get-NetTCPConnection -LocalPort 7777 -State Listen` |
| Connessioni del server | `ss -tnp '( sport = :7777 or dport = :7777 )'` | `Get-NetTCPConnection -LocalPort 7777` |

Le porte e i processi mostrati dipendono dall'istante della cattura e dai permessi dell'utente. Se Java gira su web4student, esegui i comandi Linux nella sessione SSH su quel server; `localhost` nei client deve essere sostituito con il nome del server se i client girano sul tuo PC.

Su Linux, con netcat già disponibile, prova più comandi sulla stessa connessione:

```sh
printf 'NEGOZIO\nSTATO\nQUIT\n' | nc localhost 7777
```

Le risposte attese sono tre righe: `NEGOZIO_OK`, lo stato corrente e `BYE`. Il server conclude la connessione dopo QUIT.

Per una prova con cinque clienti non interattivi, da Bash nella cartella del progetto:

```bash
mkdir -p tmp
for i in 1 2 3 4 5; do
    java DispenserClient > "tmp/cliente-$i.log" 2>&1 &
done
wait
cat tmp/cliente-*.log
```

In PowerShell, dalla stessa cartella:

```powershell
$cartellaProgetto = $PWD.Path
$lavori = 1..5 | ForEach-Object {
    Start-Job -ScriptBlock {
        param($cartella)
        Set-Location -LiteralPath $cartella
        java DispenserClient
    } -ArgumentList $cartellaProgetto
}
$lavori | Wait-Job | Receive-Job
$lavori | Remove-Job
```

Avvia prima il server e non eseguire SERVI durante questa prova se vuoi confrontare soltanto i numeri assegnati. Controlla numeri distinti e consecutivi a partire dallo stato iniziale annotato; non imporre un ordine fra i cinque processi client.

### 💬 FAQ

**Come evito numeri duplicati?**\
Proteggi l'assegnazione e la costruzione della relativa risposta con il monitor comune. Il solo fatto di usare un thread per client non protegge i dati condivisi.

**Perché ATTESA cambia significato fra PRENOTA e STATO?**\
PRENOTA mostra le persone davanti a quel cliente; STATO mostra tutti i numeri ancora da completare. Spiega entrambe le formule nella relazione.

**Il cliente riceve aggiornamenti dopo PRENOTA?**\
No. Legge la risposta e chiude. Le notifiche richiedono un'estensione del protocollo e un client che rimanga in ascolto.

**NEGOZIO garantisce che solo l'operatore possa inviare SERVI?**\
No. Il riferimento conferma l'identificazione ma non impone un controllo dei ruoli. Descrivi questa limitazione senza presentarla come autenticazione.

**I numeri sopravvivono al riavvio?**\
No. La versione base conserva lo stato nella memoria del processo. La persistenza va progettata come estensione.

**Chi deve ricevere il numero più basso nella prova concorrente?**\
Il client la cui richiesta viene elaborata per prima. L'ordine di avvio dei terminali non determina l'ordine di acquisizione del monitor.

---

## ⏰ **SCADENZE E MODALITÀ DI CONSEGNA**

### 📅 Timeline

- **Assegnazione e scadenza:** quelle comunicate dal docente.
- **Sequenza di lavoro:** analisi, stato condiviso, server, client, test e relazione.
- **Prima della consegna:** ricompila dai sorgenti e ripeti almeno una prova concorrente e una di errore.

### 📤 Modalità Consegna

**1. Documento Google:** crea la relazione, inserisci le evidenze e il collegamento al codice; condividila con il docente secondo le modalità indicate in classe e verifica che il collegamento sia accessibile.

**2. Codice sorgente:** usa il repository personale del corso con una struttura come questa:

```text
~/socket/
└── ES04b-Dispenser/
    ├── README.md
    ├── DispenserServer.java
    ├── DispenserClient.java
    ├── NegozioClient.java
    ├── .gitignore
    └── screenshot/
```

Sostituisci i segnaposto `<URL_DEL_TUO_REPOSITORY>` e `<URL_CARTELLA_CONSEGNA>` con i tuoi collegamenti effettivi. Mantieni file compilati e log temporanei fuori dalla consegna dei sorgenti.

---

## 🎉 **CONCLUSIONI**

Il Dispenser collega un problema concreto a tre scelte fondamentali: un gestore per connessione, un protocollo con messaggi ben delimitati e un monitor comune per lo stato condiviso. La soluzione è corretta quando numeri assegnati, servizi completati e risposte ai client rispettano gli stessi invarianti.

**Consiglio strategico:** implementa prima PRENOTA e STATO, aggiungi SERVI con controllo della coda vuota, poi completa la sessione NEGOZIO e la gestione delle chiusure. Documenta i risultati mentre esegui le prove.

**Challenge finale:** riesci a gestire dieci richieste PRENOTA concorrenti ottenendo dieci numeri distinti e consecutivi, per poi completare tutti i servizi senza superare il numero distribuito?

[Indice esercizi](../README.md) · [Prima: multithreading essenziale](../a-Mutithread-base/README.md) · [Dopo: prenotazioni](../c-Prenotazioni/README.md)
