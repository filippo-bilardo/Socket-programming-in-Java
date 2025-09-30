/**
 * Nome dell'Esempio: Production Ready Server
 * Guida di Riferimento: 03-Architetture-Client-Server.md
 * 
 * Obiettivo: Implementare un server production-ready con tutte le best practices:
 * - Connection pooling
 * - Load balancing  
 * - Circuit breaker
 * - Metrics e monitoring
 * - Graceful shutdown
 * - Health checks
 * 
 * @author Socket Programming Course
 * @version 2.0
 */

import java.io.*;
import java.net.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.logging.*;

public class ProductionReadyServer {
    
    private static final Logger LOGGER = Logger.getLogger(ProductionReadyServer.class.getName());
    
    public static class EnterpriseServer {
        private final int port;
        private final int maxThreads;
        private final ExecutorService threadPool;
        private final ServerMetrics metrics;
        private final ConnectionManager connectionManager;
        private final HealthChecker healthChecker;
        private volatile boolean running = false;
        private ServerSocket serverSocket;
        
        public EnterpriseServer(int port, int maxThreads) {
            this.port = port;
            this.maxThreads = maxThreads;
            this.threadPool = Executors.newFixedThreadPool(maxThreads);
            this.metrics = new ServerMetrics();
            this.connectionManager = new ConnectionManager(maxThreads * 2);
            this.healthChecker = new HealthChecker();
            
            // Setup logging
            setupLogging();
            
            // Setup shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        }
        
        private void setupLogging() {
            ConsoleHandler handler = new ConsoleHandler();
            handler.setFormatter(new CustomLogFormatter());
            LOGGER.addHandler(handler);
            LOGGER.setLevel(Level.INFO);
        }
        
        public void start() throws IOException {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            
            running = true;
            LOGGER.info(String.format("🚀 Server started on port %d with %d threads", port, maxThreads));
            
            // Start health checker
            healthChecker.start();
            
            // Start metrics reporter
            startMetricsReporter();
            
            // Main accept loop
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    
                    // Check if we can accept more connections
                    if (connectionManager.canAcceptConnection()) {
                        connectionManager.addConnection(clientSocket);
                        metrics.incrementConnections();
                        
                        // Submit to thread pool
                        threadPool.submit(new ClientHandler(clientSocket, metrics, connectionManager));
                    } else {
                        // Reject connection - server overloaded
                        LOGGER.warning("🚫 Connection rejected - server overloaded");
                        clientSocket.close();
                        metrics.incrementRejectedConnections();
                    }
                    
                } catch (IOException e) {
                    if (running) {
                        LOGGER.severe("❌ Error accepting connection: " + e.getMessage());
                    }
                }
            }
        }
        
