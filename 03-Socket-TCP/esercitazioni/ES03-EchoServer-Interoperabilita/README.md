# 🎓 ESERCITAZIONE: Server Multithreading Java con Client Java e PHP

> *Introduzione alla comunicazione multi-linguaggio - Sistemi e Reti 3*

---

## 📋 **INFORMAZIONI GENERALI**

**Materia:** Sistemi e Reti  
**Argomento:** Socket TCP Multithreading - Interoperabilità Java/PHP  
**Tempo stimato:** 2 ore  
**Difficoltà:** ⭐⭐ (Base-Intermedia)  
**Modalità:** Individuale  

---

## 🎯 **OBIETTIVI DIDATTICI**

Al termine di questa esercitazione lo studente sarà in grado di:

- ✅ **Implementare** un server TCP multithreading in Java
- ✅ **Creare** client Java che si connettono al server
- ✅ **Sviluppare** client PHP che comunicano via socket
- ✅ **Comprendere** l'interoperabilità tra linguaggi diversi
- ✅ **Gestire** connessioni multiple simultanee
- ✅ **Testare** la comunicazione con client eterogenei

---

## 📚 **PREREQUISITI**

### Conoscenze Richieste:
- 🔌 Socket TCP/IP base
- ☕ Java: Thread, I/O stream
- 🐘 PHP: Funzioni socket base (opzionale)
- 📝 String manipulation

### Strumenti Necessari:
- ☕ **Java JDK** 11+
- 🐘 **PHP** 7.4+ (con estensione sockets)
- 💻 **Terminale** o IDE

---

## 📖 **TRACCIA DELL'ESERCIZIO**

### 🚀 **Applicazione: "Echo Server Multithreading"**

Creare un sistema semplice dove:

1. **Server Java** che:
   - Ascolta sulla porta `5555`
   - Crea un thread per ogni client connesso
   - Riceve messaggi e li rimanda indietro (echo)
   - Visualizza quale client ha inviato cosa

2. **Client Java** che:
   - Si connette al server
   - Invia messaggi
   - Riceve le risposte echo

3. **Client PHP** che:
   - Fa la stessa cosa del client Java
   - Dimostra l'interoperabilità

### 📐 **Esempio di Interazione:**

```
=== SERVER ===
Server Echo avviato sulla porta 5555
In attesa di connessioni...

[CONNESSIONE] Client Java da 127.0.0.1
[JAVA-CLIENT] Ricevuto: Ciao dal client Java
[JAVA-CLIENT] Inviato echo

[CONNESSIONE] Client PHP da 127.0.0.1
[PHP-CLIENT] Ricevuto: Hello from PHP!
[PHP-CLIENT] Inviato echo

=== CLIENT JAVA ===
Connesso al server!
Inserisci messaggio: Ciao dal client Java
Echo dal server: Ciao dal client Java
Inserisci messaggio: exit

=== CLIENT PHP ===
Connesso al server!
Inserisci messaggio: Hello from PHP!
Echo dal server: Hello from PHP!
```

---

## 🛠️ **IMPLEMENTAZIONE**

### 📝 **PARTE 1: Server Java (30 minuti)**

#### File: `EchoServer.java`

```java
import java.io.*;
import java.net.*;

public class EchoServer {
    private static final int PORTA = 5555;
    private static int clientCounter = 0;
    
    public static void main(String[] args) {
        System.out.println("=== SERVER ECHO MULTITHREADING ===");
        System.out.println("Server avviato sulla porta " + PORTA);
        System.out.println("In attesa di connessioni...\n");
        
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            
            while (true) {
                // Accetta nuova connessione
                Socket clientSocket = serverSocket.accept();
                
                // Crea thread per gestire il client
                clientCounter++;
                Thread clientThread = new Thread(new ClientHandler(clientSocket, clientCounter));
                clientThread.start();
            }
            
        } catch (IOException e) {
            System.err.println("Errore server: " + e.getMessage());
        }
    }
}

/**
 * Thread per gestire un singolo client
 */
class ClientHandler implements Runnable {
    private Socket socket;
    private int clientId;
    
    public ClientHandler(Socket socket, int clientId) {
        this.socket = socket;
        this.clientId = clientId;
    }
    
    @Override
    public void run() {
        String clientAddress = socket.getInetAddress().getHostAddress();
        System.out.println("[CONNESSIONE] Client #" + clientId + " da " + clientAddress);
        
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String messaggio;
            
            // Loop di ricezione messaggi
            while ((messaggio = in.readLine()) != null) {
                
                // Se riceve "exit", chiude la connessione
                if (messaggio.equalsIgnoreCase("exit")) {
                    System.out.println("[CLIENT #" + clientId + "] Disconnessione");
                    break;
                }
                
                // Log del messaggio ricevuto
                System.out.println("[CLIENT #" + clientId + "] Ricevuto: " + messaggio);
                
                // Invia echo al client
                out.println("ECHO: " + messaggio);
                System.out.println("[CLIENT #" + clientId + "] Inviato echo");
            }
            
            socket.close();
            System.out.println("[CLIENT #" + clientId + "] Connessione chiusa\n");
            
        } catch (IOException e) {
            System.err.println("[CLIENT #" + clientId + "] Errore: " + e.getMessage());
        }
    }
}
```

