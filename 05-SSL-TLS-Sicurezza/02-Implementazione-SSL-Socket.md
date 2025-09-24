# 2. Implementazione SSL Socket in Java

## Introduzione
In questa guida impariamo come implementare **SSL Socket** in Java usando l'API JSSE (Java Secure Socket Extension). Vedremo la creazione di server e client SSL, gestione dei certificati e configurazione della sicurezza.

## Implementazione Base

### Client SSL Semplice

#### SSLSocketClient.java
```java
import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.cert.X509Certificate;

public class SSLSocketClient {
    
    public static void main(String[] args) {
        
        // Configurazione SSL per testing (non per produzione!)
        configureSSLForTesting();
        
        String hostname = "www.google.com";
        int port = 443;
        
        try {
            // Crea factory SSL socket
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            
            // Connetti al server SSL
            System.out.println("Connessione a " + hostname + ":" + port);
            SSLSocket socket = (SSLSocket) factory.createSocket(hostname, port);
            
            // Configura protocolli e cipher suites
            configureSSLSocket(socket);
            
            // Avvia handshake SSL
            socket.startHandshake();
            
            // Mostra informazioni connessione
            printConnectionInfo(socket);
            
            // Invia richiesta HTTP semplice
            sendHTTPRequest(socket, hostname);
            
            // Leggi risposta
            readHTTPResponse(socket);
            
            socket.close();
            
        } catch (Exception e) {
            System.err.println("Errore connessione SSL: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void configureSSLSocket(SSLSocket socket) {
        // Abilita solo protocolli sicuri
        socket.setEnabledProtocols(new String[]{"TLSv1.3", "TLSv1.2"});
        
        // Configura cipher suites preferite
        String[] supportedCiphers = socket.getSupportedCipherSuites();
        java.util.List<String> preferredCiphers = java.util.Arrays.asList(
            "TLS_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"
        );
        
        java.util.List<String> enabledCiphers = new java.util.ArrayList<>();
        for (String cipher : preferredCiphers) {
            if (java.util.Arrays.asList(supportedCiphers).contains(cipher)) {
                enabledCiphers.add(cipher);
            }
        }
        
        if (!enabledCiphers.isEmpty()) {
            socket.setEnabledCipherSuites(enabledCiphers.toArray(new String[0]));
        }
        
        System.out.println("Protocolli abilitati: " + 
            java.util.Arrays.toString(socket.getEnabledProtocols()));
        System.out.println("Cipher suites abilitate: " + enabledCiphers.size());
    }
    
    private static void printConnectionInfo(SSLSocket socket) throws Exception {
        SSLSession session = socket.getSession();
        
        System.out.println("\n=== INFORMAZIONI CONNESSIONE SSL ===");
        System.out.println("Protocollo: " + session.getProtocol());
        System.out.println("Cipher Suite: " + session.getCipherSuite());
        System.out.println("Peer Host: " + session.getPeerHost());
        System.out.println("Session ID: " + bytesToHex(session.getId()));
        
        // Informazioni certificato server
        X509Certificate[] certs = (X509Certificate[]) session.getPeerCertificates();
        if (certs.length > 0) {
            X509Certificate serverCert = certs[0];
            System.out.println("\nCertificato Server:");
            System.out.println("  Subject: " + serverCert.getSubjectX500Principal());
            System.out.println("  Issuer: " + serverCert.getIssuerX500Principal());
            System.out.println("  Valido da: " + serverCert.getNotBefore());
            System.out.println("  Valido fino: " + serverCert.getNotAfter());
        }
        System.out.println("=======================================\n");
    }
    
    private static void sendHTTPRequest(SSLSocket socket, String hostname) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        
        // Invia richiesta HTTP/1.1 semplice
        out.println("GET / HTTP/1.1");
        out.println("Host: " + hostname);
        out.println("User-Agent: SSL-Client-Java/1.0");
        out.println("Connection: close");
        out.println(); // Linea vuota termina headers
        
        System.out.println("Richiesta HTTP inviata");
    }
    
    private static void readHTTPResponse(SSLSocket socket) throws IOException {
        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream())
        );
        
        System.out.println("Risposta HTTP ricevuta:");
        System.out.println("------------------------");
        
        String line;
        int lineCount = 0;
        while ((line = in.readLine()) != null && lineCount < 20) {
            System.out.println(line);
            lineCount++;
            
            // Mostra solo prime 20 righe per brevità
            if (lineCount == 20) {
                System.out.println("... (risposta troncata) ...");
            }
        }
    }
    
    // Configurazione per testing (INSICURA - solo per demo!)
    private static void configureSSLForTesting() {
        // ATTENZIONE: Questo bypassa la validazione certificati!
        // NON usare mai in produzione!
        
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };
            
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            
            // NON usare in produzione!
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(
                (hostname, session) -> true
            );
            
        } catch (Exception e) {
            System.err.println("Errore configurazione SSL test: " + e.getMessage());
        }
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
```

