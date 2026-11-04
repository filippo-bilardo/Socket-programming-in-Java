# Server HTTP concorrente essenziale

Ogni socket accettato viene affidato a un thread. Il gestore legge gli header ASCII fino alla riga vuota e risponde con stato 200 e corpo testuale UTF-8. `Content-Length` conta i byte del corpo, mentre `Connection: close` indica che la connessione viene chiusa dopo la risposta. Timeout di lettura: 5 secondi.

I file dichiarano `package network`: dalla cartella corrente compila e avvia così, anche in PowerShell:

```sh
javac -encoding UTF-8 -d build TcpServer.java TcpClient.java
java -cp build network.TcpServer
```

In un secondo terminale, dalla stessa cartella:

```sh
java -cp build network.TcpClient
curl http://localhost:8765/
```

In Windows usa `curl.exe` se `curl` richiama un comando PowerShell diverso. Dal browser apri `http://localhost:8765/`. Su Linux gli script `bash build_and_run_server.sh` e `bash build_and_run_client.sh` compilano invece in `04-Multithread/tmp/serverweb-build`, conservando gli artefatti.

Apri prima una connessione senza inviare header, poi usa curl: il secondo gestore deve rispondere mentre il primo aspetta. Il server è dimostrativo: risponde con lo stesso testo per ogni richiesta completata e non implementa routing, file statici, body, keep-alive o tutti i controlli di un server HTTP completo. `/ACCENDI`, usato dal client Java, non aziona dispositivi.

Il file [canvas-termometro.html](canvas-termometro.html) è una dimostrazione grafica indipendente, da aprire direttamente nel browser. Non riceve misure dal server e non viene servito da questo programma. Collegarlo a una sorgente dati è un'estensione da progettare.

[Indice degli esempi](../README.md) · [Guida ai server multithread](../../02-Server-Multithreaded.md)