**Caratteristiche:**
- ✅ Multithreading: un thread per ogni client
- ✅ Contatore client per identificarli
- ✅ Echo: rimanda indietro il messaggio ricevuto
- ✅ Comando "exit" per disconnessione
- ✅ Log dettagliato di tutte le operazioni

---

### 💻 **PARTE 2: Client Java (20 minuti)**

#### File: `EchoClientJava.java`

```java
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class EchoClientJava {
    private static final String HOST = "localhost";
    private static final int PORTA = 5555;
    
    public static void main(String[] args) {
        System.out.println("=== CLIENT JAVA - ECHO ===");
        
        try (
            Socket socket = new Socket(HOST, PORTA);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("✓ Connesso al server " + HOST + ":" + PORTA);
            System.out.println("Digita 'exit' per disconnetterti\n");
            
            while (true) {
                // Leggi input utente
                System.out.print("Inserisci messaggio: ");
                String messaggio = scanner.nextLine();
                
                // Invia al server
                out.println(messaggio);
                
                // Se exit, chiudi
                if (messaggio.equalsIgnoreCase("exit")) {
                    System.out.println("Disconnessione...");
                    break;
                }
                
                // Ricevi echo dal server
                String risposta = in.readLine();
                System.out.println("← Server: " + risposta + "\n");
            }
            
            System.out.println("Connessione chiusa.");
            
        } catch (IOException e) {
            System.err.println("Errore connessione: " + e.getMessage());
        }
    }
}
```

**Caratteristiche:**
- ✅ Connessione semplice al server
- ✅ Input da tastiera
- ✅ Invio e ricezione messaggi
- ✅ Comando exit per chiudere

---

### 🐘 **PARTE 3: Client PHP Web (30 minuti)**

#### File: `echo_client.php`

