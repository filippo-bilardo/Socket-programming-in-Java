# a — Un mittente e più ricevitori multicast

Realizza un mittente che invia un datagramma UTF-8 a 239.255.0.1:19876 e due ricevitori che aderiscono al gruppo. Il mittente usa TTL 1 e non deve fare join. Ogni ricevitore deve specificare l'interfaccia locale, ricevere datagrammi di lunghezza diversa e lasciare il gruppo alla chiusura.

## Avvio del riferimento

[Sender.java](Sender.java), [Receiver.java](Receiver.java) e [MulticastSupport.java](MulticastSupport.java) sono una soluzione minima. Implementa prima la tua versione. La variabile MULTICAST_IFACE e i comandi per individuarla sono nella [guida](../../01-Broadcast-Multicast.md).

```sh
javac -encoding UTF-8 Sender.java Receiver.java MulticastSupport.java
java Receiver 19876 239.255.0.1
```

Apri un secondo ricevitore nello stesso modo, poi invia da un terzo terminale:

```sh
java Sender "a" 19876 239.255.0.1
java Sender "Un messaggio più lungo, con caffè" 19876 239.255.0.1
java Sender "bye" 19876 239.255.0.1
```

`bye` termina i ricevitori dell'esempio di gruppo. Un terzo parametro numerico del ricevitore limita il conteggio e attiva un timeout per prove finite. Massimo 1024 byte per datagramma; messaggi oltre il limite vengono scartati.

## Prove e consegna

Verifica che entrambi i ricevitori mostrino i messaggi completi se consegnati, soprattutto corto→lungo. Ripeti con un mittente e un ricevitore su due PC, ognuno con la propria interfaccia locale. Confronta il gruppo del sender e del receiver e fai una prova con porta errata. Consegna codice, comandi, interfacce e tabella degli esiti, spiegando i limiti di UDP.

Non confondere l'IP del gruppo con 127.0.0.1 o con l'indirizzo della scheda. Per un test incrociato con Node.js usa la [famiglia base](../../esempi/02.01-multicast-base/README.md) passando esplicitamente porta 19876.

[Indice esercizi](../README.md) · [Dopo: chat](../b-ChatMulticast/README.md)
