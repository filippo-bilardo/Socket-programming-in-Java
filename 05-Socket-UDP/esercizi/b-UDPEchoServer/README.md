# b — Echo UDP con timeout

## Obiettivo e protocollo

Implementa un server che restituisce esattamente ogni datagramma ricevuto, massimo 1024 byte. Più client devono poter usare il servizio; indirizzo e porta della risposta sono quelli della richiesta. I messaggi sono testo UTF-8, anche vuoto.

Il client conserva il peer esplicito, valida la sorgente della risposta e attende al massimo 2 s e non tiene più di una richiesta in attesa. Dopo timeout termina la sessione per non associare una risposta tardiva al messaggio successivo. `bye` viene inviato e riceve l'echo, poi termina **solo il client**; il server resta disponibile. EOF sulla console chiude solo il client senza spedire un comando.

## Procedura

Scrivi la tua versione, poi confrontala con [Server.java](Server.java) e [Client.java](Client.java). Il riferimento usa porta 9876; i parametri permettono di cambiarla.

```sh
javac -encoding UTF-8 Server.java Client.java
java Server 9876
```

In un altro terminale:

```sh
java Client localhost 9876
```

Nel ricevitore riutilizza il buffer senza limitare i pacchetti successivi alla lunghezza di quello precedente. Rifiuta datagrammi oltre il massimo. Non risolvere il nome inverso del mittente per ogni pacchetto se non serve al servizio.

## Prove obbligatorie

- Client 1 invia un messaggio corto, poi uno lungo con accenti: nessun troncamento.
- Client 2 riceve solo le proprie risposte.
- `bye` chiude un client; un terzo client può ancora usare il server.
- Un datagramma vuoto riceve una risposta vuota, senza essere interpretato come disconnessione.
- 1024 byte sono accettati; 1025 byte vengono scartati e il client va in timeout.
- Senza server, il client termina per timeout/errore ICMP, non attende indefinitamente.

Consegna sorgenti, descrizione del protocollo e tabella delle prove. Per ogni timeout scrivi “esito non noto”, senza dedurre che la richiesta non sia stata elaborata.

Estensione: aggiungi ID richiesta, ritrasmissioni limitate e deduplicazione. Non estendere il comportamento dell'echo a operazioni con effetti sullo stato senza valutare l'idempotenza.

[Indice esercizi](../README.md) · [Dopo: Daytime](../c-DaytimeServerUDP/README.md)
