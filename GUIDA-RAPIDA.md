# 📋 Guida Rapida al Corso

## Struttura Completa del Corso

Il corso **Socket Programming in Java** è organizzato in 11 esercitazioni progressive, ognuna con guide teoriche dettagliate ed esempi pratici.

### 🗂️ Struttura Cartelle

```
Corso Socket Programming in Java/
├── README.md (questo file)
├── GUIDA-RAPIDA.md (panoramica e istruzioni)
│
├── 01-Introduzione-Networking/
│   ├── README.md
│   ├── 01-Concetti-Fondamentali-Networking.md
│   ├── 02-Introduzione-ai-Socket.md
│   ├── 03-Architetture-Client-Server.md
│   └── esempi/
│       ├── TestConnettivita.java
│       ├── SocketInfo.java
│       ├── ServerIterativo.java
│       ├── ServerConcorrente.java
│       └── ClientBase.java
│
├── 02-Ambiente-Sviluppo/
│   ├── README.md
│   ├── 01-Setup-Ambiente-Sviluppo.md
│   ├── 02-Classi-Networking-Java.md
│   ├── 03-Strumenti-Debug-Networking.md
│   └── esempi/
│       ├── TestAmbiente.java
│       └── NetworkUtils.java
│
├── 03-Socket-TCP-Fondamenti/
├── 04-Socket-UDP-Fondamenti/
├── 05-Socket-Multicast-Broadcast/
├── 06-Multithreading-Network/
├── 07-Socket-Sicuri-SSL/
├── 08-NIO-Socket-Avanzati/
├── 09-Ottimizzazione-Performance/
├── 10-Architetture-Protocolli/
└── 11-Progetti-Completi/
```

### 🎯 Come Utilizzare il Corso

1. **Segui l'ordine**: Le esercitazioni sono progressive e si basano l'una sull'altra
2. **Leggi la teoria**: Ogni guida teorica contiene concetti fondamentali
3. **Prova gli esempi**: Compila ed esegui tutti i codici di esempio
4. **Fai gli esercizi**: Ogni guida ha esercizi pratici per consolidare
5. **Testa**: Usa gli strumenti di debug per comprendere il comportamento

### 🚀 Quick Start

```bash
# 1. Verifica ambiente
cd 02-Ambiente-Sviluppo/esempi
javac TestAmbiente.java
java TestAmbiente

# 2. Testa esempi base
cd ../../01-Introduzione-Networking/esempi
javac ServerIterativo.java ClientBase.java
java ServerIterativo &     # Avvia server in background
java ClientBase            # Avvia client per testare

# 3. Test utilità di rete
cd ../../02-Ambiente-Sviluppo/esempi
javac NetworkUtils.java
java NetworkUtils scan localhost 8070 8090
```

### 💡 Suggerimenti per lo Studio

- **Ambiente**: Configura prima l'ambiente con l'esercitazione 02
- **Pratica**: Ogni esempio va compilato e testato
- **Debugging**: Usa gli strumenti dell'esercitazione 02 per analizzare il traffico
- **Esperimenti**: Modifica i parametri negli esempi per vedere gli effetti
- **Documentazione**: Consulta sempre la JavaDoc delle classi java.net

### 🔧 Risoluzione Problemi Comuni

**Errore "Address already in use"**:
```bash
# Trova processo che usa la porta
netstat -tupln | grep :8080
# Termina il processo
kill -9 <PID>
```

**Errore compilazione**:
```bash
# Verifica versione Java
java -version
javac -version
# Assicurati che siano compatibili
```

**Problemi di connessione**:
```bash
# Testa connettività
telnet localhost 8080
# Verifica firewall
sudo ufw status
```

---

**Buono studio! 🚀**

Per domande o problemi, consulta le guide teoriche dettagliate in ogni esercitazione.