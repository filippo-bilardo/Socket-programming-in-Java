# 🎓 ESERCITAZIONE: Socket TCP "Sistema Dispenser Numeri"

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

- ✅ **Implementare** un server TCP che gestisce una risorsa condivisa (contatore)
- ✅ **Creare** un sistema di code con numeri progressivi
- ✅ **Gestire** richieste multiple da client diversi
- ✅ **Applicare** sincronizzazione per accesso a variabili condivise
- ✅ **Implementare** notifiche di stato ai client
- ✅ **Gestire** comunicazione bidirezionale client-server
- ✅ **Documentare** il funzionamento con screenshot

---

## 📚 **PREREQUISITI**

### Conoscenze Teoriche Richieste:
- 🔌 **Socket TCP/IP:** Concetti base di client-server
- ☕ **Java I/O:** `BufferedReader`, `PrintWriter`, `Socket`, `ServerSocket`
- 🔄 **Sincronizzazione:** Gestione variabili condivise
- 📝 **String manipulation:** Parsing e formattazione

### Strumenti Necessari:
- ☕ **Java JDK** 11 o superiore
- 💻 **IDE** o editor di testo (VS Code, IntelliJ, Eclipse)
- 🖥️ **Terminale** per compilazione ed esecuzione
- 📸 **Software screenshot** per documentazione

---

## 📖 **TRACCIA DELL'ESERCIZIO**

### 🚀 **Applicazione: "Dispenser Numeri per Code"**

Simulare il sistema di gestione code presente in banche, poste, panetterie, ecc.

1. **Il SERVER (DispenserServer):**
   - Ascolta sulla porta `7777`
   - Mantiene un **contatore progressivo** globale (inizia da 1)
   - Quando un client si connette e richiede "PRENOTA", assegna il prossimo numero disponibile
   - Risponde al client con: `"NUMERO: XX"` dove XX è il numero assegnato
   - Tiene traccia del **numero corrente in servizio**
   - Gestisce comandi da "Negozio" per aggiornare il numero servito
   - Visualizza statistiche: numeri distribuiti, numero in servizio
   - Supporta connessioni multiple simultanee

2. **Il CLIENT (DispenserClient):**
   - Si connette al server localhost:7777
   - Invia richiesta "PRENOTA"
   - Riceve e visualizza il numero assegnato
   - Può richiedere "STATO" per vedere quale numero è attualmente servito
   - Si disconnette dopo aver ricevuto il numero

3. **OPZIONALE - Il NEGOZIO (NegozioClient):**
   - Applicazione separata che simula il punto vendita/sportello
   - Si connette al server come client speciale
   - Invia comando "SERVI" per indicare che ha completato il servizio
   - Il server incrementa il contatore "numero in servizio"
   - Il server notifica tutti i client in attesa del nuovo stato

### 📐 **Esempi di Interazione:**

```
=== SERVER ===
Server Dispenser avviato sulla porta 7777
In attesa di connessioni...

[CONNESSIONE] Client da 127.0.0.1
[PRENOTA] Assegnato numero: 1
[DISCONNESSIONE] Client disconnesso

[CONNESSIONE] Client da 127.0.0.1
[PRENOTA] Assegnato numero: 2
[DISCONNESSIONE] Client disconnesso

[CONNESSIONE] Negozio da 127.0.0.1
[SERVI] Numero servito aggiornato a: 1

Statistiche:
- Numeri distribuiti: 2
- Numero in servizio: 1
- Persone in attesa: 1

=== CLIENT 1 ===
Connessione al server riuscita!
Richiesta numero in corso...

Il tuo numero è: 1

Attualmente in servizio: 0
Persone davanti a te: 1

Grazie per aver utilizzato il servizio!

=== CLIENT 2 ===
Connessione al server riuscita!
Richiesta numero in corso...

Il tuo numero è: 2

Attualmente in servizio: 1
Persone davanti a te: 1

Grazie per aver utilizzato il servizio!

=== NEGOZIO ===
Connessione al server come Negozio...
Comandi disponibili:
  SERVI - Segna come servito e passa al prossimo
  STATO - Visualizza stato corrente
  QUIT  - Disconnetti

> SERVI
Numero servito: 1
Prossimo numero da servire: 2

> SERVI
Numero servito: 2
Prossimo numero da servire: 3

> STATO
Numero in servizio: 2
Numeri distribuiti: 2
Persone in attesa: 0
```

---

## 🛠️ **PASSAGGI DA SEGUIRE**

### 📝 **STEP 1: Analisi e Progettazione (15-20 minuti)**

#### 1.1 Analizza il Problema
- Identifica gli **attori**:
  - Server (dispenser centrale)
  - Client (clienti che prendono il numero)
  - Negozio (sportello che serve i clienti) [OPZIONALE]
  
- Definisci le **variabili condivise**:
  - `ultimoNumeroAssegnato`: contatore progressivo
  - `numeroInServizio`: numero attualmente servito
  
- Pianifica la **sincronizzazione**:
  - Accesso al contatore deve essere thread-safe
  - Incremento atomico dei contatori

#### 1.2 Schema dell'Architettura
```
┌─────────────────────────────────────┐
│      DispenserServer                │
│  - Porta: 7777                      │
│  - ultimoNumeroAssegnato (sync)     │
│  - numeroInServizio (sync)          │
│  - gestione client multipli         │
└─────────────────────────────────────┘
           ▲         ▲    ▲
           │         │    │
    ┌──────┘    ┌────┘    └────┐
    │           │              │
┌───▼────┐  ┌───▼────┐  ┌──────▼───────┐
│Client 1│  │Client 2│  │NegozioClient │
│Num: 1  │  │Num: 2  │  │(SERVI cmd)   │
└────────┘  └────────┘  └──────────────┘
```

