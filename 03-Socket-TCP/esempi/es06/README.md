# ES03 - Socket Multithreading Essenziale

## Introduzione

Questa esercitazione ti insegna il concetto fondamentale di socket multithreading, realizzando un server che accoglie i client e risponde con un messaggio. Questo ti permette di concentrarti esclusivamente su **come i socket creano thread separati per gestire client contemporaneamente**.

L'idea centrale è molto semplice. Immagina una reception dove un receptionist (il server) è in ascolto alla porta principale. Quando arriva un cliente (un client si connette), anziché dire ai prossimi clienti "Aspetta il turno", il receptionist chiama un assistente (crea un thread) per gestire quel cliente specifico. Nel frattempo, il receptionist rimane libero di accogliere i prossimi clienti. Questo è esattamente quello che fa il nostro server: accetta una connessione e crea un thread per gestirla, subito dopo ritorna ad ascoltare nuove connessioni.

---

## Parte 1: Il Server Multithreading - Versione Minimale

Ecco il codice più semplice possibile per un server che gestisce più client con i thread. Leggi ogni riga e la riga di commento sopra ti spiega il perché.

### MultiThreadServer.java

```java
import java.io.*;
import java.net.*;

public class MultiThreadServer {
    // Crea il server sulla porta 5000
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(5000);
        System.out.println("Server in ascolto sulla porta 5000");

        // Il server rimane in questo loop per sempre
        // Accetta connessioni uno dopo l'altro
        while (true) {
            // Aspetta che un client si connetta
            // Se due client si connettono contemporaneamente,
            // il secondo aspetta che il primo venga gestito
            Socket client = server.accept();
            
            System.out.println("Client connesso da: " + 
                client.getInetAddress().getHostAddress());

            // Questo è il momento cruciale:
            // Anziché elaborare il client direttamente qui (che bloccherebbe il server),
            // creiamo un nuovo thread che gestirà questo client
            // Il server rimane libero di ascoltare il prossimo
            Thread thread = new Thread(new ClientTask(client));
            thread.start();
        }
    }
}
```

Noti come il codice è semplice? La vera magia avviene nel fatto che ogni volta che creiamo `new Thread(new ClientTask(client))` e chiamiamo `start()`, quel thread esegue il suo codice indipendentemente mentre il server continua il loop e accetta il prossimo client.

### ClientTask.java

```java
import java.io.*;
import java.net.Socket;

// Questa classe implementa Runnable perché i thread eseguono il metodo run()
public class ClientTask implements Runnable {
    // Ogni ClientTask ha il suo socket per quel cliente specifico
    private Socket socket;

    public ClientTask(Socket socket) {
        this.socket = socket;
    }

    // Il metodo run() è quello che il thread eseguirà
    // Ogni thread esegue questo metodo indipendentemente dagli altri
    @Override
    public void run() {
        try {
            // Crea uno stream di output per inviare dati al cliente
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);

            // Crea uno stream di input per ricevere dati dal cliente
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

            System.out.println("[Thread " + Thread.currentThread().getId() + 
                "] Ho ricevuto una connessione");

            // Leggi quello che il client invia
            String message = in.readLine();
            System.out.println("[Thread " + Thread.currentThread().getId() + 
                "] Client ha detto: " + message);

            // Rispondi al client
            out.println("Server: Ho ricevuto il tuo messaggio: " + message);

            // Chiudi il socket quando hai finito
            socket.close();
            System.out.println("[Thread " + Thread.currentThread().getId() + 
                "] Client disconnesso");

        } catch (IOException e) {
            System.err.println("Errore: " + e.getMessage());
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
import java.net.Socket;
import java.util.Scanner;

public class SimpleClient {
    public static void main(String[] args) throws IOException {
        // Connettiti al server che sta ascoltando su localhost:5000
        Socket socket = new Socket("localhost", 5000);
        System.out.println("Connesso al server");

        // Crea stream di output per inviare dati
        PrintWriter out = new PrintWriter(
            socket.getOutputStream(), true);

        // Crea stream di input per ricevere dati
        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));

        // Chiedi all'utente un messaggio
        Scanner keyboard = new Scanner(System.in);
        System.out.print("Scrivi un messaggio: ");
        String message = keyboard.nextLine();

        // Invia il messaggio al server
        out.println(message);

        // Ricevi la risposta dal server
        String response = in.readLine();
        System.out.println("Risposta: " + response);

        // Chiudi la connessione
        socket.close();
        keyboard.close();
        System.out.println("Disconnesso");
    }
}
```

Nota che il client non sa e non deve sapere che il server sta usando i thread. Per il client, è solo un server standard.

---

## Parte 3: Compilazione ed Esecuzione

Ecco come compilare ed eseguire il tutto. Leggi attentamente i comandi e segui i passaggi nell'ordine.

### Passo 1: Compilazione

```bash
# Vai nella cartella dove hai i file Java
cd ES03-MultiThread/src

# Compila tutti e tre i file
javac MultiThreadServer.java ClientTask.java SimpleClient.java

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

Il server rimane in esecuzione aspettando connessioni. Non terminerà mai a meno che tu non lo interrompa con CTRL+C.

### Passo 3: Avvia i Client (Terminali 2, 3, 4...)

Apri altri terminali e avvia il client in ognuno. Puoi farlo contemporaneamente per dimostrare il multithreading.

```bash
# Nel secondo terminale
java SimpleClient
# Output: Connesso al server
# Scrivi un messaggio: Ciao Server!
# Risposta: Server: Ho ricevuto il tuo messaggio: Ciao Server!

