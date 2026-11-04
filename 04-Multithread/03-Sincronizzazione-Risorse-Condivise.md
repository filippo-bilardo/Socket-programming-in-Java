# Sincronizzazione e risorse condivise

Questa guida applica ai server TCP i concetti introdotti nella [guida sui thread Java](01-Multithreading-Java.md). Leggi anche il [server con un gestore per client](02-Server-Multithreaded.md). Gli esempi richiedono JDK 11+; ogni classe pubblica va salvata nel file omonimo e compilata con `javac -encoding UTF-8 NomeClasse.java`. Le classi di supporto non hanno un `main`.

**Obiettivi:** individuare dati condivisi, scegliere un lock comune, mantenere invarianti e separare la gestione dei dati dalle operazioni di rete. Pool, timeout, errori e arresto sono nella [guida avanzata](04-Multithreading-Avanzato.md).

Un socket assegnato a un unico gestore è una risorsa di quel gestore. Una lista di utenti, la disponibilità di posti e i contatori del server sono invece condivisi. Anche una variabile locale può contenere il riferimento a un oggetto usato da altri thread.

## 1. Riconoscere lo stato condiviso

Quando più thread lavorano contemporaneamente sugli stessi dati, possono comparire problemi diversi:

- **Race condition:** il risultato dipende dall'ordine, non prevedibile, con cui i thread eseguono le operazioni.
- **Perdita di aggiornamenti:** due thread leggono lo stesso valore e poi lo aggiornano; l'ultimo a scrivere sovrascrive il lavoro dell'altro.
- **Visibilità:** un thread può non osservare subito una modifica eseguita da un altro, se l'accesso non è coordinato correttamente.
- **Incoerenza dei dati:** più valori che devono rispettare una relazione possono essere osservati o modificati solo in parte.
- **Contesa e deadlock:** la sincronizzazione può rallentare i thread; lock acquisiti in ordini incompatibili possono bloccarli per sempre.

La sincronizzazione protegge quindi non solo una singola variabile, ma anche le invarianti che devono rimanere vere. Va applicata agli accessi concorrenti in lettura e scrittura, scegliendo con attenzione la porzione di codice da proteggere.

## 2. Il Problema della Race Condition

Una race condition si verifica quando il risultato di un'operazione dipende dall'interleaving dei thread. Anche se `count++` sembra un'istruzione singola, in realtà comporta tre passaggi: leggere `count`, calcolare il nuovo valore e scriverlo. Due thread possono quindi leggere entrambi lo stesso valore e perdere un incremento.

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

Per esempio, se `count` vale `0`, il thread A e il thread B possono leggere entrambi `0`, calcolare `1` e scrivere `1`: il valore finale sarà `1` invece di `2`. Il problema non si presenta necessariamente a ogni esecuzione, perciò può essere difficile da riprodurre. Anche `getCount()` deve essere coordinato con le scritture: altrimenti il chiamante non ha una garanzia di osservare il valore aggiornato.

## 3. Esclusione reciproca con synchronized

La parola chiave `synchronized` fa acquisire al thread il monitor dell'oggetto prima di entrare nel metodo e lo rilascia quando esce, anche se si verifica un'eccezione. Un solo thread alla volta può entrare in una sezione protetta dallo stesso monitor. Sincronizzare anche `getCount()` assicura che lettura e scrittura usino lo stesso monitor, garantendo coordinamento e visibilità.

La protezione funziona solo se tutti gli accessi concorrenti al dato rispettano lo stesso lock. Inoltre, sezioni sincronizzate troppo ampie causano attesa e riducono la concorrenza. Evitare di acquisire più lock in ordini diversi: per esempio, A prende il lock X e attende Y mentre B prende Y e attende X; entrambi restano bloccati (deadlock).

```java
/** Incremento e lettura acquisiscono il monitor della medesima istanza. */
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

## 4. Lock Espliciti (ReentrantLock)

`ReentrantLock` offre un controllo più flessibile, ma richiede di rilasciare il lock esplicitamente. Il blocco `finally` è essenziale: senza di esso, un'eccezione potrebbe lasciare il lock acquisito e impedire agli altri thread di proseguire. Anche con i lock espliciti valgono i rischi di contesa e deadlock.

```java
/** Lock esplicito: il finally ne garantisce il rilascio anche in caso di eccezione. */
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

## 5. Esempio Pratico: Server con Statistiche Condivise

