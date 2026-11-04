# Echo concorrente — Thread, Runnable e Lambda

## Introduzione

Tre approcci per implementare un server echo concorrente. Ogni messaggio torna al mittente; il broadcast viene aggiunto nell’esercizio finale per ottenere una chat. I file `Server.java` e `Client.java` usano invece un echo di byte sulla porta 9876, con EOF della richiesta tramite `shutdownOutput()`; i programmi di questo README usano righe sulla porta 5000.

Tre varianti:
1. **Thread** - Approccio tradizionale ed esplicito
2. **Runnable** - Approccio modulare e flessibile
3. **Lambda** - Approccio moderno e conciso

## Versione Node.js di Server.java — porta 9876

[Server.js](Server.js) riproduce l'echo di byte di [Server.java](Server.java), con timeout di inattività di 30 secondi. Richiede Node.js e usa soltanto il modulo integrato `node:net`, senza installazioni npm.

Dalla cartella corrente, avvia una sola delle due versioni del server:

```sh
node Server.js
```

In un secondo terminale, dalla stessa cartella, usa il client Java già presente:

```sh
javac -encoding UTF-8 Client.java
java Client
```

Il client invia `Ciao, server!`, chiude il solo invio con `shutdownOutput()` e legge fino a EOF. La risposta attesa è `Risposta dal server: Ciao, server!`. I comandi funzionano anche in PowerShell; Ctrl+C termina il server. Per provare la versione Java, ferma prima Node.js, compila `Server.java` e avvia `java Server` sulla stessa porta.

La versione Node.js gestisce più connessioni attraverso l'event loop e l'I/O non bloccante. I callback JavaScript non creano un thread dedicato per ogni client: devono rimanere brevi per consentire alle altre connessioni di avanzare. Il server Java usa invece un thread per socket.

`socket.pipe(socket)` rimanda i byte allo stesso mittente senza convertirli in testo e regola il flusso quando il destinatario è lento. Con `allowHalfOpen: true`, l'EOF ricevuto non chiude subito l'output: `pipe()` lo termina dopo aver accodato l'echo, permettendo al client di leggere tutta la risposta. Il timeout Node.js riguarda l'inattività del socket; `setSoTimeout` nel server Java riguarda le letture bloccanti.

