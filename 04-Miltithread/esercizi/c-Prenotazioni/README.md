# 🎓 ESERCITAZIONE: Socket TCP "Sistema Prenotazione Posti Aereo"

> *Verifica delle competenze sui socket TCP e gestione dati strutturati - Sistemi e Reti 3*

---

> Protocollo: richieste e risposte UTF-8 terminate da newline. AUTH/PRENOTA/CANCELLA/QUIT restituiscono una riga. VISUALIZZA e LISTA restituiscono un'intestazione (`MAPPA` o `LISTA`), il contenuto (anche righe vuote) e una riga finale `END`. Il client deve consumare END prima di inviare il comando successivo. La connessione resta aperta fino a QUIT/EOF.


## 📋 **INFORMAZIONI GENERALI**

**Materia:** Sistemi e Reti  
**Argomento:** Socket Programming TCP in Java con Gestione Dati Complessi  
**Tempo stimato:** 3-4 ore  
**Difficoltà:** ⭐⭐⭐⭐ (Avanzata)  
**Modalità:** Individuale o a coppie  

---

## 🎯 **OBIETTIVI DIDATTICI**

Al termine di questa esercitazione lo studente sarà in grado di:

- ✅ **Implementare** un server TCP che gestisce una matrice di dati condivisa
- ✅ **Trasmettere** strutture dati complesse attraverso socket
- ✅ **Gestire** prenotazioni con stato persistente
- ✅ **Implementare** un sistema di autenticazione client-server
- ✅ **Sincronizzare** accesso concorrente a risorse condivise
- ✅ **Visualizzare** matrici formattate lato client
- ✅ **Gestire** operazioni CRUD (Create, Read, Update, Delete) via rete
- ✅ **Documentare** con screenshot professionali

---

## 📚 **PREREQUISITI**

### Conoscenze Teoriche Richieste:
- 🔌 **Socket TCP/IP:** Client-server, comunicazione bidirezionale
- 🔒 **Sincronizzazione:** `synchronized`, gestione concorrenza
- 📊 **Strutture dati:** Array bidimensionali (matrici)
- 🔐 **Autenticazione:** Gestione credenziali utente
- ☕ **Java I/O:** BufferedReader, PrintWriter, serializzazione dati
- 📝 **String manipulation:** Parsing, tokenization, formattazione

### Strumenti Necessari:
- ☕ **Java JDK** 11 o superiore
- 💻 **IDE** o editor di testo (VS Code, IntelliJ, Eclipse)
- 🖥️ **Terminale** per compilazione ed esecuzione multipla
- 📸 **Software screenshot** (Flameshot, Snipping Tool)

---

## 📖 **TRACCIA DELL'ESERCIZIO**

### 🚀 **Applicazione: "Sistema Prenotazione Posti Aereo"**

Simulare un sistema di prenotazione posti aereo dove i client possono:
- Visualizzare la mappa dei posti disponibili
- Prenotare un posto libero
- Cancellare la propria prenotazione
- Vedere chi ha prenotato ciascun posto

1. **Il SERVER (PrenotazioneServer):**
   - Ascolta sulla porta `8888`
   - Mantiene una **matrice dei posti** (es. 6 righe × 4 colonne = 24 posti)
   - Ogni posto può essere: `LIBERO` o `PRENOTATO_DA_username`
   - Gestisce richieste di:
     - **VISUALIZZA**: Invia la mappa posti al client
     - **PRENOTA fila posto**: Prenota un posto specifico
     - **CANCELLA fila posto**: Cancella una prenotazione
     - **LISTA**: Mostra tutte le prenotazioni dell'utente
   - Implementa **autenticazione** (username/password) [OPZIONALE]
   - Supporta **connessioni multiple** simultanee
   - **Sincronizzazione** per evitare doppie prenotazioni
   - Salva stato su file per persistenza [OPZIONALE]

2. **Il CLIENT (PrenotazioneClient):**
   - Si connette al server localhost:8888
   - **Autenticazione** con username/password [OPZIONALE]
   - Presenta menu interattivo:
     ```
     1. Visualizza mappa posti
     2. Prenota posto
     3. Cancella prenotazione
     4. Le mie prenotazioni
     5. Esci
     ```
   - **Visualizza mappa** con formato grafico:
     ```
     === MAPPA POSTI AEREO ===
          A    B    C    D
     1  [ ]  [ ]  [X]  [ ]
     2  [X]  [ ]  [ ]  [X]
     3  [ ]  [ ]  [ ]  [ ]
     4  [X]  [ ]  [X]  [ ]
     5  [ ]  [ ]  [ ]  [ ]
     6  [ ]  [X]  [ ]  [ ]
     
     [ ] = Libero
     [X] = Prenotato
     ```
   - Gestisce **prenotazione** con selezione fila e posto
   - Visualizza **conferme e errori** in modo chiaro

### 📐 **Esempi di Interazione:**

