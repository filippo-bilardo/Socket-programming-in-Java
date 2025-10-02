# Esempio 01: Server TCP Semplice in Python
# Questo programma implementa un server TCP di base che accetta connessioni,
# riceve dati dal client, e risponde con un messaggio fisso.

import socket  # Modulo per la programmazione socket in Python

# Creazione del socket TCP
# AF_INET: Famiglia di indirizzi IPv4
# SOCK_STREAM: Tipo di socket per TCP (flusso affidabile)
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Associazione del socket a un indirizzo e porta
# 'localhost' significa che il server ascolterà solo su questa macchina
# 12345 è la porta su cui il server sarà disponibile
server_socket.bind(('localhost', 12345))

# Il server inizia ad ascoltare le connessioni in entrata
# Il parametro 5 indica il numero massimo di connessioni in coda
server_socket.listen(5)
print("Server in ascolto su porta 12345...")

# Ciclo infinito per accettare e gestire connessioni multiple
while True:
    # accept() blocca l'esecuzione fino a quando un client si connette
    # Restituisce un nuovo socket per la comunicazione e l'indirizzo del client
    client_socket, client_address = server_socket.accept()
    print(f"Connessione stabilita con il client: {client_address}")

    # Ricezione dei dati dal client
    # recv(1024) riceve fino a 1024 byte di dati
    # Se non ci sono dati, recv() restituisce una stringa vuota
    data = client_socket.recv(1024)
    print(f"Dati ricevuti dal client: {data.decode()}")

    # Invio di una risposta al client
    # send() richiede dati in formato bytes, quindi usiamo una stringa byte
    client_socket.send(b"Hello from server!")

    # Chiusura della connessione con il client
    # È importante chiudere il socket per liberare risorse
    client_socket.close()