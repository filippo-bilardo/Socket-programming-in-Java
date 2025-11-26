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

### 🐘 **PARTE 3: Client PHP (30 minuti)**

#### File: `echo_client.php`

```php
<?php
/**
 * Client PHP per comunicare con server Java
 */

// Configurazione
define('SERVER_HOST', 'localhost');
define('SERVER_PORT', 5555);

echo "=== CLIENT PHP - ECHO ===\n";

// Crea socket
$socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
if ($socket === false) {
    die("Errore creazione socket: " . socket_strerror(socket_last_error()) . "\n");
}

// Connetti al server
echo "Connessione al server " . SERVER_HOST . ":" . SERVER_PORT . "...\n";
$result = socket_connect($socket, SERVER_HOST, SERVER_PORT);
if ($result === false) {
    die("Errore connessione: " . socket_strerror(socket_last_error($socket)) . "\n");
}

echo "✓ Connesso al server!\n";
echo "Digita 'exit' per disconnetterti\n\n";

// Loop di comunicazione
while (true) {
    // Leggi input utente
    echo "Inserisci messaggio: ";
    $messaggio = trim(fgets(STDIN));
    
    // Invia al server
    $messaggio_con_newline = $messaggio . "\n";
    socket_write($socket, $messaggio_con_newline, strlen($messaggio_con_newline));
    
    // Se exit, chiudi
    if (strtolower($messaggio) === 'exit') {
        echo "Disconnessione...\n";
        break;
    }
    
    // Ricevi echo dal server
    $risposta = socket_read($socket, 1024, PHP_NORMAL_READ);
    if ($risposta === false) {
        echo "Errore ricezione: " . socket_strerror(socket_last_error($socket)) . "\n";
        break;
    }
    
    echo "← Server: " . trim($risposta) . "\n\n";
}

// Chiudi socket
socket_close($socket);
echo "Connessione chiusa.\n";
?>
```

**Caratteristiche:**
- ✅ Socket nativi PHP
- ✅ Stessa interfaccia del client Java
- ✅ Compatibilità completa con server Java
- ✅ Gestione errori

---

### 🐘 **PARTE 3 BIS: Client PHP Semplificato (Alternativa)**

#### File: `echo_client_simple.php`

```php
<?php
/**
 * Client PHP semplificato con fsockopen
 */

define('SERVER_HOST', 'localhost');
define('SERVER_PORT', 5555);

echo "=== CLIENT PHP SEMPLICE - ECHO ===\n";

// Connetti con fsockopen (più semplice)
$socket = fsockopen(SERVER_HOST, SERVER_PORT, $errno, $errstr, 10);
if (!$socket) {
    die("Errore connessione: $errstr ($errno)\n");
}

echo "✓ Connesso al server!\n";
echo "Digita 'exit' per disconnetterti\n\n";

// Loop di comunicazione
while (true) {
    echo "Inserisci messaggio: ";
    $messaggio = trim(fgets(STDIN));
    
    // Invia al server
    fwrite($socket, $messaggio . "\n");
    
    if (strtolower($messaggio) === 'exit') {
        echo "Disconnessione...\n";
        break;
    }
    
    // Ricevi echo
    $risposta = fgets($socket);
    echo "← Server: " . trim($risposta) . "\n\n";
}

fclose($socket);
echo "Connessione chiusa.\n";
?>
```

**Vantaggi versione semplificata:**
- 🎯 Più facile da capire
- 📦 Non richiede estensione sockets
- ✅ Funzioni standard PHP (fsockopen, fgets, fwrite)

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

### Test 2: Server + Client PHP

```bash
# Terminale 1: Server già avviato

# Terminale 2: Avvia client PHP
php echo_client.php

# Test:
# Client> Hello from PHP
# Server risponde> ECHO: Hello from PHP
```

### Test 3: Client Multipli Simultanei

```bash
# Terminale 1: Server attivo

# Terminale 2: Client Java #1
java EchoClientJava

# Terminale 3: Client Java #2
java EchoClientJava

# Terminale 4: Client PHP
php echo_client.php

# Tutti e 3 i client funzionano contemporaneamente!
```

---

## 📊 **TABELLA TEST**

| Test | Client | Messaggio | Risposta Attesa | Stato |
|------|--------|-----------|-----------------|-------|
| 1 | Java | "Test Java" | "ECHO: Test Java" | ✅ |
| 2 | PHP | "Test PHP" | "ECHO: Test PHP" | ✅ |
| 3 | Java | "exit" | Disconnessione | ✅ |
| 4 | 3 client simultanei | Vari | Tutti ricevono echo | ✅ |

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
3. `echo_client.php` (o versione simple)
4. **Screenshot** che mostrano:
   - Server con 2+ client connessi
   - Client Java che invia/riceve
   - Client PHP che invia/riceve
   - Log del server con entrambi i client

---

## 🏆 **CRITERI DI VALUTAZIONE**

| Criterio | Punti | Descrizione |
|----------|-------|-------------|
| **Server Multithreading** | 30 | Gestisce client multipli |
| **Client Java** | 20 | Funziona correttamente |
| **Client PHP** | 30 | Comunica con server Java |
| **Testing** | 15 | Screenshot di test multipli |
| **Documentazione** | 5 | Commenti e README |

**Totale:** 100 punti

---

## 🚀 **GUIDA RAPIDA**

### Compilazione e Avvio

```bash
# 1. Compila Java
javac EchoServer.java
javac EchoClientJava.java

# 2. Avvia Server (terminale 1)
java EchoServer

# 3. Avvia Client Java (terminale 2)
java EchoClientJava

# 4. Avvia Client PHP (terminale 3)
php echo_client.php
# OPPURE versione semplice:
php echo_client_simple.php
```

### Troubleshooting PHP

**Problema: "Call to undefined function socket_create()"**
```bash
# Soluzione 1: Usa versione simple (fsockopen)
php echo_client_simple.php

# Soluzione 2: Installa estensione
sudo apt install php-sockets  # Linux
brew install php             # macOS (include sockets)
```

**Verifica estensione socket:**
```bash
php -m | grep socket
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