### Server SSL Base

#### SSLSocketServer.java  
```java
import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.util.concurrent.*;

public class SSLSocketServer {
    
    private static final int PORT = 8443;
    private static final String KEYSTORE_PATH = "server.jks";
    private static final String KEYSTORE_PASSWORD = "serverpass";
    
    private SSLServerSocket serverSocket;
    private ExecutorService threadPool;
    private volatile boolean running = false;
    
    public static void main(String[] args) {
        SSLSocketServer server = new SSLSocketServer();
        
        // Gestione shutdown graceful
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutdown del server in corso...");
            server.stop();
        }));
        
        server.start();
    }
    
    public void start() {
        try {
            // Inizializza SSL context
            SSLContext sslContext = createSSLContext();
            
            // Crea server socket SSL
            SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
            serverSocket = (SSLServerSocket) factory.createServerSocket(PORT);
            
            // Configura server socket
            configureServerSocket(serverSocket);
            
            // Thread pool per gestire client
            threadPool = Executors.newFixedThreadPool(10);
            
            running = true;
            System.out.println("Server SSL avviato su porta " + PORT);
            System.out.println("Protocolli supportati: " + 
                java.util.Arrays.toString(serverSocket.getSupportedProtocols()));
            
            // Loop accettazione connessioni
            while (running) {
                try {
                    SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                    threadPool.submit(new ClientHandler(clientSocket));
                    
                } catch (SocketException e) {
                    if (running) {
                        System.err.println("Errore accettazione client: " + e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Errore avvio server SSL: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void stop() {
        running = false;
        
        if (threadPool != null) {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
            }
        }
        
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Errore chiusura server socket: " + e.getMessage());
            }
        }
        
        System.out.println("Server SSL fermato");
    }
    
    private SSLContext createSSLContext() throws Exception {
        
        // Carica keystore con certificato server
        KeyStore keyStore = loadKeyStore(KEYSTORE_PATH, KEYSTORE_PASSWORD);
        
        // Inizializza KeyManager
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, KEYSTORE_PASSWORD.toCharArray());
        
        // Usa default TrustManager
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init((KeyStore) null);
        
        // Crea SSL Context
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), 
            new SecureRandom());
        
        return sslContext;
    }
    
    private KeyStore loadKeyStore(String path, String password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        
        try (FileInputStream fis = new FileInputStream(path)) {
            keyStore.load(fis, password.toCharArray());
        } catch (FileNotFoundException e) {
            System.out.println("KeyStore non trovato, creando keystore demo...");
            keyStore = createDemoKeyStore(path, password);
        }
        
        return keyStore;
    }
    
    private KeyStore createDemoKeyStore(String path, String password) throws Exception {
        // Per demo, crea keystore self-signed
        // In produzione, usa certificati firmati da CA riconosciute!
        
        System.out.println("ATTENZIONE: Usando certificato self-signed per demo!");
        System.out.println("In produzione usa certificati firmati da CA!");
        
        // Genera key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        
        // Crea certificato self-signed (solo per demo!)
        X509Certificate cert = createSelfSignedCertificate(keyPair);
        
        // Crea keystore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);
        
        // Aggiungi chiave privata e certificato
        keyStore.setKeyEntry("server", keyPair.getPrivate(), 
            password.toCharArray(), new java.security.cert.Certificate[]{cert});
        
        // Salva keystore
        try (FileOutputStream fos = new FileOutputStream(path)) {
            keyStore.store(fos, password.toCharArray());
        }
        
        return keyStore;
    }
    
    private java.security.cert.X509Certificate createSelfSignedCertificate(KeyPair keyPair) 
            throws Exception {
        
        // Questo è un esempio semplificato per demo
        // In realtà dovremmo usare BouncyCastle o altre librerie
        // per creare certificati X.509 validi
        
        throw new UnsupportedOperationException(
            "Creazione certificati self-signed richiede libreria esterna " +
            "(es. BouncyCastle). Per demo, usa keytool per creare keystore."
        );
    }
    
    private void configureServerSocket(SSLServerSocket serverSocket) {
        // Abilita solo protocolli sicuri
        String[] supportedProtocols = serverSocket.getSupportedProtocols();
        java.util.List<String> enabledProtocols = new java.util.ArrayList<>();
        
        for (String protocol : supportedProtocols) {
            if (protocol.equals("TLSv1.3") || protocol.equals("TLSv1.2")) {
                enabledProtocols.add(protocol);
            }
        }
        
        serverSocket.setEnabledProtocols(enabledProtocols.toArray(new String[0]));
        
        // Configura cipher suites sicure
        String[] supportedCiphers = serverSocket.getSupportedCipherSuites();
        String[] preferredCiphers = {
            "TLS_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384"
        };
        
        java.util.List<String> enabledCiphers = new java.util.ArrayList<>();
        for (String cipher : preferredCiphers) {
            if (java.util.Arrays.asList(supportedCiphers).contains(cipher)) {
                enabledCiphers.add(cipher);
            }
        }
        
        if (!enabledCiphers.isEmpty()) {
            serverSocket.setEnabledCipherSuites(enabledCiphers.toArray(new String[0]));
        }
        
        // Richiede autenticazione client (opzionale)
        // serverSocket.setNeedClientAuth(true);
        serverSocket.setWantClientAuth(false);
    }
    
    // Handler per ogni connessione client
    private static class ClientHandler implements Runnable {
        
        private final SSLSocket clientSocket;
        
        public ClientHandler(SSLSocket socket) {
            this.clientSocket = socket;
        }
        
        @Override
        public void run() {
            try {
                System.out.println("Nuova connessione da: " + 
                    clientSocket.getRemoteSocketAddress());
                
                // Configura socket client
                configureClientSocket(clientSocket);
                
                // Avvia handshake SSL
                clientSocket.startHandshake();
                
                // Mostra info connessione
                printSessionInfo(clientSocket);
                
                // Gestisci comunicazione
                handleCommunication(clientSocket);
                
            } catch (Exception e) {
                System.err.println("Errore gestione client: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Errore chiusura socket client: " + e.getMessage());
                }
            }
        }
        
        private void configureClientSocket(SSLSocket socket) {
            // Configura protocolli e cipher suites come server socket
            socket.setUseClientMode(false);
        }
        
        private void printSessionInfo(SSLSocket socket) throws Exception {
            SSLSession session = socket.getSession();
            
            System.out.println("=== SESSIONE SSL STABILITA ===");
            System.out.println("Client: " + socket.getRemoteSocketAddress());
            System.out.println("Protocollo: " + session.getProtocol());
            System.out.println("Cipher Suite: " + session.getCipherSuite());
            System.out.println("Session ID: " + bytesToHex(session.getId()));
            System.out.println("Creazione sessione: " + 
                new java.util.Date(session.getCreationTime()));
            System.out.println("===============================");
        }
        
        private void handleCommunication(SSLSocket socket) throws IOException {
            
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            
            // Leggi richiesta
            String requestLine = in.readLine();
            if (requestLine != null) {
                System.out.println("Richiesta: " + requestLine);
                
                // Leggi headers (HTTP-like)
                String line;
                while ((line = in.readLine()) != null && !line.isEmpty()) {
                    System.out.println("Header: " + line);
                }
                
                // Invia risposta HTTP semplice
                sendHTTPResponse(out);
            }
        }
        
        private void sendHTTPResponse(PrintWriter out) {
            String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                "<html><body>" +
                "<h1>SSL Server Java</h1>" +
                "<p>Connessione SSL stabilita con successo!</p>" +
                "<p>Timestamp: " + LocalDateTime.now() + "</p>" +
                "</body></html>";
            
            out.print(response);
            out.flush();
            
            System.out.println("Risposta HTTP inviata");
        }
        
        private static String bytesToHex(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        }
    }
}
```

