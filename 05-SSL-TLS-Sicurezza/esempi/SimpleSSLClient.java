import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;
import java.util.Arrays;

/**
 * Client SSL semplice per connettersi al SimpleSSLServer.
 * 
 * CARATTERISTICHE:
 * - Connessione SSL/TLS sicura
 * - Supporto TLS 1.2 e 1.3
 * - Validazione certificati (con opzione bypass per demo)
 * - Comunicazione HTTP over SSL
 * - Informazioni dettagliate sessione SSL
 * 
 * USO:
 * 1. Avvia SimpleSSLServer
 * 2. Compila: javac SimpleSSLClient.java
 * 3. Esegui: java SimpleSSLClient [hostname] [port]
 * 
 * ESEMPI:
 * java SimpleSSLClient                    (localhost:8443)
 * java SimpleSSLClient www.google.com 443 (Google HTTPS)
 * java SimpleSSLClient github.com 443     (GitHub HTTPS)
 */
public class SimpleSSLClient {
    
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8443;
    
    public static void main(String[] args) {
        
        String hostname = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        
        // Parse argomenti command line
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
        
        System.out.println("=== SIMPLE SSL CLIENT ===");
        System.out.println("Connessione a: " + hostname + ":" + port);
        
        SimpleSSLClient client = new SimpleSSLClient();
        client.connect(hostname, port);
    }
    
