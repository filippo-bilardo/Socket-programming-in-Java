# Comunicazione Multithreading con Socket TCP in Java

## Introduzione

Nella comunicazione socket tradizionale, un server gestisce un client per volta bloccando l'esecuzione durante l'I/O. Il multithreading consente di gestire **molteplici client contemporaneamente**, consentendo al listener di accettare nuove connessioni mentre i gestori attendono dati. La concorrenza non garantisce un aumento delle prestazioni: thread e connessioni richiedono risorse.

---

## Capitolo 1: Fondamenti del Multithreading nei Socket

### 1.1 Il Problema del Modello Single-Thread

Il server tradizionale con un unico thread:

```java
ServerSocket serverSocket = new ServerSocket(5000);
while (true) {
    Socket clientSocket = serverSocket.accept(); // Blocca qui
    // Elabora il client
    // Durante questo tempo, altri client rimangono in coda
}
```

**Limitazioni:**
- Un solo client alla volta
- Se l'elaborazione è lenta, gli altri client attendono
- Inefficiente per server con molte connessioni

### 1.2 La Soluzione Multithreading

Creare un nuovo thread per ogni client connesso:

```java
ServerSocket serverSocket = new ServerSocket(5000);
while (true) {
    Socket clientSocket = serverSocket.accept();
    // Crea un nuovo thread per gestire il client
    new Thread(new ClientHandler(clientSocket)).start();
}
```

**Vantaggi:**
- Gestione contemporanea di più client
- Server rimane responsivo
- Ogni gestore ha il proprio stack; gli oggetti condivisi restano nella stessa memoria del processo

---

## Capitolo 2: Implementazione di Base

### 2.1 La Classe ClientHandler

La classe `ClientHandler` implementa `Runnable` e gestisce un singolo client. Salvala in `ClientHandler.java`, con gli import riportati sotto. Salva poi il server in `MultiThreadedServer.java` nella stessa cartella. I primi due blocchi della guida sono invece frammenti illustrativi da inserire in un metodo.

Il protocollo è a righe UTF-8: il client invia una riga terminata da newline, il server risponde con `Echo: ` e il testo ricevuto. La sessione resta aperta fino a EOF o timeout di lettura. Non inviare una richiesta HTTP a questo echo aspettandoti una pagina web.

`socket` e `clientID` non cambiano dopo la costruzione. Il contatore atomico assegna identificativi distinti anche se in futuro i gestori vengono costruiti da più thread; non misura i client attivi. Nell'accept loop di questa guida sarebbe sufficiente un contatore confinato al listener.

```java
import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Un gestore per connessione: il try chiude sempre il socket di sua proprietà. */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private static final AtomicInteger clientCounter = new AtomicInteger();
    private final int clientID;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.clientID = clientCounter.incrementAndGet();
    }

    @Override
    public void run() {
        try (Socket owned = socket) {
            socket.setSoTimeout(30000);
            System.out.println("Client #" + clientID + " connesso da " + 
                socket.getInetAddress().getHostAddress());

            // Stream di input/output
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8);

            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("Client #" + clientID + " ricevuto: " + request);
                
                String response = processRequest(request);
                out.println(response); // println con autoflush rende disponibile la risposta.
                if (out.checkError()) break; // PrintWriter conserva gli errori di scrittura.
            }

            System.out.println("Client #" + clientID + " disconnesso");

        } catch (IOException e) {
            System.err.println("Errore client #" + clientID + ": " + e.getMessage());
        }
    }

    private String processRequest(String request) {
        // Logica di elaborazione
        return "Echo: " + request;
    }
}
```

### 2.2 Il Server Multithreaded

```java
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/** Solo il listener chiama accept(); ogni gestore diventa proprietario del proprio socket. */
public class MultiThreadedServer {
    private static final int PORT = 5000;

    public void runServer() throws IOException {
        try (ServerSocket listener = new ServerSocket(PORT)) {
            System.out.println("Server avviato sulla porta " + PORT);
            while (true) {
                Socket socket = listener.accept();
                // start() avvia il lavoro altrove; chiamare run() lo renderebbe sequenziale.
                new Thread(new ClientHandler(socket)).start();
            }
        } // Un errore del listener termina il ciclo: non ripetere all'infinito un accept fallito.
    }

    public static void main(String[] args) throws IOException {
        new MultiThreadedServer().runServer();
    }
}
```

---

## Compilazione e prova con due client

```sh
javac -encoding UTF-8 ClientHandler.java MultiThreadedServer.java
java MultiThreadedServer
```

