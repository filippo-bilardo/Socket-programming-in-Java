# Modulo 04 — Multithreading e server TCP concorrenti

In questo modulo impari ad avviare e coordinare thread Java, gestire più connessioni TCP e proteggere dati condivisi. Il percorso parte dai meccanismi Java e arriva a pool limitati, timeout e arresto del server.

## Prerequisiti e percorso

Servono classi, interfacce, eccezioni, terminale e il [modulo TCP](../03-Socket-TCP/README.md). Gli esempi di rete richiedono JDK 11+; la guida generale usa JDK 17+ e distingue l'approfondimento sui thread virtuali, che richiede JDK 21+. Un JDK 21 o successivo consente di seguire tutto il modulo.

| Ordine | Guida | Argomenti |
| --- | --- | --- |
| 01 | [Multithreading in Java](01-Multithreading-Java.md) | Thread, Runnable, lambda, join, interruzioni, monitor, code, executor e thread virtuali |
| 02 | [Server multithread](02-Server-Multithreaded.md) | Accept loop, un gestore per socket, protocollo a righe ed EOF |
| 03 | [Sincronizzazione e risorse condivise](03-Sincronizzazione-Risorse-Condivise.md) | Race condition, synchronized, ReentrantLock, invarianti, snapshot e broadcast |
| 04 | [Gestione avanzata](04-Multithreading-Avanzato.md) | Pool, code limitate, rifiuti, eccezioni, timeout, logging e arresto |

Prova gli [esempi con comandi di compilazione e avvio](esempi/README.md), poi svolgi gli [esercizi](esercizi/README.md): server essenziale, chat, dispenser e prenotazioni. I frammenti illustrativi e gli scheletri con TODO sono distinti dalle classi complete da salvare nel file omonimo.

## Regole comuni dei programmi

TCP trasporta uno stream: una lettura non equivale necessariamente a un messaggio. Nei protocolli testuali usa righe UTF-8; nelle risposte multiriga concorda un terminatore, per esempio `END`. Nei protocolli binari leggi ogni campo nello stesso ordine in cui è stato scritto. Gli esempi hanno protocolli diversi: un client `readUTF` non può dialogare con un server a righe.

Ogni socket deve avere un proprietario che lo chiude anche su errore. Un solo thread legge da uno stream; più mittenti sullo stesso output devono coordinare l'intero messaggio. Le operazioni atomiche delle collezioni concorrenti non rendono atomica una sequenza di controlli e aggiornamenti.

Un fixed thread pool limita i worker, ma non i socket aperti se la coda è illimitata. La guida avanzata limita sia coda sia worker e chiude le connessioni rifiutate. Gli esempi introduttivi con un thread per client non applicano un limite globale. `setSoTimeout` limita le letture; per lo shutdown non basta cambiare un flag o chiamare `interrupt`: occorre gestire anche listener e socket accettati.

## Verifiche di laboratorio

- Apri due client contemporaneamente: un client fermo in lettura non deve impedire le risposte all'altro.
- Invia una riga in più scritture e più righe in una scrittura; prova caratteri accentati e risposte multiriga.
- Prova EOF, disconnessione improvvisa, comando di uscita e timeout.
- Per lo stato condiviso verifica gli invarianti: nessun numero duplicato, nessun posto assegnato due volte.
- Nei server con arresto coordinato, lascia client attivi e task in coda e verifica che tutti i socket vengano chiusi.

Compila con `-encoding UTF-8` ed esegui ogni esempio separatamente: più cartelle riutilizzano nomi di classe e porte. Su un server remoto sostituisci `localhost` con il nome del server e verifica le porte accessibili; non occorrono porte inferiori a 1024. Per osservare le connessioni consulta la [guida CLI](../02-Ambiente-Sviluppo/04-Monitorare-i-Socket-dalla-CLI).

[Indice del corso](../README.md) · [Prima: TCP](../03-Socket-TCP/README.md) · [Dopo: UDP](../05-Socket-UDP/README.md)
