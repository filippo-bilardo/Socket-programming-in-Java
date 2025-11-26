# 🎓 ESERCITAZIONE: Socket TCP Multithreading in Java "Chat Server"

> *Verifica delle competenze sui socket TCP multithreading - Sistemi e Reti 3*

---

## 📋 **INFORMAZIONI GENERALI**

**Materia:** Sistemi e Reti  
**Argomento:** Socket Programming TCP Multithreading in Java  
**Tempo stimato:** 3-4 ore  
**Difficoltà:** ⭐⭐⭐⭐ (Avanzata)  
**Modalità:** Individuale o a coppie  

---

## 🎯 **OBIETTIVI DIDATTICI**

Al termine di questa esercitazione lo studente sarà in grado di:

- ✅ **Implementare** un server TCP multithreading che gestisce più client contemporaneamente
- ✅ **Creare** thread dedicati per ogni connessione client
- ✅ **Gestire** la sincronizzazione tra thread per risorse condivise
- ✅ **Implementare** broadcast di messaggi tra client multipli
- ✅ **Utilizzare** strutture dati thread-safe (Collections sincronizzate)
- ✅ **Gestire** correttamente la disconnessione di client multipli
- ✅ **Applicare** pattern di programmazione concorrente
- ✅ **Documentare** e testare applicazioni multi-client

---

## 📚 **PREREQUISITI**

### Conoscenze Teoriche Richieste:
- 🔌 **Socket TCP/IP:** Client-server, comunicazione bidirezionale
- ⚡ **Multithreading Java:** Thread, Runnable, sincronizzazione
- 🔒 **Sincronizzazione:** `synchronized`, Collections thread-safe
- ☕ **Java I/O:** BufferedReader, PrintWriter, gestione stream
- 📝 **Collections:** List, Set, gestione strutture dati

### Strumenti Necessari:
- ☕ **Java JDK** 11 o superiore
- 💻 **IDE** o editor di testo (VS Code, IntelliJ, Eclipse)
- 🖥️ **Terminale** per compilazione ed esecuzione multipla

---

## 📖 **TRACCIA DELL'ESERCIZIO**

### 🚀 **Applicazione: "Chat Server Multithreading"**

Realizzare un'applicazione client-server di chat dove:

1. **Il SERVER:**
   - Ascolta sulla porta `9999`
   - Accetta **connessioni multiple** simultanee
   - Crea un **thread dedicato** per ogni client connesso
   - Mantiene una **lista thread-safe** di tutti i client connessi
   - Implementa il **broadcast**: ogni messaggio viene inviato a tutti i client
   - Gestisce l'**assegnazione automatica di username** (Client1, Client2, ecc.)
   - Notifica tutti quando un client **si connette o disconnette**
   - Visualizza statistiche: numero client connessi, messaggi inviati
   - Gestisce la **chiusura graceful** con comando "SHUTDOWN"

2. **Il CLIENT:**
   - Si connette al server localhost:9999
   - Riceve un **username automatico** dal server
   - Ha **due thread**:
     - Thread di **ricezione**: ascolta messaggi dal server
     - Thread di **invio**: legge input utente e invia al server
   - Visualizza messaggi di altri client con formato: `[Username]: messaggio`
   - Notifica quando altri client si connettono/disconnettono
   - Comando `/quit` per disconnessione pulita
   - Comando `/list` per vedere lista utenti connessi

### 📐 **Esempi di Interazione:**

```
=== SERVER ===
Server avviato sulla porta 9999
In attesa di connessioni...

[CONNESSIONE] Client1 connesso da 127.0.0.1
[CONNESSIONE] Client2 connesso da 127.0.0.1
[CLIENT1] Ciao a tutti!
[BROADCAST] Messaggio inviato a 2 client
[CLIENT2] Ciao Client1!
[BROADCAST] Messaggio inviato a 2 client
[DISCONNESSIONE] Client1 disconnesso
Client attivi: 1

=== CLIENT 1 ===
Connessione al server riuscita!
Sei stato registrato come: Client1

[SERVER]: Benvenuto Client1! Client connessi: 1
[SERVER]: Client2 si è connesso. Client online: 2

> Ciao a tutti!
[TU]: Ciao a tutti!
[Client2]: Ciao Client1!
> /quit
Disconnessione in corso...

=== CLIENT 2 ===
Connessione al server riuscita!
Sei stato registrato come: Client2

[SERVER]: Benvenuto Client2! Client connessi: 2
[Client1]: Ciao a tutti!

> Ciao Client1!
[TU]: Ciao Client1!
[SERVER]: Client1 si è disconnesso. Client online: 1
```