Apri due terminali con un client TCP a righe. Puoi adattare il [client Runnable](esempi/es02-Runnable/TcpClient.java) alla porta 5000; su Linux con netcat installato puoi usare `nc localhost 5000`. Lascia il primo client connesso senza inviare: il secondo deve ricevere ugualmente l'echo. Poi prova caratteri accentati, più righe nella stessa scrittura ed EOF. `readLine()` attende newline o EOF, non la fine di un pacchetto TCP.

Il timeout di 30 secondi riguarda le letture del singolo socket. `PrintWriter` usa UTF-8 e autoflush con `println`; per scritture con `print` occorre un `flush()` esplicito. Un solo gestore legge ciascun input. Il registro di utenti di una chat richiederà invece [sincronizzazione](03-Sincronizzazione-Risorse-Condivise.md).

Questo server introduttivo crea thread senza un limite globale. Ctrl+C termina il processo; per coordinare esplicitamente chiusura del listener, socket attivi e task in coda passa alla [gestione avanzata](04-Multithreading-Avanzato.md). L'ID di un thread o il suo nome aiutano nei log, ma non dimostrano da soli che due gestori siano avanzati contemporaneamente.

---

## Capitolo 3: Esercizi Proposti

### Esercizio 1: Server Echo Multithreaded Base
Implementare un server che:
- Accetta multiple connessioni simultanee
- Rimanda al client ogni messaggio ricevuto
- Visualizza l'indirizzo IP di ogni client
- Gestisce il disconnessione correttamente

### Esercizio 2: Server Calcolatrice
Creare un server che:
- Riceve operazioni matematiche (es: "ADD 5 3")
- Esegue il calcolo in un thread separato
- Rimanda il risultato al client
- (OPZIONALE) Usa un thread pool con max 5 thread
- (OPZIONALE) Implementa timeout su socket

---

## Capitolo 4: Domande di Autovalutazione

### Domanda 1
Quale affermazione è corretta riguardante il multithreading nei socket?

A) Un nuovo thread per ogni client aumenta sempre le prestazioni
B) Il multithreading consente di gestire più client contemporaneamente
C) Non è necessario sincronizzare l'accesso alle risorse condivise
D) ExecutorService è obbligatorio per il multithreading

**Risposta corretta: B**
Il multithreading consente a più client di avanzare nello stesso intervallo di tempo; il parallelismo effettivo dipende dalle risorse disponibili. L'opzione A è scorretta perché creare infiniti thread degrada le prestazioni. C è errata per motivi di sicurezza dei dati. D è falsa perché si può usare `new Thread()` direttamente.

---

### Domanda 2
In che caso è necessario usare la sincronizzazione nei socket multithreaded?

A) Sempre, per ogni operazione
B) Quando accessi concorrenti incompatibili riguardano uno stato condiviso
C) Mai, perché i thread sono isolati
D) Solo in caso di errori

**Risposta corretta: B**
La sincronizzazione serve per accessi concorrenti incompatibili allo stato condiviso, per esempio quando almeno un thread modifica contatori, liste o file. Un oggetto immutabile condiviso non richiede un lock per ogni lettura. In questo esempio ogni socket e i suoi stream appartengono a un solo gestore. Se più thread inviano sullo stesso stream, occorre proteggere l’intero messaggio, non solo le singole chiamate di scrittura.

---

### Domanda 3
Cosa accade se una eccezione non è catturata nel metodo `run()` di un thread?

A) Il server si arresta completamente
B) L'eccezione propaga al thread principale
C) Il thread termina, ma il server continua
D) Viene lanciato un nuovo thread automaticamente

**Risposta corretta: C**
Se un'eccezione non è catturata in `run()`, il thread termina ma il server e gli altri thread continuano. Per gestire eccezioni non catturate si usa `setUncaughtExceptionHandler()`.

---

## Risposte Corrette - Riepilogo

| Domanda | Risposta | Spiegazione |
|---------|----------|-------------|
| 1 | B | Il multithreading consente gestione concorrente di client |
| 2 | B | La sincronizzazione è necessaria per risorse condivise |
| 3 | C | Un'eccezione non catturata termina il thread, non il server |

---

## Conclusione

Il multithreading nei socket è essenziale per costruire server scalabili e responsive. Le chiavi del successo sono:

1. **Usare ExecutorService** per gestire i thread in modo efficiente
2. **Sincronizzare correttamente** l'accesso alle risorse condivise
3. **Gestire le eccezioni** adeguatamente
4. **Implementare timeout e graceful shutdown** per robustezza
5. **Testare** almeno due client contemporanei, EOF, errori e arresto del servizio

---

## Navigazione del modulo

- [Indice e istruzioni di laboratorio](README.md)
- [Prima: thread in Java](01-Multithreading-Java.md)
- [Dopo: sincronizzazione e risorse condivise](03-Sincronizzazione-Risorse-Condivise.md)
