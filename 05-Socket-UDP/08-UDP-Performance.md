# UDP: misurare prestazioni e perdita

Ottimizzare richiede una misura ripetibile. Il throughput dipende da hardware, kernel, JVM, rete, dimensioni dei messaggi e costo del servizio. Non esiste una soglia di pacchetti al secondo garantita da UDP o da questo corso.

## Grandezze da distinguere

| Misura | Significato |
| --- | --- |
| Richieste inviate | Datagrammi accettati dallo stack locale del generatore |
| Risposte valide | Risposte riconosciute per identificativo e contenuto |
| Richieste senza risposta valida | Timeout, errori ICMP o risposte non corrispondenti; non identificano da soli il punto di perdita |
| RTT | Tempo fra invio di una richiesta e ricezione della sua risposta valida |
| Tempo di elaborazione | Intervallo nel solo server, diverso dall'RTT |
| Throughput | Byte o messaggi nell'unità di tempo, indicando sempre cosa viene contato |

“Non risposto entro il timeout” non equivale a “perso definitivamente”. Un datagramma UDP non contiene un contatore di sequenza del trasporto: per misurare duplicati e riordino bisogna aggiungerlo al protocollo applicativo.

## L'esempio con DatagramChannel

[HighPerformanceUDPServer.java](esempi/HighPerformanceUDPServer.java) usa un [DatagramChannel](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/channels/DatagramChannel.html) non bloccante e un Selector. Riusa un buffer diretto da 1401 byte e limita a 50 ricezioni ciascun giro; scarta messaggi applicativi oltre 1400 byte.

Un buffer diretto può non esporre un array: `ByteBuffer.allocateDirect(...).array()` può lanciare UnsupportedOperationException. Il canale legge direttamente nel ByteBuffer. Un buffer su heap, invece, può essere usato come array con DatagramSocket. Sono approcci diversi: non avvolgere l'array in un nuovo ByteBuffer e fingere di restituire al pool lo stesso oggetto diretto acquisito.

Il buffer di ricezione viene riusato soltanto dal thread dell'event loop. La risposta viene costruita ed inviata prima della ricezione seguente: non c'è una coda illimitata che trattiene riferimenti a dati modificabili. La versione di esempio alloca ancora stringhe e risposte: non è “zero allocation”.

In modalità non bloccante send può restituire zero: la risposta viene contata come scartata. receive restituisce null se non ci sono datagrammi, mentre un datagramma vuoto restituisce un mittente valido e un buffer senza payload. La chiusura del canale sblocca l'arresto; non occorre fare polling ogni millisecondo con un socket bloccante.

## Comandi del servizio

| Richiesta UTF-8 | Risposta |
| --- | --- |
| PING | PONG |
| ECHO testo | ECHO: testo |
| TIME | TIME seguito da epoch millisecondi |
| STATS | Contatori rx, tx e scartati |
| LOAD n | Risultato di un calcolo, con n fra 0 e 100000 |
| QUIT | BYE; il server resta disponibile agli altri client |

Le statistiche sono osservate dal thread del server e non costituiscono una promessa di prestazioni. `scartati` conta i rifiuti applicativi e le risposte non inviate; non include pacchetti mai consegnati dall'OS all'applicazione.

## Benchmark ripetibile

Dalla cartella esempi:

```sh
javac -encoding UTF-8 HighPerformanceUDPServer.java
java HighPerformanceUDPServer server 8888
```

In un altro terminale:

```sh
java HighPerformanceUDPServer loadtest localhost 8888 2 20 3
```

Parametri: host, porta, worker, **rate totale desiderato** in richieste al secondo, durata in secondi. Il rate deve essere almeno il numero dei worker. La divisione distribuisce anche il resto: 21 richieste al secondo su 2 worker significa rate 11 e 10, non 10 e 10.

Ogni worker attende una risposta prima di spedire la richiesta successiva. Il generatore è quindi un test request/response a rate massimo desiderato, non un generatore open-loop che garantisce quel rate sotto perdita o RTT elevato. I messaggi includono worker e sequenza; risposte tardive non corrispondenti non sono contate come successi. Le pause non recuperano i ritardi con raffiche.

Il processo termina anche quando il server non risponde; non attende una barriera che potrebbe restare incompleta dopo un errore di inizializzazione. Le risorse sono chiuse al termine di ciascun worker.

## Metodo di confronto

1. Registra OS, JDK, tipo di rete, host, dimensione e durata del test.
2. Esegui un riscaldamento e almeno tre misure della stessa configurazione.
3. Cambia una variabile alla volta: rate, numero di worker, payload o carico del server.
4. Riporta mediana, variazione e richieste senza risposta valida; non scegliere soltanto il risultato migliore.
5. Ripeti fra due host per separare il caso loopback dal comportamento della LAN.

I buffer kernel impostati con SO_RCVBUF/SO_SNDBUF sono richieste e possono essere limitati dal sistema. Un buffer di ricezione più grande può ridurre alcuni drop ma aumenta anche l'accodamento. Leggi i valori effettivi e osserva il carico CPU prima di cambiare la configurazione. Le modifiche sysctl, firewall o routing richiedono l'amministratore del laboratorio; non sono passaggi richiesti agli studenti senza privilegi.

## Domande di verifica

- Il benchmark prova una latenza di rete di una sola direzione? **No: misura RTT applicativo.**
- Un pool di buffer è sempre più veloce? **No: va misurato e deve conservare proprietà e ciclo di vita dei buffer.**
- Una receive su un buffer troppo piccolo si completa con i byte rimanenti alla chiamata successiva? **No.**
- Un risultato su localhost rappresenta la LAN? **No.**

[Indice modulo](README.md) · [Prima: socket UDP](07-Socket-UDP-Fondamenti.md) · [Dopo: uno a molti](../06-Socket-Multicast-Broadcast/README.md)
