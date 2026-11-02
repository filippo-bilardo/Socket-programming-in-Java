# c — Discovery broadcast con risposta unicast

Progetta uno scanner che cerchi soltanto i responder dell'applicazione di laboratorio. Leggi la [guida discovery](../../10-Discovery.md), poi confronta la soluzione con [NetworkDiscoveryBroadcast.java](../../esempi/NetworkDiscoveryBroadcast.java).

## Requisiti

- Porta condivisa 8891, configurabile su scanner e responder.
- Richiesta DISCOVER con identificativo casuale della scansione.
- Risposta DEVICE al mittente effettivo, con lo stesso identificativo e campi codificati senza ambiguità.
- Calcolo del broadcast a partire dal CIDR reale, senza assumere /24.
- Deadline complessiva di 2 s; risposte non correlate o malformate non devono estenderla.
- Deduplicazione delle risposte dello stesso host e tempo osservato a partire dall'invio.
- Chiusura di tutti i socket e nessun tentativo di dedurre una topologia completa dai soli responder trovati.

## Prove

Compila dalla cartella esempi:

```sh
javac -encoding UTF-8 NetworkDiscoveryBroadcast.java
java NetworkDiscoveryBroadcast respond 8891
```

In un altro terminale:

```sh
java NetworkDiscoveryBroadcast scan 127.0.0.1 8891
```

Questa prima prova usa unicast locale. Ripeti poi con l'indirizzo di broadcast o il CIDR della LAN e responder avviati sui PC concordati. Confronta /24 e /25 nel calcolo del broadcast senza spedire verso reti non coinvolte nella prova.

Verifica responder assente, risposte duplicate, UUID errato e traffico aggiuntivo durante la finestra. La scansione deve terminare intorno alla deadline anche se arrivano dati non validi. Registra richieste/risposte in una cattura e confrontale con i risultati stampati.

Estensione: confronta discovery su richiesta e [annunci multicast](../../esempi/ServiceDiscoveryMulticast.java); arresta l'annunciatore e verifica la scadenza locale dopo oltre 15 s. L'annuncio non crea il servizio e il nonce non è autenticazione.

Consegna protocollo, sorgenti, calcolo del prefisso, comandi e tabella con risultati e limiti osservati.

[Indice esercizi](../README.md) · [Indice modulo](../../README.md)
