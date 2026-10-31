# Multithreading avanzato: pool, stato condiviso e arresto

Leggi prima la [guida di base](04-Multithreading.md). Le classi sono esempi separati: aggiungi gli import `java.io.*`, `java.net.*`, `java.util.concurrent.*` e salva ogni classe pubblica nel file omonimo. `ClientHandler` è quello della guida base. Il frammento di utilizzo di `CustomExceptionHandler` va inserito in un metodo, non dopo una classe a livello di file.

`Executors.newFixedThreadPool(n)` riutilizza n worker ma usa una coda senza limite. Il numero di thread, il numero di task in coda e il numero di socket aperti sono grandezze distinte. L'esempio GracefulServer limita la coda a 20 task e i worker a 10; una connessione oltre la capacità viene chiusa. `shutdownNow()` tenta l'interruzione dei task: i socket devono essere chiusi esplicitamente per sbloccare le letture.

Riferimenti: [Executors](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Executors.html), [ExecutorService](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ExecutorService.html).

## Capitolo 1: Thread Pool e ExecutorService

### 1.1 Il Problema della Creazione Illimitata di Thread

Creare un thread per ogni connessione può causare problemi:
- Consumo eccessivo di memoria
- Degradazione delle prestazioni
- Risorse di sistema esaurite (Context switch)

### 1.2 Thread Pool con ExecutorService

```java
public class ServerWithThreadPool {
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private static final int PORT = 5000;
    private static final int POOL_SIZE = 10;

    public ServerWithThreadPool() throws IOException {
        serverSocket = new ServerSocket(PORT);
        // Crea un pool di 10 thread riutilizzabili
        threadPool = Executors.newFixedThreadPool(POOL_SIZE);
        System.out.println("Server avviato con thread pool di " + POOL_SIZE);
    }

    public void start() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                // Assegna al thread pool anziché crearne uno nuovo
                threadPool.execute(new ClientHandler(clientSocket));
            } catch (IOException e) {
                System.err.println("Errore: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new ServerWithThreadPool().start();
    }
}
```

### 1.3 Tipi di ExecutorService

```java
// Pool di dimensione fissa
ExecutorService fixed = Executors.newFixedThreadPool(10);

// Pool dinamico (crea thread al bisogno)
ExecutorService cached = Executors.newCachedThreadPool();

// Un solo thread
ExecutorService single = Executors.newSingleThreadExecutor();

// Pool schedulato (per task periodici)
ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(5);
```

---

## Capitolo 2: Sincronizzazione e Risorse Condivise

### 2.1 Il Problema della Race Condition

Quando più thread accedono a una risorsa condivisa:

```java
// PROBLEMA: Accesso non sincronizzato
public class UnsafeCounter {
    private int count = 0;  // Risorsa condivisa

    public void increment() {
        count++;  // Non atomico! Leggi → Modifica → Scrivi
    }

    public int getCount() {
        return count;
    }
}
```

### 2.2 Soluzione con Synchronization

```java
public class SafeCounter {
    private int count = 0;

    // Metodo sincronizzato
    public synchronized void increment() {
        count++;
    }

    public synchronized int getCount() {
        return count;
    }
}
```

### 2.3 Lock Espliciti (ReentrantLock)

```java
import java.util.concurrent.locks.ReentrantLock;

public class CounterWithLock {
    private int count = 0;
    private final ReentrantLock lock = new ReentrantLock();

    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();  // Sempre rilasciare il lock
        }
    }

    public int getCount() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }
}
```

### 2.4 Esempio Pratico: Server con Statistiche Condivise

```java
public class ServerWithStatistics {
    private int totalConnections = 0;
    private int activeConnections = 0;
    private final Object statisticsLock = new Object();
    private ExecutorService threadPool;

    public ServerWithStatistics() {
        threadPool = Executors.newFixedThreadPool(10);
    }

    private void recordConnection() {
        synchronized (statisticsLock) {
            totalConnections++;
            activeConnections++;
        }
    }

    private void recordDisconnection() {
        synchronized (statisticsLock) {
            activeConnections--;
        }
    }

    public void printStatistics() {
        synchronized (statisticsLock) {
            System.out.println("Connessioni totali: " + totalConnections);
            System.out.println("Connessioni attive: " + activeConnections);
        }
    }
}
```

---

## Capitolo 3: Gestione delle Eccezioni

### 3.1 Eccezioni nei Thread

Le eccezioni non catturate in un thread non interrompono il server:

