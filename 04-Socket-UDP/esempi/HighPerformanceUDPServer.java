/**
 * Nome dell'Esempio: High Performance UDP Server
 * Guida di Riferimento: 03-UDP-Performance.md
 * 
 * Obiettivo: Server UDP ottimizzato per massime performance e throughput.
 * 
 * Spiegazione:
 * 1. Buffer pooling per zero-allocation
 * 2. Single-threaded event loop per bassa latenza
 * 3. Monitoring real-time delle performance
 * 
 * @author Socket Programming Course
 * @version 1.0
 */

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.LockSupport;

public class HighPerformanceUDPServer {
    
    // Configurazione performance
    private static final int DEFAULT_BUFFER_SIZE = 1400; // Safe UDP size
    private static final int BUFFER_POOL_SIZE = 1000;
    private static final int MAX_BATCH_SIZE = 50;
    private static final int STATS_INTERVAL_MS = 5000; // 5 secondi
    
    /**
     * Pool di buffer per minimizzare allocazioni
     */
    public static class BufferPool {
        private final ConcurrentLinkedQueue<ByteBuffer> availableBuffers;
        private final int bufferSize;
        private final int maxPoolSize;
        private final AtomicInteger totalBuffers = new AtomicInteger(0);
        private final AtomicLong allocations = new AtomicLong(0);
        private final AtomicLong deallocations = new AtomicLong(0);
        
        public BufferPool(int bufferSize, int initialSize, int maxPoolSize) {
            this.bufferSize = bufferSize;
            this.maxPoolSize = maxPoolSize;
            this.availableBuffers = new ConcurrentLinkedQueue<>();
            
            // Pre-alloca buffer iniziali (direct memory)
            for (int i = 0; i < initialSize; i++) {
                availableBuffers.offer(ByteBuffer.allocateDirect(bufferSize));
                totalBuffers.incrementAndGet();
            }
            
            System.out.println("🏊 Buffer Pool inizializzato:");
            System.out.println("   Dimensione buffer: " + bufferSize + " bytes");
            System.out.println("   Pool iniziale: " + initialSize);
            System.out.println("   Pool massimo: " + maxPoolSize);
        }
        
        public ByteBuffer acquire() {
            allocations.incrementAndGet();
            
            ByteBuffer buffer = availableBuffers.poll();
            if (buffer == null) {
                // Pool vuoto - crea nuovo buffer se sotto limite
                if (totalBuffers.get() < maxPoolSize) {
                    buffer = ByteBuffer.allocateDirect(bufferSize);
                    totalBuffers.incrementAndGet();
                } else {
                    // Fallback: heap buffer temporaneo
                    return ByteBuffer.allocate(bufferSize);
                }
            }
            
            buffer.clear();
            return buffer;
        }
        
        public void release(ByteBuffer buffer) {
            if (buffer != null && buffer.isDirect() && buffer.capacity() == bufferSize) {
                deallocations.incrementAndGet();
                buffer.clear();
                availableBuffers.offer(buffer);
            }
        }
        
        public int getAvailableCount() {
            return availableBuffers.size();
        }
        
        public int getTotalBuffers() {
            return totalBuffers.get();
        }
        
        public long getAllocations() {
            return allocations.get();
        }
        
        public long getDeallocations() {
            return deallocations.get();
        }
    }
    
    /**
     * Metriche di performance real-time
     */
    public static class PerformanceMetrics {
        private final AtomicLong packetsReceived = new AtomicLong(0);
        private final AtomicLong packetsSent = new AtomicLong(0);
        private final AtomicLong bytesReceived = new AtomicLong(0);
        private final AtomicLong bytesSent = new AtomicLong(0);
        private final AtomicLong errors = new AtomicLong(0);
        
        // Latency tracking con histogram semplice
        private final AtomicLongArray latencyBuckets = new AtomicLongArray(10);
        private final AtomicLong totalLatency = new AtomicLong(0);
        private final AtomicLong latencyMeasurements = new AtomicLong(0);
        
        // Throughput tracking
        private volatile long lastStatsTime = System.currentTimeMillis();
        private volatile long lastPacketsReceived = 0;
        private volatile long lastBytesReceived = 0;
        private volatile double currentPps = 0.0; // Packets per second
        private volatile double currentThroughput = 0.0; // MB/s
        
        public void recordPacketReceived(int bytes) {
            packetsReceived.incrementAndGet();
            bytesReceived.addAndGet(bytes);
        }
        
        public void recordPacketSent(int bytes) {
            packetsSent.incrementAndGet();
            bytesSent.addAndGet(bytes);
        }
        
