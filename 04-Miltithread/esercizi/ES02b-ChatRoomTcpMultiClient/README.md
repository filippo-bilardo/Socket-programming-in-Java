# 🎓 ESERCITAZIONE: Chat Room TCP Multi-Client

> *Applicazione di messaggistica in tempo reale - Sistemi e Reti 3*

---

## 📋 **INFORMAZIONI GENERALI**

**Materia:** Sistemi e Reti  
**Argomento:** Socket TCP Multi-Threading in Java  
**Tempo stimato:** 3-4 ore  
**Difficoltà:** ⭐⭐⭐⭐ (Avanzata)  
**Modalità:** Individuale o a coppie  

---

## 🎯 **OBIETTIVI DIDATTICI**

Al termine di questa esercitazione lo studente sarà in grado di:

- ✅ **Implementare** un server TCP multi-client con threading
- ✅ **Gestire** connessioni simultanee di più client
- ✅ **Applicare** pattern di comunicazione broadcast
- ✅ **Sincronizzare** accesso a risorse condivise (thread safety)
- ✅ **Implementare** protocolli di autenticazione base
- ✅ **Gestire** lista utenti connessi dinamicamente
- ✅ **Utilizzare** Collections thread-safe in Java
- ✅ **Debugging** applicazioni multi-threaded

---

## 📚 **PREREQUISITI**

### Conoscenze Teoriche Richieste:
- 🧵 **Multi-threading Java:** `Thread`, `Runnable`, sincronizzazione
- 🔌 **Socket TCP avanzati:** Server multi-client, gestione connessioni
- 📦 **Collections Java:** `ConcurrentHashMap`, `CopyOnWriteArrayList`
- 🔒 **Sincronizzazione:** `synchronized`, `volatile`, thread safety
- 🎛️ **Design Patterns:** Observer, Command, Singleton

### Strumenti Necessari:
- ☕ **Java JDK** 11 o superiore
- 💻 **IDE** con supporto debugging multi-thread
- 🖥️ **Più terminali** per testare client multipli

---

## 📖 **TRACCIA DELL'ESERCIZIO**

### 🚀 **Applicazione: "ChatRoom Multi-Utente"**

Realizzare un sistema di chat in tempo reale dove:

1. **Il SERVER:**
   - Ascolta sulla porta `9999`
   - Gestisce **connessioni multiple** simultaneamente (max 10 utenti)
   - Implementa **autenticazione** con username unico
   - **Broadcast messaggi** a tutti gli utenti connessi
   - Mantiene **lista utenti online** in tempo reale
   - Supporta **comandi speciali**: `/list`, `/quit`, `/whisper`
   - **Log completo** di tutte le attività
   - **Gestione disconnessioni** improvvise

2. **Il CLIENT:**
   - Si connette al server con **username personalizzato**
   - **Riceve messaggi** in tempo reale da altri utenti
   - **Invia messaggi** al server per il broadcast
   - **Interfaccia dual-thread**: input utente + ricezione messaggi
   - Supporta **comandi speciali** per interazioni avanzate
   - **Notifiche** di ingresso/uscita utenti

### 📐 **Protocollo di Comunicazione:**

```
=== FASE AUTENTICAZIONE ===
Client → Server: "LOGIN:username"
Server → Client: "LOGIN_OK" | "LOGIN_ERROR:Username già in uso"

=== MESSAGGISTICA ===
Client → Server: "MESSAGE:testo del messaggio"
Server → All:    "BROADCAST:username:testo del messaggio"

=== COMANDI SPECIALI ===
Client → Server: "COMMAND:LIST"
Server → Client: "USERLIST:user1,user2,user3"

Client → Server: "COMMAND:WHISPER:target_user:messaggio privato"
Server → Target: "PRIVATE:sender:messaggio privato"

Client → Server: "COMMAND:QUIT"
Server → All:    "USER_LEFT:username"

=== NOTIFICHE SISTEMA ===
Server → All: "USER_JOINED:nuovo_username"
Server → All: "USER_LEFT:username_uscito"
Server → All: "SERVER_INFO:messaggio del sistema"
```

