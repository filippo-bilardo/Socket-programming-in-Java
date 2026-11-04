/**
 * Versione Node.js di Server.java: echo TCP di byte sulla porta 9876.
 * Ogni connessione riceve soltanto i propri dati, fino a EOF o timeout.
 * Il client Java può usare shutdownOutput() e continuare a leggere l'echo.
 *
 * Avvio: node Server.js (nessun pacchetto npm richiesto).
 * Node.js gestisce l'I/O con l'event loop, senza un thread JavaScript per client.
 */
'use strict';

const net = require('node:net');

const PORT = 9876;
const IDLE_TIMEOUT_MS = 30_000;

// Consente di completare l'invio dell'echo dopo l'EOF ricevuto dal client.
// Sarà pipe() a terminare il lato di scrittura quando l'input finisce.
const server = net.createServer({ allowHalfOpen: true }, (socket) => {
    const client = `${socket.remoteAddress}:${socket.remotePort}`;
    console.log(`Nuova connessione da: ${client}`);

    // Il timeout segnala inattività: la chiusura va richiesta esplicitamente.
    socket.setTimeout(IDLE_TIMEOUT_MS, () => {
        console.log(`Timeout del client: ${client}`);
        socket.destroy();
    });

    socket.on('error', (error) => {
        console.error(`Errore client ${client}: ${error.message}`);
        socket.destroy(); // Un errore di questa connessione non ferma il listener.
    });

    socket.on('close', () => {
        console.log(`Client disconnesso: ${client}`);
    });

    // Un socket è uno stream Duplex: l'input può essere collegato al proprio output.
    // Nessuna conversione in stringa: restano intatti anche byte binari e UTF-8 frammentato.
    // pipe gestisce i buffer e rallenta la lettura se il destinatario non riceve abbastanza.
    // A EOF chiama end() sull'output, lasciando inviare i dati già accodati.
    socket.pipe(socket);
});

server.on('error', (error) => {
    console.error(`Errore server: ${error.message}`);
    process.exitCode = 1; // Per esempio, la porta è già occupata.
});

// Come Server.java, ascolta sugli indirizzi locali disponibili.
server.listen(PORT, () => {
    console.log(`Server in ascolto sulla porta ${PORT}`);
});
