# 3. Ottimizzazione Performance UDP

## Introduzione
Questa guida esplora le tecniche avanzate per **massimizzare le performance** delle applicazioni UDP, coprendo ottimizzazioni a livello di socket, buffer management, threading e monitoring delle metriche di rete.

## Teoria

### Bottleneck Comuni in UDP

#### Network Bottlenecks
```java
// Problemi di rete che influenzano performance:
// 1. Bandwidth limitato
// 2. Latency alta (RTT)  
// 3. Packet loss elevato
// 4. Jitter (variabilità timing)
// 5. Congestione di rete
```

#### Application Bottlenecks
```java
// Problemi applicativi:
// 1. Buffer troppo piccoli
// 2. Threading inefficiente
// 3. Allocazioni memoria eccessive
// 4. Processing sincrono lento
// 5. I/O blocking inappropriato
```

#### System Bottlenecks
```java
// Limitazioni sistema operativo:
// 1. Limiti file descriptor
// 2. Buffer kernel insufficienti
// 3. Context switching eccessivo
// 4. Interrupt handling overhead
// 5. Memory mapping inefficiente
```

### Ottimizzazione Socket

#### Buffer Tuning
```java
public class HighPerformanceUDPSocket {
    
    public static DatagramSocket createOptimizedSocket(int port) throws SocketException {
        DatagramSocket socket = new DatagramSocket(port);
        
        // Aumenta buffer del kernel per ridurre packet drop
        socket.setReceiveBufferSize(2 * 1024 * 1024); // 2MB
        socket.setSendBufferSize(2 * 1024 * 1024);    // 2MB
        
        // Verifica dimensioni effettive (OS può limitare)
        int actualRcvBuffer = socket.getReceiveBufferSize();
        int actualSndBuffer = socket.getSendBufferSize();
        
        System.out.println("📊 Buffer configurati:");
        System.out.println("   Ricezione: " + actualRcvBuffer + " bytes");
        System.out.println("   Invio: " + actualSndBuffer + " bytes");
        
        // Performance tuning aggiuntivo
        socket.setReuseAddress(true); // Riutilizzo rapido porta
        
        return socket;
    }
    
    // Calcola dimensione buffer ottimale basata su bandwidth e RTT
    public static int calculateOptimalBufferSize(long bandwidthBps, int rttMs) {
        // Bandwidth-Delay Product (BDP)
        long bdp = (bandwidthBps * rttMs) / 8000; // Byte
        
        // Buffer dovrebbe essere almeno 2x BDP per piena utilizzazione
        int optimalSize = (int) Math.min(bdp * 2, Integer.MAX_VALUE);
        
        // Arrotonda a potenza di 2 per efficienza memoria
        return Integer.highestOneBit(optimalSize) * 2;
    }
}
```

#### Socket Options Avanzate
```java
public class AdvancedSocketConfig {
    
    public static void configureHighThroughput(DatagramSocket socket) throws SocketException {
        // Priorità di traffico (se supportato dal OS)
        socket.setTrafficClass(0x10); // IPTOS_THROUGHPUT
        
        // Disabilita controllo di congestione Nagle-like
        // (UDP non ha Nagle, ma alcuni OS applicano controlli simili)
        
        // Timeout per operazioni non-blocking
        socket.setSoTimeout(1); // 1ms per polling rapido
    }
    
    public static void configureLowLatency(DatagramSocket socket) throws SocketException {
        // Priorità bassa latenza
        socket.setTrafficClass(0x08); // IPTOS_LOWDELAY
        
        // Buffer più piccoli per ridurre buffering delay
        socket.setReceiveBufferSize(64 * 1024); // 64KB
        socket.setSendBufferSize(64 * 1024);
        
        // Timeout minimale
        socket.setSoTimeout(0); // Blocking completo o polling
    }
}
```

### Buffer Pool Management