```php
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Echo Client PHP</title>
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
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }
        
        .container {
            background: white;
            border-radius: 15px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            padding: 40px;
            max-width: 600px;
            width: 100%;
        }
        
        h1 {
            color: #667eea;
            margin-bottom: 10px;
            font-size: 2em;
        }
        
        .subtitle {
            color: #666;
            margin-bottom: 30px;
            font-size: 0.9em;
        }
        
        .form-group {
            margin-bottom: 20px;
        }
        
        label {
            display: block;
            margin-bottom: 8px;
            color: #333;
            font-weight: 600;
        }
        
        input[type="text"] {
            width: 100%;
            padding: 12px 15px;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            font-size: 16px;
            transition: border-color 0.3s;
        }
        
        input[type="text"]:focus {
            outline: none;
            border-color: #667eea;
        }
        
        button {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 12px 30px;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: transform 0.2s, box-shadow 0.2s;
            width: 100%;
        }
        
        button:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
        }
        
        button:active {
            transform: translateY(0);
        }
        
        .result {
            margin-top: 30px;
            padding: 20px;
            border-radius: 8px;
            border-left: 4px solid #667eea;
            background: #f8f9fa;
        }
        
        .result h3 {
            color: #667eea;
            margin-bottom: 10px;
        }
        
        .result p {
            color: #333;
            margin: 5px 0;
            line-height: 1.6;
        }
        
        .success {
            background: #d4edda;
            border-left-color: #28a745;
        }
        
        .success h3 {
            color: #28a745;
        }
        
        .error {
            background: #f8d7da;
            border-left-color: #dc3545;
        }
        
        .error h3 {
            color: #dc3545;
        }
        
        .info-box {
            background: #e7f3ff;
            border-left: 4px solid #2196F3;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
        }
        
        .info-box p {
            color: #1976D2;
            margin: 5px 0;
            font-size: 0.9em;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🔌 Echo Client PHP</h1>
        <p class="subtitle">Comunicazione con Server Java via Socket</p>
        
        <div class="info-box">
            <p><strong>📡 Server:</strong> localhost:5555</p>
            <p><strong>💬 Protocollo:</strong> TCP Echo</p>
        </div>
        
        <form method="POST">
            <div class="form-group">
                <label for="messaggio">Messaggio da inviare:</label>
                <input type="text" id="messaggio" name="messaggio" 
                       placeholder="Inserisci il tuo messaggio..." 
                       required autofocus>
            </div>
            
            <button type="submit">📤 Invia Messaggio</button>
        </form>
        
        <?php
        if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['messaggio'])) {
            $messaggio = trim($_POST['messaggio']);
            
            // Configurazione
            define('SERVER_HOST', 'localhost');
            define('SERVER_PORT', 5555);
            
            try {
                // Crea socket
                $socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
                if ($socket === false) {
                    throw new Exception("Errore creazione socket: " . socket_strerror(socket_last_error()));
                }
                
                // Imposta timeout
                socket_set_option($socket, SOL_SOCKET, SO_RCVTIMEO, array('sec' => 5, 'usec' => 0));
                socket_set_option($socket, SOL_SOCKET, SO_SNDTIMEO, array('sec' => 5, 'usec' => 0));
                
                // Connetti al server
                $result = socket_connect($socket, SERVER_HOST, SERVER_PORT);
                if ($result === false) {
                    throw new Exception("Errore connessione al server: " . socket_strerror(socket_last_error($socket)));
                }
                
                // Invia messaggio
                $messaggio_con_newline = $messaggio . "\n";
                $bytes_sent = socket_write($socket, $messaggio_con_newline, strlen($messaggio_con_newline));
                if ($bytes_sent === false) {
                    throw new Exception("Errore invio messaggio: " . socket_strerror(socket_last_error($socket)));
                }
                
                // Ricevi risposta
                $risposta = socket_read($socket, 1024, PHP_NORMAL_READ);
                if ($risposta === false) {
                    throw new Exception("Errore ricezione risposta: " . socket_strerror(socket_last_error($socket)));
                }
                
                // Chiudi socket
                socket_close($socket);
                
                // Mostra risultato
                echo '<div class="result success">';
                echo '<h3>✅ Comunicazione Riuscita</h3>';
                echo '<p><strong>Messaggio inviato:</strong> ' . htmlspecialchars($messaggio) . '</p>';
                echo '<p><strong>Risposta dal server:</strong> ' . htmlspecialchars(trim($risposta)) . '</p>';
                echo '<p><strong>Byte inviati:</strong> ' . $bytes_sent . '</p>';
                echo '</div>';
                
            } catch (Exception $e) {
                echo '<div class="result error">';
                echo '<h3>❌ Errore di Comunicazione</h3>';
                echo '<p>' . htmlspecialchars($e->getMessage()) . '</p>';
                echo '<p><strong>Suggerimento:</strong> Verifica che il server Java sia avviato sulla porta 5555</p>';
                echo '</div>';
            }
        }
        ?>
    </div>
</body>
</html>
```

**Caratteristiche:**
- ✅ Interfaccia web moderna e responsive
- ✅ Form HTML per input messaggi
- ✅ Comunicazione socket con server Java
- ✅ Gestione errori completa con try-catch
- ✅ Timeout configurati (5 secondi)
- ✅ Visualizzazione risultati con CSS
- ✅ Sicurezza: htmlspecialchars per prevenire XSS

---

### 🐘 **PARTE 3 BIS: API REST PHP (30 minuti)**

#### File: `api_echo.php`

