# 🔧 Documentazione Tecnica - PortScannerMultithread

## Architettura del Sistema

### Overview
Il `PortScannerMultithread` implementa un pattern **Producer-Consumer** con **Thread Pool** per massimizzare le performance nelle scansioni di rete.

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Main Thread   │───►│  ExecutorService │───►│  Worker Threads │
│  (Producer)     │    │   (Thread Pool)  │    │  (Consumers)    │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                        │                        │
         ▼                        ▼                        ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│ Task Creation   │    │ Task Distribution│    │ Socket Testing  │
│ (Port Ranges)   │    │ & Load Balancing │    │ & Result Report │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### Componenti Principali

#### 1. **PortScanTask** (Callable)
```java
class PortScanTask implements Callable<Boolean>
```
- **Responsabilità**: Testare una singola porta
- **Input**: host, porta, timeout
- **Output**: boolean (porta aperta/chiusa)
- **Thread-Safety**: Completamente thread-safe

#### 2. **ProgressReporter** (Runnable)  
```java
class ProgressReporter implements Runnable
```
- **Responsabilità**: Reporting progress in tempo reale
- **Frequenza**: Aggiornamento ogni 2 secondi
- **Thread-Safety**: Utilizza AtomicInteger per contatori

#### 3. **ExecutorService** (Thread Pool)
```java
ExecutorService executor = Executors.newFixedThreadPool(threadCount)
```
- **Pattern**: Fixed Thread Pool
- **Vantaggi**: 
  - Controllo risorse CPU/memoria
  - Riutilizzo thread (no overhead creazione)
  - Gestione automatica lifecycle

## Gestione Concorrenza

### Thread-Safe Collections
```java
// Contatori atomici per statistiche
private static final AtomicInteger portsScanned = new AtomicInteger(0);
private static final AtomicInteger portsOpen = new AtomicInteger(0);

// Lista sincronizzata per porte aperte
private static final List<Integer> openPorts = 
    Collections.synchronizedList(new ArrayList<>());
```

### Sincronizzazione
- **AtomicInteger**: Per contatori condivisi tra thread
- **Collections.synchronizedList**: Per lista risultati
- **No Locks**: Evitati per massimizzare performance

### Gestione Risorse
```java
// Pattern try-with-resources per socket
try (Socket socket = new Socket()) {
    socket.connect(new InetSocketAddress(host, port), timeout);
    return true;
} catch (IOException e) {
    return false;
}
```

## Performance Optimization

### 1. **Connection Timeout Ottimizzato**
```java
private static final int DEFAULT_TIMEOUT = 1000; // 1 secondo
```
- **Rationale**: Bilancia velocità vs accuratezza
- **Tuning**: Riducibile per LAN, aumentabile per WAN

### 2. **Thread Pool Sizing**
```java
private static final int DEFAULT_THREAD_COUNT = 100;
```
**Formula ottimale**:
```
Optimal Threads = CPU_CORES × (1 + Wait_Time/CPU_Time)
```
- **I/O Bound**: Wait_Time >> CPU_Time → Molti thread
- **Nostro caso**: ~100-200 thread ottimali per port scanning

### 3. **Batch Processing Pattern**
```java
// Crea tutti i task in una volta
List<Future<Boolean>> futures = new ArrayList<>();
for (int port = startPort; port <= endPort; port++) {
    futures.add(executor.submit(new PortScanTask(host, port, timeout)));
}
```

### 4. **Non-Blocking Results Collection**
```java
// Asyncrono - non blocca main thread
for (Future<Boolean> future : futures) {
    future.get(); // Può essere reso non-bloccante
}
```

## Algoritmi di Scansione

### 1. **Connect Scan** (Implementato)
```java
Socket socket = new Socket();
socket.connect(new InetSocketAddress(host, port), timeout);
```
**Caratteristiche**:
- ✅ **Accurato**: TCP 3-way handshake completo
- ✅ **Affidabile**: Determina stato porta definitivo  
- ❌ **Rilevabile**: Logs nei sistemi target
- ❌ **Lento**: Full TCP connection per porta

### 2. **Possibili Miglioramenti**

#### **Half-Open Scan (SYN Scan)**
```java
// Richiederebbe raw socket (non disponibili in Java standard)
// Invia solo SYN, legge SYN-ACK vs RST
```