---

## 🛠️ **PASSAGGI DA SEGUIRE**

### 📝 **STEP 1: Analisi e Progettazione (20-30 minuti)**

#### 1.1 Analizza il Problema
- Identifica le **classi necessarie**:
  - `ChatServer`: Server principale
  - `ClientHandler`: Thread per gestione singolo client
  - `ChatClient`: Client con thread ricezione e invio
- Definisci le **strutture dati condivise**:
  - Lista thread-safe dei ClientHandler connessi
  - Contatore clienti per username automatici
- Pianifica la **sincronizzazione**:
  - Accesso alla lista client (add/remove/broadcast)
  - Contatore client

#### 1.2 Schema dell'Architettura
```
┌─────────────────────────────────────────────┐
│           ChatServer (Main Thread)          │
│  - ServerSocket sulla porta 9999            │
│  - Lista<ClientHandler> sincronizzata       │
│  - Accept loop per nuovi client             │
└─────────────────────────────────────────────┘
                    │
        ┌───────────┼───────────┐
        │           │           │
   ┌────▼────┐ ┌────▼────┐ ┌────▼────┐
   │ Client  │ │ Client  │ │ Client  │
   │Handler1 │ │Handler2 │ │Handler3 │
   │(Thread) │ │(Thread) │ │(Thread) │
   └────┬────┘ └────┬────┘ └────┬────┘
        │           │           │
   ┌────▼────┐ ┌────▼────┐ ┌────▼────┐
   │ Client1 │ │ Client2 │ │ Client3 │
   │ - Thread│ │ - Thread│ │ - Thread│
   │   RX    │ │   RX    │ │   RX    │
   │ - Thread│ │ - Thread│ │ - Thread│
   │   TX    │ │   TX    │ │   TX    │
   └─────────┘ └─────────┘ └─────────┘
```

#### 1.3 Protocollo di Comunicazione
```
Client → Server:
- Messaggio normale: "testo del messaggio"
- Comando lista utenti: "/list"
- Comando quit: "/quit"

Server → Client:
- Messaggio broadcast: "[Username]: testo"
- Notifica server: "[SERVER]: testo"
- Lista utenti: "[SERVER]: Utenti online: User1, User2, User3"
```

---

### ⚙️ **STEP 2: Implementazione Server (60-90 minuti)**

#### 2.1 Struttura Base del Server
Crea il file `ChatServer.java`:

```java
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORTA = 9999;
    private static List<ClientHandler> clients = 
        Collections.synchronizedList(new ArrayList<>());
    private static int clientCounter = 0;
    
    public static void main(String[] args) {
        System.out.println("=== CHAT SERVER MULTITHREADING ===");
        System.out.println("Server avviato sulla porta " + PORTA);
        System.out.println("In attesa di connessioni...\n");
        
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            while (true) {
                // TODO: Accetta client e crea thread
            }
        } catch (IOException e) {
            System.err.println("Errore server: " + e.getMessage());
        }
    }
    
    // Metodo per broadcast messaggi a tutti i client
    public static void broadcast(String messaggio, ClientHandler mittente) {
        // TODO: Implementa broadcast
    }
    
    // Metodo per rimuovere client disconnesso
    public static void removeClient(ClientHandler client) {
        // TODO: Rimuovi client dalla lista
    }
}
```

#### 2.2 Classe ClientHandler (Thread per Client)
Crea la classe interna o separata `ClientHandler.java`:

