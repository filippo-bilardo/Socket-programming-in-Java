const dgram = require('node:dgram');
const host = process.argv[2] ?? '127.0.0.1';
const port = Number(process.argv[3] ?? 9876);
const data = Buffer.from(process.argv[4] ?? 'Ciao, UDP!', 'utf8');
if (!Number.isInteger(port) || port < 1 || port > 65535 || data.length > 1024) throw new Error('Porta o messaggio non valido');
const socket = dgram.createSocket('udp4');
socket.on('error', error => { console.error(error.message); socket.close(); process.exitCode = 1; });
socket.send(data, port, host, error => {
    if (error) { console.error(error.message); process.exitCode = 1; }
    else console.log(`INVIATO ${data.length} byte`);
    socket.close();
});