# Nel terzo terminale, allo stesso tempo
java SimpleClient
# Output: Connesso al server
# Scrivi un messaggio: Anche a te!
# Risposta: Server: Ho ricevuto il tuo messaggio: Anche a te!

# Nel quarto terminale, allo stesso tempo
java SimpleClient
# Output: Connesso al server
# Scrivi un messaggio: Multithreading funziona!
# Risposta: Server: Ho ricevuto il tuo messaggio: Multithreading funziona!
```

Guardarai il server e vedrai qualcosa come questo:

```
Server in ascolto sulla porta 5000
Client connesso da: 127.0.0.1
[Thread 12] Ho ricevuto una connessione
Client connesso da: 127.0.0.1
[Thread 13] Ho ricevuto una connessione
Client connesso da: 127.0.0.1
[Thread 14] Ho ricevuto una connessione
[Thread 12] Client ha detto: Ciao Server!
[Thread 13] Client ha detto: Anche a te!
[Thread 14] Client ha detto: Multithreading funziona!
[Thread 12] Client disconnesso
[Thread 13] Client disconnesso
[Thread 14] Client disconnesso
```

Nota i diversi thread ID (12, 13, 14). Questo dimostra che ogni client viene gestito da un thread diverso contemporaneamente.

---

## Parte 4: Variante con Loop - Per Capire Meglio

Se vuoi una variante un po' più interessante che mostra il loop del server più chiaramente, ecco una versione dove ogni client può inviare multipli messaggi:

### MultiThreadServerWithLoop.java

```java
import java.io.*;
import java.net.*;

public class MultiThreadServerWithLoop {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(5000);
        System.out.println("Server in ascolto sulla porta 5000");

        while (true) {
            Socket client = server.accept();
            System.out.println("Client connesso");
            
            // Ogni connessione viene gestita in un thread separato
            new Thread(new ClientTaskWithLoop(client)).start();
        }
    }
}
```

### ClientTaskWithLoop.java

```java
import java.io.*;
import java.net.Socket;

public class ClientTaskWithLoop implements Runnable {
    private Socket socket;

    public ClientTaskWithLoop(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

            System.out.println("Thread " + Thread.currentThread().getId() + 
                " iniziato");

            String message;
            // Continua a leggere messaggi finché il client invia "exit"
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("exit")) {
                    out.println("Arrivederci!");
                    break;
                }
                
                // Risponde a ogni messaggio
                out.println("Eco: " + message);
                System.out.println("Thread " + Thread.currentThread().getId() + 
                    " ha ricevuto: " + message);
            }

            socket.close();
            System.out.println("Thread " + Thread.currentThread().getId() + 
                " terminato");

        } catch (IOException e) {
            System.err.println("Errore: " + e.getMessage());
        }
    }
}
```

### SimpleClientWithLoop.java

```java
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class SimpleClientWithLoop {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 5000);
        System.out.println("Connesso");

        PrintWriter out = new PrintWriter(
            socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));

        Scanner keyboard = new Scanner(System.in);

        // Continua finché l'utente digita "exit"
        while (true) {
            System.out.print("> ");
            String message = keyboard.nextLine();
            
            out.println(message);
            
            String response = in.readLine();
            System.out.println("Risposta: " + response);

            if (message.equalsIgnoreCase("exit")) {
                break;
            }
        }

        socket.close();
        keyboard.close();
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

Il `while (true)` nel server significa che continua per sempre. Rimane in `accept()` aspettando connessioni. Quando arriva una connessione, la elabora, crea un thread per gestirla, e poi ritorna al `accept()` per aspettare la prossima. Non esce mai da questo loop a meno che tu non lo termini.

### Ogni Thread Ha il Suo Socket

Questo è importante. Quando crei `new ClientTask(client)`, il socket di quel client viene passato a quel specifico thread. Ogni thread ha il suo socket e comunica solo con il suo client. Due thread non interferiranno mai uno con l'altro perché ognuno ha i propri stream di I/O.

---

## Cosa Comprenderai da Questa Esercitazione

Dopo aver completato questa esercitazione, avrai compreso i fondamenti del multithreading con i socket. Capirai come il server rimane responsivo accettando nuove connessioni anche mentre sta gestendo altre connessioni. Capirai come i thread forniscono isolamento, in modo che ogni client abbia la sua comunicazione dedicata. Questo è il fondamento di quasi tutti i server moderni, dai web server ai game server. Una volta che hai capito questo concetto semplice, puoi aggiungere complessità come autenticazione, database, broadcast a più client, e molto altro. Ma il concetto fondamentale rimane lo stesso: accetta una connessione, crea un thread, prosegui.

---

## Domande di Autovalutazione

### Domanda 1
Perché il server crea un nuovo thread per ogni client anziché gestirlo direttamente nel main loop?

A) Per avere un'organizzazione del codice migliore  
B) Perché i thread sono più veloci  
C) Perché il server rimane libero di accettare nuove connessioni mentre un thread gestisce il cliente  
D) Perché Java lo richiede  

**Risposta corretta: C**

Se gestissi il client direttamente nel main loop, il server sarebbe bloccato finché quel client non si disconnetteva, impedendo a nuovi client di connettersi. Con i thread, il server rimane libero nel loop principale.

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