```php
<?php
/**
 * API REST per comunicare con server Java
 * Restituisce JSON per chiamate AJAX
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

// Configurazione
define('SERVER_HOST', 'localhost');
define('SERVER_PORT', 5555);

// Verifica metodo POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode([
        'success' => false,
        'error' => 'Metodo non consentito. Usa POST.'
    ]);
    exit;
}

// Leggi JSON input
$input = file_get_contents('php://input');
$data = json_decode($input, true);

if (!isset($data['messaggio']) || empty(trim($data['messaggio']))) {
    echo json_encode([
        'success' => false,
        'error' => 'Messaggio non fornito'
    ]);
    exit;
}

$messaggio = trim($data['messaggio']);

try {
    // Crea socket
    $socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
    if ($socket === false) {
        throw new Exception("Errore creazione socket");
    }
    
    // Timeout
    socket_set_option($socket, SOL_SOCKET, SO_RCVTIMEO, array('sec' => 5, 'usec' => 0));
    socket_set_option($socket, SOL_SOCKET, SO_SNDTIMEO, array('sec' => 5, 'usec' => 0));
    
    // Connetti
    $result = socket_connect($socket, SERVER_HOST, SERVER_PORT);
    if ($result === false) {
        throw new Exception("Server non raggiungibile");
    }
    
    // Invia
    socket_write($socket, $messaggio . "\n", strlen($messaggio) + 1);
    
    // Ricevi
    $risposta = socket_read($socket, 1024, PHP_NORMAL_READ);
    socket_close($socket);
    
    // Risposta JSON
    echo json_encode([
        'success' => true,
        'messaggio_inviato' => $messaggio,
        'risposta_server' => trim($risposta),
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    
} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'error' => $e->getMessage()
    ]);
}
?>
```

#### File: `echo_client_ajax.php`

```php
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Echo Client AJAX</title>
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
            max-width: 800px;
            margin: 0 auto;
            background: white;
            border-radius: 15px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            padding: 40px;
        }
        
        h1 {
            color: #667eea;
            margin-bottom: 10px;
        }
        
        .input-group {
            display: flex;
            gap: 10px;
            margin: 30px 0;
        }
        
        input {
            flex: 1;
            padding: 12px 15px;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            font-size: 16px;
        }
        
        button {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 12px 30px;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            font-weight: 600;
        }
        
        button:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
        }
        
        #log {
            background: #f8f9fa;
            border-radius: 8px;
            padding: 20px;
            max-height: 400px;
            overflow-y: auto;
        }
        
        .log-entry {
            padding: 10px;
            margin-bottom: 10px;
            border-radius: 5px;
            border-left: 4px solid #667eea;
            background: white;
        }
        
        .log-entry.success {
            border-left-color: #28a745;
        }
        
        .log-entry.error {
            border-left-color: #dc3545;
            background: #fff5f5;
        }
        
        .timestamp {
            color: #666;
            font-size: 0.8em;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🚀 Echo Client AJAX</h1>
        <p style="color: #666; margin-bottom: 20px;">Comunicazione in tempo reale con server Java</p>
        
        <div class="input-group">
            <input type="text" id="messaggio" placeholder="Inserisci messaggio...">
            <button onclick="inviaMessaggio()">📤 Invia</button>
        </div>
        
        <h3>📋 Log Comunicazioni</h3>
        <div id="log"></div>
    </div>
    
    <script>
        const inputMessaggio = document.getElementById('messaggio');
        const log = document.getElementById('log');
        
        // Invia con ENTER
        inputMessaggio.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') inviaMessaggio();
        });
        
        function inviaMessaggio() {
            const messaggio = inputMessaggio.value.trim();
            if (!messaggio) return;
            
            // Chiama API
            fetch('api_echo.php', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ messaggio: messaggio })
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    aggiungiLog(
                        `✅ Inviato: "${data.messaggio_inviato}"<br>` +
                        `← Risposta: "${data.risposta_server}"`,
                        'success',
                        data.timestamp
                    );
                } else {
                    aggiungiLog(`❌ Errore: ${data.error}`, 'error');
                }
            })
            .catch(error => {
                aggiungiLog(`❌ Errore di rete: ${error.message}`, 'error');
            });
            
            inputMessaggio.value = '';
        }
        
        function aggiungiLog(messaggio, tipo = 'success', timestamp = null) {
            const entry = document.createElement('div');
            entry.className = `log-entry ${tipo}`;
            
            const now = timestamp || new Date().toLocaleString('it-IT');
            entry.innerHTML = `
                <div class="timestamp">${now}</div>
                <div>${messaggio}</div>
            `;
            
            log.insertBefore(entry, log.firstChild);
        }
    </script>
</body>
</html>
```

