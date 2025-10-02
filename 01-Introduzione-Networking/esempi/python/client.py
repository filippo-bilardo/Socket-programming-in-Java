import socket

# 1. CREAZIONE del socket
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# 2. CONNESSIONE al server
client_socket.connect(('localhost', 8888))

# 3. COMUNICAZIONE
client_socket.send(b"Hello Server")
response = client_socket.recv(1024)
print(f"Received from server: {response}")

# 4. CHIUSURA
client_socket.close()