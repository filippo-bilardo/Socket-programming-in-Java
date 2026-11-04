# Multithreading avanzato: pool, errori, timeout e arresto

Dopo il [server con un thread per client](02-Server-Multithreaded.md) e la [sincronizzazione dei dati](03-Sincronizzazione-Risorse-Condivise.md), affrontiamo il controllo delle risorse e il ciclo di vita del servizio. Gli esempi richiedono JDK 11+ e non usano librerie esterne. Per i thread virtuali, che richiedono JDK 21+, consulta la [guida Java](01-Multithreading-Java.md#13-thread-virtuali-java-21-e-successivi).

## 1. Separare compiti e thread

Creare un thread di piattaforma per ogni connessione permette concorrenza, ma consuma memoria e risorse del sistema operativo. Un `ExecutorService` riceve i compiti `Runnable` e ne gestisce l'esecuzione. I worker vengono riutilizzati: il compito resta un gestore con un socket di cui è responsabile.

Le istruzioni seguenti sono frammenti alternativi da inserire in un metodo, con gli import `java.util.concurrent.*`. Ogni executor creato deve essere chiuso esplicitamente al termine: con JDK 11 non è utilizzabile nel `try-with-resources`.

```java
// Un numero fisso di worker, ma una coda senza capacità massima.
ExecutorService fixed = Executors.newFixedThreadPool(10);

// Nuovi worker secondo necessità, senza un limite di thread adatto all'ammissione.
ExecutorService cached = Executors.newCachedThreadPool();

// Un solo worker: i compiti vengono eseguiti in sequenza, coda non limitata.
ExecutorService single = Executors.newSingleThreadExecutor();

// Per scadenze e attività periodiche; non per mantenere socket senza limiti.
ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(2);
```

Il numero di thread, i task in coda e i socket aperti sono grandezze diverse. Un fixed pool limita i worker ma può accumulare un numero crescente di socket in attesa. Il backlog TCP del sistema operativo riguarda connessioni in attesa di `accept()` e non sostituisce una coda applicativa limitata.

### Worker, coda e rifiuti

Con un `ThreadPoolExecutor`, i task avviano nuovi worker fino a `corePoolSize`; poi vengono accodati. Solo quando la coda è piena il pool può crescere fino a `maximumPoolSize`. Se anche quel limite è raggiunto, interviene la politica di rifiuto.

- Una `ArrayBlockingQueue` impone un limite ai task in attesa.
- Una `SynchronousQueue` trasferisce direttamente il task a un worker, senza conservarlo in coda.
- La politica predefinita `AbortPolicy` lancia `RejectedExecutionException`: il chiamante deve chiudere il socket rifiutato.
- `CallerRunsPolicy` esegue il gestore sul thread che lo sottomette. In un accept loop può bloccare l'accettazione per l'intera sessione del client: va scelta consapevolmente.
- Scartare un task senza chiudere le sue risorse lascia aperto il socket.

Per connessioni persistenti un worker può restare occupato anche senza richieste. Dimensiona il pool considerando durata delle sessioni, memoria, CPU e risorse esterne. Misura il comportamento con client lenti e sovraccarico; più worker non garantiscono maggiore velocità.

## 2. Errori e timeout nel gestore

Un errore di una richiesta non deve necessariamente terminare la sessione. Un errore di trasporto, EOF o timeout può invece concluderla. Con `readLine()`, EOF ordinario è `null`; con letture binarie come `readUTF()` può essere `EOFException`.

**File completo `ClientHandlerWithExceptionHandling.java`:** il protocollo è una richiesta per riga UTF-8 e una risposta per riga; `quit` riceve `BYE` e conclude la sessione. Le righe vuote ricevono un errore. Questo gestore è usato anche dal server della prossima sezione.

```java
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/** Un gestore possiede un socket: EOF, errori e timeout passano dalla chiusura automatica. */
public class ClientHandlerWithExceptionHandling implements Runnable {
    private final Socket socket;

    public ClientHandlerWithExceptionHandling(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (Socket owned = socket) {
            socket.setSoTimeout(30_000); // Timeout delle letture, espresso in millisecondi.
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            String request;
            while ((request = in.readLine()) != null) {
                if (request.equalsIgnoreCase("quit")) {
                    out.println("BYE");
                    break;
                }
                try {
                    out.println(processRequest(request));
                } catch (IllegalArgumentException e) {
                    out.println("ERRORE: " + e.getMessage()); // Errore applicativo recuperabile.
                }
                // PrintWriter conserva gli errori invece di lanciare IOException.
                if (out.checkError()) break;
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Client inattivo: " + socket.getRemoteSocketAddress());
        } catch (IOException e) {
            System.err.println("Chiusura/I-O di " + socket.getRemoteSocketAddress() + ": " + e.getMessage());
        }
    }

    private String processRequest(String request) {
        if (request.isBlank()) throw new IllegalArgumentException("Richiesta vuota");
        return "Processato: " + request;
    }
}
```

`setSoTimeout()` limita l'attesa delle letture sul socket, non la durata complessiva della sessione né le scritture. Un client che invia lentamente frammenti può richiedere una scadenza applicativa separata. Anche la lunghezza massima di una riga va definita per un servizio esposto: `BufferedReader.readLine()` non impone un limite applicativo.

Per il timeout di connessione lato client usa un socket inizialmente non connesso e `connect(indirizzo, millisecondi)`. Un timeout su `Future.get()` limita l'attesa del chiamante senza cancellare il task; `cancel(true)` tenta l'interruzione, che il lavoro deve gestire.

### Eccezioni non catturate

Una `RuntimeException` non catturata termina il thread che esegue `run()`; non viene rilanciata nel thread che ha chiamato `start()`. Il processo può proseguire se restano altri thread non daemon. Pulizia delle risorse e stato condiviso restano responsabilità del programma.

**File completo `CustomExceptionHandler.java`:**

```java
/** Diagnostica di ultima istanza per un thread; la pulizia resta nei finally del compito. */
public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread thread, Throwable error) {
        System.err.println("Eccezione nel thread " + thread.getName());
        error.printStackTrace();
    }
}
```

Frammento da inserire in un metodo, dove `runnable` è già definito:

```java
Thread thread = new Thread(runnable, "gestore-client");
thread.setUncaughtExceptionHandler(new CustomExceptionHandler());
thread.start();
```

Con `execute()` un'eccezione non catturata può terminare il worker, che il pool può sostituire. Con `submit()` il fallimento viene conservato nel `Future`: leggi il risultato con `get()` e gestisci `ExecutionException`, altrimenti l'errore può passare inosservato. Non usare l'handler come sostituto della gestione degli errori di protocollo.

## 3. Un server con coda limitata e arresto coordinato

Questo esempio ammette fino a 10 gestori in esecuzione e 20 socket in coda. Il socket appena accettato oltre tale capacità viene chiuso. Il limite riguarda il lavoro applicativo ammesso; il listener e la connessione appena accettata sono risorse aggiuntive transitorie.

**File completo `GracefulServer.java`:** usa il gestore completo della sezione 2. `lifecycle` coordina ammissione e arresto, mentre il set concorrente comprende socket attivi e accodati. Non si presume che l'interruzione di un thread di piattaforma sblocchi una lettura sul suo `Socket`.

```java
import java.io.*;
import java.net.*;
import java.util.Set;
import java.util.concurrent.*;

/** Ferma l'ascolto, concede un breve tempo ai gestori, poi chiude le connessioni residue. */
public class GracefulServer {
    private final ServerSocket listener;
    private final ExecutorService pool = new ThreadPoolExecutor(
            10, 10, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(20));
    private final Set<Socket> clients = ConcurrentHashMap.newKeySet();
    private final Object lifecycle = new Object();
    private volatile boolean running = true;

    public GracefulServer(int port) throws IOException {
        listener = new ServerSocket(port);
    }

    public void start() throws IOException {
        try {
            while (running) {
                Socket socket = listener.accept(); // La chiusura del listener sblocca accept().
                synchronized (lifecycle) {
                    if (!running) {
                        close(socket);
                        break;
                    }
                    clients.add(socket); // Registrare anche i socket dei task in coda.
                    try {
                        pool.execute(() -> {
                            try {
                                new ClientHandlerWithExceptionHandling(socket).run();
                            } finally {
                                clients.remove(socket);
                                close(socket);
                            }
                        });
                    } catch (RejectedExecutionException e) {
                        clients.remove(socket);
                        close(socket); // Il task rifiutato non eseguirà mai il proprio finally.
                    }
                }
            }
        } catch (SocketException e) {
            if (running) throw e; // La chiusura locale durante shutdown è prevista.
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        synchronized (lifecycle) {
            if (!running) return; // Una sola richiesta esegue la procedura di arresto.
            running = false;
            try { listener.close(); } catch (IOException ignored) { }
            pool.shutdown(); // Nessun nuovo task; quelli accettati possono completarsi.
        }
        try {
            // Non attendere sotto lifecycle: i gestori devono poter proseguire.
            if (!pool.awaitTermination(2, TimeUnit.SECONDS)) {
                cancelRemaining();
                if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Alcuni gestori non sono terminati");
                }
            }
        } catch (InterruptedException e) {
            cancelRemaining();
            Thread.currentThread().interrupt();
        }
    }

    private void cancelRemaining() {
        // Nessuna nuova ammissione: il set contiene anche i task che non partiranno.
        for (Socket socket : clients) close(socket);
        pool.shutdownNow(); // Tenta l'interruzione e rimuove i task ancora in coda.
        clients.clear();
    }

    private static void close(Socket socket) {
        try { socket.close(); } catch (IOException ignored) { }
    }

    public static void main(String[] args) throws IOException {
        int port = args.length == 0 ? 5000 : Integer.parseInt(args[0]);
        GracefulServer server = new GracefulServer(port);
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown, "arresto-server"));
        System.out.println("Server in ascolto sulla porta " + port);
        server.start();
    }
}
```

Salva i due file nella stessa cartella:

```sh
javac -encoding UTF-8 ClientHandlerWithExceptionHandling.java GracefulServer.java
java GracefulServer 5000
```

Collegati da due terminali con un client TCP a righe, per esempio `nc localhost 5000` su Linux con netcat installato. Invia `ciao`, una riga vuota e `quit`. Per Windows puoi adattare il client dell'[esempio Runnable](esempi/es02-Runnable/TcpClient.java) cambiando `PORT` in 5000.

L'arresto ha due fasi: non accetta più connessioni e attende per due secondi; poi annulla il lavoro residuo chiudendo anche i socket dei task mai avviati. Questo è un compromesso didattico: le sessioni persistenti potrebbero non finire nel periodo concesso. Per operazioni non ripetibili occorre definire esplicitamente completamento, conferme e politica di riconnessione. Lo shutdown hook funziona durante una chiusura ordinaria della JVM, non è garantito dopo un arresto forzato del processo.

Non chiamare questo `shutdown()` da un task del medesimo pool: attenderebbe anche la propria terminazione. Un supervisore o lo shutdown hook deve coordinare la chiusura. L'esempio di [pool con statistiche](esempi/ConnectionPoolServer.java) applica invece una politica di annullamento immediato: distingue connessioni attive, accodate, completate e rifiutate.

## 4. Logging e diagnosi

Un log utile contiene istante, livello, nome del thread e identificativo della connessione. Le stampe aiutano a osservare un'esecuzione ma ne modificano i tempi: non sono una prova di assenza di race condition. Il nome del thread aiuta anche a interpretare un thread dump quando un gestore resta bloccato.

**File completo `ServerLogger.java`:** il formatter è immutabile e condivisibile; ogni record ordinario viene emesso con un'unica `println`. Non c'è un ordinamento globale tra `System.out` e `System.err`.

```java
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Logger didattico per la console; per persistenza e rotazione serve una soluzione dedicata. */
public class ServerLogger {
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static String prefix(String level) {
        return "[" + LocalDateTime.now().format(FORMAT) + "] [" + level + "] ["
                + Thread.currentThread().getName() + "] ";
    }

    public static void logInfo(String message) {
        System.out.println(prefix("INFO") + message);
    }

    public static void logDebug(String message) {
        System.out.println(prefix("DEBUG") + message);
    }

    public static void logError(String message, Exception error) {
        // Intestazione e stack trace possono occupare più righe.
        synchronized (System.err) {
            System.err.println(prefix("ERROR") + message);
            error.printStackTrace(System.err);
        }
    }
}
```

Per un servizio reale definisci livelli, destinazione, rotazione e trattamento dei dati ricevuti. Non acquisire il lock dello stato del server per effettuare operazioni lente sul file di log. Un logger asincrono richiede a sua volta una coda limitata e una politica quando è piena.

## 5. Esercizi e prove osservabili

1. **Calcolatrice:** riprendi l'esercizio della guida base e aggiungi un massimo di cinque worker, coda limitata e timeout di lettura. Specifica risposte per operazioni errate, numeri non validi e divisione per zero. Tieni occupati tutti i worker e la coda: le connessioni eccedenti devono essere chiuse e il server deve restare utilizzabile dopo il sovraccarico.
2. **Arresto:** lascia client inattivi e task in coda, poi arresta il server. Verifica la chiusura del listener, l'uscita dei lettori e la chiusura dei socket accodati.
3. **Errori isolati:** invia una richiesta non valida e poi una valida sulla stessa sessione. Una disconnessione improvvisa non deve impedire le risposte agli altri client.
4. **Log centralizzato:** ricevi record da più client, conserva timestamp e livello, poi prevedi query degli ultimi cinque minuti. Definisci risposte multiriga con terminatore e controlla la concorrenza secondo la [guida sullo stato condiviso](03-Sincronizzazione-Risorse-Condivise.md).

**Autovalutazione:**

- Un fixed pool limita anche i socket accodati? **No:** la sua coda è senza limite.
- `shutdownNow()` chiude automaticamente i socket? **No:** il programma deve gestirne la proprietà e la chiusura.
- `setSoTimeout()` limita le scritture? **No:** riguarda le letture bloccanti.
- Un'eccezione di un task passato a `submit()` arriva sempre all'`UncaughtExceptionHandler`? **No:** viene conservata nel `Future`.
- Perché l'ammissione deve coordinarsi con lo shutdown? **Per evitare di registrare un nuovo socket dopo la pulizia finale.**

## Riferimenti ufficiali

- [ThreadPoolExecutor: worker, code e rifiuti](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ThreadPoolExecutor.html)
- [ExecutorService: esecuzione e arresto](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ExecutorService.html)
- [Socket: timeout e chiusura](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/Socket.html)
- [PrintWriter: checkError](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/PrintWriter.html)

[Indice del modulo](README.md) · [Prima: sincronizzazione](03-Sincronizzazione-Risorse-Condivise.md) · [Esercizi](esercizi/README.md) · [Modulo successivo: UDP](../05-Socket-UDP/README.md)
