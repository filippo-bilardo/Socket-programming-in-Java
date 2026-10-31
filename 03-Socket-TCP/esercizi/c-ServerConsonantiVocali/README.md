# 🎓 ESERCITAZIONE: Socket TCP Client-Server in Java "Consonanti e vocali"

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
- 🔄 **Exception Handling:** `try-catch-finally`, resource management
- 📝 **String manipulation:** Parsing e manipolazione stringhe

### Strumenti Necessari:
- ☕ **Java JDK** 11 o superiore
- 💻 **IDE** o editor di testo (VS Code, IntelliJ, Eclipse)
- 🖥️ **Terminale** per compilazione ed esecuzione

---

## 📖 **TRACCIA DELL'ESERCIZIO**

### 🚀 **Applicazione: "Server Consonanti e Vocali"**

Realizzare un'applicazione client-server dove:

1. **Il SERVER:**
   - Ascolta sulla porta `8080`
   - Riceve stringhe dal client
   - Conta il numero di vocali e consonanti nella stringa
   - Restituisce il risultato al client
   - Gestisce errori (formato non valido)
   - Tiene un contatore delle operazioni eseguite
   - Si chiude quando riceve il comando "QUIT"

2. **Il CLIENT:**
   - Si connette al server localhost:8080
   - Presenta un menu interattivo all'utente
   - Invia le richieste di calcolo al server
   - Mostra i risultati ricevuti
   - Permette di disconnettersi con il comando "quit"

### 📐 **Esempi di Interazione:**

```
Client > Ciao Mondo
Server > Vocali: 4, Consonanti: 6

Client > Java Programming
Server > Vocali: 5, Consonanti: 10

Client > 12345
Server > ERRORE: Formato non valido

Client > QUIT
Server > Connessione chiusa. Operazioni eseguite: 3
```

---

## 🛠️ **PASSAGGI DA SEGUIRE**

### 📝 **STEP 1: Analisi e Progettazione (15-20 minuti)**

#### 1.1 Analizza il Problema
- Identifica gli **attori**: Server (consonanti e vocali) e Client (utente)
- Definisci il **protocollo di comunicazione**:
  ```
  Client → Server: "NUMERO1 OPERAZIONE NUMERO2"
  Server → Client: "RISULTATO: valore" | "ERRORE: descrizione"
  ```
- Elenca le **operazioni supportate**: conta vocali e consonanti
- Definisci i **casi di errore**: Formato non valido (numeri, simboli, stringhe vuote)
  

#### 1.2 Schema dell'Architettura
```
┌─────────────┐    TCP Socket    ┌─────────────┐
│   CLIENT    │◄────────────────►│   SERVER    │
│             │   porta 8080     │             │
│ - Input UI  │                  │ - Calcoli   │
│ - Validaz.  │                  │ - Contatori │
│ - Display   │                  │ - Gestione  │
│   risultati │                  │   errori    │
└─────────────┘                  └─────────────┘
```

---

### ⚙️ **STEP 2: Implementazione Server (45-60 minuti)**

#### 2.1 Struttura Base del Server


#### 2.2 Componenti da Implementare:

#### 2.3 Checklist Server:
- [ ] ServerSocket sulla porta 8080
- [ ] Loop di accettazione client
- [ ] Parsing corretto delle richieste
- [ ] Implementazione conteggio vocali/consonanti
- [ ] Invio risposte al client
- [ ] Gestione formato non valido
- [ ] Contatore operazioni eseguite
- [ ] Comando "QUIT" per chiusura
- [ ] Chiusura corretta delle risorse

---

### 💻 **STEP 3: Implementazione Client (30-45 minuti)**

#### 3.1 Struttura Base del Client

#### 3.2 Componenti da Implementare:

#### 3.3 Checklist Client:
- [ ] Connessione a localhost:8080
- [ ] Menu utente chiaro e informativo
- [ ] Loop di input dall'utente
- [ ] Invio richieste al server
- [ ] Ricezione e visualizzazione risposte
- [ ] Comando "QUIT" per disconnessione
- [ ] Gestione errori di connessione
- [ ] Chiusura corretta delle risorse

---

### 🧪 **STEP 4: Testing e Debug (20-30 minuti)**

#### 4.1 Test di Compilazione:
```bash
# Compila entrambi i file

# Verifica che non ci siano errori di compilazione
```

#### 4.2 Test Funzionale Base:
```bash
# Terminale 1: Avvia il server

# Terminale 2: Avvia il client

```

#### 4.3 Casi di Test da Verificare:

| Input | Output Atteso | Verifica |
|-------|---------------|----------|
| "Ciao Mondo" | "Vocali: 4, Consonanti: 6" | [ ] |
| "Java Programming" | "Vocali: 5, Consonanti: 10" | [ ] |
| "12345" | "ERRORE: Formato non valido" | [ ] |
| "" (stringa vuota) | "ERRORE: Formato non valido" | [ ] |
| "QUIT" | "Connessione chiusa. Operazioni eseguite: X" | [ ] |

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
1. **`CVServer.java`** - Implementazione completa del server
2. **`CVClient.java`** - Implementazione completa del client  
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
| **Gestione Errori** | 15 | Gestione corretta di input non validi e disconnessioni |
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