# 🎓 ESERCITAZIONE: Socket TCP Client-Server in Java "Calcolatrice Remota"

> *Verifica delle competenze sui socket TCP - Sistemi e Reti 3*

---

## 📋 **INFORMAZIONI GENERALI**

**Materia:** Sistemi e Reti  
**Argomento:** Socket Programming TCP in Java  
**Tempo stimato:** 2-3 ore  
**Difficoltà:** ⭐⭐⭐ (Intermedia)  
**Modalità:** Individuale o a coppie  

---

## 🎯 **OBIETTIVI DIDATTICI**

Al termine di questa esercitazione lo studente sarà in grado di:

- ✅ **Implementare** un server TCP che ascolta su una porta specifica
- ✅ **Creare** un client TCP che si connette a un server
- ✅ **Gestire** la comunicazione bidirezionale tra client e server
- ✅ **Applicare** protocolli di comunicazione personalizzati
- ✅ **Implementare** algoritmi di elaborazione dati in rete
- ✅ **Gestire** correttamente errori e chiusura delle connessioni
- ✅ **Documentare** il codice e testare l'applicazione

---

## 📚 **PREREQUISITI**

### Conoscenze Teoriche Richieste:
- 🔌 **Socket TCP/IP:** Concetti base di client-server
- ☕ **Java I/O:** `BufferedReader`, `PrintWriter`, `Socket`, `ServerSocket`
- 🔄 **Exception Handling:** `try-catch-finally`, resource management oppure `try-with-resources`
- 📝 **String manipulation:** Parsing e manipolazione stringhe

### Strumenti Necessari:
- ☕ **Java JDK** 11 o superiore
- 💻 **IDE** o editor di testo (VS Code, IntelliJ, Eclipse)
- 🖥️ **Terminale** per compilazione ed esecuzione

---

## 📖 **TRACCIA DELL'ESERCIZIO**

### 🚀 **Applicazione: "Calcolatrice Remota"**

Realizzare un'applicazione client-server dove:

1. **Il SERVER:**
   - Ascolta sulla porta `8844`
   - Riceve operazioni matematiche dal client nel formato: `NUMERO1 OPERAZIONE NUMERO2`
   - Esegue il calcolo richiesto (+, -, *, /)
   - Restituisce il risultato al client
   - Gestisce errori (divisione per zero, formato non valido)
   - Tiene un contatore delle operazioni eseguite
   - Si chiude quando riceve il comando "QUIT"

2. **Il CLIENT:**
   - Si connette al server localhost:8844
   - Presenta un menu interattivo all'utente
   - Invia le richieste di calcolo al server
   - Mostra i risultati ricevuti
   - Permette di disconnettersi con il comando "quit"

### 📐 **Esempi di Interazione:**

```
Client input: "10 + 5"
Server response: "RISULTATO: 15"

Client input: "20 / 4"  
Server response: "RISULTATO: 5.0"

Client input: "8 * 3"
Server response: "RISULTATO: 24"

Client input: "15 / 0"
Server response: "ERRORE: Divisione per zero non consentita"

Client input: "abc + 5"
Server response: "ERRORE: Formato non valido. Usa: NUMERO OPERAZIONE NUMERO"

Client input: "quit"
Server response: "CHIUSURA: Operazioni eseguite: 4. Arrivederci!"
```

---

## 🛠️ **PASSAGGI DA SEGUIRE**

### 📝 **STEP 1: Analisi e Progettazione (15-20 minuti)**

#### 1.1 Analizza il Problema
- Identifica gli **attori**: Server (calcolatrice) e Client (utente)
- Definisci il **protocollo di comunicazione**:
  ```
  Client → Server: "NUMERO1 OPERAZIONE NUMERO2"
  Server → Client: "RISULTATO: valore" | "ERRORE: descrizione"
  ```
- Elenca le **operazioni supportate**: `+`, `-`, `*`, `/`
- Pianifica la **gestione errori**: formato invalido, divisione per zero

