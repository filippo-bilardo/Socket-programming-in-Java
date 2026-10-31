# Modulo 04 — Server TCP multithread

Un server con I/O bloccante può dedicare un thread a ogni client e continuare ad accettare connessioni. In questo modulo impari a gestire connessioni simultanee, sincronizzare lo stato condiviso, limitare le risorse e arrestare un servizio chiudendo i socket.

## Prerequisiti e percorso

Servono JDK 11 o successivo, terminale e gli argomenti del [modulo TCP](../03-Socket-TCP/README.md). Node.js è facoltativo, solo per il confronto con il modello a eventi.

1. [Multithreading di base](04-Multithreading.md): accept loop, Runnable, un gestore per socket e timeout.
2. [Esempi e comandi](esempi/README.md): prova le varianti Thread, Runnable e Lambda, poi collega almeno due client insieme.
3. [Pool e sincronizzazione](05-Multithreading%20avanzato.md): contatori, coda limitata, ammissione dei client e shutdown.
4. [Esercizi a, b e c](esercizi/README.md): chat con broadcast, dispenser numeri e prenotazione posti.

## Regole comuni dei programmi

TCP trasporta uno stream: una lettura non equivale necessariamente a un messaggio. Nei protocolli testuali usa righe UTF-8; nelle risposte multiriga concorda un terminatore. Nei protocolli binari leggi ogni campo nello stesso ordine in cui è stato scritto. I protocolli di esempi diversi non sono intercambiabili.

Ogni socket deve avere un proprietario che lo chiude anche su errore. Un solo thread legge da uno stream; più mittenti sullo stesso output devono coordinare l'intero messaggio. Le operazioni atomiche delle collezioni concorrenti non rendono atomica una sequenza di controlli e aggiornamenti.

Un fixed thread pool limita i worker, ma non i socket aperti se la coda è illimitata. L'esempio avanzato limita sia coda sia worker e chiude le connessioni rifiutate. Gli esempi introduttivi con un thread per client sono destinati al laboratorio e non applicano un limite globale.

## Verifiche di laboratorio

- Apri due client contemporaneamente: un client fermo in lettura non deve impedire le risposte all'altro.
- Invia una riga in più scritture e più righe in una scrittura; controlla anche caratteri accentati e messaggi lunghi.
- Prova EOF, disconnessione improvvisa, comando di uscita e timeout.
- Per lo stato condiviso, esegui richieste concorrenti e verifica gli invarianti: nessun numero duplicato, nessun posto assegnato due volte.
- Arresta il server con client attivi: listener, socket attivi e socket in coda devono essere chiusi.

Le compilazioni richiedono `-encoding UTF-8`. Esegui gli esempi separatamente: molti riutilizzano le stesse porte e gli stessi nomi di classe. Con un server remoto sostituisci localhost con il nome del server e verifica quali indirizzi sono configurati nel client.

[Indice del corso](../README.md) · [Prima: TCP](../03-Socket-TCP/README.md) · [Dopo: UDP](../05-Socket-UDP/README.md)
