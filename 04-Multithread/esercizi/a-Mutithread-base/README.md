# a1 — Socket multithreading essenziale

## Introduzione

Questa esercitazione ti insegna il concetto fondamentale di socket multithreading, realizzando un server che accoglie i client e risponde con un messaggio. Questo ti permette di concentrarti esclusivamente su **come il programma crea thread separati per i socket per gestire client contemporaneamente**.

L'idea centrale è molto semplice. Immagina una reception dove un receptionist (il server) è in ascolto alla porta principale. Quando arriva un cliente (un client si connette), anziché dire ai prossimi clienti "Aspetta il turno", il receptionist chiama un assistente (crea un thread) per gestire quel cliente specifico. Nel frattempo, il receptionist rimane libero di accogliere i prossimi clienti. Questo è esattamente quello che fa il nostro server: accetta una connessione e crea un thread per gestirla, subito dopo ritorna ad ascoltare nuove connessioni.

---

## Parte 1: Il Server Multithreading - Versione Minimale

Ecco il codice più semplice possibile per un server che gestisce più client con i thread. Leggi ogni riga e la riga di commento sopra ti spiega il perché.

### MultiThreadServer.java

```java
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/** Listener sulla porta 5000: accetta un socket distinto e lo trasferisce a un Runnable. */
public class MultiThreadServer {
    public static void main(String[] args) throws IOException {
        try (ServerSocket listener = new ServerSocket(5000)) {
            System.out.println("Server in ascolto sulla porta 5000");
            while (true) {
                Socket socket = listener.accept(); // Solo questa attesa riguarda il listener.
                new Thread(new ClientTask(socket)).start();
            }
        }
    }
}
```

Noti come il codice è semplice? La vera magia avviene nel fatto che ogni volta che creiamo `new Thread(new ClientTask(client))` e chiamiamo `start()`, quel thread esegue il suo codice indipendentemente mentre il server continua il loop e accetta il prossimo client.

### ClientTask.java

```java
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/** Gestore a righe UTF-8: il socket è chiuso automaticamente anche su errore o timeout. */
public class ClientTask implements Runnable {
    private final Socket socket;
    public ClientTask(Socket socket) { this.socket = socket; }

    @Override
    public void run() {
        try (Socket owned = socket) {
            socket.setSoTimeout(30_000); // Limita le letture di un client inattivo.
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            System.out.println("Gestore: " + Thread.currentThread().getName());
            String message = in.readLine();
            if (message == null) return; // EOF senza richiesta: non rispondere al testo "null".
            out.println("Server: Ho ricevuto il tuo messaggio: " + message);
        } catch (IOException e) {
            System.err.println("Errore gestore: " + e.getMessage());
        }
    }
}
```

Ecco il punto cruciale: il metodo `run()` è quello che il thread esegue. Quando il server crea un thread nuovo per ogni client, quel thread esegue il suo `run()` indipendentemente dagli altri. Se due client si connettono, avrai due thread che eseguono contemporaneamente due versioni di `run()`, ognuno con il suo socket.

---

## Parte 2: Il Client - Ancora Più Semplice

Il client non ha nulla di particolare riguardo al multithreading. È solo un client standard che si connette al server, invia un messaggio, riceve una risposta e si disconnette. La bellezza del multithreading è che è completamente trasparente al client.

### SimpleClient.java

```java
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/** Client della variante con una sola richiesta: invio e lettura sono a righe UTF-8. */
public class SimpleClient {
    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("localhost", 5000);
             Scanner keyboard = new Scanner(System.in)) {
            socket.setSoTimeout(30_000);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));
            System.out.println("Scrivi un messaggio:");
            if (!keyboard.hasNextLine()) return;
            out.println(keyboard.nextLine()); // La newline rende completa la richiesta.
            String response = in.readLine();
            if (response != null) System.out.println("Risposta: " + response);
        }
    }
}
```

Nota che il client non sa e non deve sapere che il server sta usando i thread. Per il client, è solo un server standard.

---

## Parte 3: Compilazione ed Esecuzione

Ecco come compilare ed eseguire il tutto. Leggi attentamente i comandi e segui i passaggi nell'ordine.

Crea nella tua cartella `~/socket` un progetto `ES03-MultiThread` con una sottocartella `src`, poi salva i file completi indicati. La variante con loop usa altri tre file: compilala con `javac -encoding UTF-8 MultiThreadServerWithLoop.java ClientTaskWithLoop.java SimpleClientWithLoop.java`. Non avviare i due server insieme: entrambi usano la porta 5000.

### Passo 1: Compilazione

