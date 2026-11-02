# Discovery applicativo su UDP

Il discovery cerca istanze di un'applicazione che parlano un protocollo concordato. Non è una scansione completa di tutti gli host, non prova che un dispositivo sia spento e non costruisce automaticamente la topologia fisica della rete.

## Richiesta broadcast, risposta unicast

[NetworkDiscoveryBroadcast](esempi/NetworkDiscoveryBroadcast.java) usa la porta 8891. Lo scanner apre una porta effimera e invia `DISCOVER|UUID` all'indirizzo di broadcast scelto. Ogni responder riconosce la richiesta e risponde con un datagramma unicast alla sorgente effettiva della richiesta:

```text
DEVICE|UUID|nome-host-codificato|sistema-operativo-codificato
```

UUID correla la risposta alla scansione corrente. Nome e sistema operativo sono codificati con URL encoding UTF-8, così il separatore `|` non rende ambiguo il formato. Lo scanner accetta soltanto risposte con il nonce corretto e la porta sorgente del servizio. La finestra complessiva di ricezione è di 2 secondi; ogni receive usa il tempo ancora disponibile, così traffico aggiuntivo non prolunga indefinitamente la scansione.

Il campo `risposta_ms` misura il tempo osservato dall'invio della richiesta, non soltanto la durata dell'ultima receive. Comprende rete e attesa/elaborazione del responder. Le risposte vengono deduplicate per indirizzo sorgente.

```sh
javac -encoding UTF-8 NetworkDiscoveryBroadcast.java
java NetworkDiscoveryBroadcast respond
```

In un altro terminale:

```sh
java NetworkDiscoveryBroadcast scan 192.168.1.0/24
```

Sostituisci il prefisso con quello reale. Il codice calcola il broadcast del CIDR, senza assumere /24. Puoi passare direttamente l'indirizzo di broadcast oppure 127.0.0.1 per verificare localmente il protocollo prima della LAN. La porta opzionale deve essere uguale sui due lati. Ctrl+C termina il responder chiudendo il socket.

## Annunci multicast e scadenza

[ServiceDiscoveryMulticast](esempi/ServiceDiscoveryMulticast.java) invia ogni 5 secondi sul gruppo `239.255.0.2`, porta 8890, TTL 1:

```text
ANNOUNCE|nome-codificato|tipo-codificato|porta-del-servizio
```

L'indirizzo del servizio nella cache è la sorgente del datagramma, non un indirizzo dichiarato in un campo non verificato. Il nome e il tipo hanno un massimo di 64 caratteri; la porta è fra 1 e 65535. Messaggi malformati vengono ignorati.

Il ricevitore distingue le istanze per nome, tipo, indirizzo sorgente e porta. Ogni annuncio valido aggiorna l'istante **locale** di ricezione. Se non arrivano annunci per più di 15 secondi, la cache rimuove l'istanza usando il valore effettivamente osservato: la pulizia di una vecchia voce non deve cancellare un aggiornamento appena arrivato.

Si usa un orologio monotono per misurare l'età. Il timestamp del mittente non è adatto alla scadenza, perché gli orologi dei PC possono essere diversi. Una scadenza indica assenza recente di annunci, non una diagnosi certa del motivo.

Dalla cartella esempi:

```sh
javac -encoding UTF-8 ServiceDiscoveryMulticast.java MulticastSupport.java
java ServiceDiscoveryMulticast discoverer
```

In un altro terminale:

```sh
java ServiceDiscoveryMulticast announcer WebLab HTTP 8080
```

La porta 8080 è la porta **annunciata**, non quella usata per il discovery. L'annunciatore non crea un server HTTP. Per terminare automaticamente un esperimento, aggiungi la durata in secondi:

```sh
java ServiceDiscoveryMulticast both WebLab HTTP 8080 6
```

Per osservare la scadenza, lascia attivo il discoverer e arresta l'annunciatore; attendi oltre 15 secondi. Imposta MULTICAST_IFACE come descritto nella [guida multicast](09-Broadcast-Multicast.md). Il ricevitore chiude il socket per sbloccare receive e arresta il lavoro periodico alla chiusura.

## Limiti e consegna

Sono protocolli didattici personalizzati, non implementazioni di mDNS/DNS-SD. Gli annunci non autenticano il dispositivo: il nonce è correlazione, non una credenziale. Per il laboratorio prova soltanto i dispositivi e i servizi previsti dall'esercizio e confronta i risultati con i log delle istanze effettivamente avviate.

Consegna cattura e tabella con: richiesta/annuncio, IP sorgente/destinazione, porte, identificativo e istante di ricezione. Distingui ciò che hai osservato da ciò che il protocollo non può dimostrare.

[Indice modulo](README.md) · [Broadcast e multicast](09-Broadcast-Multicast.md) · [Esercizio discovery](esercizi/c-DiscoveryBroadcast/README.md)
