# a — Inviare e ricevere un datagramma

## Obiettivo

Realizza un mittente che invia un messaggio UTF-8 e un ricevitore che stampa il testo completo e termina. Usa porta 8698 e host configurabile. Il messaggio può occupare al massimo 1024 byte.

## Procedura

1. Individua cosa deve conoscere il mittente e quale porta deve occupare il ricevitore.
2. Converti il testo in byte; usa la lunghezza dell'array, non String.length().
3. Nel ricevitore decodifica soltanto i byte del datagramma usando offset e lunghezza.
4. Chiudi i socket anche in caso di errore. Il mittente non attende una risposta.

Confronta la tua soluzione con [Sender.java](Sender.java) e [Receiver.java](Receiver.java). Non dichiarare un package network se i file e i comandi non prevedono quella struttura.

```sh
javac -encoding UTF-8 Sender.java Receiver.java
java Receiver 8698
```

In un altro terminale:

```sh
java Sender localhost 8698 "Caffè, rete e UDP"
```

Puoi sostituire localhost con il server/PC su cui hai avviato Receiver, se il laboratorio consente quel traffico UDP. Il ricevitore è monouso: riavvialo per ogni prova.

## Verifiche e consegna

Prova ASCII, accenti, stringa vuota e messaggio oltre limite. Registra byte inviati e testo ricevuto. Avvia il mittente prima del ricevitore e spiega perché un invio riuscito non garantisce che il ricevitore vedrà quel messaggio in seguito. Consegna sorgenti, comandi e risultati effettivi.

Estensione: fai ricevere più datagrammi; ripristina la capacità del pacchetto prima di ogni receive.

[Indice esercizi](../README.md) · [Dopo: echo](../b-UDPEchoServer/README.md)