```bash
# Vai nella cartella dove hai i file Java
cd ES03-MultiThread/src

# Compila tutti e tre i file
javac -encoding UTF-8 MultiThreadServer.java ClientTask.java SimpleClient.java

# Dovresti vedere la creazione di tre file .class
ls *.class
```

### Passo 2: Avvia il Server (Terminale 1)

```bash
# Nel primo terminale, avvia il server
java MultiThreadServer

# Vedrai questo output:
# Server in ascolto sulla porta 5000
# (rimane in attesa di connessioni)
```

Il server rimane in esecuzione aspettando connessioni. In condizioni normali prosegue fino a CTRL+C; un errore del listener può interromperlo.

### Passo 3: Avvia i Client (Terminali 2, 3, 4...)

Apri altri terminali e avvia il client in ognuno. Puoi farlo contemporaneamente per dimostrare il multithreading.

```bash
# Nel secondo terminale
java SimpleClient
# Output: Scrivi un messaggio:
# Digita: Ciao Server!
# Risposta: Server: Ho ricevuto il tuo messaggio: Ciao Server!

# Nel terzo terminale, allo stesso tempo
java SimpleClient
# Output: Scrivi un messaggio:
# Digita: Anche a te!
# Risposta: Server: Ho ricevuto il tuo messaggio: Anche a te!

# Nel quarto terminale, allo stesso tempo
java SimpleClient
# Output: Scrivi un messaggio:
# Digita: Multithreading funziona!
# Risposta: Server: Ho ricevuto il tuo messaggio: Multithreading funziona!
```

Nel terminale del server vedrai, per esempio:

```text
Server in ascolto sulla porta 5000
Gestore: Thread-0
Gestore: Thread-1
Gestore: Thread-2
```

I nomi distinguono i gestori e possono variare. Per verificare la concorrenza lascia il primo client connesso senza inviare e controlla che il secondo riceva comunque una risposta.

---

## Parte 4: Variante con Loop - Per Capire Meglio

Se vuoi una variante un po' più interessante che mostra il loop del server più chiaramente, ecco una versione dove ogni client può inviare multipli messaggi:

### MultiThreadServerWithLoop.java

```java
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/** Listener sulla porta 5000: accetta un socket distinto e lo trasferisce a un Runnable. */
public class MultiThreadServerWithLoop {
    public static void main(String[] args) throws IOException {
        try (ServerSocket listener = new ServerSocket(5000)) {
            System.out.println("Server in ascolto sulla porta 5000");
            while (true) {
                Socket socket = listener.accept(); // Solo questa attesa riguarda il listener.
                new Thread(new ClientTaskWithLoop(socket)).start();
            }
        }
    }
}
```

### ClientTaskWithLoop.java

```java
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/** Gestore a righe UTF-8: il socket è chiuso automaticamente anche su errore o timeout. */
public class ClientTaskWithLoop implements Runnable {
    private final Socket socket;
    public ClientTaskWithLoop(Socket socket) { this.socket = socket; }

    @Override
    public void run() {
        try (Socket owned = socket) {
            socket.setSoTimeout(30_000); // Limita le letture di un client inattivo.
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            System.out.println("Gestore: " + Thread.currentThread().getName());
            String message;
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("exit")) {
                    out.println("Arrivederci!"); // Il client legge questa risposta prima di uscire.
                    break;
                }
                out.println("Eco: " + message);
                if (out.checkError()) break;
            }
        } catch (IOException e) {
            System.err.println("Errore gestore: " + e.getMessage());
        }
    }
}
```

### SimpleClientWithLoop.java

```java
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/** Client della variante persistente con exit: invio e lettura sono a righe UTF-8. */
public class SimpleClientWithLoop {
    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("localhost", 5000);
             Scanner keyboard = new Scanner(System.in)) {
            socket.setSoTimeout(30_000);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));
            System.out.println("Scrivi un messaggio (exit per uscire):");
            while (keyboard.hasNextLine()) {
                String message = keyboard.nextLine();
                out.println(message);
                String response = in.readLine();
                if (response == null) break; // EOF del server: non proseguire con altre richieste.
                System.out.println("Risposta: " + response);
                if (message.equalsIgnoreCase("exit")) break;
            }
        }
    }
}
```

---

## Concetti Chiave Spiegati Semplicemente

### Perché i Thread?

Immagina senza i thread. Il server accetterebbe un client, manderebbe il messaggio "Ciao", e poi aspetterebbe finché il client non si disconnetteva. Durante questo tempo, tutti gli altri client dovrebbero aspettare. Con i thread, il server accetta un client, crea un thread per gestirlo, e ritorna subito a accettare il prossimo.

