# Il protocollo UDP

UDP (User Datagram Protocol) è un protocollo di trasporto che invia datagrammi fra un indirizzo IP e una porta sorgente e un indirizzo IP e una porta destinazione. È definito nella [RFC 768](https://www.rfc-editor.org/rfc/rfc768.html). Questa guida descrive il protocollo; la [guida Java](01-Socket-UDP-Fondamenti.md) mostra come usarlo.

## Cosa offre e cosa non offre

- **Messaggi distinti:** una ricezione restituisce un datagramma, non una porzione arbitraria dello stream come in TCP. Se il buffer è insufficiente, il datagramma può essere troncato e il resto non viene recuperato da una seconda receive.
- **Assenza di handshake:** inviare non richiede stabilire una connessione TCP. Un invio riuscito conferma che lo stack locale ha accettato il datagramma, non che il destinatario lo abbia ricevuto.
- **Consegna best effort:** pacchetti persi, duplicati o fuori ordine sono possibili. UDP non ritrasmette, non riordina e non applica controllo di congestione.
- **Nessun EOF remoto:** un datagramma di zero byte è un messaggio valido, non una disconnessione. La chiusura di un socket UDP remoto non genera un evento equivalente alla fine di uno stream TCP.

La scelta di UDP non garantisce una latenza o una velocità superiori in ogni situazione: dipende da rete, implementazione e protocollo applicativo. Una ritrasmissione può essere inutile per la posizione ormai superata di un oggetto in un gioco, ma indispensabile per una richiesta che modifica una prenotazione.

## Header e dimensioni

L'header UDP occupa 8 byte:

| Campo | Dimensione | Significato |
| --- | --- | --- |
| Porta sorgente | 16 bit | Porta a cui indirizzare una risposta, se prevista |
| Porta destinazione | 16 bit | Servizio a cui consegnare il datagramma |
| Lunghezza | 16 bit | Header UDP più payload |
| Checksum | 16 bit | Controllo di errore su pseudoheader, header UDP e dati |

Con IPv4 senza opzioni, il limite teorico del payload UDP è 65507 byte (65535 meno 20 byte IP e 8 UDP). Non è una dimensione consigliata per un messaggio di laboratorio. Con IPv6 la gestione del limite è diversa; questi esempi di broadcast e multicast sono esplicitamente IPv4.

Una MTU Ethernet di 1500 byte consente spesso 1472 byte di payload UDP su IPv4 senza frammentazione, ma tunnel e altre reti possono ridurre questo valore. 1400 byte è una scelta prudente per questi esempi, non una garanzia universale. Evitare la frammentazione e misurare la rete effettiva, seguendo la [RFC 8085](https://www.rfc-editor.org/rfc/rfc8085.html).

In IPv4 il checksum UDP può essere omesso, pur essendo normalmente utilizzato. In IPv6 è normalmente obbligatorio; le eccezioni per specifici tunnel sono fuori dal laboratorio. Il checksum non corregge i dati, non fornisce autenticazione e non garantisce la consegna.

## UDP e TCP a confronto

| Aspetto | UDP | TCP |
| --- | --- | --- |
| Unità vista dall'applicazione | Datagramma | Stream di byte |
| Ordinamento e ritrasmissione | Da implementare se necessari | Gestiti dal trasporto |
| Fine comunicazione | Protocollo applicativo o timeout | Chiusura dello stream |
| Controllo di flusso/congestione | Da progettare a livello applicativo | Presente nel protocollo |
| Uno a molti | Broadcast/multicast IP dove disponibili | Connessioni separate |
| Perdita | Può essere tollerata o gestita dall'applicazione | Lo stream resta affidabile finché la connessione funziona |

DNS può usare UDP e TCP; una risposta DNS troncata può richiedere il passaggio a TCP. HTTP/3 usa QUIC sopra UDP, ma è QUIC a fornire affidabilità, controllo di congestione e altre funzioni. Non confondere le proprietà dell'applicazione con quelle di UDP.

## Richieste, risposte e identificativi

Un protocollo request/response deve definire:

1. Destinazione e codifica, per esempio IPv4, porta 9999 e UTF-8.
2. Dimensione massima accettata e comportamento per messaggi troppo grandi.
3. Timeout e significato dell'assenza di risposta: **esito non noto**, non prova che il server sia inattivo.
4. Un identificativo per associare risposte, duplicati e ritrasmissioni alla richiesta corretta.
5. Idempotenza: ripetere una richiesta non deve produrre effetti aggiuntivi indesiderati.

L'echo esatto del laboratorio ammette una sola richiesta in attesa; dopo un timeout la sessione va chiusa, per non interpretare una risposta tardiva come risposta alla richiesta seguente. La versione avanzata del benchmark include un identificativo worker/sequenza e misura solo risposte corrispondenti.

Un socket UDP con `connect()` seleziona il peer a cui inviare e da cui ricevere. Non esegue un handshake né garantisce che il peer esista. Un socket non connesso può ricevere da molti mittenti e usa indirizzo/porta di ciascun DatagramPacket per rispondere.

## Osservare il traffico

Esegui prima un ricevitore, poi un mittente. In Wireshark filtra, per esempio, `udp.port == 9876`. Confronta indirizzi, porte, lunghezza e payload. Il filtro di visualizzazione `udp.port == ...` non è un comando del terminale.

| Scopo | Linux | PowerShell su Windows |
| --- | --- | --- |
| Endpoint UDP locali | `ss -lun` | `Get-NetUDPEndpoint` |
| Indirizzi e prefissi | `ip -4 addr` | `Get-NetIPAddress -AddressFamily IPv4` |
| Interfacce | `ip link` | `Get-NetAdapter` |

Gli endpoint UDP non hanno stati LISTEN/ESTABLISHED equivalenti a TCP. `ss -lun` è utile per i socket UDP associati a porte; non prova che il programma stia ricevendo o elaborando correttamente. Le catture di pacchetti possono richiedere i permessi predisposti dal laboratorio.

## Domande di verifica

1. Una send riuscita prova la consegna? **No.**
2. Due send UDP possono diventare un solo messaggio nella receive? **No:** i confini dei datagrammi restano distinti; un datagramma può però andare perso o essere troncato.
3. Un payload vuoto indica EOF? **No.**
4. A cosa serve l'identificativo di richiesta? **A correlare risposte, duplicati e ritrasmissioni.**
5. Un timeout giustifica ripetere qualunque operazione? **No:** occorre considerare effetti e idempotenza.

[Indice modulo](README.md) · [Dopo: socket Java](01-Socket-UDP-Fondamenti.md) · [Uno a molti nel modulo 06](../06-Socket-Multicast-Broadcast/README.md)
