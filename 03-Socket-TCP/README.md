# Esercitazione 03: Socket TCP - Fondamenti

## 🎯 Obiettivi di Apprendimento
- Padroneggiare la creazione e gestione di socket TCP
- Comprendere il flusso di dati con InputStream/OutputStream
- Gestire correttamente la chiusura delle connessioni
- Implementare pattern robustos per la comunicazione TCP

## 📚 Guide Teoriche
0. [00-Il-protocollo-TCP](00-Il-protocollo-TCP.md)
1. [01-Creazione-Socket-TCP](01-Creazione-Socket-TCP.md)
2. [02-Comunicazione-Dati-TCP](02-Comunicazione-Dati-TCP.md)
3. [03-Chiusura-Connessioni](03-Chiusura-Connessioni.md)
4. [04-Multithreading](04-Multithreading.md)

## 🎯 Esempi Pratici

### 📁 Esempi Base
- **ClientTCPBase.java** - Client TCP con gestione errori e timeout
- **ServerTCPBase.java** - Server TCP robusto con configurazione avanzata
- **EchoUDP.java** - Comunicazione UDP bidirezionale

### 📁 Esempi Avanzati
- **FileTransfer.java** - Trasferimento file con verifica integrità
- **ConnectionPoolServer.java** - Server scalabile con pool di thread
- **CustomProtocolChat.java** - Chat multi-utente con protocollo personalizzato

> **💡 Nota:** Compila ed esegui gli esempi per sperimentare con i concetti

## Esercitazioni Suggerite
1. **Implementa un Client-Server Echo**: Crea un'applicazione che invia messaggi dal client al server e li restituisce.
2. **Trasferimento File**: Sviluppa un'applicazione che consente il trasferimento di file tra client e server.
3. **Chat Multi-Utente**: Costruisci una semplice applicazione di chat che supporta più utenti connessi contemporaneamente.
4. **Asta online**: Realizza un sistema in cui il server sia il banditore di un’asta: accoglie le offerte e comunica se sono accettabili o meno, comunica a richiesta la migliore offerta corrente; i client sono i partecipanti all’asta che possono richiedere quale sia l’offerta migliore (e di chi) e possono effettuare rilanci se il loro budget a disposizione lo consente.

## Navigazione
- [⬅️ Esercitazione Precedente](../02-Ambiente-Sviluppo/README.md)
- [📑 Torna all'Indice del Corso](../README.md)
- [➡️ Esercitazione Successiva](../04-Socket-UDP-Fondamenti/README.md)