```
=== SERVER ===
Server Prenotazioni avviato sulla porta 8888
Aereo configurato: 6 file × 4 posti (24 posti totali)
In attesa di connessioni...

[CONNESSIONE] Client da 127.0.0.1
[AUTH] Tentativo login: mario
[AUTH] Login riuscito: mario
[VISUALIZZA] Mappa inviata a mario
[PRENOTA] mario -> Fila 1, Posto A - SUCCESSO
[VISUALIZZA] Mappa inviata a mario
[DISCONNESSIONE] mario disconnesso

[CONNESSIONE] Client da 127.0.0.1
[AUTH] Tentativo login: luigi
[AUTH] Login riuscito: luigi
[PRENOTA] luigi -> Fila 1, Posto B - SUCCESSO
[PRENOTA] luigi -> Fila 1, Posto A - ERRORE: Già prenotato
[DISCONNESSIONE] luigi disconnesso

Statistiche:
- Posti totali: 24
- Posti prenotati: 2
- Posti liberi: 22

=== CLIENT (mario) ===
=== SISTEMA PRENOTAZIONE AEREO ===
Connessione al server riuscita!

--- LOGIN ---
Username: mario
Password: ****
Login effettuato con successo!

--- MENU PRINCIPALE ---
1. Visualizza mappa posti
2. Prenota posto
3. Cancella prenotazione
4. Le mie prenotazioni
5. Esci

Scelta > 1

=== MAPPA POSTI AEREO ===
     A    B    C    D
1  [ ]  [ ]  [ ]  [ ]
2  [ ]  [ ]  [ ]  [ ]
3  [ ]  [ ]  [ ]  [ ]
4  [ ]  [ ]  [ ]  [ ]
5  [ ]  [ ]  [ ]  [ ]
6  [ ]  [ ]  [ ]  [ ]

Posti liberi: 24/24

Scelta > 2

--- PRENOTA POSTO ---
Fila (1-6): 1
Posto (A-D): A

✓ Prenotazione confermata!
  Posto: 1A
  Intestato a: mario

Scelta > 1

=== MAPPA POSTI AEREO ===
     A    B    C    D
1  [X]  [ ]  [ ]  [ ]
2  [ ]  [ ]  [ ]  [ ]
3  [ ]  [ ]  [ ]  [ ]
4  [ ]  [ ]  [ ]  [ ]
5  [ ]  [ ]  [ ]  [ ]
6  [ ]  [ ]  [ ]  [ ]

Posti liberi: 23/24
[X] = Prenotato da te

Scelta > 5

Disconnessione in corso...
Grazie per aver utilizzato il servizio!

=== CLIENT (luigi) ===
Username: luigi
Password: ****
Login effettuato con successo!

Scelta > 2

--- PRENOTA POSTO ---
Fila (1-6): 1
Posto (A-D): A

✗ Errore: Posto già prenotato da mario

Scelta > 2

--- PRENOTA POSTO ---
Fila (1-6): 1
Posto (A-D): B

✓ Prenotazione confermata!
  Posto: 1B
  Intestato a: luigi
```

---

## 🛠️ **PASSAGGI DA SEGUIRE**

### 📝 **STEP 1: Analisi e Progettazione (20-30 minuti)**

#### 1.1 Analizza il Problema
- Identifica le **classi necessarie**:
  - `PrenotazioneServer`: Server principale con matrice posti
  - `Posto`: Classe per rappresentare un singolo posto
  - `ClientHandler`: Thread per gestire ogni client
  - `PrenotazioneClient`: Client con menu interattivo
  - `AutenticazioneManager`: Gestione credenziali [OPZIONALE]

- Definisci la **struttura dati posti**:
  ```java
  class Posto {
      private boolean prenotato;
      private String usernamePrenotazione;
      private int fila;
      private char lettera;
  }
  ```

- Pianifica la **sincronizzazione**:
  - Accesso alla matrice posti deve essere sincronizzato
  - Prevenire doppie prenotazioni dello stesso posto

#### 1.2 Schema dell'Architettura
```
┌──────────────────────────────────────────┐
│       PrenotazioneServer                 │
│  - ServerSocket porta 8888               │
│  - Matrice[6][4] di Posto (synchronized) │
│  - AutenticazioneManager (opzionale)     │
│  - ClientHandler thread per ogni client  │
└──────────────────────────────────────────┘
              ▲         ▲         ▲
              │         │         │
       ┌──────┘    ┌────┘    └─────┐
       │           │                │
  ┌────▼─────┐ ┌──▼──────┐  ┌──────▼──────┐
  │ Client   │ │ Client  │  │ Client      │
  │ (mario)  │ │ (luigi) │  │ (anna)      │
  │ Posto 1A │ │ Posto 1B│  │ Posto 2C    │
  └──────────┘ └─────────┘  └─────────────┘
```

#### 1.3 Protocollo di Comunicazione
```
Client → Server:
- "AUTH username password"   - Autenticazione
- "VISUALIZZA"                - Richiede mappa posti
- "PRENOTA fila posto"        - Es: "PRENOTA 1 A"
- "CANCELLA fila posto"       - Es: "CANCELLA 1 A"
- "LISTA"                     - Prenotazioni dell'utente
- "QUIT"                      - Disconnessione

Server → Client:
- "AUTH_OK"                   - Login riuscito
- "AUTH_FAIL messaggio"       - Login fallito
- "MAPPA\n[dati matrice]"     - Invia mappa serializzata
- "PRENOTA_OK posto"          - Prenotazione confermata
- "PRENOTA_FAIL motivo"       - Prenotazione fallita
- "CANCELLA_OK posto"         - Cancellazione confermata
- "CANCELLA_FAIL motivo"      - Cancellazione fallita
- "LISTA\n[elenco]"           - Lista prenotazioni
```

---

### ⚙️ **STEP 2: Implementazione Classi di Supporto (30 minuti)**

#### 2.1 Classe Posto
Crea il file `Posto.java`:

