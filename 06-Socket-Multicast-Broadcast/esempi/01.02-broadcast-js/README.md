# Broadcast UDP — Node.js

Avvia il ricevitore:

```sh
node broadcast-receiver.js 5000 3
```

Poi il mittente in un altro terminale:

```sh
node broadcast-sender.js 5000 500 255.255.255.255 3
```

Il mittente invia tre datagrammi a intervalli di 500 ms. Senza conteggio, lo zero predefinito indica esecuzione continua fino a Ctrl+C. Porta 1..65535, intervallo positivo e conteggio non negativo; un parametro errato non deve trasformarsi silenziosamente nel valore predefinito.

Il destinatario può essere il broadcast effettivo della subnet. Puoi usare 127.0.0.1 per isolare un problema applicativo, ma quel test è unicast. Il payload BROADCAST è UTF-8, massimo 1024 byte; tutte le implementazioni condividono il formato. Incrocia Node.js con l'altra implementazione.

Non chiamare “consegnato a tutti” un datagramma solo perché la send è riuscita. Per una prova fra host controlla la stessa porta e la rete locale effettiva. Mantieni un solo ricevitore broadcast per host/porta: non è il test dei gruppi multicast con più membri locali.

[Indice esempi](../README.md) · [Guida](../../09-Broadcast-Multicast.md)