#### Oggetto Buffer Riutilizzabile
```java
public class UDPBufferPool {
    private final int bufferSize;
    private final ConcurrentLinkedQueue<ByteBuffer> availableBuffers;
    private final AtomicInteger totalBuffers = new AtomicInteger(0);
    private final int maxPoolSize;
    
    public UDPBufferPool(int bufferSize, int initialPoolSize, int maxPoolSize) {
        this.bufferSize = bufferSize;
        this.maxPoolSize = maxPoolSize;
        this.availableBuffers = new ConcurrentLinkedQueue<>();
        
        // Pre-alloca buffer iniziali
        for (int i = 0; i < initialPoolSize; i++) {
            availableBuffers.offer(ByteBuffer.allocateDirect(bufferSize));
            totalBuffers.incrementAndGet();
        }
    }
    
    public ByteBuffer acquire() {
        ByteBuffer buffer = availableBuffers.poll();
        
        if (buffer == null) {
            // Crea nuovo buffer se pool vuoto e sotto limite
            if (totalBuffers.get() < maxPoolSize) {
                buffer = ByteBuffer.allocateDirect(bufferSize);
                totalBuffers.incrementAndGet();
            } else {
                // Fallback: alloca buffer temporaneo (non pooled)
                return ByteBuffer.allocate(bufferSize);
            }
        }
        
        buffer.clear(); // Reset position/limit
        return buffer;
    }
    
    public void release(ByteBuffer buffer) {
        if (buffer.isDirect() && buffer.capacity() == bufferSize) {
            buffer.clear();
            availableBuffers.offer(buffer);
        }
        // Ignora buffer non-pooled o dimensione errata
    }
    
    public int getPoolSize() {
        return availableBuffers.size();
    }
    
    public int getTotalBuffers() {
        return totalBuffers.get();
    }
}
```

#### Batch Processing
```java
public class BatchUDPProcessor {
    private final DatagramSocket socket;
    private final UDPBufferPool bufferPool;
    private final int batchSize;
    
    public BatchUDPProcessor(DatagramSocket socket, int batchSize) {
        this.socket = socket;
        this.batchSize = batchSize;
        this.bufferPool = new UDPBufferPool(1500, batchSize * 2, batchSize * 4);
    }
    
    // Riceve multipli pacchetti in batch per efficienza
    public List<DatagramPacket> receiveBatch() throws IOException {
        List<DatagramPacket> packets = new ArrayList<>(batchSize);
        
        // Configura socket per non-blocking
        socket.setSoTimeout(1); // 1ms timeout
        
        for (int i = 0; i < batchSize; i++) {
            try {
                ByteBuffer buffer = bufferPool.acquire();
                DatagramPacket packet = new DatagramPacket(
                    buffer.array(), buffer.capacity());
                
                socket.receive(packet);
                packets.add(packet);
                
            } catch (SocketTimeoutException e) {
                // Nessun pacchetto disponibile, ritorna batch parziale
                break;
            }
        }
        
        return packets;
    }
    
    // Processa batch di pacchetti
    public void processBatch(List<DatagramPacket> packets) {
        for (DatagramPacket packet : packets) {
            // Processing parallelo se appropriato
            processPacket(packet);
            
            // Rilascia buffer al pool
            if (packet.getData() instanceof byte[]) {
                ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
                bufferPool.release(buffer);
            }
        }
    }
    
    private void processPacket(DatagramPacket packet) {
        // Elaborazione specifica dell'applicazione
        String content = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Processed: " + content);
    }
}
```

### Threading Strategies

