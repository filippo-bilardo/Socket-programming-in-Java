# Dispenser: estensione web PHP

Completa prima [l'esercizio b — Dispenser](README.md). Il server Java, il protocollo e i client Java sono quelli della guida base: non creare una seconda versione del server. Il negozio usa una sessione persistente NEGOZIO → SERVI/STATO → QUIT. Anche un comando SERVI a coda vuota deve essere mostrato come errore.

Richiesti PHP con estensione sockets e un terminale separato per il server Java. Questo approfondimento è facoltativo.

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
    private $socket = null;
    
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
        
        // Letture e scritture hanno una scadenza; la connessione resta gestita dal sistema operativo.
        socket_set_option($this->socket, SOL_SOCKET, SO_RCVTIMEO, ['sec' => 5, 'usec' => 0]);
        socket_set_option($this->socket, SOL_SOCKET, SO_SNDTIMEO, ['sec' => 5, 'usec' => 0]);
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
        $wire = trim($command) . "\n";
        // Una scrittura può essere parziale: completa tutti i byte della riga.
        for ($sent = 0; $sent < strlen($wire); $sent += $n) {
            $n = socket_write($this->socket, substr($wire, $sent));
            if ($n === false || $n === 0) throw new RuntimeException('Invio incompleto');
        }
    }
    
    /**
     * Riceve risposta dal server
     */
    private function receiveResponse() {
        $response = '';
        do {
            $buffer = socket_read($this->socket, 1024, PHP_NORMAL_READ);
            if ($buffer === false || $buffer === '') {
                throw new RuntimeException('EOF o timeout prima della risposta completa');
            }
            $response .= $buffer;
        } while (substr($response, -1) !== "\n");
        $response = trim($response);
        if (strpos($response, 'ERRORE:') === 0) throw new RuntimeException(substr($response, 7));
        if ($response === '') throw new RuntimeException('Risposta assente');
        return $response;
    }
    
    /**
     * Identifica la sessione come negozio (senza password)
     */
    public function identificaNegozio() {
        $this->sendCommand("NEGOZIO");
        $response = $this->receiveResponse();
        
        if ($response !== "NEGOZIO_OK") {
            throw new Exception("Identificazione negozio fallita: " . $response);
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
            $this->socket = null;
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
        $client->identificaNegozio();
        
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
                    $messaggio = "Ultimo numero completato: " . $result['servizio'];
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
                    <h3>Ultimo completato</h3>
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
    $client->identificaNegozio();
    
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
    
    $client->identificaNegozio();
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
        $client->identificaNegozio();
        
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


## Connessioni PHP e significato dello stato

Il metodo `identificaNegozio()` invia NEGOZIO: non verifica una password. Ogni richiesta HTTP PHP apre una propria connessione, la identifica, invia i comandi e la chiude. Una variabile PHP non mantiene il socket tra due richieste del browser; la sessione persistente descritta nella guida base è quella del client Java `NegozioClient` o dei comandi eseguiti sulla stessa istanza PHP nella singola richiesta.

Il campo `servizio` rappresenta l'ultimo numero completato, come nel server Java; non è il prossimo numero da assegnare. STATO non modifica i contatori. I client PHP completano tutte le scritture e leggono fino alla newline; una risposta incompleta o ERRORE non è un'operazione riuscita. Gli aggiornamenti AJAX effettuano richieste periodiche, non ricevono notifiche push.

[Indice degli esercizi](../README.md) · [Guida sullo stato condiviso](../../03-Sincronizzazione-Risorse-Condivise.md)
