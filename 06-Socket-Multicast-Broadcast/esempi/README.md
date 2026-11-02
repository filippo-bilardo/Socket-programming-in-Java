# Esempi — Broadcast, multicast e discovery

JDK 11+ e Node.js per il confronto fra linguaggi. Nessuna libreria esterna. Compila dalla cartella del singolo esempio con `javac -encoding UTF-8 *.java`; in multicast includi il file MulticastSupport.java presente nella stessa cartella.

Le varianti Node.js usano l'utility `udp-cli.js` nella cartella superiore: conserva la struttura delle directory. `npm run sender` e `npm run receiver`, dove presenti, sono scorciatoie; non servono npm install né package2.json. Per le prove iniziali usa i comandi espliciti sotto.

## Broadcast

| Cartella | Ricevitore | Mittente |
| --- | --- | --- |
| [01.01-broadcast-java](01.01-broadcast-java/README.md) | `java BroadcastReceiver 5000` | `java BroadcastSender 5000 2000 255.255.255.255` |
| [01.02-broadcast-js](01.02-broadcast-js/README.md) | `node broadcast-receiver.js 5000` | `node broadcast-sender.js 5000 2000 255.255.255.255` |

Mittente: porta, intervallo in ms, indirizzo, conteggio opzionale (0 = senza limite). Ricevitore: porta e conteggio opzionale. Il payload è `BROADCAST|Messaggio #N|Timestamp: T`, UTF-8. Per i test finiti usa per esempio conteggio 3 su entrambi i lati. Sullo stesso host avvia un solo ricevitore broadcast per porta; fra più host avvia un ricevitore su ciascun host.

## Multicast

Imposta MULTICAST_IFACE se necessario, seguendo la [guida](../09-Broadcast-Multicast.md). Avvia i ricevitori prima del mittente. Gli argomenti delle due famiglie sono diversi e sono indicati nei rispettivi README.

| Cartella | Ricevitore | Mittente |
| --- | --- | --- |
| [02.01-multicast-base](02.01-multicast-base/README.md) | `java Receiver` oppure `node receiver.js` | `java Sender "Caffè"` oppure `node sender.js "Caffè"` |
| [02.02-multicast-java](02.02-multicast-java/README.md) | `java MulticastReceiver 239.255.0.1 5000` | `java MulticastSender 239.255.0.1 5000 2000 1` |
| [02.03-multicast-js](02.03-multicast-js/README.md) | `node multicast-receiver.js 239.255.0.1 5000` | `node multicast-sender.js 239.255.0.1 5000 2000 1` |

La famiglia base usa gruppo 239.255.0.1, porta 9876 e un messaggio di testo. La famiglia configurabile usa per default porta 5000 e messaggi con numero, timestamp e TTL. Il TTL indica hop, non una regione geografica. Gli alias BaseSender/BaseReceiver nella cartella Java configurabile usano entrambi 239.255.0.1:19876 e tre messaggi per una prova finita.

Prova due ricevitori e incrocia Java/Node.js. Verifica corto→lungo, UTF-8 e rifiuto dei messaggi oltre 1024 byte. La consegna resta best effort; una prova finita può andare in timeout se mancano datagrammi.

## Discovery nella cartella corrente

```sh
javac -encoding UTF-8 NetworkDiscoveryBroadcast.java ServiceDiscoveryMulticast.java MulticastSupport.java
```

- Broadcast: `java NetworkDiscoveryBroadcast respond`, poi in un altro terminale `java NetworkDiscoveryBroadcast scan 192.168.1.0/24`, sostituendo il CIDR. La porta predefinita è 8891; `scan 127.0.0.1` verifica localmente il protocollo tramite unicast. Trova soltanto i responder dell'esempio.
- Multicast: `java ServiceDiscoveryMulticast discoverer`, poi `java ServiceDiscoveryMulticast announcer WebLab HTTP 8080`. Gruppo 239.255.0.2:8890, annunci ogni 5 s, scadenza locale dopo oltre 15 s senza annunci. L'annunciatore non avvia un server HTTP.
- Prova finita: `java ServiceDiscoveryMulticast both WebLab HTTP 8080 6`.

La [guida discovery](../10-Discovery.md) specifica formato, correlazione delle risposte e identità delle istanze.

[Esercizi](../esercizi/README.md) · [Indice modulo](../README.md)