#### 1.3 Protocollo di Comunicazione
```
Client → Server:
- "PRENOTA" - Richiede un nuovo numero
- "STATO"   - Richiede info su numero in servizio

Server → Client:
- "NUMERO: XX" - Numero assegnato
- "SERVIZIO: XX" - Numero attualmente in servizio
- "ATTESA: XX" - Persone in attesa

Negozio → Server:
- "NEGOZIO" - Identifica come negozio
- "SERVI"   - Completa servizio numero corrente
- "STATO"   - Richiede statistiche

Server → Negozio:
- "SERVITO: XX" - Conferma numero servito
- "STATISTICHE: ..." - Info sistema
```

---

### ⚙️ **STEP 2: Implementazione Server (45-60 minuti)**

#### 2.1 Struttura Base del Server
Crea il file `DispenserServer.java`:

```java
import java.io.*;
import java.net.*;

public class DispenserServer {
    private static final int PORTA = 7777;
    
    // Variabili condivise - devono essere sincronizzate
    private static int ultimoNumeroAssegnato = 0;
    private static int numeroInServizio = 0;
    
    public static void main(String[] args) {
        System.out.println("=== SERVER DISPENSER NUMERI ===");
        System.out.println("Server avviato sulla porta " + PORTA);
        System.out.println("In attesa di connessioni...\n");
        
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Gestisci client in thread separato per supporto multi-client
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Errore server: " + e.getMessage());
        }
    }
    
    private static void handleClient(Socket socket) {
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String clientIP = socket.getInetAddress().getHostAddress();
            System.out.println("[CONNESSIONE] Client da " + clientIP);
            
            String richiesta;
            while ((richiesta = in.readLine()) != null) {
                String risposta = processaRichiesta(richiesta);
                out.println(risposta);
                
                // Se è una prenota o un servi, esci dopo la risposta
                if (richiesta.equals("PRENOTA") || richiesta.equals("NEGOZIO")) {
                    break;
                }
            }
            
            System.out.println("[DISCONNESSIONE] Client disconnesso");
            
        } catch (IOException e) {
            System.err.println("Errore gestione client: " + e.getMessage());
        }
    }
    
    private static synchronized String processaRichiesta(String richiesta) {
        switch (richiesta) {
            case "PRENOTA":
                return prenota();
                
            case "STATO":
                return getStato();
                
            case "NEGOZIO":
                return "NEGOZIO_OK";
                
            case "SERVI":
                return serviCliente();
                
            default:
                return "ERRORE: Comando non riconosciuto";
        }
    }
    
    private static String prenota() {
        ultimoNumeroAssegnato++;
        int numeroAssegnato = ultimoNumeroAssegnato;
        
        System.out.println("[PRENOTA] Assegnato numero: " + numeroAssegnato);
        
        int attesa = numeroAssegnato - numeroInServizio;
        
        // Formato: NUMERO:XX|SERVIZIO:YY|ATTESA:ZZ
        return String.format("NUMERO:%d|SERVIZIO:%d|ATTESA:%d", 
                           numeroAssegnato, numeroInServizio, attesa);
    }
    
    private static String getStato() {
        int attesa = ultimoNumeroAssegnato - numeroInServizio;
        return String.format("SERVIZIO:%d|DISTRIBUITI:%d|ATTESA:%d",
                           numeroInServizio, ultimoNumeroAssegnato, attesa);
    }
    
    private static String serviCliente() {
        numeroInServizio++;
        System.out.println("[SERVI] Numero servito aggiornato a: " + numeroInServizio);
        
        int attesa = ultimoNumeroAssegnato - numeroInServizio;
        
        stampaStatistiche();
        
        return String.format("SERVITO:%d|ATTESA:%d", numeroInServizio, attesa);
    }
    
    private static void stampaStatistiche() {
        System.out.println("\n--- STATISTICHE ---");
        System.out.println("Numeri distribuiti: " + ultimoNumeroAssegnato);
        System.out.println("Numero in servizio: " + numeroInServizio);
        System.out.println("Persone in attesa: " + 
                         (ultimoNumeroAssegnato - numeroInServizio));
        System.out.println("-------------------\n");
    }
}
```

#### 2.2 Checklist Server:
- [ ] ServerSocket sulla porta 7777
- [ ] Contatore `ultimoNumeroAssegnato` sincronizzato
- [ ] Contatore `numeroInServizio` sincronizzato
- [ ] Gestione comando "PRENOTA"
- [ ] Gestione comando "STATO"
- [ ] Gestione comando "SERVI" (per negozio)
- [ ] Supporto multi-client con thread
- [ ] Statistiche visualizzate correttamente
- [ ] Chiusura corretta delle risorse

---

### 💻 **STEP 3: Implementazione Client (30-45 minuti)**

#### 3.1 Struttura Base del Client
Crea il file `DispenserClient.java`:

