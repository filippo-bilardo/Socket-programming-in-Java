// Utility comune agli esempi CLI; nessuna dipendenza npm.
const dgram = require('node:dgram');
const net = require('node:net');
function integer(text, fallback, min, max, name) {
    const value = Number(text ?? fallback);
    if (!Number.isInteger(value) || value < min || value > max) throw new Error(`${name}: ${min}..${max}`);
    return value;
}
function multicastGroup(text = '239.255.0.1') {
    const first = Number(text.split('.')[0]);
    if (!net.isIPv4(text) || first < 224 || first > 239 || text === '224.0.0.0') throw new Error('Gruppo multicast IPv4 non valido');
    return text;
}
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
function sender({group, port, interval, ttl = 1, limit = 0, message, multicast = false}) {
    if (!net.isIPv4(group)) throw new Error('Indirizzo IPv4 non valido');
    const socket = dgram.createSocket('udp4');
    const state = socketState(socket);
    let count = 0;
    socket.bind(0, '0.0.0.0', () => {
        try {
            if (multicast) {
                socket.setMulticastTTL(ttl); socket.setMulticastLoopback(true);
                if (process.env.MULTICAST_IFACE) socket.setMulticastInterface(process.env.MULTICAST_IFACE);
            } else socket.setBroadcast(true);
            function send() {
                count++;
                const text = message ?? `${multicast ? 'MULTICAST' : 'BROADCAST'}|Messaggio #${count}|Timestamp: ${Date.now()}${multicast ? '|TTL: ' + ttl : ''}`;
                const data = Buffer.from(text, 'utf8');
                if (data.length > 1024) { state.close(new Error('Max 1024 byte')); return; }
                socket.send(data, port, group, error => {
                    if (error) { state.close(error); return; }
                    console.log('INVIATO ' + text);
                    if (limit > 0 && count >= limit) state.close();
                    else state.timer(setTimeout(send, interval));
                });
            }
            send();
        } catch (error) { state.close(error); }
    });
}
function receiver({group, port, limit = 0, multicast = false}) {
    const socket = dgram.createSocket({type: 'udp4', reuseAddr: multicast});
    const state = socketState(socket);
    let count = 0;
    socket.on('listening', () => {
        try {
            if (multicast) socket.addMembership(group, process.env.MULTICAST_IFACE);
            console.log(`PRONTO ${group ?? '0.0.0.0'}:${port}`);
            if (limit > 0) state.timer(setTimeout(() => state.close(new Error('Timeout ricezione')), 5000));
        } catch (error) { state.close(error); }
    });
    socket.on('message', data => {
        if (data.length > 1024) { console.error('SCARTATO oltre 1024 byte'); return; }
        const text = data.toString('utf8');
        console.log('RICEVUTO ' + text);
        if ((multicast && text === 'bye') || (limit > 0 && ++count >= limit)) state.close();
    });
    socket.bind(port, '0.0.0.0');
}
module.exports = {integer, multicastGroup, sender, receiver};