#### Single-threaded Event Loop (High Performance)
```java
public class SingleThreadedUDPServer {
    private final DatagramSocket socket;
    private final ByteBuffer receiveBuffer;
    private final Queue<DatagramPacket> sendQueue;
    private volatile boolean running = false;
    
    public SingleThreadedUDPServer(int port, int bufferSize) throws IOException {
        this.socket = HighPerformanceUDPSocket.createOptimizedSocket(port);
        this.receiveBuffer = ByteBuffer.allocateDirect(bufferSize);
        this.sendQueue = new ConcurrentLinkedQueue<>();
        
        // Non-blocking socket
        socket.setSoTimeout(0);
    }
    
    public void start() {
        running = true;
        
        System.out.println("🚀 Single-threaded UDP server avviato");
        
        while (running) {
            // 1. Ricevi pacchetti disponibili
            receiveAvailablePackets();
            
            // 2. Invia pacchetti in coda
            sendQueuedPackets();
            
            // 3. Processing applicativo
            processApplicationLogic();
            
            // Yield CPU brevemente per altri thread
            Thread.yield();
        }
    }
    
    private void receiveAvailablePackets() {
        try {
            socket.setSoTimeout(1); // Polling rapido
            
            byte[] buffer = new byte[receiveBuffer.capacity()];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            
            while (running) {
                socket.receive(packet);
                
                // Processa immediatamente per massima performance
                handlePacket(packet);
                
                // Reset per prossimo pacchetto
                packet.setLength(buffer.length);
            }
            
        } catch (SocketTimeoutException e) {
            // Normale - nessun pacchetto disponibile
        } catch (IOException e) {
            System.err.println("Errore ricezione: " + e.getMessage());
        }
    }
    
    private void sendQueuedPackets() {
        DatagramPacket packet;
        while ((packet = sendQueue.poll()) != null) {
            try {
                socket.send(packet);
            } catch (IOException e) {
                System.err.println("Errore invio: " + e.getMessage());
                // Considera retry o dead letter queue
            }
        }
    }
    
    private void handlePacket(DatagramPacket packet) {
        // Processing immediato per bassa latenza
        String message = new String(packet.getData(), 0, packet.getLength());
        
        // Echo response
        String response = "Echo: " + message;
        byte[] responseData = response.getBytes();
        
        DatagramPacket responsePacket = new DatagramPacket(
            responseData, responseData.length,
            packet.getAddress(), packet.getPort());
        
        // Accoda per invio
        sendQueue.offer(responsePacket);
    }
    
    private void processApplicationLogic() {
        // Logica applicativa che non blocca
        // Es: pulizia periodica, statistiche, ecc.
    }
}
```

#### Producer-Consumer Pattern
```java
public class ProducerConsumerUDPServer {
    private final DatagramSocket socket;
    private final BlockingQueue<DatagramPacket> receiveQueue;
    private final BlockingQueue<DatagramPacket> sendQueue;
    private final ExecutorService threadPool;
    private final UDPBufferPool bufferPool;
    
    public ProducerConsumerUDPServer(int port, int threadCount) throws IOException {
        this.socket = HighPerformanceUDPSocket.createOptimizedSocket(port);
        this.receiveQueue = new ArrayBlockingQueue<>(1000);
        this.sendQueue = new ArrayBlockingQueue<>(1000);
        this.threadPool = Executors.newFixedThreadPool(threadCount);
        this.bufferPool = new UDPBufferPool(1500, 100, 500);
    }
    
    public void start() {
        // Thread Producer: riceve pacchetti
        threadPool.submit(this::receiveLoop);
        
        // Thread Consumer: processa pacchetti  
        for (int i = 0; i < 4; i++) { // 4 thread processor
            threadPool.submit(this::processLoop);
        }
        
        // Thread Sender: invia risposte
        threadPool.submit(this::sendLoop);
        
        System.out.println("🚀 Producer-Consumer UDP server avviato");
    }
    
    private void receiveLoop() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                ByteBuffer buffer = bufferPool.acquire();
                DatagramPacket packet = new DatagramPacket(
                    buffer.array(), buffer.capacity());
                
                socket.receive(packet);
                
                // Accoda per processing
                if (!receiveQueue.offer(packet, 100, TimeUnit.MILLISECONDS)) {
                    System.err.println("⚠️ Receive queue piena - pacchetto scartato");
                    bufferPool.release(buffer);
                }
            }
        } catch (Exception e) {
            System.err.println("Errore receive loop: " + e.getMessage());
        }
    }
    
    private void processLoop() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket packet = receiveQueue.take();
                
                // Processing del pacchetto
                String message = new String(packet.getData(), 0, packet.getLength());
                
                // Simula processing time
                Thread.sleep(1); // 1ms processing
                
                // Crea risposta
                String response = "Processed: " + message;
                byte[] responseData = response.getBytes();
                
                DatagramPacket responsePacket = new DatagramPacket(
                    responseData, responseData.length,
                    packet.getAddress(), packet.getPort());
                
                // Accoda per invio
                sendQueue.offer(responsePacket);
                
                // Rilascia buffer
                ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
                bufferPool.release(buffer);
            }
        } catch (Exception e) {
            System.err.println("Errore process loop: " + e.getMessage());
        }
    }
    
    private void sendLoop() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket packet = sendQueue.take();
                socket.send(packet);
            }
        } catch (Exception e) {
            System.err.println("Errore send loop: " + e.getMessage());
        }
    }
}
```

