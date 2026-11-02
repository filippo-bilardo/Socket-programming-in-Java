# Socket UDP in Java

Prerequisiti: [protocollo UDP](00-Il-protocollo-UDP.md), JDK 11+ e un terminale. Le classi principali sono [DatagramSocket](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/DatagramSocket.html) e [DatagramPacket](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/DatagramPacket.html).

## Un mittente e un ricevitore

Il [mittente Java](esempi/01.01-unicast-java/UdpSender.java) apre una porta locale effimera, codifica una stringa UTF-8 e invia un DatagramPacket al ricevitore. La lunghezza da trasmettere è **il numero di byte**, non `String.length()`: i caratteri accentati e altri caratteri Unicode possono occupare più byte.

Il [ricevitore Java](esempi/01.01-unicast-java/UdpReceiver.java) associa un socket alla porta 9876. `receive()` è bloccante e imposta indirizzo/porta del mittente e lunghezza effettiva nel pacchetto. La conversione in stringa deve considerare offset e lunghezza, non l'intero array.

Dalla cartella dell'esempio:

```sh
javac -encoding UTF-8 UdpSender.java UdpReceiver.java
java UdpReceiver 9876
```

In un altro terminale:

```sh
java UdpSender localhost 9876 "Caffè e UDP"
```

Il ricevitore termina dopo il primo datagramma. Le [versioni Node.js](esempi/01.01-unicast-java/README.md) usano lo stesso protocollo e possono essere incrociate con quelle Java. Nessuna dipendenza npm è necessaria.

## Richiesta e risposta

Nell'[echo completo](esempi/01.02-EchoUdp/README.md), il server risponde all'indirizzo/porta contenuti nella richiesta. Il payload della risposta coincide esattamente con quello della richiesta: non aggiunge prefissi né timestamp.

Il client conserva indirizzo e porta del peer e valida la sorgente di ogni risposta entro una deadline di 2 secondi. Usa un socket non connesso e una destinazione esplicita anche per i datagrammi vuoti. Invia un solo messaggio per volta; un timeout termina la sessione o viene propagato al chiamante. La stringa `quit` è un comando locale dell'interfaccia interattiva; non arresta il server.

```sh
javac -encoding UTF-8 EchoUDP.java
java EchoUDP server 9999
```

In un altro terminale, dalla stessa cartella:

```sh
java EchoUDP client localhost 9999 "prova"
```

Ometti l'ultimo argomento per leggere messaggi da tastiera. In modalità interattiva una riga vuota produce un datagramma vuoto valido. Ctrl+C arresta il server; il metodo `stop()` chiude il socket per sbloccare receive.

## Capacità del pacchetto e troncamento

`receive()` aggiorna la lunghezza del DatagramPacket. Se riusi lo stesso oggetto per una nuova ricezione, ripristina la capacità con `packet.setLength(buffer.length)`; altrimenti un messaggio breve può limitare la ricezione successiva più lunga. Una nuova istanza DatagramPacket a ogni iterazione è un'alternativa.

Negli esempi testuali il massimo applicativo è 1024 byte. Un buffer di 1025 byte consente di riconoscere e scartare anche un datagramma più grande: ricevere 1025 byte basta a sapere che il limite è stato superato, anche se il messaggio originale era ancora più grande. Non ricomporre il resto con una seconda receive: il resto del datagramma troncato è perso.

## Timeout, errori e risorse

- `setSoTimeout(ms)` richiede un valore positivo per limitare l'attesa; zero significa attesa senza limite.
- `SocketTimeoutException` non distingue perdita della richiesta, perdita della risposta o ritardo del servizio.
- `PortUnreachableException` può comparire con un socket UDP connesso se arriva un errore ICMP; non è una notifica di disconnessione affidabile.
- `close()` sul socket locale sblocca una receive in un altro thread con un errore di socket. Il thread deve riconoscere l'arresto previsto.
- Usa try-with-resources anche quando un'eccezione interrompe la comunicazione. Lo stato condiviso di un server va protetto separatamente dai socket.

Non riusare in parallelo lo stesso DatagramPacket o lo stesso array modificabile fra il thread ricevitore e worker: copia i byte effettivi prima di consegnarli ad altri thread, oppure trasferisci la proprietà del buffer e riciclalo soltanto al termine dell'elaborazione.

## DNS: due esempi diversi

[SimpleDNSResolver](esempi/SimpleDNSResolver.java) usa `InetAddress.getByName`: è il resolver del sistema, che può consultare hosts, cache e servizi DNS. Non illustra un pacchetto DNS UDP costruito dall'applicazione.

[SimpleDNSQuery](esempi/SimpleDNSQuery.java) costruisce invece una query binaria DNS A/IN: ID casuale, header, etichette, QTYPE e QCLASS. Collega il socket al server scelto, controlla ID, QR, RCODE, troncamento e limiti del pacchetto, percorre le risposte e cerca un record A anche dopo un CNAME. Legge nomi compressi senza assumere che ogni nome occupi due byte.

```sh
javac -encoding UTF-8 SimpleDNSResolver.java SimpleDNSQuery.java
java SimpleDNSResolver localhost
java SimpleDNSQuery example.com 8.8.8.8 53
```

L'ultimo comando richiede accesso al DNS indicato; in laboratorio il docente può fornire un altro server. Il parser è un esempio limitato: non è un resolver ricorsivo completo, non valida DNSSEC e segnala TC come necessità di fallback TCP, senza implementarlo. L'esempio segue il formato della [RFC 1035](https://www.rfc-editor.org/rfc/rfc1035.html); il limite storico di 512 byte non è un limite universale del DNS moderno.

## Prove da eseguire

1. Invia “è”, una riga breve e una più lunga, verificando tutti i byte e la lunghezza.
2. Confronta Java→Java, Node→Node, Java→Node e Node→Java.
3. Prova payload vuoto, 1024 byte e 1025 byte: il primo è valido, il massimo è accettato e quello oltre limite è scartato.
4. Invia da due client e controlla che le risposte tornino ai rispettivi endpoint.
5. Avvia un client senza servizio, osservando timeout/ICMP senza attese infinite.
6. Arresta il server mentre attende dati e verifica che termini.

[Indice modulo](README.md) · [Prestazioni](02-UDP-Performance.md) · [Esercizi](esercizi/README.md)
