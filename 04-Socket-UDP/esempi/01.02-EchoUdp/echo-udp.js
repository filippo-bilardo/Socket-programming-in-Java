const dgram = require('dgram');
const readline = require('readline');

/**
 * Nome dell'Esempio: Echo UDP Server e Client (JavaScript/Node.js)
 * Guida di Riferimento: 01-Socket-UDP-Base.md
 * 
 * Obiettivo: Dimostrare comunicazione UDP bidirezionale con gestione errori.
 * 
 * Spiegazione:
 * 1. Server UDP che rimanda indietro i messaggi ricevuti
 * 2. Client UDP che invia messaggi e riceve risposte
 * 3. Gestione timeout e pacchetti persi
 * 
 * @author Socket Programming Course
 * @version 1.0
 */

// ==================== SERVER UDP ECHO ====================

class EchoServer {
    constructor(port) {
        this.port = port;
        this.socket = null;
        this.running = false;
    }
    
    start() {
        this.socket = dgram.createSocket('udp4');
        this.running = true;
        
        this.socket.on('error', (err) => {
            if (this.running) {
                console.error(`❌ Errore server: ${err.message}`);
            }
            this.socket.close();
        });
        
        this.socket.on('listening', () => {
            const address = this.socket.address();
            console.log('🚀 Server UDP Echo avviato su porta ' + address.port);
            console.log('📦 Dimensione buffer: 1024 byte');
            console.log('🛑 Premi Ctrl+C per fermare');
            console.log('='.repeat(50));
        });
        
        this.socket.on('message', (msg, remote) => {
            const received = msg.toString();
            console.log(`📨 Ricevuto da ${remote.address}:${remote.port} → ${received}`);
            
            // Prepara risposta (echo + timestamp)
            const response = `ECHO: ${received} [${Date.now()}]`;
            const responseBuffer = Buffer.from(response);
            
            // Invia risposta
            this.socket.send(responseBuffer, 0, responseBuffer.length, remote.port, remote.address, (err) => {
                if (err) {
                    console.error(`❌ Errore invio risposta: ${err.message}`);
                } else {
                    console.log(`📤 Risposta inviata: ${response}`);
                }
            });
        });
        
        // Binding sulla porta
        this.socket.bind(this.port);
    }
    
    stop() {
        this.running = false;
        if (this.socket) {
            this.socket.close();
            console.log('🔒 Server fermato');
        }
    }
}

// ==================== CLIENT UDP ====================

class EchoClient {
    constructor(serverHost, serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.socket = null;
    }
    
    sendMessage(message, timeoutMs) {
        return new Promise((resolve, reject) => {
            if (!this.socket) {
                this.socket = dgram.createSocket('udp4');
            }
            
            let timeoutId;
            let responseReceived = false;
            
            // Gestione timeout
            timeoutId = setTimeout(() => {
                if (!responseReceived) {
                    console.error(`⏰ Timeout - nessuna risposta in ${timeoutMs}ms`);
                    reject(new Error('Timeout'));
                }
            }, timeoutMs);
            
            // Listener per la risposta
            const onMessage = (msg, remote) => {
                if (!responseReceived) {
                    responseReceived = true;
                    clearTimeout(timeoutId);
                    
                    const response = msg.toString();
                    console.log(`📨 Risposta: ${response}`);
                    
                    resolve(response);
                }
            };
            
            // Registra listener
            this.socket.once('message', onMessage);
            
            // Gestione errori
            this.socket.once('error', (err) => {
                clearTimeout(timeoutId);
                console.error(`❌ Errore: ${err.message}`);
                reject(err);
            });
            
            // Prepara e invia pacchetto
            const buffer = Buffer.from(message);
            console.log(`📤 Invio: ${message}`);
            
            this.socket.send(buffer, 0, buffer.length, this.serverPort, this.serverHost, (err) => {
                if (err) {
                    clearTimeout(timeoutId);
                    console.error(`❌ Errore invio: ${err.message}`);
                    reject(err);
                }
            });
        });
    }
    