```java
public class Posto {
    private int fila;
    private char lettera;
    private boolean prenotato;
    private String usernamePrenotazione;
    
    public Posto(int fila, char lettera) {
        this.fila = fila;
        this.lettera = lettera;
        this.prenotato = false;
        this.usernamePrenotazione = null;
    }
    
    public synchronized boolean prenota(String username) {
        if (prenotato) {
            return false; // Già prenotato
        }
        this.prenotato = true;
        this.usernamePrenotazione = username;
        return true;
    }
    
    public synchronized boolean cancella(String username) {
        if (!prenotato) {
            return false; // Non prenotato
        }
        if (!usernamePrenotazione.equals(username)) {
            return false; // Prenotato da altro utente
        }
        this.prenotato = false;
        this.usernamePrenotazione = null;
        return true;
    }
    
    public synchronized boolean isPrenotato() {
        return prenotato;
    }
    
    public synchronized String getUsernamePrenotazione() {
        return usernamePrenotazione;
    }
    
    public int getFila() {
        return fila;
    }
    
    public char getLettera() {
        return lettera;
    }
    
    public String getCodice() {
        return fila + "" + lettera;
    }
    
    @Override
    public String toString() {
        if (prenotato) {
            return "[X:" + usernamePrenotazione + "]";
        }
        return "[ ]";
    }
}
```

#### 2.2 Classe AutenticazioneManager [OPZIONALE]
Crea il file `AutenticazioneManager.java`:

```java
import java.util.HashMap;
import java.util.Map;

public class AutenticazioneManager {
    private Map<String, String> credenziali;
    
    public AutenticazioneManager() {
        credenziali = new HashMap<>();
        // Credenziali predefinite (in produzione andrebbero in un database)
        credenziali.put("mario", "mario123");
        credenziali.put("luigi", "luigi123");
        credenziali.put("anna", "anna123");
        credenziali.put("admin", "admin");
    }
    
    public boolean autentica(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        String passwordCorretta = credenziali.get(username);
        return password.equals(passwordCorretta);
    }
    
    public boolean registra(String username, String password) {
        if (credenziali.containsKey(username)) {
            return false; // Username già esistente
        }
        credenziali.put(username, password);
        return true;
    }
    
    public int getNumeroUtenti() {
        return credenziali.size();
    }
}
```

---

### ⚙️ **STEP 3: Implementazione Server (60-90 minuti)**

#### 3.1 Struttura Base del Server
Crea il file `PrenotazioneServer.java`:

```java
import java.io.*;
import java.net.*;
import java.util.*;

public class PrenotazioneServer {
    private static final int PORTA = 8888;
    private static final int NUM_FILE = 6;
    private static final int NUM_POSTI_PER_FILA = 4;
    private static final char[] LETTERE_POSTI = {'A', 'B', 'C', 'D'};
    
    private static Posto[][] posti;
    private static AutenticazioneManager authManager;
    
    public static void main(String[] args) {
        System.out.println("=== SERVER PRENOTAZIONE POSTI AEREO ===");
        
        // Inizializza matrice posti
        inizializzaPosti();
        
        // Inizializza autenticazione
        authManager = new AutenticazioneManager();
        
        System.out.println("Server avviato sulla porta " + PORTA);
        System.out.println("Aereo configurato: " + NUM_FILE + " file × " + 
                         NUM_POSTI_PER_FILA + " posti (" + 
                         (NUM_FILE * NUM_POSTI_PER_FILA) + " posti totali)");
        System.out.println("In attesa di connessioni...\n");
        
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Errore server: " + e.getMessage());
        }
    }
    
    private static void inizializzaPosti() {
        posti = new Posto[NUM_FILE][NUM_POSTI_PER_FILA];
        for (int i = 0; i < NUM_FILE; i++) {
            for (int j = 0; j < NUM_POSTI_PER_FILA; j++) {
                posti[i][j] = new Posto(i + 1, LETTERE_POSTI[j]);
            }
        }
    }
    
    public static synchronized String getMappaPosti(String username) {
        StringBuilder sb = new StringBuilder();
        sb.append("MAPPA\n");
        
        // Header con lettere
        sb.append("     ");
        for (char lettera : LETTERE_POSTI) {
            sb.append(lettera).append("    ");
        }
        sb.append("\n");
        
        // Righe con posti
        for (int i = 0; i < NUM_FILE; i++) {
            sb.append(String.format("%-2d", (i + 1))).append("  ");
            for (int j = 0; j < NUM_POSTI_PER_FILA; j++) {
                Posto posto = posti[i][j];
                if (posto.isPrenotato()) {
                    if (posto.getUsernamePrenotazione().equals(username)) {
                        sb.append("[X]  "); // Prenotato dall'utente
                    } else {
                        sb.append("[#]  "); // Prenotato da altri
                    }
                } else {
                    sb.append("[ ]  "); // Libero
                }
            }
            sb.append("\n");
        }
        
        // Statistiche
        int liberi = contaPostiLiberi();
        int totali = NUM_FILE * NUM_POSTI_PER_FILA;
        sb.append("\nPosti liberi: ").append(liberi).append("/").append(totali);
        sb.append("\n[X] = Tue prenotazioni");
        sb.append("\n[#] = Prenotato da altri");
        
        return sb.toString();
    }
    
    public static synchronized String prenotaPosto(int fila, char lettera, String username) {
        // Valida input
        if (fila < 1 || fila > NUM_FILE) {
            return "PRENOTA_FAIL Fila non valida (1-" + NUM_FILE + ")";
        }
        
        int colonna = trovaColonna(lettera);
        if (colonna == -1) {
            return "PRENOTA_FAIL Posto non valido (A-" + LETTERE_POSTI[NUM_POSTI_PER_FILA-1] + ")";
        }
        
        Posto posto = posti[fila - 1][colonna];
        
        if (posto.prenota(username)) {
            System.out.println("[PRENOTA] " + username + " -> Fila " + fila + 
                             ", Posto " + lettera + " - SUCCESSO");
            return "PRENOTA_OK " + posto.getCodice();
        } else {
            System.out.println("[PRENOTA] " + username + " -> Fila " + fila + 
                             ", Posto " + lettera + " - ERRORE: Già prenotato");
            String proprietario = posto.getUsernamePrenotazione();
            return "PRENOTA_FAIL Posto già prenotato da " + proprietario;
        }
    }
    
    public static synchronized String cancellaPosto(int fila, char lettera, String username) {
        if (fila < 1 || fila > NUM_FILE) {
            return "CANCELLA_FAIL Fila non valida";
        }
        
        int colonna = trovaColonna(lettera);
        if (colonna == -1) {
            return "CANCELLA_FAIL Posto non valido";
        }
        
        Posto posto = posti[fila - 1][colonna];
        
        if (posto.cancella(username)) {
            System.out.println("[CANCELLA] " + username + " -> Posto " + posto.getCodice());
            return "CANCELLA_OK " + posto.getCodice();
        } else {
            return "CANCELLA_FAIL Non puoi cancellare questo posto";
        }
    }
    
    public static synchronized String getListaPrenotazioni(String username) {
        StringBuilder sb = new StringBuilder("LISTA\n");
        List<String> prenotazioni = new ArrayList<>();
        
        for (int i = 0; i < NUM_FILE; i++) {
            for (int j = 0; j < NUM_POSTI_PER_FILA; j++) {
                Posto posto = posti[i][j];
                if (posto.isPrenotato() && 
                    posto.getUsernamePrenotazione().equals(username)) {
                    prenotazioni.add(posto.getCodice());
                }
            }
        }
        
        if (prenotazioni.isEmpty()) {
            sb.append("Nessuna prenotazione attiva");
        } else {
            sb.append("Le tue prenotazioni:\n");
            for (String codice : prenotazioni) {
                sb.append("- Posto ").append(codice).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    private static int trovaColonna(char lettera) {
        for (int i = 0; i < LETTERE_POSTI.length; i++) {
            if (LETTERE_POSTI[i] == Character.toUpperCase(lettera)) {
                return i;
            }
        }
        return -1;
    }
    
    private static int contaPostiLiberi() {
        int count = 0;
        for (int i = 0; i < NUM_FILE; i++) {
            for (int j = 0; j < NUM_POSTI_PER_FILA; j++) {
                if (!posti[i][j].isPrenotato()) {
                    count++;
                }
            }
        }
        return count;
    }
    
    public static AutenticazioneManager getAuthManager() {
        return authManager;
    }
    
    public static void stampaStatistiche() {
        int liberi = contaPostiLiberi();
        int totali = NUM_FILE * NUM_POSTI_PER_FILA;
        System.out.println("\n--- STATISTICHE ---");
        System.out.println("Posti totali: " + totali);
        System.out.println("Posti prenotati: " + (totali - liberi));
        System.out.println("Posti liberi: " + liberi);
        System.out.println("-------------------\n");
    }
}
```

#### 3.2 Classe ClientHandler
Crea il file `ClientHandler.java`:

```java
import java.io.*;
import java.net.*;

class ClientHandler implements Runnable {
    private Socket socket;
    private String username;
    private boolean autenticato;
    
    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.username = null;
        this.autenticato = false;
    }
    
    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8)
        ) {
            String clientIP = socket.getInetAddress().getHostAddress();
            System.out.println("[CONNESSIONE] Client da " + clientIP);
            
            // Ciclo principale
            String comando;
            while ((comando = in.readLine()) != null) {
                String risposta = processaComando(comando);
                out.println(risposta);
                if (comando.equals("VISUALIZZA") || comando.equals("LISTA")) out.println("END");
                
                if (comando.equals("QUIT")) {
                    break;
                }
            }
            
            if (username != null) {
                System.out.println("[DISCONNESSIONE] " + username + " disconnesso");
            } else {
                System.out.println("[DISCONNESSIONE] Client non autenticato disconnesso");
            }
            
        } catch (IOException e) {
            System.err.println("[ERRORE] Gestione client: " + e.getMessage());
        }
    }
    
    private String processaComando(String comando) {
        String[] parti = comando.split(" ", 3);
        String azione = parti[0];
        
        // Comandi che non richiedono autenticazione
        if (azione.equals("AUTH")) {
            if (parti.length < 3) {
                return "AUTH_FAIL Formato: AUTH username password";
            }
            return handleAuth(parti[1], parti[2]);
        }
        
        // Tutti gli altri comandi richiedono autenticazione
        if (!autenticato) {
            return "ERROR Autenticazione richiesta";
        }
        
        switch (azione) {
            case "VISUALIZZA":
                System.out.println("[VISUALIZZA] Mappa inviata a " + username);
                return PrenotazioneServer.getMappaPosti(username);
                
            case "PRENOTA":
                if (parti.length < 3) {
                    return "PRENOTA_FAIL Formato: PRENOTA fila posto";
                }
                return handlePrenota(parti[1], parti[2]);
                
            case "CANCELLA":
                if (parti.length < 3) {
                    return "CANCELLA_FAIL Formato: CANCELLA fila posto";
                }
                return handleCancella(parti[1], parti[2]);
                
            case "LISTA":
                return PrenotazioneServer.getListaPrenotazioni(username);
                
            case "QUIT":
                return "BYE Arrivederci";
                
            default:
                return "ERROR Comando non riconosciuto";
        }
    }
    
    private String handleAuth(String user, String password) {
        System.out.println("[AUTH] Tentativo login: " + user);
        
        if (PrenotazioneServer.getAuthManager().autentica(user, password)) {
            this.username = user;
            this.autenticato = true;
            System.out.println("[AUTH] Login riuscito: " + user);
            return "AUTH_OK Benvenuto " + user;
        } else {
            System.out.println("[AUTH] Login fallito: " + user);
            return "AUTH_FAIL Credenziali non valide";
        }
    }
    
    private String handlePrenota(String filaStr, String postoStr) {
        try {
            int fila = Integer.parseInt(filaStr);
            char posto = postoStr.toUpperCase().charAt(0);
            return PrenotazioneServer.prenotaPosto(fila, posto, username);
        } catch (NumberFormatException e) {
            return "PRENOTA_FAIL Fila deve essere un numero";
        } catch (Exception e) {
            return "PRENOTA_FAIL Errore: " + e.getMessage();
        }
    }
    
    private String handleCancella(String filaStr, String postoStr) {
        try {
            int fila = Integer.parseInt(filaStr);
            char posto = postoStr.toUpperCase().charAt(0);
            return PrenotazioneServer.cancellaPosto(fila, posto, username);
        } catch (NumberFormatException e) {
            return "CANCELLA_FAIL Fila deve essere un numero";
        } catch (Exception e) {
            return "CANCELLA_FAIL Errore: " + e.getMessage();
        }
    }
}
```

