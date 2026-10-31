const dgram = require('dgram');

/**
 * Esempio 02.06 - Broadcast UDP Receiver (JavaScript/Node.js)
 * 
 * Questo esempio dimostra come ricevere pacchetti broadcast UDP con Node.js.
 * Il receiver ascolta su una porta specifica e riceve tutti i messaggi
 * broadcast inviati a quella porta sulla rete locale.
 * 
 * Caratteristiche:
 * - Ricezione di messaggi broadcast
 * - Visualizzazione informazioni mittente
 * - Parsing e validazione messaggi
 * 
 * Esecuzione: node broadcast-receiver.js [porta]
 * Esempio: node broadcast-receiver.js 5000
 */

// ==================== CONFIGURAZIONE ====================

const args = process.argv.slice(2);
const DEFAULT_PORT = 5000;

const port = parseInt(args[0]) || DEFAULT_PORT;

// ==================== FUNZIONI UTILITY ====================

/**
 * Restituisce l'ora corrente formattata
 */
function getCurrentTime() {
    const now = new Date();
    return now.toLocaleTimeString('it-IT');
}

/**
 * Parsing del messaggio broadcast
 * Formato atteso: BROADCAST|Messaggio #N|Timestamp: T
 */
function parseBroadcastMessage(message) {
    const parts = message.toString().split('|');
    
    if (parts.length >= 3 && parts[0] === 'BROADCAST') {
        return {
            type: 'BROADCAST',
            content: parts[1],
            timestamp: parts[2]
        };
    }
    
    return {
        type: 'UNKNOWN',
        content: message.toString()
    };
}

// ==================== MAIN ====================

console.log('═'.repeat(55));
console.log('📡 BROADCAST UDP RECEIVER (Node.js)');
console.log('═'.repeat(55));
console.log(`🔌 Porta: ${port}`);
console.log('═'.repeat(55));
console.log();

// Crea socket UDP
const socket = dgram.createSocket('udp4');

let messageCount = 0;

socket.on('error', (err) => {
    console.error(`❌ Errore socket: ${err.message}`);
    console.error('   Verifica che la porta non sia già in uso');
    socket.close();
    process.exit(1);
});

socket.on('listening', () => {
    const address = socket.address();
    console.log(`✅ Socket in ascolto su ${address.address}:${address.port}`);
    console.log('👂 In attesa di messaggi broadcast...');
    console.log();
    console.log('Premi Ctrl+C per terminare');
    console.log();
});

socket.on('message', (msg, remote) => {
    messageCount++;
    
    // Parsing del messaggio
    const parsed = parseBroadcastMessage(msg);
    
    // Visualizza informazioni
    console.log('╔' + '═'.repeat(53));
    console.log(`║ 📥 MESSAGGIO #${messageCount} RICEVUTO`);
    console.log('╠' + '═'.repeat(53));
    console.log(`║ ⏰ Ora: ${getCurrentTime()}`);
    console.log(`║ 👤 Mittente: ${remote.address}:${remote.port}`);
    console.log(`║ 📦 Dimensione: ${msg.length} bytes`);
    console.log('╠' + '═'.repeat(53));
    
    if (parsed.type === 'BROADCAST') {
        console.log(`║ 📝 Tipo: BROADCAST`);
        console.log(`║ 💬 Contenuto: ${parsed.content}`);
        console.log(`║ 🕐 ${parsed.timestamp}`);
    } else {
        console.log(`║ 💬 Contenuto: ${parsed.content}`);
    }
    
    console.log('╚' + '═'.repeat(53));
    console.log();
});

// Binding sulla porta
socket.bind(port);

// ==================== GESTIONE CHIUSURA ====================

function cleanup() {
    console.log('\n\n👋 Chiusura receiver...');
    socket.close();
    console.log('👋 Receiver terminato\n');
    process.exit(0);
}

// Gestione segnali di terminazione
process.on('SIGINT', cleanup);
process.on('SIGTERM', cleanup);