```java
class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    
    public ClientHandler(Socket socket, int clientNumber) {
        this.socket = socket;
        this.username = "Client" + clientNumber;
    }
    
    @Override
    public void run() {
        try {
            // Setup stream I/O
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            
            // Notifica connessione
            System.out.println("[CONNESSIONE] " + username + 
                " connesso da " + socket.getInetAddress().getHostAddress());
            
            // Invia messaggio di benvenuto
            sendMessage("[SERVER]: Benvenuto " + username + 
                "! Client connessi: " + ChatServer.clients.size());
            
            // Notifica altri client
            ChatServer.broadcast("[SERVER]: " + username + 
                " si è connesso. Client online: " + ChatServer.clients.size(), 
                this);
            
            // Loop ricezione messaggi
            String messaggio;
            while ((messaggio = in.readLine()) != null) {
                // TODO: Gestisci comandi e messaggi
            }
            
        } catch (IOException e) {
            System.err.println("[ERRORE] " + username + ": " + e.getMessage());
        } finally {
            disconnect();
        }
    }
    
    public void sendMessage(String messaggio) {
        out.println(messaggio);
    }
    
    private void disconnect() {
        // TODO: Gestisci disconnessione
    }
    
    public String getUsername() {
        return username;
    }
}
```

#### 2.3 Componenti da Implementare nel Server:

**A) Accept Loop nel Main:**
```java
while (true) {
    Socket clientSocket = serverSocket.accept();
    
    synchronized (clients) {
        clientCounter++;
        ClientHandler handler = new ClientHandler(clientSocket, clientCounter);
        clients.add(handler);
        new Thread(handler).start();
    }
}
```

**B) Metodo Broadcast:**
```java
public static void broadcast(String messaggio, ClientHandler mittente) {
    synchronized (clients) {
        int count = 0;
        for (ClientHandler client : clients) {
            if (client != mittente) {
                client.sendMessage(messaggio);
                count++;
            }
        }
        System.out.println("[BROADCAST] Messaggio inviato a " + count + " client");
    }
}
```

**C) Gestione Comandi nel ClientHandler:**
```java
if (messaggio.startsWith("/")) {
    // Comando
    if (messaggio.equals("/quit")) {
        break; // Esci dal loop
    } else if (messaggio.equals("/list")) {
        sendUserList();
    } else {
        sendMessage("[SERVER]: Comando non riconosciuto");
    }
} else {
    // Messaggio normale - broadcast a tutti
    System.out.println("[" + username + "] " + messaggio);
    String formatted = "[" + username + "]: " + messaggio;
    sendMessage("[TU]: " + messaggio); // Conferma al mittente
    ChatServer.broadcast(formatted, this);
}
```

**D) Metodo Lista Utenti:**
```java
private void sendUserList() {
    StringBuilder lista = new StringBuilder("[SERVER]: Utenti online: ");
    synchronized (ChatServer.clients) {
        for (int i = 0; i < ChatServer.clients.size(); i++) {
            lista.append(ChatServer.clients.get(i).getUsername());
            if (i < ChatServer.clients.size() - 1) {
                lista.append(", ");
            }
        }
    }
    sendMessage(lista.toString());
}
```

**E) Disconnessione Pulita:**
```java
private void disconnect() {
    try {
        ChatServer.removeClient(this);
        socket.close();
        System.out.println("[DISCONNESSIONE] " + username + " disconnesso");
        System.out.println("Client attivi: " + ChatServer.clients.size());
        
        ChatServer.broadcast("[SERVER]: " + username + 
            " si è disconnesso. Client online: " + ChatServer.clients.size(), 
            this);
    } catch (IOException e) {
        System.err.println("Errore chiusura socket: " + e.getMessage());
    }
}
```

#### 2.4 Checklist Server:
- [ ] ServerSocket sulla porta 9999
- [ ] Lista sincronizzata di ClientHandler
- [ ] Accept loop che crea thread per ogni client
- [ ] Username automatici (Client1, Client2, ecc.)
- [ ] Metodo broadcast thread-safe
- [ ] Gestione comando `/list`
- [ ] Gestione comando `/quit`
- [ ] Notifiche connessione/disconnessione
- [ ] Statistiche client connessi
- [ ] Gestione errori e chiusura risorse

---

### 💻 **STEP 3: Implementazione Client (45-60 minuti)**

#### 3.1 Struttura Base del Client
Crea il file `ChatClient.java`:

