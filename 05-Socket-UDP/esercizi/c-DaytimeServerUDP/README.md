# c — Daytime UDP

## Obiettivo

Realizza un servizio senza stato che risponde a ogni richiesta UDP con data e ora UTC. Il laboratorio usa la porta 1313, senza richiedere privilegi amministrativi; la porta standard Daytime è 13. L'esercizio prende spunto dalla [RFC 867](https://www.rfc-editor.org/rfc/rfc867.html), adottando esplicitamente il formato ISO-8601 generato da Instant, seguito da CRLF.

La richiesta può avere payload vuoto: non significa EOF. Il server risponde al mittente effettivo e continua a servire altri client. Il client usa un timeout di 2 s e verifica che la risposta sia un Instant valido. Non misura la precisione dell'orologio né sincronizza il PC: l'RTT e l'orologio del server possono influire sul confronto con l'ora locale.

## Implementazione

Progetta prima il protocollo e i casi di errore; confronta poi la soluzione con [DaytimeServer.java](DaytimeServer.java) e [DaytimeClient.java](DaytimeClient.java).

```sh
javac -encoding UTF-8 DaytimeServer.java DaytimeClient.java
java DaytimeServer 1313
```

In un altro terminale:

```sh
java DaytimeClient localhost 1313
```

## Prove e consegna

1. Invia una richiesta vuota e verifica data/ora UTC e CRLF nel datagramma.
2. Avvia più client e verifica che tutti i messaggi di risposta tornino agli endpoint corretti.
3. Ripeti alcune richieste: il server deve restare in ascolto e le risposte devono rispettare lo stesso formato.
4. Usa una porta senza servizio per osservare timeout/ICMP.
5. Arresta il server con Ctrl+C e verifica che la porta sia rilasciata.

Consegna sorgenti, README con comandi, descrizione del formato e risultati delle prove. Spiega perché questo servizio non necessita di un thread per ogni client e perché non equivale a NTP.

Estensione: aggiungi un ID richiesto dal client e restituiscilo nella risposta; aggiorna il formato concordato su entrambi i lati.

[Indice esercizi](../README.md) · [Modulo successivo](../../../06-Socket-Multicast-Broadcast/README.md)
