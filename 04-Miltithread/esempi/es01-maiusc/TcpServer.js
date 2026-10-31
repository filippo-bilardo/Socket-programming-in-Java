// Server TCP Node.js: una riga UTF-8 per connessione, porta 8765.
const net = require('net');
const server = net.createServer(socket => {
    socket.setEncoding('utf8');
    socket.setTimeout(30000, () => socket.destroy());
    let pending = '';
    let answered = false;
    socket.on('data', chunk => {
        if (answered) return;
        pending += chunk;
        const end = pending.indexOf('\n');
        if (end !== -1) {
            answered = true;
            const message = pending.slice(0, end).replace(/\r$/, '');
            socket.end('Risposta dal server: ' + message.toUpperCase() + '\n');
        } else if (pending.length > 65536) socket.destroy();
    });
    socket.on('error', err => console.error('Errore client:', err.message));
});
server.on('error', err => console.error('Errore server:', err.message));
server.listen(8765, () => console.log('Server in ascolto sulla porta 8765'));
