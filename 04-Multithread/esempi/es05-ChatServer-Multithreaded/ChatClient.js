/**
 * Client della chat Java/Node.js. Avvio: node ChatClient.js [host] [porta].
 * Default localhost:9999; console e socket sono due sorgenti di eventi indipendenti.
 * Un unico readline legge la rete; un altro legge la tastiera dopo il benvenuto.
 * /quit ed EOF della console attendono la risposta finale e la chiusura del server.
 */
'use strict';

const net = require('node:net');
const readline = require('node:readline');

const host = process.argv[2] || 'localhost';
const port = process.argv[3] === undefined ? 9999 : Number(process.argv[3]);
if (process.argv.length > 4 || !Number.isInteger(port) || port < 1 || port > 65535) {
    console.error('Uso: node ChatClient.js [host] [porta], con porta fra 1 e 65535');
    process.exit(1);
}

const socket = net.createConnection({ host, port });
let keyboard = null;
let welcomed = false;
let quitting = false;
let finished = false;

function closeConsole() {
    if (finished) return;
    finished = true;
    quitting = true;
    if (keyboard) keyboard.close();
    process.stdin.pause(); // Una chiusura remota termina anche senza un altro Invio.
}

function quit() {
    if (quitting || finished) return;
    quitting = true;
    socket.write('/quit\n', 'utf8');
    socket.setTimeout(5000); // Attesa limitata della risposta alla disconnessione.
    if (keyboard) keyboard.close();
}

function startConsole() {
    const interactive = Boolean(process.stdin.isTTY && process.stdout.isTTY);
    keyboard = readline.createInterface({ input: process.stdin, output: process.stdout, terminal: interactive });
    keyboard.setPrompt('> ');
    keyboard.on('line', message => {
        if (quitting || finished) return;
        if (message === '/quit') quit();
        else {
            socket.write(message + '\n', 'utf8');
            if (interactive) keyboard.prompt();
        }
    });
    keyboard.on('close', quit); // EOF della tastiera equivale a /quit, una sola volta.
    keyboard.on('error', error => socket.destroy(error));
    keyboard.on('SIGINT', quit);
    if (interactive) keyboard.prompt();
}

socket.setTimeout(5000); // Connessione e ricezione del benvenuto devono completarsi.
socket.on('timeout', () => socket.destroy(new Error('Timeout di connessione, benvenuto o uscita')));
socket.on('error', error => {
    console.error('Errore client: ' + error.message);
    process.exitCode = 1;
    closeConsole();
});
socket.on('close', () => {
    closeConsole();
    console.log('Connessione chiusa dal server.');
});

const network = readline.createInterface({ input: socket, crlfDelay: Infinity });
network.on('error', () => closeConsole()); // L'errore di trasporto viene già mostrato dal socket.
network.on('line', message => {
    if (!welcomed) {
        const match = /^\[SERVER\]: Benvenuto (Client\d+)!/.exec(message);
        if (!match) {
            socket.destroy(new Error('Benvenuto assente o non valido'));
            return;
        }
        welcomed = true;
        socket.setTimeout(0); // Un utente può rimanere in chat senza digitare.
        console.log('Connessione al server riuscita!');
        console.log('Sei stato registrato come: ' + match[1]);
        console.log(message);
        console.log('Comandi: /list, /quit. Scrivi un messaggio e premi Invio.');
        startConsole();
    } else {
        console.log(message);
        if (keyboard && !quitting && keyboard.terminal) keyboard.prompt(true);
    }
});
network.on('close', () => {
    if (!welcomed && !finished) {
        console.error('Errore client: Benvenuto assente o non valido');
        process.exitCode = 1;
    }
    closeConsole();
});
process.on('SIGINT', quit);
