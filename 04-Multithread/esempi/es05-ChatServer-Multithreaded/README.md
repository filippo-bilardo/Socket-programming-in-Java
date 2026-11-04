# Chat server multithread — Java e Node.js

Implementazione completa della [traccia d — Chat server multithread](../../esercizi/d-ChatServer-Multithreaded/README.md). Gli utenti ricevono nomi automatici, possono scambiarsi messaggi, vedere la lista dei connessi e uscire dalla chat. Le versioni Java e JavaScript usano lo stesso protocollo TCP e possono essere combinate.

La porta predefinita è **9999**. Questo esempio realizza il broadcast; il precedente [echo server](../es04-EchoServerMultithread/README.md) risponde invece soltanto al mittente.

## File e requisiti

| File | Ruolo |
| --- | --- |
| [ChatServer.java](ChatServer.java) | Listener, registro sincronizzato, broadcast, statistiche e console del server |
| [ClientHandler.java](ClientHandler.java) | Un Runnable per connessione, parsing dei comandi e scritture coordinate |
| [ChatClient.java](ChatClient.java) | Ricezione dal socket e invio dalla console su due thread |
| [ChatServer.js](ChatServer.js) | Stesso servizio TCP con l'event loop di Node.js |
| [ChatClient.js](ChatClient.js) | Console e ricezione della chat gestite con eventi |

Servono **JDK 11+** per Java e **Node.js** per JavaScript. Non ci sono dipendenze da installare con npm. I comandi di compilazione e avvio funzionano anche in PowerShell. Eseguili dalla cartella di questo esempio, separatamente dagli altri esempi del modulo.

## Avvio della versione Java

Compila le tre classi:

```sh
javac -encoding UTF-8 ChatServer.java ClientHandler.java ChatClient.java
```

Nel primo terminale avvia il server:

```sh
java ChatServer
```

In altri due o tre terminali avvia un client per ciascuno:

```sh
java ChatClient
```

Il primo utente riceve `Client1`, poi `Client2`, `Client3` e così via. Non viene chiesto un nome alla tastiera. Il numero identifica un'accettazione del server, non il numero di utenti attivi: dopo un'uscita il nome non viene riutilizzato durante la stessa esecuzione.

## Avvio della versione Node.js e prove miste

Ferma il server Java prima di usare la stessa porta. Nel terminale del server:

```sh
node ChatServer.js
```

Negli altri terminali:

```sh
node ChatClient.js
```

Puoi anche lasciare il server Java e usare contemporaneamente un client Java e uno Node.js:

```sh
java ChatClient
```

```sh
node ChatClient.js
```

Vale anche la combinazione inversa: `ChatServer.js` accetta sia `ChatClient.java` sia `ChatClient.js`. Si avvia un solo server sulla porta scelta; i client si collegano a quel server.

### Host e porta personalizzati

Il primo argomento del server è la porta. I client accettano host e porta, in quest'ordine:

```sh
java ChatServer 10000
java ChatClient localhost 10000
node ChatClient.js localhost 10000
```

Oppure, in un'altra prova:

```sh
node ChatServer.js 10000
java ChatClient localhost 10000
node ChatClient.js localhost 10000
```

Per un server remoto sostituisci `localhost` con il suo nome, per esempio `web4student`, e usa una porta assegnata al tuo gruppo. `localhost` indica sempre il computer che esegue il client.

## Protocollo comune

Ogni messaggio è una riga **UTF-8**. I programmi inviano `\n` e accettano anche `\r\n`. Una scrittura TCP può contenere più righe; una riga può arrivare in più letture. `BufferedReader` e `readline` ricostruiscono il testo prima di elaborarlo.

| Evento o richiesta | Risposta |
| --- | --- |
| Connessione | Prima riga: `[SERVER]: Benvenuto Client1! Client connessi: 1` |
| Ingresso di un altro utente | `[SERVER]: Client2 si è connesso. Client online: 2` |
| Invio di `ciao` da Client1 | A Client1: `[TU]: ciao`; agli altri: `[Client1]: ciao` |
| `/list` | Una sola riga: `[SERVER]: Utenti online: Client1, Client2` |
| `/quit` | `[SERVER]: Arrivederci Client1`, poi chiusura della connessione |
| Uscita di un altro utente | `[SERVER]: Client1 si è disconnesso. Client online: 1` |
| Comando con slash non riconosciuto | `[SERVER]: Comando non riconosciuto` |
| Riga vuota | Ignorata, senza risposta |
| EOF della console client | Invio automatico di `/quit` |
| EOF o errore della connessione | Rimozione dell'utente e notifica agli altri |

Il mittente riceve una conferma `[TU]`, non una seconda copia del proprio messaggio con il suo username. La lista è ordinata per numero del client, quindi Client10 segue Client9. Le righe con soli spazi sono messaggi; i comandi sono esattamente `/list` e `/quit`, in minuscolo e senza spazi aggiuntivi.

Il benvenuto viene inviato prima di rendere il gestore visibile ai broadcast. Il conteggio nel benvenuto Java è indicativo: altri gestori possono entrare o uscire tra lettura del conteggio e registrazione. `/list` rappresenta una copia del registro al momento della richiesta; la presenza di un utente non garantisce che resterà connesso dopo la risposta.

Una singola notifica o risposta occupa una riga: non occorre un terminatore `END`. Dopo un messaggio normale, il client continua ad ascoltare gli eventi della chat e non aspetta la chiusura del socket per mostrarli. Non è un servizio HTTP o WebSocket: il client interattivo si esegue dal terminale.

## Esempio con due utenti

