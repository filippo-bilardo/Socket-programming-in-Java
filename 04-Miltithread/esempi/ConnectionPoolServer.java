/**
 * Nome dell'Esempio: Connection Pool TCP Server
 * Guida di Riferimento: 03-Gestione-Connessioni-TCP.md
 * 
 * Obiettivo: Dimostrare pattern di connection pooling per scalabilità.
 * 
 * Spiegazione:
 * 1. ThreadPoolExecutor per gestione efficiente dei thread
 * 2. Limitazione delle connessioni concorrenti
 * 3. Monitoring delle risorse e statistiche
 * 
 * @author Socket Programming Course
 * @version 1.0
 */

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConnectionPoolServer {
    
    private final int port;
    private final int maxConnections;
    private final int coreThreads;
    private final int maxThreads;
    private final long keepAliveTime;
    
    private ServerSocket serverSocket;
    private ThreadPoolExecutor threadPool;
    private volatile boolean running = false;
    
    // Statistiche
    private final AtomicLong totalConnections = new AtomicLong(0);
    private final AtomicLong activeConnections = new AtomicLong(0);
    private final AtomicLong rejectedConnections = new AtomicLong(0);
    private final AtomicLong completedConnections = new AtomicLong(0);
    
    public ConnectionPoolServer(int port) {
        this(port, 50, 5, 20, 60000L); // Default: max 50 conn, 5-20 threads, 60s keep-alive
    }
    
    public ConnectionPoolServer(int port, int maxConnections, int coreThreads, 
                              int maxThreads, long keepAliveTime) {
        this.port = port;
        this.maxConnections = maxConnections;
        this.coreThreads = coreThreads;
        this.maxThreads = maxThreads;
        this.keepAliveTime = keepAliveTime;
    }
    
    public void start() throws IOException {
        System.out.println("🏊 Connection Pool Server");
        System.out.println("   Porta: " + port);
        System.out.println("   Max Connessioni: " + maxConnections);
        System.out.println("   Thread Pool: " + coreThreads + "-" + maxThreads + 
                          " (keep-alive: " + (keepAliveTime/1000) + "s)");
        System.out.println("🛑 Premi Ctrl+C per fermare");
        System.out.println("=" .repeat(50));
        
        // Configura thread pool
        threadPool = new ThreadPoolExecutor(
            coreThreads,
            maxThreads,
            keepAliveTime,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(maxConnections),
            new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "ClientHandler-" + counter.getAndIncrement());
                    t.setDaemon(false);
                    return t;
                }
            },
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    rejectedConnections.incrementAndGet();
                    System.err.println("🚫 Connessione rifiutata - pool pieno");
                    
                    // Chiude la connessione rifiutata
                    if (r instanceof ClientHandler) {
                        ClientHandler handler = (ClientHandler) r;
                        try {
                            handler.getSocket().close();
                        } catch (IOException e) {
                            // Ignora errori nella chiusura
                        }
                    }
                }
            }
        );
        
        // Shutdown hook per chiusura pulita
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        
        // Avvia thread per statistiche
        startStatsThread();
        
        serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
        running = true;
        
        System.out.println("✅ Server avviato e in ascolto...\n");
        
        try {
            while (running) {
                Socket clientSocket = serverSocket.accept();
                
                // Controlla limite connessioni
                if (activeConnections.get() >= maxConnections) {
                    System.err.println("🚫 Limite connessioni raggiunto, rifiuto client: " + 
                                     clientSocket.getRemoteSocketAddress());
                    clientSocket.close();
                    rejectedConnections.incrementAndGet();
                    continue;
                }
                
                totalConnections.incrementAndGet();
                activeConnections.incrementAndGet();
                
                System.out.println("🔗 Nuova connessione: " + clientSocket.getRemoteSocketAddress() + 
                                 " (attive: " + activeConnections.get() + ")");
                
                // Submette al thread pool
                ClientHandler handler = new ClientHandler(clientSocket);
                threadPool.execute(handler);
            }
            
        } catch (IOException e) {
            if (running) {
                System.err.println("💥 Errore nel server: " + e.getMessage());
            }
        } finally {
            shutdown();
        }
    }
    
    public void shutdown() {
        if (!running) return;
        
        running = false;
        
        System.out.println("\n🛑 Arresto server...");
        
        // Chiude server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Errore chiusura server socket: " + e.getMessage());
            }
        }
        
        // Arresta thread pool
        if (threadPool != null) {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                    System.out.println("⚠️ Forzata chiusura thread pool");
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        printFinalStats();
        System.out.println("✅ Server arrestato");
    }
    
    private void startStatsThread() {
        Thread statsThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(10000); // Ogni 10 secondi
                    if (running) {
                        printStats();
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "StatsThread");
        statsThread.setDaemon(true);
        statsThread.start();
    }
    
    private void printStats() {
        System.out.println("\n📊 STATISTICHE " + 
                          LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        System.out.println("   Connessioni attive: " + activeConnections.get());
        System.out.println("   Totale connessioni: " + totalConnections.get());
        System.out.println("   Completate: " + completedConnections.get());
        System.out.println("   Rifiutate: " + rejectedConnections.get());
        
        if (threadPool != null) {
            System.out.println("   Thread attivi: " + threadPool.getActiveCount());
            System.out.println("   Thread pool size: " + threadPool.getPoolSize());
            System.out.println("   Coda tasks: " + threadPool.getQueue().size());
        }
        System.out.println();
    }
    
    private void printFinalStats() {
        System.out.println("\n📈 STATISTICHE FINALI");
        System.out.println("   Totale connessioni gestite: " + totalConnections.get());
        System.out.println("   Connessioni completate: " + completedConnections.get());
        System.out.println("   Connessioni rifiutate: " + rejectedConnections.get());
        
        if (threadPool != null) {
            System.out.println("   Tasks completati: " + threadPool.getCompletedTaskCount());
        }
    }
    
    /**
     * Handler per singola connessione client
     */
    private class ClientHandler implements Runnable {
        private final Socket socket;
        private final String clientAddress;
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.clientAddress = socket.getRemoteSocketAddress().toString();
        }
        
        public Socket getSocket() {
            return socket;
        }
        
        @Override
        public void run() {
            System.out.println("🔧 Handler avviato per: " + clientAddress + 
                             " (thread: " + Thread.currentThread().getName() + ")");
            
            try {
                // Configura timeout
                socket.setSoTimeout(30000); // 30 secondi
                
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                
                // Messaggio di benvenuto
                out.println("🏊 Benvenuto nel Connection Pool Server!");
                out.println("Comandi disponibili: info, stats, echo <messaggio>, quit");
                out.print("> ");
                
                String line;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    
                    if (line.equalsIgnoreCase("quit")) {
                        out.println("👋 Arrivederci!");
                        break;
                    }
                    
                    if (line.equalsIgnoreCase("info")) {
                        out.println("ℹ️ Informazioni connessione:");
                        out.println("   Client: " + clientAddress);
                        out.println("   Thread: " + Thread.currentThread().getName());
                        out.println("   Timeout: " + socket.getSoTimeout() + "ms");
                        
                    } else if (line.equalsIgnoreCase("stats")) {
                        out.println("📊 Statistiche server:");
                        out.println("   Connessioni attive: " + activeConnections.get());
                        out.println("   Totale connessioni: " + totalConnections.get());
                        out.println("   Thread pool attivi: " + threadPool.getActiveCount());
                        
                    } else if (line.toLowerCase().startsWith("echo ")) {
                        String message = line.substring(5);
                        out.println("📢 Echo: " + message);
                        
                    } else if (line.isEmpty()) {
                        // Ignora righe vuote
                        
                    } else {
                        out.println("❓ Comando non riconosciuto: " + line);
                    }
                    
                    out.print("> ");
                }
                
            } catch (SocketTimeoutException e) {
                System.out.println("⏰ Timeout per: " + clientAddress);
            } catch (IOException e) {
                System.out.println("💥 Errore I/O per " + clientAddress + ": " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignora errori nella chiusura
                }
                
                activeConnections.decrementAndGet();
                completedConnections.incrementAndGet();
                
                System.out.println("🔌 Connessione chiusa: " + clientAddress + 
                                 " (attive: " + activeConnections.get() + ")");
            }
        }
    }
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("🏊 Connection Pool TCP Server");
            System.out.println("Utilizzo: java ConnectionPoolServer <porta> [max-conn] [core-threads] [max-threads]");
            System.out.println();
            System.out.println("Parametri:");
            System.out.println("  porta        - Porta di ascolto");
            System.out.println("  max-conn     - Max connessioni concorrenti (default: 50)");
            System.out.println("  core-threads - Thread core nel pool (default: 5)");
            System.out.println("  max-threads  - Thread massimi nel pool (default: 20)");
            System.out.println();
            System.out.println("Esempio: java ConnectionPoolServer 8080 100 10 30");
            return;
        }
        
        try {
            int port = Integer.parseInt(args[0]);
            
            ConnectionPoolServer server;
            
            if (args.length >= 4) {
                int maxConn = Integer.parseInt(args[1]);
                int coreThreads = Integer.parseInt(args[2]);
                int maxThreads = Integer.parseInt(args[3]);
                server = new ConnectionPoolServer(port, maxConn, coreThreads, maxThreads, 60000L);
            } else {
                server = new ConnectionPoolServer(port);
            }
            
            server.start();
            
        } catch (NumberFormatException e) {
            System.err.println("❌ Parametri numerici non validi");
        } catch (IOException e) {
            System.err.println("💥 Errore avvio server: " + e.getMessage());
        }
    }
}