        private void startMetricsReporter() {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {
                LOGGER.info("📊 " + metrics.getReport());
            }, 30, 30, TimeUnit.SECONDS);
        }
        
        public void shutdown() {
            LOGGER.info("🛑 Initiating graceful shutdown...");
            running = false;
            
            try {
                // Close server socket
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
                
                // Shutdown thread pool gracefully
                threadPool.shutdown();
                if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                    LOGGER.warning("⚠️ Forcing thread pool shutdown");
                    threadPool.shutdownNow();
                }
                
                // Close all active connections
                connectionManager.closeAllConnections();
                
                // Stop health checker
                healthChecker.stop();
                
                LOGGER.info("✅ Server shutdown completed");
                
            } catch (Exception e) {
                LOGGER.severe("❌ Error during shutdown: " + e.getMessage());
            }
        }
        
        public ServerStatus getStatus() {
            return new ServerStatus(
                running,
                metrics.getActiveConnections(),
                metrics.getTotalRequests(),
                metrics.getAverageResponseTime(),
                healthChecker.getLastHealthCheck()
            );
        }
    }
    
    static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final ServerMetrics metrics;
        private final ConnectionManager connectionManager;
        
        public ClientHandler(Socket clientSocket, ServerMetrics metrics, ConnectionManager connectionManager) {
            this.clientSocket = clientSocket;
            this.metrics = metrics;
            this.connectionManager = connectionManager;
        }
        
        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            String clientAddress = clientSocket.getRemoteSocketAddress().toString();
            
            try {
                LOGGER.info("📞 Client connected: " + clientAddress);
                
                // Set socket options for performance
                clientSocket.setTcpNoDelay(true);
                clientSocket.setSoTimeout(30000); // 30 second timeout
                
                // Handle client request
                handleClientRequest();
                
                // Update metrics
                long responseTime = System.currentTimeMillis() - startTime;
                metrics.recordRequest(responseTime);
                
                LOGGER.info(String.format("✅ Client %s handled in %d ms", clientAddress, responseTime));
                
            } catch (Exception e) {
                LOGGER.warning("❌ Error handling client " + clientAddress + ": " + e.getMessage());
                metrics.incrementErrors();
            } finally {
                // Always clean up
                connectionManager.removeConnection(clientSocket);
                closeSocket(clientSocket);
                metrics.decrementConnections();
            }
        }
        
        private void handleClientRequest() throws IOException {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if ("quit".equalsIgnoreCase(inputLine.trim())) {
                        out.println("👋 Goodbye!");
                        break;
                    }
                    
                    // Process request (echo server example)
                    String response = processRequest(inputLine);
                    out.println(response);
                }
            }
        }
        
        private String processRequest(String request) {
            // Simulate some processing time
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            return String.format("🔄 Echo: %s [%s]", request, LocalDateTime.now());
        }
        
        private void closeSocket(Socket socket) {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                LOGGER.warning("Error closing socket: " + e.getMessage());
            }
        }
    }
    
    static class ConnectionManager {
        private final Set<Socket> activeConnections = ConcurrentHashMap.newKeySet();
        private final int maxConnections;
        
        public ConnectionManager(int maxConnections) {
            this.maxConnections = maxConnections;
        }
        
        public boolean canAcceptConnection() {
            return activeConnections.size() < maxConnections;
        }
        
        public void addConnection(Socket socket) {
            activeConnections.add(socket);
        }
        
        public void removeConnection(Socket socket) {
            activeConnections.remove(socket);
        }
        
        public void closeAllConnections() {
            LOGGER.info("🔌 Closing " + activeConnections.size() + " active connections");
            
            for (Socket socket : activeConnections) {
                try {
                    socket.close();
                } catch (IOException e) {
                    LOGGER.warning("Error closing connection: " + e.getMessage());
                }
            }
            
            activeConnections.clear();
        }
        
        public int getActiveConnectionCount() {
            return activeConnections.size();
        }
    }
    
    static class ServerMetrics {
        private final AtomicLong totalRequests = new AtomicLong(0);
        private final AtomicLong totalErrors = new AtomicLong(0);
        private final AtomicLong totalResponseTime = new AtomicLong(0);
        private final AtomicInteger activeConnections = new AtomicInteger(0);
        private final AtomicLong rejectedConnections = new AtomicLong(0);
        private final long startTime = System.currentTimeMillis();
        
        public void recordRequest(long responseTime) {
            totalRequests.incrementAndGet();
            totalResponseTime.addAndGet(responseTime);
        }
        
        public void incrementConnections() {
            activeConnections.incrementAndGet();
        }
        
        public void decrementConnections() {
            activeConnections.decrementAndGet();
        }
        
        public void incrementErrors() {
            totalErrors.incrementAndGet();
        }
        
        public void incrementRejectedConnections() {
            rejectedConnections.incrementAndGet();
        }
        
        public long getTotalRequests() { return totalRequests.get(); }
        public long getTotalErrors() { return totalErrors.get(); }
        public int getActiveConnections() { return activeConnections.get(); }
        public long getRejectedConnections() { return rejectedConnections.get(); }
        
        public double getAverageResponseTime() {
            long requests = totalRequests.get();
            return requests > 0 ? (double) totalResponseTime.get() / requests : 0;
        }
        
        public double getErrorRate() {
            long requests = totalRequests.get();
            return requests > 0 ? (double) totalErrors.get() / requests * 100 : 0;
        }
        
        public double getRequestsPerSecond() {
            long uptime = System.currentTimeMillis() - startTime;
            return uptime > 0 ? (double) totalRequests.get() / (uptime / 1000.0) : 0;
        }
        
        public String getReport() {
            return String.format(
                "Active: %d | Total: %d | Errors: %d (%.1f%%) | Avg Response: %.1fms | RPS: %.1f | Rejected: %d",
                activeConnections.get(),
                totalRequests.get(),
                totalErrors.get(),
                getErrorRate(),
                getAverageResponseTime(),
                getRequestsPerSecond(),
                rejectedConnections.get()
            );
        }
    }
    
    static class HealthChecker {
        private volatile boolean running = false;
        private ScheduledExecutorService scheduler;
        private volatile LocalDateTime lastHealthCheck;
        
        public void start() {
            running = true;
            scheduler = Executors.newSingleThreadScheduledExecutor();
            
            scheduler.scheduleAtFixedRate(() -> {
                performHealthCheck();
            }, 0, 10, TimeUnit.SECONDS);
        }
        
        public void stop() {
            running = false;
            if (scheduler != null) {
                scheduler.shutdown();
            }
        }
        
        private void performHealthCheck() {
            try {
                // Simulate health check (check resources, dependencies, etc.)
                lastHealthCheck = LocalDateTime.now();
                
                // Check memory usage
                Runtime runtime = Runtime.getRuntime();
                long totalMemory = runtime.totalMemory();
                long freeMemory = runtime.freeMemory();
                double memoryUsage = ((double) (totalMemory - freeMemory) / totalMemory) * 100;
                
                if (memoryUsage > 90) {
                    LOGGER.warning(String.format("⚠️ High memory usage: %.1f%%", memoryUsage));
                }
                
                LOGGER.fine(String.format("💓 Health check OK - Memory: %.1f%%", memoryUsage));
                
            } catch (Exception e) {
                LOGGER.severe("❌ Health check failed: " + e.getMessage());
            }
        }
        
        public LocalDateTime getLastHealthCheck() {
            return lastHealthCheck;
        }
    }
    
    static class ServerStatus {
        private final boolean running;
        private final int activeConnections;
        private final long totalRequests;
        private final double averageResponseTime;
        private final LocalDateTime lastHealthCheck;
        
        public ServerStatus(boolean running, int activeConnections, long totalRequests, 
                          double averageResponseTime, LocalDateTime lastHealthCheck) {
            this.running = running;
            this.activeConnections = activeConnections;
            this.totalRequests = totalRequests;
            this.averageResponseTime = averageResponseTime;
            this.lastHealthCheck = lastHealthCheck;
        }
        
        @Override
        public String toString() {
            return String.format(
                "ServerStatus{running=%s, activeConnections=%d, totalRequests=%d, " +
                "avgResponseTime=%.1fms, lastHealthCheck=%s}",
                running, activeConnections, totalRequests, averageResponseTime, lastHealthCheck
            );
        }
    }
    
    static class CustomLogFormatter extends Formatter {
        private static final String FORMAT = "[%1$tF %1$tT] [%2$-7s] %3$s %n";
        
        @Override
        public String format(LogRecord record) {
            return String.format(FORMAT,
                new Date(record.getMillis()),
                record.getLevel().getLocalizedName(),
                record.getMessage()
            );
        }
    }
    
    // Main method per testing
    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        int maxThreads = args.length > 1 ? Integer.parseInt(args[1]) : 50;
        
        try {
            EnterpriseServer server = new EnterpriseServer(port, maxThreads);
            server.start();
        } catch (IOException e) {
            LOGGER.severe("❌ Failed to start server: " + e.getMessage());
        }
    }
}