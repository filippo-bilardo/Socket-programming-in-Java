const dgram = require('node:dgram');
const port = Number(process.argv[2] ?? 9876);
if (!Number.isInteger(port) || port < 1 || port > 65535) throw new Error('Porta non valida');
const socket = dgram.createSocket('udp4');
socket.on('error', error => { console.error(error.message); socket.close(); process.exitCode = 1; });
socket.on('listening', () => console.log(`PRONTO ${socket.address().port}`));
socket.on('message', data => {
    if (data.length > 1024) { console.error('Datagramma troppo grande'); process.exitCode = 1; }
    else console.log('RICEVUTO ' + data.toString('utf8'));
    socket.close();
});
socket.bind(port);