```java
import java.io.*;
import java.net.*;

public class DispenserClient {
    private static final String HOST = "localhost";
    private static final int PORTA = 7777;
    
    public static void main(String[] args) {
        System.out.println("=== CLIENT DISPENSER ===");
        
        try (
            Socket socket = new Socket(HOST, PORTA);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))
        ) {
            System.out.println("Connessione al server riuscita!");
            System.out.println("Richiesta numero in corso...\n");
            
            // Invia richiesta prenota
            out.println("PRENOTA");
            
            // Ricevi risposta
            String risposta = in.readLine();
            
            if (risposta != null) {
                visualizzaRisposta(risposta);
            }
            
            System.out.println("\nGrazie per aver utilizzato il servizio!");
            
        } catch (IOException e) {
            System.err.println("Errore connessione: " + e.getMessage());
        }
    }
    
    private static void visualizzaRisposta(String risposta) {
        // Formato: NUMERO:XX|SERVIZIO:YY|ATTESA:ZZ
        String[] parti = risposta.split("\\|");
        
        for (String parte : parti) {
            String[] keyValue = parte.split(":");
            if (keyValue.length == 2) {
                String chiave = keyValue[0];
                String valore = keyValue[1];
                
                switch (chiave) {
                    case "NUMERO":
                        System.out.println("╔════════════════════════════╗");
                        System.out.println("║   IL TUO NUMERO È: " + 
                                         String.format("%3s", valore) + "   ║");
                        System.out.println("╚════════════════════════════╝");
                        break;
                        
                    case "SERVIZIO":
                        System.out.println("\nAttualmente in servizio: " + valore);
                        break;
                        
                    case "ATTESA":
                        System.out.println("Persone davanti a te: " + valore);
                        break;
                }
            }
        }
    }
}
```

#### 3.2 Checklist Client:
- [ ] Connessione a localhost:7777
- [ ] Invio comando "PRENOTA"
- [ ] Ricezione e parsing risposta
- [ ] Visualizzazione numero assegnato
- [ ] Visualizzazione informazioni attesa
- [ ] Gestione errori di connessione
- [ ] Chiusura corretta delle risorse

---

### 🏪 **STEP 4: Implementazione Negozio [OPZIONALE] (30-45 minuti)**

#### 4.1 Struttura Negozio
Crea il file `NegozioClient.java`:

```java
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class NegozioClient {
    private static final String HOST = "localhost";
    private static final int PORTA = 7777;
    
    public static void main(String[] args) {
        System.out.println("=== NEGOZIO - GESTIONE SERVIZIO ===");
        
        try (
            Socket socket = new Socket(HOST, PORTA);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Connessione al server come Negozio...\n");
            
            // Identifica come negozio
            out.println("NEGOZIO");
            String conferma = in.readLine();
            
            if (!"NEGOZIO_OK".equals(conferma)) {
                System.err.println("Errore autenticazione negozio");
                return;
            }
            
            System.out.println("Autenticazione riuscita!");
            mostraMenu();
            
            // Loop comandi
            while (true) {
                System.out.print("\n> ");
                String comando = scanner.nextLine().trim().toUpperCase();
                
                if (comando.equals("QUIT")) {
                    System.out.println("Disconnessione...");
                    break;
                }
                
                if (comando.equals("SERVI") || comando.equals("STATO")) {
                    out.println(comando);
                    String risposta = in.readLine();
                    
                    if (risposta != null) {
                        visualizzaRisposta(comando, risposta);
                    }
                } else {
                    System.out.println("Comando non riconosciuto. Usa SERVI, STATO o QUIT");
                }
            }
            
        } catch (IOException e) {
            System.err.println("Errore connessione: " + e.getMessage());
        }
    }
    
    private static void mostraMenu() {
        System.out.println("\nComandi disponibili:");
        System.out.println("  SERVI - Segna come servito e passa al prossimo");
        System.out.println("  STATO - Visualizza stato corrente");
        System.out.println("  QUIT  - Disconnetti");
    }
    
    private static void visualizzaRisposta(String comando, String risposta) {
        String[] parti = risposta.split("\\|");
        
        if (comando.equals("SERVI")) {
            System.out.println("\n✓ Cliente servito!");
            for (String parte : parti) {
                String[] kv = parte.split(":");
                if (kv.length == 2) {
                    if (kv[0].equals("SERVITO")) {
                        System.out.println("  Numero servito: " + kv[1]);
                    } else if (kv[0].equals("ATTESA")) {
                        System.out.println("  Persone in attesa: " + kv[1]);
                    }
                }
            }
        } else if (comando.equals("STATO")) {
            System.out.println("\n--- STATO SISTEMA ---");
            for (String parte : parti) {
                String[] kv = parte.split(":");
                if (kv.length == 2) {
                    switch (kv[0]) {
                        case "SERVIZIO":
                            System.out.println("Numero in servizio: " + kv[1]);
                            break;
                        case "DISTRIBUITI":
                            System.out.println("Numeri distribuiti: " + kv[1]);
                            break;
                        case "ATTESA":
                            System.out.println("Persone in attesa: " + kv[1]);
                            break;
                    }
                }
            }
            System.out.println("--------------------");
        }
    }
}
```

#### 4.2 Checklist Negozio:
- [ ] Connessione come client speciale
- [ ] Autenticazione con comando "NEGOZIO"
- [ ] Menu interattivo
- [ ] Comando "SERVI" funzionante
- [ ] Comando "STATO" funzionante
- [ ] Comando "QUIT" per disconnessione
- [ ] Visualizzazione risposte formattata

---

### 🧪 **STEP 5: Testing e Documentazione (30-45 minuti)**

#### 5.1 Test di Compilazione:
```bash
# Compila tutti i file
javac DispenserServer.java DispenserClient.java NegozioClient.java

# Verifica che non ci siano errori
```

#### 5.2 Test Funzionale:
```bash
# Terminale 1: Avvia il server
java DispenserServer

# Terminale 2: Avvia primo client
java DispenserClient

# Terminale 3: Avvia secondo client
java DispenserClient

# Terminale 4: Avvia negozio (OPZIONALE)
java NegozioClient
```

#### 5.3 Casi di Test da Verificare:

| Test | Azione | Risultato Atteso | Screenshot |
|------|--------|------------------|------------|
| **Test 1** | Avvio server | Server in ascolto su porta 7777 | ✅ Screenshot 1 |
| **Test 2** | Client 1 prenota | Riceve numero 1 | ✅ Screenshot 2 |
| **Test 3** | Client 2 prenota | Riceve numero 2 | ✅ Screenshot 3 |
| **Test 4** | Client 3 prenota | Riceve numero 3 | ✅ Screenshot 4 |
| **Test 5** | Negozio SERVI | Numero servizio → 1 | ✅ Screenshot 5 |
| **Test 6** | Negozio SERVI | Numero servizio → 2 | ✅ Screenshot 6 |
| **Test 7** | Negozio STATO | Visualizza statistiche | ✅ Screenshot 7 |
| **Test 8** | 5 client simultanei | Numeri progressivi 1-5 | ✅ Screenshot 8 |

#### 5.4 Creazione Documento Google:

**Titolo:** Esercitazione Socket TCP - Sistema Dispenser Numeri

**Contenuto richiesto:**
1. **Copertina**
   - Titolo esercitazione
   - Nome e Cognome studente
   - Classe e Data
   
2. **Introduzione**
   - Breve descrizione del sistema implementato
   - Tecnologie utilizzate (Java, Socket TCP)
   
3. **Architettura**
   - Schema client-server
   - Descrizione del protocollo di comunicazione
   
4. **Screenshot del Funzionamento**
   - Screenshot 1: Server avviato
   - Screenshot 2-4: Client che ricevono numeri
   - Screenshot 5-7: Negozio che serve clienti (se implementato)
   - Screenshot 8: Test con client multipli
   
5. **Codice Sorgente**
   - Link al repository GitHub/GitLab
   - Oppure link a Google Drive con file .java
   
6. **Problemi Riscontrati**
   - Eventuali difficoltà e soluzioni adottate
   
7. **Conclusioni**
   - Cosa hai imparato
   - Possibili miglioramenti

**Template Link:**
```
Link al codice:
https://github.com/tuousername/SIS3-Socket/tree/main/ES02b-Dispenser

Oppure:
https://drive.google.com/drive/folders/XXXXX
```

---

## 📝 **DELIVERABLE RICHIESTI**

### 📁 File da Consegnare:

1. **Codice Sorgente:**
   - `DispenserServer.java`
   - `DispenserClient.java`
   - `NegozioClient.java` (se implementato)

2. **Documento Google:** (Link da fornire)
   - Screenshot che dimostrano il funzionamento
   - Link al codice sorgente
   - Relazione tecnica

3. **Repository (consigliato):**
   - Cartella `/ES02b-Dispenser/` contenente tutti i file
   - README.md con istruzioni di esecuzione

---

## 🏆 **CRITERI DI VALUTAZIONE**

### 📊 Griglia di Valutazione (Totale: 100 punti)

| Criterio | Punti | Descrizione |
|----------|-------|-------------|
| **Funzionalità Server** | 25 | Server assegna numeri progressivi correttamente |
| **Funzionalità Client** | 20 | Client riceve e visualizza numero |
| **Sincronizzazione** | 15 | Contatori thread-safe, no race condition |
| **Protocollo Comunicazione** | 10 | Formato messaggi corretto |
| **Testing Multi-Client** | 10 | Funziona con client multipli simultanei |
| **Documentazione Screenshot** | 15 | Screenshot completi e descrittivi |
| **Qualità Codice** | 5 | Leggibilità, commenti, struttura |
| **OPZIONALE: Negozio** | +20 | Implementazione NegozioClient funzionante |
| **OPZIONALE: PHP** | +30 | Server PHP + pagina riepilogativa |

### 🎯 Livelli di Competenza:

**Base (obbligatorio):**
- **90-100 punti:** ⭐⭐⭐⭐⭐ **Eccellente** - Tutto funziona perfettamente
- **80-89 punti:** ⭐⭐⭐⭐ **Buono** - Funziona con piccoli difetti
- **70-79 punti:** ⭐⭐⭐ **Sufficiente** - Funzionalità base implementate
- **< 70 punti:** ⭐⭐ **Insufficiente** - Non funzionante o incompleto

**Estensioni opzionali:** +50 punti extra possibili

---

## 💡 **SUGGERIMENTI E TRUCCHI**

### 🔧 **Best Practices:**
- **Usa `synchronized`** sul metodo che incrementa il contatore
- **Thread separati** per gestire client multipli simultanei
- **Formato messaggi consistente** (usa separatori come `|` e `:`)
- **Chiusura risorse** con try-with-resources
- **Logging dettagliato** per debug

### 🚨 **Errori Comuni da Evitare:**
- ❌ Dimenticare `synchronized` sui contatori (race condition!)
- ❌ Non chiudere socket e stream
- ❌ Blocking sul main thread del server
- ❌ Non gestire client multipli simultanei
- ❌ Screenshot poco chiari o illeggibili

### 📸 **Tips per Screenshot:**
```bash
# Linux: usa scrot o gnome-screenshot
gnome-screenshot -w

# Oppure Flameshot (consigliato)
flameshot gui

# Organizza screenshot con nomi descrittivi:
01-server-avvio.png
02-client1-numero1.png
03-client2-numero2.png
04-negozio-servi.png
```

### 🎯 **Estensioni Opzionali Avanzate:**

#### OPZIONALE 1: Implementazione PHP Server

Crea `dispenser_server.php`:
```php
<?php
// Server Socket PHP
$host = "0.0.0.0";
$port = 7777;

$ultimoNumero = 0;
$numeroServizio = 0;

// Crea socket
$socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
socket_bind($socket, $host, $port);
socket_listen($socket);

echo "Server PHP avviato su $host:$port\n";

while (true) {
    $client = socket_accept($socket);
    
    // Leggi comando
    $comando = trim(socket_read($client, 1024));
    
    if ($comando == "PRENOTA") {
        $ultimoNumero++;
        $attesa = $ultimoNumero - $numeroServizio;
        $risposta = "NUMERO:$ultimoNumero|SERVIZIO:$numeroServizio|ATTESA:$attesa";
        socket_write($client, $risposta . "\n");
        echo "Assegnato numero: $ultimoNumero\n";
    }
    
    socket_close($client);
}
?>
```