#### **UDP Scan**
```java
DatagramSocket udpSocket = new DatagramSocket();
// Invia UDP packet, interpreta risposte ICMP
```

## Metriche e Monitoring

### Statistiche Raccolte
```java
// Performance metrics
long startTime = System.currentTimeMillis();
double throughputPortsPerSecond = portsScanned.get() / durationSeconds;

// Accuracy metrics  
double successRate = (portsOpen.get() * 100.0) / portsScanned.get();
```

### Real-time Progress
```java
double progress = (scanned * 100.0) / totalPorts;
System.out.printf("📊 Progress: %.1f%% (%d/%d) - Aperte: %d%n", 
    progress, scanned, totalPorts, open);
```

## Gestione Errori

### Hierarchy delle Eccezioni
```java
try {
    socket.connect(address, timeout);
} catch (SocketTimeoutException e) {
    // Timeout → porta filtrata/chiusa
} catch (ConnectException e) {
    // Connection refused → porta chiusa
} catch (IOException e) {
    // Altri errori di rete
}
```

### Error Recovery
```java
// Graceful degradation - continua scansione anche con errori
catch (Exception e) {
    System.err.println("Errore porta " + port + ": " + e.getMessage());
    // Non interrompe scansione
}
```

## Scalabilità

### Horizontal Scaling
```java
// Possibile distribuzione su più host
public class DistributedPortScanner {
    // Range 1-10000 → Host A
    // Range 10001-20000 → Host B  
    // Range 20001-30000 → Host C
}
```

### Vertical Scaling
```java
// Auto-tuning basato su risorse sistema
int optimalThreads = Runtime.getRuntime().availableProcessors() * 4;
int maxMemoryMB = (int) (Runtime.getRuntime().maxMemory() / 1024 / 1024);
```

## Considerazioni di Sicurezza

### Rate Limiting
```java
// Implementabile per evitare detection
Thread.sleep(delayBetweenConnections);
```

### Source Port Randomization
```java
// Java gestisce automaticamente, ma potrebbe essere randomizzato manualmente
socket.bind(new InetSocketAddress(randomSourcePort));
```

### Stealth Mode
```java
// Connessioni più lente per evitare IDS/IPS
private static final int STEALTH_TIMEOUT = 5000; // 5 secondi
private static final int STEALTH_THREADS = 10;   // Pochi thread
```

## Testing e Benchmarking

### Performance Test
```bash
# Benchmark locale (massima velocità)
time java PortScannerMultithread localhost 1 1000 200

# Benchmark remoto (realistico)  
time java PortScannerMultithread scanme.nmap.org 1 1000 50
```

### Accuracy Test
```bash
# Confronta con nmap per accuracy
nmap -p1-1000 localhost
java PortScannerMultithread localhost 1 1000 100
```

### Stress Test
```bash
# Test stabilità con molti thread
java PortScannerMultithread localhost 1 65535 500
```

## Possibili Estensioni

### 1. **Service Detection**
```java
// Dopo connect, invia probe per identificare servizio
socket.getOutputStream().write("GET / HTTP/1.0\r\n\r\n".getBytes());
String response = readResponse(socket);
```

### 2. **OS Fingerprinting**  
```java
// Analizza TCP window size, TTL, ecc per OS detection
int windowSize = socket.getReceiveBufferSize();
```

### 3. **Vulnerability Scanning**
```java
// Integrazione con database CVE per porte/servizi noti
if (isVulnerableService(port, serviceVersion)) {
    reportVulnerability(port, cveId);
}
```

### 4. **Output Formats**
```java
// XML, JSON, CSV export
public void exportResults(Format format) {
    switch (format) {
        case XML: generateXMLReport(); break;
        case JSON: generateJSONReport(); break;
        case CSV: generateCSVReport(); break;
    }
}
```

## Conclusioni

Il `PortScannerMultithread` implementa un design robusto e scalabile che bilancia:
- **Performance** tramite multithreading ottimizzato
- **Accuratezza** tramite TCP connect scan completo  
- **Usabilità** tramite interfaccia CLI intuitiva
- **Affidabilità** tramite gestione errori robusta

La architettura modulare permette facili estensioni e personalizzazioni per esigenze specifiche.