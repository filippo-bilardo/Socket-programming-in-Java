# 📡 Applicazione Client-Server: Conteggio Vocali e Consonanti

> *Esercizio di Socket Programming - SISTEMI E RETI 3*

## 📋 Descrizione del Problema

Realizzare un'applicazione client-server dove:

1. **Il client** invia messaggi al server
2. **Il server** conta vocali e consonanti di ogni messaggio
3. **Il server** restituisce i conteggi al client
4. **Il client** continua a inviare frasi finché il numero di consonanti sia esattamente **la metà del numero di vocali**
5. **Il server** termina quando si raggiunge questa condizione

### 🎯 Condizione di Terminazione
```
consonanti = vocali / 2
```

---

## 📁 Struttura del Progetto

```
ES01-04-ServerConsonantiVocali/
├── 📄 esercizio.md                    # Testo dell'esercizio
├── ☕ ServerVocaliConsonanti.java     # Server socket
├── ☕ ClientVocaliConsonanti.java     # Client socket  
└── 📖 README.md                       # Questa documentazione
```

---

## ⚙️ Specifiche Tecniche

### 🖥️ Server (`ServerVocaliConsonanti.java`)

**Funzionalità:**
- ✅ Ascolta sulla porta `12345`
- ✅ Accetta connessione da un client
- ✅ Riceve messaggi di testo
- ✅ Conta vocali (`aeiouAEIOU`) e consonanti
- ✅ Invia risultati al client nel formato `VOCALI:x;CONSONANTI:y;TOTALE:z`
- ✅ Verifica condizione di terminazione: `consonanti == vocali/2`
- ✅ Termina e notifica il client quando la condizione è raggiunta

**Protocollo di comunicazione:**
```
Client → Server: "messaggio di testo"
Server → Client: "VOCALI:5;CONSONANTI:7;TOTALE:12"

Oppure (quando termina):
Server → Client: "TERMINA:Condizione raggiunta - Server in chiusura"
```

### 💻 Client (`ClientVocaliConsonanti.java`)

**Funzionalità:**
- ✅ Si connette al server localhost:12345
- ✅ Interfaccia utente interattiva
- ✅ Invia messaggi inseriti dall'utente
- ✅ Riceve e visualizza i risultati del conteggio
- ✅ Mostra rapporto consonanti/vocali per monitorare la condizione
- ✅ Termina quando il server chiude la connessione
- ✅ Comando `exit` per disconnessione manuale

---

## 🚀 Istruzioni per l'Esecuzione

```bash
# 1. Compilazione
javac ServerVocaliConsonanti.java ClientVocaliConsonanti.java

# 2. Avvio server (terminale 1)
java ServerVocaliConsonanti

# 3. Avvio client (terminale 2)  
java ClientVocaliConsonanti

# 4. Pulizia
rm *.class
```

---

## 🧪 Esempi di Test

### ❌ Messaggi che NON terminano il server:

| Messaggio | Vocali | Consonanti | Rapporto | Terminazione |
|-----------|--------|------------|----------|--------------|
| `"ciao mondo"` | 4 | 5 | 1.25 | ❌ (5 ≠ 2) |
| `"programmazione"` | 6 | 7 | 1.17 | ❌ (7 ≠ 3) |
| `"java socket"` | 4 | 6 | 1.50 | ❌ (6 ≠ 2) |

### ✅ Messaggi che terminano il server:

| Messaggio | Vocali | Consonanti | Rapporto | Terminazione |
|-----------|--------|------------|----------|--------------|
| `"aeioubc"` | 5 | 2 | 0.40 | ✅ (2 = 5/2) |
| `"aob"` | 2 | 1 | 0.50 | ✅ (1 = 2/2) |
| `"aeioubcd"` | 5 | 3 | 0.60 | ❌ (3 ≠ 2) |

### 🎯 Sequenza di test completa:

