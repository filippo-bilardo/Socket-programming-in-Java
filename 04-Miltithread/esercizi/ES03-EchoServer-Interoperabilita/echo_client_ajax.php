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