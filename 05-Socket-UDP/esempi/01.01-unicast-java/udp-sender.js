/**
 * 05-Socket-UDP/esempi/01.01-unicast-java/udp-sender.js
 *
 * Obiettivo: Inviare un solo messaggio a un ricevitore UDP unicast.
 * Protocollo: Testo UTF-8, massimo 1024 byte; destinazione predefinita localhost:9876.
 * Parametri: host, porta, messaggio (tutti facoltativi).
 *
 * Avvio:
 *   node udp-sender.js localhost 9876 "Caffè e UDP"
 *
 * Avvia prima il ricevitore. La send conferma l’invio locale, non la consegna al destinatario.
 */

// dgram espone le API UDP native di Node.js: non occorre installare una libreria esterna.
const dgram = require('node:dgram');
const host = process.argv[2] ?? '127.0.0.1';
const port = Number(process.argv[3] ?? 9876);
// Il Buffer contiene i byte UTF-8 da spedire, compreso il caso del messaggio vuoto.
const data = Buffer.from(process.argv[4] ?? 'Ciao, UDP!', 'utf8');
if (!Number.isInteger(port) || port < 1 || port > 65535 || data.length > 1024) throw new Error('Porta o messaggio non valido');
const socket = dgram.createSocket('udp4');
// Questa dimostrazione è monouso: chiude il socket dopo il messaggio o dopo un errore.
socket.on('error', error => { console.error(error.message); socket.close(); process.exitCode = 1; });
// La callback segnala l’esito dell’invio locale; non è una conferma generata dal ricevitore.
socket.send(data, port, host, error => {
    if (error) { console.error(error.message); process.exitCode = 1; }
    else console.log(`INVIATO ${data.length} byte`);
    socket.close();
});