Crea `riepilogo.php`:
```php
<!DOCTYPE html>
<html>
<head>
    <title>Dispenser - Riepilogo</title>
    <meta charset="utf-8">
    <style>
        body { font-family: Arial; padding: 20px; }
        .numero-servizio { 
            font-size: 72px; 
            color: #007bff; 
            text-align: center; 
            padding: 40px;
            border: 5px solid #007bff;
            border-radius: 10px;
            margin: 20px;
        }
        .stats { 
            display: flex; 
            justify-content: space-around; 
            margin: 20px;
        }
        .stat-box {
            padding: 20px;
            border: 2px solid #28a745;
            border-radius: 5px;
            text-align: center;
        }
    </style>
</head>
<body>
    <h1>🏪 Sistema Dispenser - Pannello di Controllo</h1>
    
    <div class="numero-servizio">
        <div>ORA IN SERVIZIO:</div>
        <div id="numeroServizio">--</div>
    </div>
    
    <div class="stats">
        <div class="stat-box">
            <h3>Numeri Distribuiti</h3>
            <h2 id="distribuiti">0</h2>
        </div>
        <div class="stat-box">
            <h3>In Attesa</h3>
            <h2 id="attesa">0</h2>
        </div>
    </div>
    
    <script>
        // Aggiorna ogni 2 secondi
        setInterval(async () => {
            const response = await fetch('api_stato.php');
            const data = await response.json();
            
            document.getElementById('numeroServizio').textContent = data.servizio;
            document.getElementById('distribuiti').textContent = data.distribuiti;
            document.getElementById('attesa').textContent = data.attesa;
        }, 2000);
    </script>
</body>
</html>
```

**Punti extra:** +30 per implementazione PHP completa e funzionante

---

### 🌐 **STEP 5: Implementazione Negozio in PHP [OPZIONALE AVANZATO] (45-60 minuti)**

#### 5.1 Perché PHP per il Negozio?

PHP offre vantaggi interessanti per questa implementazione:
- 🌐 **Interfaccia Web**: Crea un pannello di controllo accessibile da browser
- 🔌 **Socket nativi**: PHP supporta socket TCP out-of-the-box
- 📊 **Dashboard in tempo reale**: Visualizzazione grafica dello stato del sistema
- 🚀 **Deploy facile**: Basta un web server (Apache, Nginx, PHP built-in)

#### 5.2 Architettura PHP-Java

```
┌─────────────────────────────────────────┐
│    Browser (Cliente Negozio)            │
│    http://localhost:8000/negozio.php    │
└────────────────┬────────────────────────┘
                 │ HTTP
                 ▼
┌─────────────────────────────────────────┐
│    PHP Web Server (porta 8000)          │
│    - negozio.php (interfaccia)          │
│    - negozio_socket.php (logica socket) │
│    - api_stato.php (API REST)           │
└────────────────┬────────────────────────┘
                 │ TCP Socket
                 ▼
┌─────────────────────────────────────────┐
│    DispenserServer.java (porta 7777)    │
│    - Gestisce numeri                    │
│    - Risponde a comandi NEGOZIO         │
└─────────────────────────────────────────┘
```

#### 5.3 Implementazione Completa

**File 1: `negozio_socket.php`** - Classe per comunicazione socket

```php
<?php
/**
 * Classe per comunicazione con il server Java via socket TCP
 */
class DispenserSocketClient {
    private $host;
    private $port;
    private $socket;
    
    public function __construct($host = 'localhost', $port = 7777) {
        $this->host = $host;
        $this->port = $port;
    }
    
    /**
     * Connessione al server
     */
    public function connect() {
        $this->socket = @socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
        
        if ($this->socket === false) {
            throw new Exception("Errore creazione socket: " . socket_strerror(socket_last_error()));
        }
        
        $result = @socket_connect($this->socket, $this->host, $this->port);
        
        if ($result === false) {
            throw new Exception("Errore connessione al server: " . socket_strerror(socket_last_error($this->socket)));
        }
        
        return true;
    }
    
    /**
     * Invia comando al server
     */
    private function sendCommand($command) {
        $command = trim($command) . "\n";
        socket_write($this->socket, $command, strlen($command));
    }
    
    /**
     * Riceve risposta dal server
     */
    private function receiveResponse() {
        $response = '';
        while ($buffer = socket_read($this->socket, 1024, PHP_NORMAL_READ)) {
            $response .= $buffer;
            if (strpos($buffer, "\n") !== false) {
                break;
            }
        }
        return trim($response);
    }
    
    /**
     * Autentica come negozio
     */
    public function autenticaNegozio() {
        $this->sendCommand("NEGOZIO");
        $response = $this->receiveResponse();
        
        if ($response !== "NEGOZIO_OK") {
            throw new Exception("Autenticazione negozio fallita: " . $response);
        }
        
        return true;
    }
    
    /**
     * Comando SERVI - Segna cliente come servito
     */
    public function serviCliente() {
        $this->sendCommand("SERVI");
        $response = $this->receiveResponse();
        
        // Formato risposta: SERVITO:XX|ATTESA:YY
        return $this->parseResponse($response);
    }
    
    /**
     * Comando STATO - Ottiene stato sistema
     */
    public function getStato() {
        $this->sendCommand("STATO");
        $response = $this->receiveResponse();
        
        // Formato risposta: SERVIZIO:XX|DISTRIBUITI:YY|ATTESA:ZZ
        return $this->parseResponse($response);
    }
    
    /**
     * Parse risposta del server
     */
    private function parseResponse($response) {
        $data = [];
        $parts = explode('|', $response);
        
        foreach ($parts as $part) {
            $kv = explode(':', $part);
            if (count($kv) == 2) {
                $data[strtolower($kv[0])] = $kv[1];
            }
        }
        
        return $data;
    }
    
    /**
     * Chiude connessione
     */
    public function close() {
        if ($this->socket) {
            socket_close($this->socket);
        }
    }
    
    /**
     * Destructor
     */
    public function __destruct() {
        $this->close();
    }
}
?>
```

