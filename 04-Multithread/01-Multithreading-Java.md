# Multithreading in Java

Una guida progressiva per comprendere come eseguire più attività concorrenti, condividere dati in sicurezza e coordinare i thread.

**Prerequisiti:** classi e oggetti, ereditarietà, interfacce, eccezioni e uso essenziale del terminale. Le espressioni lambda vengono introdotte negli esempi.

**Ambiente:** gli esempi di base richiedono un JDK 17 o successivo. La sezione sui thread virtuali richiede un JDK 21 o successivo. Non servono librerie esterne né funzionalità preview.

**Obiettivi:** al termine della guida saprai avviare e attendere thread, riconoscere una race condition, proteggere uno stato condiviso, gestire l'interruzione e affidare attività a un executor.

## Indice

1. [Processi, thread e concorrenza](#1-processi-thread-e-concorrenza)
2. [Il primo programma multithread](#2-il-primo-programma-multithread)
3. [Definire il lavoro: Thread, Runnable e lambda](#3-definire-il-lavoro-thread-runnable-e-lambda)
4. [Ciclo di vita e scheduling](#4-ciclo-di-vita-e-scheduling)
5. [Attendere con sleep e join](#5-attendere-con-sleep-e-join)
6. [Arresto cooperativo e interruzioni](#6-arresto-cooperativo-e-interruzioni)
7. [Dati condivisi e race condition](#7-dati-condivisi-e-race-condition)
8. [Sincronizzazione con synchronized](#8-sincronizzazione-con-synchronized)
9. [Visibilità, volatile e variabili atomiche](#9-visibilità-volatile-e-variabili-atomiche)
10. [Coordinamento: wait, notifyAll e BlockingQueue](#10-coordinamento-wait-notifyall-e-blockingqueue)
11. [Deadlock e altri problemi di avanzamento](#11-deadlock-e-altri-problemi-di-avanzamento)
12. [ExecutorService, Callable e Future](#12-executorservice-callable-e-future)
13. [Thread virtuali: Java 21 e successivi](#13-thread-virtuali-java-21-e-successivi)
14. [Applicazione ai server di rete](#14-applicazione-ai-server-di-rete)
15. [Errori comuni e metodo di verifica](#15-errori-comuni-e-metodo-di-verifica)
16. [Esercizi e domande di ripasso](#16-esercizi-e-domande-di-ripasso)
17. [Riferimenti ufficiali](#17-riferimenti-ufficiali)

## 1. Processi, thread e concorrenza

Un **processo** è un programma in esecuzione con le risorse assegnate dal sistema operativo. Un **thread** è un flusso di esecuzione all'interno di un processo.

Più thread dello stesso processo condividono risorse e oggetti in memoria. Ogni thread mantiene però il proprio stack di chiamate: metodi attivi, parametri e variabili locali delle rispettive invocazioni.

Una variabile locale può contenere il riferimento a un oggetto condiviso: il fatto che il riferimento sia locale non rende privato l'oggetto.

| Aspetto | Processi distinti | Thread dello stesso processo |
| --- | --- | --- |
| Memoria | Spazi di indirizzamento normalmente separati | Oggetti e risorse del processo condivisibili |
| Comunicazione | Per esempio socket, pipe e memoria condivisa esplicita | Per esempio oggetti e code condivise |
| Isolamento | Maggiore isolamento tra applicazioni | Una modifica errata può danneggiare lo stato comune |

### Concorrenza e parallelismo

La **concorrenza** permette a più attività di avanzare nello stesso intervallo di tempo. Anche un singolo core può alternarne l'esecuzione.

Il **parallelismo** consiste nell'esecuzione simultanea di attività su più unità di calcolo. Un programma concorrente può sfruttarlo, ma non è garantito che lo faccia in ogni momento.

```text
Concorrenza su un core:   A A | B B | A | B B
Parallelismo su due core:
  core 1:                A A A A A
  core 2:                B B B B B
```

I thread sono utili per gestire richieste indipendenti, svolgere calcoli divisibili e continuare altre attività durante attese di input/output. Introducono però costi di gestione, memoria e coordinamento. Aumentarne il numero non garantisce un programma più veloce.

Riferimento: [Oracle, Processes and Threads](https://docs.oracle.com/javase/tutorial/essential/concurrency/procthread.html).

## 2. Il primo programma multithread

Il metodo `main` viene eseguito da un thread. Possiamo ottenere il thread corrente con `Thread.currentThread()` e avviarne un altro mediante `start()`.

**File completo: `PrimoThread.java`**

```java
/** Avvia un lavoratore e lo attende con join; l’ordine relativo delle stampe può variare. */
public class PrimoThread {
    public static void main(String[] args) throws InterruptedException {
        Runnable lavoro = () -> {
            for (int i = 1; i <= 3; i++) {
                System.out.println(Thread.currentThread().getName() + ": " + i);
            }
        };

        Thread lavoratore = new Thread(lavoro, "lavoratore");
        // start crea il nuovo flusso: il chiamante continua subito.
        lavoratore.start();

        for (int i = 1; i <= 3; i++) {
            System.out.println(Thread.currentThread().getName() + ": " + i);
        }

        // Dopo join il lavoro è terminato e i suoi risultati sono visibili.
        lavoratore.join();
        System.out.println("Attività completate");
    }
}
```

Per eseguire ogni esempio completo, salva il blocco nel file indicato, quindi compila e avvia la classe. Per questo esempio:

```bash
javac -encoding UTF-8 --release 17 PrimoThread.java
java PrimoThread
```

Una possibile esecuzione è:

```text
main: 1
lavoratore: 1
main: 2
main: 3
lavoratore: 2
lavoratore: 3
Attività completate
```

Le righe dei due cicli possono alternarsi diversamente. Ciascun ciclo mantiene il proprio ordine, ma il programma non stabilisce un ordine tra le stampe dei due thread. La riga finale arriva dopo la conclusione di entrambi i cicli, perché `main` esegue `join()` sul lavoratore.

### La differenza tra start() e run()

| Chiamata | Effetto su un normale Thread creato con un Runnable |
| --- | --- |
| `lavoratore.start()` | Avvia un nuovo thread che eseguirà il compito |
| `lavoratore.run()` | Esegue il compito nel thread chiamante, come una normale chiamata di metodo |

Un oggetto `Thread` può essere avviato **una sola volta**. Per ripetere un'attività dopo la sua conclusione occorre creare un nuovo thread o sottoporla a un executor.

Riferimento: [API Thread: start e run](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Thread.html#start()).

## 3. Definire il lavoro: Thread, Runnable e lambda

### 3.1 Estendere Thread

Una sottoclasse può ridefinire `run()` per descrivere l'attività.

**File completo: `SalutoThread.java`**

```java
/** Sottoclasse di Thread: il compito è nel metodo run, avviato con start. */
public class SalutoThread extends Thread {
    public SalutoThread(String nome) {
        super(nome);
    }

    @Override
    public void run() {
        System.out.println("Saluto da " + getName());
    }

    public static void main(String[] args) throws InterruptedException {
        SalutoThread thread = new SalutoThread("saluto");
        thread.start();
        thread.join();
    }
}
```

Questo approccio lega l'attività alla classe che rappresenta il thread e occupa l'unica possibilità di estensione di classe prevista da Java.

### 3.2 Implementare Runnable

`Runnable` rappresenta un compito senza risultato di ritorno. Il suo metodo è `void run()`.

**File completo: `DemoRunnable.java`**

```java
/** Separa il compito Runnable dal Thread che lo esegue. */
public class DemoRunnable {
    static class Saluto implements Runnable {
        private final String messaggio;

        Saluto(String messaggio) {
            this.messaggio = messaggio;
        }

        @Override
        public void run() {
            System.out.println(messaggio);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Runnable compito = new Saluto("Esecuzione di un Runnable");
        Thread thread = new Thread(compito, "saluto-runnable");
        thread.start();
        thread.join();
    }
}
```

Separare compito e thread consente di passare lo stesso tipo di attività anche a un executor. Se più thread utilizzano la stessa istanza di `Runnable`, gli eventuali campi modificabili di quell'istanza diventano stato condiviso da proteggere.

### 3.3 Usare una lambda

Poiché `Runnable` è un'interfaccia funzionale, una lambda può esprimere il suo metodo `run()`:

```java
// Frammento da inserire in un metodo.
Runnable compito = () -> System.out.println("Compito espresso con una lambda");
Thread thread = new Thread(compito, "lambda");
thread.start();
```

`() -> ...` significa: nessun parametro e un corpo da eseguire. Per più istruzioni si usano le parentesi graffe, come nel primo esempio.

Le variabili locali catturate da una lambda devono essere `final` o **effettivamente final**, cioè non riassegnate. Questo vincolo riguarda la variabile: non rende immutabile l'oggetto a cui essa si riferisce.

Riferimento per i primi due approcci: [Oracle, Defining and Starting a Thread](https://docs.oracle.com/javase/tutorial/essential/concurrency/runthread.html).

## 4. Ciclo di vita e scheduling

Lo **scheduler** decide quali thread pronti possono avanzare. Il programma non deve dipendere da una particolare alternanza, dalla velocità della macchina o dalla priorità assegnata ai thread.

Gli stati definiti da `Thread.State` sono:

| Stato | Significato |
| --- | --- |
| `NEW` | Oggetto creato, ma `start()` non ancora chiamato |
| `RUNNABLE` | Thread in esecuzione nella JVM o pronto a ottenere tempo di CPU |
| `BLOCKED` | Attesa dell'acquisizione di un monitor, per esempio entrando in `synchronized` |
| `WAITING` | Attesa senza scadenza, per esempio con `wait()` o `join()` senza timeout |
| `TIMED_WAITING` | Attesa con limite temporale, per esempio con `sleep()` o `join(timeout)` |
| `TERMINATED` | Esecuzione terminata, normalmente o per un'eccezione non gestita |

```text
NEW --start()--> RUNNABLE --fine di run()--> TERMINATED
                    |
                    +--> BLOCKED / WAITING / TIMED_WAITING
                              |
                              +--> RUNNABLE quando può riprendere
```

Lo schema riassume i passaggi principali: per esempio, un thread risvegliato da `wait()` potrebbe dover riacquisire il monitor e passare per `BLOCKED`.

`getState()` restituisce una fotografia momentanea, utile per diagnosi. Lo stato può cambiare subito dopo: non va usato come meccanismo di coordinamento. Inoltre `BLOCKED` indica specificamente l'attesa di un monitor, non qualsiasi operazione di input/output bloccante.

Riferimento: [API Thread.State](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Thread.State.html).

### Thread daemon

La JVM può avviare la chiusura quando non rimangono thread non daemon in esecuzione. I thread daemon non mantengono da soli attiva l'applicazione: non bisogna affidare loro un salvataggio essenziale contando sul completamento automatico. Per un thread di piattaforma lo stato daemon va configurato prima di `start()`.

## 5. Attendere con sleep e join

### sleep: sospendere il thread corrente

`Thread.sleep(500)` sospende il thread che esegue la chiamata per circa 500 millisecondi, salvo interruzione. La ripresa effettiva dipende anche dallo scheduler.

Il metodo è statico: scrivere `Thread.sleep(...)` rende chiaro che non si sta sospendendo un altro thread. Se il chiamante possiede un monitor, `sleep()` **non lo rilascia**.

Una pausa serve, per esempio, a simulare un'attività lenta in un esercizio. Non dimostra che un altro thread abbia concluso il proprio lavoro.

Riferimento: [Oracle, Pausing Execution with Sleep](https://docs.oracle.com/javase/tutorial/essential/concurrency/sleep.html).

### join: attendere la conclusione di un thread

`t.join()` fa attendere il chiamante finché `t` termina. Non avvia `t`: l'avvio deve essere già stato richiesto con `start()`.

Con più lavoratori, avviali tutti prima di attenderli se vuoi che possano procedere contemporaneamente. La sequenza `t1.start(); t1.join(); t2.start(); t2.join();` serializza le due attività.

`join(1000)` può tornare anche se il thread non è terminato, dopo l'attesa prevista. Per distinguere questo caso bisogna verificare `isAlive()` e decidere come gestirlo; il timeout non arresta il lavoratore.

Sia `sleep()` sia `join()` possono lanciare `InterruptedException`.

Riferimento: [Oracle, Joins](https://docs.oracle.com/javase/tutorial/essential/concurrency/join.html).

## 6. Arresto cooperativo e interruzioni

L'arresto deve permettere al lavoratore di chiudere risorse e lasciare coerenti i dati. `interrupt()` invia una richiesta che il codice del thread deve gestire: non forza la terminazione in un punto arbitrario.

**File completo: `ArrestoCooperativo.java`**

```java
/** Richiede l’interruzione dopo il segnale di avvio e attende il cleanup. */
import java.util.concurrent.CountDownLatch;

public class ArrestoCooperativo {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch avviato = new CountDownLatch(1);

        Thread lavoratore = new Thread(() -> {
            try {
                avviato.countDown();
                while (!Thread.currentThread().isInterrupted()) {
                    System.out.println("Lavoro in corso");
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                System.out.println("Pulizia delle risorse e uscita");
            }
        }, "lavoratore-interrompibile");

        // start crea il nuovo flusso: il chiamante continua subito.
        lavoratore.start();
        avviato.await();
        lavoratore.interrupt();
        // Dopo join il lavoro è terminato e i suoi risultati sono visibili.
        lavoratore.join();
        System.out.println("Lavoratore terminato");
    }
}
```

`CountDownLatch` è un segnale di coordinamento: `await()` attende che il conteggio arrivi a zero; `countDown()` lo decrementa. Qui assicura che il lavoratore abbia raggiunto l'inizio dell'attività prima della richiesta di interruzione. Può quindi non comparire nessuna riga «Lavoro in corso»: l'interruzione potrebbe essere osservata prima del primo ciclo.

Se il lavoratore è dentro `sleep()`, la richiesta provoca `InterruptedException`. Se sta eseguendo il controllo del ciclo, può osservare il flag e uscire senza eccezione.

| Operazione | Comportamento |
| --- | --- |
| `t.interrupt()` | Richiede l'interruzione del thread `t` |
| `t.isInterrupted()` | Legge lo stato di interruzione di `t` senza azzerarlo |
| `Thread.interrupted()` | Legge e azzera lo stato di interruzione del thread corrente |

Quando un'attesa come `sleep()` lancia `InterruptedException`, il flag viene azzerato. In un metodo che può dichiararla, propaga l'eccezione. In un `Runnable`, che non può dichiarare eccezioni checked in `run()`, normalmente ripristina il flag ed esci, come nell'esempio. Non catturare l'eccezione per poi proseguire il lavoro ignorando la richiesta.

Un ciclo di calcolo lungo deve controllare periodicamente l'interruzione. Per le operazioni di rete occorre considerare anche timeout e chiusura dei socket: il solo `interrupt()` non sblocca universalmente qualunque API di I/O.

Non usare `Thread.stop()`, `suspend()` o `resume()`: sono API obsolete e non costituiscono un modello sicuro di gestione del ciclo di vita.

Riferimento: [Oracle, Interrupts](https://docs.oracle.com/javase/tutorial/essential/concurrency/interrupt.html).

## 7. Dati condivisi e race condition

Una **race condition** si verifica quando il risultato dipende dall'ordine temporale di operazioni concorrenti non coordinate correttamente.

L'istruzione `valore++` sembra un'operazione unica, ma comprende lettura, incremento e scrittura. Due thread possono leggere entrambi `10` e scrivere entrambi `11`: uno degli incrementi viene perso.

| Passaggio | Thread A | Thread B |
| --- | --- | --- |
| 1 | Legge 10 | |
| 2 | | Legge 10 |
| 3 | Calcola e scrive 11 | |
| 4 | | Calcola e scrive 11 |

**File completo, volutamente errato: `ContatoreNonSicuro.java`**

```java
/** Esempio volutamente errato: due thread incrementano lo stesso campo senza lock. */
public class ContatoreNonSicuro {
    static class Contatore {
        private int valore;

        void incrementa() {
            // Lettura, incremento e scrittura: qui manca volutamente il monitor comune.
            valore++;
        }

        int leggi() {
            return valore;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Contatore contatore = new Contatore();
        Runnable lavoro = () -> {
            for (int i = 0; i < 100_000; i++) {
                contatore.incrementa();
            }
        };

        Thread primo = new Thread(lavoro, "primo");
        Thread secondo = new Thread(lavoro, "secondo");
        primo.start();
        secondo.start();
        primo.join();
        secondo.join();

        System.out.println("Atteso: 200000");
        System.out.println("Ottenuto: " + contatore.leggi());
    }
}
```

Il risultato può essere inferiore a `200000`, ma può anche coincidere con quello atteso. Un'esecuzione corretta non dimostra l'assenza del difetto. I due `join()` attendono i lavoratori e rendono visibili i loro effetti al chiamante, ma non rendono atomici gli incrementi che i lavoratori hanno eseguito tra loro.

Riferimenti: [Oracle, Thread Interference](https://docs.oracle.com/javase/tutorial/essential/concurrency/interfere.html) e [Memory Consistency Errors](https://docs.oracle.com/javase/tutorial/essential/concurrency/memconsist.html).

## 8. Sincronizzazione con synchronized

Una **sezione critica** è una parte di codice che accede a uno stato condiviso e deve essere protetta da accessi concorrenti incompatibili.

Ogni oggetto Java possiede un monitor, utilizzabile come lock. `synchronized` garantisce l'esclusione reciproca tra sezioni che acquisiscono **lo stesso monitor** e fornisce garanzie di visibilità della memoria.

**File completo: `ContatoreSicuro.java`**

```java
/** Lo stesso monitor protegge ogni incremento e ogni lettura del contatore. */
public class ContatoreSicuro {
    static class Contatore {
        private int valore;

        synchronized void incrementa() {
            // Lettura, incremento e scrittura: serve il monitor comune per renderli atomici.
            valore++;
        }

        synchronized int leggi() {
            return valore;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Contatore contatore = new Contatore();
        Runnable lavoro = () -> {
            for (int i = 0; i < 100_000; i++) {
                contatore.incrementa();
            }
        };

        Thread primo = new Thread(lavoro, "primo");
        Thread secondo = new Thread(lavoro, "secondo");
        primo.start();
        secondo.start();
        primo.join();
        secondo.join();

        System.out.println("Risultato: " + contatore.leggi());
    }
}
```

Il risultato è `200000`: ogni incremento avviene mentre il thread possiede il monitor dell'istanza condivisa di `Contatore`. Anche il metodo di lettura usa quel monitor, così la classe rimane utilizzabile per letture concorrenti agli incrementi.

Riferimento: [Oracle, Synchronized Methods](https://docs.oracle.com/javase/tutorial/essential/concurrency/syncmeth.html).

### Quale oggetto viene bloccato?

| Forma | Monitor acquisito |
| --- | --- |
| Metodo di istanza `synchronized` | `this` |
| Metodo `static synchronized` | L'oggetto `Class` della classe che dichiara il metodo |
| Blocco `synchronized (lock)` | L'oggetto referenziato da `lock` |

Un lock privato permette di evitare che codice esterno usi accidentalmente lo stesso monitor.

**Classe completa senza `main`, da salvare in `ContatoreConLock.java`:**

```java
/** Il lock privato è unico e condiviso da tutti gli accessi al campo. */
public class ContatoreConLock {
    private final Object lock = new Object();
    private int valore;

    public void incrementa() {
        synchronized (lock) {
            // Lettura, incremento e scrittura: serve il monitor comune per renderli atomici.
            valore++;
        }
    }

    public int leggi() {
        synchronized (lock) {
            return valore;
        }
    }
}
```

Se ogni thread crea un proprio lock, non c'è esclusione reciproca tra i thread. Inoltre un metodo sincronizzato non impedisce l'accesso tramite eventuali metodi non sincronizzati: tutti gli accessi concorrenti allo stato protetto devono rispettare lo stesso protocollo.

Il monitor viene rilasciato all'uscita dal blocco, anche in caso di eccezione. I monitor sono **rientranti**: un thread che possiede un monitor può acquisirlo nuovamente, per esempio chiamando un altro metodo sincronizzato sullo stesso oggetto.

Mantieni le sezioni critiche brevi e limita le operazioni lente al loro interno, soprattutto le attese di rete.

Riferimento: [Oracle, Intrinsic Locks and Synchronization](https://docs.oracle.com/javase/tutorial/essential/concurrency/locksync.html).

## 9. Visibilità, volatile e variabili atomiche

La correttezza concorrente richiede di distinguere tre aspetti:

- **Atomicità:** un'operazione non può essere osservata o interferita come una successione di passi separati.
- **Visibilità:** le modifiche effettuate da un thread vengono osservate dagli altri secondo le garanzie previste.
- **Ordinamento:** le operazioni rispettano le relazioni di ordine necessarie al programma, anche in presenza di ottimizzazioni.

Il Java Memory Model definisce queste garanzie mediante relazioni **happens-before**. Tra le relazioni utili: le azioni precedenti a `start()` sono ordinate prima delle azioni del thread avviato; la terminazione di un thread è ordinata prima del ritorno con successo dal suo `join()`; il rilascio di un monitor precede una successiva acquisizione dello stesso monitor.

`volatile` è utile per un flag condiviso: una scrittura sul campo stabilisce una relazione di visibilità con le successive letture di quel campo. Non rende atomiche operazioni composte come `contatore++` o «controlla e poi modifica».

**Classe completa senza `main`, da salvare in `SegnaleArresto.java`:**

```java
/** Un flag volatile rende visibile la richiesta, ma non risveglia attese bloccanti. */
public class SegnaleArresto {
    private volatile boolean arrestoRichiesto;

    public void richiediArresto() {
        arrestoRichiesto = true;
    }

    public boolean isArrestoRichiesto() {
        return arrestoRichiesto;
    }
}
```

Un lavoratore può consultare il flag tra due unità di lavoro. Il flag non risveglia un thread fermo in un'attesa bloccante; per questo l'interruzione è spesso più adatta a un'attività cancellabile.

Riferimento: [Java Language Specification, capitolo 17](https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html).

### AtomicInteger

Per un contatore indipendente, `AtomicInteger` offre operazioni atomiche già pronte.

**Classe completa senza `main`, da salvare in `ContatoreAtomico.java`:**

```java
/** Una singola operazione atomica realizza lettura, incremento e scrittura. */
import java.util.concurrent.atomic.AtomicInteger;

public class ContatoreAtomico {
    private final AtomicInteger valore = new AtomicInteger();

    public void incrementa() {
        valore.incrementAndGet();
    }

    public int leggi() {
        return valore.get();
    }
}
```

`incrementAndGet()` incrementa e restituisce il nuovo valore; `getAndIncrement()` restituisce quello precedente all'incremento. Non sostituirli con `set(get() + 1)`, che spezza l'operazione in passi separati.

Due variabili atomiche distinte non costituiscono automaticamente una transazione: se bisogna mantenere una relazione tra più campi, può essere necessario un lock comune.

Riferimento: [API AtomicInteger](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/atomic/AtomicInteger.html).

## 10. Coordinamento: wait, notifyAll e BlockingQueue

Proteggere i dati risponde alla domanda «chi può modificarli?». Coordinare i thread risponde anche a «quando un'attività può proseguire?».

### 10.1 Attendere una condizione con un monitor

Un consumatore potrebbe dover aspettare finché un produttore rende disponibile un valore. Un ciclo che controlla continuamente una variabile spreca CPU; `wait()` permette di sospendersi rilasciando il monitor su cui viene invocato.

**Classe completa senza `main`, da salvare in `Casella.java`:**

```java
/** Produttore e consumatore attendono una condizione sul medesimo monitor. */
public class Casella {
    private String valore;
    private boolean piena;

    public synchronized void inserisci(String nuovoValore)
            throws InterruptedException {
        while (piena) {
            // Rilascia il monitor della casella; il while ricontrolla la condizione al risveglio.
            wait();
        }
        valore = nuovoValore;
        piena = true;
        notifyAll(); // La condizione è cambiata; i risvegliati devono riacquisire il monitor.
    }

    public synchronized String preleva() throws InterruptedException {
        while (!piena) {
            // Rilascia il monitor della casella; il while ricontrolla la condizione al risveglio.
            wait();
        }
        String risultato = valore;
        valore = null;
        piena = false;
        notifyAll(); // La condizione è cambiata; i risvegliati devono riacquisire il monitor.
        return risultato;
    }
}
```

Questa casella contiene al massimo un valore. Tutti i partecipanti devono usare la stessa istanza.

La condizione va ricontrollata con `while`, poiché sono possibili risvegli spuri e un altro thread potrebbe aver modificato lo stato prima della riacquisizione del monitor.

Riferimento: [Oracle, Guarded Blocks](https://docs.oracle.com/javase/tutorial/essential/concurrency/guardmeth.html).

Le chiamate `wait()`, `notify()` e `notifyAll()` richiedono il possesso del monitor dell'oggetto su cui vengono invocate; altrimenti lanciano `IllegalMonitorStateException`. `notify()` risveglia un solo thread in attesa, senza permettere di scegliere quale. `notifyAll()` li risveglia tutti, ma ognuno deve ancora riacquisire il monitor e ricontrollare la propria condizione. La notifica non rilascia immediatamente il lock.

| Operazione | Effetto sul monitor posseduto |
| --- | --- |
| `Thread.sleep(...)` | Non rilascia i monitor |
| `oggetto.wait()` | Rilascia il monitor di `oggetto` durante l'attesa; lo riacquisisce prima del ritorno |

Se il thread possiede altri monitor, `wait()` non rilascia anche quelli.

Riferimento: [API Object: wait e notifiche](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html#wait()).

### 10.2 Produttore e consumatore con BlockingQueue

Per un'applicazione, una coda concorrente evita di riscrivere molti dettagli del coordinamento. `ArrayBlockingQueue` ha capacità fissata: `put()` attende se la coda è piena, mentre `take()` attende se è vuota.

**File completo: `ProduttoreConsumatore.java`**

```java
/** Coda limitata, un produttore e un consumatore, terminazione con un valore riservato. */
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ProduttoreConsumatore {
    private static final int FINE = -1;

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Integer> coda = new ArrayBlockingQueue<>(2);

        Thread produttore = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    coda.put(i); // Con coda piena il produttore attende senza accumulare dati.
                }
                coda.put(FINE);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "produttore");

        Thread consumatore = new Thread(() -> {
            try {
                while (true) {
                    int valore = coda.take(); // Attende un elemento; nessuna attesa attiva.
                    if (valore == FINE) {
                        break;
                    }
                    System.out.println("Consumato: " + valore);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "consumatore");

        produttore.start();
        consumatore.start();
        produttore.join();
        consumatore.join();
    }
}
```

L'esempio stampa i valori da `1` a `5` in ordine e termina. `FINE` è un valore riservato, chiamato anche *poison pill*: segnala al consumatore che non arriveranno altri elementi. Qui il dominio dei dati comprende solo numeri positivi, perciò `-1` non è ambiguo.

Il protocollo è progettato per un produttore e un consumatore nel percorso normale. Con più consumatori occorre prevedere un segnale per ciascuno; con più produttori occorre stabilire chi invia i segnali quando tutti hanno finito. Se una parte fallisce o viene interrotta prima di inviare il segnale, un supervisore deve cancellare anche l'altra per evitare un'attesa infinita.

La capacità limitata introduce **contropressione**: un produttore troppo veloce deve attendere invece di accumulare dati senza limite. La coda protegge le proprie operazioni, ma non rende sicure successive modifiche concorrenti agli oggetti inseriti.

Riferimento: [API BlockingQueue](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/BlockingQueue.html).

## 11. Deadlock e altri problemi di avanzamento

Un **deadlock** è una situazione in cui più thread attendono risorse possedute l'uno dall'altro e nessuno riesce a proseguire.

```text
Thread A: possiede lock1, attende lock2
Thread B: possiede lock2, attende lock1
```

Per prevenire il ciclo, stabilisci un ordine comune nell'acquisizione dei lock. Se ogni percorso acquisisce prima `lock1` e poi `lock2`, non può formarsi quel particolare ciclo. Riduci inoltre i lock annidati ed evita chiamate a codice esterno mentre possiedi un lock.

Una sezione `synchronized` non diventa cancellabile solo perché il thread in attesa riceve un'interruzione. Quando occorrono acquisizioni interrompibili o con timeout, si può valutare `ReentrantLock`: `lockInterruptibly()` e `tryLock(timeout, unit)` offrono queste possibilità. Con un lock esplicito il rilascio va garantito in `finally` dopo l'acquisizione riuscita.

Altri problemi sono la **starvation**, quando un'attività rimane continuamente privata delle risorse necessarie, e il **livelock**, quando le attività reagiscono ripetutamente l'una all'altra senza avanzare nel lavoro utile.

Riferimento sul deadlock: [Oracle, Deadlock](https://docs.oracle.com/javase/tutorial/essential/concurrency/deadlock.html).

## 12. ExecutorService, Callable e Future

Creare manualmente un thread per ogni piccolo compito rende più difficile controllare risorse e terminazione. Un `ExecutorService` separa la descrizione dei compiti dalla gestione della loro esecuzione.

| Tipo | Ruolo |
| --- | --- |
| `Runnable` | Compito senza risultato, metodo `void run()` |
| `Callable<T>` | Compito che restituisce un `T` e può lanciare eccezioni checked |
| `Future<T>` | Rappresenta il completamento e l'eventuale risultato di un compito |
| `ExecutorService` | Accetta compiti e gestisce il ciclo di vita dell'esecuzione |

**File completo: `RisultatiExecutor.java`**

```java
/** Sottomette entrambi i calcoli prima di attenderli e chiude sempre l’executor. */
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class RisultatiExecutor {
    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Integer> primoCalcolo = () -> somma(1, 500);
            Callable<Integer> secondoCalcolo = () -> somma(501, 1000);

            Future<Integer> primo = pool.submit(primoCalcolo);
            Future<Integer> secondo = pool.submit(secondoCalcolo);

            int totale = primo.get() + secondo.get();
            System.out.println("Somma da 1 a 1000: " + totale);
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            System.err.println("Calcolo fallito: " + e.getCause());
        } finally {
            pool.shutdown(); // Rifiuta nuovi compiti, lasciando completare quelli accettati.
            try {
                if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                    if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                        System.err.println("Alcune attività non sono terminate");
                    }
                }
            } catch (InterruptedException e) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private static int somma(int inizio, int fine) throws InterruptedException {
        int risultato = 0;
        for (int i = inizio; i <= fine; i++) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Calcolo interrotto");
            }
            risultato += i;
        }
        return risultato;
    }
}
```

Il risultato è `500500`. I due compiti vengono sottoposti prima delle chiamate a `get()`, così entrambi possono avanzare. `get()` può attendere; se il compito fallisce, lancia `ExecutionException`, la cui causa contiene l'errore originario. I cinque secondi nell'esempio riguardano l'attesa durante la chiusura: non impongono un timeout alle precedenti chiamate a `get()`.

`shutdown()` rifiuta nuovi compiti e lascia completare quelli accettati. `awaitTermination()` permette di attendere la terminazione dopo la richiesta di chiusura. `shutdownNow()` tenta di interrompere i compiti attivi e restituisce quelli mai iniziati: non garantisce l'arresto se il lavoro ignora l'interruzione. Nell'esempio i compiti sono calcoli senza risorse esterne; compiti mai avviati che possiedono risorse richiederebbero anche una pulizia esplicita.

Riferimento: [API ExecutorService](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ExecutorService.html).

### Timeout, cancellazione e dimensionamento

- `future.get(timeout, unit)` limita l'attesa del chiamante; alla scadenza lancia `TimeoutException`, senza cancellare automaticamente il compito.
- `future.cancel(true)` tenta la cancellazione e può richiedere l'interruzione del thread che esegue il compito; il codice deve collaborare.
- Con `submit()`, gli errori del compito sono conservati nel `Future`: ignorarlo può far perdere informazioni sui fallimenti.
- `newFixedThreadPool(n)` limita i lavoratori, ma usa una coda non limitata: troppe richieste possono accumularsi in memoria.
- Un `ThreadPoolExecutor` configurato con coda limitata richiede una politica per i compiti rifiutati. Occorre decidere se rifiutare, rallentare il chiamante o applicare un'altra strategia coerente con l'applicazione.

Per calcoli intensivi, parti da un numero di lavoratori proporzionato ai processori disponibili e misura. Per attività con molte attese, considera anche limiti delle risorse esterne, timeout e thread virtuali. Non esiste un numero ottimale universale.

Riferimenti: [API Future](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Future.html) e [API ThreadPoolExecutor](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ThreadPoolExecutor.html).

## 13. Thread virtuali: Java 21 e successivi

I thread creati con `new Thread(...)` sono **thread di piattaforma**, normalmente associati a thread del sistema operativo. I **thread virtuali** sono gestiti dalla JVM e consentono molte attività concorrenti con un costo per thread generalmente inferiore.

Sono particolarmente utili quando le attività passano molto tempo in attesa di I/O. Non aggiungono core e non accelerano automaticamente un calcolo che occupa continuamente la CPU.

**File completo, JDK 21+: `ThreadVirtuali.java`**

```java
/** JDK 21+: un thread virtuale per compito; close attende la conclusione dei task. */
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadVirtuali {
    public static void main(String[] args) throws Exception {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<String> primo = executor.submit(() -> {
                Thread.sleep(100);
                return "Primo compito completato";
            });
            Future<String> secondo = executor.submit(() -> {
                Thread.sleep(100);
                return "Secondo compito completato";
            });

            System.out.println(primo.get());
            System.out.println(secondo.get());
        }
    }
}
```

```bash
javac -encoding UTF-8 --release 21 ThreadVirtuali.java
java ThreadVirtuali
```

L'executor crea un thread virtuale per compito. Il `try-with-resources`, disponibile per `ExecutorService` da Java 19, ne chiude il ciclo di vita attendendo la conclusione delle attività. Qui `sleep()` simula soltanto un'attesa.

I thread virtuali sono daemon: quando vengono avviati direttamente, per esempio con `Thread.startVirtualThread(...)`, bisogna prevedere un'attesa come `join()` se il loro completamento deve precedere l'uscita dell'applicazione.

Anche con i thread virtuali restano necessari sincronizzazione dei dati, cancellazione e limiti alle risorse esterne. Per esempio, migliaia di thread non autorizzano migliaia di connessioni simultanee verso un database: un semaforo o un pool di connessioni può porre un limite separato. Non serve costruire un pool di thread virtuali da riutilizzare: si crea un thread per attività.

Riferimento: [Oracle, Virtual Threads](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html).

## 14. Applicazione ai server di rete

In un server TCP sequenziale, un client lento può occupare il flusso di esecuzione mentre altri client attendono. Una struttura concorrente assegna la gestione delle connessioni a lavoratori separati.

```text
Thread di accettazione
    |
    +-- accept() --> connessione A --> gestore A
    +-- accept() --> connessione B --> gestore B
    +-- accept() --> connessione C --> gestore C
```

Ogni gestore dovrebbe possedere il socket della propria connessione e chiuderlo al termine, preferibilmente mediante `try-with-resources`. Se i gestori condividono un registro di utenti o un contatore, quello stato richiede una strategia di concorrenza distinta.

| Decisione | Motivo |
| --- | --- |
| Limitare richieste o connessioni ammesse | Evitare l'esaurimento di memoria, descrittori e risorse esterne |
| Chiudere il socket se il compito viene rifiutato | Il gestore non inizierà e non potrà chiuderlo |
| Gestire anche i socket di compiti accodati mai eseguiti | La cancellazione della coda non chiude le risorse applicative |
| Definire timeout e limiti del protocollo | Evitare che un client occupi risorse indefinitamente |
| Coordinare lo spegnimento | Smettere di accettare e concludere o cancellare i gestori |

`Socket.setSoTimeout(...)` imposta il timeout delle letture bloccanti; non è un timeout generale di tutta la connessione né delle scritture. La chiusura del socket può sbloccare operazioni in attesa con un'eccezione. Per `ServerSocket.accept()` occorre gestire separatamente il socket di ascolto, per esempio chiudendolo durante lo spegnimento.

Riferimenti: [API Socket](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/Socket.html) e [API ServerSocket](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/ServerSocket.html).

## 15. Errori comuni e metodo di verifica

| Errore | Correzione |
| --- | --- |
| Chiamare `run()` per avviare concorrenza | Chiamare `start()` |
| Avviare due volte lo stesso Thread | Creare un nuovo thread o usare un executor |
| Usare `sleep()` per aspettare un risultato | Usare `join()`, `Future.get()` o un segnale |
| Supporre che `volatile int` renda sicuro `++` | Usare un'operazione atomica o un lock |
| Proteggere lo stesso dato con lock diversi | Stabilire un unico protocollo di accesso |
| Scrivere `if` prima di `wait()` | Ricontrollare la condizione con `while` |
| Ignorare `InterruptedException` | Propagare o ripristinare l'interruzione e uscire |
| Non chiudere un executor | Definire sempre il suo ciclo di vita |
| Attendere sottocompiti nello stesso pool saturo | Evitare dipendenze che occupano tutti i lavoratori |
| Considerare sicuro il codice dopo una sola esecuzione | Ragionare sugli accessi condivisi e variare le esecuzioni |

Per analizzare un programma:

1. Elenca gli oggetti condivisi e individua quali possono cambiare.
2. Per ogni dato modificabile, indica chi lo legge, chi lo scrive e con quale protezione.
3. Individua le attese e verifica quale evento permette di uscirne.
4. Definisci chi richiede l'arresto e chi chiude ciascuna risorsa.
5. Verifica proprietà osservabili: totale atteso, elaborazione una sola volta, terminazione e gestione degli errori.

Le stampe aiutano a seguire un'esecuzione, ma ne alterano i tempi e possono nascondere un difetto. Evita test che richiedano un ordine casuale di stampe o una durata esatta. Per riprodurre un incontro tra thread, usa segnali come latch e barriere invece di pause arbitrarie.

Un thread dump permette di osservare stack e attese; gli strumenti di diagnosi possono aiutare a riconoscere blocchi e contese. Sono indizi da collegare al codice, non una prova generale di correttezza.

## 16. Esercizi e domande di ripasso

### Esercizio 1 — Due sequenze concorrenti

Crea due `Runnable`: uno stampa i numeri da 1 a 10, l'altro le lettere da A a J. Dai un nome ai thread, avviali entrambi e attendili prima di stampare «Fine».

**Verifica:** ogni sequenza mantiene il proprio ordine; la disposizione relativa delle due sequenze può cambiare; «Fine» è sempre l'ultima riga.

### Esercizio 2 — Contatore condiviso

Adatta `ContatoreNonSicuro` a quattro thread, ciascuno con 100000 incrementi. Realizza poi una versione con `synchronized` e una con `AtomicInteger`.

**Verifica:** le versioni corrette devono restituire `400000`. Non pretendere che la versione errata fallisca a ogni esecuzione.

### Esercizio 3 — Lavoratore interrompibile

Avvia un thread che alterna un breve lavoro e un'attesa. Usa un `CountDownLatch` per sapere quando è pronto, richiedi l'interruzione e attendi l'uscita.

**Verifica:** il lavoratore termina, esegue la pulizia e non continua a lavorare dopo aver riconosciuto la richiesta.

### Esercizio 4 — Coda con più consumatori

Estendi `ProduttoreConsumatore` a due consumatori. Il produttore inserisce i valori da 1 a 20 e, alla fine, un segnale di terminazione per ogni consumatore.

**Verifica:** ciascun numero viene elaborato una sola volta, la somma dei valori elaborati è `210` ed entrambi i consumatori terminano. La somma condivisa deve essere protetta oppure calcolata da risultati separati.

### Esercizio 5 — Risultati con un executor

Dividi la somma da 1 a 1000000 in quattro intervalli. Usa `Callable<Long>` e combina i risultati ottenuti dai `Future`. Usa `long` anche per gli accumulatori parziali.

**Verifica:** il totale è `500000500000`; l'executor termina anche quando un compito fallisce.

### Esercizio 6 — Progetto di un echo server concorrente

Progetta un server che accetta più client, legge righe e le rimanda al mittente. Scegli un pool con coda limitata oppure un executor di thread virtuali con un limite separato alle connessioni ammesse.

**Verifica:** un client in attesa non impedisce il servizio agli altri; i socket vengono chiusi anche in caso di rifiuto o errore; lo spegnimento gestisce sia l'ascolto sia le connessioni già accettate.

### Domande di ripasso

1. Perché chiamare `run()` non equivale a chiamare `start()`?
2. Qual è la differenza tra concorrenza e parallelismo?
3. Perché `valore++` può perdere incrementi?
4. Quale monitor acquisisce un metodo di istanza `synchronized`?
5. Perché un flag `volatile` non risolve tutti i problemi di condivisione?
6. Quale differenza c'è tra `sleep()` e `wait()` rispetto ai monitor?
7. Perché la condizione prima di `wait()` va controllata con `while`?
8. `shutdownNow()` garantisce che tutti i compiti siano terminati?
9. Un timeout di `Future.get()` cancella il compito?
10. Quando sono particolarmente utili i thread virtuali?

<details>
<summary>Risposte essenziali</summary>

1. `run()` è una normale chiamata nel thread corrente; `start()` avvia un nuovo flusso di esecuzione.
2. La concorrenza permette attività in progresso nello stesso intervallo; il parallelismo le esegue simultaneamente.
3. L'incremento comprende lettura, calcolo e scrittura, che possono interferire con gli stessi passi di un altro thread.
4. Il monitor di `this`; serve quindi la stessa istanza per coordinare i chiamanti.
5. Fornisce garanzie di visibilità e ordinamento, ma non rende atomiche operazioni composte.
6. `sleep()` non rilascia i monitor; `wait()` rilascia quello dell'oggetto su cui viene chiamato.
7. Sono possibili risvegli spuri e modifiche della condizione prima che il thread riacquisisca il monitor.
8. No, è un tentativo di interruzione: il lavoro deve collaborare.
9. No; la cancellazione richiede un'azione distinta.
10. Con molte attività che passano gran parte del tempo in attesa, soprattutto di I/O.

</details>

## 17. Riferimenti ufficiali

Le API collegano questa guida a Java 21, distinguendo nel testo le funzionalità che richiedono quella versione. I tutorial storici di Oracle sono riferiti a JDK 8: rimangono utili per i meccanismi fondamentali, mentre per le API moderne occorre usare la documentazione della versione scelta.

- [Oracle, percorso Concurrency](https://docs.oracle.com/javase/tutorial/essential/concurrency/)
- [API Thread](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Thread.html)
- [Java Language Specification, Threads and Locks](https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html)
- [API java.util.concurrent](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/package-summary.html)
- [API ReentrantLock](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/locks/ReentrantLock.html)
- [API CountDownLatch](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/CountDownLatch.html)
- [Oracle, Virtual Threads](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html)


[Indice del modulo](README.md) · [Applicazione: server multithread](02-Server-Multithreaded.md) · [Sincronizzazione nei server](03-Sincronizzazione-Risorse-Condivise.md) · [Gestione avanzata](04-Multithreading-Avanzato.md)