#### 1.2 Schema dell'Architettura
```
┌─────────────┐    TCP Socket    ┌─────────────┐
│   CLIENT    │◄────────────────►│   SERVER    │
│             │   porta 8844     │             │
│ - Input UI  │                  │ - Calcoli   │
│ - Validaz.  │                  │ - Contatori │
│ - Display   │                  │ - Gestione  │
│   risultati │                  │   errori    │
└─────────────┘                  └─────────────┘
```

---

### ⚙️ **STEP 2: Implementazione Server (45-60 minuti)**

#### 2.1 Struttura Base del Server
Crea il file `CalcolatriceServer.java`:

```java
import java.io.*;
import java.net.*;

public class CalcolatriceServer {
    private static final int PORTA = 8844;
    
    public static void main(String[] args) {
        // TODO: Implementa qui il server
    }
}
```

#### 2.2 Componenti da Implementare:

**A) Setup del ServerSocket:**
```java
try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
    System.out.println("Server avviato sulla porta " + PORTA);
    // Accetta connessioni...
}
```

**B) Gestione Client:**
```java
while (true) {
    Socket client = serverSocket.accept();
    // Gestisci comunicazione con client
}
```

**C) Parsing delle Richieste:**
```java
// Esempio di parsing: "10 + 5"
String[] parti = richiesta.split(" ");
if (parti.length != 3) {
    return "ERRORE: Formato non valido";
}
double num1 = Double.parseDouble(parti[0]);
String operazione = parti[1];
double num2 = Double.parseDouble(parti[2]);
```

**D) Esecuzione Calcoli:**
```java
switch (operazione) {
    case "+": return num1 + num2;
    case "-": return num1 - num2;
    case "*": return num1 * num2;
    case "/": 
        if (num2 == 0) throw new ArithmeticException("Divisione per zero");
        return num1 / num2;
    default: throw new IllegalArgumentException("Operazione non supportata");
}
```

#### 2.3 Checklist Server:
- [ ] ServerSocket sulla porta 8080
- [ ] Loop di accettazione client
- [ ] Parsing corretto delle richieste
- [ ] Implementazione delle 4 operazioni matematiche
- [ ] Gestione divisione per zero
- [ ] Gestione formato non valido
- [ ] Contatore operazioni eseguite
- [ ] Comando "QUIT" per chiusura
- [ ] Chiusura corretta delle risorse

---

### 💻 **STEP 3: Implementazione Client (30-45 minuti)**

#### 3.1 Struttura Base del Client
Crea il file `CalcolatriceClient.java`:

```java
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class CalcolatriceClient {
    private static final String HOST = "localhost";
    private static final int PORTA = 8080;
    
    public static void main(String[] args) {
        // TODO: Implementa qui il client
    }
}
```

#### 3.2 Componenti da Implementare:

**A) Connessione al Server:**
```java
try (Socket socket = new Socket(HOST, PORTA);
     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
     Scanner scanner = new Scanner(System.in)) {
    // Comunicazione...
}
```

**B) Menu Utente:**
```java
System.out.println("=== CALCOLATRICE REMOTA ===");
System.out.println("Formato: NUMERO OPERAZIONE NUMERO");
System.out.println("Operazioni: + - * /");
System.out.println("Scrivi 'quit' per uscire");
```

**C) Loop di Interazione:**
```java
while (true) {
    System.out.print("Calcolo > ");
    String input = scanner.nextLine();
    
    if (input.equalsIgnoreCase("quit")) {
        out.println("QUIT");
        break;
    }
    
    // Invia richiesta e ricevi risposta
}
```

#### 3.3 Checklist Client:
- [ ] Connessione a localhost:8080
- [ ] Menu utente chiaro e informativo
- [ ] Loop di input dall'utente
- [ ] Invio richieste al server
- [ ] Ricezione e visualizzazione risposte
- [ ] Comando "quit" per disconnessione
- [ ] Gestione errori di connessione
- [ ] Chiusura corretta delle risorse