Queste due versioni non aggiungono prefissi o delimitatori e non effettuano broadcast. Le tre varianti Java illustrate sotto usano invece righe e la porta 5000. Documentazione: [socket e server TCP Node.js](https://nodejs.org/api/net.html), [stream e pipe](https://nodejs.org/api/stream.html#readablepipedestination-options).

---

## Versione 1: Con Estensione di Thread

La versione più semplice: creiamo una classe che estende `Thread`.

### Server - ChatServerThread.java

```java
import java.io.*;
import java.net.*;

public class ChatServerThread {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(5000);
        System.out.println("Server avviato sulla porta 5000");

        while (true) {
            Socket socket = server.accept();
            System.out.println("Client connesso");
            new ClientThread(socket).start();
        }
    }
}
```

### ClientThread.java

```java
import java.io.*;
import java.net.Socket;

public class ClientThread extends Thread {
    private Socket socket;

    public ClientThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (Socket owned = socket) {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Ricevuto: " + message);
                out.println("Echo: " + message);
            }

            // Il try-with-resources chiude il socket anche su errore.
            System.out.println("Gestione completata");

        } catch (IOException e) {
            System.err.println("Errore: " + e.getMessage());
        }
    }
}
```

### Client - ChatClientThread.java

```java
import java.io.*;
import java.net.Socket;

public class ChatClientThread {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 5000);
        
        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
        PrintWriter out = new PrintWriter(
            socket.getOutputStream(), true);

        // Thread per ricevere messaggi dal server
        new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    System.out.println(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Leggi da tastiera e invia
        BufferedReader keyboard = new BufferedReader(
            new InputStreamReader(System.in));
        String input;
        while ((input = keyboard.readLine()) != null) {
            out.println(input);
        }

        socket.close();
    }
}
```

**Vantaggi:**
- Molto semplice da capire
- Codice diretto

**Svantaggi:**
- Crea un nuovo thread per ogni client
- Consuma molta memoria
- Difficile da controllare

---

## Versione 2: Con Runnable (Consigliato)

Implementiamo `Runnable` invece di estendere `Thread`.

### Server - ChatServerRunnable.java

```java
import java.io.*;
import java.net.*;

public class ChatServerRunnable {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(5000);
        System.out.println("Server avviato sulla porta 5000");

        while (true) {
            Socket socket = server.accept();
            System.out.println("Client connesso");
            new Thread(new ClientRunnable(socket)).start();
        }
    }
}
```

### ClientRunnable.java

```java
import java.io.*;
import java.net.Socket;

public class ClientRunnable implements Runnable {
    private Socket socket;

    public ClientRunnable(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (Socket owned = socket) {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Ricevuto: " + message);
                out.println("Echo: " + message);
            }

            // Il try-with-resources chiude il socket anche su errore.
            System.out.println("Gestione completata");

        } catch (IOException e) {
            System.err.println("Errore: " + e.getMessage());
        }
    }
}
```

### Client - ChatClientRunnable.java

```java
import java.io.*;
import java.net.Socket;

public class ChatClientRunnable {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 5000);
        
        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
        PrintWriter out = new PrintWriter(
            socket.getOutputStream(), true);

        // Runnable per ricevere messaggi
        Runnable receiver = new Runnable() {
            @Override
            public void run() {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        System.out.println(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(receiver).start();

        // Leggi da tastiera e invia
        BufferedReader keyboard = new BufferedReader(
            new InputStreamReader(System.in));
        String input;
        while ((input = keyboard.readLine()) != null) {
            out.println(input);
        }

        socket.close();
    }
}
```

**Vantaggi:**
- Separa il compito dalla gestione del thread
- Più flessibile
- Adatto a separare compito e thread

**Svantaggi:**
- Leggermente più complesso di Thread

---

## Versione 3: Con Lambda Expression

Versione moderna usando lambda (Java 8+).

### Server - ChatServerLambda.java

```java
import java.io.*;
import java.net.*;

public class ChatServerLambda {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(5000);
        System.out.println("Server avviato sulla porta 5000");

        while (true) {
            Socket socket = server.accept();
            System.out.println("Client connesso");
            
            new Thread(() -> handleClient(socket)).start();
        }
    }

    private static void handleClient(Socket socket) {
        try (Socket owned = socket) {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Ricevuto: " + message);
                out.println("Echo: " + message);
            }

            // Il try-with-resources chiude il socket anche su errore.
            System.out.println("Gestione completata");

        } catch (IOException e) {
            System.err.println("Errore: " + e.getMessage());
        }
    }
}
```

### Client - ChatClientLambda.java

```java
import java.io.*;
import java.net.Socket;

public class ChatClientLambda {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 5000);
        
        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
        PrintWriter out = new PrintWriter(
            socket.getOutputStream(), true);

        // Lambda per ricevere messaggi
        new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    System.out.println(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Leggi da tastiera e invia
        BufferedReader keyboard = new BufferedReader(
            new InputStreamReader(System.in));
        String input;
        while ((input = keyboard.readLine()) != null) {
            out.println(input);
        }

        socket.close();
    }
}
```

**Vantaggi:**
- Codice più conciso
- Moderno e leggibile
- Meno boilerplate

**Svantaggi:**
- Richiede Java 8+
- Difficile debuggare dentro la lambda

---

## Confronto Rapido

| Aspetto | Thread | Runnable | Lambda |
|---------|--------|----------|--------|
| **Complessità** | Bassa | Media | Bassa |
| **Linee di codice** | Molte | Medie | Poche |
| **Readability** | Buona | Ottima | Ottima |
| **Performance** | Identica | Identica | Identica |
| **Consigliato per** | Imparare | Produzione | Prototipazione |
| **JDK usato nel modulo** | 11+ | 11+ | 11+ |

---

## Esercizi Proposti

### Esercizio 1: Server Echo Multiplo
Modifica il server per:
- Numerare ogni client (Client #1, Client #2, ecc.)
- Stampare l'indirizzo IP di ogni client
- Inviare un messaggio di benvenuto

### Esercizio 2: Server con Comando Exit
Aggiungi la possibilità di scrivere `/exit` per disconnettersi

### Esercizio 3: Broadcast
Modifica il server per mandare il messaggio di un client a TUTTI gli altri client connessi

### Esercizio 4: Timestamp
Aggiungi un timestamp a ogni messaggio ricevuto

---

## Domande di Autovalutazione

### Domanda 1
Qual è la differenza principale tra estendere `Thread` e implementare `Runnable`?

A) `Thread` è più veloce  
B) `Runnable` consente di estendere un'altra classe  
C) Non c'è differenza  
D) `Thread` è più moderno  

