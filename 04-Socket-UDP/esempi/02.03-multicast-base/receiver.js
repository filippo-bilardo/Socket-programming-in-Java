/**
 * @file receiver.js
 * 
 * TPSIT_3/UDPMulticast/Receiver application (JavaScript version)
 * 
 * Questo programma implementa un ricevitore UDP multicast che si unisce
 * a un gruppo multicast e riceve messaggi inviati a quel gruppo.
 * 
 * @author Filippo Bilardo
 * @version 1.00 25/01/2026
 */

const dgram = require('dgram');

// ==================== CONFIGURAZIONE ====================
/** Indirizzo IP del gruppo multicast */
const MULTICAST_IP = '239.0.0.1';
/** Porta UDP su cui ricevere i messaggi multicast */
const MULTICAST_PORT = 9876;
/** Messaggio che indica la fine della comunicazione */
const EXIT_MESSAGE = 'bye';

// ==================== MAIN ====================

// Crea un socket UDP IPv4
// Il socket dgram è l'equivalente JavaScript di MulticastSocket
const socket = dgram.createSocket({ type: 'udp4', reuseAddr: true });

console.log('═══════════════════════════════════════════════════');
console.log('📡 MULTICAST UDP RECEIVER (JavaScript)');
console.log('═══════════════════════════════════════════════════');
console.log('🌐 Gruppo Multicast: ' + MULTICAST_IP);
console.log('🔌 Porta: ' + MULTICAST_PORT);
console.log('═══════════════════════════════════════════════════\n');

// ==================== EVENT HANDLERS ====================
/**
 * Event handler: 'listening'
 * Si attiva quando il socket è pronto e in ascolto
 */
socket.on('listening', () => {
    const address = socket.address();
    console.log('✅ Socket in ascolto su ' + address.address + ':' + address.port);
    
    // IMPORTANTE: Unisce il socket al gruppo multicast
    // Equivalente a socket.joinGroup(group) in Java
    // Senza questo, il socket NON riceverà i pacchetti multicast
    socket.addMembership(MULTICAST_IP);
    console.log('🤝 Unito al gruppo multicast: ' + MULTICAST_IP);
    
    console.log('\n👂 Receiver in ascolto...');
    console.log('💡 Invia "bye" per terminare la comunicazione');
    console.log('Ctrl+C per forzare la chiusura\n');
});

/**
 * Event handler: 'message'
 * Si attiva quando arriva un messaggio UDP
 * 
 * @param {Buffer} msg - Dati ricevuti (Buffer di byte)
 * @param {Object} rinfo - Informazioni sul mittente
 */
let messageCount = 0;

socket.on('message', (msg, rinfo) => {
    // Converte il Buffer in stringa
    // Equivalente a: new String(packet.getData(), 0, packet.getLength())
    const message = msg.toString('utf8');
    
    messageCount++;
    
    // Stampa il messaggio ricevuto con informazioni dettagliate
    console.log('╔═══════════════════════════════════════════════════');
    console.log('║ 📥 MESSAGGIO #' + messageCount + ' RICEVUTO');
    console.log('╠═══════════════════════════════════════════════════');
    console.log('║ 👤 Da: ' + rinfo.address + ':' + rinfo.port);
    console.log('║ 📦 Dimensione: ' + rinfo.size + ' bytes');
    console.log('║ 💬 Contenuto: ' + message);
    console.log('╚═══════════════════════════════════════════════════\n');
    
    // Verifica se è il messaggio di uscita
    // Equivalente a: message.equals("bye")
    if (message === EXIT_MESSAGE) {
        console.log('🛑 Ricevuto messaggio di terminazione');
        cleanup();
    }
});

/**
 * Event handler: 'error'
 * Si attiva quando si verifica un errore
 * 
 * @param {Error} err - Oggetto errore
 */
socket.on('error', (err) => {
    console.error('❌ Errore socket:', err.message);
    cleanup();
});

/**
 * Event handler: 'close'
 * Si attiva quando il socket viene chiuso
 */
socket.on('close', () => {
    console.log('\n👋 Receiver terminato\n');
});

// ==================== CLEANUP ====================

/**
 * Funzione per la chiusura pulita delle risorse
 */
function cleanup() {
    try {
        // IMPORTANTE: Lascia il gruppo multicast prima di chiudere
        // Equivalente a: socket.leaveGroup(group)
        socket.dropMembership(MULTICAST_IP);
        console.log('\n👋 Lasciato gruppo multicast: ' + MULTICAST_IP);
    } catch (err) {
        console.error('⚠️  Errore nel lasciare il gruppo multicast:', err.message);
    }
    
    // Chiude il socket
    // Equivalente a: socket.close()
    socket.close();
    console.log('🔒 Socket chiuso');
}

// ==================== GESTIONE SEGNALI ====================
/**
 * Intercetta Ctrl+C per chiusura pulita
 */
process.on('SIGINT', () => {
    console.log('\n\n⚠️  Interruzione ricevuta (Ctrl+C)');
    cleanup();
});

/**
 * Gestione errori non catturati
 */
process.on('uncaughtException', (err) => {
    console.error('❌ Errore non gestito:', err.message);
    cleanup();
    process.exit(1);
});

// ==================== AVVIO ====================
// Bind del socket alla porta multicast
// Equivalente a: new MulticastSocket(MULTICAST_PORT)
socket.bind(MULTICAST_PORT);