### Performance Monitoring

#### Metriche Real-time
```java
public class UDPPerformanceMonitor {
    private final AtomicLong packetsReceived = new AtomicLong(0);
    private final AtomicLong packetsSent = new AtomicLong(0);
    private final AtomicLong bytesReceived = new AtomicLong(0);
    private final AtomicLong bytesSent = new AtomicLong(0);
    private final AtomicLong packetsDropped = new AtomicLong(0);
    
    // Latenza tracking
    private final LongAdder totalLatency = new LongAdder();
    private final AtomicLong latencyMeasurements = new AtomicLong(0);
    
    // Throughput sliding window
    private final RingBuffer<Long> throughputWindow = new RingBuffer<>(60); // 60 secondi
    private volatile long lastStatsTime = System.currentTimeMillis();
    
    public void recordPacketReceived(int bytes) {
        packetsReceived.incrementAndGet();
        bytesReceived.addAndGet(bytes);
    }
    
    public void recordPacketSent(int bytes) {
        packetsSent.incrementAndGet();
        bytesSent.addAndGet(bytes);
    }
    
    public void recordPacketDropped() {
        packetsDropped.incrementAndGet();
    }
    
    public void recordLatency(long latencyNanos) {
        totalLatency.add(latencyNanos);
        latencyMeasurements.incrementAndGet();
    }
    
    public void printStats() {
        long now = System.currentTimeMillis();
        long timeDiff = now - lastStatsTime;
        
        if (timeDiff >= 1000) { // Ogni secondo
            long currentThroughput = bytesReceived.get();
            throughputWindow.add(currentThroughput);
            
            System.out.println("\n📊 UDP Performance Stats:");
            System.out.println("   Pacchetti RX: " + packetsReceived.get());
            System.out.println("   Pacchetti TX: " + packetsSent.get());
            System.out.println("   Bytes RX: " + formatBytes(bytesReceived.get()));
            System.out.println("   Bytes TX: " + formatBytes(bytesSent.get()));
            System.out.println("   Throughput: " + calculateThroughput() + " MB/s");
            System.out.println("   Latenza media: " + getAverageLatency() + " μs");
            System.out.println("   Packet loss: " + getPacketLossRate() + "%");
            
            lastStatsTime = now;
        }
    }
    
    private double calculateThroughput() {
        if (throughputWindow.size() < 2) return 0.0;
        
        long first = throughputWindow.get(0);
        long last = throughputWindow.get(throughputWindow.size() - 1);
        long timeSpan = throughputWindow.size(); // secondi
        
        return ((last - first) / (double) timeSpan) / (1024 * 1024); // MB/s
    }
    
    private double getAverageLatency() {
        long measurements = latencyMeasurements.get();
        if (measurements == 0) return 0.0;
        
        return (totalLatency.sum() / (double) measurements) / 1000.0; // microsecondi
    }
    
    private double getPacketLossRate() {
        long total = packetsReceived.get() + packetsDropped.get();
        if (total == 0) return 0.0;
        
        return (packetsDropped.get() * 100.0) / total;
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
    
    // Simple ring buffer per sliding window
    private static class RingBuffer<T> {
        private final Object[] buffer;
        private int position = 0;
        private int size = 0;
        
        public RingBuffer(int capacity) {
            buffer = new Object[capacity];
        }
        
        public void add(T item) {
            buffer[position] = item;
            position = (position + 1) % buffer.length;
            if (size < buffer.length) size++;
        }
        
        @SuppressWarnings("unchecked")
        public T get(int index) {
            if (index >= size) throw new IndexOutOfBoundsException();
            int actualIndex = (position - size + index + buffer.length) % buffer.length;
            return (T) buffer[actualIndex];
        }
        
        public int size() { return size; }
    }
}
```

### System-level Optimizations