## Gestione Certificati

### Creazione KeyStore per Demo

#### CreateKeyStore.java
```java
import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;

public class CreateKeyStore {
    
    public static void main(String[] args) {
        createServerKeyStore();
        createClientKeyStore();
        createTrustStore();
        printInstructions();
    }
    
    public static void createServerKeyStore() {
        System.out.println("=== CREAZIONE SERVER KEYSTORE ===");
        
        // Per demo, usiamo keytool command
        String[] commands = {
            "keytool -genkeypair -alias server -keyalg RSA -keysize 2048 " +
            "-validity 365 -keystore server.jks -storepass serverpass " +
            "-keypass serverpass -dname \"CN=localhost,OU=Demo,O=SSLDemo,C=IT\"",
            
            "keytool -exportcert -alias server -keystore server.jks " +
            "-storepass serverpass -file server.crt"
        };
        
        executeCommands(commands);
    }
    
    public static void createClientKeyStore() {
        System.out.println("\n=== CREAZIONE CLIENT KEYSTORE ===");
        
        String[] commands = {
            "keytool -genkeypair -alias client -keyalg RSA -keysize 2048 " +
            "-validity 365 -keystore client.jks -storepass clientpass " +
            "-keypass clientpass -dname \"CN=client,OU=Demo,O=SSLDemo,C=IT\"",
            
            "keytool -exportcert -alias client -keystore client.jks " +
            "-storepass clientpass -file client.crt"
        };
        
        executeCommands(commands);
    }
    
    public static void createTrustStore() {
        System.out.println("\n=== CREAZIONE TRUSTSTORE ===");
        
        String[] commands = {
            // Importa certificato server nel truststore client
            "keytool -importcert -alias server -file server.crt " +
            "-keystore client-truststore.jks -storepass trustpass -noprompt",
            
            // Importa certificato client nel truststore server
            "keytool -importcert -alias client -file client.crt " +
            "-keystore server-truststore.jks -storepass trustpass -noprompt"
        };
        
        executeCommands(commands);
    }
    
    private static void executeCommands(String[] commands) {
        for (String command : commands) {
            System.out.println("Eseguendo: " + command);
            
            try {
                Process process = Runtime.getRuntime().exec(command.split(" "));
                int exitCode = process.waitFor();
                
                if (exitCode == 0) {
                    System.out.println("✓ Comando eseguito con successo");
                } else {
                    System.err.println("✗ Errore esecuzione comando (exit code: " + exitCode + ")");
                    
                    // Mostra errori
                    BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()));
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        System.err.println("  " + line);
                    }
                }
                
            } catch (Exception e) {
                System.err.println("✗ Eccezione esecuzione comando: " + e.getMessage());
                System.out.println("  Prova a eseguire manualmente: " + command);
            }
        }
    }
    
    private static void printInstructions() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("KEYSTORE E TRUSTSTORE CREATI");
        System.out.println("=".repeat(60));
        
        System.out.println("\nFile generati:");
        System.out.println("  server.jks           - Chiave privata server");
        System.out.println("  client.jks           - Chiave privata client");
        System.out.println("  server-truststore.jks - Certificati fidati server");
        System.out.println("  client-truststore.jks - Certificati fidati client");
        System.out.println("  server.crt           - Certificato pubblico server");
        System.out.println("  client.crt           - Certificato pubblico client");
        
        System.out.println("\nPassword:");
        System.out.println("  Server keystore: serverpass");
        System.out.println("  Client keystore: clientpass");
        System.out.println("  Truststore:      trustpass");
        
        System.out.println("\nPer usare con SSL Server/Client:");
        System.out.println("  -Djavax.net.ssl.keyStore=server.jks");
        System.out.println("  -Djavax.net.ssl.keyStorePassword=serverpass");
        System.out.println("  -Djavax.net.ssl.trustStore=client-truststore.jks");
        System.out.println("  -Djavax.net.ssl.trustStorePassword=trustpass");
        
        System.out.println("\n" + "=".repeat(60));
    }
    
    // Metodo alternativo usando Java KeyStore API
    public static void createKeyStoreProgammatically() {
        try {
            // Genera coppia chiavi RSA
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048, new SecureRandom());
            KeyPair keyPair = keyGen.generateKeyPair();
            
            // Crea keystore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, null);
            
            // Per creare un certificato X.509 valido servirebbe
            // una libreria come BouncyCastle
            System.out.println("Per implementazione completa, usa keytool o BouncyCastle");
            
        } catch (Exception e) {
            System.err.println("Errore creazione keystore: " + e.getMessage());
        }
    }
}
```