```java
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static final String HOST = "localhost";
    private static final int PORTA = 9999;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    
    public static void main(String[] args) {
        new ChatClient().start();
    }
    
    public void start() {
        try {
            socket = new Socket(HOST, PORTA);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            
            System.out.println("=== CHAT CLIENT ===");
            System.out.println("Connessione al server riuscita!");
            
            // Leggi username dal server
            String welcome = in.readLine();
            System.out.println(welcome);
            extractUsername(welcome);
            
            // Avvia thread di ricezione
            new Thread(new MessageReceiver()).start();
            
            // Thread principale gestisce invio
            handleUserInput();
            
        } catch (IOException e) {
            System.err.println("Errore connessione: " + e.getMessage());
        }
    }
    
    private void extractUsername(String welcome) {
        // TODO: Estrai username da messaggio benvenuto
    }
    
    private void handleUserInput() {
        // TODO: Loop input utente e invio messaggi
    }
    
    // Inner class per thread ricezione
    private class MessageReceiver implements Runnable {
        @Override
        public void run() {
            // TODO: Ricevi e visualizza messaggi
        }
    }
}
```

#### 3.2 Componenti da Implementare nel Client:

**A) Thread di Ricezione:**
```java
private class MessageReceiver implements Runnable {
    @Override
    public void run() {
        try {
            String messaggio;
            while ((messaggio = in.readLine()) != null) {
                System.out.println(messaggio);
            }
        } catch (IOException e) {
            System.err.println("Connessione chiusa dal server");
        }
    }
}
```

**B) Gestione Input Utente:**
```java
private void handleUserInput() {
    Scanner scanner = new Scanner(System.in);
    
    System.out.println("\nComandi disponibili:");
    System.out.println("  /list  - Visualizza utenti online");
    System.out.println("  /quit  - Disconnetti dalla chat");
    System.out.println("\nScrivi un messaggio e premi INVIO per inviarlo\n");
    
    while (true) {
        System.out.print("> ");
        String messaggio = scanner.nextLine();
        
        if (messaggio.equals("/quit")) {
            out.println("/quit");
            break;
        }
        
        out.println(messaggio);
    }
    
    try {
        socket.close();
        System.out.println("Disconnessione completata.");
    } catch (IOException e) {
        System.err.println("Errore chiusura: " + e.getMessage());
    }
}
```

**C) Estrazione Username:**
```java
private void extractUsername(String welcome) {
    // Esempio: "[SERVER]: Benvenuto Client1! ..."
    if (welcome.contains("Benvenuto")) {
        String[] parts = welcome.split(" ");
        for (String part : parts) {
            if (part.startsWith("Client")) {
                username = part.replace("!", "");
                System.out.println("Sei stato registrato come: " + username + "\n");
                break;
            }
        }
    }
}
```

#### 3.3 Checklist Client:
- [ ] Connessione a localhost:9999
- [ ] Ricezione e visualizzazione username assegnato
- [ ] Thread separato per ricezione messaggi
- [ ] Loop input utente per invio messaggi
- [ ] Gestione comando `/list`
- [ ] Gestione comando `/quit`
- [ ] Visualizzazione messaggi con formato chiaro
- [ ] Gestione disconnessione pulita
- [ ] Gestione errori di connessione

---

### 🧪 **STEP 4: Testing e Debug (30-45 minuti)**

#### 4.1 Test di Compilazione:
```bash
# Compila tutti i file
javac ChatServer.java ClientHandler.java ChatClient.java

# Verifica assenza errori
```

#### 4.2 Test Funzionale Multi-Client:
```bash
# Terminale 1: Avvia il server
java ChatServer

# Terminale 2: Avvia primo client
java ChatClient

# Terminale 3: Avvia secondo client
java ChatClient

# Terminale 4: Avvia terzo client
java ChatClient
```

#### 4.3 Scenari di Test da Verificare:

| Scenario | Azione | Verifica |
|----------|--------|----------|
| **Connessione multipla** | Apri 3 client | ✅ Server accetta tutti |
| **Username automatici** | Controlla username | ✅ Client1, Client2, Client3 |
| **Broadcast** | Client1 invia messaggio | ✅ Client2 e Client3 lo ricevono |
| **Notifiche connessione** | Nuovo client si connette | ✅ Tutti ricevono notifica |
| **Comando /list** | Client1 digita `/list` | ✅ Vede tutti gli utenti online |
| **Disconnessione** | Client2 digita `/quit` | ✅ Altri ricevono notifica |
| **Resilienza** | Chiudi forzatamente un client | ✅ Server non crasha |
| **Messaggi multipli** | Invii rapidi da più client | ✅ Nessun messaggio perso |