### Come Funziona `new Thread(new ClientTask(client)).start()`

Questa riga è la chiave di tutto. La parte `new ClientTask(client)` crea un oggetto che implementa `Runnable`. La parte `new Thread(...)` avvolge questo oggetto in un thread. La parte `.start()` dice al thread di iniziare l'esecuzione. Quando chiami `start()`, Java crea un nuovo thread separato che eseguirà il metodo `run()` di `ClientTask`.

### Il Loop Infinito nel Server

Il `while (true)` nel server significa che continua per sempre. Rimane in `accept()` aspettando connessioni. Quando arriva una connessione, la elabora, crea un thread per gestirla, e poi ritorna al `accept()` per aspettare la prossima. Un errore di accept può interrompere il ciclo; Ctrl+C termina il processo.

### Ogni Thread Ha il Suo Socket

Questo è importante. Quando crei `new ClientTask(client)`, il socket di quel client viene passato a quel specifico thread. Ogni thread ha il suo socket e comunica solo con il suo client. Le letture dai rispettivi socket restano indipendenti. Eventuali oggetti condivisi, come liste o contatori, richiedono invece coordinamento.

---

## Cosa Comprenderai da Questa Esercitazione

Dopo aver completato questa esercitazione, avrai compreso i fondamenti del multithreading con i socket. Capirai come il server rimane responsivo accettando nuove connessioni anche mentre sta gestendo altre connessioni. Ogni gestore ha un socket dedicato e uno stack distinto; gli oggetti condivisi appartengono comunque alla memoria dello stesso processo. È un modello introduttivo comune; altri server impiegano pool, thread virtuali o I/O a eventi. Una volta che hai capito questo concetto semplice, puoi aggiungere complessità come autenticazione, database, broadcast a più client, e molto altro. Ma il concetto fondamentale rimane lo stesso: accetta una connessione, crea un thread, prosegui.

---

## Domande di Autovalutazione

### Domanda 1
Perché il server crea un nuovo thread per ogni client anziché gestirlo direttamente nel main loop?

A) Per avere un'organizzazione del codice migliore  
B) Perché i thread sono più veloci  
C) Perché il server rimane libero di accettare nuove connessioni mentre un thread gestisce il cliente  
D) Perché Java lo richiede  

**Risposta corretta: C**

Se gestissi il client direttamente nel main loop, il server sarebbe bloccato finché quel client non si disconnetteva, ritardando il servizio degli altri client, le cui connessioni possono attendere nella coda del sistema operativo. Con i thread, il server rimane libero nel loop principale.

---

### Domanda 2
Cosa accade quando chiami `.start()` su un thread?

A) Il thread esegue immediatamente  
B) Java crea un nuovo thread separato che eseguirà il metodo `run()`  
C) Il thread esegue fino a quando non finisce  
D) Il thread viene aggiunto a una coda di attesa  

**Risposta corretta: B**

`start()` dice a Java di creare un nuovo thread di esecuzione separato che eseguirà il metodo `run()`. Non è immediato e il thread eseguirà in parallelo con il resto del codice.

---

### Domanda 3
Se tre client si connettono contemporaneamente, cosa vedrai nel server?

A) Un messaggio di errore  
B) Solo il primo client verrà elaborato  
C) Tre thread verranno creati e eseguiranno contemporaneamente  
D) Il server si bloccherà  

**Risposta corretta: C**

Il server creerà tre thread separati, uno per ogni client. Ognuno eseguirà il suo metodo `run()` contemporaneamente.

---

### Domanda 4
Quale metodo deve implementare una classe affinché possa essere eseguita come thread?

A) `main()`  
B) `run()`  
C) `start()`  
D) `execute()`  

**Risposta corretta: B**

Quando una classe implementa `Runnable`, deve implementare il metodo `run()`. Questo è il metodo che il thread eseguirà.

---

### Domanda 5
Ogni thread ha il suo socket o condividono tutti lo stesso socket?

A) Tutti i thread condividono lo stesso socket  
B) Ogni thread ha il suo socket  
C) Solo il main thread ha un socket  
D) Non importa, i socket non hanno a che fare con i thread  

**Risposta corretta: B**

Ogni thread `ClientTask` riceve il suo socket specifico nel costruttore. Questo socket rappresenta la connessione con quel cliente specifico e nessun altro thread ha accesso a esso.


Le tracce di output sono illustrative: ordine, identificativi e nomi dei thread variano. Lo stato del listener è distinto dai socket dei client. Il timeout riguarda ciascuna lettura, non la durata totale della sessione.

[Indice degli esercizi](../README.md) · [Guida ai server multithread](../../02-Server-Multithreaded.md)