## Client SSL con Autenticazione

### MutualSSLClient.java
```java
import javax.net.ssl.*;
import java.io.*;
import java.security.*;

public class MutualSSLClient {
    
    private static final String CLIENT_KEYSTORE = "client.jks";
    private static final String CLIENT_KEYSTORE_PASS = "clientpass";
    private static final String TRUSTSTORE = "client-truststore.jks";
    private static final String TRUSTSTORE_PASS = "trustpass";
    
    public static void main(String[] args) {
        
        // Configura SSL context con autenticazione mutua
        try {
            SSLContext sslContext = createMutualSSLContext();
            
            // Connetti al server
            connectToServer(sslContext, "localhost", 8443);
            
        } catch (Exception e) {
            System.err.println("Errore SSL client: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static SSLContext createMutualSSLContext() throws Exception {
        
        // 1. Carica keystore client (chiave privata per autenticazione)
        KeyStore clientKeyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(CLIENT_KEYSTORE)) {
            clientKeyStore.load(fis, CLIENT_KEYSTORE_PASS.toCharArray());
        }
        
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(clientKeyStore, CLIENT_KEYSTORE_PASS.toCharArray());
        
        // 2. Carica truststore (certificati server fidati)
        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(TRUSTSTORE)) {
            trustStore.load(fis, TRUSTSTORE_PASS.toCharArray());
        }
        
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(trustStore);
        
        // 3. Crea SSL context
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), 
            new SecureRandom());
        
        return sslContext;
    }
    
    private static void connectToServer(SSLContext sslContext, String host, int port) 
            throws Exception {
        
        SSLSocketFactory factory = sslContext.getSocketFactory();
        
        try (SSLSocket socket = (SSLSocket) factory.createSocket(host, port)) {
            
            // Configura socket
            socket.setEnabledProtocols(new String[]{"TLSv1.3", "TLSv1.2"});
            
            // Avvia handshake
            socket.startHandshake();
            
            // Verifica autenticazione mutua
            SSLSession session = socket.getSession();
            System.out.println("=== CONNESSIONE SSL AUTENTICATA ===");
            System.out.println("Protocollo: " + session.getProtocol());
            System.out.println("Cipher Suite: " + session.getCipherSuite());
            
            // Verifica certificato server
            java.security.cert.Certificate[] serverCerts = session.getPeerCertificates();
            System.out.println("Certificati server ricevuti: " + serverCerts.length);
            
            // Verifica se server ha richiesto certificato client
            java.security.cert.Certificate[] localCerts = session.getLocalCertificates();
            if (localCerts != null) {
                System.out.println("Certificato client inviato: " + localCerts.length);
            } else {
                System.out.println("Nessun certificato client richiesto/inviato");
            }
            
            System.out.println("=================================");
            
            // Test comunicazione
            testCommunication(socket);
        }
    }
    
    private static void testCommunication(SSLSocket socket) throws IOException {
        
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));
        
        // Invia messaggio
        out.println("GET / HTTP/1.1");
        out.println("Host: localhost");
        out.println("User-Agent: MutualSSL-Client/1.0");
        out.println();
        
        // Leggi risposta
        System.out.println("\nRisposta server:");
        String line;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
            if (line.contains("</html>")) break; // Fine HTML
        }
    }
}
```

