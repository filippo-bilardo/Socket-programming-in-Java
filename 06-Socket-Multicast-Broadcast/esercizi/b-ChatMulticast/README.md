# b — Chat multicast con uscita locale

## Traccia

Completa [MulticastChat.java](MulticastChat.java), che segnala esplicitamente il TODO quando viene avviato: lo scheletro non implementa ancora il protocollo e non deve lanciare thread con cicli vuoti. [MulticastSupport.java](MulticastSupport.java) è l'utility già pronta per scegliere l'interfaccia e fare join.

Usa gruppo 239.255.0.3, porta 5001 e TTL 1. Esegui almeno due istanze, con username diversi, prima sullo stesso host e poi fra host della LAN.

## Requisiti

1. Crea il socket multicast con riuso dell'indirizzo prima del bind e aderisci al gruppo sull'interfaccia scelta.
2. Un thread legge dalla console; un thread separato riceve i datagrammi.
3. Definisci un formato che contenga identità dell'istanza, nome visualizzato, numero di sequenza e testo UTF-8, per esempio JSON con massimo 1024 byte.
4. Ripristina la capacità del DatagramPacket a ogni ricezione e scarta messaggi malformati o oltre limite.
5. Il loopback è abilitato: decidi se mostrare anche i tuoi messaggi oppure filtrarli usando l'identità dell'istanza, non il solo username.
6. `/quit` è **un comando locale**, non un messaggio che arresta tutti gli altri partecipanti. Anche EOF sulla console termina soltanto l'istanza locale.
7. Alla chiusura imposta uno stato di arresto visibile al ricevitore, lascia il gruppo e chiudi il socket per sbloccare receive; poi attendi il termine del thread ricevitore. Riconosci SocketException come normale solo quando l'arresto è stato richiesto.
8. Non attendere per sempre il thread di ricezione prima di aver chiuso il socket. Non usare busy loop.

Letture bloccanti da System.in non vengono necessariamente sbloccate dall'interruzione di un altro thread. Disegna quindi il ciclo di vita a partire dall'uscita locale della console, senza promettere un arresto remoto istantaneo non implementato.

## Compilazione e prove

```sh
javac -encoding UTF-8 MulticastChat.java MulticastSupport.java
java MulticastChat
```

Il comando di avvio funziona come chat solo dopo il completamento del TODO. Prendi spunto dall'[esercizio a](../a-UDPMulticast/README.md) e dalle regole della [guida multicast](../../09-Broadcast-Multicast.md), non da vecchie cartelle ES04.

Prova due istanze, nomi uguali con identità diverse, messaggi accentati, corto→lungo, una sequenza di invii, uscita di un utente mentre l'altro continua, EOF console e dati malformati. Perdita e riordino sono possibili: registra le sequenze osservate invece di dichiarare la chat affidabile.

Consegna sorgenti, specifica del formato, ciclo di vita dei thread, comandi e risultati. Valutazione: protocollo e identità 30%, ricezione concorrente 25%, uscita/risorse 25%, prove e documentazione 20%.

[Indice esercizi](../README.md) · [Dopo: discovery](../c-DiscoveryBroadcast/README.md)
