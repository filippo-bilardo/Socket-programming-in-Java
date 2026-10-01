# Esempio 02: Server TCP essenziale che stampa le richieste HTTP
# Apri http://localhost:8765 oppure esegui: curl http://localhost:8765
# Per semplicità legge un solo blocco: la richiesta potrebbe essere incompleta.
# (c) Filippo Bilardo

import socket

# 1. Creazione del socket e ascolto sulla porta 8765.
with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server_socket:
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server_socket.bind(('localhost', 8765))
    server_socket.listen(5)
    print("Server in ascolto su http://localhost:8765", flush=True)

    while True:
        # 2. Connessione del client e stampa dei dati ricevuti.
        client_socket, client_address = server_socket.accept()
        with client_socket:
            richiesta = client_socket.recv(4096)
            if not richiesta:
                continue
            print(richiesta.decode('utf-8', errors='replace'), flush=True)

            # 3. Risposta HTTP; il blocco with chiude poi la connessione.
            client_socket.sendall(
                b"HTTP/1.1 200 OK\r\n"
                b"Content-Type: text/plain; charset=utf-8\r\n"
                b"Connection: close\r\n"
                b"\r\n"
                b"Richiesta ricevuta! Ciao! Controlla il terminale del server.\n"
            )
