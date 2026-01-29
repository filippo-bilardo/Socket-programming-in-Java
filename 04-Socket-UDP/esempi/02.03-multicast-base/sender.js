/**
 * @file sender.js
 * 
 * TPSIT_3/UDPMulticast/Sender application (JavaScript version)
 * 
 * Questo programma implementa un mittente UDP multicast che invia messaggi
 * a un gruppo multicast. Tutti i receiver che si sono uniti al gruppo
 * riceveranno il messaggio.
 * 
 * NOTA: In JavaScript, come in Java, per inviare messaggi multicast è
 * sufficiente un socket UDP normale (DatagramSocket/dgram).
 * 
 * @author Filippo Bilardo
 * @version 1.00 25/01/2026
 */

const dgram = require('dgram');

// ==================== CONFIGURAZIONE ====================
/** Indirizzo IP del gruppo multicast */
const MULTICAST_IP = '239.0.0.1';
/** Porta UDP su cui inviare i messaggi multicast */
const MULTICAST_PORT = 9876;
/** TTL (Time To Live) per i pacchetti multicast */
const MULTICAST_TTL = 1; // 1 = solo rete locale

// ==================== MAIN ====================
// Crea un socket UDP IPv4
// Equivalente a: new DatagramSocket() in Java
const socket = dgram.createSocket('udp4');

console.log('═══════════════════════════════════════════════════');
console.log('📡 MULTICAST UDP SENDER (JavaScript)');
console.log('═══════════════════════════════════════════════════');
console.log('🌐 Gruppo Multicast: ' + MULTICAST_IP);
console.log('🔌 Porta: ' + MULTICAST_PORT);
console.log('🌍 TTL: ' + MULTICAST_TTL + ' (rete locale)');
console.log('═══════════════════════════════════════════════════\n');

// ==================== CONFIGURAZIONE SOCKET ====================
// Imposta il TTL per i pacchetti multicast
// Equivalente a: socket.setTimeToLive(MULTICAST_TTL)
socket.setMulticastTTL(MULTICAST_TTL);
console.log('⏱️  TTL impostato a: ' + MULTICAST_TTL);

// Opzionale: Imposta se ricevere i propri messaggi (loopback)
// socket.setMulticastLoopback(true);  // riceve i propri messaggi (default)
// socket.setMulticastLoopback(false); // NON riceve i propri messaggi

// ==================== PREPARAZIONE MESSAGGIO ====================
// Messaggio da inviare
const message = 'Messaggio da Sender';

// Converte la stringa in Buffer (array di byte)
// Equivalente a: message.getBytes() in Java
const messageBuffer = Buffer.from(message, 'utf8');

console.log('\n📦 Preparato pacchetto:');
console.log('   └─ Messaggio: "' + message + '"');
console.log('   └─ Dimensione: ' + messageBuffer.length + ' bytes');
console.log('   └─ Destinazione: ' + MULTICAST_IP + ':' + MULTICAST_PORT);

// ==================== INVIO ====================

console.log('\n📤 Invio messaggio al gruppo multicast...');
// Invia il messaggio al gruppo multicast
// Equivalente a: socket.send(packet) in Java
socket.send(
    messageBuffer,      // Dati da inviare (Buffer)
    0,                  // Offset di inizio nel buffer
    messageBuffer.length, // Lunghezza dei dati
    MULTICAST_PORT,     // Porta di destinazione
    MULTICAST_IP,       // Indirizzo multicast
    (err) => {
        if (err) {
            console.error('❌ Errore durante l\'invio:', err.message);
        } else {
            console.log('✅ Messaggio inviato con successo!');
            console.log('   Tutti i membri del gruppo ' + MULTICAST_IP + ' riceveranno il messaggio');
        }
        
        // ==================== CLEANUP ====================
        // Chiude il socket dopo l'invio
        // Equivalente a: socket.close() in Java
        socket.close(() => {
            console.log('\n🔒 Socket chiuso');
            console.log('\n👋 Sender terminato\n');
        });
    }
);

// ==================== GESTIONE ERRORI ====================
/**
 * Event handler: 'error'
 * Si attiva quando si verifica un errore
 */
socket.on('error', (err) => {
    console.error('❌ Errore socket:', err.message);
    socket.close();
    process.exit(1);
});

/**
 * Gestione errori non catturati
 */
process.on('uncaughtException', (err) => {
    console.error('❌ Errore non gestito:', err.message);
    socket.close();
    process.exit(1);
});
