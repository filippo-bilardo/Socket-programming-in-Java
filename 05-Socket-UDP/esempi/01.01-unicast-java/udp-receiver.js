/**
 * 05-Socket-UDP/esempi/01.01-unicast-java/udp-receiver.js
 *
 * Obiettivo: Ricevere e visualizzare un datagramma UDP, poi terminare.
 * Protocollo: Testo UTF-8, massimo 1024 byte; porta predefinita 9876.
 * Parametri: porta di ricezione (facoltativa).
 *
 * Avvio:
 *   node udp-receiver.js 9876
 *
 * Il ricevitore è monouso: riavvialo per il messaggio successivo. Un payload vuoto è valido.
 */

// dgram espone le API UDP native di Node.js: non occorre installare una libreria esterna.
const dgram = require('node:dgram');
const port = Number(process.argv[2] ?? 9876);
if (!Number.isInteger(port) || port < 1 || port > 65535) throw new Error('Porta non valida');
const socket = dgram.createSocket('udp4');
// Questa dimostrazione è monouso: chiude il socket dopo il messaggio o dopo un errore.
socket.on('error', error => { console.error(error.message); socket.close(); process.exitCode = 1; });
// L’evento listening conferma che il bind è terminato e la porta locale è pronta.
socket.on('listening', () => console.log(`PRONTO ${socket.address().port}`));
// Ogni evento message corrisponde a un datagramma, con il proprio confine di messaggio.
socket.on('message', data => {
    if (data.length > 1024) { console.error('Datagramma troppo grande'); process.exitCode = 1; }
    else console.log('RICEVUTO ' + data.toString('utf8'));
    socket.close();
});
// Avvia il bind asincrono sulla porta del ricevitore; il mittente può usare una porta effimera.
socket.bind(port);