**File 2: `negozio.php`** - Interfaccia web principale

```php
<?php
// Abilita gestione errori
error_reporting(E_ALL);
ini_set('display_errors', 1);

require_once 'negozio_socket.php';

// Gestione azioni
$messaggio = '';
$tipo_messaggio = 'info'; // success, error, info

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        $client = new DispenserSocketClient();
        $client->connect();
        $client->autenticaNegozio();
        
        if (isset($_POST['azione'])) {
            switch ($_POST['azione']) {
                case 'servi':
                    $result = $client->serviCliente();
                    $messaggio = "✓ Cliente servito! Numero: " . $result['servito'];
                    $messaggio .= " | Persone in attesa: " . $result['attesa'];
                    $tipo_messaggio = 'success';
                    break;
                    
                case 'stato':
                    $result = $client->getStato();
                    $messaggio = "Numero in servizio: " . $result['servizio'];
                    $messaggio .= " | Numeri distribuiti: " . $result['distribuiti'];
                    $messaggio .= " | In attesa: " . $result['attesa'];
                    $tipo_messaggio = 'info';
                    break;
            }
        }
        
        $client->close();
        
    } catch (Exception $e) {
        $messaggio = "✗ Errore: " . $e->getMessage();
        $tipo_messaggio = 'error';
    }
}
?>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pannello Negozio - Sistema Dispenser</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        
        .container {
            max-width: 900px;
            margin: 0 auto;
        }
        
        .header {
            background: white;
            padding: 30px;
            border-radius: 15px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
            margin-bottom: 20px;
            text-align: center;
        }
        
        .header h1 {
            color: #333;
            font-size: 2.5em;
            margin-bottom: 10px;
        }
        
        .header p {
            color: #666;
            font-size: 1.1em;
        }
        
        .card {
            background: white;
            padding: 30px;
            border-radius: 15px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
            margin-bottom: 20px;
        }
        
        .numero-display {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 50px;
            border-radius: 15px;
            text-align: center;
            margin-bottom: 30px;
        }
        
        .numero-display h2 {
            font-size: 1.5em;
            margin-bottom: 20px;
            opacity: 0.9;
        }
        
        .numero-display .numero {
            font-size: 5em;
            font-weight: bold;
            text-shadow: 0 5px 15px rgba(0,0,0,0.3);
        }
        
        .buttons {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            margin-bottom: 20px;
        }
        
        .btn {
            padding: 20px;
            font-size: 1.2em;
            font-weight: bold;
            border: none;
            border-radius: 10px;
            cursor: pointer;
            transition: all 0.3s;
            text-transform: uppercase;
        }
        
        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        
        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
        }
        
        .btn-secondary {
            background: #f0f0f0;
            color: #333;
        }
        
        .btn-secondary:hover {
            background: #e0e0e0;
            transform: translateY(-2px);
        }
        
        .messaggio {
            padding: 20px;
            border-radius: 10px;
            margin-bottom: 20px;
            font-size: 1.1em;
            animation: slideIn 0.5s;
        }
        
        @keyframes slideIn {
            from {
                opacity: 0;
                transform: translateY(-20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        .messaggio.success {
            background: #d4edda;
            color: #155724;
            border: 2px solid #c3e6cb;
        }
        
        .messaggio.error {
            background: #f8d7da;
            color: #721c24;
            border: 2px solid #f5c6cb;
        }
        
        .messaggio.info {
            background: #d1ecf1;
            color: #0c5460;
            border: 2px solid #bee5eb;
        }
        
        .stats {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 15px;
            margin-top: 20px;
        }
        
        .stat-box {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 10px;
            text-align: center;
        }
        
        .stat-box h3 {
            color: #666;
            font-size: 0.9em;
            margin-bottom: 10px;
            text-transform: uppercase;
        }
        
        .stat-box .value {
            font-size: 2em;
            font-weight: bold;
            color: #667eea;
        }
        
        .istruzioni {
            background: #fff3cd;
            border: 2px solid #ffc107;
            padding: 20px;
            border-radius: 10px;
            margin-bottom: 20px;
        }
        
        .istruzioni h3 {
            color: #856404;
            margin-bottom: 10px;
        }
        
        .istruzioni ul {
            margin-left: 20px;
            color: #856404;
        }
        
        .istruzioni li {
            margin: 5px 0;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🏪 Pannello Negozio</h1>
            <p>Sistema Dispenser - Gestione Clienti</p>
        </div>
        
        <?php if ($messaggio): ?>
        <div class="messaggio <?php echo $tipo_messaggio; ?>">
            <?php echo htmlspecialchars($messaggio); ?>
        </div>
        <?php endif; ?>
        
        <div class="card">
            <div class="numero-display" id="numeroDisplay">
                <h2>NUMERO IN SERVIZIO</h2>
                <div class="numero" id="numeroServizio">--</div>
            </div>
            
            <form method="POST" action="">
                <div class="buttons">
                    <button type="submit" name="azione" value="servi" class="btn btn-primary">
                        ✓ SERVI CLIENTE
                    </button>
                    <button type="submit" name="azione" value="stato" class="btn btn-secondary">
                        📊 AGGIORNA STATO
                    </button>
                </div>
            </form>
            
            <div class="stats">
                <div class="stat-box">
                    <h3>In Servizio</h3>
                    <div class="value" id="statServizio">-</div>
                </div>
                <div class="stat-box">
                    <h3>Distribuiti</h3>
                    <div class="value" id="statDistribuiti">-</div>
                </div>
                <div class="stat-box">
                    <h3>In Attesa</h3>
                    <div class="value" id="statAttesa">-</div>
                </div>
            </div>
        </div>
        
        <div class="istruzioni">
            <h3>📖 Istruzioni</h3>
            <ul>
                <li><strong>SERVI CLIENTE:</strong> Segna il numero corrente come servito e passa al successivo</li>
                <li><strong>AGGIORNA STATO:</strong> Visualizza lo stato attuale del sistema</li>
                <li>Le statistiche si aggiornano automaticamente ogni 3 secondi</li>
                <li>Assicurati che il server Java sia in esecuzione sulla porta 7777</li>
            </ul>
        </div>
    </div>
    
    <script>
        // Auto-refresh statistiche ogni 3 secondi
        setInterval(async function() {
            try {
                const response = await fetch('api_stato.php');
                const data = await response.json();
                
                if (data.success) {
                    document.getElementById('numeroServizio').textContent = data.servizio;
                    document.getElementById('statServizio').textContent = data.servizio;
                    document.getElementById('statDistribuiti').textContent = data.distribuiti;
                    document.getElementById('statAttesa').textContent = data.attesa;
                }
            } catch (error) {
                console.error('Errore aggiornamento:', error);
            }
        }, 3000);
        
        // Carica stato iniziale
        window.addEventListener('load', function() {
            setTimeout(function() {
                fetch('api_stato.php')
                    .then(response => response.json())
                    .then(data => {
                        if (data.success) {
                            document.getElementById('numeroServizio').textContent = data.servizio;
                            document.getElementById('statServizio').textContent = data.servizio;
                            document.getElementById('statDistribuiti').textContent = data.distribuiti;
                            document.getElementById('statAttesa').textContent = data.attesa;
                        }
                    });
            }, 500);
        });
    </script>
</body>
</html>
```

