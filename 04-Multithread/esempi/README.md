# Esempi del modulo 04

## Compilazione e ordine di prova

Apri un terminale nella cartella dell'esempio e compila le sue classi insieme. Ogni cartella ha un classpath indipendente: i nomi `TcpServer` e `TcpClient` si ripetono intenzionalmente. Servono JDK 11+ e codifica UTF-8. Per il server HTTP con package `network` segui invece il comando con `-d build` nel suo README.

```sh
javac -encoding UTF-8 *.java
```

Avvia server e client in terminali separati; i comandi `javac` e `java` valgono anche in PowerShell. Ferma un server prima di provarne un altro sulla stessa porta. Ctrl+C termina i server introduttivi. Prova due client insieme lasciandone uno inattivo.

| Cartella | Server | Client | Porta e protocollo |
| --- | --- | --- | --- |
| [es01-Thread](es01-Thread/MultithreadedTcpServer.java) | `java MultithreadedTcpServer` | `java TcpClient` | 8698, `writeUTF/readUTF`, `end` riceve echo e chiude; incompatibile con netcat testuale |
| [es02-Runnable](es02-Runnable/TcpServer.java) | `java TcpServer` | `java TcpClient` | 8080, righe UTF-8; `quit` chiude localmente il client, il server rileva EOF |
| [es02-Runnable2](es02-Runnable2/TcpServer.java) | `java TcpServer` | `java TcpClient` | 8080, stesso protocollo; la classe del server implementa anche Runnable |
| [es03-Lambda](es03-Lambda/TcpServer.java) | `java TcpServer` oppure `java TcpServer_v2` | `java TcpClient` | 8765, righe UTF-8; avvia una sola variante alla volta |
| [es04-EchoServerMultithread](es04-EchoServerMultithread/README.md) | `java Server` oppure `node Server.js` | `java Client` | 9876, echo di byte fino a EOF; `shutdownOutput()` conclude l'invio e permette ancora di leggere |
| [es05-ChatServer-Multithreaded](es05-ChatServer-Multithreaded/README.md) | `java ChatServer` oppure `node ChatServer.js` | `java ChatClient` oppure `node ChatClient.js` | 9999, chat UTF-8 a righe, nomi automatici, broadcast, `/list` e `/quit` |
| [es06-ServerWeb](es06-ServerWeb/README.md) | `java -cp build network.TcpServer` | browser, curl o `java -cp build network.TcpClient` | 8765, risposta HTTP testuale; compilazione con `-d build` descritta nel README |

L'esempio `Thread` smette di accettare dopo **999 connessioni totali**, non dopo 999 client simultanei; i gestori esistenti proseguono. Le varianti a righe e il server di byte hanno un timeout di lettura di 30 secondi. Una risposta letta come `null` significa EOF; non è un messaggio da mostrare o a cui rispondere.

La cartella `es04-EchoServerMultithread` contiene un **echo di byte**. Il README confronta tre implementazioni a righe sulla porta 5000; il nuovo [esempio di chat](es05-ChatServer-Multithreaded/README.md) completa il passaggio al broadcast. Per il confronto sequenziale, maiuscole e trasferimento file consulta gli [esempi del modulo TCP](../../03-Socket-TCP/esempi/).

## Pool con connessioni e coda limitate

Nella cartella `esempi`, separatamente dalle sottocartelle:

```sh
javac -encoding UTF-8 ConnectionPoolServer.java CustomProtocolChat.java
java ConnectionPoolServer 8080 50 5 20
```

Parametri: porta, limite complessivo di connessioni ammesse, worker core e worker massimi. Deve valere `1 <= core <= max-thread <= max-conn`. La coda ha capacità `max-conn - max-thread`; se vale zero si usa una consegna diretta senza coda. Il pool riempie prima i worker core, poi la coda, poi cresce fino al massimo. I rifiuti chiudono il socket. Le connessioni in esecuzione inattive scadono dopo 30 secondi; l'attesa in coda non è coperta da quel timeout.

Collegati con un client TCP testuale e invia righe: `info`, `stats`, `echo ciao`, `quit`. Il prompt `> ` non termina con newline: un client automatico deve riconoscerlo, non attendere un'altra riga. Le metriche atomiche sono lette separatamente e rappresentano una vista approssimata durante l'attività. Ctrl+C chiude anche i socket accodati. Qui i thread vengono riutilizzati per gestire connessioni diverse: non si tratta di un pool di socket verso un database.

## Chat con protocollo binario

```sh
java CustomProtocolChat server 8080
```

In altri due terminali, dalla stessa cartella:

```sh
java CustomProtocolChat client localhost 8080 Alice
java CustomProtocolChat client localhost 8080 Bob
```

Ogni messaggio contiene `int tipo`, due campi Java `writeUTF` (mittente e contenuto) e `long timestamp` in secondi UTC. Il server usa il nome registrato come mittente dei messaggi chat; un nome duplicato viene rifiutato senza disconnettere l'utente originario. I nomi distinguono maiuscole e minuscole. Sono ammessi al massimo 50 gestori, senza coda; login entro 10 secondi e inattività massima di 5 minuti.

Scrivi `quit` per uscire. Se il ricevitore termina mentre la console aspetta input, premi Invio per completare l'uscita. `writeUTF` usa UTF modificato Java e ammette al massimo 65535 byte per campo: il protocollo non si invia direttamente con curl. `HEARTBEAT` è un tipo riservato: il client interattivo non genera heartbeat automatici. Non sono previsti autenticazione o cifratura.

Gli invii sincroni sono adatti al laboratorio: un destinatario lento può ritardare il broadcast. Una coda di uscita limitata per destinatario richiede una politica per i client lenti.

[Indice del modulo](../README.md) · [Sincronizzazione](../03-Sincronizzazione-Risorse-Condivise.md) · [Gestione avanzata](../04-Multithreading-Avanzato.md) · [Esercizi](../esercizi/README.md)