---

### 🧪 **STEP 4: Testing e Debug (20-30 minuti)**

#### 4.1 Test di Compilazione:
```bash
# Compila entrambi i file
javac CalcolatriceServer.java CalcolatriceClient.java

# Verifica che non ci siano errori di compilazione
```

#### 4.2 Test Funzionale Base:
```bash
# Terminale 1: Avvia il server
java CalcolatriceServer

# Terminale 2: Avvia il client
java CalcolatriceClient
```

#### 4.3 Casi di Test da Verificare:

| Input | Output Atteso | Verifica |
|-------|---------------|----------|
| `10 + 5` | `RISULTATO: 15.0` | ✅ Addizione |
| `20 - 8` | `RISULTATO: 12.0` | ✅ Sottrazione |
| `6 * 7` | `RISULTATO: 42.0` | ✅ Moltiplicazione |
| `15 / 3` | `RISULTATO: 5.0` | ✅ Divisione |
| `10 / 0` | `ERRORE: Divisione per zero` | ✅ Gestione errori |
| `abc + 5` | `ERRORE: Formato non valido` | ✅ Input invalido |
| `quit` | Disconnessione client | ✅ Chiusura |

#### 4.4 Debug Checklist:
- [ ] Server si avvia senza errori
- [ ] Client si connette correttamente
- [ ] Tutte le operazioni matematiche funzionano
- [ ] Gestione errori corretta
- [ ] Chiusura graceful di entrambi i programmi
- [ ] Nessuna eccezione non gestita

---

## 📝 **DELIVERABLE RICHIESTI**

### 📁 File da Consegnare:
1. **`CalcolatriceServer.java`** - Implementazione completa del server
2. **`CalcolatriceClient.java`** - Implementazione completa del client  
3. **`RELAZIONE.md`** - Relazione tecnica (vedi template sotto)
4. **Screenshot** - Dimostrazione del funzionamento

### 📖 Template Relazione (`RELAZIONE.md`):

```markdown
# Relazione: Calcolatrice Remota TCP

## Dati Studente
- **Nome:** [Il tuo nome]
- **Cognome:** [Il tuo cognome]  
- **Classe:** [La tua classe]
- **Data:** [Data di consegna]

## Descrizione dell'Applicazione
[Descrivi brevemente cosa fa l'applicazione]

## Architettura Implementata
### Server:
- Porta di ascolto: 
- Operazioni supportate:
- Gestione errori:

### Client:  
- Modalità di connessione:
- Interfaccia utente:
- Gestione input:

## Protocollo di Comunicazione
[Descrivi il formato dei messaggi scambiati]

## Problemi Riscontrati e Soluzioni
[Elenca eventuali difficoltà incontrate e come le hai risolte]

## Testing Effettuato
[Descrivi i test eseguiti e i risultati]

## Screenshot
[Inserisci screenshot che dimostrano il funzionamento]

## Conclusioni  
[Considera cosa hai imparato e possibili miglioramenti]
```

---

## 🏆 **CRITERI DI VALUTAZIONE**

### 📊 Griglia di Valutazione (Totale: 100 punti)

| Criterio | Punti | Descrizione |
|----------|-------|-------------|
| **Funzionalità Server** | 25 | Server accetta connessioni e esegue calcoli correttamente |
| **Funzionalità Client** | 20 | Client si connette e gestisce l'interazione utente |
| **Gestione Errori** | 15 | Divisione per zero, formato invalido, connessioni |
| **Protocollo Comunicazione** | 15 | Formato messaggi corretto e consistente |
| **Qualità Codice** | 10 | Leggibilità, commenti, struttura |
| **Testing** | 10 | Completezza dei test e verifica funzionamento |
| **Documentazione** | 5 | Relazione tecnica completa e chiara |

### 🎯 Livelli di Competenza:

- **90-100 punti:** ⭐⭐⭐⭐⭐ **Eccellente** - Implementazione completa e robusta
- **80-89 punti:** ⭐⭐⭐⭐ **Buono** - Funziona correttamente con piccole imperfezioni  
- **70-79 punti:** ⭐⭐⭐ **Sufficiente** - Funzionalità base implementate
- **60-69 punti:** ⭐⭐ **Insufficiente** - Implementazione incompleta o con errori
- **< 60 punti:** ⭐ **Gravemente insufficiente** - Non funzionante

---

## 💡 **SUGGERIMENTI E TRUCCHI**

### 🔧 **Best Practices:**
- **Usa try-with-resources** per la gestione automatica delle risorse
- **Valida sempre l'input** prima di processarlo  
- **Gestisci tutte le eccezioni** in modo appropriato
- **Usa costanti** per porta e messaggi di protocollo
- **Commenta il codice** specialmente le parti complesse

### 🚨 **Errori Comuni da Evitare:**
- ❌ Non chiudere correttamente socket e stream
- ❌ Non gestire la divisione per zero
- ❌ Non validare il formato dell'input
- ❌ Usare numeri di porta < 1024 (riservate)
- ❌ Non gestire la disconnessione improvvisa del client

### 🎯 **Estensioni Opzionali (Punti Bonus):**
- 🔄 **Server multi-client** (ThreadPoolExecutor)
- 📊 **Storico operazioni** salvato su file
- 🧮 **Operazioni avanzate** (potenza, radice, sin, cos)
- 🎨 **Interfaccia grafica** per il client (Swing/JavaFX)
- 🔐 **Autenticazione** client con username/password

---

## 📚 **RISORSE UTILI**

### 📖 **Documentazione Oracle:**
- [Socket Programming Tutorial](https://docs.oracle.com/javase/tutorial/networking/sockets/)
- [ServerSocket Class](https://docs.oracle.com/javase/8/docs/api/java/net/ServerSocket.html)
- [Socket Class](https://docs.oracle.com/javase/8/docs/api/java/net/Socket.html)

### 🔍 **Comandi Debug Utili:**
```bash
# Verifica porta in uso
netstat -an | grep 8080

# Testa connessione manualmente  
telnet localhost 8080

# Monitora connessioni
ss -tuln | grep 8080
```

### 💬 **FAQ Comuni:**

**Q: Il server non si avvia, errore "Address already in use"**  
A: La porta 8080 è già occupata. Cambia porta o termina il processo esistente.

**Q: Client non si connette al server**  
A: Verifica che il server sia avviato e che la porta sia corretta.

**Q: Come gestisco input con spazi nei numeri decimali?**  
A: Usa `Double.parseDouble()` che gestisce automaticamente i decimali.

---

## ⏰ **SCADENZE E MODALITÀ DI CONSEGNA**

### 📅 **Timeline:**
- **Assegnazione:** [Data di oggi]
- **Consegna:** [Data + 1 settimana]
- **Presentazione:** [Data + 10 giorni]

### 📤 **Modalità Consegna:**
1. **Codice:** Repository Git o archivio ZIP
2. **Relazione:** File PDF o Markdown
3. **Screenshot:** Immagini che dimostrano il funzionamento
4. **Video (opzionale):** Demo di 2-3 minuti

### 📧 **Contatti:**
- **Email docente:** [email@scuola.it]
- **Orario ricevimento:** [Giorni e orari]
- **Supporto tecnico:** [Modalità di supporto]

---

## 🎉 **CONCLUSIONI**

Questa esercitazione ti permetterà di consolidare le competenze sui socket TCP e di comprendere meglio l'architettura client-server. 

**Ricorda:** La programmazione di rete richiede attenzione ai dettagli e una gestione robusta degli errori. Non scoraggiarti se incontri difficoltà - è normale! Ogni errore è un'opportunità di apprendimento.

**Buon lavoro! 🚀**

---

*Esercitazione creata per il corso di Sistemi e Reti 3 - ITCS Cannizzaro di Rho*  
*Anno Scolastico 2025/26*