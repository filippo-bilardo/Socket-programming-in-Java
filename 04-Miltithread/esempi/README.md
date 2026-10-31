# Esempi del modulo 04

## Compilazione

Apri un terminale nella cartella dell'esempio, poi compila le sue classi insieme. Non compilare tutte le sottocartelle nello stesso classpath: contengono nomi uguali con implementazioni diverse.

```sh
javac -encoding UTF-8 *.java
```

Apri il server in un terminale e i client in terminali separati. I comandi seguenti funzionano anche in PowerShell; Ctrl+C termina un server senza comando locale di arresto.

| Cartella | Server | Client | Porta e protocollo |
| --- | --- | --- | --- |
| [es01-maiusc](es01-maiusc/README_nodejs.md) | `java TcpServer` oppure `node TcpServer.js` | `java TcpClient` oppure `node TcpClient.js` | 8765, una riga UTF-8 per connessione. Java termina dopo il primo client; Node resta in ascolto. |
| [es03-completo](es03-completo/ServerTCPBase.java) | `java ServerTCPBase 8080` | `java ClientTCPBase localhost 8080` | 8080, server sequenziale di confronto; benvenuto terminato da END, poi echo a righe. |
| [es04-Thread](es04-Thread/MultithreadedTcpServer.java) | `java MultithreadedTcpServer` | `java TcpClient` | 8698, `writeUTF/readUTF`, comando `end`; non compatibile con netcat o client testuali. |
| [es04-Runnable](es04-Runnable/TcpServer.java) | `java TcpServer` | `java TcpClient` | 8080, echo a righe; `quit` chiude il client. |
| [es04-Runnable2](es04-Runnable2/TcpServer.java) | `java TcpServer` | `java TcpClient` | 8080, stessa funzione con gestore nella classe principale. |
| [es04-Lambda](es04-Lambda/TcpServer.java) | `java TcpServer` oppure `java TcpServer_v2` | `java TcpClient` | 8765, echo a righe; avvia una sola variante alla volta. |
| [es05-ChatMultithread](es05-ChatMultithread/README.md) | `java Server` | `java Client` | 9876, echo di byte fino a EOF; il client chiude solo l'invio con shutdownOutput, poi legge tutta la risposta. Il README propone tre echo a righe sulla porta 5000 e il broadcast come estensione. |
| [es06](es06/README.md) | Codice nel README: `java MultiThreadServer` | `java SimpleClient` | 5000, esempio guidato; salva prima le classi nei file omonimi. |

[es02-ftp](es02-ftp/README.md) contiene due helper senza main: integrali in un programma che crei i socket.

## Esempi avanzati nella cartella corrente

Compila questi file nella cartella `esempi`, separatamente dalle sottocartelle:

```sh
javac -encoding UTF-8 ConnectionPoolServer.java CustomProtocolChat.java FileTransfer.java
```

### Pool con connessioni e coda limitate

```sh
java ConnectionPoolServer 8080 50 5 20
```

Parametri: porta, limite complessivo di connessioni, worker core, worker massimi. Deve valere `1 <= core <= max-thread <= max-conn`. La coda ha capacità `max-conn - max-thread`; se questa vale zero si usa una consegna diretta senza coda. Con tutti i worker occupati, il pool riempie la coda e poi cresce fino al massimo. I rifiuti chiudono il socket. Le connessioni inattive scadono dopo 30 secondi. Il numero di task in coda è distinto dai worker attivi.

Collegati con un client TCP testuale e invia righe: `info`, `stats`, `echo ciao`, `quit`. Ctrl+C chiude anche i socket ancora in coda.

### Chat con protocollo binario

```sh
java CustomProtocolChat server 8080
```

In altri due terminali, dalla stessa cartella:

```sh
java CustomProtocolChat client localhost 8080 Alice
java CustomProtocolChat client localhost 8080 Bob
```

Ogni messaggio contiene int tipo, due campi Java writeUTF (mittente e contenuto) e long timestamp in secondi UTC. Il server ignora il mittente dichiarato nei messaggi di chat e usa il nome registrato; un duplicato viene rifiutato senza disconnettere l'utente originario. Massimo 50 gestori, senza coda; login entro 10 secondi e inattività massima di 5 minuti. Scrivi `quit` per uscire. Se il ricevitore termina mentre la console aspetta input, premi Invio per completare l'uscita. writeUTF usa UTF modificato Java e ha un limite di 65535 byte per campo: non è un protocollo testuale da inviare con curl.

Gli invii sincroni della chat sono adatti al laboratorio: un destinatario lento può ritardare un broadcast. Un'architettura con code di uscita separate è un approfondimento successivo.

### Trasferimento file: base sequenziale

```sh
java FileTransfer server 8080 ricevuti
```

In un altro terminale:

```sh
java FileTransfer client localhost 8080 documento.txt
```

Il server accetta un semplice nome senza componenti di percorso e una dimensione non negativa. Riceve su un file temporaneo nella destinazione; lo pubblica solo dopo il controllo MD5. Il checksum rileva errori del trasferimento, non autentica il mittente. File incompleti e checksum errati non sostituiscono la destinazione. Il file finale può sostituire un file omonimo. Timeout di lettura: 30 secondi. Il server gestisce un trasferimento alla volta: trasformarlo in un servizio concorrente richiede anche di definire la politica per nomi simultanei uguali.

[Indice del modulo](../README.md) · [Esercizi](../esercizi/README.md)