---

## 🛠️ **PASSAGGI DA SEGUIRE**

### 📝 **STEP 1: Analisi e Progettazione (20-30 minuti)**

#### 1.1 Architettura Multi-Thread
```
                    SERVER CENTRALE
                   ┌─────────────────┐
                   │  ServerSocket   │
                   │   (porta 9999)  │
                   └─────────┬───────┘
                            │
               ┌─────────────┼─────────────┐
               │             │             │
         ┌─────▼────┐ ┌─────▼────┐ ┌─────▼────┐
         │ Thread   │ │ Thread   │ │ Thread   │
         │ Client 1 │ │ Client 2 │ │ Client N │
         └─────┬────┘ └─────┬────┘ └─────┬────┘
               │             │             │
         ┌─────▼────┐ ┌─────▼────┐ ┌─────▼────┐
         │ Client 1 │ │ Client 2 │ │ Client N │
         │ Socket   │ │ Socket   │ │ Socket   │
         └──────────┘ └──────────┘ └──────────┘
```

#### 1.2 Strutture Dati Condivise
```java
// Lista thread-safe degli utenti connessi
private static ConcurrentHashMap<String, ClientHandler> connectedUsers;

// Classe per gestire ogni client
class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String username;
    private PrintWriter out;
    private BufferedReader in;
}
```

---

### ⚙️ **STEP 2: Server Multi-Thread (60-90 minuti)**

#### 2.1 Struttura Base Server
Crea il file `ChatServer.java`:

```java
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatServer {
    private static final int PORTA = 9999;
    private static final int MAX_CLIENTS = 10;
    
    // Mappa thread-safe: username → ClientHandler
    private static ConcurrentHashMap<String, ClientHandler> connectedUsers = 
        new ConcurrentHashMap<>();
    
    public static void main(String[] args) {
        // TODO: Implementa server multi-thread
    }
}
```

#### 2.2 Componenti Server da Implementare:

**A) Main Loop Server:**
```java
try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
    System.out.println("=== CHAT SERVER AVVIATO ===");
    System.out.println("Porta: " + PORTA + " | Max utenti: " + MAX_CLIENTS);
    
    while (true) {
        Socket clientSocket = serverSocket.accept();
        
        if (connectedUsers.size() >= MAX_CLIENTS) {
            // Rifiuta connessione
        } else {
            // Crea nuovo thread per il client
            ClientHandler handler = new ClientHandler(clientSocket);
            new Thread(handler).start();
        }
    }
}
```

**B) Classe ClientHandler:**
```java
class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String username;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected = true;
    
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        // Inizializza stream I/O
    }
    
    @Override
    public void run() {
        try {
            // 1. Fase autenticazione
            // 2. Loop ricezione messaggi
            // 3. Cleanup disconnessione
        } catch (IOException e) {
            // Gestione errori
        }
    }
}
```

**C) Sistema Broadcast:**
```java
public static void broadcastMessage(String message, String excludeUser) {
    for (ClientHandler client : connectedUsers.values()) {
        if (!client.getUsername().equals(excludeUser)) {
            client.sendMessage(message);
        }
    }
}
```

**D) Gestione Comandi:**
```java
private void handleCommand(String command) {
    String[] parts = command.split(":", 3);
    
    switch (parts[1]) {
        case "LIST":
            // Invia lista utenti
            break;
        case "WHISPER":
            // Messaggio privato
            break;
        case "QUIT":
            // Disconnetti utente
            break;
    }
}
```

#### 2.3 Checklist Server:
- [ ] ServerSocket multi-thread funzionante
- [ ] Gestione max 10 client simultanei
- [ ] Autenticazione username univoci
- [ ] Sistema broadcast thread-safe
- [ ] Comandi `/list`, `/quit`, `/whisper`
- [ ] Notifiche ingresso/uscita utenti
- [ ] Logging completo con timestamp
- [ ] Gestione disconnessioni improvvise
- [ ] Cleanup risorse corretto

