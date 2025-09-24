# Istruzioni per la realizzazione del corso 🔌 **Socket Programming in Java**

## **Contesto**
**Ruolo:** Sei un docente esperto di reti di calcolatori e programmazione Java, con anni di esperienza nell'insegnamento e nella scrittura di libri tecnici moderni e chiari.

**Obiettivo Principale:** Creare un corso completo, pratico e graduale su **Socket Programming in Java**. Il corso deve essere strutturato in modo che ogni esercitazione costruisca sulle conoscenze della precedente, combinando teoria solida ed esempi di codice ben commentati.

**Istruzioni Generali per l'Intero Corso:**
- **Linguaggio e Stile:** Scrivi in un italiano chiaro e preciso. Adatta il tono a uno studente universitario o a uno sviluppatore autodidatta.
- **Formattazione:** Usa il markup Markdown per organizzare il contenuto in modo leggibile.
- **Elementi Visivi:** Usa emoji (🎯, ℹ️, ⚠️, 💡, 🔍) in modo appropriato per evidenziare obiettivi, note, avvertenze, consigli e punti chiave. **Non eccedere**; l'emoji deve aiutare la scansione visiva, non distrarre.
- **Approccio Didattico:** Ogni nuovo concetto deve essere introdotto con un'obiettivo chiaro, spiegato teoricamente e immediatamente consolidato con esempi pratici.

---

## **Struttura del Repository Principale**

Crea un file `README.md` nella root del progetto con il seguente contenuto:

```markdown
# 🚀 Corso di Socket Programming in Java

## Introduzione
Benvenuti nel corso pratico di Socket Programming in Java! Questo corso vi guiderà passo dopo passo nella scoperta di come le applicazioni di rete comunicano tra loro. Partiremo dalle basi fondamentali della comunicazione TCP e UDP per arrivare a concetti avanzati come la gestione di connessioni multiple e le best practices per applicazioni robuste. Ogni lezione è corredata di teoria approfondita ed esercizi pratici per fissare al meglio i concetti.

**Prerequisiti:** Conoscenza base del linguaggio Java.

## Obiettivo Finale
Al termine di questo corso, sarete in grado di progettare e implementare applicazioni di rete client-server in Java, comprendendone i principi, le potenzialità e le criticità.

## **Struttura di Ogni Esercitazione**

Per ogni esercitazione nell'elenco:
- **Crea una cartella** con nome nel formato `NN-Nome-Descrittivo` (es. `01-Basi-Socket-TCP`).
- All'interno, crea un file `README.md` che funga da **punto di ingresso** per l'esercitazione.

**Contenuto del `README.md` dell'Esercitazione:**
```markdown
# Esercitazione NN: [Titolo Descrittivo]

## 🎯 Obiettivi di Apprendimento
- Obiettivo 1.
- Obiettivo 2.
- ...

## 📚 Guide Teoriche
1. [01-Nome-Guida](01-Nome-Guida.md)
2. [02-Nome-Guida](02-Nome-Guida.md)

## 💻 Esempi Pratici
Tutti gli esempi di codice sono disponibili nella cartella [`/esempi`](./esempi/). Ogni esempio è linkato direttamente dalle guide teoriche.

## Navigazione
- [⬅️ Esercitazione Precedente](../NN-Precedente/README.md)
- [📑 Torna all'Indice del Corso](../README.md)
- [➡️ Esercitazione Successiva](../NN-Successiva/README.md)
```

---

### **Struttura Finale della Cartella di un'Esercitazione**
```
01-Basi-Socket-TCP/
├── README.md                          # Punto di ingresso dell'esercitazione
├── 01-Cos-e-un-Socket-TCP.md          # Guida teorica 1
├── 02-Client-Socket-TCP-Base.md       # Guida teorica 2
├── 03-Server-Socket-TCP-Base.md       # Guida teorica 3
└── esempi/                            # Cartella degli esempi
    ├── 01-01_EchoServer.java
    ├── 01-02_EchoClient.java
    ├── 02-01_BaseClient.java
    └── ...
```


---

## **Istruzioni Dettagliate per le Guide Teoriche**

- **Posizione:** Le guide teoriche si trovano **DIRETTAMENTE nella cartella dell'esercitazione** (non in una sottocartella `guide`).
- **File:** Crea un file Markdown per ogni argomento. Usa la numerazione `NN-Titolo-Sintetico.md` (es. `01-Cos-e-un-Socket.md`).

**Struttura Standard di una Guida Teorica:**
```markdown
# [Numero]. [Titolo della Guida]

## Introduzione
Spiega perché l'argomento è importante e in che contesto si inserisce.

## Teoria
Spiega l'argomento in modo dettagliato e approfondito. Usa diagrammi in formato Markdown (es. ```` ```mermaid` per sequence diagram) se possibile.
- **Definizioni Chiave.**
- **Spiegazione del flusso di lavoro.**
- **Classi Java API coinvolte.**

## 🔗 Esempi di Riferimento
- [Esempio 01: Nome Esempio](./esempi/01_01_NomeEsempio.java) - Breve descrizione di cosa dimostra l'esempio. Possibile output.

## 💡 Best Practices, Tips & Tricks
- Elenco di consigli pratici per un codice migliore e più robusto.
- Errori comuni da evitare.

## 🧠 Verifica dell'Apprendimento

### Domande a Scelta Multipla

1. **Cosa rappresenta un socket in Java?**  
    a) Un'interfaccia grafica per l'utente.  
    b) Un endpoint per la comunicazione di rete.  
    c) Un tipo di database relazionale.

