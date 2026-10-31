const dgram = require('dgram');

/**
 * Esempio 02.07 - Multicast UDP Sender (JavaScript/Node.js)
 * 
 * Questo esempio dimostra come inviare pacchetti multicast UDP con Node.js.
 * Il multicast permette di inviare dati a un gruppo specifico di destinatari.
 * 
 * Caratteristiche:
 * - Invio di messaggi multicast periodici
 * - Utilizzo di indirizzi multicast (224.0.0.0 - 239.255.255.255)
 * - Configurazione TTL (Time To Live)
 * 
 * Esecuzione: node multicast-sender.js [indirizzo] [porta] [intervallo] [ttl]
 * Esempio: node multicast-sender.js 239.255.0.1 5000 2000 1
 */

// ==================== CONFIGURAZIONE ====================

const args = process.argv.slice(2);
const DEFAULT_MULTICAST_ADDRESS = '239.255.0.1';
const DEFAULT_PORT = 5000;
const DEFAULT_INTERVAL = 2000; // 2 secondi
const DEFAULT_TTL = 1; // Solo rete locale

const multicastAddress = args[0] || DEFAULT_MULTICAST_ADDRESS;
const port = parseInt(args[1]) || DEFAULT_PORT;
const interval = parseInt(args[2]) || DEFAULT_INTERVAL;
const ttl = parseInt(args[3]) || DEFAULT_TTL;

// ==================== FUNZIONI UTILITY ====================

/**
 * Valida se l'indirizzo è multicast
 */
function isValidMulticastAddress(address) {
    const parts = address.split('.').map(Number);
    if (parts.length !== 4) return false;
    
    // Range multicast: 224.0.0.0 - 239.255.255.255
    const firstOctet = parts[0];
    return firstOctet >= 224 && firstOctet <= 239;
}

/**
 * Restituisce descrizione del TTL
 */
function getTTLDescription(ttl) {
    if (ttl === 0) return 'Solo questo host';
    if (ttl === 1) return 'Rete locale';
    if (ttl <= 32) return 'Stesso sito/organizzazione';
    if (ttl <= 64) return 'Stessa regione';
    if (ttl <= 128) return 'Stesso continente';
    return 'Globale';
}

/**
 * Restituisce l'ora corrente formattata
 */
function getCurrentTime() {
    const now = new Date();
    return now.toLocaleTimeString('it-IT');
}

/**
 * Crea un messaggio multicast
 */
function createMulticastMessage(count) {
    return `MULTICAST|Messaggio #${count}|Timestamp: ${Date.now()}|TTL: ${ttl}`;
}

// ==================== VALIDAZIONE ====================

if (!isValidMulticastAddress(multicastAddress)) {
    console.error('❌ Indirizzo multicast non valido:', multicastAddress);
    console.error('   Deve essere nel range 224.0.0.0 - 239.255.255.255');
    process.exit(1);
}

// ==================== MAIN ====================

console.log('═'.repeat(55));
console.log('📡 MULTICAST UDP SENDER (Node.js)');
console.log('═'.repeat(55));
console.log(`🔌 Porta: ${port}`);
console.log(`📡 Indirizzo Multicast: ${multicastAddress}`);
console.log(`⏱️  Intervallo: ${interval}ms`);
console.log(`🌐 TTL: ${ttl} (${getTTLDescription(ttl)})`);
console.log('═'.repeat(55));
console.log();

// Crea socket UDP
const socket = dgram.createSocket({ type: 'udp4', reuseAddr: true });

let messageCount = 0;
let intervalId;

socket.on('error', (err) => {
    console.error(`❌ Errore socket: ${err.message}`);
    socket.close();
    process.exit(1);
});

socket.bind(() => {
    // Configura TTL per multicast
    socket.setMulticastTTL(ttl);
    
    // Join al gruppo multicast (opzionale per il sender, ma utile)
    socket.addMembership(multicastAddress);
    
    console.log('✅ Socket creato e configurato per multicast');
    console.log('🚀 Inizio invio messaggi multicast...');
    console.log();
    console.log('Premi Ctrl+C per terminare');
    console.log();
    
    // Invia il primo messaggio immediatamente
    sendMulticastMessage();
    
    // Configura invio periodico
    intervalId = setInterval(sendMulticastMessage, interval);
});

/**
 * Invia un messaggio multicast
 */
function sendMulticastMessage() {
    messageCount++;
    
    const message = createMulticastMessage(messageCount);
    const buffer = Buffer.from(message);
    
    socket.send(buffer, 0, buffer.length, port, multicastAddress, (err) => {
        if (err) {
            console.error(`❌ Errore invio messaggio #${messageCount}: ${err.message}`);
        } else {
            console.log(
                `📤 [${getCurrentTime()}] Inviato messaggio #${messageCount} ` +
                `a ${multicastAddress}:${port} (${buffer.length} bytes)`
            );
        }
    });
}

// ==================== GESTIONE CHIUSURA ====================

function cleanup() {
    console.log('\n\n👋 Chiusura sender...');
    
    if (intervalId) {
        clearInterval(intervalId);
    }
    
    // Leave dal gruppo multicast
    try {
        socket.dropMembership(multicastAddress);
        console.log(`👋 Lasciato gruppo multicast ${multicastAddress}`);
    } catch (err) {
        console.error('⚠️  Errore nel lasciare il gruppo:', err.message);
    }
    
    socket.close();
    console.log('👋 Sender terminato\n');
    process.exit(0);
}

// Gestione segnali di terminazione
process.on('SIGINT', cleanup);
process.on('SIGTERM', cleanup);
