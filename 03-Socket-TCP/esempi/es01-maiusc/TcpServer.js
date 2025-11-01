/**
 * Esempio di un semplice server TCP che riceve un messaggio da un client,
 * lo elabora e restituisce una risposta con il messaggio in maiuscolo.
 *
 * Eseguo il server in background: 
 * node TcpServer.js &
 */
const net = require('net');

const PORT = 8765;

// Crea il server TCP
const server = net.createServer((socket) => {
    console.log('Connessione stabilita con il client!');
    console.log(`Client: ${socket.remoteAddress}:${socket.remotePort}`);

    // Gestione dati ricevuti dal client
    socket.on('data', (data) => {
        const message = data.toString().trim();
        console.log('Messaggio ricevuto: ' + message);
        
        // Invia la risposta al client (messaggio in maiuscolo)
        const response = 'Risposta dal server: ' + message.toUpperCase() + '\n';
        socket.write(response);
        
        // Chiude la connessione dopo aver inviato la risposta
        socket.end();
    });

    // Gestione chiusura connessione
    socket.on('end', () => {
        console.log('Client disconnesso');
        server.close(() => {
            console.log('Server terminato.');
        });
    });

    // Gestione errori
    socket.on('error', (err) => {
        console.error('Errore socket:', err.message);
    });
});

// Gestione errori del server
server.on('error', (err) => {
    console.error('Errore server:', err.message);
});

// Avvia il server
server.listen(PORT, () => {
    console.log(`Server in ascolto sulla porta ${PORT}...`);
});

/*
Notare che il server rimane in esecuzione in background 
    (senza il codice di riga 36-40)
    server.close(() => {
        console.log('Server terminato.');
    });
e può gestire più connessioni sequenziali dai client.
per terminare il server, usare il comando:
fg # per portare il processo in foreground
kill %1
oppure
premere Ctrl+C nel terminale.

*/