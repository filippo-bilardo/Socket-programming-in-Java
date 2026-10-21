# ES03 - Chat Multi-Utente Sincronizzata

## Descrizione dell'Esercitazione

Sviluppare un'applicazione di chat multithreaded dove:

1. **Server** - Gestisce molteplici client simultanei
2. **Registrazione** - Ogni client comunica il proprio nome alla prima connessione
3. **Broadcast** - Ogni messaggio inviato da un client viene visualizzato su **tutti** gli altri client
4. **Graceful Shutdown** - Il client termina quando digita `quit`
5. **Server Shutdown** - Il server termina quando **tutti i client** sono disconnessi

## Obiettivi Didattici

- ✅ Multithreading sincronizzato
- ✅ Broadcast di messaggi a molteplici client
- ✅ Gestione liste di client attivi
- ✅ Sincronizzazione tra thread
- ✅ Graceful disconnect e shutdown
- ✅ Stato della chat in tempo reale

---

## Struttura del Progetto

```
ES03-ChatMultiUtente/
├── src/
│   ├── ChatServer.java                # Server principale
│   ├── ClientHandler.java             # Gestore client
│   ├── ChatClient.java                # Client chat
│   └── ClientInfo.java                # Info del client
├── doc/
│   └── ESERCITAZIONE.md              # Questa guida
└── README.md                          # Quick start
```

---

## Parte 1: ClientInfo - Informazioni Client

Classe semplice per rappresentare un client connesso.

### ClientInfo.java

```java
import java.net.Socket;
import java.io.*;

public class ClientInfo {
    private String username;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientInfo(String username, Socket socket) throws IOException {
        this.username = username;
        this.socket = socket;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));
    }

    // Getter
    public String getUsername() {
        return username;
    }

    public Socket getSocket() {
        return socket;
    }

    public PrintWriter getOut() {
        return out;
    }

    public BufferedReader getIn() {
        return in;
    }

    public void close() throws IOException {
        socket.close();
    }

    @Override
    public String toString() {
        return username + " [" + socket.getInetAddress().getHostAddress() + "]";
    }
}
```

---

## Parte 2: ClientHandler - Gestore Client

Gestisce la comunicazione con un singolo client.

### ClientHandler.java

```java
import java.io.*;

public class ClientHandler implements Runnable {
    private ClientInfo client;
    private ChatServer server;

    public ClientHandler(ClientInfo client, ChatServer server) {
        this.client = client;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            System.out.println("✅ " + client + " connesso");

            // Notifica agli altri client che questo utente è entrato
            server.broadcastMessage("[SISTEMA] 🟢 " + client.getUsername() + 
                " è entrato in chat");
            
            // Invia lista client attuali
            server.sendClientList(client);

            // Leggi messaggi dal client
            String message;
            while ((message = client.getIn().readLine()) != null) {
                
                // Controlla se il client vuole uscire
                if (message.equalsIgnoreCase("quit")) {
                    break;
                }

                // Filtra messaggi vuoti
                if (message.trim().isEmpty()) {
                    continue;
                }

                // Broadcast del messaggio
                String formattedMessage = client.getUsername() + ": " + message;
                System.out.println(formattedMessage);
                server.broadcastMessage(formattedMessage);
            }

        } catch (IOException e) {
            System.err.println("❌ Errore client " + client.getUsername() + 
                ": " + e.getMessage());
        } finally {
            // Disconnessione
            disconnectClient();
        }
    }

    private void disconnectClient() {
        try {
            client.close();
            System.out.println("❌ " + client + " disconnesso");

            // Notifica agli altri
            server.broadcastMessage("[SISTEMA] 🔴 " + client.getUsername() + 
                " ha lasciato la chat");

            // Rimuovi dalla lista
            server.removeClient(client);

            // Se no sono rimasti client, il server si spegne
            if (server.getClientCount() == 0) {
                System.out.println("\n🛑 Tutti i client disconnessi. Server in arresto...\n");
                server.shutdown();
            }

        } catch (IOException e) {
            System.err.println("❌ Errore nella disconnessione: " + e.getMessage());
        }
    }
}
```

---

## Parte 3: ChatServer - Server Principale

Server che gestisce molteplici client simultanei.

### ChatServer.java