---

### 💻 **STEP 3: Client Multi-Thread (45-60 minuti)**

#### 3.1 Struttura Base Client
Crea il file `ChatClient.java`:

```java
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static final String HOST = "localhost";
    private static final int PORTA = 9999;
    private static boolean isConnected = true;
    
    public static void main(String[] args) {
        // TODO: Implementa client con thread separati
    }
}
```

#### 3.2 Architettura Client Dual-Thread:
```
CLIENT APPLICATION
├── Main Thread (Input Utente)
│   ├── Legge input da tastiera
│   ├── Processa comandi locali
│   └── Invia messaggi al server
└── Receiver Thread (Messaggi Server)
    ├── Ascolta messaggi dal server
    ├── Processa notifiche sistema
    └── Visualizza messaggi ricevuti
```

#### 3.3 Componenti Client da Implementare:

**A) Thread Principale (Input):**
```java
// Thread principale gestisce input utente
Scanner scanner = new Scanner(System.in);
PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

while (isConnected) {
    String input = scanner.nextLine();
    
    if (input.startsWith("/")) {
        // Gestione comandi locali
        handleLocalCommand(input, out);
    } else {
        // Invia messaggio normale
        out.println("MESSAGE:" + input);
    }
}
```

**B) Thread Receiver (Ricezione):**
```java
class MessageReceiver implements Runnable {
    private BufferedReader in;
    
    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null && isConnected) {
                processServerMessage(message);
            }
        } catch (IOException e) {
            System.out.println("Disconnesso dal server");
        }
    }
}
```

**C) Processamento Messaggi Server:**
```java
private static void processServerMessage(String message) {
    String[] parts = message.split(":", 3);
    
    switch (parts[0]) {
        case "BROADCAST":
            System.out.println("[" + parts[1] + "]: " + parts[2]);
            break;
        case "PRIVATE":
            System.out.println("(Privato da " + parts[1] + "): " + parts[2]);
            break;
        case "USER_JOINED":
            System.out.println("*** " + parts[1] + " è entrato nella chat ***");
            break;
        // Altri tipi di messaggio...
    }
}
```

#### 3.4 Checklist Client:
- [ ] Connessione al server con autenticazione
- [ ] Thread separato per ricezione messaggi
- [ ] Input utente non bloccante
- [ ] Supporto comandi `/list`, `/quit`, `/whisper`
- [ ] Visualizzazione messaggi formattata
- [ ] Gestione notifiche sistema
- [ ] Disconnessione graceful
- [ ] Gestione errori di rete

---

### 🧪 **STEP 4: Testing Multi-Client (30-45 minuti)**

#### 4.1 Scenario di Test:
```bash
# Terminale 1: Server
java ChatServer

# Terminale 2: Client Alice
java ChatClient
# Login: Alice

# Terminale 3: Client Bob  
java ChatClient
# Login: Bob

# Terminale 4: Client Charlie
java ChatClient  
# Login: Charlie
```

#### 4.2 Casi di Test da Verificare:

| Azione | Client | Risultato Atteso | Verifica |
|--------|--------|------------------|----------|
| Login Alice | Alice | "Benvenuto Alice!" | ✅ |
| Login Bob | Bob | "Bob è entrato" a Alice | ✅ |
| Username duplicato | Charlie→Alice | "Username già in uso" | ✅ |
| Messaggio Alice | Alice | Broadcast a Bob | ✅ |
| `/list` | Bob | Lista: Alice, Bob | ✅ |
| `/whisper Alice ciao` | Bob | Privato solo ad Alice | ✅ |
| `/quit` | Alice | "Alice è uscito" a Bob | ✅ |
| Disconnessione improvvisa | Bob | Server rileva disconnessione | ✅ |