**Risposta corretta: B**

Quando implementi `Runnable`, la classe può ancora estendere un'altra classe. Con `extends Thread`, non puoi estendere nulla di più. Per questo `Runnable` è preferito.

---

### Domanda 2
Cos'è una lambda expression?

A) Una funzione anonima semplificata  
B) Un'estensione di Thread  
C) Un tipo di Socket  
D) Un metodo privato  

**Risposta corretta: A**

Una lambda fornisce l'implementazione del metodo astratto di un'interfaccia funzionale, qui Runnable; non è sintatticamente una classe anonima. Disponibile da Java 8.

---

### Domanda 3
Come crei un nuovo thread che esegue un Runnable?

A) `new Runnable().start()`  
B) `new Thread(runnable).start()`  
C) `runnable.start()`  
D) `Thread.execute(runnable)`  

**Risposta corretta: B**

Devi avvolgere il Runnable in un Thread e poi chiamare `start()`. Una chiamata diretta a run viene invece eseguita nel thread corrente; il parallelismo fisico non è garantito nemmeno con start.

---

### Domanda 4
Quale versione è consigliata per un progetto professionale?

A) Thread  
B) Runnable  
C) Lambda  
D) Tutte vanno bene  

**Risposta corretta: B**

`Runnable` offre il miglior compromesso tra semplicità, performance e flessibilità. Anche una lambda può rappresentare un Runnable; la forma sintattica non determina prestazioni o robustezza.

---

### Domanda 5
Cosa accade se non chiami `start()` ma chiami direttamente `run()`?

A) Il thread non viene creato  
B) Il codice viene eseguito nel thread corrente  
C) Non succede niente  
D) Si genera un'eccezione  

**Risposta corretta: B**

Chiamare `run()` direttamente lo esegue nel thread attuale. Devi usare `start()` per creare un nuovo thread.

---

## Risposte - Riepilogo

| Q | Risposta | Spiegazione |
|---|----------|-------------|
| 1 | B | Runnable permette di estendere altre classi |
| 2 | A | Lambda è una funzione anonima semplificata |
| 3 | B | Wrappa il Runnable in Thread e chiama start() |
| 4 | B | Runnable separa il compito dalla gestione del thread |
| 5 | B | run() diretto = esecuzione nel thread corrente |

---

## Best Practice

1. **Preferisci Runnable a Thread** - È la pratica standard
2. **Usa lambda per codice semplice** - Ma non abusarne
3. **Sempre try-catch su IOException** - I socket possono lanciare eccezioni
4. **Chiudi i socket nel finally** - O usa try-with-resources
5. **Testa con più client** - Esegui più istanze del client
6. **Aggiungi logging** - Stampa operazioni importanti
7. **Gestisci EOF** - readLine restituisce null; le letture binarie possono lanciare EOFException

---

## Conclusione

- **Thread**: Rappresenta il flusso di esecuzione; estenderlo lega il compito alla sua gestione
- **Runnable**: La scelta migliore per quasi tutti i casi
- **Lambda**: Una forma concisa per esprimere un Runnable

Tutte e tre le versioni sono equivalenti in termini di performance. La scelta dipende dal vostro stile di programmazione e dai requisiti del progetto.
[Indice degli esempi](../README.md) · [Esercizio chat con broadcast](../../esercizi/d-ChatServer-Multithreaded/README.md)


## Confini delle prove

Salva ciascuna classe pubblica nel file omonimo e compila insieme solo i file della variante scelta, con `javac -encoding UTF-8`. Le tre varianti a righe usano la porta 5000; i file Java già presenti nella cartella usano invece la porta 9876 e byte fino a EOF. Il thread della console può restare bloccato aspettando input anche quando il ricevitore è terminato: aggiungi un protocollo di uscita e prevedi Invio dopo una chiusura remota.

I server introduttivi non impongono un limite alle connessioni. Per il broadcast servono un registro condiviso, snapshot dei destinatari e scritture coordinate per ogni output: consulta la [guida sulla sincronizzazione](../../03-Sincronizzazione-Risorse-Condivise.md). Non dedurre l'assenza di errori concorrenti da una sola esecuzione.