**File 3: `api_stato.php`** - API REST per aggiornamenti AJAX

```php
<?php
header('Content-Type: application/json');

require_once 'negozio_socket.php';

try {
    $client = new DispenserSocketClient();
    $client->connect();
    $client->autenticaNegozio();
    
    $stato = $client->getStato();
    
    $client->close();
    
    echo json_encode([
        'success' => true,
        'servizio' => $stato['servizio'] ?? '0',
        'distribuiti' => $stato['distribuiti'] ?? '0',
        'attesa' => $stato['attesa'] ?? '0'
    ]);
    
} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'error' => $e->getMessage()
    ]);
}
?>
```

#### 5.4 Installazione e Avvio

**Prerequisiti:**
```bash
# Verifica PHP installato
php --version  # Minimo PHP 7.0

# Verifica estensione socket
php -m | grep sockets
# Se non presente: sudo apt install php-sockets (Linux)
```

**Struttura Directory:**
```
ES02b-Dispenser/
├── DispenserServer.java
├── DispenserClient.java
├── NegozioClient.java
└── php/
    ├── negozio_socket.php
    ├── negozio.php
    └── api_stato.php
```

**Avvio Sistema Completo:**

```bash
# Terminale 1: Avvia server Java
cd ES02b-Dispenser
java DispenserServer

# Terminale 2: Avvia web server PHP
cd ES02b-Dispenser/php
php -S localhost:8000

# Browser: Apri interfaccia negozio
http://localhost:8000/negozio.php

# Terminale 3-5: Avvia client Java (opzionale)
java DispenserClient
```

#### 5.5 Testing Integrazione PHP-Java

**Test 1: Verifica Connessione**
```bash
# Script di test: test_connection.php
<?php
require_once 'negozio_socket.php';

try {
    $client = new DispenserSocketClient();
    $client->connect();
    echo "✓ Connessione riuscita!\n";
    
    $client->autenticaNegozio();
    echo "✓ Autenticazione negozio OK!\n";
    
    $client->close();
} catch (Exception $e) {
    echo "✗ Errore: " . $e->getMessage() . "\n";
}
?>

# Esegui test
php test_connection.php
```

**Test 2: Ciclo Completo**
1. Avvia server Java
2. Client Java richiede numeri (1, 2, 3)
3. Apri interfaccia PHP nel browser
4. Clicca "SERVI CLIENTE" → dovrebbe passare a 1
5. Clicca nuovamente → dovrebbe passare a 2
6. Verifica statistiche aggiornate

**Test 3: Stress Test**
```php
// stress_test.php
<?php
require_once 'negozio_socket.php';

for ($i = 0; $i < 10; $i++) {
    try {
        $client = new DispenserSocketClient();
        $client->connect();
        $client->autenticaNegozio();
        
        $result = $client->serviCliente();
        echo "Servito numero: " . $result['servito'] . "\n";
        
        $client->close();
        sleep(1);
    } catch (Exception $e) {
        echo "Errore: " . $e->getMessage() . "\n";
    }
}
?>
```

#### 5.6 Checklist Implementazione PHP:
- [ ] File `negozio_socket.php` creato e testato
- [ ] File `negozio.php` con interfaccia funzionante
- [ ] File `api_stato.php` per aggiornamenti AJAX
- [ ] Connessione socket PHP → Java server funziona
- [ ] Comando NEGOZIO autenticazione OK
- [ ] Comando SERVI incrementa numero servito
- [ ] Comando STATO ritorna statistiche
- [ ] Aggiornamento automatico ogni 3 secondi
- [ ] Gestione errori di connessione
- [ ] Interfaccia responsive e user-friendly

