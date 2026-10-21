/**
 * Esempio di un semplice client TCP che invia un messaggio al server
 * e riceve la risposta.
 *
 * Esecuzione: node TcpClient.js
 */
const net = require('net');

const PORT = 8765;
const HOST = 'localhost';

// Crea il socket client
const client = new net.Socket();

// Connessione al server
client.connect(PORT, HOST, () => {
    console.log(`Connesso al server ${HOST}:${PORT}`);
    
    // Invia un messaggio al server
    const message = 'Ciao dal client Node.js!';
    console.log('Invio messaggio: ' + message);
    client.write(message + '\n');
});

// Gestione dati ricevuti dal server
client.on('data', (data) => {
    console.log('Risposta ricevuta: ' + data.toString().trim());
    client.destroy(); // Chiude la connessione
});

// Gestione chiusura connessione
client.on('close', () => {
    console.log('Connessione chiusa');
});

// Gestione errori
client.on('error', (err) => {
    console.error('Errore:', err.message);
});