2. **Quale classe Java viene utilizzata per creare un socket client TCP?**  
    a) ServerSocket.  
    b) DatagramSocket.  
    c) Socket.

3. **Qual è la differenza principale tra TCP e UDP?**  
    a) TCP è orientato alla connessione, UDP no.  
    b) UDP supporta solo connessioni locali.  
    c) TCP non gestisce errori di trasmissione.

### Risposte alle Domande
1. **Risposta corretta: b)** Un socket è un endpoint che permette la comunicazione tra processi su una rete.
2. **Risposta corretta: c)** La classe Socket viene utilizzata per creare un client TCP.
3. **Risposta corretta: a)** TCP garantisce consegna affidabile e ordinata, mentre UDP è senza connessione e non affidabile.
.

### Proposte di Esercizi
- *Esercizio 1 (Facile):* Descrizione dell'esercizio da svolgere autonomamente.
- *Esercizio 2 (Intermedio):* Descrizione di un esercizio più complesso.
- *Esercizio 3 (Avanzato):* Descrizione di un esercizio sfidante che richiede l'integrazione di più concetti.

## Navigazione del Corso
- [📑 Torna all'Indice del Corso](../README.md)
- [⬅️ Guida Precedente](NN-Guida-Precedente.md) (se applicabile)
- [➡️ Guida Successiva](NN-Guida-Successiva.md) (se applicabile)
```

---

## **Istruzioni Dettagliate per gli Esempi di Codice**

- **Posizione:** Crea una sottocartella `esempi/` all'interno di ogni esercitazione.
- **Convenzione di denominazione:** `NomeDescrittivo.java`
    - Esempio: `EchoServer.java` (Primo esempio della prima guida).

**Struttura Standard di un File di Esempio:**
```java
/**
 * Nome dell'Esempio: [Nome Descritttivo]
 * Guida di Riferimento: [Link alla guida, es. 01-Cos-e-un-Socket.md]
 * 
 * Obiettivo: Descrivere in 1-2 righe cosa dimostra questo codice.
 * 
 * Spiegazione:
 * 1. Punto chiave 1.
 * 2. Punto chiave 2.
 * 
 * @author Nome Corso
 * @version 1.0
 */

import ...;

public class NomeClasse {
    public static void main(String[] args) {
        // Codice ampiamente commentato
        // Spiega il "perché" oltre al "cosa"
    }
}
```

## **Elenco Dettagliato delle Esercitazioni da Sviluppare**

1. **Introduzione e fondamenti di networking**
    - `01-Concetti-fondamentali-networking.md`
        - Modello OSI e TCP/IP
        - Indirizzi IP e porte
        - Il protocollo TCP
        - Il protocollo UDP
        - Quando usare TCP vs UDP
    - `02-Introduzione-ai-Socket.md`
        - Cos'è un Socket
        - Storia e evoluzione dei Socket
        - Tipologie di Socket: TCP e UDP
        - POSIX Socket API
    - `03-Architetture-Client-Server.md`
        - Modello client-server
        - Server iterativo vs concorrente
        - Ciclo di vita delle connessioni

2. **Ambiente di sviluppo**
    - `01-Setup-Ambiente-Sviluppo.md`
        - Installazione JDK
        - Configurazione IDE
        - Uso della shell bash
        - Strumenti di rete (ping, netstat, telnet)
    - `02-Classi-Networking-Java.md`
        - Panoramica `java.net`
        - Socket, ServerSocket, DatagramSocket
        - InetAddress e URL
    - `03-Strumenti-Debug-Networking.md`
        - Debugging delle connessioni
        - Log e monitoraggio
        - Troubleshooting comune
        - Strumenti di rete utili (ping, netstat, telnet, Wireshark, ...)

2. **Fondamenti dei Socket TCP**
    - `01-Creazione-Socket-TCP.md`
        - Classe Socket e ServerSocket
        - Stabilire connessioni
        - Gestione delle eccezioni
    - `02-Comunicazione-Dati-TCP.md`
        - InputStream e OutputStream
        - Invio e ricezione messaggi
        - Serializzazione oggetti
    - `03-Chiusura-Connessioni.md`
        - Chiusura corretta delle socket
        - Gestione risorse con try-with-resources
        - Cleanup e best practices

3. **Fondamenti dei Socket UDP**
    - `01-Socket-UDP-Base.md`
        - DatagramSocket e DatagramPacket
        - Invio e ricezione datagrammi
        - Gestione indirizzi destinazione
    - `02-Gestione-Eccezioni-UDP.md`
        - Eccezioni specifiche UDP
        - Timeout e retry logic
        - Gestione pacchetti persi
    - `03-Limitazioni-UDP.md`
        - Dimensione massima datagrammi
        - Ordine e duplicazione
        - Quando non usare UDP
    - `04-Applicazioni-UDP-Comuni.md`
        - DNS, DHCP, VoIP
        - Streaming multimediale
        - Giochi online

4. **Socket Multicast e Broadcast**
    - `01-Concetti-Multicast.md`
        - Indirizzi multicast
        - Gruppi multicast
        - IGMP e routing
    - `02-Socket-Multicast-Java.md`
        - MulticastSocket
        - Join e leave gruppi
        - TTL e scope
    - `03-Broadcast-Networking.md`
        - Broadcast limitato vs diretto
        - Implementazione broadcast
        - Limitazioni e sicurezza

5. **Multithreading**
    - `01-Multithreading-Java.md`
        - Thread e Runnable
        - Sincronizzazione
        - Problemi di concorrenza
    - `02-Server-Multithreaded.md`
        - Thread per client
        - Pool di thread
        - ExecutorService
    - `03-Gestione-Risorse-Condivise.md`
        - Locks e synchronized
        - Collections thread-safe
        - Atomic operations
    - `04-Esempi-Pratici-Multithreading.md`
        - Server Chat Multiutente
        - File Transfer Server

6. **Socket Sicuri (SSL/TLS)**
    - `01-Sicurezza-Comunicazioni.md`
        - Crittografia simmetrica/asimmetrica
        - SSL/TLS overview
        - Handshake SSL
    - `02-SSLSocket-Java.md`
        - SSLSocket e SSLServerSocket
        - SSLContext e KeyManager
        - Configurazione SSL
    - `03-Certificati-TrustStore.md`
        - Generazione certificati
        - KeyStore e TrustStore
        - Validazione certificati

7. **Socket e Networking Avanzato**
    - `01-Introduzione-NIO.md`
        - Differenze IO vs NIO
        - Channels e Buffers
        - Non-blocking I/O
    - `02-Selector-Multiplexing.md`
        - Selector e SelectionKey
        - Event-driven programming
        - Gestione eventi multipli
    - `03-Socket-Asincroni.md`
        - AsynchronousSocketChannel
        - CompletionHandler
        - Future-based operations
8. **Ottimizzazione**
    - `01-Timeout-KeepAlive.md`
        - Socket timeout
        - Keep-alive connections
        - Heartbeat patterns
    - `02-Buffering-Performance.md`
        - Buffer sizing
        - TCP_NODELAY
        - Performance tuning
    - `03-Gestione-Errori-Avanzata.md`
        - Strategie di retry
        - Circuit breaker pattern
        - Monitoring e metriche
9. **Architetture e Protocolli Personalizzati**
    - `01-Architettura-Applicazioni.md`
        - Design patterns per networking
        - Scalabilità e affidabilità
        - Testing e deployment
    - `02-Protocolli-Personalizzati.md`
        - Definizione protocolli applicativi
        - Serializzazione personalizzata
        - Versioning protocolli
10. **Casi di Studio e Progetti Completi**
    - `01-Progetto-Client-Server.md`
        - Specifiche del progetto
        - Pianificazione e design
        - Implementazione passo-passo
    - `02-Test-Validazione.md`
        - Test unitari e di integrazione
        - Strumenti di testing
        - Validazione delle performance
    - `03-Deployment-Manutenzione.md`
        - Deployment su server
        - Monitoraggio in produzione
        - Aggiornamenti e manutenzione
    - `04_web_server` - Web server HTTP
    - `05_distributed_chat` - Sistema chat distribuito
    - `06_file_sharing` - Sistema condivisione file
    - `07_game_server` - Server gioco multiplayer

---

