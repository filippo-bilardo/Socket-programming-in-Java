# Echo UDP esatto — Java e Node.js

Un messaggio è un datagramma UTF-8 di massimo 1024 byte. Il server restituisce gli stessi byte, senza prefissi né timestamp. Il testo ECHO stampato dal client è solo presentazione sulla console. Una risposta vuota è valida.

```sh
javac -encoding UTF-8 EchoUDP.java
java EchoUDP server 9999
```

In un altro terminale:

```sh
java EchoUDP client localhost 9999 "prova"
```

La versione Node.js è interoperabile:

```sh
node echo-udp.js server 9999
```

```sh
node echo-udp.js client 127.0.0.1 9999 "prova"
```

Avvia una sola implementazione server sulla porta scelta. Il client Java invia al peer esplicito e valida indirizzo/porta della risposta; quello Node.js usa connect. L’attesa massima è di 2 secondi, anche per payload vuoti. Senza l'ultimo argomento legge righe dalla console; `quit` termina solo il client. Dopo un timeout termina la sessione: il protocollo non ha un identificativo per riconoscere una risposta tardiva. Il metodo Java sendMessage propaga gli errori al chiamante; chi usa direttamente l'API deve chiudere il client su timeout.

Il server scarta i datagrammi oltre 1024 byte. La versione Java ripristina la capacità del DatagramPacket prima di ogni receive, così un datagramma corto non tronca il successivo più lungo. Ctrl+C arresta il server; Java stop chiude il socket per sbloccare l'attesa.

Prove: più client, corto→lungo, accenti, payload vuoto, 1024/1025 byte, timeout, Java→Node e Node→Java. Non interpretare il timeout come prova certa che la richiesta non sia stata elaborata.

[Indice esempi](../README.md) · [Esercizio echo](../../esercizi/b-UDPEchoServer/README.md)
