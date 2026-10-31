# Esercizio b — HelloServer HTTP

Il server completo stampa la riga di richiesta e gli header HTTP, poi risponde
con `200 OK` e un messaggio testuale. È un esempio didattico di GET/HEAD, con
una richiesta per connessione e un client per volta. Non implementa HTTPS o
un server HTTP completo.

Dalla cartella dell'esempio compila e avvia:

```bash
javac -encoding UTF-8 TcpServer.java
java TcpServer
```

In un altro terminale prova:

| Operazione | PowerShell Windows | Bash Linux / Web4Student |
|---|---|---|
| GET con header di risposta | `curl.exe -i http://127.0.0.1:2223/` | `curl -i http://127.0.0.1:2223/` |
| HEAD, senza corpo | `curl.exe -I http://127.0.0.1:2223/` | `curl -I http://127.0.0.1:2223/` |
| Metodo non supportato | `curl.exe -i -X POST http://127.0.0.1:2223/` | `curl -i -X POST http://127.0.0.1:2223/` |

GET e HEAD devono restituire 200; POST deve restituire 405 e `Allow: GET, HEAD`.
Puoi anche aprire `http://127.0.0.1:2223/` nel browser del computer del server.
Su Web4Student esegui curl nella sessione SSH: il server ascolta sul loopback
remoto, che non è quello del PC Windows.

La porta predefinita è **2223**. Puoi cambiarla con `java TcpServer 2224`,
aggiornando anche l'URL del client. Ctrl+C termina il server.

## Attività

Individua status line, header, riga vuota e corpo. Spiega perché si leggono
gli header fino alla riga vuota e perché l'echo della riga `GET / HTTP/1.1`
non costituisce una risposta HTTP valida. Confronta la lunghezza dichiarata
con i byte effettivi del corpo e verifica che HEAD non invii il corpo.

[Indice del modulo](../../README.md)