        public void recordLatency(long nanos) {
            totalLatency.addAndGet(nanos);
            latencyMeasurements.incrementAndGet();
            
            // Histogram buckets (microsecondi)
            long micros = nanos / 1000;
            int bucket = Math.min((int) Math.log10(Math.max(1, micros)), 9);
            latencyBuckets.incrementAndGet(bucket);
        }
        
        public void recordError() {
            errors.incrementAndGet();
        }
        
        public void updateThroughput() {
            long now = System.currentTimeMillis();
            long timeDiff = now - lastStatsTime;
            
            if (timeDiff >= 1000) { // Ogni secondo
                long currentPackets = packetsReceived.get();
                long currentBytes = bytesReceived.get();
                
                currentPps = ((currentPackets - lastPacketsReceived) * 1000.0) / timeDiff;
                currentThroughput = ((currentBytes - lastBytesReceived) * 1000.0) / (timeDiff * 1024 * 1024);
                
                lastStatsTime = now;
                lastPacketsReceived = currentPackets;
                lastBytesReceived = currentBytes;
            }
        }
        
        public void printStats(BufferPool bufferPool) {
            System.out.println("\n📊 PERFORMANCE METRICS:");
            System.out.println("   Packets RX: " + packetsReceived.get() + 
                             " (" + String.format("%.0f", currentPps) + " pps)");
            System.out.println("   Packets TX: " + packetsSent.get());
            System.out.println("   Throughput: " + String.format("%.2f", currentThroughput) + " MB/s");
            System.out.println("   Bytes RX: " + formatBytes(bytesReceived.get()));
            System.out.println("   Bytes TX: " + formatBytes(bytesSent.get()));
            System.out.println("   Errori: " + errors.get());
            
            long measurements = latencyMeasurements.get();
            if (measurements > 0) {
                double avgLatency = (totalLatency.get() / (double) measurements) / 1000.0;
                System.out.println("   Latenza media: " + String.format("%.1f", avgLatency) + " μs");
            }
            
            System.out.println("   Buffer pool: " + bufferPool.getAvailableCount() + 
                             "/" + bufferPool.getTotalBuffers() + " disponibili");
        }
        
