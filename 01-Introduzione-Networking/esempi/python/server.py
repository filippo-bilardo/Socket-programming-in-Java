import socket

# 1. CREAZIONE del socket
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# 2. BINDING all'indirizzo locale
server_socket.bind(('localhost', 8888))

# 3. LISTENING per connessioni in entrata
server_socket.listen(5)  # Coda di max 5 connessioni

# 4. ACCEPTING connessioni
while True:
    client_sock, addr = server_socket.accept()
    
# 5. COMUNICAZIONE bidirezionale
    data = client_sock.recv(1024)
    print(f"Received: {data} from {addr}")
    client_sock.send(b"Response")
    
# 6. CHIUSURA connessione
    client_sock.close()