    public void connect(String hostname, int port) {
        
        try {
            // 1. Configura SSL context
            SSLContext sslContext = createSSLContext(hostname);
            
            // 2. Crea connessione SSL
            SSLSocketFactory factory = sslContext.getSocketFactory();
            
            System.out.println("Connessione in corso...");
            
            try (SSLSocket socket = (SSLSocket) factory.createSocket(hostname, port)) {
                
                // 3. Configura socket per sicurezza
                configureSSLSocket(socket);
                
                // 4. Esegui handshake SSL
                performHandshake(socket);
                
                // 5. Mostra informazioni sessione
                printConnectionInfo(socket);
                
                // 6. Test comunicazione HTTP
                testHTTPCommunication(socket, hostname);
                
            }
            
        } catch (SSLHandshakeException e) {
            System.err.println("ERRORE SSL Handshake: " + e.getMessage());
            
            if (e.getMessage().contains("certificate")) {
                System.err.println("\nSuggerimento: Per server self-signed esegui con opzione:");
                System.err.println("java -Dssl.trust.all=true SimpleSSLClient " + hostname + " " + port);
            }
            
        } catch (ConnectException e) {
            System.err.println("ERRORE Connessione: " + e.getMessage());
            System.err.println("Verifica che il server sia in esecuzione su " + hostname + ":" + port);
            
        } catch (Exception e) {
            System.err.println("ERRORE: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private SSLContext createSSLContext(String hostname) throws Exception {
        
        SSLContext sslContext = SSLContext.getInstance("TLS");
        
        // Controlla se bypassare validazione certificati (solo per demo!)
        boolean trustAll = Boolean.getBoolean("ssl.trust.all") || 
                          hostname.equals("localhost") || 
                          hostname.equals("127.0.0.1");
        
        if (trustAll) {
            System.out.println("ATTENZIONE: Bypassando validazione certificati (solo per demo!)");
            sslContext.init(null, createTrustAllManagers(), new SecureRandom());
        } else {
            // Usa validazione certificati standard
            sslContext.init(null, null, new SecureRandom());
        }
        
        return sslContext;
    }
    
    private TrustManager[] createTrustAllManagers() {
        return new TrustManager[] {
            new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
                
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    // Accetta tutti i certificati client
                }
                
                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    // Accetta tutti i certificati server (INSICURO!)
                    System.out.println("Certificato server accettato senza validazione (DEMO)");
                }
            }
        };
    }
    
    private void configureSSLSocket(SSLSocket socket) {
        
        // Abilita solo protocolli sicuri
        socket.setEnabledProtocols(new String[]{"TLSv1.3", "TLSv1.2"});
        
        // Configura cipher suites preferite
        String[] supportedCiphers = socket.getSupportedCipherSuites();
        String[] preferredCiphers = {
            "TLS_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_CHACHA20_POLY1305_SHA256"
        };
        
        java.util.List<String> enabledCiphers = new java.util.ArrayList<>();
        for (String cipher : preferredCiphers) {
            if (Arrays.asList(supportedCiphers).contains(cipher)) {
                enabledCiphers.add(cipher);
            }
        }
        
        if (!enabledCiphers.isEmpty()) {
            socket.setEnabledCipherSuites(enabledCiphers.toArray(new String[0]));
        }
        
        System.out.println("✓ Socket configurato:");
        System.out.println("  Protocolli: " + Arrays.toString(socket.getEnabledProtocols()));
        System.out.println("  Cipher suites: " + enabledCiphers.size() + " abilitate");
    }
    
    private void performHandshake(SSLSocket socket) throws Exception {
        
        System.out.println("Esecuzione handshake SSL...");
        long startTime = System.currentTimeMillis();
        
        socket.startHandshake();
        
        long handshakeTime = System.currentTimeMillis() - startTime;
        System.out.println("✓ Handshake SSL completato in " + handshakeTime + " ms");
    }
    
    private void printConnectionInfo(SSLSocket socket) throws Exception {
        
        SSLSession session = socket.getSession();
        
        System.out.println("\n=== INFORMAZIONI CONNESSIONE SSL ===");
        System.out.println("Server: " + socket.getRemoteSocketAddress());
        System.out.println("Protocollo: " + session.getProtocol());
        System.out.println("Cipher Suite: " + session.getCipherSuite());
        System.out.println("Session ID: " + bytesToHex(session.getId()));
        System.out.println("Creazione sessione: " + new java.util.Date(session.getCreationTime()));
        System.out.println("Ultimo accesso: " + new java.util.Date(session.getLastAccessedTime()));
        
        // Informazioni certificato server
        try {
            java.security.cert.Certificate[] serverCerts = session.getPeerCertificates();
            System.out.println("\nCertificati server: " + serverCerts.length);
            
            if (serverCerts.length > 0) {
                X509Certificate serverCert = (X509Certificate) serverCerts[0];
                System.out.println("  Subject: " + serverCert.getSubjectX500Principal().getName());
                System.out.println("  Issuer: " + serverCert.getIssuerX500Principal().getName());
                System.out.println("  Valido da: " + serverCert.getNotBefore());
                System.out.println("  Valido fino: " + serverCert.getNotAfter());
                System.out.println("  Algoritmo: " + serverCert.getSigAlgName());
                
                // Verifica validità temporale
                try {
                    serverCert.checkValidity();
                    System.out.println("  ✓ Certificato temporalmente valido");
                } catch (CertificateExpiredException e) {
                    System.out.println("  ✗ Certificato SCADUTO");
                } catch (CertificateNotYetValidException e) {
                    System.out.println("  ✗ Certificato NON ANCORA VALIDO");
                }
            }
            
        } catch (SSLPeerUnverifiedException e) {
            System.out.println("Nessun certificato server (peer non verificato)");
        }
        
        System.out.println("====================================");
    }
    
    private void testHTTPCommunication(SSLSocket socket, String hostname) throws IOException {
        
        System.out.println("\n=== TEST COMUNICAZIONE HTTP ===");
        
        // Crea stream di comunicazione
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        // Invia richiesta HTTP GET
        System.out.println("Invio richiesta HTTP GET...");
        out.println("GET / HTTP/1.1");
        out.println("Host: " + hostname);
        out.println("User-Agent: SimpleSSLClient/1.0 (Java)");
        out.println("Accept: text/html,text/plain,*/*");
        out.println("Connection: close");
        out.println(); // Linea vuota termina headers
        
        // Leggi risposta HTTP
        System.out.println("Lettura risposta...");
        
        String line;
        boolean inHeaders = true;
        int lineCount = 0;
        StringBuilder response = new StringBuilder();
        
        while ((line = in.readLine()) != null && lineCount < 50) {
            
            if (inHeaders) {
                if (line.isEmpty()) {
                    inHeaders = false;
                    System.out.println("--- BODY ---");
                } else {
                    System.out.println("Header: " + line);
                }
            } else {
                // Body della risposta
                response.append(line).append("\n");
                
                // Mostra solo prime righe del body per brevità
                if (lineCount < 30) {
                    System.out.println(line);
                }
            }
            
            lineCount++;
        }
        
        if (lineCount >= 50) {
            System.out.println("... (risposta troncata dopo 50 righe) ...");
        }
        
        System.out.println("\n✓ Risposta ricevuta: " + lineCount + " righe");
        System.out.println("===============================");
    }
    
    private static String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
    
    // Test con diversi server
    public static void testMultipleServers() {
        
        System.out.println("=== TEST MULTIPLI SERVER SSL ===");
        
        String[][] servers = {
            {"localhost", "8443"},      // Server locale
            {"www.google.com", "443"},  // Google
            {"github.com", "443"},      // GitHub
            {"www.facebook.com", "443"} // Facebook
        };
        
        SimpleSSLClient client = new SimpleSSLClient();
        
        for (String[] server : servers) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("Test connessione: " + server[0] + ":" + server[1]);
            System.out.println("=".repeat(60));
            
            try {
                client.connect(server[0], Integer.parseInt(server[1]));
                System.out.println("✓ Test completato con successo");
            } catch (Exception e) {
                System.err.println("✗ Test fallito: " + e.getMessage());
            }
            
            // Pausa tra test
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}