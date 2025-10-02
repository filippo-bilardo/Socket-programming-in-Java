# Esempio 02: Client TCP Semplice in Python
# Questo programma implementa un client TCP che si connette a un server,
# invia un messaggio e riceve una risposta.

import socket  # Modulo per la programmazione socket

# Creazione del socket TCP per il client
# AF_INET: Per indirizzi IPv4
# SOCK_STREAM: Per connessione TCP affidabile
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Connessione al server
# Specifica l'indirizzo IP (o hostname) e la porta del server
server_address = ('localhost', 12345)
client_socket.connect(server_address)
print("Connessione al server stabilita con successo")

# Invio di un messaggio al server
# send() richiede dati in formato bytes, quindi codifichiamo la stringa
message = "Hello, Server!"
client_socket.send(message.encode())
print(f"Messaggio inviato al server: {message}")

# Ricezione della risposta dal server
# recv() riceve fino a 1024 byte; se la risposta è più lunga, potrebbe richiedere multiple chiamate
response = client_socket.recv(1024)
print(f"Risposta ricevuta dal server: {response.decode()}")

# Chiusura della connessione
# È essenziale chiudere il socket per liberare risorse di rete
client_socket.close()
print("Connessione chiusa")