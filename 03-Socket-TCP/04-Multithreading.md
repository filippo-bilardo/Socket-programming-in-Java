# Comunicazione Multithreading con Socket TCP in Java

## Introduzione

Nella comunicazione socket tradizionale, un server gestisce un client per volta bloccando l'esecuzione durante l'I/O. Il multithreading consente di gestire **molteplici client contemporaneamente**, mantenendo la responsività dell'applicazione e massimizzando l'utilizzo delle risorse di sistema.

Questa guida approfondisce i concetti, le strategie e le implementazioni per una comunicazione socket robusta e scalabile.

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
- Ogni client ha il suo contesto di esecuzione isolato

---

## Capitolo 2: Implementazione di Base

### 2.1 La Classe ClientHandler

La classe `ClientHandler` implementa `Runnable` e gestisce un singolo client:

```java
public class ClientHandler implements Runnable {
    private Socket socket;
    private static int clientCounter = 0;
    private int clientID;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.clientID = ++clientCounter;
    }

    @Override
    public void run() {
        try {
            System.out.println("Client #" + clientID + " connesso da " + 
                socket.getInetAddress().getHostAddress());

            // Stream di input/output
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);

            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("Client #" + clientID + " ricevuto: " + request);
                
                String response = processRequest(request);
                out.println(response);
            }

            socket.close();
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
public class MultiThreadedServer {
    private ServerSocket serverSocket;
    private static final int PORT = 5000;

    public MultiThreadedServer() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Server avviato sulla porta " + PORT);
    }

    public void start() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            } catch (IOException e) {
                System.err.println("Errore nell'accettare connessione: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new MultiThreadedServer().start();
    }
}
```

---

## Capitolo 7: Esercizi Proposti

### Esercizio 1: Server Echo Multithreaded Base
Implementare un server che:
- Accetta multiple connessioni simultanee
- Rimanda al client ogni messaggio ricevuto
- Visualizza l'indirizzo IP di ogni client
- Gestisce il disconnetimento correttamente

### Esercizio 2: Server Calcolatrice
Creare un server che:
- Riceve operazioni matematiche (es: "ADD 5 3")
- Esegue il calcolo in un thread separato
- Rimanda il risultato al client
- (OPZIONALE) Usa un thread pool con max 5 thread
- (OPZIONALE) Implementa timeout su socket

---

## Capitolo 8: Domande di Autovalutazione

### Domanda 1
Quale affermazione è corretta riguardante il multithreading nei socket?

A) Un nuovo thread per ogni client aumenta sempre le prestazioni
B) Il multithreading consente di gestire più client contemporaneamente
C) Non è necessario sincronizzare l'accesso alle risorse condivise
D) ExecutorService è obbligatorio per il multithreading

**Risposta corretta: B**
Il multithreading consente effettivamente di gestire più client in parallelo. L'opzione A è scorretta perché creare infiniti thread degrada le prestazioni. C è errata per motivi di sicurezza dei dati. D è falsa perché si può usare `new Thread()` direttamente.

---

### Domanda 3
In che caso è necessario usare la sincronizzazione nei socket multithreaded?

A) Sempre, per ogni operazione
B) Solo quando più thread accedono alla stessa risorsa non thread-safe
C) Mai, perché i thread sono isolati
D) Solo in caso di errori

**Risposta corretta: B**
La sincronizzazione è necessaria solo quando risorse condivise (contatori, liste, file) sono accedute da più thread. I BufferedReader/PrintWriter sono già thread-safe per socket singoli.

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