---

### 💻 **STEP 4: Implementazione Client (60-75 minuti)**

#### 4.1 Struttura Client
Crea il file `PrenotazioneClient.java`:

```java
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class PrenotazioneClient {
    private static final String HOST = "localhost";
    private static final int PORTA = 8888;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Scanner scanner;
    private String username;
    
    public static void main(String[] args) {
        new PrenotazioneClient().start();
    }
    
    public void start() {
        System.out.println("=== SISTEMA PRENOTAZIONE AEREO ===");
        
        try {
            socket = new Socket(HOST, PORTA);
            out = new PrintWriter(socket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            scanner = new Scanner(System.in);
            
            System.out.println("Connessione al server riuscita!\n");
            
            // Autenticazione
            if (!login()) {
                System.out.println("Autenticazione fallita. Disconnessione...");
                socket.close();
                return;
            }
            
            // Menu principale
            menuPrincipale();
            
            // Chiusura
            out.println("QUIT");
            socket.close();
            System.out.println("\nGrazie per aver utilizzato il servizio!");
            
        } catch (IOException e) {
            System.err.println("Errore connessione: " + e.getMessage());
        }
    }
    
    private boolean login() {
        System.out.println("--- LOGIN ---");
        System.out.print("Username: ");
        username = scanner.nextLine();
        
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        // Invia credenziali
        out.println("AUTH " + username + " " + password);
        
        try {
            String risposta = in.readLine();
            
            if (risposta == null) return false;
            if (risposta.startsWith("AUTH_OK")) {
                System.out.println("✓ Login effettuato con successo!\n");
                return true;
            } else {
                String motivo = risposta.substring("AUTH_FAIL ".length());
                System.out.println("✗ Login fallito: " + motivo);
                return false;
            }
        } catch (IOException e) {
            System.err.println("Errore durante login: " + e.getMessage());
            return false;
        }
    }
    
    private void menuPrincipale() {
        while (true) {
            System.out.println("\n--- MENU PRINCIPALE ---");
            System.out.println("1. Visualizza mappa posti");
            System.out.println("2. Prenota posto");
            System.out.println("3. Cancella prenotazione");
            System.out.println("4. Le mie prenotazioni");
            System.out.println("5. Esci");
            System.out.print("\nScelta > ");
            
            String scelta = scanner.nextLine();
            
            switch (scelta) {
                case "1":
                    visualizzaMappa();
                    break;
                case "2":
                    prenotaPosto();
                    break;
                case "3":
                    cancellaPrenotazione();
                    break;
                case "4":
                    visualizzaPrenotazioni();
                    break;
                case "5":
                    System.out.println("\nDisconnessione in corso...");
                    return;
                default:
                    System.out.println("✗ Scelta non valida");
            }
        }
    }
    
    private void visualizzaMappa() {
        try {
            out.println("VISUALIZZA");
            
            String linea;
            System.out.println("\n=== MAPPA POSTI AEREO ===");
            
            // Salta "MAPPA"
            in.readLine();
            
            // Le righe vuote sono contenuto; solo END termina la risposta.
            while ((linea = in.readLine()) != null && !linea.equals("END")) {
                System.out.println(linea);
            }
            
        } catch (IOException e) {
            System.err.println("Errore visualizzazione mappa: " + e.getMessage());
        }
    }
    
    private void prenotaPosto() {
        System.out.println("\n--- PRENOTA POSTO ---");
        
        System.out.print("Fila (1-6): ");
        String fila = scanner.nextLine();
        
        System.out.print("Posto (A-D): ");
        String posto = scanner.nextLine().toUpperCase();
        
        try {
            out.println("PRENOTA " + fila + " " + posto);
            String risposta = in.readLine();
            
            if (risposta.startsWith("PRENOTA_OK")) {
                String codice = risposta.substring("PRENOTA_OK ".length());
                System.out.println("\n✓ Prenotazione confermata!");
                System.out.println("  Posto: " + codice);
                System.out.println("  Intestato a: " + username);
            } else {
                String motivo = risposta.substring("PRENOTA_FAIL ".length());
                System.out.println("\n✗ Errore: " + motivo);
            }
            
        } catch (IOException e) {
            System.err.println("Errore prenotazione: " + e.getMessage());
        }
    }
    
    private void cancellaPrenotazione() {
        System.out.println("\n--- CANCELLA PRENOTAZIONE ---");
        
        System.out.print("Fila (1-6): ");
        String fila = scanner.nextLine();
        
        System.out.print("Posto (A-D): ");
        String posto = scanner.nextLine().toUpperCase();
        
        try {
            out.println("CANCELLA " + fila + " " + posto);
            String risposta = in.readLine();
            
            if (risposta.startsWith("CANCELLA_OK")) {
                String codice = risposta.substring("CANCELLA_OK ".length());
                System.out.println("\n✓ Prenotazione cancellata!");
                System.out.println("  Posto: " + codice);
            } else {
                String motivo = risposta.substring("CANCELLA_FAIL ".length());
                System.out.println("\n✗ Errore: " + motivo);
            }
            
        } catch (IOException e) {
            System.err.println("Errore cancellazione: " + e.getMessage());
        }
    }
    
    private void visualizzaPrenotazioni() {
        try {
            out.println("LISTA");
            
            String linea;
            System.out.println("\n--- LE TUE PRENOTAZIONI ---");
            
            // Salta "LISTA"
            in.readLine();
            
            // Consuma tutta la risposta fino al terminatore END.
            while ((linea = in.readLine()) != null && !linea.equals("END")) {
                System.out.println(linea);
            }
            
        } catch (IOException e) {
            System.err.println("Errore visualizzazione prenotazioni: " + e.getMessage());
        }
    }
}
```