#### 4.4 Test di Carico (Opzionale):
```bash
# Script per avviare 10 client contemporaneamente
for i in {1..10}; do
    java ChatClient &
done
```

#### 4.5 Debug Checklist:
- [ ] Server accetta connessioni multiple
- [ ] Ogni client ha thread dedicato
- [ ] Broadcast funziona correttamente
- [ ] Nessun deadlock o race condition
- [ ] Disconnessioni gestite correttamente
- [ ] Nessuna eccezione non catturata
- [ ] Messaggi non si sovrappongono
- [ ] Comandi `/list` e `/quit` funzionano

---

## 📝 **DELIVERABLE RICHIESTI**

### 📁 File da Consegnare:
1. **`ChatServer.java`** - Server multithreading completo
2. **`ClientHandler.java`** - Thread handler per singolo client
3. **`ChatClient.java`** - Client con thread RX/TX
4. **`RELAZIONE.md`** - Relazione tecnica dettagliata
5. **Screenshot/Video** - Demo con almeno 3 client

### 📖 Template Relazione (`RELAZIONE.md`):

```markdown
# Relazione: Chat Server Multithreading TCP

## Dati Studente
- **Nome:** [Il tuo nome]
- **Cognome:** [Il tuo cognome]  
- **Classe:** [La tua classe]
- **Data:** [Data di consegna]

## Descrizione dell'Applicazione
[Descrivi il sistema di chat implementato]

## Architettura Implementata

### Server:
- **Porta di ascolto:** 9999
- **Gestione client:** Thread dedicato per ogni client (ClientHandler)
- **Strutture dati condivise:** 
  - Lista sincronizzata di ClientHandler
  - Contatore atomico per username
- **Sincronizzazione:** synchronized su lista client per add/remove/broadcast

### Client:
- **Thread di ricezione:** Ascolta continuamente messaggi dal server
- **Thread di invio:** Gestisce input utente e invio al server
- **Comandi supportati:** /list, /quit

### Protocollo di Comunicazione
**Messaggi Client → Server:**
- Messaggio testuale normale
- `/list` - richiesta lista utenti
- `/quit` - disconnessione

**Messaggi Server → Client:**
- `[Username]: testo` - messaggio da altro utente
- `[SERVER]: testo` - notifica/info server
- `[TU]: testo` - conferma messaggio inviato

## Gestione del Multithreading

### Thread nel Server:
1. **Main Thread:** Accept loop per nuovi client
2. **ClientHandler Thread:** Uno per ogni client connesso

### Sincronizzazione Implementata:
```java
// Esempio di sincronizzazione sulla lista client
synchronized (clients) {
    clients.add(newHandler);
}
```

### Risorse Condivise:
- Lista `clients` (accesso sincronizzato)
- Contatore `clientCounter` (incremento atomico)

## Problemi Riscontrati e Soluzioni

### Problema 1: Race Condition sulla Lista Client
**Descrizione:** [...]
**Soluzione:** Uso di Collections.synchronizedList() e synchronized block

### Problema 2: Deadlock Potenziale
**Descrizione:** [...]
**Soluzione:** [...]

## Testing Effettuato

### Test di Funzionalità:
- ✅ Connessione simultanea di 5 client
- ✅ Broadcast messaggi corretto
- ✅ Comando `/list` funzionante
- ✅ Disconnessione pulita

### Test di Carico:
- ✅ 10 client contemporanei senza errori
- ✅ Invio rapido di 100 messaggi

### Screenshot
[Inserisci screenshot con server e 3+ client attivi]

## Estensioni Implementate (Opzionali)
- [ ] ThreadPoolExecutor con max 10 thread
- [ ] Timeout su socket
- [ ] Logging su file
- [ ] GUI per client (Swing/JavaFX)

## Conclusioni
[Cosa hai imparato sul multithreading e sincronizzazione]
[Possibili miglioramenti futuri]
```

---

## 🏆 **CRITERI DI VALUTAZIONE**

