# Esercizi — Server TCP multithread

Segui le guide su [thread Java](../01-Multithreading-Java.md), [server multithread](../02-Server-Multithreaded.md), [sincronizzazione](../03-Sincronizzazione-Risorse-Condivise.md) e [gestione avanzata](../04-Multithreading-Avanzato.md).

| Ordine | Esercizio | Obiettivo | Porta e protocollo |
| --- | --- | --- | --- |
| a | [Multithreading essenziale](a-Mutithread-base/README.md) | Accept loop, Runnable, un gestore per socket | 5000, righe UTF-8; variante con `exit` |
| b | [Dispenser](b-Dispenser/README.md) | Aggiornamenti atomici e sessione negozio persistente | 7777, righe UTF-8; PRENOTA, STATO, NEGOZIO, SERVI, QUIT |
| c | [Prenotazioni](c-Prenotazioni/README.md) | Autenticazione e assegnazione esclusiva dei posti | 8888, righe UTF-8; VISUALIZZA e LISTA ricevono risposte terminate da END |
| d | [Chat server](d-ChatServer-Multithreaded/README.md) | Registro condiviso, broadcast e disconnessione | 9999, righe UTF-8, `/list` e `/quit` |

Le tracce distinguono scheletri con TODO, frammenti da integrare e classi complete. Conserva le estensioni separatamente: i frammenti bonus non sostituiscono automaticamente le classi complete. Il [Dispenser web PHP](b-Dispenser/README_avanzato.md) è facoltativo e riusa il server Java del punto b.

Compila con JDK 11+ e `javac -encoding UTF-8`. Prova due client contemporanei e una disconnessione improvvisa. Per b controlla numeri univoci e rifiuto di SERVI sulla coda vuota; per c prova due prenotazioni simultanee dello stesso posto e VISUALIZZA → PRENOTA → LISTA senza riconnetterti. Salva sorgenti, comandi e risultati dei test nella tua cartella `~/socket`.

I campi `<URL_DEL_TUO_REPOSITORY>` e `<URL_CARTELLA_CONSEGNA>` sono segnaposto. Scadenze e modalità di consegna sono quelle comunicate dal docente. Le altre cartelle eventualmente disponibili localmente contengono approfondimenti e bozze: chat con nomi, interoperabilità Java/PHP, sensori e chat a righe. Non sono passaggi obbligatori del percorso a–d.

[Indice del modulo](../README.md) · [Indice del corso](../../README.md)
