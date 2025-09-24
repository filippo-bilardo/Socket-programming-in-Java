import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Arrays;

/**
 * Server SSL semplice per dimostrazioni e testing.
 * 
 * CARATTERISTICHE:
 * - Server SSL/TLS con certificato self-signed per demo
 * - Supporto TLS 1.2 e 1.3
 * - Cipher suites sicure
 * - Gestione client multipli
 * - Logging dettagliato connessioni
 * 
 * USO:
 * 1. Esegui CreateKeyStore.java per generare certificati
 * 2. Compila: javac SimpleSSLServer.java
 * 3. Avvia: java SimpleSSLServer
 * 4. Connetti con SimpleSSLClient o browser su https://localhost:8443
 * 
 * NOTA: Usa certificati self-signed solo per testing!
 */
public class SimpleSSLServer {
    
    private static final int PORT = 8443;
    private static final String KEYSTORE_PATH = "server.jks";
    private static final String KEYSTORE_PASSWORD = "serverpass";
    
    private SSLServerSocket serverSocket;
    private volatile boolean running = false;
    
    public static void main(String[] args) {
        SimpleSSLServer server = new SimpleSSLServer();
        
        // Gestione shutdown graceful
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n=== SHUTDOWN SERVER ===");
            server.stop();
        }));
        
        System.out.println("=== SIMPLE SSL SERVER ===");
        System.out.println("Avvio del server SSL sulla porta " + PORT);
        
        server.start();
    }
    
    public void start() {
        try {
            // 1. Inizializza SSL context
            SSLContext sslContext = createSSLContext();
            
            // 2. Crea server socket SSL
            SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
            serverSocket = (SSLServerSocket) factory.createServerSocket(PORT);
            
            // 3. Configura server socket per sicurezza
            configureServerSocket(serverSocket);
            
            System.out.println("✓ Server SSL avviato su https://localhost:" + PORT);
            System.out.println("✓ Protocolli supportati: " + 
                Arrays.toString(serverSocket.getEnabledProtocols()));
            System.out.println("✓ Cipher suites abilitate: " + 
                serverSocket.getEnabledCipherSuites().length);
            
            System.out.println("\nIn attesa di connessioni...");
            System.out.println("(Usa Ctrl+C per fermare il server)");
            
            running = true;
            
            // 4. Loop principale - accetta connessioni
            while (running) {
                try {
                    SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                    
                    // Gestisci client in thread separato
                    Thread clientThread = new Thread(() -> handleClient(clientSocket));
                    clientThread.setDaemon(true);
                    clientThread.start();
                    
                } catch (SocketException e) {
                    if (running) {
                        System.err.println("Errore accettazione client: " + e.getMessage());
                    }
                } catch (Exception e) {
                    System.err.println("Errore server: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
        } catch (Exception e) {
            System.err.println("ERRORE FATALE: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void stop() {
        running = false;
        
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                System.out.println("✓ Server socket chiuso");
            } catch (IOException e) {
                System.err.println("Errore chiusura server: " + e.getMessage());
            }
        }
    }
    
    private SSLContext createSSLContext() throws Exception {
        
        System.out.println("Inizializzazione SSL Context...");
        
        // Carica keystore con certificato server
        KeyStore keyStore = loadKeyStore();
        
        // Inizializza KeyManager per certificato server
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, KEYSTORE_PASSWORD.toCharArray());
        
        // Usa default TrustManager (accetta CA standard)
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init((KeyStore) null);
        
        // Crea SSL Context
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), 
                       new SecureRandom());
        
        System.out.println("✓ SSL Context inizializzato");
        return sslContext;
    }
    
    private KeyStore loadKeyStore() throws Exception {
        
        KeyStore keyStore = KeyStore.getInstance("JKS");
        File keystoreFile = new File(KEYSTORE_PATH);
        
        if (!keystoreFile.exists()) {
            System.out.println("Keystore non trovato: " + KEYSTORE_PATH);
            System.out.println("Creazione keystore demo...");
            createDemoKeyStore();
        }
        
        try (FileInputStream fis = new FileInputStream(KEYSTORE_PATH)) {
            keyStore.load(fis, KEYSTORE_PASSWORD.toCharArray());
        }
        
        System.out.println("✓ Keystore caricato: " + KEYSTORE_PATH);
        return keyStore;
    }
    
    private void createDemoKeyStore() throws Exception {
        
        System.out.println("ATTENZIONE: Creazione certificato self-signed per DEMO!");
        System.out.println("In produzione usa certificati firmati da CA riconosciute!");
        
        // Usa keytool per creare keystore demo
        String command = String.format(
            "keytool -genkeypair -alias server -keyalg RSA -keysize 2048 " +
            "-validity 365 -keystore %s -storepass %s -keypass %s " +
            "-dname \"CN=localhost,OU=Demo,O=SSLDemo,L=City,ST=State,C=IT\"",
            KEYSTORE_PATH, KEYSTORE_PASSWORD, KEYSTORE_PASSWORD
        );
        
        try {
            Process process = Runtime.getRuntime().exec(command.split(" "));
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                System.out.println("✓ Keystore demo creato con successo");
            } else {
                throw new RuntimeException("Errore creazione keystore (exit code: " + exitCode + ")");
            }
            
        } catch (Exception e) {
            System.err.println("Errore esecuzione keytool: " + e.getMessage());
            System.out.println("\nEsegui manualmente:");
            System.out.println(command);
            throw e;
        }
    }
    
    private void configureServerSocket(SSLServerSocket serverSocket) {
        
        // Abilita solo protocolli sicuri
        String[] supportedProtocols = serverSocket.getSupportedProtocols();
        java.util.List<String> enabledProtocols = new java.util.ArrayList<>();
        
        for (String protocol : supportedProtocols) {
            if ("TLSv1.3".equals(protocol) || "TLSv1.2".equals(protocol)) {
                enabledProtocols.add(protocol);
            }
        }
        
        serverSocket.setEnabledProtocols(enabledProtocols.toArray(new String[0]));
        
        // Configura cipher suites sicure
        String[] supportedCiphers = serverSocket.getSupportedCipherSuites();
        String[] preferredCiphers = {
            // TLS 1.3
            "TLS_AES_256_GCM_SHA384",
            "TLS_CHACHA20_POLY1305_SHA256",
            "TLS_AES_128_GCM_SHA256",
            
            // TLS 1.2 con PFS
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384"
        };
        
        java.util.List<String> enabledCiphers = new java.util.ArrayList<>();
        for (String cipher : preferredCiphers) {
            if (Arrays.asList(supportedCiphers).contains(cipher)) {
                enabledCiphers.add(cipher);
            }
        }
        
        if (!enabledCiphers.isEmpty()) {
            serverSocket.setEnabledCipherSuites(enabledCiphers.toArray(new String[0]));
        }
        
        // Non richiedere autenticazione client (per semplicità)
        serverSocket.setWantClientAuth(false);
        
        System.out.println("✓ Server socket configurato per sicurezza");
    }
    
    private void handleClient(SSLSocket clientSocket) {
        
        String clientInfo = clientSocket.getRemoteSocketAddress().toString();
        
        try {
            System.out.println("\n=== NUOVA CONNESSIONE ===");
            System.out.println("Client: " + clientInfo);
            
            // Avvia handshake SSL
            long handshakeStart = System.currentTimeMillis();
            clientSocket.startHandshake();
            long handshakeTime = System.currentTimeMillis() - handshakeStart;
            
            // Mostra informazioni sessione SSL
            printSessionInfo(clientSocket, handshakeTime);
            
            // Gestisci comunicazione HTTP-like
            handleHTTPCommunication(clientSocket);
            
        } catch (SSLHandshakeException e) {
            System.err.println("SSL Handshake fallito per " + clientInfo + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Errore gestione client " + clientInfo + ": " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("✓ Connessione chiusa: " + clientInfo);
            } catch (IOException e) {
                System.err.println("Errore chiusura socket: " + e.getMessage());
            }
        }
    }
    
    private void printSessionInfo(SSLSocket socket, long handshakeTime) throws Exception {
        
        SSLSession session = socket.getSession();
        
        System.out.println("=== SESSIONE SSL STABILITA ===");
        System.out.println("Protocollo: " + session.getProtocol());
        System.out.println("Cipher Suite: " + session.getCipherSuite());
        System.out.println("Handshake time: " + handshakeTime + " ms");
        System.out.println("Session ID: " + bytesToHex(session.getId()));
        
        // Informazioni certificato se presente
        try {
            java.security.cert.Certificate[] certs = session.getPeerCertificates();
            System.out.println("Client certificates: " + certs.length);
        } catch (SSLPeerUnverifiedException e) {
            System.out.println("Client certificates: Nessuno (non richiesto)");
        }
        
        System.out.println("==============================");
    }
    
    private void handleHTTPCommunication(SSLSocket socket) throws IOException {
        
        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream())
        );
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        
        // Leggi richiesta HTTP
        String requestLine = in.readLine();
        if (requestLine != null) {
            System.out.println("Richiesta: " + requestLine);
            
            // Leggi headers HTTP
            String header;
            while ((header = in.readLine()) != null && !header.isEmpty()) {
                System.out.println("Header: " + header);
            }
            
            // Invia risposta HTTP
            sendHTTPResponse(out, requestLine);
        }
    }
    
    private void sendHTTPResponse(PrintWriter out, String requestLine) {
        
        // Determina tipo risposta basato su richiesta
        String path = "/";
        if (requestLine != null && requestLine.contains(" ")) {
            String[] parts = requestLine.split(" ");
            if (parts.length >= 2) {
                path = parts[1];
            }
        }
        
        // Headers HTTP
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/html; charset=UTF-8");
        out.println("Connection: close");
        out.println("Server: SimpleSSLServer/1.0");
        out.println();
        
        // Body HTML
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("    <title>Simple SSL Server</title>");
        out.println("    <style>");
        out.println("        body { font-family: Arial, sans-serif; margin: 40px; }");
        out.println("        .header { color: #2c5aa0; border-bottom: 2px solid #2c5aa0; padding-bottom: 10px; }");
        out.println("        .info { background: #f0f0f0; padding: 15px; margin: 20px 0; border-radius: 5px; }");
        out.println("        .success { color: #008000; font-weight: bold; }");
        out.println("    </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("    <h1 class=\"header\">🔒 Simple SSL Server</h1>");
        out.println("    <p class=\"success\">✓ Connessione SSL/TLS stabilita con successo!</p>");
        
        out.println("    <div class=\"info\">");
        out.println("        <h3>Informazioni Richiesta:</h3>");
        out.println("        <p><strong>Path richiesto:</strong> " + path + "</p>");
        out.println("        <p><strong>Timestamp:</strong> " + new java.util.Date() + "</p>");
        out.println("        <p><strong>Server:</strong> SimpleSSLServer/1.0</p>");
        out.println("    </div>");
        
        out.println("    <div class=\"info\">");
        out.println("        <h3>Test SSL:</h3>");
        out.println("        <ul>");
        out.println("            <li>✓ Certificato server caricato</li>");
        out.println("            <li>✓ Handshake SSL completato</li>");
        out.println("            <li>✓ Comunicazione crittografata attiva</li>");
        out.println("            <li>✓ Protocolli sicuri (TLS 1.2/1.3)</li>");
        out.println("        </ul>");
        out.println("    </div>");
        
        if (path.equals("/test")) {
            out.println("    <div class=\"info\">");
            out.println("        <h3>Pagina di Test:</h3>");
            out.println("        <p>Questa è una pagina di test per verificare il routing.</p>");
            out.println("        <p>Il server SSL può gestire diverse pagine.</p>");
            out.println("    </div>");
        }
        
        out.println("    <div class=\"info\">");
        out.println("        <h3>Links di Test:</h3>");
        out.println("        <ul>");
        out.println("            <li><a href=\"/\">Home</a></li>");
        out.println("            <li><a href=\"/test\">Pagina Test</a></li>");
        out.println("            <li><a href=\"/info\">Info Server</a></li>");
        out.println("        </ul>");
        out.println("    </div>");
        
        out.println("    <footer style=\"margin-top: 40px; font-size: 0.9em; color: #666;\">");
        out.println("        <p>Simple SSL Server - Corso Socket Programming Java</p>");
        out.println("    </footer>");
        out.println("</body>");
        out.println("</html>");
        
        out.flush();
        System.out.println("✓ Risposta HTTP inviata per path: " + path);
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}