### 📊 Griglia di Valutazione (Totale: 100 punti)

| Criterio | Punti | Descrizione |
|----------|-------|-------------|
| **Server Multithreading** | 20 | Thread dedicato per ogni client, gestione corretta |
| **Sincronizzazione** | 20 | Liste thread-safe, synchronized corretti, no race condition |
| **Broadcast Messaggi** | 15 | Messaggi inviati a tutti i client correttamente |
| **Gestione Comandi** | 10 | `/list` e `/quit` implementati |
| **Client Multi-Thread** | 15 | Thread RX/TX separati e funzionanti |
| **Notifiche** | 10 | Connessione/disconnessione notificate |
| **Gestione Errori** | 5 | Disconnessioni improvvise gestite |
| **Documentazione** | 5 | Relazione completa e chiara |

### 🎯 Livelli di Competenza:

- **90-100 punti:** ⭐⭐⭐⭐⭐ **Eccellente** - Multithreading robusto e sincronizzazione perfetta
- **80-89 punti:** ⭐⭐⭐⭐ **Buono** - Funziona con più client, piccoli difetti di sincronizzazione
- **70-79 punti:** ⭐⭐⭐ **Sufficiente** - Funzionalità base con alcuni problemi di concorrenza
- **60-69 punti:** ⭐⭐ **Insufficiente** - Thread implementati ma con race condition
- **< 60 punti:** ⭐ **Gravemente insufficiente** - Non gestisce correttamente client multipli

---

## 💡 **SUGGERIMENTI E TRUCCHI**

### 🔧 **Best Practices Multithreading:**
- **Sincronizza accesso a risorse condivise:** Usa `synchronized` su liste/contatori
- **Usa Collections thread-safe:** `Collections.synchronizedList()` è tuo amico
- **Try-with-resources per socket:** Garantisce chiusura automatica
- **Gestisci eccezioni nei thread:** Ogni thread deve avere try-catch
- **Evita nested synchronized:** Può causare deadlock

### 🚨 **Errori Comuni da Evitare:**
- ❌ **Accesso non sincronizzato** a `clients.add()`/`remove()`
- ❌ **Dimenticare close()** su socket nei thread
- ❌ **Broadcast al mittente stesso** (usa controllo `if (client != mittente)`)
- ❌ **Usare System.in nel thread** (blocca il thread)
- ❌ **Non gestire IOException** in ClientHandler

### 🐛 **Debug Tips:**
```java
// Aggiungi logging dettagliato
System.out.println("[DEBUG] Thread " + Thread.currentThread().getName() + 
                   " - Client connessi: " + clients.size());

// Usa thread name personalizzato
Thread thread = new Thread(handler);
thread.setName("ClientHandler-" + username);
thread.start();

// Controlla stato thread
System.out.println("Thread attivi: " + Thread.activeCount());
```

### 🎯 **Estensioni Opzionali (Punti Bonus +10 ciascuna):**
- 🔄 **ThreadPoolExecutor** invece di `new Thread()` per ogni client
- ⏱️ **Timeout su socket** con `socket.setSoTimeout(30000)`
- 💾 **Logging su file** di tutti i messaggi inviati
- 🎨 **GUI Swing/JavaFX** per client con area chat e lista utenti
- 🔐 **Autenticazione** con username/password
- 💬 **Messaggi privati** con comando `/msg username messaggio`
- 📊 **Dashboard server** che mostra statistiche in tempo reale
- 🔔 **Notifiche audio** per nuovi messaggi (Java Sound API)

---

## 📚 **RISORSE UTILI**

