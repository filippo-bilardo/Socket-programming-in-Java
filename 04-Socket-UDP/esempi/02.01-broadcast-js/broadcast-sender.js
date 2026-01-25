const dgram = require('dgram');

/**
 * Esempio 02.05 - Broadcast UDP Sender (JavaScript/Node.js)
 * 
 * Questo esempio dimostra come inviare pacchetti broadcast UDP con Node.js.
 * Il broadcast raggiunge tutti i dispositivi nella rete locale.
 * 
 * Caratteristiche:
 * - Invio di messaggi broadcast periodici
 * - Utilizzo dell'indirizzo di broadcast 255.255.255.255
 * - Configurazione setBroadcast(true) necessaria
 * 
 * Esecuzione: node broadcast-sender.js [porta] [intervallo_ms]
 * Esempio: node broadcast-sender.js 5000 2000
 */

// ==================== CONFIGURAZIONE ====================
// lettura argomenti da linea di comando
const args = process.argv.slice(2);
// costanti di default
const BROADCAST_ADDRESS = '255.255.255.255';
const DEFAULT_PORT = 5000;
const DEFAULT_INTERVAL = 2000; // 2 secondi

// setting da linea di comando o default
const port = parseInt(args[0]) || DEFAULT_PORT;
const interval = parseInt(args[1]) || DEFAULT_INTERVAL;

// ==================== FUNZIONI UTILITY ====================

/**
 * Restituisce l'ora corrente formattata
 */
function getCurrentTime() {
    const now = new Date();
    return now.toLocaleTimeString('it-IT');
}

/**
 * Crea un messaggio broadcast
 */
function createBroadcastMessage(count) {
    return `BROADCAST|Messaggio #${count}|Timestamp: ${Date.now()}`;
}

// ==================== MAIN ====================

console.log('═'.repeat(55));
console.log('📡 BROADCAST UDP SENDER (Node.js)');
console.log('═'.repeat(55));
console.log(`🔌 Porta: ${port}`);
console.log(`📡 Indirizzo Broadcast: ${BROADCAST_ADDRESS}`);
console.log(`⏱️  Intervallo: ${interval}ms`);
console.log('═'.repeat(55));
console.log();

// Crea socket UDP
const socket = dgram.createSocket('udp4');

let messageCount = 0;
let intervalId;

socket.on('error', (err) => {
    console.error(`❌ Errore socket: ${err.message}`);
    socket.close();
    process.exit(1);
});

socket.bind(() => {
    // IMPORTANTE: Abilita il broadcast
    socket.setBroadcast(true);
    
    console.log('✅ Socket creato e configurato per broadcast');
    console.log('🚀 Inizio invio messaggi broadcast...');
    console.log();
    console.log('Premi Ctrl+C per terminare');
    console.log();
    
    // Invia il primo messaggio immediatamente
    sendBroadcastMessage();
    
    // Configura invio periodico
    intervalId = setInterval(sendBroadcastMessage, interval);
});

/**
 * Invia un messaggio broadcast
 */
function sendBroadcastMessage() {
    messageCount++;
    
    const message = createBroadcastMessage(messageCount);
    const buffer = Buffer.from(message);
    
    socket.send(buffer, 0, buffer.length, port, BROADCAST_ADDRESS, (err) => {
        if (err) {
            console.error(`❌ Errore invio messaggio #${messageCount}: ${err.message}`);
        } else {
            console.log(`📤 [${getCurrentTime()}] Inviato messaggio #${messageCount} (${buffer.length} bytes)`);
        }
    });
}

// ==================== GESTIONE CHIUSURA ====================

function cleanup() {
    console.log('\n\n👋 Chiusura sender...');
    
    if (intervalId) {
        clearInterval(intervalId);
    }
    
    socket.close();
    console.log('👋 Sender terminato\n');
    process.exit(0);
}

// Gestione segnali di terminazione 
process.on('SIGINT', cleanup);  // Ctrl+C
process.on('SIGTERM', cleanup); // kill command
