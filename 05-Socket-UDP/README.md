# Modulo 05: Socket UDP

## 📝 Descrizione
Questo modulo approfondisce l'utilizzo dei **socket UDP** in Java, coprendo dalle basi ai pattern avanzati per comunicazioni ad alta velocità. UDP è ideale per applicazioni che richiedono velocità e possono tollerare perdite occasionali di pacchetti.

## 🎯 Obiettivi di Apprendimento
Al termine di questo modulo sarete in grado di:
- ✅ Implementare client e server UDP efficienti
- ✅ Utilizzare broadcast e multicast per comunicazioni uno-a-molti
- ✅ Ottimizzare performance per applicazioni real-time
- ✅ Implementare sistemi di service discovery
- ✅ Gestire reliability in ambiente connectionless

## 📚 Contenuti Teorici

0. [Il protocollo UDP](00-Il-protocollo-UDP.md)
### [1. Socket UDP - Fondamenti](01-Socket-UDP-Fondamenti.md)
- Caratteristiche del protocollo UDP
- API DatagramSocket e DatagramPacket
- Implementazione client/server base
- Gestione errori e timeout
- Best practices per UDP

### [2. Broadcast e Multicast](02-Broadcast-Multicast.md)
- Comunicazioni uno-a-molti
- Broadcast limitato e diretto
- Multicast con MulticastSocket
- Service discovery patterns
- Configurazione TTL e interfacce

### [3. Ottimizzazione Performance](03-UDP-Performance.md)
- Buffer management e pooling
- Single-threaded vs multi-threaded
- Metriche e monitoring real-time
- System-level tuning
- Load testing e benchmarking

## 🎯 Esempi Pratici

### 📁 Esempi Base
- **EchoUDP.java** - Echo server/client UDP semplice (dal Modulo 03)

### 📁 Esempi Avanzati
- **ServiceDiscoveryMulticast.java** - Sistema discovery servizi via multicast
- **HighPerformanceUDPServer.java** - Server UDP ad alta performance con monitoring
- **NetworkDiscoveryBroadcast.java** - Discovery dispositivi di rete via broadcast

> **💡 Nota:** Compila ed esegui gli esempi per sperimentare con i concetti

## 🔧 Esercitazioni Pratiche

### Esercizio 1: Chat Multicast
Implementa un sistema di chat che utilizza multicast UDP per permettere comunicazione di gruppo.

**Requisiti:**
- Gruppi multipli (canali)
- Join/leave dinamico
- Heartbeat per presenza utenti
- Interfaccia console interattiva

### Esercizio 2: File Sharing P2P
Crea un sistema di condivisione file peer-to-peer usando UDP per discovery e metadata.

**Requisiti:**
- Discovery automatico peers
- Catalog condivisione file
- Transfer via TCP (reliability)
- Load balancing su peer multipli

### Esercizio 3: Real-time Metrics
Sviluppa un sistema di monitoraggio che raccoglie metriche via UDP multicast.

**Requisiti:**
- Agent di raccolta dati
- Aggregazione real-time
- Dashboard metriche
- Alerting soglie

## 🧪 Test e Validazione

### Test di Performance
```bash
# Test throughput UDP
java HighPerformanceUDPServer server 8888
java HighPerformanceUDPServer loadtest localhost 8888 4 10000 60

# Test service discovery
java ServiceDiscoveryMulticast announcer MyService HTTP 8080 &
java ServiceDiscoveryMulticast discoverer

# Test network scanning
java NetworkDiscoveryBroadcast respond &
java NetworkDiscoveryBroadcast scan
```

### Metriche di Successo
- **Throughput**: >50K pps su hardware standard
- **Latency**: <1ms per processing singolo pacchetto
- **Discovery Time**: <5s per trovare servizi locali
- **Reliability**: <1% packet loss in LAN

## 🔗 Collegamenti
- **Modulo Precedente:** [04 - Server TCP multithread](../04-Miltithread/README.md)
- **Corso Principale:** [Socket Programming in Java](../README.md)

## 📖 Risorse Aggiuntive
- [RFC 768 - User Datagram Protocol](https://tools.ietf.org/html/rfc768)
- [RFC 1112 - Host Extensions for IP Multicasting](https://tools.ietf.org/html/rfc1112)
- [RFC 3171 - IANA Guidelines for IPv4 Multicast Address Assignments](https://tools.ietf.org/html/rfc3171)
- [Oracle Java Networking Tutorial](https://docs.oracle.com/javase/tutorial/networking/)

---