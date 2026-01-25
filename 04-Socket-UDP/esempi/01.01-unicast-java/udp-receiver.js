/**
 * udp-receiver.js
 * Esempio di ricevitore UDP in JavaScript/Node.js
 * Riceve un messaggio UDP e lo stampa a schermo
 * Versione: 1.0
 * Data: 25/01/26
 * Autore: Filippo Bilardo
 * 
 * node udp-receiver.js
 */

const dgram = require('dgram');

// Crea socket UDP
const socket = dgram.createSocket('udp4');

// Configurazione
const port = 9876;

// Gestione errori
socket.on('error', (err) => {
    console.error('Errore socket:', err);
    socket.close();
    process.exit(1);
});

// Quando il socket è pronto
socket.on('listening', () => {
    const address = socket.address();
    console.log(`In attesa di messaggi su ${address.address}:${address.port}...`);
});

// Quando arriva un messaggio
socket.on('message', (msg, remote) => {
    const message = msg.toString();
    console.log(`Messaggio ricevuto: ${message}`);
    console.log(`Da: ${remote.address}:${remote.port}`);
    
    // Chiude il socket dopo aver ricevuto un messaggio
    socket.close();
});

// Binding sulla porta
socket.bind(port);
