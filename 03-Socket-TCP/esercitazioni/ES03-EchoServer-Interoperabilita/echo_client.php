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