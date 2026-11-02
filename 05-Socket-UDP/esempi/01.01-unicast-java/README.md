# Un datagramma, Java e Node.js

Protocollo: un datagramma UTF-8, massimo 1024 byte, porta predefinita 9876. Il ricevitore stampa il messaggio e termina; il mittente non attende risposta. Inviare non prova la consegna.

```sh
javac -encoding UTF-8 UdpSender.java UdpReceiver.java
java UdpReceiver 9876
```

Da un altro terminale nella stessa cartella:

```sh
java UdpSender localhost 9876 "Caffè e UDP"
```

Alternative Node.js, senza installare pacchetti:

```sh
node udp-receiver.js 9876
```

```sh
node udp-sender.js 127.0.0.1 9876 "Caffè e UDP"
```

I parametri del mittente sono host, porta e messaggio; quelli del ricevitore sono la porta. Usa anche Java→Node e Node→Java. Le versioni parlano lo stesso protocollo. Per ripetere una prova riavvia il ricevitore monouso.

Prova un messaggio accentato, uno vuoto e il limite di 1024 byte. Il ricevitore Java usa un buffer con un byte aggiuntivo per riconoscere messaggi oltre il massimo; quello Node verifica la lunghezza del Buffer ricevuto.

[Indice esempi](../README.md) · [Fondamenti](../../07-Socket-UDP-Fondamenti.md)