#### 4.3 Test di Stress:
```bash
# Test connessioni simultanee (10 client)
for i in {1..10}; do
    java ChatClient &
done

# Test messaggio intensivo  
# Ogni client invia 50 messaggi rapidamente

# Test disconnessioni casuali
# Chiudi client a caso e verifica cleanup
```

---

## 📝 **DELIVERABLE RICHIESTI**

### 📁 File da Consegnare:
1. **`ChatServer.java`** - Server multi-thread completo
2. **`ChatClient.java`** - Client dual-thread  
3. **`RELAZIONE_CHAT.md`** - Relazione tecnica dettagliata
4. **`TEST_RESULTS.md`** - Report dei test effettuati
5. **Video Demo** - Dimostrazione con 3+ client

### 📖 Template Relazione (`RELAZIONE_CHAT.md`):

```markdown
# Relazione: ChatRoom Multi-Client TCP

## Dati Studente
- **Nome:** [Nome]
- **Cognome:** [Cognome]  
- **Classe:** [Classe]
- **Data:** [Data]

## Architettura Implementata

### Server Multi-Thread
- **Porta:** 9999
- **Max client simultanei:** 10
- **Pattern threading:** Thread per client
- **Sincronizzazione:** ConcurrentHashMap per utenti

### Client Dual-Thread  
- **Thread principale:** Input utente
- **Thread receiver:** Messaggi server
- **Sincronizzazione:** volatile boolean per stato

## Protocollo di Comunicazione
[Descrivi formato messaggi con esempi]

## Gestione Thread Safety
[Spiega come hai gestito la concorrenza]

## Comandi Implementati
- `/list` - Lista utenti online
- `/quit` - Disconnessione
- `/whisper user msg` - Messaggio privato

## Testing Multi-Client
[Risultati test con 3+ client simultanei]

## Problemi e Soluzioni
[Difficoltà nel debugging multi-thread, ecc.]

## Performance e Limiti
[Analisi prestazioni e possibili miglioramenti]

## Conclusioni
[Cosa hai imparato sui thread e networking]
```

---

## 🏆 **CRITERI DI VALUTAZIONE**

### 📊 Griglia di Valutazione (Totale: 100 punti)

| Criterio | Punti | Descrizione |
|----------|-------|-------------|
| **Server Multi-Thread** | 25 | Threading corretto, gestione client simultanei |
| **Client Dual-Thread** | 20 | Input non-bloccante, ricezione asincrona |
| **Thread Safety** | 20 | Sincronizzazione corretta, no race conditions |
| **Protocollo Comunicazione** | 15 | Formato messaggi, comandi speciali |
| **Testing Multi-Client** | 10 | Verifica con 3+ client, test di stress |
| **Gestione Errori** | 5 | Disconnessioni, errori di rete |
| **Documentazione** | 5 | Relazione completa, commenti codice |

### 🎯 Livelli Competenza Thread Safety:

- **90-100:** ⭐⭐⭐⭐⭐ **Master** - Sincronizzazione perfetta, no bugs
- **80-89:** ⭐⭐⭐⭐ **Avanzato** - Threading corretto, piccole imperfezioni  
- **70-79:** ⭐⭐⭐ **Intermedio** - Funziona ma con possibili race conditions
- **60-69:** ⭐⭐ **Base** - Threading implementato ma instabile
- **< 60:** ⭐ **Insufficiente** - Problemi gravi di concorrenza

---

## 💡 **SUGGERIMENTI AVANZATI**

### 🧵 **Thread Safety Best Practices:**
```java
// ✅ CORRETTO - Usa Collections thread-safe
ConcurrentHashMap<String, ClientHandler> users = new ConcurrentHashMap<>();

// ✅ CORRETTO - Sincronizzazione metodi critici
public synchronized void addUser(String username, ClientHandler handler) {
    users.put(username, handler);
}

// ❌ SBAGLIATO - HashMap normale in ambiente multi-thread
HashMap<String, ClientHandler> users = new HashMap<>(); // RACE CONDITIONS!
```

