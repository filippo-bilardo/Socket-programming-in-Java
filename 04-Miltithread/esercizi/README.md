# Esercizi — Server TCP multithread

Segui la [guida di base](../04-Multithreading.md) e quella su [pool e sincronizzazione](../05-Multithreading%20avanzato.md), quindi svolgi:

| Esercizio | Obiettivo | Porta e protocollo |
| --- | --- | --- |
| [a — Chat server](a-ChatServer-Multithreaded/README.md) | Thread per client, lista condivisa, broadcast e disconnessione | 9999, righe UTF-8, comandi /list e /quit |
| [b — Dispenser](b-Dispenser/README.md) | Aggiornamenti atomici di contatori condivisi e sessione negozio | 7777, una riga per richiesta e risposta; PRENOTA, STATO, NEGOZIO, SERVI, QUIT |
| [c — Prenotazioni](c-Prenotazioni/README.md) | Autenticazione e prenotazione esclusiva dei posti | 8888, righe UTF-8; MAPPA e LISTA terminano con END |

Le tracce distinguono scheletri con TODO, frammenti da integrare e classi complete. Non sovrascrivere le classi complete con gli esempi parziali delle estensioni. Il [Dispenser web PHP](b-Dispenser/README_avanzato.md) è facoltativo e riusa lo stesso server Java.

Compila con JDK 11+ e `javac -encoding UTF-8`. Prova almeno due client contemporanei e una disconnessione improvvisa. Per b verifica che i numeri siano univoci e che SERVI su coda vuota fallisca; per c prova due prenotazioni simultanee dello stesso posto e la sequenza VISUALIZZA → PRENOTA → LISTA senza riconnetterti.

I campi `<URL_DEL_TUO_REPOSITORY>` e `<URL_CARTELLA_CONSEGNA>` sono segnaposto da sostituire nelle relazioni. Scadenze e modalità di consegna sono quelle comunicate dal docente. Le altre cartelle di esercizi presenti localmente sono escluse dal versionamento tramite .gitignore.

[Indice del modulo](../README.md) · [Indice del corso](../../README.md)
