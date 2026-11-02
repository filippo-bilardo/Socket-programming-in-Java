/**
 * 05-Socket-UDP/esempi/01.02-EchoUdp/echo-udp.js
 *
 * Obiettivo: Confrontare server echo e client request/response sullo stesso protocollo UDP.
 * Protocollo: Un datagramma UTF-8 di massimo 1024 byte; la risposta contiene gli stessi byte.
 * Parametri: server <porta> oppure client <host> <porta> [messaggio].
 *
 * Avvio:
 *   node echo-udp.js server 9999
 *   node echo-udp.js client localhost 9999 "Caffè"
 *
 * Avvia server e client in terminali diversi. Senza messaggio il client legge la console; quit è locale.
 */

// dgram espone le API UDP native di Node.js: non occorre installare una libreria esterna.
const dgram = require('node:dgram');
const readline = require('node:readline');
// Il primo argomento sceglie il ruolo; i successivi dipendono da server o client.
const [mode, ...args] = process.argv.slice(2);
if (mode !== 'server' && mode !== 'client') {
    console.log('node echo-udp.js server <porta> | client <host> <porta> [messaggio]');
    process.exit(0);
}
const port = Number(mode === 'server' ? args[0] : args[1]);
if (!Number.isInteger(port) || port < 1 || port > 65535) throw new Error('Porta non valida');
const socket = dgram.createSocket('udp4');
// Eventi, segnali e timeout possono chiedere la chiusura: lo stato evita una seconda close del socket.
let closed = false;
let timer;
let rl;
function close() {
    if (closed) return;
    closed = true; clearTimeout(timer); if (rl) rl.close(); socket.close();
}
socket.on('error', error => { console.error(error.message); process.exitCode = 1; close(); });
process.on('SIGINT', close);
process.on('SIGTERM', close);
// Il server è stateless: la risposta usa indirizzo e porta del datagramma appena arrivato.
if (mode === 'server') {
    socket.on('message', (data, remote) => {
        if (data.length > 1024) { console.error('SCARTATO datagramma oltre 1024 byte'); return; }
        // Restituisce il Buffer originale, compreso il payload vuoto, senza prefissi aggiunti sul filo.
        socket.send(data, remote.port, remote.address);
    });
    socket.bind(port, () => console.log(`PRONTO ${port}`));
} else {
    // connect seleziona il peer UDP; non crea una connessione come TCP.
    // Una sola richiesta in attesa rende esplicito il limite del protocollo echo senza identificativi.
    let waiting = false;
    // La presenza del messaggio sulla CLI distingue la prova singola dall’input interattivo, anche se il testo è vuoto.
    const once = args.length > 2;
    // Avvia invio e timer insieme; non accetta una nuova riga finché attende la risposta precedente.
    function send(message) {
        if (waiting) { console.error('Attendi la risposta precedente'); return; }
        const data = Buffer.from(message, 'utf8');
        if (data.length > 1024) { console.error('Max 1024 byte'); if (once) { process.exitCode = 1; close(); } return; }
        waiting = true;
        socket.send(data);
        // Il timeout non dimostra che il server non abbia ricevuto la richiesta: l’esito rimane non noto.
        timer = setTimeout(() => {
            console.error('TIMEOUT: esito non noto');
            // Termina la sessione: una risposta tardiva non va attribuita alla richiesta seguente.
            process.exitCode = 1; close();
        }, 2000);
    }
    // Ogni evento message corrisponde a un datagramma, con il proprio confine di messaggio.
    socket.on('message', data => {
        if (!waiting) return;
        // Una risposta valida conclude l’attesa e annulla il timer, evitando un timeout dopo il successo.
        waiting = false; clearTimeout(timer);
        console.log('ECHO ' + data.toString('utf8'));
        if (once) close();
    });
    // UDP connect sceglie il peer e filtra le sorgenti; non effettua un handshake TCP.
    socket.connect(port, args[0], () => {
        if (once) send(args[2]);
        else {
            // L’event loop gestisce console e rete; quit ed EOF riguardano solo questa istanza client.
            rl = readline.createInterface({input: process.stdin, output: process.stdout});
            rl.on('line', line => { if (line === 'quit') close(); else send(line); });
            rl.on('close', close);
        }
    });
}
