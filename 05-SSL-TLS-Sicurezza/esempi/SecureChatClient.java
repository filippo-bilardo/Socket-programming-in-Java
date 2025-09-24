import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Scanner;

/**
 * Client Chat SSL per connessioni sicure al SecureChatServer.
 * 
 * CARATTERISTICHE:
 * - Connessione SSL/TLS sicura al chat server
 * - Interfaccia utente interattiva
 * - Gestione messaggi in tempo reale
 * - Supporto comandi chat
 * - Riconnessione automatica (opzionale)
 * - Validazione input utente
 * 
 * FUNZIONALITÀ:
 * - Invio messaggi pubblici e privati
 * - Comandi chat (/users, /help, /quit, /private)
 * - Thread separato per ricezione messaggi
 * - Gestione disconnessioni graceful
 * - Timestamping locale messaggi
 * 
 * USO:
 * 1. Avvia SecureChatServer
 * 2. Compila: javac SecureChatClient.java
 * 3. Esegui: java SecureChatClient [hostname] [port]
 * 4. Interagisci con la chat
 */
public class SecureChatClient {
    
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8443;
    
    private SSLSocket socket;
    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean connected = false;
    private String hostname;
    private int port;
    
    public SecureChatClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }
    
    public static void main(String[] args) {
        
        String hostname = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        
        // Parse argomenti
        if (args.length >= 1) {
            hostname = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Porta non valida: " + args[1]);
                System.exit(1);
            }
        }
        
        System.out.println("=== SECURE CHAT CLIENT ===");
        System.out.println("Connessione a chat server: " + hostname + ":" + port);
        
        SecureChatClient client = new SecureChatClient(hostname, port);
        client.start();
    }
    
    public void start() {
        
        try {
            // Connetti al server
            connect();
            
            // Avvia thread ricezione messaggi
            Thread messageReceiver = new Thread(this::receiveMessages);
            messageReceiver.setDaemon(true);
            messageReceiver.start();
            
            // Interfaccia utente per invio messaggi
            handleUserInput();
            
        } catch (Exception e) {
            System.err.println("ERRORE CLIENT: " + e.getMessage());
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }
    
    private void connect() throws Exception {
        
        System.out.println("Connessione al server...");
        
        // Configura SSL context
        SSLContext sslContext = createSSLContext();
        
        // Connetti
        SSLSocketFactory factory = sslContext.getSocketFactory();
        socket = (SSLSocket) factory.createSocket(hostname, port);
        
        // Configura socket
        configureSSLSocket(socket);
        
        // Handshake SSL
        socket.startHandshake();
        
        // Inizializza stream
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        connected = true;
        
        // Mostra info connessione
        printConnectionInfo();
        
        System.out.println("✓ Connesso al chat server!");
        System.out.println("Digita messaggi per chattare, /help per comandi, /quit per uscire\n");
    }
    
    private SSLContext createSSLContext() throws Exception {
        
        SSLContext sslContext = SSLContext.getInstance("TLS");
        
        // Per localhost/demo, bypassa validazione certificati
        if (hostname.equals("localhost") || hostname.equals("127.0.0.1")) {
            System.out.println("DEMO MODE: Bypassando validazione certificati per localhost");
            sslContext.init(null, createTrustAllManagers(), new SecureRandom());
        } else {
            // Usa validazione standard per server remoti
            sslContext.init(null, null, new SecureRandom());
        }
        
        return sslContext;
    }
    
    private TrustManager[] createTrustAllManagers() {
        return new TrustManager[] {
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[0];
                }
                public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {}
            }
        };
    }
    
    private void configureSSLSocket(SSLSocket socket) {
        
        // Protocolli sicuri
        socket.setEnabledProtocols(new String[]{"TLSv1.3", "TLSv1.2"});
        
        // Cipher suites sicure
        String[] supportedCiphers = socket.getSupportedCipherSuites();
        String[] preferredCiphers = {
            "TLS_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"
        };
        
        java.util.List<String> enabledCiphers = new java.util.ArrayList<>();
        for (String cipher : preferredCiphers) {
            if (java.util.Arrays.asList(supportedCiphers).contains(cipher)) {
                enabledCiphers.add(cipher);
            }
        }
        
        if (!enabledCiphers.isEmpty()) {
            socket.setEnabledCipherSuites(enabledCiphers.toArray(new String[0]));
        }
    }
    
    private void printConnectionInfo() throws Exception {
        
        SSLSession session = socket.getSession();
        
        System.out.println("\n=== CONNESSIONE SSL STABILITA ===");
        System.out.println("Server: " + socket.getRemoteSocketAddress());
        System.out.println("Protocollo: " + session.getProtocol());
        System.out.println("Cipher Suite: " + session.getCipherSuite());
        System.out.println("================================\n");
    }
    
    private void receiveMessages() {
        
        try {
            String message;
            while (connected && (message = in.readLine()) != null) {
                
                // Mostra messaggio ricevuto
                System.out.println(message);
                
                // Controlla disconnessione server
                if (message.contains("SERVER") && message.contains("disconn")) {
                    System.out.println("\n⚠️  Server ha chiuso la connessione");
                    break;
                }
            }
            
        } catch (IOException e) {
            if (connected) {
                System.err.println("\n⚠️  Errore ricezione messaggi: " + e.getMessage());
                System.out.println("Connessione al server persa");
            }
        }
        
        connected = false;
    }
    
    private void handleUserInput() {
        
        Scanner scanner = new Scanner(System.in);
        
        try {
            while (connected) {
                
                // Prompt per input utente
                System.out.print("> ");
                
                if (!scanner.hasNextLine()) {
                    break;
                }
                
                String input = scanner.nextLine().trim();
                
                if (input.isEmpty()) {
                    continue;
                }
                
                // Gestisci comandi locali
                if (handleLocalCommand(input)) {
                    continue;
                }
                
                // Invia messaggio al server
                if (connected) {
                    out.println(input);
                    
                    // Controlla errori invio
                    if (out.checkError()) {
                        System.err.println("⚠️  Errore invio messaggio");
                        break;
                    }
                    
                    // Comando quit locale
                    if (input.equals("/quit")) {
                        System.out.println("Disconnessione...");
                        break;
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Errore input utente: " + e.getMessage());
        }
    }
    
    private boolean handleLocalCommand(String input) {
        
        switch (input.toLowerCase()) {
            case "/clear":
                // Pulisci schermo (ANSI escape codes)
                System.out.print("\033[2J\033[H");
                System.out.flush();
                return true;
                
            case "/status":
                showConnectionStatus();
                return true;
                
            case "/reconnect":
                attemptReconnect();
                return true;
                
            default:
                return false; // Non è comando locale
        }
    }
    
    private void showConnectionStatus() {
        
        System.out.println("\n=== STATUS CONNESSIONE ===");
        System.out.println("Server: " + hostname + ":" + port);
        System.out.println("Connesso: " + (connected ? "SÌ" : "NO"));
        
        if (connected && socket != null) {
            try {
                SSLSession session = socket.getSession();
                System.out.println("Protocollo SSL: " + session.getProtocol());
                System.out.println("Cipher Suite: " + session.getCipherSuite());
                System.out.println("Session valida: " + session.isValid());
            } catch (Exception e) {
                System.out.println("Errore lettura sessione SSL");
            }
        }
        
        System.out.println("=========================\n");
    }
    
    private void attemptReconnect() {
        
        if (connected) {
            System.out.println("Già connesso al server");
            return;
        }
        
        System.out.println("Tentativo riconnessione...");
        
        try {
            disconnect();
            Thread.sleep(1000); // Pausa breve
            connect();
            
            // Riavvia thread ricezione
            Thread messageReceiver = new Thread(this::receiveMessages);
            messageReceiver.setDaemon(true);
            messageReceiver.start();
            
        } catch (Exception e) {
            System.err.println("Riconnessione fallita: " + e.getMessage());
        }
    }
    
    public void disconnect() {
        
        connected = false;
        
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Ignora errori chiusura
        }
        
        System.out.println("\nConnessione chiusa");
    }
    
    // Modalità test automatico
    public static void runAutomatedTest(String hostname, int port) {
        
        System.out.println("=== TEST AUTOMATICO CHAT CLIENT ===");
        
        SecureChatClient client = new SecureChatClient(hostname, port);
        
        try {
            client.connect();
            
            // Test messaggi automatici
            String[] testMessages = {
                "Ciao a tutti!",
                "/users",
                "Questo è un messaggio di test",
                "/help",
                "Test messaggio finale",
                "/quit"
            };
            
            for (String message : testMessages) {
                System.out.println("Invio: " + message);
                client.out.println(message);
                Thread.sleep(1000); // Pausa tra messaggi
                
                if (message.equals("/quit")) {
                    break;
                }
            }
            
            Thread.sleep(2000); // Attesa risposta finale
            
        } catch (Exception e) {
            System.err.println("Test automatico fallito: " + e.getMessage());
        } finally {
            client.disconnect();
        }
        
        System.out.println("Test automatico completato");
    }
    
    // Utility per test multipli client
    public static void runMultiClientTest(String hostname, int port, int numClients) {
        
        System.out.println("=== TEST MULTI-CLIENT ===");
        System.out.println("Avvio " + numClients + " client simultanei...");
        
        for (int i = 1; i <= numClients; i++) {
            final int clientNum = i;
            
            Thread clientThread = new Thread(() -> {
                SecureChatClient client = new SecureChatClient(hostname, port);
                
                try {
                    client.connect();
                    
                    // Invia messaggio identificativo
                    client.out.println("Sono il client " + clientNum);
                    
                    Thread.sleep(5000); // Mantieni connessione per 5 secondi
                    
                    client.out.println("/quit");
                    
                } catch (Exception e) {
                    System.err.println("Client " + clientNum + " errore: " + e.getMessage());
                } finally {
                    client.disconnect();
                }
            });
            
            clientThread.start();
            
            // Pausa breve tra avvii client
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                break;
            }
        }
        
        System.out.println("Test multi-client avviato");
    }
}