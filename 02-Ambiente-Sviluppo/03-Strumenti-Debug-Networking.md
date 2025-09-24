# 3. Strumenti Debug Networking

## Introduzione
Il debugging delle applicazioni di rete richiede strumenti specifici e tecniche mirate. Questa guida presenta gli strumenti essenziali per identificare e risolvere problemi di rete.

## Teoria

### Strumenti Essenziali

#### 1. Wireshark 🦈
**Analizzatore di pacchetti grafico**
```bash
# Installazione
sudo apt install wireshark
# Cattura traffico su interfaccia
sudo wireshark
```

#### 2. TCPDump 📦
**Analizzatore a riga di comando**
```bash
# Cattura tutto il traffico TCP
sudo tcpdump -i any tcp

# Cattura su porta specifica
sudo tcpdump -i any port 8080

# Salva in file per analisi
sudo tcpdump -w capture.pcap -i any port 8080
```

#### 3. Netcat (nc) 🔧
**Swiss Army Knife per networking**
```bash
# Server di test
nc -l 8080

# Client di test  
nc localhost 8080

# Port scanning
nc -zv target 1-1000
```

### Debugging Java Network Applications

#### JVM Network Properties
```bash
# Debug SSL/TLS
-Djavax.net.debug=ssl,handshake

# Debug tutti i network events
-Djava.net.useSystemProxies=true
-Dcom.sun.net.useExclusiveBind=false
```

#### Logging Avanzato
```java
// Configurazione logger per networking
Logger netLogger = Logger.getLogger("java.net");
netLogger.setLevel(Level.ALL);
```

## 🔗 Esempi di Riferimento

- [Esempio 01: Network Debugger](./esempi/NetworkDebugger.java) - Tool per debugging connessioni

## Navigazione del Corso
- [📑 Torna all'Indice del Corso](../README.md)
- [⬅️ Guida Precedente](02-Classi-Networking-Java.md)
- [➡️ Esercitazione Successiva](../03-Socket-TCP-Fondamenti/README.md)