```java
public class ClientHandlerWithExceptionHandling implements Runnable {
    private Socket socket;

    public ClientHandlerWithExceptionHandling(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);

            String request;
            while ((request = in.readLine()) != null) {
                try {
                    String response = processRequest(request);
                    out.println(response);
                } catch (IllegalArgumentException e) {
                    out.println("ERRORE: Richiesta non valida");
                    System.err.println("Input non valido: " + e.getMessage());
                }
            }

        } catch (EOFException e) {
            System.out.println("Client disconnesso normalmente");
        } catch (IOException e) {
            System.err.println("Errore I/O: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Errore nella chiusura del socket: " + e.getMessage());
            }
        }
    }

    private String processRequest(String request) {
        if (request == null || request.isEmpty()) {
            throw new IllegalArgumentException("Richiesta vuota");
        }
        return "Processato: " + request;
    }
}
```

### 3.2 UncaughtExceptionHandler

```java
public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.err.println("Eccezione nel thread " + t.getName() + ": " + e.getMessage());
        e.printStackTrace();
    }
}

// Utilizzo
Thread thread = new Thread(runnable);
thread.setUncaughtExceptionHandler(new CustomExceptionHandler());
thread.start();
```

---

## Capitolo 4: Best Practice e Pattern

### 4.1 Graceful Shutdown

```java
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GracefulServer {
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool = new ThreadPoolExecutor(
        10, 10, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(20));
    private final Set<Socket> clients = ConcurrentHashMap.newKeySet();
    private final Object lifecycle = new Object();
    private volatile boolean running = true;

    public GracefulServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }
    public void start() throws IOException {
        try {
            while (running) {
                Socket socket = serverSocket.accept();
                synchronized (lifecycle) {
                    if (!running) { socket.close(); break; }
                    clients.add(socket);
                    try {
                        threadPool.execute(() -> {
                            try { new ClientHandler(socket).run(); }
                            finally { clients.remove(socket); close(socket); }
                        });
                    } catch (RejectedExecutionException e) {
                        clients.remove(socket);
                        close(socket); // Coda piena: non abbandonare il socket accettato.
                    }
                }
            }
        } catch (SocketException e) {
            if (running) throw e;
        } finally { shutdown(); }
    }
    public void shutdown() {
        synchronized (lifecycle) {
            if (!running) return;
            running = false;
            try { serverSocket.close(); } catch (IOException ignored) { }
            // Sblocca readLine(), anche per i task ancora in coda.
            for (Socket socket : clients) close(socket);
            threadPool.shutdownNow();
        }
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS))
                System.err.println("Alcuni gestori non sono ancora terminati");
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
    private static void close(Socket socket) {
        try { socket.close(); } catch (IOException ignored) { }
    }
}
```

### 4.2 Timeout sui Socket

```java
public class ClientHandlerWithTimeout implements Runnable {
    private Socket socket;
    private static final int SOCKET_TIMEOUT = 30000; // 30 secondi

    public ClientHandlerWithTimeout(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            socket.setSoTimeout(SOCKET_TIMEOUT);
            
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);

            String request;
            while ((request = in.readLine()) != null) {
                String response = processRequest(request);
                out.println(response);
            }

        } catch (SocketTimeoutException e) {
            System.out.println("Timeout: Client inattivo");
        } catch (IOException e) {
            System.err.println("Errore I/O: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String processRequest(String request) {
        return "Echo: " + request;
    }
}
```

### 4.3 Logging Strutturato

```java
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerLogger {
    private static final DateTimeFormatter formatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void logInfo(String message) {
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] [INFO] " + message);
    }

    public static void logError(String message, Exception e) {
        System.err.println("[" + LocalDateTime.now().format(formatter) + "] [ERROR] " + message);
        e.printStackTrace();
    }

    public static void logDebug(String message) {
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] [DEBUG] " + message);
    }
}
```

---

## Capitolo 5: Esercizi Proposti

### Esercizio 1: Estensione della Calcolatrice
Riprendi la calcolatrice proposta nella guida base e aggiungi pool limitato e timeout. Il server:
- Riceve operazioni matematiche (es: "ADD 5 3")
- Esegue il calcolo in un thread separato
- Rimanda il risultato al client
- (OPZIONALE) Usa un thread pool con max 5 thread
- (OPZIONALE) Implementa timeout su socket

### Esercizio 2: Server Chat Multicanale
Sviluppare un server chat con:
- Multiple canali (rooms)
- Broadcast dei messaggi a tutti gli utenti di un canale
- Sincronizzazione corretta dell'accesso ai canali
- Conteggio degli utenti per canale
- Comandi: /join /leave /list