### 🔍 **Debug Multi-Threading:**
```java
// Logging thread-safe con timestamp
private static void log(String message) {
    String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
    System.out.println("[" + timestamp + "][" + 
                      Thread.currentThread().getName() + "] " + message);
}
```

### 🚨 **Errori Comuni Multi-Thread:**
- ❌ **Race Conditions:** Accesso non sincronizzato a dati condivisi
- ❌ **Deadlock:** Thread che si bloccano a vicenda
- ❌ **Memory Leaks:** Thread non terminati correttamente
- ❌ **Broadcast inefficiente:** Iterazione non thread-safe
- ❌ **Client disconnect:** Non rimuovere utente dalla lista

### 🎯 **Estensioni Avanzate (Bonus):**
- 🎨 **GUI Client** con Swing/JavaFX
- 💾 **Persistenza messaggi** su database
- 🔐 **Crittografia** comunicazioni (SSL/TLS)
- 🏠 **Chat rooms** multiple
- 📱 **Client mobile** Android
- ⚡ **WebSocket** per client web
- 📊 **Monitoring** server real-time

---

## 🔧 **TOOLS E DEBUG**

### 💻 **Comandi Utili:**
```bash
# Monitoraggio connessioni
netstat -an | grep 9999

# Thread dump Java (per debugging)
jstack <pid_del_server>

# Stress test automatico
for i in {1..5}; do java ChatClient & done

# Kill tutti i client
pkill -f ChatClient
```

### 🕵️ **Debugging Multi-Thread:**
1. **Usa logging dettagliato** con thread names
2. **Thread.sleep()** per rallentare e osservare race conditions  
3. **Debugger IDE** con breakpoints thread-specific
4. **jstack** per analizzare deadlock
5. **Profiler** per memory leaks

---

## 📚 **RISORSE APPROFONDIMENTO**

### 📖 **Documentazione Java:**
- [Java Concurrency Tutorial](https://docs.oracle.com/javase/tutorial/essential/concurrency/)
- [ConcurrentHashMap](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentHashMap.html)
- [Thread Safety Best Practices](https://docs.oracle.com/javase/tutorial/essential/concurrency/safety.html)

### 🎓 **Approfondimenti:**
- **Patterns:** Producer-Consumer, Observer per messaging
- **Java NIO:** Non-blocking I/O per performance superiori
- **Thread Pools:** ExecutorService per gestione thread ottimale
- **Lock-free:** Algoritmi senza synchronization

---

## ⏰ **TIMELINE SUGGERITA**

### 📅 **Pianificazione 4 ore:**
- **Ora 1:** Analisi, progettazione, setup base server
- **Ora 2:** Implementazione threading server e broadcast  
- **Ora 3:** Client dual-thread e testing base
- **Ora 4:** Testing multi-client, debugging, documentazione

### 🎯 **Milestone Intermedie:**
- ✅ **30 min:** Server accetta 1 client
- ✅ **60 min:** Server gestisce 2+ client  
- ✅ **90 min:** Broadcast funzionante
- ✅ **120 min:** Client non-bloccante
- ✅ **180 min:** Comandi speciali implementati
- ✅ **240 min:** Testing completo e documentazione

---

## 🎉 **CONCLUSIONI**

Questa esercitazione rappresenta un **salto qualitativo** nella programmazione di rete, introducendo concetti fondamentali del **computing distribuito**:

- 🧵 **Multi-threading** e sincronizzazione
- 🔄 **Comunicazione asincrona** 
- 📡 **Pattern publish-subscribe**
- 🛡️ **Thread safety** e data consistency
- 🚀 **Scalabilità** applicazioni di rete

**Preparati per una sfida stimolante!** Il multi-threading richiede precisione e pazienza, ma le competenze acquisite sono **fondamentali** per lo sviluppo di applicazioni moderne.

**Che la concorrenza sia con te! 🚀👨‍💻**

---

*Esercitazione avanzata per Sistemi e Reti 3 - ITES "Alessandro Volta"*  
*Anno Accademico 2024/2025*