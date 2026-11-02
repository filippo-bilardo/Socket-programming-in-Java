const dgram = require('node:dgram');
const readline = require('node:readline');
const [mode, ...args] = process.argv.slice(2);
if (mode !== 'server' && mode !== 'client') {
    console.log('node echo-udp.js server <porta> | client <host> <porta> [messaggio]');
    process.exit(0);
}
const port = Number(mode === 'server' ? args[0] : args[1]);
if (!Number.isInteger(port) || port < 1 || port > 65535) throw new Error('Porta non valida');
const socket = dgram.createSocket('udp4');
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
if (mode === 'server') {
    socket.on('message', (data, remote) => {
        if (data.length > 1024) { console.error('SCARTATO datagramma oltre 1024 byte'); return; }
        socket.send(data, remote.port, remote.address);
    });
    socket.bind(port, () => console.log(`PRONTO ${port}`));
} else {
    // connect seleziona il peer UDP; non crea una connessione come TCP.
    let waiting = false;
    const once = args.length > 2;
    function send(message) {
        if (waiting) { console.error('Attendi la risposta precedente'); return; }
        const data = Buffer.from(message, 'utf8');
        if (data.length > 1024) { console.error('Max 1024 byte'); if (once) { process.exitCode = 1; close(); } return; }
        waiting = true;
        socket.send(data);
        timer = setTimeout(() => {
            console.error('TIMEOUT: esito non noto');
            // Termina la sessione: una risposta tardiva non va attribuita alla richiesta seguente.
            process.exitCode = 1; close();
        }, 2000);
    }
    socket.on('message', data => {
        if (!waiting) return;
        waiting = false; clearTimeout(timer);
        console.log('ECHO ' + data.toString('utf8'));
        if (once) close();
    });
    socket.connect(port, args[0], () => {
        if (once) send(args[2]);
        else {
            rl = readline.createInterface({input: process.stdin, output: process.stdout});
            rl.on('line', line => { if (line === 'quit') close(); else send(line); });
            rl.on('close', close);
        }
    });
}
