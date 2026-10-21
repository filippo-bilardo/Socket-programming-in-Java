<?php
/**
 * Client PHP per comunicare con server Java
 */

// Configurazione
define('SERVER_HOST', 'localhost');
define('SERVER_PORT', 5555);

echo "=== CLIENT PHP - ECHO ===\n";

// Crea socket
$socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
if ($socket === false) {
    die("Errore creazione socket: " . socket_strerror(socket_last_error()) . "\n");
}

// Connetti al server
echo "Connessione al server " . SERVER_HOST . ":" . SERVER_PORT . "...\n";
$result = socket_connect($socket, SERVER_HOST, SERVER_PORT);
if ($result === false) {
    die("Errore connessione: " . socket_strerror(socket_last_error($socket)) . "\n");
}

echo "✓ Connesso al server!\n";
echo "Digita 'exit' per disconnetterti\n\n";

// Loop di comunicazione
while (true) {
    // Leggi input utente
    echo "Inserisci messaggio: ";
    $messaggio = trim(fgets(STDIN));
    
    // Invia al server
    $messaggio_con_newline = $messaggio . "\n";
    socket_write($socket, $messaggio_con_newline, strlen($messaggio_con_newline));
    
    // Se exit, chiudi
    if (strtolower($messaggio) === 'exit') {
        echo "Disconnessione...\n";
        break;
    }
    
    // Ricevi echo dal server
    $risposta = socket_read($socket, 1024, PHP_NORMAL_READ);
    if ($risposta === false) {
        echo "Errore ricezione: " . socket_strerror(socket_last_error($socket)) . "\n";
        break;
    }
    
    echo "← Server: " . trim($risposta) . "\n\n";
}

// Chiudi socket
socket_close($socket);
echo "Connessione chiusa.\n";
?>