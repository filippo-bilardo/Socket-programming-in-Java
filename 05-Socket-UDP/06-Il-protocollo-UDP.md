# Il protocollo UDP

## In breve

UDP è un servizio di trasporto minimale: aggiunge a ogni messaggio le porte e alcuni controlli, lasciando al protocollo applicativo gran parte delle decisioni. IP si occupa di instradare i pacchetti verso una rete e un host; UDP permette al sistema operativo di consegnare il payload al socket associato alla porta corretta. Non esiste una fase di apertura della connessione e ogni datagramma è indipendente dagli altri.

Un modello utile è quindi: **applicazione → UDP → IP → rete**. In ricezione il percorso è inverso. Ogni livello può rifiutare, perdere o non consegnare i dati; la conferma di `send` riguarda soltanto l'accettazione locale, non il completamento dell'intero percorso.

UDP (User Datagram Protocol) è un protocollo di trasporto che invia datagrammi fra un indirizzo IP e una porta sorgente e un indirizzo IP e una porta destinazione. È definito nella [RFC 768](https://www.rfc-editor.org/rfc/rfc768.html). Questa guida descrive il protocollo; la [guida Java](07-Socket-UDP-Fondamenti.md) mostra come usarlo.

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

Le porte sono numeri a 16 bit e identificano endpoint di trasporto, non processi in modo permanente: il sistema operativo associa i socket alle porte in uso. La porta sorgente può essere zero se non è necessaria una risposta, anche se nei normali scambi request/response il mittente usa una porta locale. La porta destinazione individua il servizio sul sistema ricevente. L'indirizzo IP non è contenuto nell'header UDP: fa parte dell'header IP esterno.

Il campo Lunghezza include l'header di 8 byte. Perciò il payload massimo teorico in IPv4 si ottiene sottraendo anche l'header IP: 65535 − 20 − 8 = 65507 byte, assumendo IPv4 senza opzioni e il limite massimo del pacchetto IP. Non tutte le reti o API consentono di trasportare datagrammi così grandi, e il limite pratico è spesso molto inferiore.

Il checksum rileva molte corruzioni accidentali, ma non le corregge. Il calcolo include uno pseudoheader con informazioni IP (come indirizzi e protocollo), oltre all'header UDP e al payload: questo aiuta a rilevare alcuni errori di consegna. Per IPv4, un valore di checksum pari a zero indica che il mittente non lo ha calcolato; un checksum calcolato il cui risultato è zero viene rappresentato con tutti i bit a uno. In IPv6 il checksum è normalmente obbligatorio. Nessuna di queste verifiche equivale a una firma crittografica o a un controllo di autenticità.

Con IPv4 senza opzioni, il limite teorico del payload UDP è 65507 byte (65535 meno 20 byte IP e 8 UDP). Non è una dimensione consigliata per un messaggio di laboratorio. Con IPv6 la gestione del limite è diversa; questi esempi di broadcast e multicast sono esplicitamente IPv4.

Una MTU Ethernet di 1500 byte consente spesso 1472 byte di payload UDP su IPv4 senza frammentazione, ma tunnel e altre reti possono ridurre questo valore. 1400 byte è una scelta prudente per questi esempi, non una garanzia universale. Evitare la frammentazione e misurare la rete effettiva, seguendo la [RFC 8085](https://www.rfc-editor.org/rfc/rfc8085.html).

Se un datagramma IP è più grande della MTU del collegamento, IPv4 può frammentarlo quando consentito; i frammenti vengono ricomposti prima della consegna a UDP. La perdita di un solo frammento rende inutilizzabile l'intero datagramma. IPv6 non usa router intermedi per frammentare: la frammentazione, se necessaria, è gestita dal mittente. Per questo i messaggi applicativi grandi sono fragili e possono essere meno efficienti di più messaggi piccoli. Non basta aumentare il buffer di ricezione per evitare la frammentazione sulla rete.

La lunghezza del buffer applicativo è un limite distinto dalla dimensione massima teorica IP. Se il buffer fornito alla ricezione è troppo piccolo, la parte eccedente del singolo datagramma non diventa una seconda lettura: è persa. Conviene definire un massimo applicativo esplicito, rifiutare input fuori limite e verificare come l'API utilizzata segnala eventuali troncamenti.

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

La scelta non è semplicemente «veloce contro lento». UDP evita alcune funzioni di TCP, ma l'applicazione deve valutare l'effetto di perdita, riordino, duplicazione e congestione. Se aggiunge ritrasmissioni senza limiti o invia a velocità eccessiva, può peggiorare la congestione e il carico della rete. Per un protocollo applicativo affidabile servono regole di timeout e ritrasmissione, gestione dei duplicati, controllo del ritmo e limiti alle dimensioni; in molti casi è preferibile usare un trasporto o una libreria che fornisca già queste funzioni.

## Richieste, risposte e identificativi

Un protocollo request/response deve definire:

1. Destinazione e codifica, per esempio IPv4, porta 9999 e UTF-8.
2. Dimensione massima accettata e comportamento per messaggi troppo grandi.
3. Timeout e significato dell'assenza di risposta: **esito non noto**, non prova che il server sia inattivo.
4. Un identificativo per associare risposte, duplicati e ritrasmissioni alla richiesta corretta.
5. Idempotenza: ripetere una richiesta non deve produrre effetti aggiuntivi indesiderati.

L'echo esatto del laboratorio ammette una sola richiesta in attesa; dopo un timeout la sessione va chiusa, per non interpretare una risposta tardiva come risposta alla richiesta seguente. La versione avanzata del benchmark include un identificativo worker/sequenza e misura solo risposte corrispondenti.

Un identificativo dovrebbe essere sufficientemente ampio da non riutilizzarsi mentre vecchie risposte possono ancora arrivare. In caso di ritrasmissione, il server può ricevere la stessa operazione più volte: per operazioni con effetti, una chiave di idempotenza o una cache delle richieste già elaborate può evitare esecuzioni duplicate. Il client deve comunque distinguere tra «non ho ricevuto risposta» e «l'operazione non è avvenuta»: dopo un timeout l'esito può essere incerto.

Progettare anche un comportamento per errori e sovraccarico: formato non valido, versione non supportata, richiesta troppo grande e risorse esaurite. Una risposta di errore non deve rivelare dati non necessari. UDP non autentica il mittente; per operazioni sensibili occorre un meccanismo di autenticazione e integrità adeguato, e non si deve considerare l'indirizzo IP sorgente come prova dell'identità dell'utente.

Un socket UDP con `connect()` seleziona il peer a cui inviare e da cui ricevere. Non esegue un handshake né garantisce che il peer esista. Un socket non connesso può ricevere da molti mittenti e usa indirizzo/porta di ciascun DatagramPacket per rispondere.

Il termine «socket connesso» descrive qui una configurazione locale: il sistema operativo filtra o associa le operazioni al peer selezionato e può riportare alcuni errori relativi a quel peer. Non implica uno stato condiviso o una notifica affidabile di chiusura remota. Più processi o socket che tentano di usare la stessa porta possono inoltre essere soggetti alle regole specifiche del sistema operativo e delle opzioni del socket.

## Porte, firewall ed errori

Una porta UDP può essere associata a un socket in ascolto locale, ma non c'è una negoziazione con il mittente prima dell'invio. Firewall e router possono filtrare datagrammi in base a indirizzi, porte o stato osservato; una regola che consente traffico in uscita non implica necessariamente che le risposte o il traffico in ingresso siano consentiti. Anche NAT può modificare indirizzi o porte e mantenere una mappatura solo per un certo periodo.

Alcuni errori possono essere comunicati tramite ICMP e, a seconda del sistema operativo e dell'API, comparire come errori di socket. Non è garantito che un mittente riceva un errore per ogni datagramma non consegnato. L'assenza di errore non è una conferma di consegna, così come l'assenza di risposta non dimostra che il server sia spento.

Non inviare datagrammi a raffica senza limiti. Un server dovrebbe limitare la dimensione e il costo di elaborazione dei messaggi, validare i dati ricevuti e gestire richieste ripetute. Queste precauzioni proteggono sia il programma sia la rete da errori, sovraccarico e traffico indesiderato.

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

[Indice modulo](README.md) · [Dopo: socket Java](07-Socket-UDP-Fondamenti.md) · [Uno a molti nel modulo 06](../06-Socket-Multicast-Broadcast/README.md)
