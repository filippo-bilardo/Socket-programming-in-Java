# Esempio 03: Server TCP con lettura completa delle richieste HTTP
# Apri http://localhost:8765 nel browser oppure esegui:
# curl -v http://localhost:8765/saluto?nome=Mario
# curl -d 'nome=Mario' http://localhost:8765
# Server didattico: una richiesta per connessione, senza HTTPS o chunked encoding.
# (c) Filippo Bilardo
# versione 2.0 - 01/10/26 - stampa delle richieste HTTP

import socket


def ricevi_richiesta(client_socket):
    """Legge gli header e l'eventuale corpo indicato da Content-Length."""
    richiesta = b""

    # TCP trasporta un flusso: una richiesta può arrivare in più recv().
    # In HTTP una riga vuota separa gli header dal corpo della richiesta.
    while b"\r\n\r\n" not in richiesta:
        dati = client_socket.recv(4096)
        if not dati:
            return richiesta
        richiesta += dati

    header, corpo = richiesta.split(b"\r\n\r\n", 1)
    lunghezza = 0
    for riga in header.split(b"\r\n")[1:]:
        nome, valore = riga.split(b":", 1)
        if nome.lower() == b"transfer-encoding":
            raise ValueError("Transfer-Encoding non supportato: usare Content-Length")
        if nome.lower() == b"content-length":
            lunghezza = int(valore.strip())
            if lunghezza < 0:
                raise ValueError("Content-Length non valido")

    # Per POST/PUT leggiamo anche il corpo, senza aspettare che il client chiuda.
    while len(corpo) < lunghezza:
        dati = client_socket.recv(min(4096, lunghezza - len(corpo)))
        if not dati:
            raise ValueError("Corpo della richiesta incompleto")
        corpo += dati

    return header + b"\r\n\r\n" + corpo[:lunghezza]


# 1. Creazione, binding e ascolto del socket TCP.
# I blocchi with chiudono automaticamente i socket all'uscita.
with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server_socket:
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server_socket.bind(('localhost', 8765))
    server_socket.listen(5)
    print("Server HTTP in ascolto su http://localhost:8765 (Ctrl+C per terminare)",
          flush=True)

    try:
        while True:
            # 2. Accettazione di una connessione dal browser o da curl.
            client_socket, client_address = server_socket.accept()
            with client_socket:
                client_socket.settimeout(5)
                try:
                    richiesta = ricevi_richiesta(client_socket)
                    if not richiesta:
                        continue

                    # 3. Stampa di metodo, percorso, header ed eventuale corpo.
                    print(f"\n--- Richiesta HTTP da {client_address} ---", flush=True)
                    print(richiesta.decode('utf-8', errors='replace'), flush=True)

                    # 4. Risposta HTTP: stato, header, riga vuota e corpo.
                    corpo = "Richiesta ricevuta! Controlla il terminale del server.\n".encode('utf-8')
                    header = (
                        "HTTP/1.1 200 OK\r\n"
                        "Content-Type: text/plain; charset=utf-8\r\n"
                        f"Content-Length: {len(corpo)}\r\n"
                        "Connection: close\r\n"
                        "\r\n"
                    ).encode('ascii')
                    # HEAD restituisce gli stessi header di GET, senza corpo.
                    if richiesta.startswith(b"HEAD "):
                        corpo = b""
                    client_socket.sendall(header + corpo)
                except (OSError, ValueError) as errore:
                    print(f"Errore nella richiesta da {client_address}: {errore}", flush=True)
            # La connessione viene chiusa; il server attende il prossimo client.
    except KeyboardInterrupt:
        print("\nServer terminato.")
