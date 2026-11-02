/**
 * 06-Socket-Multicast-Broadcast/esempi/udp-cli.js
 *
 * Obiettivo: Raccogliere le operazioni comuni ai mittenti e ricevitori Node.js del modulo 06.
 * Protocollo: Datagrammi IPv4 UTF-8, massimo 1024 byte; opzioni multicast e broadcast distinte.
 * Parametri: È un modulo importato: i parametri arrivano dagli script nelle sottocartelle.
 *
 * Avvio:
 *   Usa per esempio node 02.03-multicast-js/multicast-sender.js
 *
 * Centralizza validazione, bind asincrono, invio periodico, conteggio e chiusura idempotente.
 */

// Utility comune agli esempi CLI; nessuna dipendenza npm.
// dgram espone le API UDP native di Node.js: non occorre installare una libreria esterna.
const dgram = require('node:dgram');
const net = require('node:net');
// Usa il default solo quando manca il parametro: zero deve restare valido per TTL o conteggio, se ammesso.
function integer(text, fallback, min, max, name) {
    const value = Number(text ?? fallback);
    if (!Number.isInteger(value) || value < min || value > max) throw new Error(`${name}: ${min}..${max}`);
    return value;
}
// Controlla tutti gli ottetti IPv4, oltre all’intervallo multicast del primo ottetto.
function multicastGroup(text = '239.255.0.1') {
    const first = Number(text.split('.')[0]);
    if (!net.isIPv4(text) || first < 224 || first > 239 || text === '224.0.0.0') throw new Error('Gruppo multicast IPv4 non valido');
    return text;
}
// Riunisce timer, errori e segnali in una chiusura idempotente: una seconda chiamata non deve fallire.
function socketState(socket) {
    let closed = false;
    let timer;
    function close(error) {
        if (closed) return;
        closed = true; clearTimeout(timer);
        if (error instanceof Error) { console.error(error.message); process.exitCode = 1; }
        try { socket.close(); } catch (ignored) { }
    }
    socket.on('error', close);
    process.once('SIGINT', close);
    process.once('SIGTERM', close);
    return {close, timer: value => { timer = value; }};
}
// Il mittente apre una porta effimera e spedisce con una pausa tra gli invii, fino al limite richiesto.
function sender({group, port, interval, ttl = 1, limit = 0, message, multicast = false}) {
    if (!net.isIPv4(group)) throw new Error('Indirizzo IPv4 non valido');
    const socket = dgram.createSocket('udp4');
    const state = socketState(socket);
    let count = 0;
    // Attende il bind prima di impostare le opzioni multicast/broadcast, che richiedono un socket inizializzato.
    socket.bind(0, '0.0.0.0', () => {
        try {
            if (multicast) {
                // TTL limita gli hop; il loopback permette di osservare gli invii sullo stesso host.
                socket.setMulticastTTL(ttl); socket.setMulticastLoopback(true);
                // Per l’invio, l’opzione seleziona l’IPv4 della scheda locale, non il gruppo destinatario.
                if (process.env.MULTICAST_IFACE) socket.setMulticastInterface(process.env.MULTICAST_IFACE);
            } else socket.setBroadcast(true);
            function send() {
                count++;
                // Il contenuto esplicito può essere vuoto; altrimenti costruisce il formato periodico dell’esempio.
                const text = message ?? `${multicast ? 'MULTICAST' : 'BROADCAST'}|Messaggio #${count}|Timestamp: ${Date.now()}${multicast ? '|TTL: ' + ttl : ''}`;
                const data = Buffer.from(text, 'utf8');
                if (data.length > 1024) { state.close(new Error('Max 1024 byte')); return; }
                // La callback conferma l’invio locale; UDP non restituisce una ricevuta da ciascun destinatario.
                socket.send(data, port, group, error => {
                    if (error) { state.close(error); return; }
                    console.log('INVIATO ' + text);
                    if (limit > 0 && count >= limit) state.close();
                    // Pianifica l’invio successivo solo dopo il completamento di quello attuale.
                    else state.timer(setTimeout(send, interval));
                });
            }
            send();
        } catch (error) { state.close(error); }
    });
}
// reuseAddr è usato in multicast per consentire più membri locali sulla stessa porta.
function receiver({group, port, limit = 0, multicast = false}) {
    const socket = dgram.createSocket({type: 'udp4', reuseAddr: multicast});
    const state = socketState(socket);
    let count = 0;
    // L’evento listening conferma che il bind è terminato e la porta locale è pronta.
    socket.on('listening', () => {
        try {
            // Il ricevitore aderisce al gruppo sulla scheda indicata; al mittente il join non serve.
            if (multicast) socket.addMembership(group, process.env.MULTICAST_IFACE);
            console.log(`PRONTO ${group ?? '0.0.0.0'}:${port}`);
            // Una prova finita ha una durata massima anche se mancano datagrammi: la perdita non deve lasciarla bloccata.
            if (limit > 0) state.timer(setTimeout(() => state.close(new Error('Timeout ricezione')), 5000));
        } catch (error) { state.close(error); }
    });
    // Ogni evento message corrisponde a un datagramma, con il proprio confine di messaggio.
    socket.on('message', data => {
        if (data.length > 1024) { console.error('SCARTATO oltre 1024 byte'); return; }
        const text = data.toString('utf8');
        console.log('RICEVUTO ' + text);
        // bye è un comando del ricevitore multicast base; il conteggio permette invece prove finite senza quel comando.
        if ((multicast && text === 'bye') || (limit > 0 && ++count >= limit)) state.close();
    });
    socket.bind(port, '0.0.0.0');
}
module.exports = {integer, multicastGroup, sender, receiver};
