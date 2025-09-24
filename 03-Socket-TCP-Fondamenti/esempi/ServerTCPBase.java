/**
 * Nome dell'Esempio: Server TCP Base
 * Guida di Riferimento: 01-Creazione-Socket-TCP.md
 * 
 * Obiettivo: Implementare un server TCP configurabile e robusto.
 * 
 * Spiegazione:
 * 1. ServerSocket con configurazioni ottimizzate
 * 2. Gestione connessioni client con timeout
 * 3. Logging dettagliato e gestione errori specifica
 * 
 * @author Socket Programming Course
 * @version 1.0
 */

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerTCPBase {
    private static final int DEFAULT_PORT = 8080;
    private static final int DEFAULT_BACKLOG = 50;
    private static final int CLIENT_TIMEOUT = 30000; // 30 secondi
    
    private final int port;
    private final int backlog;
    private ServerSocket serverSocket;
    private volatile boolean running = false;
    private final AtomicInteger clientCounter = new AtomicInteger(0);
    
    public ServerTCPBase(int port, int backlog) {
        this.port = port;
        this.backlog = backlog;
    }
    
    public ServerTCPBase(int port) {
        this(port, DEFAULT_BACKLOG);
    }
    
    /**
     * Avvia il server con configurazioni ottimizzate
     */
    public void start() throws IOException {
        System.out.println("🚀 Avvio Server TCP Base");
        System.out.println("📍 Porta: " + port);
        System.out.println("📋 Backlog: " + backlog);
        System.out.println("⏰ Timeout client: " + CLIENT_TIMEOUT + "ms");
        
        try {
            // Crea ServerSocket con configurazioni
            serverSocket = new ServerSocket();
            
            // Configurazioni pre-bind
            serverSocket.setReuseAddress(true);           // Riusa indirizzo
            serverSocket.setReceiveBufferSize(128 * 1024); // Buffer 128KB
            
            System.out.println("⚙️ Configurazioni ServerSocket:");
            System.out.println("   - Reuse Address: " + serverSocket.getReuseAddress());
            System.out.println("   - Receive Buffer: " + serverSocket.getReceiveBufferSize() + " byte");
            
            // Bind e listen
            InetSocketAddress address = new InetSocketAddress(port);
            serverSocket.bind(address, backlog);
            
            System.out.println("✅ Server avviato su " + serverSocket.getLocalSocketAddress());
            System.out.println("👂 In ascolto per connessioni...");
            System.out.println("🛑 Premi Ctrl+C per fermare il server");
            System.out.println("=" .repeat(60));
            
            running = true;
            
            // Loop principale di accettazione
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    int clientId = clientCounter.incrementAndGet();
                    
                    System.out.println("🔗 [" + getCurrentTimestamp() + "] " +
                                     "Client #" + clientId + " connesso da: " + 
                                     clientSocket.getRemoteSocketAddress());
                    
                    // Configura socket client
                    configureClientSocket(clientSocket, clientId);
                    
                    // Gestisce client (modalità semplice - un client alla volta per questo esempio)
                    handleClient(clientSocket, clientId);
                    
                } catch (SocketTimeoutException e) {
                    // Timeout accept - normale per permettere controllo running
                    continue;
                } catch (IOException e) {
                    if (running) {
                        System.err.println("❌ Errore accettazione connessione: " + e.getMessage());
                    }
                }
            }
            
        } catch (BindException e) {
            throw new BindException("Porta " + port + " già in uso. " +
                                  "Verifica che non ci siano altri processi sulla porta.");
        } catch (IOException e) {
            throw new IOException("Errore avvio server: " + e.getMessage(), e);
        }
    }
    
    /**
     * Configura le opzioni del socket client
     */
    private void configureClientSocket(Socket clientSocket, int clientId) throws IOException {
        clientSocket.setTcpNoDelay(true);              // Bassa latenza
        clientSocket.setKeepAlive(true);               // Keep-alive TCP
        clientSocket.setSoTimeout(CLIENT_TIMEOUT);     // Timeout lettura
        clientSocket.setSendBufferSize(64 * 1024);     // Buffer invio
        clientSocket.setReceiveBufferSize(64 * 1024);  // Buffer ricezione
        
        System.out.println("⚙️ Client #" + clientId + " configurato:");
        System.out.println("   - TCP NoDelay: " + clientSocket.getTcpNoDelay());
        System.out.println("   - Keep Alive: " + clientSocket.getKeepAlive());
        System.out.println("   - SO Timeout: " + clientSocket.getSoTimeout() + "ms");
    }
    
    /**
     * Gestisce un singolo client
     */
    private void handleClient(Socket clientSocket, int clientId) {
        String clientAddr = clientSocket.getRemoteSocketAddress().toString();
        
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            // Messaggio di benvenuto
            out.println("🎉 Benvenuto nel Server TCP Base!");
            out.println("🆔 Sei il client #" + clientId);
            out.println("🕐 Timestamp connessione: " + getCurrentTimestamp());
            out.println("💡 Digita 'info' per informazioni, 'quit' per disconnetterti");
            
            String inputLine;
            int messageCount = 0;
            
            while ((inputLine = in.readLine()) != null) {
                messageCount++;
                String timestamp = getCurrentTimestamp();
                
                System.out.println("[" + timestamp + "] Client #" + clientId + " → " + inputLine);
                
                // Comandi speciali
                if ("quit".equalsIgnoreCase(inputLine.trim())) {
                    out.println("👋 Arrivederci! Hai inviato " + (messageCount - 1) + " messaggi.");
                    break;
                    
                } else if ("info".equalsIgnoreCase(inputLine.trim())) {
                    // Informazioni connessione
                    out.println("📊 Informazioni Connessione:");
                    out.println("   Client ID: #" + clientId);
                    out.println("   Indirizzo: " + clientAddr);
                    out.println("   Messaggi inviati: " + messageCount);
                    out.println("   Connesso da: " + formatDuration(System.currentTimeMillis()));
                    out.println("   Socket locale: " + clientSocket.getLocalSocketAddress());
                    
                } else if ("stats".equalsIgnoreCase(inputLine.trim())) {
                    // Statistiche server
                    out.println("📈 Statistiche Server:");
                    out.println("   Porta: " + port);
                    out.println("   Client totali serviti: " + clientCounter.get());
                    out.println("   Server avviato: " + formatDuration(System.currentTimeMillis()));
                    
                } else {
                    // Echo normale con timestamp
                    String response = String.format("📡 Echo #%d [%s]: %s", 
                                                   messageCount, timestamp, inputLine);
                    out.println(response);
                }
            }
            
        } catch (SocketTimeoutException e) {
            System.out.println("⏰ Client #" + clientId + " timeout - disconnessione");
            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println("⏰ Timeout - disconnessione automatica dopo " + CLIENT_TIMEOUT + "ms di inattività");
            } catch (IOException ignored) { }
            
        } catch (IOException e) {
            System.err.println("❌ Errore comunicazione con client #" + clientId + ": " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("🔒 [" + getCurrentTimestamp() + "] Client #" + clientId + " disconnesso");
            } catch (IOException e) {
                System.err.println("⚠️ Errore chiusura socket client #" + clientId + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Ferma il server gracefully
     */
    public void stop() {
        System.out.println("\n🛑 Fermata server in corso...");
        running = false;
        
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                System.out.println("✅ Server fermato correttamente");
            } catch (IOException e) {
                System.err.println("⚠️ Errore durante la chiusura: " + e.getMessage());
            }
        }
    }
    
    /**
     * Utility per timestamp formattato
     */
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    /**
     * Utility per durata formattata (placeholder)
     */
    private String formatDuration(long timestamp) {
        // Implementazione semplificata
        return getCurrentTimestamp();
    }
    
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        int backlog = DEFAULT_BACKLOG;
        
        // Parsing argomenti
        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
                if (port < 1 || port > 65535) {
                    System.err.println("❌ Porta non valida: " + port + " (range: 1-65535)");
                    return;
                }
            } catch (NumberFormatException e) {
                System.err.println("❌ Porta non valida: " + args[0]);
                return;
            }
        }
        
        if (args.length >= 2) {
            try {
                backlog = Integer.parseInt(args[1]);
                if (backlog < 1) {
                    System.err.println("❌ Backlog non valido: " + backlog + " (minimo: 1)");
                    return;
                }
            } catch (NumberFormatException e) {
                System.err.println("❌ Backlog non valido: " + args[1]);
                return;
            }
        }
        
        ServerTCPBase server = new ServerTCPBase(port, backlog);
        
        // Shutdown hook per chiusura graceful
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("💥 Impossibile avviare il server: " + e.getMessage());
            System.exit(1);
        }
    }
}