**Caratteristiche:**
- ✅ Comunicazione AJAX senza reload pagina
- ✅ API REST JSON
- ✅ Log in tempo reale
- ✅ Interfaccia moderna e interattiva
- ✅ Invio con tasto ENTER

---

## 🧪 **TESTING**

### Test 1: Server + Client Java

```bash
# Terminale 1: Avvia server
javac EchoServer.java
java EchoServer

# Terminale 2: Avvia client Java
javac EchoClientJava.java
java EchoClientJava

# Test:
# Client> Ciao!
# Server risponde> ECHO: Ciao!
```

### Test 2: Server + Client PHP Web

```bash
# Terminale 1: Server già avviato

# Terminale 2: Avvia server web PHP
cd /path/to/esercitazione
php -S localhost:8000

# Browser: apri http://localhost:8000/echo_client.php
# Inserisci messaggio nel form e premi Invia
# Vedrai la risposta del server Java
```

### Test 2B: Server + Client AJAX

```bash
# Terminale 1: Server Java avviato
# Terminale 2: Server web PHP avviato

# Browser: apri http://localhost:8000/echo_client_ajax.php
# Invia più messaggi e guarda il log in tempo reale!
```

### Test 3: Client Multipli Simultanei

```bash
# Terminale 1: Server attivo
# Terminale 2: Server web PHP attivo

# Terminale 3: Client Java #1
java EchoClientJava

# Browser 1: http://localhost:8000/echo_client.php
# Browser 2: http://localhost:8000/echo_client_ajax.php

# Tutti i client funzionano contemporaneamente!
# Il server Java gestisce tutte le connessioni in parallelo
```

---

## 📊 **TABELLA TEST**

| Test | Client | Messaggio | Risposta Attesa | Stato |
|------|--------|-----------|-----------------|-------|
| 1 | Java | "Test Java" | "ECHO: Test Java" | ✅ |
| 2 | PHP Web | "Test PHP" | "ECHO: Test PHP" | ✅ |
| 3 | PHP AJAX | "Test AJAX" | "ECHO: Test AJAX" | ✅ |
| 4 | Java | "exit" | Disconnessione | ✅ |
| 5 | Client multipli | Vari | Tutti ricevono echo | ✅ |

---

## 💡 **CONCETTI CHIAVE**

### 1️⃣ **Multithreading**
Ogni client ottiene un thread dedicato:
```java
Thread clientThread = new Thread(new ClientHandler(socket, clientCounter));
clientThread.start();
```

### 2️⃣ **Interoperabilità**
Java e PHP comunicano usando lo stesso protocollo:
- Messaggi terminati con `\n` (newline)
- Formato testo semplice
- Socket TCP standard

### 3️⃣ **Protocollo Semplice**
```
Client → Server: "messaggio\n"
Server → Client: "ECHO: messaggio\n"
Client → Server: "exit\n" (per chiudere)
```

---

## 🎯 **ESERCIZI PROPOSTI**

### Esercizio 1: Migliora il Server
Modifica il server per:
- Inviare un messaggio di benvenuto al client
- Contare i messaggi totali ricevuti
- Mostrare statistiche quando un client si disconnette

### Esercizio 2: Client con Nome
Modifica i client per:
- Chiedere nome utente all'inizio
- Inviare messaggi nel formato: `[NOME]: messaggio`
- Il server mostra il nome nel log

### Esercizio 3: Broadcast
Modifica il server per:
- Mantenere lista di tutti i client connessi
- Inviare ogni messaggio a TUTTI i client (chat!)

---

## 📝 **DELIVERABLE**

### File da Consegnare:
1. `EchoServer.java`
2. `EchoClientJava.java`
3. `echo_client.php` (form web)
4. `api_echo.php` (API REST)
5. `echo_client_ajax.php` (client AJAX)
6. **Screenshot** che mostrano:
   - Server con 2+ client connessi
   - Client Java che invia/riceve
   - Browser con echo_client.php funzionante
   - Browser con echo_client_ajax.php e log messaggi
   - Log del server con tutti i client connessi

---