```
1. "ciao" → vocali=3, consonanti=1 → 1 ≠ 1.5 → continua
2. "hello world" → vocali=3, consonanti=7 → 7 ≠ 1.5 → continua  
3. "aeiou" → vocali=5, consonanti=0 → 0 ≠ 2.5 → continua
4. "aeioubc" → vocali=5, consonanti=2 → 2 = 2.5 → TERMINA! ✅
```

---

## 🔍 Output di Esempio

### Server Output:
```
=== SERVER VOCALI-CONSONANTI AVVIATO ===
In ascolto sulla porta 12345
Condizione di terminazione: consonanti = vocali/2
==========================================

Client connesso: /127.0.0.1

--- Messaggio #1 ---
Ricevuto: "ciao mondo"
Vocali: 4
Consonanti: 5
Lettere totali: 9

--- Messaggio #2 ---
Ricevuto: "aeioubc"
Vocali: 5
Consonanti: 2  
Lettere totali: 7

🎯 CONDIZIONE DI TERMINAZIONE RAGGIUNTA!
Consonanti (2) = Vocali/2 (2)

=== SESSIONE TERMINATA ===
```

### Client Output:
```
=== CLIENT VOCALI-CONSONANTI ===
Connessione al server localhost:12345
================================

✅ Connesso al server!
📝 Inserisci frasi per il conteggio vocali/consonanti
🎯 Il server terminerà quando consonanti = vocali/2
❌ Scrivi 'exit' per disconnetterti manualmente

Messaggio #1 > ciao mondo
   📊 RISULTATI ANALISI:
   ┌─────────────────────────────────┐
   │ Testo analizzato: "ciao mondo"
   │ Vocali trovate: 4
   │ Consonanti trovate: 5
   │ Lettere totali: 9
   └─────────────────────────────────┘
   📈 Rapporto consonanti/vocali: 1.25
   🎯 Serve rapporto 0.5 per terminare

Messaggio #2 > aeioubc
   📊 RISULTATI ANALISI:
   ┌─────────────────────────────────┐
   │ Testo analizzato: "aeioubc"
   │ Vocali trovate: 5
   │ Consonanti trovate: 2
   │ Lettere totali: 7
   └─────────────────────────────────┘
   📈 Rapporto consonanti/vocali: 0.40
   🎯 Serve rapporto 0.5 per terminare
   🔥 CONDIZIONE RAGGIUNTA! Il server terminerà.

🏁 Condizione raggiunta - Server in chiusura
Il server ha terminato l'applicazione.

=== SESSIONE CLIENT TERMINATA ===
```

---

## 🛠️ Dettagli Implementativi

### 🔒 Gestione Errori
- ✅ **Connessione fallita:** Messaggio chiaro se il server non è raggiungibile
- ✅ **Disconnessione improvvisa:** Gestione della chiusura inaspettata
- ✅ **Input non valido:** Controllo di input vuoti o malformati
- ✅ **Parsing errori:** Gestione robusta del parsing delle risposte

### 🚦 Protocollo di Comunicazione
```java
// Formato risposta standard del server
"VOCALI:X;CONSONANTI:Y;TOTALE:Z"

// Formato terminazione server  
"TERMINA:Motivo di chiusura"

// Comando uscita client
"EXIT"
```

### 📊 Algoritmo di Conteggio
```java
private static final String VOCALI = "aeiouAEIOU";

for (char carattere : testo.toCharArray()) {
    if (Character.isLetter(carattere)) {
        if (VOCALI.indexOf(carattere) != -1) {
            vocali++;
        } else {
            consonanti++;
        }
    }
}
```

### 🎯 Condizione di Terminazione
```java
private static boolean verificaCondizioneDiUscita(int vocali, int consonanti) {
    return vocali > 0 && consonanti == vocali / 2;
}
```

---

## 🔧 Troubleshooting

### ❌ Problemi Comuni

**1. "Connection refused"**
- ✅ Verifica che il server sia avviato
- ✅ Controlla che la porta 12345 sia libera
- ✅ Usa `./run.sh server` prima di `./run.sh client`

