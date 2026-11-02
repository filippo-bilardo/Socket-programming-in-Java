# Modulo 03: Socket TCP — fondamenti

## Obiettivi

Creare client e server TCP in Java, definire come delimitare i messaggi,
scambiare dati e chiudere correttamente connessioni e stream.
Serve un JDK 11 o successivo; per l'ambiente Windows e Web4Student vedi il
[modulo 02](../02-Ambiente-Sviluppo/README.md).

## Guide

1. [Il protocollo TCP](00-Il-protocollo-TCP.md)
2. [Creazione dei socket](01-Creazione-Socket-TCP.md)
3. [Comunicazione tramite stream](02-Comunicazione-Dati-TCP.md)
4. [Chiusura delle connessioni](03-Chiusura-Connessioni.md)

I frammenti delle guide illustrano singole tecniche; quando non hanno un
`main` vanno inseriti nel programma che li utilizza. Gli esempi completi
sono indicati esplicitamente qui sotto.

## Esempi ed esercizi

| Attività | Materiale disponibile | Protocollo e porta predefinita |
|---|---|---|
| [a — EchoService](esercizi/a-EchoService/README.md) | Client e server completi, compilabili separatamente dagli altri esempi | `writeUTF` / `readUTF`, porta 8765 |
| [b — HelloServer HTTP](esercizi/b-HelloServer/README.md) | Server completo da provare con browser o curl | HTTP GET/HEAD, porta 2223 |
| [c — Vocali e consonanti](esercizi/c-ServerConsonantiVocali/README.md) | Traccia da implementare: creare client e server | Righe UTF-8, porta 12345 |
| [d — Calcolatrice remota](esercizi/d-CalcolatriceRemota/README.md) | Traccia e scheletri da implementare | Righe UTF-8, porta 8844 |

Lavora nelle cartelle corrispondenti in `~/socket` della tua home. Sullo stesso
computer le porte devono essere libere; se ne cambi una, aggiornala anche nel
client. Le classi chiamate `TcpServer` appartengono a esercizi diversi:
compila dalla cartella del singolo esercizio, non tutti i file insieme.

Il framing binario dell'esempio a non è intercambiabile con le righe di testo
degli esercizi c/d. Il server HTTP richiede invece un messaggio HTTP completo.

## Approfondimenti nel modulo multithreading

- [Server con più client](../04-Miltithread/04-Multithreading.md).
- [Trasferimento file](../04-Miltithread/esempi/FileTransfer.java).
- [Pool di thread del server](../04-Miltithread/esempi/ConnectionPoolServer.java).
- [Chat con protocollo personalizzato](../04-Miltithread/esempi/CustomProtocolChat.java).

Queste attività seguono i fondamenti TCP e introducono concorrenza e protocolli
più articolati. La comunicazione UDP è trattata nel [modulo UDP](../05-Socket-UDP/README.md).

## Navigazione

[Modulo precedente: ambiente di sviluppo](../02-Ambiente-Sviluppo/README.md) ·
[Indice del corso](../README.md) ·
[Passo successivo: multithreading](../04-Miltithread/04-Multithreading.md)
