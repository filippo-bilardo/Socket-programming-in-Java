/**
 * udp-sender.js
 * Esempio di mittente UDP in JavaScript/Node.js
 * Invia un messaggio UDP a un ricevitore
 * Versione: 1.0
 * Data: 25/01/26
 * Autore: Filippo Bilardo
 *
 * node udp-sender.js
 */

const dgram = require('dgram');

// Crea socket UDP
const socket = dgram.createSocket('udp4');

// Configurazione
const message = 'Hello, UDP!';
const host = 'localhost';
const port = 9876;

// Prepara il buffer
const buffer = Buffer.from(message);

// Invia il messaggio
socket.send(buffer, 0, buffer.length, port, host, (err) => {
    if (err) {
        console.error('Errore durante l\'invio:', err);
        socket.close();
        process.exit(1);
    }
    
    console.log('Messaggio inviato!');
    socket.close();
});

// Gestione errori
socket.on('error', (err) => {
    console.error('Errore socket:', err);
    socket.close();
    process.exit(1);
});
