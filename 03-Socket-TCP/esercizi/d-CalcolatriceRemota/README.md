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
   - Conta soltanto i calcoli riusciti; richieste errate e QUIT non incrementano il contatore
   - Su QUIT risponde con CHIUSURA e il contatore, poi chiude client e server

2. **Il CLIENT:**
   - Si connette al server localhost:8844
   - Presenta un menu interattivo all'utente
   - Invia le richieste di calcolo al server
   - Mostra i risultati ricevuti
   - Invia QUIT senza distinzione di maiuscole e legge il saluto prima di disconnettersi

### Protocollo e materiale disponibile

I due programmi sono **da implementare**: gli scheletri e i frammenti sotto
illustrano i passaggi e non costituiscono già una soluzione completa. Crea i
file indicati prima di usare i comandi di compilazione.

Usa righe di testo UTF-8, una richiesta e una risposta per riga, con flush.
Per ogni calcolo invia `NUMERO OPERAZIONE NUMERO`; accetta il punto decimale e
spazi tra gli elementi. Rifiuta NaN, infinito, operatori sconosciuti e divisione
per zero con una riga `ERRORE: ...`, continuando la sessione. Il risultato usa
la rappresentazione `double`, per esempio `RISULTATO: 15.0`.

Il client invia `QUIT`, legge `CHIUSURA: Operazioni eseguite: N. Arrivederci!`
e poi chiude. Il server termina dopo il flush della risposta. Se il client
si disconnette senza QUIT, il server libera la sessione e può accettare un altro
client in sequenza. Il contatore del server conta soltanto i calcoli riusciti.

### 📐 **Esempi di Interazione:**

```
Client input: "10 + 5"
Server response: "RISULTATO: 15.0"

Client input: "20 / 4"  
Server response: "RISULTATO: 5.0"

Client input: "8 * 3"
Server response: "RISULTATO: 24.0"

Client input: "15 / 0"
Server response: "ERRORE: Divisione per zero non consentita"

Client input: "abc + 5"
Server response: "ERRORE: Formato non valido. Usa: NUMERO OPERAZIONE NUMERO"

Client input: "quit"
Server response: "CHIUSURA: Operazioni eseguite: 3. Arrivederci!"
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

**C) Parsing delle richieste, dentro un metodo che restituisce la risposta testuale:**
```java
// Esempio di parsing: "10 + 5"
String[] parti = richiesta.trim().split("\\s+");
if (parti.length != 3) {
    return "ERRORE: Formato non valido";
}
double num1 = Double.parseDouble(parti[0]);
String operazione = parti[1];
double num2 = Double.parseDouble(parti[2]);
if (!Double.isFinite(num1) || !Double.isFinite(num2)) {
    return "ERRORE: Sono ammessi soltanto numeri finiti";
}
```

Gestisci `NumberFormatException`, `ArithmeticException` e gli altri errori di
validazione nel metodo di elaborazione, trasformandoli in risposte `ERRORE: ...`.

**D) Metodo di calcolo da aggiungere alla classe server:**
```java
private static double calcola(double num1, String operazione, double num2) {
    switch (operazione) {
        case "+": return num1 + num2;
        case "-": return num1 - num2;
        case "*": return num1 * num2;
        case "/":
            if (num2 == 0) throw new ArithmeticException("Divisione per zero");
            return num1 / num2;
        default: throw new IllegalArgumentException("Operazione non supportata");
    }
}
```

#### 2.3 Checklist Server:
- [ ] ServerSocket sulla porta 8844
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
    private static final int PORTA = 8844;
    
    public static void main(String[] args) {
        // TODO: Implementa qui il client
    }
}
```

#### 3.2 Componenti da Implementare:

**A) Connessione al Server:**
```java
try (Socket socket = new Socket(HOST, PORTA);
     PrintWriter out = new PrintWriter(socket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8);
     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
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
    if (!scanner.hasNextLine()) {
        break; // EOF da tastiera
    }
    String input = scanner.nextLine().trim();
    
    if (input.equalsIgnoreCase("quit")) {
        out.println("QUIT");
        String chiusura = in.readLine();
        System.out.println(chiusura == null ? "Server disconnesso" : chiusura);
        break;
    }
    
    out.println(input);
    String risposta = in.readLine();
    if (risposta == null) {
        System.out.println("Server disconnesso");
        break;
    }
    System.out.println(risposta);
}
```

#### 3.3 Checklist Client:
- [ ] Connessione a localhost:8844
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
javac -encoding UTF-8 CalcolatriceServer.java CalcolatriceClient.java

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
netstat -an | grep 8844

# Testa connessione manualmente  
telnet localhost 8844

# Monitora connessioni
ss -tanp '( sport = :8844 or dport = :8844 )'
```

### 💬 **FAQ Comuni:**

**Q: Il server non si avvia, errore "Address already in use"**  
A: La porta 8844 è già occupata. Cambia porta o termina il processo esistente.

**Q: Client non si connette al server**  
A: Verifica che il server sia avviato e che la porta sia corretta.

**Q: Come gestisco input con spazi nei numeri decimali?**  
A: Usa il punto decimale, per esempio `2.5 + 1.5`. Normalizza gli spazi tra i tre elementi con `trim().split("\\s+")`; non inserire spazi dentro un numero.

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

[Indice del modulo](../../README.md)
