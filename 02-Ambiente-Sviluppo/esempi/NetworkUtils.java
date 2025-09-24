/**
 * Nome dell'Esempio: Network Utilities
 * Guida di Riferimento: 03-Strumenti-Debug-Networking.md
 * 
 * Obiettivo: Fornire utilities per il debugging e l'analisi di rete.
 * 
 * Spiegazione:
 * 1. Port scanner per verificare servizi attivi
 * 2. Latency tester per misurare tempi di risposta
 * 3. Bandwidth tester per verificare throughput
 * 
 * @author Socket Programming Course
 * @version 1.0
 */

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public class NetworkUtils {
    
    /**
     * Esegue un port scan su un range di porte
     * 
     * @param host Host da scannerizzare
     * @param startPort Porta iniziale
     * @param endPort Porta finale
     * @param timeout Timeout per ogni tentativo
     */
    public static void portScan(String host, int startPort, int endPort, int timeout) {
        System.out.println("🔍 Port Scan: " + host + " (porte " + startPort + "-" + endPort + ")");
        System.out.println("=" .repeat(60));
        
        ExecutorService executor = Executors.newFixedThreadPool(50);
        
        for (int port = startPort; port <= endPort; port++) {
            final int currentPort = port;
            
            executor.submit(() -> {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(host, currentPort), timeout);
                    System.out.println("✅ Porta " + currentPort + " APERTA");
                } catch (IOException e) {
                    // Porta chiusa - non stampiamo nulla per ridurre output
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("❌ Scan interrotto");
        }
    }
    
    /**
     * Testa la latenza verso un host
     * 
     * @param host Host da testare
     * @param port Porta da testare
     * @param attempts Numero di tentativi
     */
    public static void latencyTest(String host, int port, int attempts) {
        System.out.println("\n⏱️ Test Latenza: " + host + ":" + port);
        System.out.println("=" .repeat(40));
        
        long totalTime = 0;
        int successful = 0;
        
        for (int i = 1; i <= attempts; i++) {
            try {
                long startTime = System.currentTimeMillis();
                
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(host, port), 3000);
                }
                
                long endTime = System.currentTimeMillis();
                long latency = endTime - startTime;
                
                totalTime += latency;
                successful++;
                
                System.out.printf("Tentativo %d: %d ms%n", i, latency);
                
                Thread.sleep(1000); // Attesa tra i tentativi
                
            } catch (Exception e) {
                System.out.printf("Tentativo %d: FALLITO (%s)%n", i, e.getMessage());
            }
        }
        
        if (successful > 0) {
            double avgLatency = (double) totalTime / successful;
            System.out.printf("%nRisultati: %d/%d successi, latenza media: %.1f ms%n", 
                            successful, attempts, avgLatency);
        }
    }
    
    /**
     * Testa il throughput di una connessione
     * 
     * @param host Host server
     * @param port Porta server
     * @param dataSize Dimensione dati da trasferire (byte)
     */
    public static void throughputTest(String host, int port, int dataSize) {
        System.out.println("\n📈 Test Throughput: " + host + ":" + port);
        System.out.println("Trasferimento di " + (dataSize / 1024) + " KB");
        System.out.println("=" .repeat(40));
        
        try (Socket socket = new Socket(host, port);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {
            
            // Genera dati casuali
            byte[] data = new byte[dataSize];
            for (int i = 0; i < dataSize; i++) {
                data[i] = (byte) (i % 256);
            }
            
            // Test upload
            long startTime = System.currentTimeMillis();
            out.write(data);
            out.flush();
            long uploadTime = System.currentTimeMillis() - startTime;
            
            // Test download (se il server rimanda i dati)
            startTime = System.currentTimeMillis();
            byte[] received = new byte[dataSize];
            int totalRead = 0;
            while (totalRead < dataSize) {
                int read = in.read(received, totalRead, dataSize - totalRead);
                if (read == -1) break;
                totalRead += read;
            }
            long downloadTime = System.currentTimeMillis() - startTime;
            
            // Calcola throughput
            double uploadThroughput = (dataSize * 8.0) / (uploadTime / 1000.0) / 1000; // Kbps
            double downloadThroughput = (totalRead * 8.0) / (downloadTime / 1000.0) / 1000; // Kbps
            
            System.out.printf("Upload: %d byte in %d ms (%.1f Kbps)%n", 
                            dataSize, uploadTime, uploadThroughput);
            System.out.printf("Download: %d byte in %d ms (%.1f Kbps)%n", 
                            totalRead, downloadTime, downloadThroughput);
            
        } catch (Exception e) {
            System.err.println("❌ Test fallito: " + e.getMessage());
        }
    }
    
    /**
     * Mostra statistiche di connessione in tempo reale
     */
    public static void connectionMonitor(String host, int port, int duration) {
        System.out.println("\n📊 Monitor Connessione: " + host + ":" + port);
        System.out.println("Durata: " + duration + " secondi");
        System.out.println("=" .repeat(40));
        
        long endTime = System.currentTimeMillis() + (duration * 1000);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        
        while (System.currentTimeMillis() < endTime) {
            try (Socket socket = new Socket()) {
                long connectStart = System.currentTimeMillis();
                socket.connect(new InetSocketAddress(host, port), 2000);
                long connectTime = System.currentTimeMillis() - connectStart;
                
                String timestamp = LocalDateTime.now().format(formatter);
                System.out.printf("[%s] Connesso in %d ms%n", timestamp, connectTime);
                
                socket.close();
                Thread.sleep(2000); // Check ogni 2 secondi
                
            } catch (Exception e) {
                String timestamp = LocalDateTime.now().format(formatter);
                System.out.printf("[%s] Connessione fallita: %s%n", timestamp, e.getMessage());
                
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("🛠️ Network Utilities - Strumenti per Debug di Rete");
        System.out.println("=" .repeat(50));
        
        if (args.length == 0) {
            System.out.println("Utilizzo:");
            System.out.println("  java NetworkUtils scan <host> <start-port> <end-port>");
            System.out.println("  java NetworkUtils latency <host> <port> <attempts>");
            System.out.println("  java NetworkUtils throughput <host> <port> <size-kb>");
            System.out.println("  java NetworkUtils monitor <host> <port> <duration-sec>");
            System.out.println();
            System.out.println("Esempi:");
            System.out.println("  java NetworkUtils scan localhost 80 90");
            System.out.println("  java NetworkUtils latency google.com 80 5");
            System.out.println("  java NetworkUtils monitor localhost 8080 30");
            return;
        }
        
        String command = args[0];
        
        switch (command.toLowerCase()) {
            case "scan":
                if (args.length >= 4) {
                    portScan(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]), 1000);
                }
                break;
                
            case "latency":
                if (args.length >= 4) {
                    latencyTest(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                }
                break;
                
            case "throughput":
                if (args.length >= 4) {
                    int sizeKB = Integer.parseInt(args[3]);
                    throughputTest(args[1], Integer.parseInt(args[2]), sizeKB * 1024);
                }
                break;
                
            case "monitor":
                if (args.length >= 4) {
                    connectionMonitor(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                }
                break;
                
            default:
                System.err.println("❌ Comando non riconosciuto: " + command);
        }
    }
}