## 🏆 **CRITERI DI VALUTAZIONE**

| Criterio | Punti | Descrizione |
|----------|-------|-------------|
| **Server Multithreading** | 25 | Gestisce client multipli |
| **Client Java** | 20 | Funziona correttamente |
| **Client PHP Web** | 25 | Form funzionante |
| **API REST + AJAX** | 20 | Comunicazione asincrona |
| **Testing** | 10 | Screenshot completi |

**Totale:** 100 punti

**Bonus:**
- +10 punti: Aggiungi funzionalità broadcast (chat)
- +5 punti: Gestione disconnessioni graceful

---

## 🚀 **GUIDA RAPIDA**

### Compilazione e Avvio

```bash
# 1. Compila e avvia Server Java (terminale 1)
javac EchoServer.java
java EchoServer

# 2. Avvia server web PHP (terminale 2)
php -S localhost:8000

# 3. Test Client Java (terminale 3)
javac EchoClientJava.java
java EchoClientJava

# 4. Test Client PHP Web
# Browser: http://localhost:8000/echo_client.php

# 5. Test Client AJAX
# Browser: http://localhost:8000/echo_client_ajax.php
```

### Troubleshooting PHP

**Problema: "Call to undefined function socket_create()"**
```bash
# Installa estensione sockets
sudo apt install php-sockets  # Ubuntu/Debian
sudo yum install php-sockets  # CentOS/RHEL
brew install php             # macOS

# Verifica
php -m | grep socket
```

**Problema: "Address already in use" (porta 8000)**
```bash
# Usa porta diversa
php -S localhost:8080

# OPPURE trova processo e killalo
lsof -i :8000
kill -9 <PID>
```

**Problema: Server Java non raggiungibile**
```bash
# Verifica server attivo
netstat -an | grep 5555

# Verifica firewall
sudo ufw allow 5555
```

---

## 📚 **RISORSE UTILI**

### Documentazione:
- [Java ServerSocket](https://docs.oracle.com/javase/8/docs/api/java/net/ServerSocket.html)
- [Java Thread](https://docs.oracle.com/javase/tutorial/essential/concurrency/)
- [PHP Socket Functions](https://www.php.net/manual/en/ref.sockets.php)
- [PHP fsockopen](https://www.php.net/manual/en/function.fsockopen.php)

### Comandi Utili:
```bash
# Verifica porta in uso
netstat -an | grep 5555

# Test connessione manuale
telnet localhost 5555

# Monitora connessioni
watch -n 1 'netstat -an | grep 5555'
```

---

## 💬 **FAQ**

**Q: Perché usare PHP oltre a Java?**  
A: Per dimostrare che i socket TCP sono **indipendenti dal linguaggio**. Qualsiasi linguaggio può comunicare con qualsiasi server!

**Q: Posso usare Python invece di PHP?**  
A: Sì! Il protocollo è lo stesso. Ecco un esempio:
```python
import socket

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect(('localhost', 5555))
sock.send(b'Hello from Python\n')
print(sock.recv(1024).decode())
sock.close()
```

**Q: Come faccio a testare senza PHP?**  
A: Usa `telnet` o `nc` (netcat):
```bash
telnet localhost 5555
# Poi digita messaggi e premi ENTER
```

**Q: Il server si blocca con un client?**  
A: No! Grazie al multithreading, ogni client ha il suo thread. Il server rimane sempre responsive.

---

## 🎉 **CONCLUSIONI**

Questa esercitazione ti ha mostrato:

- 🎯 **Server multithreading** funzionante e scalabile
- 🔄 **Interoperabilità** tra Java e PHP
- 🌐 **Protocollo TCP** indipendente dal linguaggio
- 👥 **Gestione client multipli** simultanei

**Prossimi Passi:**
1. Prova ad aggiungere funzionalità (broadcast, nomi utente)
2. Crea client in altri linguaggi (Python, JavaScript/Node.js)
3. Implementa un protocollo più complesso (comandi, autenticazione)

**Ricorda:** I socket sono lo strumento fondamentale per la comunicazione in rete. Padroneggiandoli, puoi creare qualsiasi tipo di applicazione client-server! 🚀

---

**Buon lavoro! 💻**

---

*Esercitazione creata per il corso di Sistemi e Reti 3*  
*Anno Scolastico 2025/26*