```java
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatServer {
    private ServerSocket serverSocket;
    private List<ClientInfo> clients = Collections.synchronizedList(new ArrayList<>());
    private static final int PORT = 5000;
    private static final DateTimeFormatter formatter = 
        DateTimeFormatter.ofPattern("HH:mm:ss");
    private volatile boolean running = true;

    public ChatServer() throws IOException {
        serverSocket = new ServerSocket(PORT);
        printBanner();
    }

    private void printBanner() {
        System.out.println("\n╔═════════════════════════════════════════╗");
        System.out.println("║   Chat Multi-Utente - Server             ║");
        System.out.println("╠═════════════════════════════════════════╣");
        System.out.println("║ 🔌 Porta: " + PORT + "                        ║");
        System.out.println("║ 👥 In attesa di client...               ║");
        System.out.println("╚═════════════════════════════════════════╝\n");
    }

    public void start() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                
                // Leggi il nome utente dal client
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                
                String username = reader.readLine();
                
                if (username == null || username.trim().isEmpty()) {
                    System.out.println("❌ Client connesso senza nome, disconnesso");
                    socket.close();
                    continue;
                }

                username = username.trim();

                // Verifica se il nome è già in uso
                if (isUsernameTaken(username)) {
                    PrintWriter out = new PrintWriter(
                        socket.getOutputStream(), true);
                    out.println("[SISTEMA] ❌ Nome utente già in uso! Disconnesso.");
                    socket.close();
                    System.out.println("❌ Tentativo di registrazione con nome duplicato: " + username);
                    continue;
                }

                // Crea ClientInfo
                ClientInfo client = new ClientInfo(username, socket);
                
                // Aggiungi alla lista
                clients.add(client);
                
                System.out.println("📝 Registrato: " + username + " [Totale: " + 
                    clients.size() + "]");

                // Crea thread per gestire il client
                new Thread(new ClientHandler(client, this)).start();

            } catch (IOException e) {
                if (running) {
                    System.err.println("❌ Errore accettazione: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Verifica se un nome utente è già registrato
     */
    private synchronized boolean isUsernameTaken(String username) {
        return clients.stream()
            .anyMatch(c -> c.getUsername().equalsIgnoreCase(username));
    }

    /**
     * Invia un messaggio a tutti i client
     */
    public synchronized void broadcastMessage(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String formattedMsg = "[" + timestamp + "] " + message;
        
        for (ClientInfo client : clients) {
            try {
                client.getOut().println(formattedMsg);
            } catch (Exception e) {
                System.err.println("❌ Errore invio a " + client.getUsername() + 
                    ": " + e.getMessage());
            }
        }
    }

    /**
     * Invia lista client a un client specifico
     */
    public synchronized void sendClientList(ClientInfo requestor) {
        StringBuilder list = new StringBuilder();
        list.append("[SISTEMA] 👥 Utenti online (").append(clients.size()).append("): ");
        
        for (int i = 0; i < clients.size(); i++) {
            if (i > 0) list.append(", ");
            if (!clients.get(i).equals(requestor)) {
                list.append(clients.get(i).getUsername());
            } else {
                list.append(clients.get(i).getUsername()).append(" (te)");
            }
        }
        
        requestor.getOut().println(list.toString());
    }

    /**
     * Rimuove un client dalla lista
     */
    public synchronized void removeClient(ClientInfo client) {
        clients.remove(client);
        System.out.println("📊 Client rimasti: " + clients.size());
    }

    /**
     * Ritorna il numero di client connessi
     */
    public synchronized int getClientCount() {
        return clients.size();
    }

    /**
     * Spegne il server
     */
    public void shutdown() {
        running = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void main(String[] args) throws IOException {
        ChatServer server = new ChatServer();
        server.start();
    }
}
```

---

## Parte 4: ChatClient - Client Chat

Client che si connette al server e interagisce con la chat.

### ChatClient.java

