# 🎓 ESERCITAZIONE: Socket UDP Server-Client in Java "Daytime Server con Tracking"

> Verifica delle competenze sui socket UDP - Sistemi e Reti 3

## 📋 INFORMAZIONI GENERALI

**Materia:** Sistemi e Reti  
**Argomento:** Socket Programming UDP in Java  
**Tempo stimato:** 2-3 ore  
**Difficoltà:** ⭐⭐⭐ (Intermedia)  
**Modalità:** Individuale o a coppie

## 🎯 OBIETTIVI DIDATTICI

Al termine di questa esercitazione lo studente sarà in grado di:

- ✅ Implementare un server UDP che ascolta su una porta specifica
- ✅ Creare un client UDP che invia datagrammi a un server
- ✅ Gestire la comunicazione connectionless tra client e server
- ✅ Identificare client tramite indirizzo IP e porta
- ✅ Implementare persistenza dei dati su file
- ✅ Gestire logica di business (conteggio connessioni, limitazioni)
- ✅ Applicare pattern di autenticazione con API key
- ✅ Gestire correttamente errori e eccezioni in ambiente UDP

## 📚 PREREQUISITI

### Conoscenze Teoriche Richieste:

- 📡 **Socket UDP**: Differenze tra TCP e UDP, comunicazione connectionless
- ☕ **Java I/O**: `DatagramSocket`, `DatagramPacket`, `InetAddress`
- 💾 **File I/O**: Lettura/scrittura file, serializzazione dati
- 🔄 **Exception Handling**: `try-catch-finally`, resource management
- 🗺️ **Collections**: `HashMap`, `Properties` per memorizzare dati

### Strumenti Necessari:

- ☕ Java JDK 11 o superiore
- 💻 IDE o editor di testo (VS Code, IntelliJ, Eclipse)
- 🖥️ Terminale per compilazione ed esecuzione

## 📦 MODALITÀ DI CONSEGNA

> **⚠️ IMPORTANTE:** Seguire attentamente le modalità di consegna per ogni step!

### 📌 Consegna del Codice:

- ✅ Crea un repository **GitHub** per l'esercitazione
- ✅ Carica i file sorgente (`.java`) su GitHub
- ✅ Nella relazione, inserisci il **link del repository** (non copiare/incollare il codice)
- ✅ Assicurati che il repository contenga:
  - Codice sorgente di ogni step
  - README.md con istruzioni di compilazione ed esecuzione
  - File `.gitignore` per escludere file compilati (`.class`)

### 📸 Documentazione dei Test:

- ✅ Per ogni step, esegui i test richiesti
- ✅ Cattura **screenshot** che dimostrano:
  - Output del server (log, messaggi, contatori)
  - Output del client (richieste, risposte)
  - File creati (quando richiesto)
- ✅ Nomina gli screenshot in modo descrittivo: `stepN_descrizione.png`
- ✅ Organizza gli screenshot in una cartella `screenshots/` nel repository o nella relazione

### 📝 Struttura della Relazione:

La relazione deve contenere:
1. **Introduzione** - Obiettivi e tecnologie utilizzate
2. **Per ogni STEP:**
   - 📌 Link del repository GitHub con il codice
   - 📸 Screenshot dei test eseguiti
   - 💬 Breve spiegazione delle scelte implementative
3. **Conclusioni** - Difficoltà incontrate e soluzioni adottate

##  TRACCIA DELL'ESERCIZIO- Applicazione: "Daytime Server UDP con Sistema Bonus"
Realizzare un'applicazione client-server dove:

### 1. Il SERVER:
- Ascolta sulla **porta 13** (servizio standard "daytime")
- Riceve pacchetti UDP da client (anche vuoti)
- Restituisce la **data e ora correnti** in formato leggibile (es. "2025-12-18 15:30:45")
- **Traccia il numero di connessioni** per ogni client (identificato tramite IP)
- **Memorizza i dati persistentemente** su file (`client_connections.dat`)
- **Implementa sistema bonus:**
  - Prime 9 connessioni: invia data/ora normalmente
  - 10ª connessione: invia avviso "Hai esaurito i bonus gratuiti! Il servizio è ora a pagamento."
  - Dalla 11ª connessione in poi: invia solo messaggio di servizio a pagamento
- **Supporta autenticazione con API_KEY:**
  - Client può inviare una API_KEY valida per continuare a ricevere il servizio
  - Se la chiave è valida, il contatore viene resettato o il limite bypassato

### 2. Il CLIENT:
- Si connette al server (localhost o remoto) sulla porta 13
- Presenta un menu interattivo all'utente:
  - `1` - Richiedi data/ora
  - `2` - Richiedi data/ora con API_KEY
  - `3` - Esci
- Invia pacchetti UDP al server
- Mostra le risposte ricevute
- Gestisce timeout (se il server non risponde entro 5 secondi)

### 📐 Esempi di Interazione:
```
==== CONNESSIONE 1 ====
Client → Server: [pacchetto vuoto]
Server → Client: "2025-12-18 10:15:30"

==== CONNESSIONE 5 ====
Client → Server: [pacchetto vuoto]
Server → Client: "2025-12-18 10:20:15"

==== CONNESSIONE 10 ====
Client → Server: [pacchetto vuoto]
Server → Client: "Hai esaurito i bonus gratuiti! Il servizio è ora a pagamento."

==== CONNESSIONE 11 ====
Client → Server: [pacchetto vuoto]
Server → Client: "Servizio a pagamento. Invia una API_KEY valida."

==== CONNESSIONE 12 con API_KEY ====
Client → Server: "API_KEY:abc123xyz"
Server → Client: "2025-12-18 10:25:45"  [servizio ripristinato]
```

# 🎓 Guida Passo-Passo: Daytime Server UDP con Tracking