#### 5.7 Troubleshooting PHP

**Problema: "Connection refused"**
```bash
# Verifica server Java in ascolto
netstat -an | grep 7777

# Verifica firewall
sudo ufw allow 7777/tcp
```

**Problema: "Call to undefined function socket_create()"**
```bash
# Linux
sudo apt install php-sockets
sudo systemctl restart apache2

# macOS
# Uncomment in php.ini: extension=sockets
```

**Problema: "Permission denied"**
```bash
# Assicurati che PHP possa creare socket
chmod +x negozio_socket.php
```

**Debug Mode:**
```php
// Aggiungi all'inizio di negozio.php
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Log dettagliato
$client->setDebug(true);
```

#### 5.8 Screenshot Richiesti per PHP

| Screenshot | Descrizione |
|------------|-------------|
| **SS_PHP01** | Interfaccia negozio.php caricata |
| **SS_PHP02** | Click "SERVI CLIENTE" → conferma |
| **SS_PHP03** | Statistiche aggiornate automaticamente |
| **SS_PHP04** | Server Java log comando SERVI da PHP |
| **SS_PHP05** | Test con client Java + PHP simultanei |

#### 5.9 Vantaggi Soluzione PHP

✅ **Accessibilità:** Interfaccia web utilizzabile da qualsiasi dispositivo  
✅ **User-Friendly:** GUI grafica invece di terminale  
✅ **Real-time:** Aggiornamenti automatici ogni 3 secondi  
✅ **Scalabilità:** Aggiungibile dashboard, statistiche avanzate  
✅ **Interoperabilità:** Dimostra comunicazione multi-linguaggio (PHP ↔ Java)  

#### 5.10 Valutazione Extra PHP

**Punti Bonus: +30**

| Criterio | Punti |
|----------|-------|
| Classe socket PHP funzionante | 10 |
| Interfaccia web completa | 10 |
| API AJAX con auto-refresh | 5 |
| Gestione errori robusta | 3 |
| Screenshot e documentazione | 2 |

**Totale possibile con PHP:** 130 punti (100 base + 30 PHP)

---

## 📚 **RISORSE UTILI**

### 📖 **Documentazione:**
- [Java ServerSocket](https://docs.oracle.com/javase/8/docs/api/java/net/ServerSocket.html)
- [Java Thread](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.html)
- [Synchronized Methods](https://docs.oracle.com/javase/tutorial/essential/concurrency/syncmeth.html)

### 🔍 **Comandi Utili:**
```bash
# Verifica porta occupata
netstat -an | grep 7777

# Testa connessione manuale
telnet localhost 7777

# Avvia client multipli con script
for i in {1..5}; do
    java DispenserClient &
done
```

### 💬 **FAQ:**

**Q: Come gestisco client multipli simultanei?**  
A: Crea un nuovo thread per ogni client nel server.

**Q: Come evito race condition sul contatore?**  
A: Usa `synchronized` sul metodo che incrementa il contatore.

**Q: Posso usare AtomicInteger invece di synchronized?**  
A: Sì! È un'ottima alternativa thread-safe:
```java
private static AtomicInteger ultimoNumeroAssegnato = new AtomicInteger(0);
// ...
int numero = ultimoNumeroAssegnato.incrementAndGet();
```

**Q: Come faccio screenshot di terminali multipli?**  
A: Usa un tool di screenshot che cattura l'intero schermo o usa un window manager che permette di affiancare i terminali.

---

## ⏰ **SCADENZE E MODALITÀ DI CONSEGNA**

### 📅 **Timeline:**
- **Assegnazione:** [Data di oggi]
- **Consegna:** [Data + 1 settimana]

### 📤 **Modalità Consegna:**

**Su Classroom/Piattaforma didattica:**
1. **Link Documento Google** con:
   - Screenshot dimostrativi
   - Descrizione funzionamento
   - Link al codice
   
2. **Link Repository Git** (consigliato):
   ```
   https://github.com/tuousername/SIS3-Socket/tree/main/ES02b-Dispenser
   ```

3. **Oppure ZIP con codice** caricato su Drive:
   ```
   ES02b-Dispenser.zip
   ├── DispenserServer.java
   ├── DispenserClient.java
   ├── NegozioClient.java (opzionale)
   └── README.txt (istruzioni esecuzione)
   ```

### 📧 **Contatti:**
- **Email docente:** [email@scuola.it]
- **Orario ricevimento:** [Giorni e orari]

---

## 🎉 **CONCLUSIONI**

Questa esercitazione ti permetterà di:

- 🎯 **Applicare** concetti di socket TCP in uno scenario reale
- 🔒 **Comprendere** l'importanza della sincronizzazione
- 👥 **Gestire** client multipli simultanei
- 📊 **Implementare** un sistema di gestione code
- 📸 **Documentare** professionalmente il tuo lavoro

**Ricorda:** I sistemi di code sono usati ovunque! Da banche a ristoranti, da ospedali a uffici pubblici. Con questa esercitazione stai imparando a costruire un sistema utilizzato quotidianamente da milioni di persone.

**Suggerimento finale:** Inizia con la versione base (Server + Client). Una volta funzionante, aggiungi il Negozio. Infine, se hai tempo, sperimenta con PHP per il bonus!

---

**Buon lavoro! 🚀**

---

*Esercitazione creata per il corso di Sistemi e Reti 3 - ITCS Cannizzaro di Rho*  
*Anno Scolastico 2025/26*  
*Versione 1.0 - Novembre 2025*