## Custom Trust Manager

### CustomTrustManager.java
```java
import javax.net.ssl.*;
import java.security.cert.*;
import java.util.*;

/**
 * Custom TrustManager per validazione certificati personalizzata.
 * ATTENZIONE: Usare solo per scopi specifici, non bypassare sicurezza!
 */
public class CustomTrustManager implements X509TrustManager {
    
    private final X509TrustManager defaultTrustManager;
    private final Set<String> acceptedIssuers;
    private final boolean allowSelfSigned;
    
    public CustomTrustManager(boolean allowSelfSigned) throws Exception {
        this.allowSelfSigned = allowSelfSigned;
        this.acceptedIssuers = new HashSet<>();
        
        // Inizializza default trust manager
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init((KeyStore) null);
        
        TrustManager[] trustManagers = tmf.getTrustManagers();
        this.defaultTrustManager = (X509TrustManager) trustManagers[0];
        
        // Aggiungi issuer accettati personalizzati
        acceptedIssuers.add("CN=Demo CA,OU=Demo,O=SSLDemo,C=IT");
        acceptedIssuers.add("CN=localhost,OU=Demo,O=SSLDemo,C=IT");
    }
    
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) 
            throws CertificateException {
        
        System.out.println("Validazione certificato client: " + authType);
        
        if (chain == null || chain.length == 0) {
            throw new CertificateException("Catena certificati client vuota");
        }
        
        try {
            // Prova validazione standard
            defaultTrustManager.checkClientTrusted(chain, authType);
            System.out.println("✓ Certificato client validato con trust manager default");
            
        } catch (CertificateException e) {
            // Se fallisce, prova validazione custom
            if (!performCustomValidation(chain, "client")) {
                throw e;
            }
        }
    }
    
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) 
            throws CertificateException {
        
        System.out.println("Validazione certificato server: " + authType);
        
        if (chain == null || chain.length == 0) {
            throw new CertificateException("Catena certificati server vuota");
        }
        
        try {
            // Prova validazione standard
            defaultTrustManager.checkServerTrusted(chain, authType);
            System.out.println("✓ Certificato server validato con trust manager default");
            
        } catch (CertificateException e) {
            System.out.println("Validazione standard fallita: " + e.getMessage());
            
            // Se fallisce, prova validazione custom
            if (!performCustomValidation(chain, "server")) {
                throw new CertificateException("Validazione certificato server fallita", e);
            }
        }
    }
    
    private boolean performCustomValidation(X509Certificate[] chain, String type) {
        
        System.out.println("Esecuzione validazione custom per " + type);
        
        try {
            X509Certificate cert = chain[0];
            
            // 1. Verifica validità temporale
            cert.checkValidity();
            System.out.println("✓ Validità temporale OK");
            
            // 2. Verifica issuer accettato
            String issuer = cert.getIssuerX500Principal().getName();
            boolean issuerAccepted = acceptedIssuers.stream()
                .anyMatch(accepted -> issuer.contains(accepted.split(",")[0]));
            
            if (!issuerAccepted && !allowSelfSigned) {
                System.out.println("✗ Issuer non accettato: " + issuer);
                return false;
            }
            System.out.println("✓ Issuer accettato: " + issuer);
            
            // 3. Se self-signed, verifica firma
            if (allowSelfSigned && isSelfSigned(cert)) {
                cert.verify(cert.getPublicKey());
                System.out.println("✓ Certificato self-signed validato");
            }
            
            // 4. Verifica key usage (se presente)
            boolean[] keyUsage = cert.getKeyUsage();
            if (keyUsage != null) {
                // Per server: digitalSignature(0) e keyEncipherment(2)
                // Per client: digitalSignature(0) e nonRepudiation(1)
                if (type.equals("server") && (!keyUsage[0] || !keyUsage[2])) {
                    System.out.println("✗ Key usage non appropriato per server");
                    return false;
                }
                System.out.println("✓ Key usage appropriato");
            }
            
            // 5. Verifica extended key usage
            List<String> extKeyUsage = cert.getExtendedKeyUsage();
            if (extKeyUsage != null) {
                boolean hasServerAuth = extKeyUsage.contains("1.3.6.1.5.5.7.3.1");
                boolean hasClientAuth = extKeyUsage.contains("1.3.6.1.5.5.7.3.2");
                
                if (type.equals("server") && !hasServerAuth) {
                    System.out.println("✗ Manca Server Authentication EKU");
                    return false;
                }
                
                if (type.equals("client") && !hasClientAuth) {
                    System.out.println("✗ Manca Client Authentication EKU");
                    return false;
                }
                System.out.println("✓ Extended Key Usage appropriato");
            }
            
            System.out.println("✓ Validazione custom completata con successo");
            return true;
            
        } catch (Exception e) {
            System.out.println("✗ Errore validazione custom: " + e.getMessage());
            return false;
        }
    }
    
    private boolean isSelfSigned(X509Certificate cert) {
        return cert.getIssuerX500Principal().equals(cert.getSubjectX500Principal());
    }
    
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        // Combina issuer default con quelli custom
        X509Certificate[] defaultIssuers = defaultTrustManager.getAcceptedIssuers();
        
        // In un'implementazione reale, potresti aggiungere 
        // certificati CA custom qui
        
        return defaultIssuers;
    }
    
    // Metodo helper per creare SSLContext con questo TrustManager
    public static SSLContext createSSLContextWithCustomTrust(boolean allowSelfSigned) 
            throws Exception {
        
        CustomTrustManager customTM = new CustomTrustManager(allowSelfSigned);
        
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{customTM}, new java.security.SecureRandom());
        
        return sslContext;
    }
    
    // Test del Custom TrustManager
    public static void main(String[] args) {
        try {
            System.out.println("Test Custom TrustManager");
            
            SSLContext sslContext = createSSLContextWithCustomTrust(true);
            SSLSocketFactory factory = sslContext.getSocketFactory();
            
            // Test connessione (sostituisci con server reale)
            try (SSLSocket socket = (SSLSocket) factory.createSocket("localhost", 8443)) {
                socket.startHandshake();
                System.out.println("Connessione SSL stabilita con Custom TrustManager");
                
            } catch (Exception e) {
                System.out.println("Connessione fallita: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Errore: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

## Best Practices SSL/TLS

### Configurazione Sicura
```java
public class SSLBestPractices {
    
