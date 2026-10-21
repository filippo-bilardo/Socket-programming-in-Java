<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Server Log - Echo Server</title>
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
            max-width: 1200px;
            margin: 0 auto;
        }
        
        .header {
            background: white;
            border-radius: 15px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            padding: 30px 40px;
            margin-bottom: 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 20px;
        }
        
        .header h1 {
            color: #667eea;
            font-size: 2em;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .header-controls {
            display: flex;
            gap: 10px;
        }
        
        .btn {
            padding: 10px 20px;
            border: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
            text-decoration: none;
            display: inline-block;
        }
        
        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        
        .btn-success {
            background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
            color: white;
        }
        
        .btn-secondary {
            background: #6c757d;
            color: white;
        }
        
        .btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 20px rgba(0,0,0,0.2);
        }
        
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        
        .stat-card {
            background: white;
            border-radius: 10px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
            padding: 25px;
            text-align: center;
        }
        
        .stat-icon {
            font-size: 2.5em;
            margin-bottom: 10px;
        }
        
        .stat-value {
            font-size: 2em;
            font-weight: bold;
            color: #667eea;
            margin-bottom: 5px;
        }
        
        .stat-label {
            color: #666;
            font-size: 0.9em;
        }
        
        .log-panel {
            background: white;
            border-radius: 15px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            padding: 30px;
        }
        
        .log-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
            flex-wrap: wrap;
            gap: 15px;
        }
        
        .log-header h2 {
            color: #667eea;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .filter-controls {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }
        
        .filter-btn {
            padding: 8px 15px;
            border: 2px solid #e0e0e0;
            background: white;
            border-radius: 6px;
            cursor: pointer;
            transition: all 0.3s;
            font-size: 13px;
        }
        
        .filter-btn:hover {
            border-color: #667eea;
            color: #667eea;
        }
        
        .filter-btn.active {
            background: #667eea;
            color: white;
            border-color: #667eea;
        }
        
        .terminal {
            background: #1e1e1e;
            color: #00ff00;
            padding: 25px;
            border-radius: 10px;
            font-family: 'Courier New', monospace;
            font-size: 14px;
            max-height: 600px;
            overflow-y: auto;
            line-height: 1.6;
        }
        
        .log-entry {
            margin-bottom: 10px;
            padding: 8px;
            border-left: 3px solid transparent;
            transition: all 0.2s;
        }
        
        .log-entry:hover {
            background: rgba(255,255,255,0.05);
        }
        
        .log-entry.connection {
            color: #00bfff;
            border-left-color: #00bfff;
        }
        
        .log-entry.message {
            color: #00ff00;
            border-left-color: #00ff00;
        }
        
        .log-entry.echo {
            color: #ffa500;
            border-left-color: #ffa500;
        }
        
        .log-entry.disconnect {
            color: #ff6b6b;
            border-left-color: #ff6b6b;
        }
        
        .log-entry.error {
            color: #ff0000;
            border-left-color: #ff0000;
            background: rgba(255,0,0,0.1);
        }
        
        .timestamp {
            color: #888;
            font-size: 0.9em;
        }
        
        .empty-state {
            text-align: center;
            padding: 60px 20px;
            color: #666;
        }
        
        .empty-state-icon {
            font-size: 4em;
            margin-bottom: 20px;
            opacity: 0.3;
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
        
        .client-list {
            background: #f8f9fa;
            border-radius: 8px;
            padding: 15px;
            margin-top: 20px;
        }
        
        .client-item {
            display: flex;
            justify-content: space-between;
            padding: 10px;
            background: white;
            border-radius: 6px;
            margin-bottom: 10px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        }
        
        .client-item:last-child {
            margin-bottom: 0;
        }
        
        .status-badge {
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 0.85em;
            font-weight: 600;
        }
        
        .status-online {
            background: #d4edda;
            color: #155724;
        }
        
        .status-offline {
            background: #f8d7da;
            color: #721c24;
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="header">
            <h1>📋 Server Log Monitor</h1>
            <div class="header-controls">
                <button class="btn btn-success" onclick="location.reload()">🔄 Refresh</button>
                <a href="index.php" class="btn btn-secondary">← Dashboard</a>
            </div>
        </div>
        
        <?php
        // Configurazione
        $serverHost = 'localhost';
        $serverPort = 5555;
        
        // Verifica stato server
        $serverOnline = false;
        $socket = @socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
        if ($socket !== false) {
            socket_set_option($socket, SOL_SOCKET, SO_RCVTIMEO, array('sec' => 1, 'usec' => 0));
            if (@socket_connect($socket, $serverHost, $serverPort)) {
                $serverOnline = true;
                socket_close($socket);
            }
        }
        
        // Simula statistiche (in un caso reale, queste verrebbero da un database o file log)
        $stats = [
            'clients_connected' => $serverOnline ? rand(0, 5) : 0,
            'total_connections' => rand(15, 50),
            'messages_sent' => rand(100, 500),
            'uptime' => rand(30, 1440) // minuti
        ];
        
        // Genera log simulati (in un caso reale, questi verrebbero letti da un file)
        $logs = [];
        if ($serverOnline) {
            $logs[] = [
                'time' => date('H:i:s'),
                'type' => 'connection',
                'message' => '[CONNESSIONE] Client #1 da 127.0.0.1'
            ];
            $logs[] = [
                'time' => date('H:i:s', strtotime('-2 minutes')),
                'type' => 'message',
                'message' => '[CLIENT #1] Ricevuto: Hello Server!'
            ];
            $logs[] = [
                'time' => date('H:i:s', strtotime('-2 minutes')),
                'type' => 'echo',
                'message' => '[CLIENT #1] Inviato echo'
            ];
            $logs[] = [
                'time' => date('H:i:s', strtotime('-5 minutes')),
                'type' => 'connection',
                'message' => '[CONNESSIONE] Client #2 da 127.0.0.1'
            ];
            $logs[] = [
                'time' => date('H:i:s', strtotime('-6 minutes')),
                'type' => 'disconnect',
                'message' => '[CLIENT #2] Disconnessione'
            ];
        }
        
        // Formatta uptime
        $hours = floor($stats['uptime'] / 60);
        $minutes = $stats['uptime'] % 60;
        $uptime_str = $hours > 0 ? "{$hours}h {$minutes}m" : "{$minutes}m";
        ?>
        
        <!-- Statistics Grid -->
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon">👥</div>
                <div class="stat-value"><?php echo $stats['clients_connected']; ?></div>
                <div class="stat-label">Client Connessi</div>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon">🔌</div>
                <div class="stat-value"><?php echo $stats['total_connections']; ?></div>
                <div class="stat-label">Connessioni Totali</div>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon">💬</div>
                <div class="stat-value"><?php echo $stats['messages_sent']; ?></div>
                <div class="stat-label">Messaggi Processati</div>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon">⏱️</div>
                <div class="stat-value"><?php echo $uptime_str; ?></div>
                <div class="stat-label">Uptime Server</div>
            </div>
        </div>
        
        <?php if (!$serverOnline): ?>
        <div class="alert alert-warning">
            <strong>⚠️ Server Offline</strong><br>
            Il server Java non è attualmente in esecuzione. I log mostrati sono simulati a scopo dimostrativo.
            Per avviare il server, esegui:
            <div style="margin-top: 10px; background: rgba(0,0,0,0.1); padding: 10px; border-radius: 5px; font-family: monospace;">
                javac EchoServer.java<br>
                java EchoServer
            </div>
        </div>
        <?php else: ?>
        <div class="alert alert-info">
            <strong>✅ Server Online</strong><br>
            Il server è attivo su <?php echo $serverHost; ?>:<?php echo $serverPort; ?>
            <span style="float: right;">Ultimo aggiornamento: <?php echo date('H:i:s'); ?></span>
        </div>
        <?php endif; ?>
        
        <!-- Log Panel -->
        <div class="log-panel">
            <div class="log-header">
                <h2>📜 Log Attività Server</h2>
                <div class="filter-controls">
                    <button class="filter-btn active" onclick="filterLog('all')">Tutti</button>
                    <button class="filter-btn" onclick="filterLog('connection')">Connessioni</button>
                    <button class="filter-btn" onclick="filterLog('message')">Messaggi</button>
                    <button class="filter-btn" onclick="filterLog('error')">Errori</button>
                </div>
            </div>
            
            <div class="terminal" id="logTerminal">
                <?php if (empty($logs)): ?>
                    <div class="empty-state">
                        <div class="empty-state-icon">📭</div>
                        <p>Nessun log disponibile.</p>
                        <p style="font-size: 0.9em; margin-top: 10px;">
                            Avvia il server Java e connetti alcuni client per vedere i log qui.
                        </p>
                    </div>
                <?php else: ?>
                    <div style="color: #888; margin-bottom: 15px;">
                        === SERVER ECHO MULTITHREADING ===<br>
                        Server avviato sulla porta <?php echo $serverPort; ?><br>
                        In attesa di connessioni...<br>
                        <br>
                    </div>
                    <?php foreach (array_reverse($logs) as $log): ?>
                        <div class="log-entry <?php echo $log['type']; ?>" data-type="<?php echo $log['type']; ?>">
                            <span class="timestamp">[<?php echo $log['time']; ?>]</span>
                            <?php echo htmlspecialchars($log['message']); ?>
                        </div>
                    <?php endforeach; ?>
                <?php endif; ?>
            </div>
            
            <?php if ($serverOnline && $stats['clients_connected'] > 0): ?>
            <div class="client-list">
                <h3 style="margin-bottom: 15px; color: #667eea;">👥 Client Attivi</h3>
                <?php for ($i = 1; $i <= $stats['clients_connected']; $i++): ?>
                <div class="client-item">
                    <div>
                        <strong>Client #<?php echo $i; ?></strong>
                        <span style="color: #666; margin-left: 10px;">127.0.0.1</span>
                    </div>
                    <span class="status-badge status-online">● Online</span>
                </div>
                <?php endfor; ?>
            </div>
            <?php endif; ?>
        </div>
        
        <div style="margin-top: 20px; text-align: center; color: white;">
            <p style="font-size: 0.9em; opacity: 0.8;">
                💡 <strong>Nota:</strong> In un ambiente di produzione, i log verrebbero letti da un file di log del server Java.<br>
                Per implementarlo, configura il server Java per scrivere su un file e leggi quel file in questa pagina.
            </p>
        </div>
    </div>
    
    <script>
        function filterLog(type) {
            const entries = document.querySelectorAll('.log-entry');
            const buttons = document.querySelectorAll('.filter-btn');
            
            // Update button states
            buttons.forEach(btn => btn.classList.remove('active'));
            event.target.classList.add('active');
            
            // Filter entries
            entries.forEach(entry => {
                if (type === 'all' || entry.dataset.type === type) {
                    entry.style.display = 'block';
                } else {
                    entry.style.display = 'none';
                }
            });
        }
        
        // Auto-scroll to bottom
        const terminal = document.getElementById('logTerminal');
        if (terminal) {
            terminal.scrollTop = terminal.scrollHeight;
        }
        
        // Auto-refresh ogni 5 secondi
        setTimeout(() => {
            location.reload();
        }, 5000);
    </script>
</body>
</html>
