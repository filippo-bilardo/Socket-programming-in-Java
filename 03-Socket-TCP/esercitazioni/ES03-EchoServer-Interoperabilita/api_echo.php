<?php
/**
 * API REST per comunicare con server Java
 * Restituisce JSON per chiamate AJAX
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

// Configurazione
define('SERVER_HOST', 'localhost');
define('SERVER_PORT', 5555);

// Verifica metodo POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode([
        'success' => false,
        'error' => 'Metodo non consentito. Usa POST.'
    ]);
    exit;
}

// Leggi JSON input
$input = file_get_contents('php://input');
$data = json_decode($input, true);

if (!isset($data['messaggio']) || empty(trim($data['messaggio']))) {
    echo json_encode([
        'success' => false,
        'error' => 'Messaggio non fornito'
    ]);
    exit;
}

$messaggio = trim($data['messaggio']);

try {
    // Crea socket
    $socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
    if ($socket === false) {
        throw new Exception("Errore creazione socket");
    }
    
    // Timeout
    socket_set_option($socket, SOL_SOCKET, SO_RCVTIMEO, array('sec' => 5, 'usec' => 0));
    socket_set_option($socket, SOL_SOCKET, SO_SNDTIMEO, array('sec' => 5, 'usec' => 0));
    
    // Connetti
    $result = socket_connect($socket, SERVER_HOST, SERVER_PORT);
    if ($result === false) {
        throw new Exception("Server non raggiungibile");
    }
    
    // Invia
    socket_write($socket, $messaggio . "\n", strlen($messaggio) + 1);
    
    // Ricevi
    $risposta = socket_read($socket, 1024, PHP_NORMAL_READ);
    socket_close($socket);
    
    // Risposta JSON
    echo json_encode([
        'success' => true,
        'messaggio_inviato' => $messaggio,
        'risposta_server' => trim($risposta),
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    
} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'error' => $e->getMessage()
    ]);
}
?>