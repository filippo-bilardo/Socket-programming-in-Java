# Chat Multithreading - Tre Versioni (Thread, Runnable, Lambda)

## Introduzione

Tre approcci progressivi per implementare un server chat multithreaded in Java:
1. **Thread** - Approccio tradizionale ed esplicito
2. **Runnable** - Approccio modulare e flessibile
3. **Lambda** - Approccio moderno e conciso

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
        try {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Ricevuto: " + message);
                out.println("Echo: " + message);
            }

            socket.close();
            System.out.println("Client disconnesso");

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
            new InputStreamReader(socket.getInputStream()));
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
        try {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Ricevuto: " + message);
                out.println("Echo: " + message);
            }

            socket.close();
            System.out.println("Client disconnesso");

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
            new InputStreamReader(socket.getInputStream()));
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
- Consigliato per il 90% dei casi

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
        try {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Ricevuto: " + message);
                out.println("Echo: " + message);
            }

            socket.close();
            System.out.println("Client disconnesso");

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
            new InputStreamReader(socket.getInputStream()));
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
| **Java richiesto** | 1.0+ | 1.5+ | 8+ |

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

Una lambda è un modo conciso per scrivere una classe anonima che implementa un'interfaccia funzionale (con un solo metodo). Disponibile da Java 8.

---

### Domanda 3
Come crei un nuovo thread che esegue un Runnable?

A) `new Runnable().start()`  
B) `new Thread(runnable).start()`  
C) `runnable.start()`  
D) `Thread.execute(runnable)`  

**Risposta corretta: B**

Devi avvolgere il Runnable in un Thread e poi chiamare `start()`. Altrimenti il codice non viene eseguito in parallelo.

---

### Domanda 4
Quale versione è consigliata per un progetto professionale?

A) Thread  
B) Runnable  
C) Lambda  
D) Tutte vanno bene  

**Risposta corretta: B**

`Runnable` offre il miglior compromesso tra semplicità, performance e flessibilità. È lo standard di industria.

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
| 4 | B | Runnable è lo standard per il codice professionale |
| 5 | B | run() diretto = esecuzione nel thread corrente |

---

## Best Practice

1. **Preferisci Runnable a Thread** - È la pratica standard
2. **Usa lambda per codice semplice** - Ma non abusarne
3. **Sempre try-catch su IOException** - I socket lanciamenti eccezioni
4. **Chiudi i socket nel finally** - O usa try-with-resources
5. **Testa con più client** - Esegui più istanze del client
6. **Aggiungi logging** - Stampa operazioni importanti
7. **Gestisci l'EOFException** - Quando il client si disconnette

---

## Conclusione

- **Thread**: Imparate i fondamenti, ma non usate in produzione
- **Runnable**: La scelta migliore per quasi tutti i casi
- **Lambda**: Moderna e concisa, perfetta per codice semplice

Tutte e tre le versioni sono equivalenti in termini di performance. La scelta dipende dal vostro stile di programmazione e dai requisiti del progetto.