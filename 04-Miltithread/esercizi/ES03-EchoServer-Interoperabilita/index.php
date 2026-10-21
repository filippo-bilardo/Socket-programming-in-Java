<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Echo Server - Dashboard</title>
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
            max-width: 1000px;
            margin: 0 auto;
        }
        
        .header {
            background: white;
            border-radius: 15px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            padding: 40px;
            margin-bottom: 30px;
            text-align: center;
        }
        
        .header h1 {
            color: #667eea;
            font-size: 2.5em;
            margin-bottom: 10px;
        }
        
        .header p {
            color: #666;
            font-size: 1.1em;
        }
        
        .status-panel {
            background: white;
            border-radius: 15px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            padding: 30px;
            margin-bottom: 30px;
        }
        
        .status-panel h2 {
            color: #667eea;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .status-indicator {
            display: flex;
            align-items: center;
            gap: 15px;
            padding: 20px;
            background: #f8f9fa;
            border-radius: 10px;
            margin-bottom: 20px;
        }
        
        .status-dot {
            width: 20px;
            height: 20px;
            border-radius: 50%;
            animation: pulse 2s infinite;
        }
        
        .status-dot.online {
            background: #28a745;
            box-shadow: 0 0 20px rgba(40, 167, 69, 0.5);
        }
        
        .status-dot.offline {
            background: #dc3545;
            animation: none;
        }
        
        @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.5; }
        }
        
        .status-text {
            flex: 1;
        }
        
        .status-text h3 {
            color: #333;
            margin-bottom: 5px;
        }
        
        .status-text p {
            color: #666;
            font-size: 0.9em;
        }
        
        .server-controls {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }
        
        .btn {
            padding: 12px 30px;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
            text-decoration: none;
            display: inline-block;
        }
        
        .btn-primary {
            background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
            color: white;
        }
        
        .btn-danger {
            background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);
            color: white;
        }
        
        .btn-info {
            background: linear-gradient(135deg, #17a2b8 0%, #138496 100%);
            color: white;
        }
        
        .btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 20px rgba(0,0,0,0.2);
        }
        
        .btn:disabled {
            opacity: 0.5;
            cursor: not-allowed;
            transform: none;
        }
        
        .clients-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        
        .client-card {
            background: white;
            border-radius: 15px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
            padding: 30px;
            text-align: center;
            transition: transform 0.3s;
        }
        
        .client-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 15px 40px rgba(0,0,0,0.3);
        }
        
        .client-card h3 {
            color: #667eea;
            margin-bottom: 15px;
            font-size: 1.5em;
        }
        
        .client-card p {
            color: #666;
            margin-bottom: 20px;
            line-height: 1.6;
        }
        
        .client-icon {
            font-size: 4em;
            margin-bottom: 15px;
        }
        
        .info-box {
            background: white;
            border-radius: 15px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
            padding: 30px;
        }
        
        .info-box h3 {
            color: #667eea;
            margin-bottom: 15px;
        }
        
        .info-item {
            display: flex;
            justify-content: space-between;
            padding: 10px 0;
            border-bottom: 1px solid #e0e0e0;
        }
        
        .info-item:last-child {
            border-bottom: none;
        }
        
        .info-label {
            color: #666;
            font-weight: 600;
        }
        
        .info-value {
            color: #333;
        }
        
        .terminal-output {
            background: #1e1e1e;
            color: #00ff00;
            padding: 20px;
            border-radius: 10px;
            font-family: 'Courier New', monospace;
            max-height: 300px;
            overflow-y: auto;
            margin-top: 20px;
        }
        
        .terminal-output pre {
            margin: 0;
            white-space: pre-wrap;
        }
        
        .alert {
            padding: 15px 20px;
            border-radius: 8px;
            margin-bottom: 20px;
        }
        
        .alert-info {
            background: #d1ecf1;
            color: #0c5460;
            border-left: 4px solid #17a2b8;
        }
        
        .alert-warning {
            background: #fff3cd;
            color: #856404;
            border-left: 4px solid #ffc107;
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="header">
            <h1>🚀 Echo Server Dashboard</h1>
            <p>Pannello di controllo per Server Java Multithreading e Client PHP</p>
        </div>
        
        <!-- Server Status Panel -->
        <div class="status-panel">
            <h2>⚙️ Stato Server</h2>
            
            <?php
            // Verifica se il server è attivo
            $serverHost = 'localhost';
            $serverPort = 5555;
            $serverOnline = false;
            
            $socket = @socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
            if ($socket !== false) {
                socket_set_option($socket, SOL_SOCKET, SO_RCVTIMEO, array('sec' => 1, 'usec' => 0));
                if (@socket_connect($socket, $serverHost, $serverPort)) {
                    $serverOnline = true;
                    socket_close($socket);
                }
            }
            ?>
            
            <div class="status-indicator">
                <div class="status-dot <?php echo $serverOnline ? 'online' : 'offline'; ?>"></div>
                <div class="status-text">
                    <h3><?php echo $serverOnline ? '✅ Server Online' : '⚠️ Server Offline'; ?></h3>
                    <p>
                        <?php 
                        if ($serverOnline) {
                            echo "Server Java è attivo su $serverHost:$serverPort";
                        } else {
                            echo "Server non raggiungibile. Avviare il server Java manualmente.";
                        }
                        ?>
                    </p>
                </div>
            </div>
            
            <?php if (!$serverOnline): ?>
            <div class="alert alert-warning">
                <strong>⚠️ Attenzione:</strong> Il server Java non è attivo. Per avviarlo, esegui i seguenti comandi in un terminale:
                <div class="terminal-output">
                    <pre>cd <?php echo dirname(__FILE__); ?>
javac EchoServer.java
java EchoServer</pre>
                </div>
            </div>
            <?php endif; ?>
            
            <div class="server-controls">
                <button class="btn btn-info" onclick="checkServer()">🔄 Verifica Stato</button>
                <a href="server_log.php" class="btn btn-info">📋 Visualizza Log</a>
            </div>
        </div>
        
        <!-- Client Cards -->
        <div class="clients-grid">
            <!-- Client Java -->
            <div class="client-card">
                <div class="client-icon">☕</div>
                <h3>Client Java</h3>
                <p>Client da terminale scritto in Java. Connessione diretta al server via socket TCP.</p>
                <div class="alert alert-info" style="text-align: left; font-size: 0.9em;">
                    <strong>Avvio da terminale:</strong>
                    <div class="terminal-output" style="margin-top: 10px; font-size: 0.8em;">
                        <pre>javac EchoClientJava.java
java EchoClientJava</pre>
                    </div>
                </div>
            </div>
            
            <!-- Client PHP Form -->
            <div class="client-card">
                <div class="client-icon">🌐</div>
                <h3>Client Web (Form)</h3>
                <p>Interfaccia web con form HTML. Invia messaggi e riceve echo dal server Java.</p>
                <a href="echo_client.php" class="btn btn-primary" <?php echo $serverOnline ? '' : 'style="opacity: 0.5; pointer-events: none;"'; ?>>
                    🚀 Apri Client
                </a>
            </div>
            
            <!-- Client PHP AJAX -->
            <div class="client-card">
                <div class="client-icon">⚡</div>
                <h3>Client AJAX</h3>
                <p>Client interattivo con comunicazione in tempo reale via AJAX. Log messaggi live.</p>
                <a href="echo_client_ajax.php" class="btn btn-primary" <?php echo $serverOnline ? '' : 'style="opacity: 0.5; pointer-events: none;"'; ?>>
                    🚀 Apri Client
                </a>
            </div>
        </div>
        
        <!-- Info Box -->
        <div class="info-box">
            <h3>ℹ️ Informazioni Sistema</h3>
            <div class="info-item">
                <span class="info-label">Server Host:</span>
                <span class="info-value"><?php echo $serverHost; ?></span>
            </div>
            <div class="info-item">
                <span class="info-label">Server Port:</span>
                <span class="info-value"><?php echo $serverPort; ?></span>
            </div>
            <div class="info-item">
                <span class="info-label">Protocollo:</span>
                <span class="info-value">TCP Socket</span>
            </div>
            <div class="info-item">
                <span class="info-label">PHP Version:</span>
                <span class="info-value"><?php echo phpversion(); ?></span>
            </div>
            <div class="info-item">
                <span class="info-label">Socket Extension:</span>
                <span class="info-value">
                    <?php echo extension_loaded('sockets') ? '✅ Installata' : '❌ Non installata'; ?>
                </span>
            </div>
            <div class="info-item">
                <span class="info-label">Directory:</span>
                <span class="info-value" style="font-size: 0.8em;"><?php echo dirname(__FILE__); ?></span>
            </div>
        </div>
        
        <!-- Help Section -->
        <div class="info-box" style="margin-top: 20px;">
            <h3>📚 Guida Rapida</h3>
            <div style="line-height: 2;">
                <p><strong>1. Avvia il Server Java:</strong></p>
                <div class="terminal-output">
                    <pre>javac EchoServer.java
java EchoServer</pre>
                </div>
                
                <p style="margin-top: 15px;"><strong>2. Avvia il Server Web PHP:</strong></p>
                <div class="terminal-output">
                    <pre>php -S localhost:8000</pre>
                </div>
                
                <p style="margin-top: 15px;"><strong>3. Apri il Browser:</strong></p>
                <div class="terminal-output">
                    <pre>http://localhost:8000/</pre>
                </div>
                
                <p style="margin-top: 15px;"><strong>4. Testa i Client:</strong></p>
                <ul style="margin-left: 20px; color: #666;">
                    <li>Clicca su "Client Web (Form)" per testare il form HTML</li>
                    <li>Clicca su "Client AJAX" per testare la comunicazione in tempo reale</li>
                    <li>Apri un terminale per testare il client Java</li>
                </ul>
            </div>
        </div>
    </div>
    
    <script>
        function checkServer() {
            location.reload();
        }
        
        // Auto-refresh status ogni 10 secondi
        setTimeout(() => {
            location.reload();
        }, 10000);
    </script>
</body>
</html>