### 📖 **Documentazione Oracle:**
- [Concurrency Tutorial](https://docs.oracle.com/javase/tutorial/essential/concurrency/)
- [Thread Class](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.html)
- [Collections.synchronizedList](https://docs.oracle.com/javase/8/docs/api/java/util/Collections.html#synchronizedList-java.util.List-)
- [Socket Programming](https://docs.oracle.com/javase/tutorial/networking/sockets/)

### 🔍 **Comandi Debug Utili:**
```bash
# Verifica porta in uso e connessioni attive
netstat -an | grep 9999

# Conta thread Java attivi
jps -l
jstack <PID>

# Monitora connessioni TCP
watch -n 1 'netstat -an | grep 9999 | wc -l'

# Simula client multipli con script
for i in {1..5}; do 
    gnome-terminal -- java ChatClient
done
```

### 💬 **FAQ Comuni:**

**Q: Come evito race condition sulla lista client?**  
A: Usa `Collections.synchronizedList()` E synchronized block durante iterazione.

**Q: Il server crasha quando un client si disconnette improvvisamente**  
A: Gestisci `IOException` nel thread ClientHandler e rimuovi client dalla lista.

**Q: I messaggi si sovrappongono nell'output**  
A: È normale con multi-threading. Per output pulito usa librerie di logging.

**Q: Come testo con molti client senza aprire 10 terminali?**  
A: Crea uno script bash che apre client in background o usa `screen`/`tmux`.

**Q: Differenza tra synchronized method e synchronized block?**  
A: Block sincronizza solo sezione critica (più efficiente), method sincronizza tutto.

---

## 🎓 **CONCETTI CHIAVE DA PADRONEGGIARE**

### 1️⃣ **Thread per Client:**
Ogni client ha un thread dedicato (ClientHandler) che gestisce I/O in modo indipendente.

### 2️⃣ **Sincronizzazione:**
Accesso a risorse condivise (lista client) deve essere sincronizzato per evitare race condition.

### 3️⃣ **Broadcast:**
Iterare lista client in modo thread-safe e inviare messaggio a tutti tranne il mittente.

### 4️⃣ **Graceful Shutdown:**
Disconnessioni devono rimuovere client dalla lista e chiudere socket correttamente.

### 5️⃣ **Exception Handling:**
Ogni thread deve gestire le proprie IOException per non bloccare server.

---

## ⏰ **SCADENZE E MODALITÀ DI CONSEGNA**

### 📅 **Timeline:**
- **Assegnazione:** [Data di oggi]
- **Consegna:** [Data + 2 settimane]
- **Presentazione:** [Data + 3 settimane]

### 📤 **Modalità Consegna:**
1. **Repository Git:** URL repository con codice commentato
2. **Relazione:** File Markdown o PDF
3. **Demo Video:** 3-5 minuti che mostra:
   - Avvio server
   - Connessione di 3+ client
   - Invio messaggi e broadcast
   - Uso comandi `/list` e `/quit`
   - Gestione disconnessione

### 📧 **Contatti:**
- **Email docente:** [email@scuola.it]
- **Orario ricevimento:** [Giorni e orari]
- **Discord/Teams:** [Link canale supporto]

---

## 🎉 **CONCLUSIONI**

Questa esercitazione rappresenta un passo fondamentale nella comprensione del **multithreading applicato alle reti**. Implementare un chat server ti permetterà di:

- 🧠 **Comprendere** la concorrenza reale nelle applicazioni di rete
- 🔒 **Padroneggiare** sincronizzazione e gestione risorse condivise
- 🏗️ **Costruire** sistemi scalabili che gestiscono client multipli
- 🐛 **Imparare** debugging di problemi di concorrenza

**Ricorda:** Il multithreading è potente ma richiede attenzione! Race condition e deadlock sono errori subdoli. **Testa sempre con carichi reali** e usa logging per debug.

**Non mollare se incontri difficoltà!** La programmazione concorrente è tra le più complesse, ma anche tra le più gratificanti. Ogni bug risolto è una competenza acquisita.

---

### 🚀 **Challenge Extra (Per i più coraggiosi):**

Riesci a implementare **tutte** queste feature avanzate?

1. ✨ **Colori ANSI** per username diversi nel terminale
2. 🎯 **Messaggi privati** `/msg username testo`
3. 📝 **Cronologia chat** salvata su file log
4. 👑 **Admin commands** `/kick username`, `/broadcast testo`
5. 🔐 **Login con password** prima di accedere alla chat
6. 🌐 **Stanze multiple** `/join nomeStanza`

Se riesci a implementarne 3+, meriti un **bonus eccezionale!** 🏆

---

**Buon lavoro e buon coding! 💻🔥**

---

*Esercitazione creata per il corso di Sistemi e Reti 3 - ITCS Cannizzaro di Rho*  
*Anno Scolastico 2025/26*  
*Versione 1.0 - Novembre 2025*