```java
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public ChatClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    /**
     * Richiede il nome utente
     */
    private void requestUsername() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Inserisci il tuo nome utente: ");
        username = scanner.nextLine().trim();
        
        if (username.isEmpty()) {
            username = "Anonimo";
        }
        
        // Invia il nome al server
        out.println(username);
    }

    /**
     * Avvia il client
     */
    public void start() {
        try {
            System.out.println("\n╔════════════════════════════════════╗");
            System.out.println("║   Chat Multi-Utente - Client       ║");
            System.out.println("╚════════════════════════════════════╝\n");

            requestUsername();

            System.out.println("✅ Connesso come: " + username);
            System.out.println("📝 Digita 'quit' per uscire\n");

            // Thread per ricevere messaggi dal server
            Thread receiveThread = new Thread(new ReceiveMessages());
            receiveThread.setDaemon(true);
            receiveThread.start();

            // Thread principale per inviare messaggi
            sendMessages();

        } catch (IOException e) {
            System.err.println("❌ Errore connessione: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    /**
     * Legge messaggi da tastiera e li invia al server
     */
    private void sendMessages() {
        try {
            BufferedReader userInput = new BufferedReader(
                new InputStreamReader(System.in));
            
            String message;
            while ((message = userInput.readLine()) != null) {
                
                // Controlla se l'utente vuole uscire
                if (message.equalsIgnoreCase("quit")) {
                    out.println("quit");
                    break;
                }

                // Invia il messaggio
                if (!message.trim().isEmpty()) {
                    out.println(message);
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Errore lettura input: " + e.getMessage());
        }
    }

    /**
     * Inner class per ricevere messaggi dal server
     */
    private class ReceiveMessages implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(message);
                    System.out.print("> "); // Prompt
                }
            } catch (IOException e) {
                // Silenzioso se il client è chiuso volontariamente
            }
        }
    }

    /**
     * Disconnette il client
     */
    private void disconnect() {
        try {
            socket.close();
            System.out.println("\n👋 Disconnesso dal server");
        } catch (IOException e) {
            System.err.println("❌ Errore disconnessione: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 5000;

        // Permette di passare host e port come argomenti
        if (args.length >= 1) host = args[0];
        if (args.length >= 2) port = Integer.parseInt(args[1]);

        try {
            new ChatClient(host, port).start();
        } catch (IOException e) {
            System.err.println("❌ Impossibile connettersi: " + e.getMessage());
        }
    }
}
```

---

## Istruzioni di Esecuzione

### 1. Compilazione

```bash
cd src
javac *.java
```

### 2. Avvia il Server (Terminale 1)

```bash
java ChatServer
```

Output atteso:
```
╔═════════════════════════════════════════╗
║   Chat Multi-Utente - Server             ║
╠═════════════════════════════════════════╣
║ 🔌 Porta: 5000                        ║
║ 👥 In attesa di client...               ║
╚═════════════════════════════════════════╝
```

### 3. Avvia il Client 1 (Terminale 2)

```bash
java ChatClient
```

Digita il nome: `Alice`

```
╔════════════════════════════════════╗
║   Chat Multi-Utente - Client       ║
╚════════════════════════════════════╝

Inserisci il tuo nome utente: Alice
✅ Connesso come: Alice
📝 Digita 'quit' per uscire

[12:34:56] [SISTEMA] 👥 Utenti online (1): Alice (te)
```

### 4. Avvia il Client 2 (Terminale 3)

```bash
java ChatClient
```

Digita il nome: `Bob`

Nel terminale 2 vedrai:
```
[12:34:58] [SISTEMA] 🟢 Bob è entrato in chat
[12:34:58] [SISTEMA] 👥 Utenti online (2): Alice (te), Bob
```

### 5. Invia Messaggi

Nel terminale 2 (Alice):
```
> Ciao a tutti!
```

Vedrai in tutti i terminali:
```
[12:35:00] Alice: Ciao a tutti!
```

Nel terminale 3 (Bob):
```
> Ciao Alice!
```

Vedrai in tutti i terminali:
```
[12:35:02] Bob: Ciao Alice!
```

### 6. Esci dalla Chat

Nel terminale 2 (Alice):
```
> quit
```

Vedrai:
```
👋 Disconnesso dal server
```

E negli altri terminali:
```
[12:35:10] [SISTEMA] 🔴 Alice ha lasciato la chat
```

### 7. Ultimo Client Esce

Quando l'ultimo client digita `quit`, il server si spegne automaticamente:
```
🛑 Tutti i client disconnessi. Server in arresto...
```

---

## Diagramma di Flusso

```
┌─────────────┐
│   Client 1  │
│   (Alice)   │
└──────┬──────┘
       │
       │ Connessione + Nome
       ▼
┌──────────────────────┐
│    Chat Server       │
│  (Port 5000)         │
│                      │
│ • ClientInfo         │
│ • Broadcast          │
│ • Sincronizzazione   │
└──────┬───────────────┘
       │
       ├─────────────────┬────────────────┐
       ▼                 ▼                 ▼
    Client 2          Client 3          Client 4
    (Bob)             (Carol)           (David)
```

---

## Esempio di Sessione Completa

```
[TERMINALE SERVER]
📝 Registrato: Alice [Totale: 1]
✅ [127.0.0.1:54321] connesso
📝 Registrato: Bob [Totale: 2]
✅ [127.0.0.1:54322] connesso
Bob: Ciao!
Alice: Ciao Bob!
❌ Bob disconnesso
📊 Client rimasti: 1
🛑 Tutti i client disconnessi. Server in arresto...

[CLIENT ALICE]
✅ Connesso come: Alice
[12:35:01] [SISTEMA] 👥 Utenti online (1): Alice (te)
[12:35:03] [SISTEMA] 🟢 Bob è entrato in chat
[12:35:03] [SISTEMA] 👥 Utenti online (2): Alice (te), Bob
> Ciao Bob!
[12:35:04] Alice: Ciao Bob!
[12:35:05] Bob: Ciao!
[12:35:06] [SISTEMA] 🔴 Bob ha lasciato la chat

[CLIENT BOB]
✅ Connesso come: Bob
[12:35:03] [SISTEMA] 👥 Utenti online (2): Alice, Bob (te)
[12:35:04] Alice: Ciao Bob!
> Ciao!
[12:35:05] Bob: Ciao!
[12:35:06] Disconnesso dal server
👋 Arrivederci!
```

