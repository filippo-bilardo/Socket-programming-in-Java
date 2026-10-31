/**
 * Nome dell'Esempio: Client TCP Base
 * Guida di Riferimento: 01-Creazione-Socket-TCP.md
 * 
 * Obiettivo: Dimostrare la creazione e configurazione di un client TCP robusto.
 * 
 * Spiegazione:
 * 1. Crea una connessione TCP con timeout e retry
 * 2. Configura le opzioni del socket per performance ottimale
 * 3. Gestisce le eccezioni in modo specifico e informativo
 * 
 * @author Socket Programming Course
 * @version 1.0
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class ClientTCPBase {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;
    private static final int CONNECT_TIMEOUT = 5000; // 5 secondi
    private static final int READ_TIMEOUT = 10000;   // 10 secondi
    
    /**
     * Crea e configura un socket client TCP
     */
    public static Socket createConfiguredSocket(String host, int port) throws IOException {
        System.out.println("🔗 Creazione socket per " + host + ":" + port);
        
        // Crea socket non connesso per configurazione avanzata
        Socket socket = new Socket();
        
        try {
            // Configurazioni pre-connessione
            socket.setReuseAddress(true);   // Riusa indirizzo se disponibile
            socket.setTcpNoDelay(true);     // Disabilita algoritmo Nagle per bassa latenza
            socket.setKeepAlive(true);      // Abilita keep-alive TCP
            
            // Configura buffer (ottimizzazione performance)
            socket.setReceiveBufferSize(64 * 1024);   // Buffer ricezione 64KB
            socket.setSendBufferSize(64 * 1024);      // Buffer invio 64KB
            
            System.out.println("⚙️ Configurazione socket completata");
            System.out.println("   - TCP NoDelay: " + socket.getTcpNoDelay());
            System.out.println("   - Keep Alive: " + socket.getKeepAlive());
            System.out.println("   - Receive Buffer: " + socket.getReceiveBufferSize() + " byte");
            System.out.println("   - Send Buffer: " + socket.getSendBufferSize() + " byte");
            
            // Connessione con timeout
            System.out.println("⏳ Connessione in corso...");
            InetSocketAddress address = new InetSocketAddress(host, port);
            socket.connect(address, CONNECT_TIMEOUT);
            
            // Configurazioni post-connessione
            socket.setSoTimeout(READ_TIMEOUT);        // Timeout per operazioni read
            
            System.out.println("✅ Connessione stabilita!");
            System.out.println("   - Locale: " + socket.getLocalSocketAddress());
            System.out.println("   - Remoto: " + socket.getRemoteSocketAddress());
            System.out.println("   - Timeout lettura: " + socket.getSoTimeout() + "ms");
            
            return socket;
            
        } catch (ConnectException e) {
            socket.close();
            throw new ConnectException("Server non disponibile su " + host + ":" + port + 
                                     " - Verificare che il server sia avviato");
        } catch (SocketTimeoutException e) {
            socket.close();
            throw new SocketTimeoutException("Timeout connessione a " + host + ":" + port + 
                                           " dopo " + CONNECT_TIMEOUT + "ms");
        } catch (UnknownHostException e) {
            socket.close();
            throw new UnknownHostException("Host sconosciuto: " + host + 
                                         " - Verificare l'indirizzo");
        }
    }
    
    /**
     * Esegue un test di connessione con retry automatico
     */
    public static Socket connectWithRetry(String host, int port, int maxRetries) {
        Socket socket = null;
        int attempt = 0;
        
        while (attempt < maxRetries) {
            try {
                attempt++;
                System.out.println("🔄 Tentativo " + attempt + "/" + maxRetries);
                
                socket = createConfiguredSocket(host, port);
                return socket; // Connessione riuscita
                
            } catch (ConnectException e) {
                System.err.println("❌ Tentativo " + attempt + " fallito: " + e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        System.out.println("⏸️ Attesa 2 secondi prima del prossimo tentativo...");
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("❌ Errore I/O: " + e.getMessage());
                break; // Errore non recuperabile
            }
        }
        
        System.err.println("💥 Impossibile connettersi dopo " + maxRetries + " tentativi");
        return null;
    }
    
    /**
     * Test comunicazione base
     */
    public static boolean testCommunication(Socket socket) {
        System.out.println("\n💬 Test comunicazione...");
        
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8)) {
            
            // Consuma il benvenuto, delimitato da una riga END.
            String welcome;
            while ((welcome = in.readLine()) != null && !welcome.equals("END")) {
                System.out.println(welcome);
            }
            if (welcome == null) throw new IOException("Benvenuto incompleto");
            // Invia messaggio di test
            String testMessage = "Hello Server! Timestamp: " + System.currentTimeMillis();
            System.out.println("📤 Invio: " + testMessage);
            out.println(testMessage);
            
            // Attende risposta
            System.out.println("⏳ Attesa risposta...");
            String response = in.readLine();
            
            if (response != null) {
                if (!response.endsWith(testMessage)) throw new IOException("Echo inatteso: " + response);
                System.out.println("📨 Risposta: " + response);
            } else {
                throw new IOException("Nessuna risposta ricevuta");
            }
            
            return true;
        } catch (SocketTimeoutException e) {
            System.err.println("⏰ Timeout durante la comunicazione: " + e.getMessage());
            return false;
        } catch (IOException e) {
            System.err.println("❌ Errore durante la comunicazione: " + e.getMessage());
            return false;
        }
    }
    
    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        
        // Parsing argomenti
        if (args.length >= 1) host = args[0];
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("❌ Porta non valida: " + args[1]);
                return;
            }
        }
        
        System.out.println("🚀 Client TCP Base");
        System.out.println("Target: " + host + ":" + port);
        System.out.println("=" .repeat(40));
        
        // Connessione con retry
        Socket socket = connectWithRetry(host, port, 3);
        
        if (socket != null) {
            try {
                // Test comunicazione
                if (testCommunication(socket)) System.out.println("\n✅ Test completato con successo");
                else System.err.println("Test comunicazione fallito");
                
            } finally {
                // Chiusura socket
                try {
                    System.out.println("🔒 Chiusura connessione...");
                    socket.close();
                    System.out.println("✅ Socket chiuso");
                } catch (IOException e) {
                    System.err.println("⚠️ Errore durante la chiusura: " + e.getMessage());
                }
            }
        } else {
            System.err.println("💥 Test fallito - impossibile stabilire connessione");
            System.exit(1);
        }
    }
}