        private String formatBytes(long bytes) {
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
            if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
            return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
    
    /**
     * Server UDP ad alta performance con single-threaded event loop
     */
    public static class Server {
        private final int port;
        private final BufferPool bufferPool;
        private final PerformanceMetrics metrics;
        private volatile boolean running = false;
        private DatagramSocket socket;
        
        // Batch processing
        private final List<DatagramPacket> receiveBatch = new ArrayList<>(MAX_BATCH_SIZE);
        private final Queue<DatagramPacket> sendQueue = new ConcurrentLinkedQueue<>();
        
        public Server(int port) {
            this.port = port;
            this.bufferPool = new BufferPool(DEFAULT_BUFFER_SIZE, BUFFER_POOL_SIZE, BUFFER_POOL_SIZE * 2);
            this.metrics = new PerformanceMetrics();
        }
        
        public void start() throws IOException {
            socket = new DatagramSocket(port);
            
            // Ottimizzazioni socket
            optimizeSocket(socket);
            
            running = true;
            
            System.out.println("🚀 High Performance UDP Server");
            System.out.println("   Porta: " + port);
            System.out.println("   Buffer RX: " + socket.getReceiveBufferSize() + " bytes");
            System.out.println("   Buffer TX: " + socket.getSendBufferSize() + " bytes");
            System.out.println("🛑 Premi Ctrl+C per fermare");
            System.out.println("=" .repeat(50));
            
            // Avvia thread per statistiche
            startStatsThread();
            
            // Main event loop - single thread per massima performance
            eventLoop();
        }
        
        private void optimizeSocket(DatagramSocket socket) throws SocketException {
            // Buffer kernel grandi per ridurre packet drop
            socket.setReceiveBufferSize(4 * 1024 * 1024); // 4MB
            socket.setSendBufferSize(4 * 1024 * 1024);    // 4MB
            
            // Riutilizzo address per restart rapido
            socket.setReuseAddress(true);
            
            // Timeout minimale per polling
            socket.setSoTimeout(1); // 1ms
        }
        
        private void eventLoop() {
            System.out.println("✅ Event loop avviato (single-threaded)");
            
            while (running) {
                try {
                    // 1. Ricevi batch di pacchetti
                    int received = receiveBatch();
                    
                    // 2. Processa batch
                    if (received > 0) {
                        processBatch();
                    }
                    
                    // 3. Invia risposte in coda
                    sendQueuedPackets();
                    
                    // 4. Aggiorna metriche
                    metrics.updateThroughput();
                    
                    // Yield CPU se nessuna attività
                    if (received == 0) {
                        Thread.yield();
                    }
                    
                } catch (IOException e) {
                    if (running) {
                        metrics.recordError();
                        System.err.println("💥 Errore event loop: " + e.getMessage());
                    }
                }
            }
        }
        
        private int receiveBatch() throws IOException {
            receiveBatch.clear();
            
            for (int i = 0; i < MAX_BATCH_SIZE; i++) {
                try {
                    ByteBuffer buffer = bufferPool.acquire();
                    DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.capacity());
                    
                    socket.receive(packet);
                    receiveBatch.add(packet);
                    
                } catch (SocketTimeoutException e) {
                    // Normale - nessun pacchetto disponibile
                    break;
                }
            }
            
            return receiveBatch.size();
        }
        
        private void processBatch() {
            for (DatagramPacket packet : receiveBatch) {
                long startTime = System.nanoTime();
                
                try {
                    processPacket(packet);
                    
                } catch (Exception e) {
                    metrics.recordError();
                    System.err.println("⚠️ Errore processing: " + e.getMessage());
                } finally {
                    // Rilascia buffer al pool
                    ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
                    bufferPool.release(buffer);
                    
                    // Record latency
                    long latency = System.nanoTime() - startTime;
                    metrics.recordLatency(latency);
                }
            }
        }
        
        private void processPacket(DatagramPacket packet) throws IOException {
            metrics.recordPacketReceived(packet.getLength());
            
            // Estrae messaggio
            String message = new String(packet.getData(), 0, packet.getLength());
            
            // Elaborazione basata su comando
            String response = handleCommand(message, packet.getAddress(), packet.getPort());
            
            // Prepara risposta
            if (response != null) {
                byte[] responseData = response.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(
                    responseData, responseData.length,
                    packet.getAddress(), packet.getPort());
                
                sendQueue.offer(responsePacket);
            }
        }
        
        private String handleCommand(String message, InetAddress clientAddr, int clientPort) {
            String[] parts = message.trim().split("\\s+", 2);
            String command = parts[0].toUpperCase();
            
            switch (command) {
                case "PING":
                    return "PONG " + System.currentTimeMillis();
                
                case "ECHO":
                    return parts.length > 1 ? "ECHO: " + parts[1] : "ECHO: (empty)";
                
                case "TIME":
                    return "TIME " + System.currentTimeMillis();
                
                case "STATS":
                    return String.format("STATS pkt_rx=%d pkt_tx=%d throughput=%.2f_MBps",
                                       metrics.packetsReceived.get(),
                                       metrics.packetsSent.get(),
                                       metrics.currentThroughput);
                
                case "LOAD":
                    // Comando per load testing - genera carico CPU
                    if (parts.length > 1) {
                        try {
                            int iterations = Integer.parseInt(parts[1]);
                            return "LOAD " + simulateLoad(iterations);
                        } catch (NumberFormatException e) {
                            return "ERROR invalid load parameter";
                        }
                    }
                    return "LOAD 1000";
                
                case "QUIT":
                    return "BYE";
                
                default:
                    return "ERROR unknown command: " + command;
            }
        }
        
        private int simulateLoad(int iterations) {
            // Simula carico computazionale
            int result = 0;
            for (int i = 0; i < iterations; i++) {
                result += Math.sqrt(i) * Math.sin(i);
            }
            return result;
        }
        
        private void sendQueuedPackets() throws IOException {
            int sent = 0;
            DatagramPacket packet;
            
            // Limita il numero di invii per batch per evitare starvation
            while (sent < MAX_BATCH_SIZE && (packet = sendQueue.poll()) != null) {
                socket.send(packet);
                metrics.recordPacketSent(packet.getLength());
                sent++;
            }
        }
        
        private void startStatsThread() {
            Thread statsThread = new Thread(() -> {
                while (running) {
                    try {
                        Thread.sleep(STATS_INTERVAL_MS);
                        if (running) {
                            metrics.printStats(bufferPool);
                        }
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }, "StatsThread");
            
            statsThread.setDaemon(true);
            statsThread.start();
        }
        
        public void stop() {
            running = false;
            
            if (socket != null) {
                socket.close();
            }
            
            System.out.println("\n✅ Server arrestato");
            System.out.println("📊 Statistiche finali:");
            metrics.printStats(bufferPool);
        }
    }
    
    /**
     * Client per load testing
     */
    public static class LoadTestClient {
        
        public static void runLoadTest(String host, int port, int threadsCount, 
                                     int packetsPerSecond, int durationSeconds) {
            
            System.out.println("🔥 UDP Load Test avviato:");
            System.out.println("   Target: " + host + ":" + port);
            System.out.println("   Thread: " + threadsCount);
            System.out.println("   Rate: " + packetsPerSecond + " pps");
            System.out.println("   Durata: " + durationSeconds + "s");
            System.out.println("-" .repeat(40));
            
            ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
            CountDownLatch startLatch = new CountDownLatch(threadsCount);
            CountDownLatch endLatch = new CountDownLatch(threadsCount);
            
            AtomicLong totalSent = new AtomicLong(0);
            AtomicLong totalReceived = new AtomicLong(0);
            AtomicLong totalLatency = new AtomicLong(0);
            
            int packetsPerThread = packetsPerSecond / threadsCount;
            long intervalNanos = TimeUnit.SECONDS.toNanos(1) / packetsPerThread;
            
            // Avvia thread di carico
            for (int i = 0; i < threadsCount; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try (DatagramSocket socket = new DatagramSocket()) {
                        InetAddress address = InetAddress.getByName(host);
                        socket.setSoTimeout(5000); // 5s timeout
                        
                        startLatch.countDown();
                        startLatch.await(); // Sync start
                        
                        long endTime = System.nanoTime() + TimeUnit.SECONDS.toNanos(durationSeconds);
                        long nextSendTime = System.nanoTime();
                        
                        while (System.nanoTime() < endTime) {
                            // Rate limiting preciso
                            long now = System.nanoTime();
                            if (now < nextSendTime) {
                                LockSupport.parkNanos(nextSendTime - now);
                            }
                            
                            // Invia PING con timestamp
                            long sendTime = System.nanoTime();
                            String message = "PING " + sendTime;
                            byte[] data = message.getBytes();
                            
                            DatagramPacket packet = new DatagramPacket(
                                data, data.length, address, port);
                            
                            socket.send(packet);
                            totalSent.incrementAndGet();
                            
                            // Ricevi PONG (opzionale per latency)
                            try {
                                byte[] buffer = new byte[1024];
                                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                                socket.receive(response);
                                
                                long receiveTime = System.nanoTime();
                                totalReceived.incrementAndGet();
                                totalLatency.addAndGet(receiveTime - sendTime);
                                
                            } catch (SocketTimeoutException e) {
                                // Ignora timeout per maintainre rate
                            }
                            
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
                
                System.out.println("\n📊 LOAD TEST COMPLETATO:");
                System.out.println("   Pacchetti inviati: " + totalSent.get());
                System.out.println("   Pacchetti ricevuti: " + totalReceived.get());
                
                double lossRate = (totalSent.get() - totalReceived.get()) * 100.0 / totalSent.get();
                System.out.println("   Packet loss: " + String.format("%.2f", lossRate) + "%");
                
                if (totalReceived.get() > 0) {
                    double avgLatency = (totalLatency.get() / (double) totalReceived.get()) / 1_000_000.0;
                    System.out.println("   Latenza media: " + String.format("%.2f", avgLatency) + " ms");
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                executor.shutdown();
            }
        }
    }
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("🚀 High Performance UDP Server");
            System.out.println("Utilizzo:");
            System.out.println("  java HighPerformanceUDPServer server <porta>");
            System.out.println("  java HighPerformanceUDPServer loadtest <host> <porta> <threads> <pps> <durata>");
            System.out.println();
            System.out.println("Comandi server: PING, ECHO <msg>, TIME, STATS, LOAD <iterations>, QUIT");
            System.out.println();
            System.out.println("Esempi:");
            System.out.println("  java HighPerformanceUDPServer server 8888");
            System.out.println("  java HighPerformanceUDPServer loadtest localhost 8888 4 1000 30");
            return;
        }
        
        String mode = args[0].toLowerCase();
        
        try {
            if ("server".equals(mode)) {
                if (args.length < 2) {
                    System.err.println("❌ Porta richiesta per il server");
                    return;
                }
                
                int port = Integer.parseInt(args[1]);
                Server server = new Server(port);
                
                // Shutdown hook
                Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
                
                server.start();
                
            } else if ("loadtest".equals(mode)) {
                if (args.length < 6) {
                    System.err.println("❌ Parametri insufficienti per load test");
                    System.err.println("Utilizzo: loadtest <host> <porta> <threads> <pps> <durata>");
                    return;
                }
                
                String host = args[1];
                int port = Integer.parseInt(args[2]);
                int threads = Integer.parseInt(args[3]);
                int pps = Integer.parseInt(args[4]);
                int duration = Integer.parseInt(args[5]);
                
                LoadTestClient.runLoadTest(host, port, threads, pps, duration);
                
            } else {
                System.err.println("❌ Modalità non riconosciuta: " + mode);
            }
            
        } catch (NumberFormatException e) {
            System.err.println("❌ Parametro numerico non valido");
        } catch (Exception e) {
            System.err.println("💥 Errore: " + e.getMessage());
            e.printStackTrace();
        }
    }
}