    close() {
        if (this.socket) {
            this.socket.close();
        }
    }
    
    async interactive() {
        console.log('💬 Modalità interattiva avviata');
        console.log('💡 Digita \'quit\' per uscire');
        console.log('-'.repeat(30));
        
        const rl = readline.createInterface({
            input: process.stdin,
            output: process.stdout,
            prompt: 'Tu: '
        });
        
        let messageCount = 0;
        
        rl.prompt();
        
        rl.on('line', async (input) => {
            const trimmed = input.trim();
            
            if (trimmed.toLowerCase() === 'quit') {
                console.log('👋 Disconnessione...');
                rl.close();
                this.close();
                process.exit(0);
                return;
            }
            
            if (trimmed === '') {
                rl.prompt();
                return;
            }
            
            try {
                messageCount++;
                const messageWithId = `[#${messageCount}] ${trimmed}`;
                await this.sendMessage(messageWithId, 5000);
            } catch (err) {
                if (err.message === 'Timeout') {
                    console.error('⚠️ Messaggio perso (timeout)');
                } else {
                    console.error(`❌ Errore invio: ${err.message}`);
                }
            }
            
            console.log();
            rl.prompt();
        });
        
        rl.on('close', () => {
            console.log('👋 Disconnessione...');
            this.close();
            process.exit(0);
        });
    }
}

// ==================== MAIN ====================

function printUsage() {
    console.log('🛠️ Echo UDP - Server e Client (Node.js)');
    console.log('Utilizzo:');
    console.log('  node echo-udp.js server <porta>');
    console.log('  node echo-udp.js client <host> <porta>');
    console.log();
    console.log('Esempi:');
    console.log('  node echo-udp.js server 9999');
    console.log('  node echo-udp.js client localhost 9999');
}

async function main() {
    const args = process.argv.slice(2);
    
    if (args.length === 0) {
        printUsage();
        process.exit(0);
    }
    
    const mode = args[0].toLowerCase();
    
    if (mode === 'server') {
        if (args.length < 2) {
            console.error('❌ Porta richiesta per il server');
            process.exit(1);
        }
        
        const port = parseInt(args[1]);
        
        if (isNaN(port)) {
            console.error(`❌ Porta non valida: ${args[1]}`);
            process.exit(1);
        }
        
        const server = new EchoServer(port);
        
        // Shutdown hook per chiusura pulita
        process.on('SIGINT', () => {
            console.log();
            server.stop();
            process.exit(0);
        });
        
        process.on('SIGTERM', () => {
            server.stop();
            process.exit(0);
        });
        
        server.start();
        
    } else if (mode === 'client') {
        if (args.length < 3) {
            console.error('❌ Host e porta richiesti per il client');
            process.exit(1);
        }
        
        const host = args[1];
        const port = parseInt(args[2]);
        
        if (isNaN(port)) {
            console.error(`❌ Porta non valida: ${args[2]}`);
            process.exit(1);
        }
        
        console.log('🔗 Client UDP Echo');
        console.log(`Target: ${host}:${port}`);
        console.log('='.repeat(30));
        
        const client = new EchoClient(host, port);
        
        // Test singolo messaggio
        try {
            await client.sendMessage('Test di connettività', 3000);
            console.log('✅ Connessione OK\n');
            
            // Modalità interattiva
            await client.interactive();
            
        } catch (err) {
            console.error(`💥 Test connessione fallito: ${err.message}`);
            client.close();
            process.exit(1);
        }
        
    } else {
        console.error(`❌ Modalità non riconosciuta: ${mode}`);
        console.log('Modalità disponibili: server, client');
        process.exit(1);
    }
}

// Avvia l'applicazione
main().catch((err) => {
    console.error('💥 Errore fatale:', err);
    process.exit(1);
});