    // ✅ Protocolli sicuri
    public static final String[] SECURE_PROTOCOLS = {
        "TLSv1.3", "TLSv1.2"
    };
    
    // ✅ Cipher suites raccomandate
    public static final String[] SECURE_CIPHER_SUITES = {
        // TLS 1.3
        "TLS_AES_256_GCM_SHA384",
        "TLS_CHACHA20_POLY1305_SHA256",
        "TLS_AES_128_GCM_SHA256",
        
        // TLS 1.2 con PFS
        "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
        "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256",
        "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"
    };
    
    // ❌ Da evitare
    public static final String[] INSECURE_PROTOCOLS = {
        "SSLv3", "TLSv1", "TLSv1.1"
    };
    
    public static void configureSecureSSL() {
        // Disabilita protocolli insicuri
        System.setProperty("jdk.tls.disabledAlgorithms", 
            "SSLv3, TLSv1, TLSv1.1, RC4, MD5withRSA, DH keySize < 2048");
        
        // Abilita solo protocolli sicuri
        System.setProperty("https.protocols", "TLSv1.3,TLSv1.2");
        
        // Session timeout ragionevole
        System.setProperty("jdk.tls.server.defaultSessionTimeout", "3600");
        
        // Dimensioni cache sessioni
        System.setProperty("jdk.tls.server.sessionCacheSize", "10000");
    }
}
```

---
[⬅️ Lezione Precedente](01-Fondamenti-SSL-TLS.md) | [🏠 Torna al Modulo](../README.md) | [➡️ Prossima Lezione](03-Sicurezza-Avanzata.md)