> **Approccio Incrementale:** Costruiamo l'applicazione partendo da una versione semplice e aggiungendo funzionalità progressive.

## 📚 Indice

1. [STEP 1: Versione Base - Server e Client Minimali](#step-1-versione-base)
2. [STEP 2: Aggiungere Tracking Connessioni (In Memoria)](#step-2-tracking-in-memoria)
3. [STEP 3: Implementare Logica Bonus](#step-3-logica-bonus)
4. [STEP 4: Aggiungere Persistenza su File](#step-4-persistenza)
5. [STEP 5: Implementare API_KEY](#step-5-api-key)
6. [STEP 6: Miglioramenti Finali](#step-6-miglioramenti)

---

## STEP 1: Versione Base - Server e Client Minimali {#step-1-versione-base}

**🎯 Obiettivo:** Creare un server UDP che invia data/ora e un client che la riceve.

### � Analisi dei Requisiti:

**Requisiti Funzionali:**
- RF1: Il server deve ascoltare sulla porta 1313
- RF2: Il server deve rispondere a qualsiasi richiesta UDP con la data/ora corrente
- RF3: Il client deve inviare una richiesta al server
- RF4: Il client deve visualizzare la risposta ricevuta
- RF5: Il formato data/ora deve essere: "yyyy-MM-dd HH:mm:ss"

**Requisiti Non Funzionali:**
- RNF1: Il server deve gestire richieste multiple (loop infinito)
- RNF2: Il client deve avere un timeout di 5 secondi
- RNF3: Utilizzo del protocollo UDP (connectionless)
- RNF4: Gestione delle eccezioni di rete

**Vincoli Tecnici:**
- Porta 1313 (test) invece di porta 13 (standard) per evitare permessi root
- Buffer di 1024 byte per i datagrammi
- Java 11+ per LocalDateTime e DateTimeFormatter

### �📋 Cosa implementiamo:
- Server che riceve un datagramma e risponde con la data/ora corrente
- Client che invia una richiesta e visualizza la risposta

### 🔧 Server Versione 1.0 (DaytimeServerV1.java)

```java
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DaytimeServerV1 {
    // Porta su cui il server ascolta (1313 per test, 13 è la porta standard daytime)
    private static final int PORTA = 1313;
    
    // Formattatore per visualizzare data e ora in formato leggibile
    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static void main(String[] args) {
        System.out.println("=== DAYTIME SERVER V1.0 ===");
        System.out.println("Server avviato sulla porta " + PORTA);
        
        // try-with-resources: chiude automaticamente il socket alla fine
        try (DatagramSocket socket = new DatagramSocket(PORTA)) {
            
            // Loop infinito per gestire richieste multiple
            while (true) {
                // ─────────────────────────────────────────
                // PASSO 1: RICEZIONE RICHIESTA DAL CLIENT
                // ─────────────────────────────────────────
                
                // Buffer per contenere i dati ricevuti (max 1024 byte)
                byte[] buffer = new byte[1024];
                
                // Crea il pacchetto per la ricezione
                DatagramPacket packetRicevuto = new DatagramPacket(buffer, buffer.length);
                
                // BLOCCA qui fino all'arrivo di un datagramma
                socket.receive(packetRicevuto);
                
                // Estrae e stampa l'indirizzo IP del client
                System.out.println("Richiesta da: " + 
                    packetRicevuto.getAddress().getHostAddress());
                
                // ─────────────────────────────────────────
                // PASSO 2: PREPARAZIONE RISPOSTA
                // ─────────────────────────────────────────
                
                // Ottieni data/ora corrente formattata
                String dataOra = LocalDateTime.now().format(FORMATTER);
                
                // Converti la stringa in array di byte per l'invio
                byte[] dati = dataOra.getBytes();
                
                // ─────────────────────────────────────────
                // PASSO 3: INVIO RISPOSTA AL CLIENT
                // ─────────────────────────────────────────
                
                // Crea il pacchetto di risposta indirizzato al client
                // Usa indirizzo e porta del pacchetto ricevuto per rispondere
                DatagramPacket packetRisposta = new DatagramPacket(
                    dati, dati.length,
                    packetRicevuto.getAddress(),  // Indirizzo del client
                    packetRicevuto.getPort()       // Porta del client
                );
                
                // Invia il pacchetto
                socket.send(packetRisposta);
                
                // Log della risposta inviata
                System.out.println("Inviato: " + dataOra);
            }
            
        } catch (Exception e) {
            // Gestione errori (porta occupata, permessi, ecc.)
            System.err.println("Errore: " + e.getMessage());
        }
    }
}
```

### 🔧 Client Versione 1.0 (DaytimeClientV1.java)

```java
import java.io.*;
import java.net.*;

public class DaytimeClientV1 {
    // Indirizzo del server (localhost per test su stessa macchina)
    private static final String HOST = "localhost";
    
    // Porta su cui il server ascolta (deve corrispondere a quella del server)
    private static final int PORTA = 1313;
    
    // Timeout per la ricezione (5 secondi = 5000 millisecondi)
    private static final int TIMEOUT = 5000;
    
    public static void main(String[] args) {
        System.out.println("=== DAYTIME CLIENT V1.0 ===");
        
        // try-with-resources: chiude automaticamente il socket
        try (DatagramSocket socket = new DatagramSocket()) {
            
            // Imposta timeout: se non arriva risposta entro 5 secondi, lancia eccezione
            socket.setSoTimeout(TIMEOUT);
            
            // ─────────────────────────────────────────
            // PASSO 1: PREPARAZIONE E INVIO RICHIESTA
            // ─────────────────────────────────────────
            
            // Risolve il nome host in indirizzo IP
            InetAddress serverAddress = InetAddress.getByName(HOST);
            
            // Dati da inviare (stringa vuota per questo protocollo)
            byte[] dati = "".getBytes();
            
            // Crea il pacchetto destinato al server
            DatagramPacket packetInvio = new DatagramPacket(
                dati, dati.length, serverAddress, PORTA
            );
            
            System.out.println("Invio richiesta al server...");
            
            // Invia il pacchetto al server
            socket.send(packetInvio);
            
            // ─────────────────────────────────────────
            // PASSO 2: RICEZIONE E VISUALIZZAZIONE RISPOSTA
            // ─────────────────────────────────────────
            
            // Buffer per ricevere la risposta
            byte[] buffer = new byte[1024];
            
            // Crea il pacchetto per la ricezione
            DatagramPacket packetRisposta = new DatagramPacket(buffer, buffer.length);
            
            // BLOCCA qui fino all'arrivo della risposta (o timeout)
            socket.receive(packetRisposta);
            
            // Estrae la stringa dai byte ricevuti
            // Usa getLength() per leggere solo i byte effettivamente ricevuti
            String risposta = new String(
                packetRisposta.getData(), 0, packetRisposta.getLength()
            );
            
            // Visualizza la risposta
            System.out.println("Data/Ora server: " + risposta);
            
        } catch (SocketTimeoutException e) {
            // Eccezione specifica per timeout
            System.err.println("TIMEOUT: Server non risponde");
        } catch (Exception e) {
            // Altre eccezioni (host non trovato, errori I/O, ecc.)
            System.err.println("Errore: " + e.getMessage());
        }
    }
}
```

### ✅ Test STEP 1:

```bash
# Terminale 1
javac DaytimeServerV1.java
java DaytimeServerV1

# Terminale 2
javac DaytimeClientV1.java
java DaytimeClientV1
# Ripeti più volte
```

**Risultato atteso:** Il client riceve e stampa la data/ora corrente del server.

> 📌 **CONSEGNA CODICE:**  
> Carica i file `DaytimeServerV1.java` e `DaytimeClientV1.java` su GitHub e inserisci il **link del repository** nella relazione.  
> **NON** copiare/incollare il codice direttamente nel documento.

> 📸 **DOCUMENTAZIONE TEST:**  
> Allega screenshot che mostrano:
> - Output del server quando riceve la richiesta
> - Output del client con la data/ora ricevuta  

---

## STEP 2: Aggiungere Tracking Connessioni (In Memoria) {#step-2-tracking-in-memoria}

**🎯 Obiettivo:** Tenere traccia di quante volte ogni client si connette (senza persistenza).

### � Analisi dei Requisiti:

**Requisiti Funzionali:**
- RF1: Il server deve identificare ogni client tramite indirizzo IP
- RF2: Il server deve mantenere un contatore delle connessioni per ogni IP
- RF3: Il contatore deve incrementare ad ogni richiesta dello stesso client
- RF4: La risposta deve includere il numero di connessione corrente
- RF5: Il formato risposta diventa: "data/ora [Connessione #N]"

**Requisiti Non Funzionali:**
- RNF1: Utilizzo di strutture dati efficienti (HashMap)
- RNF2: I dati sono volatili (in memoria RAM)
- RNF3: Perdita dei dati al riavvio del server
- RNF4: Complessità O(1) per lookup e update del contatore

**Vincoli Tecnici:**
- Chiave HashMap: String (indirizzo IP)
- Valore HashMap: Integer (contatore connessioni)
- Uso di `getOrDefault()` per inizializzazione automatica

**Limitazioni:**
- ⚠️ I dati NON sopravvivono al riavvio del server
- ⚠️ Nessuna distinzione tra client dietro lo stesso NAT

### �📋 Cosa aggiungiamo:
- HashMap per memorizzare IP → numero connessioni
- Conteggio e visualizzazione del numero di connessione

### 🔧 Modifiche al Server (DaytimeServerV2.java)

**Aggiungi in cima (dopo le costanti):**
```java
// HashMap per tracciare le connessioni: chiave = IP, valore = numero connessioni
private static Map<String, Integer> clientConnections = new HashMap<>();
```

**Modifica il loop principale:**
```java
while (true) {
    // Ricevi richiesta (come prima)
    byte[] buffer = new byte[1024];
    DatagramPacket packetRicevuto = new DatagramPacket(buffer, buffer.length);
    socket.receive(packetRicevuto);
    
    // Estrai l'IP del client
    String clientIP = packetRicevuto.getAddress().getHostAddress();
    
    // ═════════════════════════════════════════════
    // NUOVO: TRACKING CONNESSIONI
    // ═════════════════════════════════════════════
    
    // Recupera il contatore attuale (0 se prima volta)
    // e incrementa di 1
    int count = clientConnections.getOrDefault(clientIP, 0) + 1;
    
    // Salva il nuovo valore nella mappa
    clientConnections.put(clientIP, count);
    
    // Log con numero connessione
    System.out.println("Richiesta da: " + clientIP + " (connessione #" + count + ")");
    
    // ═════════════════════════════════════════════
    // RISPOSTA MODIFICATA: include conteggio
    // ═════════════════════════════════════════════
    
    // Prepara risposta con data/ora + conteggio
    String dataOra = LocalDateTime.now().format(FORMATTER);
    String risposta = dataOra + " [Connessione #" + count + "]";
    byte[] dati = risposta.getBytes();
    
    // Invia risposta (come prima)
    DatagramPacket packetRisposta = new DatagramPacket(
        dati, dati.length,
        packetRicevuto.getAddress(),
        packetRicevuto.getPort()
    );
    socket.send(packetRisposta);
    
    System.out.println("Inviato: " + risposta);
}
```

### ✅ Test STEP 2:

Esegui il client più volte e osserva il contatore che incrementa.

**Risultato atteso:**
```
Connessione 1: 2025-12-18 15:30:45 [Connessione #1]
Connessione 2: 2025-12-18 15:31:10 [Connessione #2]
Connessione 3: 2025-12-18 15:31:25 [Connessione #3]
```

⚠️ **Nota:** Se riavvii il server, il conteggio riparte da zero (non c'è persistenza).

> 📌 **CONSEGNA CODICE:**  
> Carica il file `DaytimeServerV2.java` aggiornato su GitHub e inserisci il **link del repository** nella relazione.

> 📸 **DOCUMENTAZIONE TEST:**  
> Allega screenshot che mostrano:
> - Output del server con il tracking delle richieste (#1, #2, #3)  
> 
> Nomina il file: `step2_server_tracking.png`

---

## STEP 3: Implementare Logica Bonus {#step-3-logica-bonus}

**🎯 Obiettivo:** Prime 9 richieste gratuite, poi avviso e blocco.

### � Analisi dei Requisiti:

**Requisiti Funzionali:**
- RF1: Il server deve concedere 9 richieste gratuite per ogni client
- RF2: Alla 10ª richiesta, inviare un messaggio di avviso
- RF3: Dalla 11ª richiesta in poi, bloccare il servizio
- RF4: Le risposte devono differenziarsi in base al contatore:
  - Connessioni 1-9: "data/ora [#N/9]"
  - Connessione 10: "Hai esaurito i bonus gratuiti! Il servizio è ora a pagamento."
  - Connessioni 11+: "Servizio a pagamento. Invia una API_KEY valida."

**Requisiti Non Funzionali:**
- RNF1: Separazione della logica di elaborazione in metodo dedicato
- RNF2: Codice facilmente modificabile (MAX_BONUS come costante)
- RNF3: Messaggi chiari e informativi per l'utente

**Regole di Business:**
- MAX_BONUS = 9 (configurabile)
- Soglia avviso: count == MAX_BONUS + 1
- Soglia blocco: count > MAX_BONUS + 1
- Il contatore non viene mai azzerato (in questo step)

**Casi d'Uso:**
1. **Utente normale (1-9 richieste):** Riceve servizio gratuito con contatore visibile
2. **Utente al limite (10ª richiesta):** Riceve avviso cortese
3. **Utente oltre il limite (11+):** Servizio bloccato, richiesta API_KEY

### �📋 Cosa aggiungiamo:
- Costante MAX_BONUS = 9
- Logica condizionale per la risposta basata sul contatore

### 🔧 Modifiche al Server (DaytimeServerV3.java)

**Aggiungi costante:**
```java
// Numero massimo di richieste gratuite per ogni client
private static final int MAX_BONUS = 9;
```

**Crea metodo per elaborare la risposta:**
```java
/**
 * Elabora la risposta in base al numero di connessioni del client.
 * 
 * @param count Numero di connessioni effettuate da questo client
 * @return Stringa da inviare come risposta
 */
private static String elaboraRisposta(int count) {
    // Ottieni data/ora corrente
    String dataOra = LocalDateTime.now().format(FORMATTER);
    
    // ═════════════════════════════════════════════
    // LOGICA CONDIZIONALE BASATA SUL CONTATORE
    // ═════════════════════════════════════════════
    
    if (count <= MAX_BONUS) {
        // CASO 1: Ancora bonus disponibili (1-9)
        // Invia data/ora con indicazione progresso bonus
        return dataOra + " [Connessione #" + count + "/" + MAX_BONUS + "]";
        
    } else if (count == MAX_BONUS + 1) {
        // CASO 2: Esattamente la 10ª connessione
        // Prima volta che supera il limite: invia avviso
        return "Hai esaurito i bonus gratuiti! Il servizio è ora a pagamento.";
        
    } else {
        // CASO 3: Dalla 11ª connessione in poi
        // Servizio bloccato: richiede API_KEY
        return "Servizio a pagamento. Invia una API_KEY valida.";
    }
}
```

**Modifica il loop per usare il nuovo metodo:**
```java
// ... dopo aver aggiornato il contatore ...

String risposta = elaboraRisposta(count);
byte[] dati = risposta.getBytes();

// ... invio risposta ...
```

### ✅ Test STEP 3:

Esegui il client 12 volte consecutivamente e osserva:

```
Connessione 1-9:  Data/ora normale [#1/9] ... [#9/9]
Connessione 10:   "Hai esaurito i bonus..."
Connessione 11+:  "Servizio a pagamento..."
```

> 📌 **CONSEGNA CODICE:**  
> Carica il file `DaytimeServerV3.java` con la logica bonus su GitHub e inserisci il **link del repository** nella relazione.

> 📸 **DOCUMENTAZIONE TEST:**  
> Allega screenshot che mostrano:
> - Connessioni dalla #1 alla #9 (richieste gratuite)
> - Connessione #10 (messaggio di avviso)
> - Connessioni #11+ (servizio bloccato)


---

## STEP 4: Aggiungere Persistenza su File {#step-4-persistenza}

**🎯 Obiettivo:** Salvare e caricare i dati su file per sopravvivere ai riavvii.

### � Analisi dei Requisiti:

**Requisiti Funzionali:**
- RF1: Il server deve salvare la mappa delle connessioni su file
- RF2: Il server deve caricare i dati esistenti all'avvio
- RF3: I contatori devono persistere anche dopo riavvio del server
- RF4: Il salvataggio deve avvenire dopo ogni aggiornamento del contatore
- RF5: Se il file non esiste, iniziare con mappa vuota
- RF6: Se il file è corrotto, iniziare con mappa vuota (con log errore)

**Requisiti Non Funzionali:**
- RNF1: Utilizzo di serializzazione Java (ObjectOutputStream/ObjectInputStream)
- RNF2: Gestione robusta degli errori I/O
- RNF3: Il server non deve bloccarsi se il salvataggio fallisce
- RNF4: Formato file binario (non human-readable)
- RNF5: Performance: salvataggio dopo ogni richiesta (file piccolo)

**Meccanismo di Serializzazione:**
- File: `client_connections.dat` (binario)
- Formato: Oggetto HashMap serializzato
- Operazioni:
  1. **Caricamento:** All'avvio del server (main)
  2. **Salvataggio:** Dopo ogni aggiornamento contatore (loop)
  3. **Verifica:** Controllo esistenza file prima del caricamento

**Gestione Errori:**
- File non esistente → Nuova mappa (prima esecuzione)
- File corrotto → Nuova mappa + log errore
- Errore salvataggio → Log errore ma continua esecuzione
- ClassNotFoundException → File incompatibile, nuova mappa

**Vantaggi:**
- ✅ Dati permanenti tra sessioni
- ✅ Tracking continuativo dei client
- ✅ Nessuna perdita informazioni al crash del server

**Svantaggi:**
- ⚠️ File binario non leggibile da umani
- ⚠️ Salvataggio frequente (overhead I/O minimo per file piccolo)
- ⚠️ Possibile corruzione file in caso di crash durante scrittura

### �📋 Cosa aggiungiamo:
- Metodo `salvaConnessioni()` con ObjectOutputStream
- Metodo `caricaConnessioni()` con ObjectInputStream
- Chiamate ai metodi di salvataggio/caricamento

### 🔧 Modifiche al Server (DaytimeServerV4.java)

**Aggiungi costante:**
```java
private static final String FILE_CONNESSIONI = "client_connections.dat";
```

**Aggiungi metodo di salvataggio:**
```java
/**
 * Salva la mappa delle connessioni su file usando serializzazione.
 * Questo permette ai dati di sopravvivere al riavvio del server.
 * 
 * @param connections Mappa da salvare
 */
private static void salvaConnessioni(Map<String, Integer> connections) {
    // try-with-resources: chiude automaticamente lo stream
    try (ObjectOutputStream oos = new ObjectOutputStream(
            new FileOutputStream(FILE_CONNESSIONI))) {
        
        // Serializza l'intera mappa su file
        oos.writeObject(connections);
        
    } catch (IOException e) {
        // In caso di errore, log ma non bloccare il server
        System.err.println("Errore salvataggio: " + e.getMessage());
    }
}
```

**Aggiungi metodo di caricamento:**
```java
/**
 * Carica la mappa delle connessioni dal file.
 * Se il file non esiste o è corrotto, ritorna una mappa vuota.
 * 
 * @return Mappa caricata dal file, o vuota se errore
 */
@SuppressWarnings("unchecked")  // Necessario per il cast della mappa
private static Map<String, Integer> caricaConnessioni() {
    // Crea riferimento al file
    File file = new File(FILE_CONNESSIONI);
    
    // Controlla se il file esiste
    if (!file.exists()) {
        System.out.println("Nessun file esistente, nuovo tracking");
        return new HashMap<>();  // Prima esecuzione: mappa vuota
    }
    
    // Tenta di caricare il file
    try (ObjectInputStream ois = new ObjectInputStream(
            new FileInputStream(file))) {
        
        // Deserializza l'oggetto e fai il cast a Map<String, Integer>
        return (Map<String, Integer>) ois.readObject();
        
    } catch (IOException | ClassNotFoundException e) {
        // Se il file è corrotto o la classe non è trovata
        System.err.println("Errore caricamento: " + e.getMessage());
        return new HashMap<>();  // Riparti con mappa vuota
    }
}
```

**Modifica il main:**
```java
public static void main(String[] args) {
    System.out.println("=== DAYTIME SERVER V4.0 ===");
    
    // NUOVO: Carica dati esistenti
    Map<String, Integer> clientConnections = caricaConnessioni();
    System.out.println("Caricati " + clientConnections.size() + " client");
    System.out.println("Server avviato sulla porta " + PORTA);
    
    try (DatagramSocket socket = new DatagramSocket(PORTA)) {
        while (true) {
            // ... gestione richiesta ...
            
            // NUOVO: Salva dopo ogni aggiornamento
            salvaConnessioni(clientConnections);
        }
    } catch (Exception e) {
        System.err.println("Errore: " + e.getMessage());
    }
}
```

### ✅ Test STEP 4:

1. Esegui il client 5 volte → connessioni #1-5
2. **Arresta il server** (Ctrl+C)
3. **Riavvia il server**
4. Esegui il client → dovrebbe essere connessione #6!
5. Verifica che esista il file `client_connections.dat`

**Risultato atteso:** I dati sopravvivono al riavvio del server.

> 📌 **CONSEGNA CODICE:**  
> Carica il file `DaytimeServerV4.java` con le funzioni di persistenza su GitHub e inserisci il **link del repository** nella relazione.

> 📸 **DOCUMENTAZIONE TEST:**  
> Allega screenshot che mostrano:
> - Server prima dell'arresto (connessioni #1-5)
> - Server dopo il riavvio (connessioni ripartono da #6)
> - File `client_connections.dat` nel filesystem (usa `ls -l` o esplora file)


---

## STEP 5: Implementare API_KEY {#step-5-api-key}

**🎯 Obiettivo:** Permettere ai client di sbloccare il servizio con una chiave valida.

### � Analisi dei Requisiti:

**Requisiti Funzionali:**
- RF1: Il client deve poter inviare una API_KEY per sbloccare il servizio
- RF2: Il server deve riconoscere messaggi nel formato "API_KEY:xxx"
- RF3: Il server deve validare la chiave contro un set di chiavi valide
- RF4: API_KEY valida → reset del contatore a 0 + invio data/ora
- RF5: API_KEY non valida → messaggio di errore
- RF6: Il client deve offrire un menu interattivo:
  - Opzione 1: Richiesta data/ora standard
  - Opzione 2: Richiesta con API_KEY
  - Opzione 3: Uscita

**Requisiti Non Funzionali:**
- RNF1: API_KEY conservate in Set immutabile (Set.of())
- RNF2: Parsing robusto del messaggio (trim degli spazi)
- RNF3: Distinzione chiara tra autenticazione e richiesta standard
- RNF4: Interfaccia utente semplice con Scanner

**Protocollo di Comunicazione:**
- **Richiesta standard:** Stringa vuota ""
- **Richiesta con API_KEY:** "API_KEY:chiave_segreta"
- **Risposta normale:** "2025-12-18 15:30:45 [#3/9]"
- **Risposta autenticata:** "2025-12-18 15:30:45" (senza contatore)
- **Risposta errore:** "ERRORE: API_KEY non valida"

**Logica di Business:**
- Set di chiavi valide: {"abc123xyz", "key456def", "test123"}
- Reset contatore: connections.put(clientIP, 0)
- Validazione: API_KEYS_VALIDE.contains(apiKey)
- Effetto: Il client può ripartire con 9 nuovi bonus

**Casi d'Uso:**
1. **Client normale:** Esaurisce i 9 bonus, riceve blocco
2. **Client con API_KEY valida:** Sblocca servizio, contatore a 0
3. **Client con API_KEY invalida:** Riceve errore, contatore non cambia
4. **Client riabilitato:** Può fare altre 9 richieste gratuite

**Considerazioni di Sicurezza (per produzione):**
- ⚠️ Le API_KEY in codice sono solo per test
- ✅ In produzione: database con hash+salt (bcrypt)
- ✅ Trasmissione: DTLS (UDP over TLS)
- ✅ Rate limiting: evitare brute force
- ✅ Logging: audit trail delle autenticazioni

### �📋 Cosa aggiungiamo:
- Set di API_KEY valide
- Parsing del messaggio per rilevare "API_KEY:xxx"
- Validazione e reset contatore

### 🔧 Modifiche al Server (DaytimeServerV5.java)

**Aggiungi costante:**
```java
// Set immutabile di API_KEY valide
// In produzione andrebbero salvate in database cifrate (hash + salt)
private static final Set<String> API_KEYS_VALIDE = Set.of(
    "abc123xyz",  // Chiave di test 1
    "key456def",  // Chiave di test 2
    "test123"     // Chiave di test 3
);
```

**Modifica `elaboraRisposta` per accettare il messaggio:**
```java
/**
 * Elabora la risposta in base al messaggio ricevuto e allo stato del client.
 * Gestisce sia richieste standard che autenticazione con API_KEY.
 * 
 * @param messaggio Messaggio ricevuto dal client
 * @param clientIP IP del client (per reset contatore)
 * @param count Numero di connessioni effettuate
 * @param connections Mappa delle connessioni (per modificare il contatore)
 * @return Stringa da inviare come risposta
 */
private static String elaboraRisposta(
        String messaggio, 
        String clientIP, 
        int count,
        Map<String, Integer> connections) {
    
    // ═════════════════════════════════════════════
    // CASO SPECIALE: MESSAGGIO CON API_KEY
    // ═════════════════════════════════════════════
    
    // Controlla se il messaggio inizia con "API_KEY:"
    if (messaggio.startsWith("API_KEY:")) {
        // Estrae la chiave (dopo i primi 8 caratteri)
        String apiKey = messaggio.substring(8).trim();
        
        // Verifica se la chiave è nel set delle chiavi valide
        if (API_KEYS_VALIDE.contains(apiKey)) {
            // ✅ API_KEY VALIDA
            
            // Reset del contatore: il cliente può ricominciare
            connections.put(clientIP, 0);
            
            System.out.println("  ✅ API_KEY valida - Contatore resettato");
            
            // Invia data/ora come ricompensa
            return LocalDateTime.now().format(FORMATTER);
            
        } else {
            // ❌ API_KEY NON VALIDA
            System.out.println("  ❌ API_KEY invalida");
            return "ERRORE: API_KEY non valida";
        }
    }
    
    // ═════════════════════════════════════════════
    // RICHIESTA STANDARD: LOGICA BONUS
    // ═════════════════════════════════════════════
    
    String dataOra = LocalDateTime.now().format(FORMATTER);
    
    if (count <= MAX_BONUS) {
        // Ancora bonus disponibili
        return dataOra + " [#" + count + "/" + MAX_BONUS + "]";
        
    } else if (count == MAX_BONUS + 1) {
        // Prima volta oltre il limite: avviso
        return "Hai esaurito i bonus gratuiti! Il servizio è ora a pagamento.";
        
    } else {
        // Oltre il limite: blocco
        return "Servizio a pagamento. Invia una API_KEY valida.";
    }
}
```

**Modifica il loop per estrarre il messaggio:**
```java
// ... ricezione pacchetto ...

// Estrai IP del client
String clientIP = packetRicevuto.getAddress().getHostAddress();

// ═════════════════════════════════════════════
// NUOVO: ESTRAZIONE MESSAGGIO DAL PACCHETTO
// ═════════════════════════════════════════════

// Converti i byte ricevuti in stringa
// getLength() ritorna solo i byte effettivi (non tutto il buffer)
String messaggio = new String(
    packetRicevuto.getData(),      // Array di byte
    0,                              // Offset iniziale
    packetRicevuto.getLength()      // Numero di byte effettivi
).trim();  // Rimuove spazi vuoti

// ... aggiornamento contatore ...

// ═════════════════════════════════════════════
// CHIAMATA AL METODO CON PARAMETRI AGGIUNTIVI
// ═════════════════════════════════════════════

// Passa anche messaggio, clientIP e la mappa per gestire API_KEY
String risposta = elaboraRisposta(messaggio, clientIP, count, clientConnections);

// ... invio risposta e salvataggio ...
```

### 🔧 Modifiche al Client (DaytimeClientV5.java)

**Aggiungi menu interattivo nel main:**
```java
public static void main(String[] args) {
    System.out.println("=== DAYTIME CLIENT V5.0 ===");
    
    try (DatagramSocket socket = new DatagramSocket();
         Scanner scanner = new Scanner(System.in)) {
        
        socket.setSoTimeout(TIMEOUT);
        InetAddress serverAddress = InetAddress.getByName(HOST);
        
        boolean continua = true;
        while (continua) {
            System.out.println("\n=== MENU ===");
            System.out.println("1. Richiedi data/ora");
            System.out.println("2. Richiedi con API_KEY");
            System.out.println("3. Esci");
            System.out.print("Scelta > ");
            
            String scelta = scanner.nextLine().trim();
            
            switch (scelta) {
                case "1":
                    inviaRichiesta(socket, serverAddress, "");
                    break;
                case "2":
                    System.out.print("Inserisci API_KEY: ");
                    String apiKey = scanner.nextLine().trim();
                    inviaRichiesta(socket, serverAddress, "API_KEY:" + apiKey);
                    break;
                case "3":
                    System.out.println("Arrivederci!");
                    continua = false;
                    break;
                default:
                    System.out.println("Scelta non valida");
            }
        }
    } catch (Exception e) {
        System.err.println("Errore: " + e.getMessage());
    }
}

private static void inviaRichiesta(
        DatagramSocket socket, 
        InetAddress serverAddress, 
        String messaggio) {
    try {
        // Invia
        byte[] dati = messaggio.getBytes();
        DatagramPacket packetInvio = new DatagramPacket(
            dati, dati.length, serverAddress, PORTA
        );
        socket.send(packetInvio);
        
        // Ricevi
        byte[] buffer = new byte[1024];
        DatagramPacket packetRisposta = new DatagramPacket(buffer, buffer.length);
        socket.receive(packetRisposta);
        
        String risposta = new String(
            packetRisposta.getData(), 0, packetRisposta.getLength()
        );
        
        System.out.println("\n>>> SERVER: " + risposta + "\n");
        
    } catch (SocketTimeoutException e) {
        System.err.println("TIMEOUT: Server non risponde");
    } catch (IOException e) {
        System.err.println("Errore: " + e.getMessage());
    }
}
```

### ✅ Test STEP 5:

**Scenario di test completo:**

1. Esegui client 10 volte con opzione 1 (standard)
   - Connessioni 1-9: ricevi data/ora
   - Connessione 10: ricevi avviso bonus esauriti

2. Connessione 11 con opzione 1
   - Ricevi: "Servizio a pagamento..."

3. Connessione 12 con opzione 2 e API_KEY "abc123xyz"
   - Ricevi: data/ora (servizio ripristinato!)

4. Connessione 13 con opzione 2 e API_KEY "wrongkey"
   - Ricevi: "ERRORE: API_KEY non valida"

> 📌 **CONSEGNA CODICE:**  
> Carica i file `DaytimeServerV5.java` e `DaytimeClientV5.java` con gestione API_KEY su GitHub e inserisci il **link del repository** nella relazione.

> 📸 **DOCUMENTAZIONE TEST:**  
> Allega screenshot che mostrano:
> - Client bloccato dopo 10 richieste
> - Menu del client con opzione API_KEY
> - Sblocco riuscito con API_KEY valida
> - Contatore resettato (nuove richieste gratuite)
> - Tentativo con API_KEY non valida (messaggio di errore)
`

---

## STEP 6: Miglioramenti Finali {#step-6-miglioramenti}

**🎯 Obiettivo:** Rendere il codice più robusto e professionale.

### � Analisi dei Requisiti:

**Requisiti di Qualità del Codice:**
- RQ1: Il codice deve essere ben documentato (Javadoc)
- RQ2: Le eccezioni devono essere gestite in modo specifico
- RQ3: I log devono essere informativi e visualmente chiari
- RQ4: L'interfaccia utente deve essere user-friendly
- RQ5: Il codice deve seguire best practices Java

**Requisiti Non Funzionali:**
- RNF1: **Manutenibilità:** Codice facile da modificare e estendere
- RNF2: **Leggibilità:** Nomi variabili descrittivi, commenti chiari
- RNF3: **Robustezza:** Gestione errori completa
- RNF4: **User Experience:** Messaggi chiari con emoji/simboli
- RNF5: **Professionalità:** Output formattato, aspetto curato

**Aree di Miglioramento:**

**1. Logging e Output:**
- Utilizzo emoji per categorizzare i messaggi:
  - 📥 Ricezione dati
  - 📤 Invio dati
  - ✅ Operazione riuscita
  - ❌ Errore
  - ⚠️ Avviso
  - 💡 Suggerimento
- Intestazioni grafiche con caratteri box-drawing
- Timestamp nei log
- Livelli di verbosità configurabili

**2. Gestione Eccezioni:**
- Catch specifici invece di Exception generica
- Messaggi di errore con suggerimenti pratici
- Logging strutturato degli errori
- Graceful degradation (continua anche con errori)

**3. Validazione Input:**
- Controllo API_KEY vuota
- Validazione scelte menu
- Gestione input non validi
- Feedback immediato all'utente

**4. Documentazione:**
- Javadoc completo per tutti i metodi pubblici
- Commenti inline per logica complessa
- README con esempi di utilizzo
- Diagrammi di sequenza

**5. Configurazione:**
- Costanti ben nominate e raggruppate
- Possibilità di configurazione esterna
- Valori di default sensati
- Commenti esplicativi per ogni costante

**Best Practices Applicate:**
- ✅ Try-with-resources per chiusura automatica
- ✅ Immutabilità dove possibile (Set.of, final)
- ✅ Separazione delle responsabilità (metodi dedicati)
- ✅ DRY (Don't Repeat Yourself)
- ✅ Single Responsibility Principle
- ✅ Defensive programming

### �📋 Migliorie da applicare:

#### 1. **Log più dettagliati con emoji/simboli:**

```java
System.out.println("📥 Richiesta da " + clientIP + " (connessione #" + count + ")");
System.out.println("📤 Risposta: " + risposta);
```

#### 2. **Intestazioni grafiche:**

```java
System.out.println("╔════════════════════════════════════════╗");
System.out.println("║   DAYTIME SERVER UDP - TRACKING MODE   ║");
System.out.println("╚════════════════════════════════════════╝");
```

#### 3. **Gestione eccezioni più specifica:**

```java
} catch (SocketException e) {
    System.err.println("❌ Errore socket: " + e.getMessage());
    System.err.println("💡 Suggerimento: Se usi porta < 1024, esegui con sudo");
}
```

#### 4. **Validazione input nel client:**

```java
if (apiKey.isEmpty()) {
    System.out.println("⚠️  API_KEY vuota, riprova");
    continue;
}
```

#### 5. **Commenti Javadoc:**

```java
/**
 * Elabora la risposta in base al messaggio ricevuto e allo stato del client.
 * 
 * @param messaggio Messaggio ricevuto dal client
 * @param clientIP IP del client
 * @param count Numero di connessioni del client
 * @param connections Mappa delle connessioni
 * @return Stringa da inviare come risposta
 */
private static String elaboraRisposta(...) {
    // ...
}
```

#### 6. **Separazione delle responsabilità:**

```java
// Metodo dedicato per ottenere data/ora
private static String getDataOraCorrente() {
    return LocalDateTime.now().format(FORMATTER);
}

// Metodo dedicato per stampare il menu
private static void stampaMenu() {
    System.out.println("═══════════════════════════════════════════");
    System.out.println("              MENU PRINCIPALE              ");
    // ...
}
```

> 📌 **CONSEGNA FINALE:**  
> Al completamento dello STEP 6, carica la versione finale (server e client) su GitHub e inserisci il **link del repository** nella relazione.  
> Assicurati che il README del repository contenga:
> - Istruzioni di compilazione ed esecuzione
> - Esempi di utilizzo
> - Elenco delle funzionalità implementate

> 📸 **DOCUMENTAZIONE COMPLETA:**  
> Allega nella relazione una cartella `screenshots/` contenente tutti gli screenshot dei vari step, organizzati con nomi descrittivi.

---

## 📊 Riepilogo della Progressione

| Step | Funzionalità | Complessità | File da creare |
|------|--------------|-------------|----------------|
| 1 | Server/Client base | ⭐ | ServerV1, ClientV1 |
| 2 | Tracking in memoria | ⭐⭐ | ServerV2 |
| 3 | Logica bonus | ⭐⭐ | ServerV3 |
| 4 | Persistenza file | ⭐⭐⭐ | ServerV4 |
| 5 | API_KEY | ⭐⭐⭐ | ServerV5, ClientV5 |
| 6 | Miglioramenti | ⭐⭐ | Versione finale |

---

## 💡 Suggerimenti per lo Sviluppo

### 🔄 Approccio Iterativo:

1. **Implementa un passo alla volta**
2. **Testa dopo ogni modifica**
3. **Non passare allo step successivo finché quello corrente non funziona**
4. **Tieni una copia di backup dopo ogni step funzionante**

### 🐛 Debug Tips:

- Usa `System.out.println()` abbondantemente per capire il flusso
- Stampa sempre i valori delle variabili chiave (IP, count, messaggio)
- Testa con più client contemporaneamente
- Prova a riavviare il server per verificare la persistenza
- Usa `netstat` o `ss` per verificare che la porta sia in uso

### 📝 Comandi Utili:

```bash
# Compila e esegui in un colpo solo
javac DaytimeServerV1.java && java DaytimeServerV1

# Verifica porta in uso
netstat -an | grep 1313

# Verifica file creato
ls -lh client_connections.dat

# Test rapido con netcat (Linux/Mac)
echo "" | nc -u localhost 1313
```

---

## 🎯 Checklist Finale

Prima di considerare il progetto completo, verifica:

- [ ] Server si avvia senza errori
- [ ] Client si connette correttamente
- [ ] Conteggio connessioni funziona
- [ ] Bonus system: 9 gratis → avviso → blocco
- [ ] File di persistenza viene creato e letto correttamente
- [ ] Dati sopravvivono al riavvio del server
- [ ] API_KEY valida sblocca il servizio
- [ ] API_KEY invalida produce errore
- [ ] Contatore viene resettato dopo API_KEY valida
- [ ] Gestione timeout client funziona
- [ ] Log server sono chiari e informativi
- [ ] Codice è ben commentato
- [ ] Nessuna eccezione non gestita

---

## 🚀 Prossimi Passi (Estensioni Opzionali)

Dopo aver completato tutti gli step, puoi provare:

1. **Multi-threading:** Gestire client concorrenti
2. **Database:** Usare SQLite invece di file serializzato
3. **Crittografia:** Cifrare le API_KEY
4. **Statistiche:** Ultima connessione, tempo medio tra richieste
5. **GUI:** Interfaccia grafica con Swing/JavaFX
6. **Rate Limiting:** Limitare richieste per IP per secondo
7. **Logging su file:** Log strutturati con timestamp

---

## 📚 Risorse Aggiuntive

- [Java Networking Tutorial](https://docs.oracle.com/javase/tutorial/networking/)
- [DatagramSocket API](https://docs.oracle.com/javase/8/docs/api/java/net/DatagramSocket.html)
- [RFC 867 - Daytime Protocol](https://tools.ietf.org/html/rfc867)

---

**Buon lavoro! 🎉**

*Ricorda: La programmazione è un processo iterativo. Non scoraggiarti se qualcosa non funziona al primo tentativo - ogni errore è un'opportunità di apprendimento!*