#### Linux Kernel Tuning
```bash
# Configurazioni kernel per alta performance UDP

# Aumenta buffer di rete del kernel
echo 'net.core.rmem_max = 134217728' >> /etc/sysctl.conf          # 128MB
echo 'net.core.rmem_default = 65536' >> /etc/sysctl.conf         # 64KB  
echo 'net.core.wmem_max = 134217728' >> /etc/sysctl.conf         # 128MB
echo 'net.core.wmem_default = 65536' >> /etc/sysctl.conf         # 64KB

# Aumenta buffer UDP specifici
echo 'net.ipv4.udp_mem = 102400 873800 16777216' >> /etc/sysctl.conf
echo 'net.ipv4.udp_rmem_min = 8192' >> /etc/sysctl.conf
echo 'net.ipv4.udp_wmem_min = 8192' >> /etc/sysctl.conf

# Ottimizzazioni generali rete
echo 'net.core.netdev_max_backlog = 30000' >> /etc/sysctl.conf   # Queue NIC
echo 'net.core.netdev_budget = 600' >> /etc/sysctl.conf          # Packet per interrupt

# Applica modifiche
sysctl -p
```

#### JVM Tuning
```bash
# Parametri JVM per performance UDP
java -XX:+UseG1GC \                          # Garbage collector a bassa latenza
     -XX:MaxGCPauseMillis=1 \                # Target GC pause 1ms
     -XX:+UnlockExperimentalVMOptions \
     -XX:+UseLargePages \                    # Large pages per memoria
     -XX:+AlwaysPreTouch \                   # Pre-alloca memoria
     -Xms4g -Xmx4g \                         # Heap fisso per evitare resize
     -XX:NewRatio=1 \                        # 50% young generation
     -XX:+DisableExplicitGC \                # Disabilita System.gc()
     com.example.HighPerformanceUDPApp
```

## Benchmarking e Testing

### Load Testing UDP
```java
public class UDPLoadTester {
    
    public static void loadTestServer(String host, int port, int threadCount, 
                                    int packetsPerSecond, int durationSeconds) {
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(threadCount);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        
        AtomicLong packetsSent = new AtomicLong(0);
        AtomicLong packetsReceived = new AtomicLong(0);
        
        int packetsPerThread = packetsPerSecond / threadCount;
        long intervalNanos = TimeUnit.SECONDS.toNanos(1) / packetsPerThread;
        
        // Avvia thread di load
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try (DatagramSocket socket = new DatagramSocket()) {
                    InetAddress address = InetAddress.getByName(host);
                    
                    startLatch.countDown();
                    startLatch.await(); // Sincronizza avvio
                    
                    long startTime = System.nanoTime();
                    long endTime = startTime + TimeUnit.SECONDS.toNanos(durationSeconds);
                    long nextSendTime = startTime;
                    
                    while (System.nanoTime() < endTime) {
                        // Rate limiting preciso
                        long now = System.nanoTime();
                        if (now < nextSendTime) {
                            LockSupport.parkNanos(nextSendTime - now);
                        }
                        
                        // Invia pacchetto
                        String message = "Thread-" + threadId + "-" + packetsSent.incrementAndGet();
                        byte[] data = message.getBytes();
                        
                        DatagramPacket packet = new DatagramPacket(
                            data, data.length, address, port);
                        
                        socket.send(packet);
                        
                        // Prossimo invio
                        nextSendTime += intervalNanos;
                    }
                    
                } catch (Exception e) {
                    System.err.println("Thread " + threadId + " errore: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }
        
        try {
            endLatch.await();
            
            System.out.println("📊 Load Test Completato:");
            System.out.println("   Pacchetti inviati: " + packetsSent.get());
            System.out.println("   Target rate: " + packetsPerSecond + " pps");
            System.out.println("   Durata: " + durationSeconds + " secondi");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
    }
}
```

## Best Practices Avanzate

### ✅ Performance Tips
1. **Usa buffer diretti** (ByteBuffer.allocateDirect()) per zero-copy
2. **Pool di buffer** per ridurre GC pressure
3. **Single-threaded event loop** per massima performance
4. **Batch processing** per ridurre syscall overhead
5. **Tune kernel buffers** per il tuo workload specifico

### ❌ Performance Anti-patterns
1. **Allocare buffer in loop** - causa GC thrashing
2. **Threading eccessivo** - context switching overhead
3. **Blocking I/O** in hot path - limita throughput
4. **Buffer troppo piccoli** - aumenta syscall frequency
5. **Non monitorare metriche** - performance degradation invisibile

---
[🏠 Torna al Modulo](../README.md) | [⬅️ Lezione Precedente](02-Broadcast-Multicast.md)