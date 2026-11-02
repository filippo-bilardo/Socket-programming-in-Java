# Modulo 06 — Socket multicast e broadcast

Comunicazione uno a molti su UDP IPv4: broadcast sulla rete locale, gruppi multicast, selezione dell'interfaccia e discovery applicativo. Prerequisito: [modulo UDP](../05-Socket-UDP/README.md), indirizzi IPv4 e prefissi.

## Percorso

1. [Broadcast e multicast](01-Broadcast-Multicast.md): destinatari, indirizzi, TTL, join/leave e interfacce.
2. [Esempi Java e Node.js](esempi/README.md): un mittente e più ricevitori, con protocolli interoperabili.
3. [Discovery](02-Discovery.md): richiesta broadcast e risposta unicast, annunci multicast e scadenza della cache.
4. [Esercizi](esercizi/README.md): ricezione di gruppo, chat e discovery.

## Preparazione

Servono JDK 11+, una rete/interfaccia IPv4 che supporti multicast e, per il confronto fra linguaggi, Node.js. Non sono necessarie librerie esterne. Esegui i comandi dalla cartella dell'esempio e avvia sempre i ricevitori prima del mittente.

Se il PC ha più adattatori, imposta `MULTICAST_IFACE` all'indirizzo IPv4 dell'adattatore scelto; trovi i comandi Linux e PowerShell nella [guida](01-Broadcast-Multicast.md). Non fissare 127.0.0.1 come interfaccia multicast per una prova fra host. Il gruppo (239.255.0.1), la porta e l'interfaccia locale sono tre parametri distinti.

Il test sullo stesso host usa loopback multicast e non dimostra il funzionamento fra due PC. Il broadcast può essere filtrato da rete o firewall, mentre il multicast può dipendere da Wi-Fi, VPN, IGMP snooping e configurazione degli apparati. Registra l'interfaccia usata e gli esiti effettivi; eventuali modifiche di rete sono compito dell'amministratore del laboratorio.

## Protocolli adottati

| Esempio | Destinazione predefinita | Payload |
| --- | --- | --- |
| Broadcast periodico | 255.255.255.255:5000 | BROADCAST con numero e timestamp |
| Multicast base | 239.255.0.1:9876 | Un messaggio UTF-8, massimo 1024 byte |
| Multicast configurabile | 239.255.0.1:5000 | MULTICAST con numero, timestamp e TTL |
| Discovery broadcast | Broadcast scelto:8891; risposta unicast | DISCOVER/DEVICE con UUID |
| Annunci servizi | 239.255.0.2:8890 | ANNOUNCE con nome, tipo e porta |

Gli annunci non avviano il servizio annunciato e non fanno discovery universale di tutti gli host. UDP non garantisce che tutti i membri ricevano ciascun messaggio.

[Indice corso](../README.md) · [Prima: UDP](../05-Socket-UDP/README.md)
