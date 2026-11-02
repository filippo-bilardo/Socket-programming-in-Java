# Multicast configurabile — Java

Compila con `javac -encoding UTF-8 *.java`. BaseSender e BaseReceiver sono alias finiti: avvia prima `java BaseReceiver`, poi `java BaseSender`. Entrambi usano 239.255.0.1:19876 e tre messaggi.


```sh
java MulticastReceiver 239.255.0.1 5000 3
```

In un altro terminale:

```sh
java MulticastSender 239.255.0.1 5000 100 1 3
```

Ricevitore: gruppo, porta, conteggio opzionale. Mittente: gruppo, porta, intervallo ms, TTL e conteggio opzionale. Zero o conteggio omesso significa esecuzione continua; il ricevitore termina anche su `bye`. Con conteggio finito è previsto un timeout: UDP non garantisce che tutti i datagrammi arrivino.

Il payload è `MULTICAST|Messaggio #N|Timestamp: T|TTL: X` in UTF-8, massimo 1024 byte. TTL 0 è un valore valido per prove locali dove supportato: non sostituirlo con il default usando un'espressione come `valore || default`. La porta deve essere 1..65535, l'intervallo positivo e il TTL 0..255. Il TTL è un limite di hop e non indica continente, regione o organizzazione.

Imposta MULTICAST_IFACE all'IPv4 dell'interfaccia locale scelta, come nella [guida](../../09-Broadcast-Multicast.md). Inviare non richiede aderire al gruppo; per ricevere serve join sulla stessa interfaccia configurata nel laboratorio. Le implementazioni Java e Node.js della famiglia configurabile sono interoperabili: prova entrambe le direzioni e due ricevitori sulla stessa porta/gruppo.

[Indice esempi](../README.md) · [Esercizi](../../esercizi/README.md)
