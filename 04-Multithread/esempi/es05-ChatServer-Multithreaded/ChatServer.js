/**
 * Chat TCP compatibile con ChatServer.java: UTF-8 a righe, porta predefinita 9999.
 * Avvio: node ChatServer.js [porta]. Nessun pacchetto npm.
 * I callback JavaScript avanzano nell'event loop; nessun thread dedicato per socket.
 * Console locale: STATS e SHUTDOWN. Ogni messaggio chat è contato una sola volta.
 */
'use strict';

const net = require('node:net');
const readline = require('node:readline');

const port = process.argv[2] === undefined ? 9999 : Number(process.argv[2]);
if (process.argv.length > 3 || !Number.isInteger(port) || port < 1 || port > 65535) {
    console.error('Uso: node ChatServer.js [porta], con porta fra 1 e 65535');
    process.exit(1);
}

const clients = new Map(); // Registro degli utenti già accolti; chiave numerica ordinabile.
const connections = new Set(); // Comprende anche socket in fase di chiusura.
let nextClientId = 0;
let messages = 0;
let running = true;

function printStatistics() {
    console.log(`[STATISTICHE] Client attivi: ${clients.size} | Messaggi chat: ${messages}`);
}

function sendMessage(client, message) {
    if (client.closed || client.socket.destroyed || !client.socket.writable) return false;
    // write accoda anche quando restituisce false: non reinviare la stessa riga.
    client.socket.write(message + '\n', 'utf8');
    return true;
}

function broadcast(message, sender) {
    for (const client of [...clients.values()]) {
        if (client !== sender) sendMessage(client, message);
    }
}

function disconnect(client, graceful = false) {
    if (client.closed) return; // EOF, errore e /quit possono arrivare sulla stessa sessione.
    client.closed = true;
    const wasRegistered = clients.delete(client.id);
    if (client.lines) client.lines.close();
    if (graceful && !client.socket.destroyed) {
        client.socket.end(); // Invia prima le risposte già accodate, inclusa Arrivederci.
        // Un peer che non chiude il proprio invio non deve trattenere il socket per sempre.
        client.closeTimer = setTimeout(() => client.socket.destroy(), 1000);
        client.closeTimer.unref();
    } else {
        client.socket.destroy();
    }
    if (wasRegistered && running) {
        console.log(`[DISCONNESSIONE] ${client.username}`);
        broadcast(`[SERVER]: ${client.username} si è disconnesso. Client online: ${clients.size}`, client);
        printStatistics();
    }
}

function processMessage(client, message) {
    if (!running || client.closed) return;
    if (message === '/quit') {
        sendMessage(client, `[SERVER]: Arrivederci ${client.username}`);
        disconnect(client, true);
    } else if (message === '/list') {
        const names = [...clients.values()].sort((a, b) => a.id - b.id).map(c => c.username);
        sendMessage(client, '[SERVER]: Utenti online: ' + names.join(', '));
    } else if (message.startsWith('/')) {
        sendMessage(client, '[SERVER]: Comando non riconosciuto');
    } else if (message.length !== 0) {
        messages++;
        sendMessage(client, '[TU]: ' + message);
        broadcast(`[${client.username}]: ${message}`, client);
        console.log(`[${client.username}] ${message}`);
        printStatistics();
    }
}

const server = net.createServer({ allowHalfOpen: true }, socket => {
    const id = ++nextClientId;
    const client = { id, username: `Client${id}`, socket, lines: null, closed: false, closeTimer: null };
    connections.add(client);
    const onError = error => {
        if (!client.closed && running) console.error(`Errore ${client.username}: ${error.message}`);
        disconnect(client);
    };
    socket.on('error', onError);
    socket.on('close', () => {
        clearTimeout(client.closeTimer);
        connections.delete(client);
        disconnect(client);
    });
    if (!running) {
        disconnect(client);
        return;
    }
    // Il benvenuto viene accodato prima della pubblicazione nel registro condiviso.
    sendMessage(client, `[SERVER]: Benvenuto ${client.username}! Client connessi: ${clients.size + 1}`);
    clients.set(id, client);
    // readline ricostruisce UTF-8 frammentato e tratta CRLF come un solo terminatore.
    client.lines = readline.createInterface({ input: socket, crlfDelay: Infinity });
    client.lines.on('line', message => processMessage(client, message));
    client.lines.on('error', onError);
    client.lines.on('close', () => disconnect(client, true));
    console.log(`[CONNESSIONE] ${client.username} da ${socket.remoteAddress}:${socket.remotePort}`);
    broadcast(`[SERVER]: ${client.username} si è connesso. Client online: ${clients.size}`, client);
    printStatistics();
});

const consoleInput = readline.createInterface({ input: process.stdin, terminal: false });
consoleInput.on('line', command => {
    const localCommand = command.trim().toUpperCase();
    if (localCommand === 'SHUTDOWN') shutdown();
    else if (localCommand === 'STATS') printStatistics();
    else console.log('Console: STATS oppure SHUTDOWN');
});
consoleInput.on('error', error => console.error('Errore console: ' + error.message));
// EOF della console non arresta il servizio: funziona anche avviato in background.

function shutdown() {
    if (!running) return;
    running = false;
    consoleInput.close();
    process.stdin.destroy(); // Rilascia anche il pipe/terminale locale durante l’uscita.
    // Annullo le sessioni attive: non aspetto gli utenti o nuovi dati dalla rete.
    for (const client of [...connections]) disconnect(client);
    clients.clear();
    if (server.listening) server.close();
    console.log('[SERVER] Arresto completato');
    printStatistics();
}

server.on('error', error => {
    console.error('Errore server: ' + error.message);
    process.exitCode = 1;
    shutdown();
});
process.on('SIGINT', shutdown);
process.on('SIGTERM', shutdown);
server.listen(port, () => {
    console.log(`Server in ascolto sulla porta ${port}`);
    console.log('Console: STATS oppure SHUTDOWN');
});
