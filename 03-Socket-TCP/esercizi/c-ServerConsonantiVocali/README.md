# 🎓 ESERCITAZIONE: Socket TCP Client-Server in Java "Consonanti e vocali"

Realizzare un’applicazione client-server dove il client invia un messaggio al server, questo conta il numero di vocali e di consonanti, e restituisce tali valori: il client continua a inviare frasi fino a che il numero di consonanti sia esattamente la metà del numero di vocali o quando riceve il comando "QUIT". In questo caso il server termina l’applicazione.
Consegnare un documento di Google contenente lo screenshot che ne dimostra il funzionamento, e il link ai file di codice.

Questa è la traccia completa dell'esercizio, da implementare con un JDK 11 o
successivo. Crea `TcpServer.java` e `TcpClient.java`, usando la porta **12345**
e il protocollo a righe UTF-8 definito qui sotto.

## Specifica del protocollo

Conta le vocali ASCII `aeiou` e le altre lettere ASCII come consonanti, senza
distinzione di maiuscole. Ignora spazi, cifre, punteggiatura e caratteri fuori
dall'alfabeto ASCII. `TOTALE` è la somma delle lettere contate.

Ogni richiesta e ogni risposta occupano una riga UTF-8, inviata con flush.
Per una frase valida rispondi con tutti i conteggi e il campo `TERMINA`:

```text
VOCALI:5;CONSONANTI:4;TOTALE:9;TERMINA:NO
VOCALI:2;CONSONANTI:1;TOTALE:3;TERMINA:SI
```

Invia `TERMINA:SI` soltanto se `vocali > 0 && 2 * consonanti == vocali`, poi
chiudi client e server. Con `QUIT`, senza distinzione di maiuscole, invia
`CHIUSURA:Server terminato su richiesta` prima di arrestare il server.
Per una riga senza lettere ASCII rispondi `ERRORE:Nessuna lettera ASCII` e
continua. EOF o una disconnessione improvvisa chiudono soltanto la sessione:
il server può accettare un nuovo client in sequenza.

## Casi di prova

| Richiesta | Vocali | Consonanti | Risultato |
|---|---|---|---|
| `ciao mondo` | 5 | 4 | Continua |
| `programmazione` | 6 | 8 | Continua |
| `java socket` | 4 | 6 | Continua |
| `ciao` | 3 | 1 | Continua |
| `aeiou` | 5 | 0 | Continua |
| `aeioubc` | 5 | 2 | Continua |
| `aob` | 2 | 1 | Arresta con `TERMINA:SI` |
| `aeiobc` | 4 | 2 | Arresta con `TERMINA:SI` |
| `bcdf` | 0 | 4 | Continua |
| Stringa vuota o `12345 !!!` | 0 | 0 | Errore, continua |
| `QUIT` | — | — | Saluto `CHIUSURA`, arresta client e server |

Usa una nuova sessione del server per ciascun caso di arresto. Nella stessa
sessione puoi inviare `ciao`, `aeioubc` e infine `aob`: solo l'ultimo termina.

## 1. Progettare il protocollo

Disegna il dialogo: invio di una riga, lettura di una risposta, nuova richiesta.
Definisci come riconoscere una risposta di conteggio, un errore e `CHIUSURA`.
Nella risposta di conteggio leggi anche il campo `TERMINA`.

Distingui tre eventi: `QUIT` arresta il server dopo il saluto `CHIUSURA`,
`TERMINA:SI` arresta il server dopo i conteggi, EOF interrompe soltanto la
sessione senza un nuovo messaggio da elaborare.

## 2. Implementare e provare il conteggio prima della rete

Crea una funzione che esamini i caratteri della frase. Puoi convertire ciascun
carattere in minuscolo, poi controllare se è compreso tra `a` e `z`; soltanto
in questo caso verifica se appartiene ad `aeiou`. Gli altri caratteri si ignorano.

Verifica `ciao mondo` (5/4), `programmazione` (6/8), `aeioubc` (5/2) e `aob` (2/1).
Per la terminazione usa:

```java
boolean termina = vocali > 0 && 2 * consonanti == vocali;
```

Non usare la divisione intera: con tre vocali e una consonante non è raggiunto
il rapporto richiesto. Definisci l'errore per una frase senza lettere ASCII.

## 3. Completare il server

Crea `TcpServer.java` e implementa:

1. Creazione del `ServerSocket` sulla porta 12345.
2. Ciclo esterno di accettazione delle sessioni.
3. Per ciascun client, reader e writer UTF-8 con chiusura automatica.
4. Ciclo interno che legge una riga, la elabora e invia una sola risposta.
5. Gestione di `QUIT`, EOF e condizione di arresto del programma.

Il ciclo interno termina quando si chiude il client; il ciclo esterno termina
quando una frase soddisfa la condizione matematica oppure arriva `QUIT`. Invia e scarica
con flush la risposta prima di chiudere la connessione.

## 4. Creare il client

Crea `TcpClient.java` con connessione a `127.0.0.1:12345`. Per ogni riga letta
dalla tastiera: controlla EOF, invia il messaggio e leggi la risposta prima di
chiedere un altro input. Termina con `CHIUSURA`, `TERMINA:SI` o EOF dal server.
Gestisci gli errori di apertura e lettura con messaggi comprensibili.

## 5. Compilare e provare

Dopo aver implementato entrambi i programmi:

```bash
javac -encoding UTF-8 TcpServer.java TcpClient.java
```

Apri due terminali nella cartella dell'esercizio. In A esegui `java TcpServer`,
in B `java TcpClient`. Su Web4Student entrambi devono essere sessioni SSH sul
server. Svolgi tutti i [casi di prova](README.md#casi-di-prova), compreso un
secondo client dopo la disconnessione del primo per EOF. Riavvia il server
per provare separatamente `QUIT` e un messaggio con `TERMINA:SI`.

## 6. Documentare e verificare

Nella relazione indica porta, encoding, framing e significato dei campi. Mostra
come hai distinto l'arresto del client da quello del server e come hai corretto
un eventuale errore. Usa screenshot di comandi e risultati realmente ottenuti.

| Criterio | Punti |
|---|---|
| Conteggio e condizione matematica corretti | 25 |
| Server, sessioni e chiusura delle risorse | 25 |
| Client e interpretazione delle risposte | 20 |
| Input limite, errori e disconnessioni | 15 |
| Test e documentazione | 15 |

Segui le [indicazioni di consegna](README.md#consegna). Come estensione
facoltativa puoi supportare le vocali accentate, dichiarando la nuova regola e
aggiungendo test, oppure passare alla gestione concorrente nel modulo successivo.

## Consegna

Registra i sorgenti e la relazione nel repository **Socket-Programming**, senza
includere i file `.class`. Consegna il documento Google con screenshot leggibili,
link ai sorgenti e risultati dei casi di prova, dell'arresto con `QUIT` e della
gestione delle disconnessioni.

[Indice del modulo](../../README.md)
