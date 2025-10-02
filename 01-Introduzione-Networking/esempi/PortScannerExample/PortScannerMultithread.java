/**
 * Nome dell'Esempio: Port Scanner Multithread
 * Guida di Riferimento: 02-Introduzione-ai-Socket.md
 * 
 * Obiettivo: Scansionare tutte le porte aperte di un host usando multithreading.
 * 
 * Spiegazione:
 * 1. Accetta host (IP o nome) da riga di comando
 * 2. Utilizza un ThreadPoolExecutor per gestire le connessioni concorrenti
 * 3. Scansiona tutte le porte (1-65535) o un range specificato
 * 4. Mostra risultati in tempo reale e statistiche finali
 * 
 * Utilizzo:
 * java PortScannerMultithread <host> [porta_inizio] [porta_fine] [num_thread]
 * 
 * Esempi:
 * java PortScannerMultithread google.com
 * java PortScannerMultithread 192.168.1.1 1 1000 50
 * java PortScannerMultithread localhost 8000 9000
 * 
 * @author Socket Programming Course
 * @version 1.0
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PortScannerMultithread {
    
    // Configurazioni di default
    private static final int DEFAULT_TIMEOUT = 1000; // 1 secondo
    private static final int DEFAULT_THREAD_COUNT = 100; // Thread simultanei
    private static final int DEFAULT_START_PORT = 1;
    private static final int DEFAULT_END_PORT = 65535;
    
    // Statistiche
    private static final AtomicInteger portsScanned = new AtomicInteger(0);
    private static final AtomicInteger portsOpen = new AtomicInteger(0);
    private static final AtomicInteger portsClosed = new AtomicInteger(0);
    private static final List<Integer> openPorts = Collections.synchronizedList(new ArrayList<>());
    
    /**
     * Task per testare una singola porta
     */
    static class PortScanTask implements Callable<Boolean> {
        private final String host;
        private final int port;
        private final int timeout;
        
        public PortScanTask(String host, int port, int timeout) {
            this.host = host;
            this.port = port;
            this.timeout = timeout;
        }
        
        @Override
        public Boolean call() {
            boolean isOpen = testPort(host, port, timeout);
            
            portsScanned.incrementAndGet();
            
            if (isOpen) {
                portsOpen.incrementAndGet();
                openPorts.add(port);
                
                // Determina il servizio comune per la porta
                String service = getCommonService(port);
                String serviceInfo = service != null ? " (" + service + ")" : "";
                
                System.out.printf("✅ APERTA: %s:%d%s%n", host, port, serviceInfo);
            } else {
                portsClosed.incrementAndGet();
            }
            
            return isOpen;
        }
        
        /**
         * Testa una singola porta
         */
        private boolean testPort(String host, int port, int timeout) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), timeout);
                return true;
            } catch (SocketTimeoutException e) {
                // Timeout - porta filtrata o chiusa
                return false;
            } catch (IOException e) {
                // Connessione rifiutata o altri errori
                return false;
            }
        }
        
        /**
         * Restituisce il servizio comune per una porta nota
         */
        private String getCommonService(int port) {
            switch (port) {
                case 21: return "FTP";
                case 22: return "SSH";
                case 23: return "Telnet";
                case 25: return "SMTP";
                case 53: return "DNS";
                case 80: return "HTTP";
                case 110: return "POP3";
                case 143: return "IMAP";
                case 443: return "HTTPS";
                case 993: return "IMAPS";
                case 995: return "POP3S";
                case 3306: return "MySQL";
                case 5432: return "PostgreSQL";
                case 6379: return "Redis";
                case 8080: return "HTTP Alt";
                case 8443: return "HTTPS Alt";
                default: return null;
            }
        }
    }
    
    /**
     * Thread per mostrare progress in tempo reale
     */
    static class ProgressReporter implements Runnable {
        private final int totalPorts;
        private volatile boolean running = true;
        
        public ProgressReporter(int totalPorts) {
            this.totalPorts = totalPorts;
        }
        
        public void stop() {
            running = false;
        }
        
        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(2000); // Aggiorna ogni 2 secondi
                    
                    int scanned = portsScanned.get();
                    int open = portsOpen.get();
                    double progress = (scanned * 100.0) / totalPorts;
                    
                    System.out.printf("📊 Progress: %.1f%% (%d/%d) - Aperte: %d%n", 
                        progress, scanned, totalPorts, open);
                        
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    
    /**
     * Valida se l'host è raggiungibile
     */
    public static boolean validateHost(String host) {
        try {
            // Test di raggiungibilità base
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, 80), 3000);
            socket.close();
            return true;
        } catch (IOException e) {
            // Prova con una porta alternativa comune
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(host, 22), 3000);
                socket.close();
                return true;
            } catch (IOException e2) {
                System.err.println("⚠️  Host potrebbe non essere raggiungibile: " + host);
                System.err.println("   Continuando comunque la scansione...");
                return true; // Continuiamo comunque
            }
        }
    }
    
    /**
     * Esegue la scansione delle porte
     */
    public static void scanPorts(String host, int startPort, int endPort, int threadCount) {
        System.out.println("🔍 Avvio scansione porte");
        System.out.println("=" .repeat(50));
        System.out.printf("🎯 Target: %s%n", host);
        System.out.printf("📊 Range porte: %d-%d (%d porte totali)%n", 
            startPort, endPort, (endPort - startPort + 1));
        System.out.printf("🧵 Thread concorrenti: %d%n", threadCount);
        System.out.printf("⏱️  Timeout per porta: %d ms%n", DEFAULT_TIMEOUT);
        System.out.println("=" .repeat(50));
        
        // Valida host
        validateHost(host);
        
        long startTime = System.currentTimeMillis();
        
        // Crea thread pool
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        // Avvia reporter progress
        int totalPorts = endPort - startPort + 1;
        ProgressReporter reporter = new ProgressReporter(totalPorts);
        Thread reporterThread = new Thread(reporter);
        reporterThread.start();
        
        try {
            // Crea e sottometti tutti i task
            List<Future<Boolean>> futures = new ArrayList<>();
            
            for (int port = startPort; port <= endPort; port++) {
                PortScanTask task = new PortScanTask(host, port, DEFAULT_TIMEOUT);
                Future<Boolean> future = executor.submit(task);
                futures.add(future);
            }
            
            // Aspetta completamento di tutti i task
            for (Future<Boolean> future : futures) {
                try {
                    future.get(); // Attende risultato
                } catch (ExecutionException e) {
                    System.err.println("Errore durante scansione: " + e.getCause());
                }
            }
            
        } catch (InterruptedException e) {
            System.err.println("Scansione interrotta");
            Thread.currentThread().interrupt();
        } finally {
            // Ferma reporter e shutdown executor
            reporter.stop();
            reporterThread.interrupt();
            
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        long endTime = System.currentTimeMillis();
        double durationSeconds = (endTime - startTime) / 1000.0;
        
        // Mostra risultati finali
        showFinalResults(host, startPort, endPort, durationSeconds);
    }
    
    /**
     * Mostra i risultati finali della scansione
     */
    private static void showFinalResults(String host, int startPort, int endPort, double duration) {
        System.out.println("\n" + "=" .repeat(60));
        System.out.println("📋 RISULTATI SCANSIONE COMPLETA");
        System.out.println("=" .repeat(60));
        
        System.out.printf("🎯 Host: %s%n", host);
        System.out.printf("📊 Range: %d-%d%n", startPort, endPort);
        System.out.printf("⏱️  Durata: %.2f secondi%n", duration);
        System.out.printf("🔢 Porte totali: %d%n", portsScanned.get());
        System.out.printf("✅ Porte aperte: %d%n", portsOpen.get());
        System.out.printf("❌ Porte chiuse: %d%n", portsClosed.get());
        
        if (portsOpen.get() > 0) {
            System.out.println("\n🔓 PORTE APERTE TROVATE:");
            System.out.println("-" .repeat(30));
            
            // Ordina le porte aperte
            openPorts.sort(Integer::compareTo);
            
            for (int port : openPorts) {
                String service = getServiceDescription(port);
                System.out.printf("  %d%s%n", port, service);
            }
        } else {
            System.out.println("\n🔒 Nessuna porta aperta trovata");
        }
        
        // Statistiche performance
        double portsPerSecond = portsScanned.get() / duration;
        System.out.printf("\n⚡ Performance: %.1f porte/secondo%n", portsPerSecond);
        System.out.println("=" .repeat(60));
    }
    
    /**
     * Restituisce descrizione servizio per porta nota
     */
    private static String getServiceDescription(int port) {
        String service = null;
        switch (port) {
            case 21: service = "FTP"; break;
            case 22: service = "SSH"; break;
            case 23: service = "Telnet"; break;
            case 25: service = "SMTP"; break;
            case 53: service = "DNS"; break;
            case 80: service = "HTTP"; break;
            case 110: service = "POP3"; break;
            case 143: service = "IMAP"; break;
            case 443: service = "HTTPS"; break;
            case 993: service = "IMAPS"; break;
            case 995: service = "POP3S"; break;
            case 3306: service = "MySQL"; break;
            case 5432: service = "PostgreSQL"; break;
            case 6379: service = "Redis"; break;
            case 8080: service = "HTTP Alt"; break;
            case 8443: service = "HTTPS Alt"; break;
        }
        return service != null ? " (" + service + ")" : "";
    }
    
    /**
     * Mostra l'utilizzo del programma
     */
    private static void showUsage() {
        System.out.println("🔍 PORT SCANNER MULTITHREAD");
        System.out.println("=" .repeat(50));
        System.out.println("Utilizzo:");
        System.out.println("  java PortScannerMultithread <host> [porta_inizio] [porta_fine] [num_thread]");
        System.out.println();
        System.out.println("Parametri:");
        System.out.println("  host         - IP o nome host da scansionare (obbligatorio)");
        System.out.println("  porta_inizio - Porta iniziale (default: 1)");
        System.out.println("  porta_fine   - Porta finale (default: 65535)");
        System.out.println("  num_thread   - Numero thread (default: 100)");
        System.out.println();
        System.out.println("Esempi:");
        System.out.println("  java PortScannerMultithread google.com");
        System.out.println("  java PortScannerMultithread 192.168.1.1 1 1000");
        System.out.println("  java PortScannerMultithread localhost 8000 9000 50");
        System.out.println("  java PortScannerMultithread scanme.nmap.org 1 1000 200");
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            showUsage();
            System.exit(1);
        }
        
        try {
            String host = args[0];
            int startPort = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_START_PORT;
            int endPort = args.length > 2 ? Integer.parseInt(args[2]) : DEFAULT_END_PORT;
            int threadCount = args.length > 3 ? Integer.parseInt(args[3]) : DEFAULT_THREAD_COUNT;
            
            // Validazione parametri
            if (startPort < 1 || startPort > 65535) {
                throw new IllegalArgumentException("Porta iniziale deve essere tra 1 e 65535");
            }
            if (endPort < 1 || endPort > 65535) {
                throw new IllegalArgumentException("Porta finale deve essere tra 1 e 65535");
            }
            if (startPort > endPort) {
                throw new IllegalArgumentException("Porta iniziale deve essere <= porta finale");
            }
            if (threadCount < 1 || threadCount > 1000) {
                throw new IllegalArgumentException("Numero thread deve essere tra 1 e 1000");
            }
            
            // Avvia scansione
            scanPorts(host, startPort, endPort, threadCount);
            
        } catch (NumberFormatException e) {
            System.err.println("❌ Errore: Parametri numerici non validi");
            showUsage();
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Errore: " + e.getMessage());
            showUsage();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("❌ Errore imprevisto: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}