Le statistiche condividono un'unica sezione critica: `totalConnections` e `activeConnections` vengono aggiornate insieme, così una stampa non legge uno stato intermedio. Tutti i metodi che leggono o modificano queste variabili devono usare `statisticsLock`. In un server reale, inoltre, la disconnessione va registrata anche se la gestione della connessione termina con un'eccezione, e il contatore delle connessioni attive non deve scendere sotto zero.

La seguente classe di supporto completa va salvata nel file `ServerWithStatistics.java`. Non apre socket: ciascun gestore chiama `recordConnection()` all'inizio e `recordDisconnection()` nel proprio `finally`, una sola volta. Il totale conta qui i gestori iniziati, non i socket ancora in coda.

```java
/** Statistiche coerenti: tutti gli accessi ai due campi usano lo stesso lock. */
public class ServerWithStatistics {
    private long totalConnections;
    private long activeConnections;
    private final Object statisticsLock = new Object();

    public void recordConnection() {
        synchronized (statisticsLock) {
            totalConnections++;
            activeConnections++;
        }
    }

    public void recordDisconnection() {
        synchronized (statisticsLock) {
            if (activeConnections == 0) {
                throw new IllegalStateException("Nessun gestore attivo da rimuovere");
            }
            activeConnections--;
        }
    }

    public String snapshot() {
        synchronized (statisticsLock) {
            // Entrambi i valori appartengono allo stesso istante logico.
            return "Totali: " + totalConnections + "; attive: " + activeConnections;
        }
    }

    public void printStatistics() {
        String message = snapshot();
        // La console può essere lenta: non mantenerla nella sezione critica.
        System.out.println(message);
    }
}
```

Non si espongono riferimenti a strutture interne modificabili. La costruzione dello snapshot avviene sotto lock, mentre stampa e invio sulla rete avvengono dopo averlo rilasciato.

## 6. Atomicità, visibilità e invarianti

| Strumento | Uso adatto | Limite |
| --- | --- | --- |
| `synchronized` | Proteggere uno o più campi e una sequenza controllo/aggiornamento | Tutti gli accessi devono usare lo stesso monitor |
| `ReentrantLock` | Esclusione con acquisizione interrompibile o con timeout | Il rilascio va scritto in `finally` dopo l'acquisizione riuscita |
| `volatile` | Pubblicare un flag, per esempio una richiesta di arresto | Non rende atomico `count++` e non sblocca da solo una lettura di rete |
| `AtomicInteger` | Incrementare un contatore indipendente | Più variabili atomiche non formano una transazione |
| `ConcurrentHashMap` | Registro con operazioni atomiche come `putIfAbsent` | `containsKey` seguito da `put` rimane una sequenza separata |

Il rilascio di un monitor rende visibili le scritture a chi acquisisce successivamente lo stesso monitor. Un metodo di istanza `synchronized` usa `this`; uno `static synchronized` usa l'oggetto `Class`. Bloccare istanze diverse non coordina i relativi thread.

Un'invariante è una proprietà che deve restare vera: per il dispenser `0 <= ultimoCompletato <= ultimoAssegnato`; per le prenotazioni un posto ha al massimo un titolare. Proteggi insieme controllo, aggiornamento e costruzione della risposta. Il metodo seguente è un frammento da inserire nella classe che possiede la mappa e un `Object lockPosti` condiviso:

```java
// Frammento di metodo: posti e lockPosti sono campi della stessa istanza condivisa.
public boolean prenota(String posto, String utente) {
    synchronized (lockPosti) {
        if (posti.containsKey(posto)) return false;
        posti.put(posto, utente); // Nessun altro thread può inserirsi tra i due passi.
        return true;
    }
}
```

Per una semplice mappa concorrente si può usare invece `putIfAbsent(posto, utente) == null`. Se l'operazione riguarda più posti o un contatore separato, serve coordinare l'intero insieme. Due letture separate, anche se singolarmente sincronizzate, non costituiscono uno snapshot comune.

## 7. Collezioni, broadcast e ordine dei lock

Con `Collections.synchronizedList`, `add` e `remove` sono protetti, ma l'iterazione deve acquisire il monitor della lista. Copia i destinatari sotto quel monitor, rilascialo e poi invia. Anche con una collezione concorrente resta necessario scegliere una semantica: gli utenti entrati dopo lo snapshot riceveranno i messaggi successivi.

