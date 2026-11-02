# Modulo 05 — Socket UDP

Datagrammi unicast, richieste/risposte, timeout, codifica e limiti dei messaggi. Dopo la comunicazione TCP e i server multithread, studia le differenze di un trasporto senza stream e senza garanzia di consegna.

## Percorso

1. [Il protocollo UDP](06-Il-protocollo-UDP.md): header, confini dei messaggi, perdita, duplicati e confronto con TCP.
2. [Socket UDP in Java](07-Socket-UDP-Fondamenti.md): DatagramSocket/DatagramPacket, echo e DNS.
3. [Esempi e comandi](esempi/README.md): Java e Node.js con protocolli compatibili.
4. [Misurare prestazioni e perdita](08-UDP-Performance.md): buffer, DatagramChannel, RTT e benchmark limitato.
5. [Esercizi a, b e c](esercizi/README.md): invio/ricezione, echo e Daytime.

JDK 11+; Node.js è facoltativo per le prove fra linguaggi. Tutti gli esempi Java usano solo la libreria standard; quelli Node.js solo API native, senza npm install. Compila con `javac -encoding UTF-8` nella cartella del singolo esempio, perché nomi uguali in cartelle diverse indicano programmi diversi.

## Cosa verificare

Una send riuscita non prova la consegna. Il timeout indica esito non noto. Il payload vuoto è valido; un buffer troppo piccolo tronca il datagramma. Gli esempi testuali dichiarano un limite in byte e usano UTF-8. Prova messaggi accentati, payload vuoti, limite massimo, richieste da più client e server non disponibile.

Le misure su localhost non descrivono automaticamente una LAN. I risultati del benchmark devono indicare configurazione, rate desiderato ed effettivo, risposte valide e RTT, senza soglie garantite.

Broadcast, multicast e discovery sono raccolti nel [modulo 06](../06-Socket-Multicast-Broadcast/README.md); non sono richiesti per svolgere gli esercizi di questo modulo.

[Indice corso](../README.md) · [Prima: multithreading TCP](../04-Miltithread/README.md) · [Dopo: multicast e broadcast](../06-Socket-Multicast-Broadcast/README.md)