---

## Domande di Autovalutazione

### Domanda 1
Perché è necessario sincronizzare la lista `clients`?

A) Per velocità di esecuzione  
B) Per evitare race condition quando più thread la modificano  
C) Per risparmiare memoria  
D) Non è necessario  

**Risposta corretta: B**

Più `ClientHandler` potrebbero accedere/modificare la lista contemporaneamente. `Collections.synchronizedList()` previene corruzione.

---

### Domanda 2
Come viene gestita la broadcast dei messaggi?

A) Un thread per ogni messaggio  
B) Tramite il metodo `broadcastMessage()` sincronizzato  
C) Ogni client invia direttamente agli altri  
D) Un database centralizzato  

**Risposta corretta: B**

Il metodo `broadcastMessage()` in `ChatServer` itera su tutti i client e invia il messaggio.

---

### Domanda 3
Cosa accade quando un client scrive "quit"?

A) Il messaggio viene inviato agli altri  
B) Il `ClientHandler` esce dal loop e disconnette il client  
C) Il server viene interrotto  
D) Errore di connessione  

**Risposta corretta: B**

La parola "quit" fa break dal loop while in `ClientHandler.run()`, eseguendo la disconnessione.

---

### Domanda 4
Quando il server si spegne?

A) Manualmente con CTRL+C  
B) Quando tutti i client sono disconnessi  
C) Dopo 1 ora  
D) Mai, rimane sempre acceso  

**Risposta corretta: B**

In `disconnectClient()`, se `getClientCount() == 0`, il server chiama `shutdown()`.

---

### Domanda 5
Come un client riceve messaggi dal server?

A) Con polling (chiedere continuamente)  
B) Con un thread daemon che legge continuamente  
C) Manualmente richiamando un metodo  
D) Non li riceve  

**Risposta corretta: B**

La classe interna `ReceiveMessages` è un `Runnable` che gira in un thread daemon e legge continuamente da `in`.

---

## Risposte Corrette

| Q | Risposta | Spiegazione |
|---|----------|-------------|
| 1 | B | `Collections.synchronizedList()` previene race condition |
| 2 | B | `broadcastMessage()` sincronizzato itera su tutti |
| 3 | B | "quit" causa exit dal loop e disconnessione |
| 4 | B | Server chiama `shutdown()` quando `clientCount == 0` |
| 5 | B | Thread daemon legge continuamente da `BufferedReader` |

---

## Estensioni Possibili

1. **Chat Rooms** - Dividere in stanze/canali
2. **Messaggi Privati** - PM tra due utenti
3. **Comando Help** - Mostrare comandi disponibili
4. **Bloccare Utenti** - `/block username`
5. **Cronologia** - Salvare messaggi in file
6. **Autenticazione** - Username + Password
7. **Statistiche** - Contare messaggi per utente
8. **Kick** - Admin può rimuovere utenti
9. **Emoji** - Supporto emoji nei messaggi
10. **Geolocalizzazione** - Mostrare città del client

---

## Troubleshooting

### Problema: "Address already in use"
```
java.net.BindException: Address already in use
```
**Soluzione:** Cambia porta o aspetta qualche secondo prima di riavviare il server.

### Problema: Client non riceve messaggi
**Soluzione:** Verifica che il thread `ReceiveMessages` sia avviato correttamente.

### Problema: Server non si spegne
**Soluzione:** Verifica che `ClientHandler.disconnect()` chiami `server.removeClient()`.

### Problema: Nome duplicato non viene rifiutato
**Soluzione:** Verifica che `isUsernameTaken()` sia sincronizzato e che il controllo sia prima della creazione del `ClientInfo`.

---

## Conclusione

Questa esercitazione copre:
- ✅ Multithreading complesso con sincronizzazione
- ✅ Broadcast sincronizzato a molteplici client
- ✅ Gestione di liste thread-safe
- ✅ Graceful shutdown con controllo di stato
- ✅ Comunicazione bidirezionale
- ✅ Pattern Client-Server avanzato

Perfetto per comprendere architetture di chat e applicazioni collaborative real-time!