```java
// Frammento da inserire in un metodo del server chat.
List<ClientHandler> destinatari;
synchronized (clients) {
    destinatari = new ArrayList<>(clients);
}
for (ClientHandler client : destinatari) {
    client.sendMessage(messaggio); // Metodo sincronizzato sul singolo destinatario.
}
```

Gli import del frammento sono `java.util.List` e `java.util.ArrayList`; `clients` è la lista condivisa. `ClientHandler` qui è il gestore della [chat](esercizi/a-ChatServer-Multithreaded/README.md), con `sendMessage`, non il gestore echo della guida precedente.

Un solo thread deve leggere da ciascun input. Più thread che scrivono sullo stesso output devono proteggere **il messaggio completo**, soprattutto per messaggi binari composti da più campi. Non basta che le singole scritture siano protette separatamente. Inizializza gli stream prima di pubblicare il gestore nel registro condiviso.

Evita di inviare sulla rete tenendo il lock globale della lista: un destinatario lento fermerebbe anche registrazioni e disconnessioni. Il lock dell'output del singolo destinatario può comunque far attendere i mittenti; una coda limitata per ogni destinatario è un'estensione, con una politica per i client lenti.

Se servono più lock, stabilisci un ordine comune. A che possiede X e attende Y, mentre B possiede Y e attende X, produce un deadlock. `sleep()` non rilascia il monitor e non corregge una race condition.

## 8. Attendere una condizione

Per produttori e consumatori preferisci una `BlockingQueue` con capacità fissata: `put()` attende spazio e `take()` attende dati. L'attesa rilascia le risorse interne necessarie agli altri partecipanti, senza un ciclo che spreca CPU. La capacità limita gli elementi in coda, non eventuali altre risorse possedute dal programma.

Con `wait()` acquisisci il monitor dell'oggetto, verifica la condizione con `while`, chiama `wait()` su quell'oggetto e usa `notifyAll()` dopo aver cambiato la condizione. Il risveglio non garantisce che la condizione sia ancora vera. Propaga `InterruptedException` oppure ripristina il flag ed esci. Trovi classi complete e protocollo di terminazione nella [sezione sul coordinamento](01-Multithreading-Java.md#10-coordinamento-wait-notifyall-e-blockingqueue).

## 9. Laboratorio e verifica

1. Confronta `UnsafeCounter`, `SafeCounter` e `CounterWithLock` con quattro thread, ciascuno con 100000 incrementi. Avvia tutti i thread prima dei `join()`: le versioni protette devono dare 400000. La versione errata può occasionalmente dare lo stesso risultato.
2. Applica `ServerWithStatistics` a un gestore e prova EOF ed eccezioni: ogni avvio deve avere una sola disconnessione registrata; alla fine le attive devono essere zero.
3. Svolgi [Dispenser](esercizi/b-Dispenser/README.md) e [Prenotazioni](esercizi/c-Prenotazioni/README.md). Invia richieste concorrenti, verifica numeri distinti e una sola prenotazione riuscita dello stesso posto.
4. Per una chat multicanale definisci registro utenti, stanze e ordine dei lock. Prova `/join`, `/leave`, `/list`, broadcast e uscita improvvisa. Specifica i confini dei messaggi del protocollo.
5. Per un log condiviso costruisci ogni record prima di acquisire il lock del file. Proteggi la scrittura completa; se statistiche e file devono essere coerenti anche in caso di errore, definisci quando aggiornare i contatori.

**Domande:** perché `volatile` non basta per incrementare? Quale oggetto protegge un metodo `static synchronized`? Perché copiare i destinatari prima del broadcast? Come mantenere atomico controllo e assegnazione di un posto?

**Risposte:** l'incremento è composto; il monitor è quello della classe; la copia evita I/O sotto il lock globale; controllo e assegnazione devono appartenere alla stessa sezione critica o a un'unica operazione atomica adeguata.

## Riferimenti

- [Oracle: sincronizzazione e monitor](https://docs.oracle.com/javase/tutorial/essential/concurrency/locksync.html)
- [ReentrantLock](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/locks/ReentrantLock.html)
- [ConcurrentHashMap](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ConcurrentHashMap.html)
- [Java Memory Model](https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html)

[Indice del modulo](README.md) · [Prima: server multithread](02-Server-Multithreaded.md) · [Dopo: gestione avanzata](04-Multithreading-Avanzato.md)
