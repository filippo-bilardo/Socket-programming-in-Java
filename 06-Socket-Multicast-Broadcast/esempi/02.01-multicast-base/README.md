# Multicast essenziale — Java e Node.js

Tutte le versioni usano per default 239.255.0.1:9876, TTL 1, payload UTF-8 di massimo 1024 byte. Il mittente non fa join; il ricevitore sì. La selezione di MULTICAST_IFACE è spiegata nella [guida](../../09-Broadcast-Multicast.md).

```sh
javac -encoding UTF-8 Sender.java Receiver.java MulticastSupport.java _Sender.java _Receiver.java
java Receiver 9876 239.255.0.1
```

In un altro terminale:

```sh
java Sender "Caffè e multicast" 9876 239.255.0.1
```

Gli alias _Sender e _Receiver rinviano alle stesse implementazioni, senza una seconda configurazione divergente. I programmi Node.js sono equivalenti:

```sh
node receiver.js 9876 239.255.0.1
```

```sh
node sender.js "Caffè e multicast" 9876 239.255.0.1
```

Il ricevitore stampa tutti i datagrammi e termina su `bye`; un terzo argomento positivo limita il numero di messaggi e attiva un timeout di 5 s. `bye` è qui un comando di gruppo dimostrativo: nella chat dell'esercizio b l'uscita è invece locale.

Avvia due ricevitori, poi invia “a” e un messaggio più lungo con accenti. Entrambi devono mostrare il testo completo se i datagrammi sono consegnati. Java ripristina la capacità del pacchetto prima di ogni receive. Node.js imposta TTL e interfaccia solo dopo il bind del socket, evitando EBADF.

Le versioni Node.js richiedono anche `../udp-cli.js`, incluso nel repository. Non copiare il solo file in una directory isolata senza la sua utility.

[Indice esempi](../README.md) · [Esercizio multicast](../../esercizi/a-UDPMulticast/README.md)
