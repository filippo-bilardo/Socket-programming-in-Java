// Una riga UTF-8 inviata e una risposta letta fino a newline.
const net = require('net');
const client = net.createConnection({host: 'localhost', port: 8765}, () => {
    client.write('Ciao dal client Node.js!\n');
});
client.setEncoding('utf8');
client.setTimeout(5000, () => client.destroy(new Error('Timeout risposta')));
let pending = '';
let complete = false;
client.on('data', chunk => {
    pending += chunk;
    const end = pending.indexOf('\n');
    if (end !== -1 && !complete) {
        complete = true;
        console.log('Risposta ricevuta: ' + pending.slice(0, end).replace(/\r$/, ''));
        client.end();
    }
});
client.on('end', () => { if (!complete) console.error('Risposta incompleta'); });
client.on('error', err => console.error('Errore:', err.message));