### Esercizio 3: Server di Log Centralizzato
Realizzare un server che:
- Riceve messaggi di log da più client
- Salva i log in un file con timestamp
- Implementa sincronizzazione per l'accesso al file
- Mantiene statistiche (errori, warning, info)
- Permette query dei log (es: errori degli ultimi 5 minuti)

---

## Capitolo 6: Domande di Autovalutazione

### Domanda 1
Quale affermazione è corretta riguardante il multithreading nei socket?

A) Un nuovo thread per ogni client aumenta sempre le prestazioni
B) Il multithreading consente di gestire più client contemporaneamente
C) Non è necessario sincronizzare l'accesso alle risorse condivise
D) ExecutorService è obbligatorio per il multithreading

**Risposta corretta: B**
Il multithreading consente effettivamente di gestire più client in parallelo. L'opzione A è scorretta perché creare infiniti thread degrada le prestazioni. C è errata per motivi di sicurezza dei dati. D è falsa perché si può usare `new Thread()` direttamente.

---

### Domanda 2
Qual è il vantaggio principale di usare ExecutorService rispetto alla creazione diretta di thread?

A) È più veloce
B) Evita il consumo di memoria da troppe istanze di thread
C) Garantisce che non ci saranno eccezioni
D) Permette di usare socket in modo sicuro

**Risposta corretta: B**
ExecutorService gestisce un pool di thread riutilizzabili, evitando la creazione/distruzione continua di thread che consumerebbe risorse. Le altre opzioni sono imprecise o errate.

---

### Domanda 3
In che caso è necessario usare la sincronizzazione nei socket multithreaded?

A) Sempre, per ogni operazione
B) Solo quando più thread accedono alla stessa risorsa non thread-safe
C) Mai, perché i thread sono isolati
D) Solo in caso di errori

**Risposta corretta: B**
La sincronizzazione è necessaria solo quando risorse condivise (contatori, liste, file) sono accedute da più thread. Ogni gestore usa il proprio socket. Se più thread condividono uno stream, devono coordinare la scrittura di messaggi completi.

---

### Domanda 4
Cosa accade se una eccezione non è catturata nel metodo `run()` di un thread?

A) Il server si arresta completamente
B) L'eccezione propaga al thread principale
C) Il thread termina, ma il server continua
D) Viene lanciato un nuovo thread automaticamente

**Risposta corretta: C**
Se un'eccezione non è catturata in `run()`, il thread termina ma il server e gli altri thread continuano. Per gestire eccezioni non catturate si usa `setUncaughtExceptionHandler()`.

---

### Domanda 5
Quale scelta crea un numero potenzialmente illimitato di thread per le connessioni?

A) Usare Executors.newFixedThreadPool(n)
B) Creare un nuovo thread senza limite per ogni client
C) Implementare una coda di connessioni in attesa
D) Usare un pool di dimensione dinamica con limite massimo

**Risposta corretta: B**
Creare thread senza limite è pericoloso e può esaurire le risorse di sistema. Un fixed pool limita i worker ma usa una coda senza limite: non limita da solo le connessioni accettate. Per un limite complessivo servono ammissione controllata, coda limitata e rifiuto con chiusura del socket. Anche una coda generica, senza capacità massima, non costituisce un limite.

---

## Risposte Corrette - Riepilogo

| Domanda | Risposta | Spiegazione |
|---------|----------|-------------|
| 1 | B | Il multithreading consente gestione parallela di client |
| 2 | B | ExecutorService evita consumo eccessivo di memoria |
| 3 | B | La sincronizzazione è necessaria per risorse condivise |
| 4 | C | Un'eccezione non catturata termina il thread, non il server |
| 5 | B | Creare infiniti thread è dannoso per le risorse di sistema |

---

## Conclusione

Il multithreading nei socket è essenziale per costruire server scalabili e responsive. Le chiavi del successo sono:

1. **Usare ExecutorService** per gestire i thread in modo efficiente
2. **Sincronizzare correttamente** l'accesso alle risorse condivise
3. **Gestire le eccezioni** adeguatamente
4. **Implementare timeout e graceful shutdown** per robustezza
5. **Testare** con carichi elevati per verificare la scalabilità


## Navigazione

- [Indice del modulo](README.md)
- [Guida di base e classe ClientHandler](04-Multithreading.md)
- [Esercizi a, b e c](esercizi/README.md)
- [Modulo successivo: UDP](../05-Socket-UDP/README.md)