Dopo aver connesso due client, nel terminale di Client1 digita `ciao a tutti`:

```text
[TU]: ciao a tutti
```

Client2 riceve:

```text
[Client1]: ciao a tutti
```

Client2 digita `/list`:

```text
[SERVER]: Utenti online: Client1, Client2
```

Quando Client1 digita `/quit`, riceve il saluto finale e termina. Client2 vede:

```text
[SERVER]: Client1 si è disconnesso. Client online: 1
```

L'arrivo delle notifiche può alternarsi alle righe digitate nella console. I messaggi del protocollo rimangono distinti; nei test concorrenti non assumere un ordine globale fra mittenti diversi.

## Console del server e statistiche

Nella **console locale del server**, Java oppure Node.js:

- `STATS` stampa il numero di utenti registrati e i messaggi chat accettati.
- `SHUTDOWN` ferma l'accettazione e chiude tutte le connessioni, incluse quelle non ancora registrate.
- Ctrl+C richiede lo stesso arresto. Anche SIGTERM viene gestito dalla versione Node.js e dallo shutdown hook Java.

Esempio di statistica:

```text
[STATISTICHE] Client attivi: 3 | Messaggi chat: 12
```

Un messaggio chat incrementa il totale una sola volta, indipendentemente dal numero di destinatari. Comandi, righe vuote, benvenuti e notifiche non vengono contati. La statistica viene mostrata anche dopo gli ingressi, le uscite e i messaggi.

`SHUTDOWN` inviato **nella chat** è testo ordinario: non arresta il server. La console è locale al processo; quando il server gira in SSH, scrivi il comando nel suo terminale remoto. EOF della console del server non ferma il servizio, che può continuare anche in background.

L'arresto annulla le sessioni attive chiudendo i socket; non garantisce la consegna dei messaggi ancora in transito. I client mostrano la chiusura e terminano anche se l'utente non preme un altro Invio. Al successivo avvio del server i nomi ripartono da Client1 e i contatori da zero.

## Come viene gestita la concorrenza

| Aspetto | Java | Node.js |
| --- | --- | --- |
| Nuova connessione | Il listener crea un thread con `ClientHandler` | Il server invoca il callback della connessione |
| Registro utenti | Set protetto da `stateLock` | Map modificata dai callback dell'event loop |
| Broadcast | Copia del registro sotto lock, invii dopo il rilascio | Copia dei destinatari e scritture asincrone |
| Scrittura verso un utente | `outputLock` protegge testo, newline e flush | Ogni `socket.write` accoda una riga completa |
| Client interattivo | Main di ricezione e thread daemon per la console | Un `readline` per la rete e uno per la console |
| Disconnessione | Chiusura idempotente del socket e rimozione dal registro | Flag `closed`, rimozione dal registro e chiusura del socket |

Java non mantiene il monitor del registro durante i broadcast: un invio lento non deve trattenere il lock di registrazione e rimozione. Il lock del singolo output impedisce a due mittenti di sovrapporre i campi di una riga. Chiudere il socket durante l'arresto sblocca i gestori che attendono dati o stanno scrivendo.

Nel client Java il main è l'unico lettore del socket; il thread della console è daemon, così una tastiera in attesa non trattiene la JVM dopo la disconnessione. La versione Node.js chiude l'interfaccia della console quando termina la connessione.

Node.js non crea un thread JavaScript per ogni utente. I callback non fanno calcoli o attese bloccanti: letture, invii e input della console sono coordinati attraverso gli eventi. Non occorrono Worker Threads per questa versione della chat.

## Prove da ripetere

1. Connetti tre utenti: verifica nomi distinti e notifiche agli altri.
2. Invia un testo con accenti, emoji e due punti: il mittente riceve `[TU]`, gli altri `[ClientN]`.
3. Esegui `/list`, un comando non valido e `/quit`.
4. Chiudi bruscamente un client: verifica una sola notifica di uscita e una lista aggiornata.
5. Usa insieme client Java e Node.js su ciascuna delle due versioni del server.
6. Invia più righe nella stessa scrittura e una riga UTF-8 in più scritture; verifica che non venga spezzata in più messaggi.
7. Con più client invia messaggi simultanei: ogni destinatario deve ricevere una riga completa per messaggio, senza duplicati o perdite.
8. Lascia aperta la console di un client senza digitare e usa `SHUTDOWN` nel server: entrambi i processi devono terminare.
9. Controlla `STATS`: `/list` non incrementa i messaggi, mentre ogni testo non vuoto lo incrementa una volta.

Questa implementazione completa le funzionalità di base della traccia e l'estensione del comando locale di arresto. Le altre estensioni proposte dalla traccia, come messaggi privati, stanze, GUI, autenticazione e persistenza, sono sviluppi successivi.

È un esempio introduttivo con thread per client in Java e scritture asincrone in Node.js: non impone un limite globale alle connessioni o alle code di uscita. Un destinatario lento può ritardare il broadcast Java; in Node.js gli invii vengono accodati. Per controllare il carico servono limiti e una politica per i client lenti, come discusso nella [gestione avanzata](../../04-Multithreading-Avanzato.md). La chat non applica timeout di inattività agli utenti già connessi.

## Riferimenti

- [Traccia d — Chat server multithread](../../esercizi/d-ChatServer-Multithreaded/README.md)
- [Sincronizzazione e risorse condivise](../../03-Sincronizzazione-Risorse-Condivise.md)
- [Node.js: socket TCP](https://nodejs.org/api/net.html)
- [Node.js: readline e righe UTF-8](https://nodejs.org/api/readline.html)

[Indice degli esempi](../README.md) · [Indice del modulo](../../README.md)
