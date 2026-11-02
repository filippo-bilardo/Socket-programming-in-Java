# Esempi — UDP unicast

Apri il terminale nella cartella indicata e compila le sole classi di quell'esempio. I comandi sono su righe separate, così sono utilizzabili anche da PowerShell. Apri server e client in terminali diversi.

| Cartella | Avvio ricevitore/server | Avvio mittente/client | Protocollo |
| --- | --- | --- | --- |
| [01.01-unicast-java](01.01-unicast-java/README.md) | `java UdpReceiver 9876` oppure `node udp-receiver.js 9876` | `java UdpSender localhost 9876 "Caffè"` oppure `node udp-sender.js 127.0.0.1 9876 "Caffè"` | Un datagramma UTF-8; ricevitore monouso; max 1024 byte |
| [01.02-EchoUdp](01.02-EchoUdp/README.md) | `java EchoUDP server 9999` oppure `node echo-udp.js server 9999` | `java EchoUDP client localhost 9999 "prova"` oppure `node echo-udp.js client 127.0.0.1 9999 "prova"` | Echo esatto, timeout 2 s, max 1024 byte |

Dalla cartella corrente puoi compilare gli approfondimenti:

```sh
javac -encoding UTF-8 SimpleDNSResolver.java SimpleDNSQuery.java HighPerformanceUDPServer.java
java SimpleDNSResolver localhost
```

- [SimpleDNSResolver](SimpleDNSResolver.java) usa la risoluzione del sistema, non costruisce query DNS.
- [SimpleDNSQuery](SimpleDNSQuery.java): `java SimpleDNSQuery example.com 8.8.8.8 53`. La destinazione DNS è configurabile; occorre un server raggiungibile. Il codec controlla header e limiti della risposta e cerca un record A, ma non implementa un resolver completo o il fallback TCP.
- [HighPerformanceUDPServer](HighPerformanceUDPServer.java): `java HighPerformanceUDPServer server 8888`, poi `java HighPerformanceUDPServer loadtest localhost 8888 2 20 3`. Sono worker, rate totale desiderato e durata; il test termina anche senza risposte valide. Vedi [metodo e limiti delle misure](../08-UDP-Performance.md).

Le porte 9876, 9999 e 8888 sono esempi di laboratorio. Non avviare due server unicast sulla stessa porta dello stesso host. Per distinguere i risultati, cambia la porta su entrambi i lati.

[Esercizi](../esercizi/README.md) · [Indice modulo](../README.md)
