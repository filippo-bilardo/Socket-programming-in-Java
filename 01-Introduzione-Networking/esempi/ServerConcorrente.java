/**
 * Nome dell'Esempio: Server Concorrente
 * Guida di Riferimento: 03-Architetture-Client-Server.md
 * 
 * Obiettivo: Implementare un server echo concorrente che gestisce più client simultaneamente.
 * 
 * Spiegazione:
 * 1. Crea un ServerSocket in ascolto su una porta
 * 2. Per ogni client connesso, crea un thread dedicato
 * 3. Ogni thread gestisce un client indipendentemente
 * 4. Usa ExecutorService per limitare il numero di thread
 * 
 * @author Socket Programming Course
 * @version 1.0
 */

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerConcorrente {
    private static final int PORT = 8081;
    private static final int MAX_THREADS = 10;
    private static final String EXIT_COMMAND = "quit";
    
    // Contatore thread-safe per i client
    private static final AtomicInteger clientCounter = new AtomicInteger(0);
    
    /**
     * Task per gestire un singolo client in un thread separato
     */
    static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final int clientId;
        
        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            this.clientId = clientCounter.incrementAndGet();
        }
        
        @Override
        public void run() {
            String clientAddress = clientSocket.getRemoteSocketAddress().toString();
            String threadName = Thread.currentThread().getName();
            
            System.out.println("📞 Client #" + clientId + " connesso da: " + clientAddress + 
                             " [Thread: " + threadName + "]");
            
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                // Invia messaggio di benvenuto
                out.println("🎉 Benvenuto nel Server Echo Concorrente! (Client #" + clientId + ")");
                out.println("🧵 Gestito dal thread: " + threadName);
                out.println("💡 Digita 'quit' per disconnetterti");
                
                String inputLine;
                int messageCount = 0;
                
                // Loop di comunicazione con il client
                while ((inputLine = in.readLine()) != null) {
                    messageCount++;
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    
                    System.out.println("[" + timestamp + "] Client #" + clientId + 
                                     " [" + threadName + "] → " + inputLine);
                    
                    // Controlla comando di uscita
                    if (EXIT_COMMAND.equalsIgnoreCase(inputLine.trim())) {
                        out.println("👋 Arrivederci! Hai inviato " + (messageCount - 1) + " messaggi.");
                        break;
                    }
                    
                    // Simula elaborazione (per dimostrare la concorrenza)
                    if (inputLine.trim().toLowerCase().startsWith("slow")) {
                        try {
                            Thread.sleep(3000); // 3 secondi di ritardo
                            out.println("🐌 Elaborazione lenta completata per: " + inputLine);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    } else {
                        // Echo normale del messaggio
                        String response = String.format("📡 Echo #%d [%s] [%s]: %s", 
                                                       messageCount, timestamp, threadName, inputLine);
                        out.println(response);
                    }
                }
                
            } catch (IOException e) {
                System.err.println("❌ Errore nella gestione del client #" + clientId + ": " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                    System.out.println("📞 Client #" + clientId + " disconnesso [" + threadName + "]");
                } catch (IOException e) {
                    System.err.println("❌ Errore nella chiusura del socket: " + e.getMessage());
                }
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("🚀 Avvio Server Echo Concorrente");
        System.out.println("👂 In ascolto sulla porta " + PORT);
        System.out.println("🧵 Massimo " + MAX_THREADS + " thread simultanei");
        System.out.println("⚡ Gestione concorrente: più client simultaneamente");
        System.out.println("💡 Prova a scrivere 'slow messaggio' per simulare elaborazione lenta");
        System.out.println("🛑 Premi Ctrl+C per fermare il server");
        System.out.println("=" .repeat(60));
        
        // Crea il pool di thread
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            
            // Shutdown hook per chiudere gracefully
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n🛑 Spegnimento server in corso...");
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                }
                System.out.println("✅ Server spento");
            }));
            
            // Loop principale del server
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    System.out.println("⏳ In attesa di connessioni... (Client attivi: " + 
                                     (clientCounter.get() - getCompletedTasks(executor)) + ")");
                    
                    // Accetta una connessione (operazione bloccante)
                    Socket clientSocket = serverSocket.accept();
                    
                    // Sottomette il task al pool di thread
                    executor.submit(new ClientHandler(clientSocket));
                    
                } catch (IOException e) {
                    if (!Thread.currentThread().isInterrupted()) {
                        System.err.println("❌ Errore nell'accettare connessione: " + e.getMessage());
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("💥 Impossibile avviare il server: " + e.getMessage());
            System.err.println("💡 Assicurati che la porta " + PORT + " sia disponibile");
        } finally {
            executor.shutdown();
        }
    }
    
    /**
     * Helper per ottenere il numero di task completati (approssimativo)
     */
    private static long getCompletedTasks(ExecutorService executor) {
        if (executor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) executor).getCompletedTaskCount();
        }
        return 0;
    }
}