---

### 🧪 **STEP 5: Testing e Documentazione (45-60 minuti)**

#### 5.1 Test di Compilazione:
```bash
# Compila tutti i file
javac Posto.java AutenticazioneManager.java \
      PrenotazioneServer.java ClientHandler.java \
      PrenotazioneClient.java

# Verifica assenza errori
```

#### 5.2 Test Funzionale Multi-Client:
```bash
# Terminale 1: Avvia il server
java PrenotazioneServer

# Terminale 2: Client mario
java PrenotazioneClient
# Login: mario / mario123

# Terminale 3: Client luigi
java PrenotazioneClient
# Login: luigi / luigi123

# Terminale 4: Client anna
java PrenotazioneClient
# Login: anna / anna123
```

#### 5.3 Scenari di Test Completi:

| # | Scenario | Client | Azione | Risultato Atteso | Screenshot |
|---|----------|--------|--------|------------------|------------|
| 1 | Avvio sistema | - | Avvia server | Server in ascolto | ✅ SS01 |
| 2 | Login valido | mario | Login mario/mario123 | AUTH_OK | ✅ SS02 |
| 3 | Mappa vuota | mario | Visualizza mappa | 24/24 posti liberi | ✅ SS03 |
| 4 | Prima prenotazione | mario | Prenota 1A | Successo | ✅ SS04 |
| 5 | Mappa aggiornata | mario | Visualizza mappa | 1A prenotato [X] | ✅ SS05 |
| 6 | Login secondo user | luigi | Login luigi/luigi123 | AUTH_OK | ✅ SS06 |
| 7 | Doppia prenotazione | luigi | Prenota 1A | Errore: già prenotato | ✅ SS07 |
| 8 | Prenotazione OK | luigi | Prenota 1B | Successo | ✅ SS08 |
| 9 | Mappa multi-user | luigi | Visualizza mappa | 1A=[#], 1B=[X] | ✅ SS09 |
| 10 | Lista prenotazioni | mario | Comando LISTA | Mostra 1A | ✅ SS10 |
| 11 | Cancellazione | mario | Cancella 1A | Successo | ✅ SS11 |
| 12 | Cancellazione altrui | luigi | Cancella 2A (di mario) | Errore | ✅ SS12 |
| 13 | Test concorrenza | 3 client | Prenotazioni simultanee | No race condition | ✅ SS13 |
| 14 | Login fallito | - | Login errato | AUTH_FAIL | ✅ SS14 |
| 15 | Statistiche server | - | Visualizza statistiche | Conteggi corretti | ✅ SS15 |

#### 5.4 Creazione Documento Google

**Template Documento:**

```
SISTEMA PRENOTAZIONE POSTI AEREO - Esercitazione Socket TCP
============================================================

Studente: [Nome Cognome]
Classe: [Classe]
Data: [Data]

---

1. INTRODUZIONE
   Descrizione del sistema implementato:
   - Server TCP con matrice 6×4 posti
   - Autenticazione username/password
   - Gestione prenotazioni concorrenti
   - Visualizzazione mappa grafica

2. ARCHITETTURA
   [Inserire schema client-server]
   
   Classi implementate:
   - PrenotazioneServer.java
   - ClientHandler.java
   - Posto.java
   - AutenticazioneManager.java
   - PrenotazioneClient.java

3. PROTOCOLLO DI COMUNICAZIONE
   Comandi Client → Server:
   - AUTH username password
   - VISUALIZZA
   - PRENOTA fila posto
   - CANCELLA fila posto
   - LISTA
   - QUIT
   
   Risposte Server → Client:
   - AUTH_OK / AUTH_FAIL
   - PRENOTA_OK / PRENOTA_FAIL
   - CANCELLA_OK / CANCELLA_FAIL
   - MAPPA [dati]
   - LISTA [dati]

4. SCREENSHOT FUNZIONAMENTO
   
   Screenshot 1: Server avviato
   [Inserire immagine]
   
   Screenshot 2: Login mario
   [Inserire immagine]
   
   Screenshot 3: Mappa posti vuota
   [Inserire immagine]
   
   Screenshot 4-15: [Seguire tabella test]
   [Inserire immagini]

5. CODICE SORGENTE
   Link repository:
   <URL_DEL_TUO_REPOSITORY>
   
   Oppure link Google Drive:
   <URL_CARTELLA_CONSEGNA>

6. FUNZIONALITÀ IMPLEMENTATE
   ✅ Server multi-client con thread
   ✅ Matrice posti sincronizzata
   ✅ Autenticazione utenti
   ✅ Visualizzazione mappa grafica
   ✅ Prenotazione con controllo disponibilità
   ✅ Cancellazione solo proprie prenotazioni
   ✅ Lista prenotazioni per utente
   ✅ Gestione errori completa

7. PROBLEMI RISCONTRATI E SOLUZIONI
   [Descrivere eventuali difficoltà]
   
   Es: "Race condition su prenotazioni simultanee"
   Soluzione: synchronized sui metodi della classe Posto

8. TESTING
   Test effettuati:
   - 3 client contemporanei: OK
   - Doppia prenotazione stesso posto: BLOCCATA
   - Cancellazione prenotazione altrui: BLOCCATA
   - Login con credenziali errate: RIFIUTATO
   
9. POSSIBILI ESTENSIONI
   - Persistenza su database/file
   - Classi di viaggio (Economy, Business, First)
   - Prezzi differenziati per posto
   - Scelta posti adiacenti per gruppi
   - Timer di scadenza prenotazione
   - GUI Swing/JavaFX

10. CONCLUSIONI
    [Cosa hai imparato]
    [Competenze acquisite]
```

---

## 📝 **DELIVERABLE RICHIESTI**

### 📁 File da Consegnare:

1. **Codice Sorgente Java:**
   - `PrenotazioneServer.java`
   - `ClientHandler.java`
   - `Posto.java`
   - `AutenticazioneManager.java` (se implementato)
   - `PrenotazioneClient.java`

2. **Documento Google:** (Link obbligatorio)
   - Minimo 15 screenshot
   - Descrizione funzionamento
   - Link al codice sorgente

3. **Repository Git:** (consigliato)
   - Cartella `/ES02c-Prenotazioni/`
   - README.md con istruzioni
   - .gitignore per .class

---

## 🏆 **CRITERI DI VALUTAZIONE**

### 📊 Griglia di Valutazione (Totale: 100 punti)

| Criterio | Punti | Descrizione |
|----------|-------|-------------|
| **Server Funzionante** | 20 | Server gestisce matrice e client multipli |
| **Sincronizzazione** | 15 | No race condition, synchronized corretto |
| **Trasmissione Matrice** | 10 | Mappa serializzata e visualizzata correttamente |
| **Prenotazione/Cancellazione** | 20 | CRUD completo e funzionante |
| **Client Interattivo** | 10 | Menu chiaro e gestione errori |
| **Documentazione Screenshot** | 20 | 15+ screenshot chiari e descrittivi |
| **Qualità Codice** | 5 | Leggibilità, commenti, struttura |
| **OPZIONALE: Autenticazione** | +20 | Login/password implementato |

### 🎯 Livelli di Competenza:

**Valutazione Base (100 punti):**
- **90-100:** ⭐⭐⭐⭐⭐ **Eccellente** - Tutto perfetto con autenticazione
- **80-89:** ⭐⭐⭐⭐ **Buono** - Funziona correttamente
- **70-79:** ⭐⭐⭐ **Sufficiente** - Funzionalità base OK
- **< 70:** ⭐⭐ **Insufficiente** - Incompleto o non funzionante

**Bonus Opzionali:** +20 punti per autenticazione completa

---

## 💡 **SUGGERIMENTI E TRUCCHI**

### 🔧 **Best Practices:**

```java
// 1. Usa synchronized sulla classe Posto
public synchronized boolean prenota(String username) {
    // Thread-safe
}

// 2. Serializza mappa con StringBuilder
StringBuilder sb = new StringBuilder();
sb.append("MAPPA\n");
// ...costruisci mappa

// 3. Valida sempre input utente
if (fila < 1 || fila > MAX_FILE) {
    return "ERRORE: Fila non valida";
}

// 4. Usa try-with-resources
try (Socket socket = new Socket(HOST, PORT)) {
    // ...
}
```

### 🚨 **Errori Comuni da Evitare:**

- ❌ **Race condition** su matrice posti (usa `synchronized`)
- ❌ **Trasmissione matrice** incompleta (usa delimitatore EOF)
- ❌ **Password in chiaro** nel codice (usa AutenticazioneManager)
- ❌ **Non validare** fila/posto prima di accedere array
- ❌ **Dimenticare close()** su socket e stream

### 📸 **Tips Screenshot Professionali:**

```bash
# Organizza screenshot con nomi parlanti
01-server-avviato.png
02-login-mario-success.png
03-mappa-vuota.png
04-prenotazione-1A-success.png
05-mappa-con-prenotazioni.png
06-doppia-prenotazione-error.png
...

# Usa Flameshot per annotazioni
flameshot gui

# Dimensione finestre uniformi per estetica
```

### 🎯 **Estensioni Avanzate (Extra):**

#### 1. Persistenza su File
```java
private static void salvaSuFile() {
    try (PrintWriter pw = new PrintWriter("prenotazioni.txt")) {
        for (int i = 0; i < NUM_FILE; i++) {
            for (int j = 0; j < NUM_POSTI_PER_FILA; j++) {
                Posto p = posti[i][j];
                if (p.isPrenotato()) {
                    pw.println(p.getCodice() + ":" + p.getUsernamePrenotazione());
                }
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

#### 2. Prenotazione con Prezzo
```java
class Posto {
    private double prezzo;
    
    public Posto(int fila, char lettera) {
        // File 1-2: €150, File 3-4: €100, File 5-6: €80
        if (fila <= 2) prezzo = 150.0;
        else if (fila <= 4) prezzo = 100.0;
        else prezzo = 80.0;
    }
}
```

#### 3. Timeout Prenotazione
```java
import java.time.LocalDateTime;

class Prenotazione {
    private final String username;
    private LocalDateTime scadenza;
    
    public Prenotazione(String user) {
        this.username = user;
        this.scadenza = LocalDateTime.now().plusMinutes(15);
    }
    
    public boolean isScaduta() {
        return LocalDateTime.now().isAfter(scadenza);
    }
}
```

---

## 📚 **RISORSE UTILI**

### 📖 **Documentazione:**
- [Java Networking](https://docs.oracle.com/javase/tutorial/networking/sockets/)
- [Synchronized Methods](https://docs.oracle.com/javase/tutorial/essential/concurrency/syncmeth.html)
- [Arrays 2D in Java](https://docs.oracle.com/javase/tutorial/java/nutsandbolts/arrays.html)

### 🔍 **Comandi Debug:**
```bash
# Test manuale protocollo
telnet localhost 8888
AUTH mario mario123
VISUALIZZA
PRENOTA 1 A
LISTA
QUIT

# Monitoring connessioni
watch -n 1 'netstat -an | grep 8888'

# Test concorrenza con script
for i in {1..5}; do
    java PrenotazioneClient &
done
```

### 💬 **FAQ:**

**Q: Come evito che due client prenotino lo stesso posto?**  
A: Usa `synchronized` sul metodo `prenota()` della classe Posto.

**Q: Come trasmetto la matrice attraverso il socket?**  
A: Serializzala in una stringa con righe separate da `\n`. Il client la ricompone.

**Q: Le password sono sicure?**  
A: No, sono in chiaro. In produzione usa hash (SHA-256) e salt.

**Q: Posso usare JSON per trasmettere la mappa?**  
A: Sì! Puoi usare librerie come Gson o Jackson, ma non è obbligatorio.

**Q: Come faccio screenshot di 4 terminali contemporaneamente?**  
A: Usa un tiling window manager o cattura l'intero desktop.

---

## ⏰ **SCADENZE E MODALITÀ DI CONSEGNA**

### 📅 **Timeline:**
- **Assegnazione:** [Data di oggi]
- **Consegna:** [Data + 2 settimane]

### 📤 **Modalità Consegna:**

**1. Documento Google (OBBLIGATORIO):**
- Creare documento Google Docs
- Inserire tutti gli screenshot richiesti
- Aggiungere link al codice sorgente
- Condividere link con docente

**2. Codice Sorgente:**

Opzione A - Repository Git (consigliato):
```
<URL_DEL_TUO_REPOSITORY>
└── ES02c-Prenotazioni/
    ├── README.md
    ├── Posto.java
    ├── AutenticazioneManager.java
    ├── PrenotazioneServer.java
    ├── ClientHandler.java
    └── PrenotazioneClient.java
```

Opzione B - Google Drive:
```
ES02c-Prenotazioni.zip
├── src/
│   ├── Posto.java
│   ├── AutenticazioneManager.java
│   ├── PrenotazioneServer.java
│   ├── ClientHandler.java
│   └── PrenotazioneClient.java
└── README.txt (istruzioni compilazione)
```

### 📧 **Contatti:**
- **Email docente:** [email@scuola.it]
- **Orario ricevimento:** [Giorni e orari]

---

## 🎉 **CONCLUSIONI**

Questa esercitazione avanzata ti permetterà di:

- 🎯 **Gestire** strutture dati complesse in rete
- 🔒 **Implementare** sincronizzazione robusta
- 🔐 **Applicare** autenticazione client-server
- 👥 **Coordinare** client multipli su risorsa condivisa
- 📊 **Visualizzare** dati matriciali in modo user-friendly
- 📸 **Documentare** professionalmente il tuo lavoro

**Scenario Reale:** Stai costruendo un sistema usato da compagnie aeree, cinema, teatri, stadi! La gestione di prenotazioni è fondamentale nell'industria IT.

**Consiglio Strategico:**
1. Inizia con server base (senza autenticazione)
2. Aggiungi client con menu semplice
3. Testa con 2-3 client per verificare sincronizzazione
4. Aggiungi autenticazione come estensione
5. Documenta con screenshot durante lo sviluppo

**Challenge Finale:** Riesci a gestire 10 client che prenotano simultaneamente senza race condition? 🏆

---

**Buon lavoro e buon coding! ✈️🚀**

---

*Esercitazione creata per il corso di Sistemi e Reti 3 - ITCS Cannizzaro di Rho*  
*Anno Scolastico 2025/26*  
*Versione 1.0 - Novembre 2025*
