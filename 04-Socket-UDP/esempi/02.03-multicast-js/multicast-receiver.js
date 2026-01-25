const dgram = require('dgram');

/**
 * Esempio 02.08 - Multicast UDP Receiver (JavaScript/Node.js)
 * 
 * Questo esempio dimostra come ricevere pacchetti multicast UDP con Node.js.
 * Il receiver si unisce a un gruppo multicast e riceve tutti i messaggi
 * inviati a quel gruppo.
 * 
 * Caratteristiche:
 * - Ricezione di messaggi multicast
 * - Join e leave da gruppi multicast
 * - Visualizzazione informazioni mittente
 * - Parsing e validazione messaggi
 * 
 * Esecuzione: node multicast-receiver.js [indirizzo_multicast] [porta]
 * Esempio: node multicast-receiver.js 239.255.0.1 5000
 */

// ==================== CONFIGURAZIONE ====================

const args = process.argv.slice(2);
const DEFAULT_MULTICAST_ADDRESS = '239.255.0.1';
const DEFAULT_PORT = 5000;

const multicastAddress = args[0] || DEFAULT_MULTICAST_ADDRESS;
const port = parseInt(args[1]) || DEFAULT_PORT;

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
 * Restituisce l'ora corrente formattata
 */
function getCurrentTime() {
    const now = new Date();
    return now.toLocaleTimeString('it-IT');
}

/**
 * Parsing del messaggio multicast
 * Formato atteso: MULTICAST|Messaggio #N|Timestamp: T|TTL: X
 */
function parseMulticastMessage(message) {
    const parts = message.toString().split('|');
    
    if (parts.length >= 4 && parts[0] === 'MULTICAST') {
        return {
            type: 'MULTICAST',
            content: parts[1],
            timestamp: parts[2],
            ttl: parts[3]
        };
    }
    
    return {
        type: 'UNKNOWN',
        content: message.toString()
    };
}

// ==================== VALIDAZIONE ====================

if (!isValidMulticastAddress(multicastAddress)) {
    console.error('❌ Indirizzo multicast non valido:', multicastAddress);
    console.error('   Deve essere nel range 224.0.0.0 - 239.255.255.255');
    process.exit(1);
}

// ==================== MAIN ====================

console.log('═'.repeat(55));
console.log('📡 MULTICAST UDP RECEIVER (Node.js)');
console.log('═'.repeat(55));
console.log(`🔌 Porta: ${port}`);
console.log(`📡 Gruppo Multicast: ${multicastAddress}`);
console.log('═'.repeat(55));
console.log();

// Crea socket UDP con reuseAddr per permettere multipli receiver
const socket = dgram.createSocket({ type: 'udp4', reuseAddr: true });

let messageCount = 0;

socket.on('error', (err) => {
    console.error(`❌ Errore socket: ${err.message}`);
    console.error('   Verifica che la porta non sia già in uso');
    socket.close();
    process.exit(1);
});

socket.on('listening', () => {
    // IMPORTANTE: Join al gruppo multicast
    socket.addMembership(multicastAddress);
    
    const address = socket.address();
    console.log(`✅ Socket creato sulla porta ${address.port}`);
    console.log(`🤝 Unito al gruppo multicast ${multicastAddress}`);
    console.log('👂 In attesa di messaggi multicast...');
    console.log();
    console.log('Premi Ctrl+C per terminare');
    console.log();
});

socket.on('message', (msg, remote) => {
    messageCount++;
    
    // Parsing del messaggio
    const parsed = parseMulticastMessage(msg);
    
    // Visualizza informazioni
    console.log('╔' + '═'.repeat(53));
    console.log(`║ 📥 MESSAGGIO MULTICAST #${messageCount} RICEVUTO`);
    console.log('╠' + '═'.repeat(53));
    console.log(`║ ⏰ Ora: ${getCurrentTime()}`);
    console.log(`║ 👤 Mittente: ${remote.address}:${remote.port}`);
    console.log(`║ 📦 Dimensione: ${msg.length} bytes`);
    console.log('╠' + '═'.repeat(53));
    
    if (parsed.type === 'MULTICAST') {
        console.log(`║ 📝 Tipo: MULTICAST`);
        console.log(`║ 💬 Contenuto: ${parsed.content}`);
        console.log(`║ 🕐 ${parsed.timestamp}`);
        console.log(`║ 🌐 ${parsed.ttl}`);
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
    
    // Leave dal gruppo multicast
    try {
        socket.dropMembership(multicastAddress);
        console.log(`👋 Lasciato gruppo multicast ${multicastAddress}`);
    } catch (err) {
        console.error('⚠️  Errore nel lasciare il gruppo:', err.message);
    }
    
    socket.close();
    console.log('👋 Receiver terminato\n');
    process.exit(0);
}

// Gestione segnali di terminazione
process.on('SIGINT', cleanup);
process.on('SIGTERM', cleanup);