**2. "Address already in use"**
- ✅ Il server è già in esecuzione
- ✅ Termina processi java attivi: `pkill java`
- ✅ Cambia porta nel codice se necessario

**3. "Class not found"**
- ✅ Compila con `./run.sh compile`
- ✅ Verifica presenza file .java nella directory corrente

**4. Il server non termina**
- ✅ Verifica che il messaggio contenga esattamente consonanti = vocali/2
- ✅ Usa esempi testati come `"aeioubc"` o `"aob"`

---

## 📝 Note Tecniche

### 🏗️ Architettura
- **Pattern:** Client-Server tradizionale
- **Protocollo:** TCP Socket  
- **Porta:** 12345 (configurabile)
- **Concorrenza:** Single-threaded (un client alla volta)

### 🔢 Calcoli Matematici
- **Vocali:** Solo caratteri alfabetici in `aeiouAEIOU`
- **Consonanti:** Caratteri alfabetici non vocali
- **Divisione:** Usa divisione intera (`vocali / 2`)
- **Spazi e punteggiatura:** Ignorati nel conteggio

### 🚀 Possibili Estensioni
- 🔄 **Multi-client:** Server che gestisce più client contemporaneamente
- 💾 **Persistenza:** Salvataggio statistiche su file
- 🌐 **Web UI:** Interfaccia web al posto del client console
- 📊 **Analytics:** Statistiche avanzate sui messaggi processati

---

## 👨‍🎓 Obiettivi Didattici

### 💡 Competenze Sviluppate
- ✅ **Socket Programming:** TCP client-server
- ✅ **Gestione I/O:** BufferedReader, PrintWriter
- ✅ **Parsing Stringhe:** Analisi e manipolazione testo
- ✅ **Gestione Errori:** Try-catch, resource management
- ✅ **Protocolli Rete:** Definizione formato messaggi
- ✅ **Debugging:** Testing e risoluzione problemi

### 🎯 Concetti Chiave
- **TCP Socket:** Comunicazione affidabile client-server
- **Blocking I/O:** Lettura/scrittura sincrona  
- **Resource Management:** Auto-closeable resources
- **Protocol Design:** Formato messaggi strutturato
- **State Management:** Gestione stati connessione

---

## 📚 Bibliografia e Riferimenti

- 📖 **Java Network Programming** - Elliotte Rusty Harold
- 🌐 **Oracle Java Socket Tutorial:** https://docs.oracle.com/javase/tutorial/networking/sockets/
- 📚 **Sistemi di Elaborazione e Trasmissione delle Informazioni** - Materiale didattico

---

## ✅ Checklist Verifica Funzionamento

### 🖥️ Server
- [ ] Si avvia correttamente sulla porta 12345
- [ ] Accetta connessioni dai client  
- [ ] Riceve e processa messaggi di testo
- [ ] Conta correttamente vocali e consonanti
- [ ] Invia risposte nel formato corretto
- [ ] Verifica accuratamente la condizione di terminazione
- [ ] Termina e notifica quando consonanti = vocali/2

### 💻 Client
- [ ] Si connette correttamente al server
- [ ] Interfaccia utente funzionale e chiara
- [ ] Invia messaggi inseriti dall'utente
- [ ] Riceve e mostra correttamente i risultati  
- [ ] Calcola e visualizza il rapporto consonanti/vocali
- [ ] Gestisce correttamente la terminazione del server
- [ ] Comando 'exit' funziona per disconnessione manuale

### 🧪 Test Integration
- [ ] Comunicazione bidirezionale funziona
- [ ] Messaggi vengono trasmessi correttamente
- [ ] Conteggi sono accurati per diversi tipi di testo
- [ ] Condizione di terminazione viene rilevata correttamente
- [ ] Gestione errori robusta in tutti gli scenari

---

*Esercizio realizzato per il corso di **Sistemi e Reti 3** - ITIS "S. Cannizzaro"*  
*Socket Programming in Java - Anno Accademico 2025/26*