# 3. Sicurezza Avanzata e Best Practices

## Introduzione
Questa guida copre **aspetti avanzati della sicurezza SSL/TLS**: certificate pinning, OCSP, gestione sessioni, protezione contro attacchi comuni e configurazioni enterprise-grade per applicazioni mission-critical.

## Certificate Pinning

### Concetti Base
Il **Certificate Pinning** è una tecnica di sicurezza che "blocca" l'applicazione su certificati specifici, prevenendo attacchi man-in-the-middle anche se un'autorità di certificazione (CA) viene compromessa.

#### Tipi di Pinning
```java
/*
 * TIPI DI CERTIFICATE PINNING:
 * 
 * 1. Certificate Pinning - Pin su certificato completo
 * 2. Public Key Pinning - Pin su chiave pubblica  
 * 3. CA Pinning - Pin su CA specifica
 * 4. SPKI Pinning - Pin su Subject Public Key Info hash
 */

public enum PinningType {
    CERTIFICATE,    // Pin certificato completo (fragile - cambio = rottura)
    PUBLIC_KEY,     // Pin chiave pubblica (più flessibile)
    CA_CERTIFICATE, // Pin CA (più flessibile per certificati multipli)
    SPKI_HASH      // Pin hash SPKI (raccomandato)
}
```

### Implementazione Certificate Pinning

#### CertificatePinningTrustManager.java
```java
import javax.net.ssl.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;

public class CertificatePinningTrustManager implements X509TrustManager {
    
    private final X509TrustManager defaultTrustManager;
    private final Map<String, Set<String>> pinnedCertificates;
    private final Map<String, Set<String>> pinnedPublicKeys;
    private final Map<String, Set<String>> pinnedSPKIHashes;
    
    public CertificatePinningTrustManager() throws Exception {
        // Inizializza default trust manager
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init((KeyStore) null);
        this.defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        
        // Inizializza strutture dati pinning
        this.pinnedCertificates = new HashMap<>();
        this.pinnedPublicKeys = new HashMap<>();
        this.pinnedSPKIHashes = new HashMap<>();
        
        // Configura pin per domini specifici
        configurePins();
    }
    
    private void configurePins() {
        try {
            // Pin per Google (esempio)
            addSPKIPin("www.google.com", 
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", // Primary
                "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="  // Backup
            );
            
            // Pin per GitHub (esempio)  
            addSPKIPin("api.github.com",
                "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC=",
                "DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD="
            );
            
            // Pin per dominio locale (per testing)
            addPublicKeyPin("localhost", getLocalHostPublicKeyHash());
            
        } catch (Exception e) {
            System.err.println("Errore configurazione certificate pinning: " + e.getMessage());
        }
    }
    
    public void addSPKIPin(String hostname, String... spkiHashes) {
        pinnedSPKIHashes.put(hostname.toLowerCase(), new HashSet<>(Arrays.asList(spkiHashes)));
    }
    
    public void addPublicKeyPin(String hostname, String publicKeyHash) {
        pinnedPublicKeys.computeIfAbsent(hostname.toLowerCase(), k -> new HashSet<>())
                       .add(publicKeyHash);
    }
    
    public void addCertificatePin(String hostname, String certificateHash) {
        pinnedCertificates.computeIfAbsent(hostname.toLowerCase(), k -> new HashSet<>())
                          .add(certificateHash);
    }
    
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) 
            throws CertificateException {
        
        // 1. Validazione standard
        defaultTrustManager.checkServerTrusted(chain, authType);
        
        // 2. Verifica certificate pinning
        String hostname = getCurrentHostname(); // Implementare secondo il contesto
        if (hostname != null && hasPinsForHost(hostname)) {
            validatePinning(hostname, chain);
        }
    }
    
    private void validatePinning(String hostname, X509Certificate[] chain) 
            throws CertificateException {
        
        hostname = hostname.toLowerCase();
        X509Certificate leafCert = chain[0];
        
        System.out.println("Validazione pinning per: " + hostname);
        
        try {
            // 1. Controlla SPKI hash pinning (raccomandato)
            if (pinnedSPKIHashes.containsKey(hostname)) {
                if (validateSPKIPinning(hostname, chain)) {
                    System.out.println("✓ SPKI pinning validato");
                    return;
                }
            }
            
            // 2. Controlla public key pinning
            if (pinnedPublicKeys.containsKey(hostname)) {
                if (validatePublicKeyPinning(hostname, leafCert)) {
                    System.out.println("✓ Public key pinning validato");
                    return;
                }
            }
            
            // 3. Controlla certificate pinning
            if (pinnedCertificates.containsKey(hostname)) {
                if (validateCertificatePinning(hostname, leafCert)) {
                    System.out.println("✓ Certificate pinning validato");
                    return;
                }
            }
            
            // Se arriviamo qui, il pinning è fallito
            throw new CertificateException(
                "Certificate pinning validation failed for: " + hostname);
                
        } catch (Exception e) {
            throw new CertificateException("Pinning validation error", e);
        }
    }
    
    private boolean validateSPKIPinning(String hostname, X509Certificate[] chain) 
            throws Exception {
        
        Set<String> pinnedHashes = pinnedSPKIHashes.get(hostname);
        
        // Verifica ogni certificato nella catena
        for (X509Certificate cert : chain) {
            String spkiHash = calculateSPKIHash(cert);
            
            if (pinnedHashes.contains(spkiHash)) {
                System.out.println("SPKI hash match trovato: " + spkiHash);
                return true;
            }
        }
        
        System.out.println("Nessun SPKI hash match per " + hostname);
        System.out.println("Hash attesi: " + pinnedHashes);
        System.out.println("Hash ricevuti: " + getSPKIHashesFromChain(chain));
        
        return false;
    }
    
    private boolean validatePublicKeyPinning(String hostname, X509Certificate cert) 
            throws Exception {
        
        Set<String> pinnedKeys = pinnedPublicKeys.get(hostname);
        String publicKeyHash = calculatePublicKeyHash(cert);
        
        boolean match = pinnedKeys.contains(publicKeyHash);
        System.out.println("Public key hash: " + publicKeyHash + " - Match: " + match);
        
        return match;
    }
    
    private boolean validateCertificatePinning(String hostname, X509Certificate cert) 
            throws Exception {
        
        Set<String> pinnedCerts = pinnedCertificates.get(hostname);
        String certHash = calculateCertificateHash(cert);
        
        boolean match = pinnedCerts.contains(certHash);
        System.out.println("Certificate hash: " + certHash + " - Match: " + match);
        
        return match;
    }
    
    private String calculateSPKIHash(X509Certificate cert) throws Exception {
        // SPKI (Subject Public Key Info) hash - raccomandato per pinning
        byte[] spkiBytes = cert.getPublicKey().getEncoded();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(spkiBytes);
        return Base64.getEncoder().encodeToString(hash);
    }
    
    private String calculatePublicKeyHash(X509Certificate cert) throws Exception {
        byte[] publicKeyBytes = cert.getPublicKey().getEncoded();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(publicKeyBytes);
        return Base64.getEncoder().encodeToString(hash);
    }
    
    private String calculateCertificateHash(X509Certificate cert) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(cert.getEncoded());
        return Base64.getEncoder().encodeToString(hash);
    }
    
    private Set<String> getSPKIHashesFromChain(X509Certificate[] chain) {
        return Arrays.stream(chain)
                     .map(cert -> {
                         try {
                             return calculateSPKIHash(cert);
                         } catch (Exception e) {
                             return "ERROR: " + e.getMessage();
                         }
                     })
                     .collect(java.util.stream.Collectors.toSet());
    }
    
    private boolean hasPinsForHost(String hostname) {
        hostname = hostname.toLowerCase();
        return pinnedSPKIHashes.containsKey(hostname) ||
               pinnedPublicKeys.containsKey(hostname) ||
               pinnedCertificates.containsKey(hostname);
    }
    
    private String getCurrentHostname() {
        // In una implementazione reale, questo dovrebbe essere 
        // passato dal contesto della connessione SSL
        // Per ora restituiamo null per disabilitare pinning generico
        return null;
    }
    
    private String getLocalHostPublicKeyHash() throws Exception {
        // Per testing locale - calcola hash chiave pubblica localhost
        // In produzione, usa hash reali dei tuoi certificati
        return "LOCAL_HOST_PUBLIC_KEY_HASH_PLACEHOLDER";
    }
    
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) 
            throws CertificateException {
        defaultTrustManager.checkClientTrusted(chain, authType);
    }
    
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return defaultTrustManager.getAcceptedIssuers();
    }
}
```

### Pinning Helper Utility

#### CertificatePinningUtils.java
```java
import java.security.cert.X509Certificate;
import java.security.MessageDigest;
import java.util.Base64;
import javax.net.ssl.*;
import java.io.*;
import java.net.*;

public class CertificatePinningUtils {
    
    /**
     * Utility per estrarre hash SPKI da un server remoto
     * Utile per configurare il pinning iniziale
     */
    public static void extractSPKIHashes(String hostname, int port) {
        System.out.println("=== ESTRAZIONE SPKI HASHES ===");
        System.out.println("Host: " + hostname + ":" + port);
        
        try {
            // Crea connessione SSL temporanea (bypass validazione)
            SSLContext sc = createTrustAllContext();
            SSLSocketFactory factory = sc.getSocketFactory();
            
            try (SSLSocket socket = (SSLSocket) factory.createSocket(hostname, port)) {
                socket.startHandshake();
                
                // Ottieni catena certificati
                SSLSession session = socket.getSession();
                java.security.cert.Certificate[] certs = session.getPeerCertificates();
                
                System.out.println("Certificati trovati: " + certs.length);
                
                for (int i = 0; i < certs.length; i++) {
                    X509Certificate cert = (X509Certificate) certs[i];
                    String spkiHash = calculateSPKIHash(cert);
                    
                    System.out.println("\nCertificato " + (i + 1) + ":");
                    System.out.println("  Subject: " + cert.getSubjectX500Principal());
                    System.out.println("  SPKI Hash: " + spkiHash);
                    
                    if (i < 2) { // Mostra solo primi 2 per il pinning
                        System.out.println("  // Aggiungi al pinning:");
                        System.out.println("  addSPKIPin(\"" + hostname + "\", \"" + spkiHash + "\");");
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Errore estrazione hash: " + e.getMessage());
        }
    }
    
    /**
     * Valida configurazione pinning contro server reale
     */
    public static boolean validatePinConfiguration(String hostname, int port, 
            String expectedSPKIHash) {
        
        System.out.println("=== VALIDAZIONE PINNING ===");
        System.out.println("Host: " + hostname + ":" + port);
        System.out.println("Hash atteso: " + expectedSPKIHash);
        
        try {
            SSLContext sc = createTrustAllContext();
            SSLSocketFactory factory = sc.getSocketFactory();
            
            try (SSLSocket socket = (SSLSocket) factory.createSocket(hostname, port)) {
                socket.startHandshake();
                
                java.security.cert.Certificate[] certs = 
                    socket.getSession().getPeerCertificates();
                
                for (java.security.cert.Certificate cert : certs) {
                    X509Certificate x509 = (X509Certificate) cert;
                    String actualHash = calculateSPKIHash(x509);
                    
                    if (expectedSPKIHash.equals(actualHash)) {
                        System.out.println("✓ Hash SPKI match trovato!");
                        System.out.println("  Certificato: " + x509.getSubjectX500Principal());
                        return true;
                    }
                }
                
                System.out.println("✗ Nessun hash match trovato");
                return false;
                
            }
            
        } catch (Exception e) {
            System.err.println("Errore validazione: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Genera configurazione pinning Java per un host
     */
    public static void generatePinningCode(String hostname, int port) {
        System.out.println("=== GENERAZIONE CODICE PINNING ===");
        
        try {
            SSLContext sc = createTrustAllContext();
            SSLSocketFactory factory = sc.getSocketFactory();
            
            try (SSLSocket socket = (SSLSocket) factory.createSocket(hostname, port)) {
                socket.startHandshake();
                
                java.security.cert.Certificate[] certs = 
                    socket.getSession().getPeerCertificates();
                
                System.out.println("// Certificate Pinning per " + hostname);
                System.out.println("// Generato: " + new java.util.Date());
                System.out.println();
                
                // Leaf certificate (certificato server)
                if (certs.length > 0) {
                    X509Certificate leaf = (X509Certificate) certs[0];
                    String leafHash = calculateSPKIHash(leaf);
                    
                    System.out.println("// Pin certificato leaf (primario)");
                    System.out.println("addSPKIPin(\"" + hostname + "\", \"" + leafHash + "\");");
                }
                
                // Intermediate certificate (backup)
                if (certs.length > 1) {
                    X509Certificate intermediate = (X509Certificate) certs[1];
                    String intHash = calculateSPKIHash(intermediate);
                    
                    System.out.println("// Pin certificato intermediate (backup)");
                    System.out.println("addSPKIPin(\"" + hostname + "\", \"" + intHash + "\");");
                }
                
                System.out.println();
                System.out.println("// Configurazione completa:");
                System.out.println("pinnedSPKIHashes.put(\"" + hostname + "\", Set.of(");
                
                for (int i = 0; i < Math.min(certs.length, 2); i++) {
                    X509Certificate cert = (X509Certificate) certs[i];
                    String hash = calculateSPKIHash(cert);
                    String comment = (i == 0) ? "primary" : "backup";
                    
                    System.out.print("    \"" + hash + "\"" + 
                        (i < Math.min(certs.length, 2) - 1 ? "," : "") + 
                        " // " + comment);
                    
                    if (i < Math.min(certs.length, 2) - 1) System.out.println();
                }
                
                System.out.println("\n));");
            }
            
        } catch (Exception e) {
            System.err.println("Errore generazione codice: " + e.getMessage());
        }
    }
    
    private static String calculateSPKIHash(X509Certificate cert) throws Exception {
        byte[] spkiBytes = cert.getPublicKey().getEncoded();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(spkiBytes);
        return Base64.getEncoder().encodeToString(hash);
    }
    
    private static SSLContext createTrustAllContext() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {}
            }
        };
        
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        return sc;
    }
    
    // Test utility
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java CertificatePinningUtils <hostname> [port]");
            System.out.println("Esempi:");
            System.out.println("  java CertificatePinningUtils www.google.com");
            System.out.println("  java CertificatePinningUtils github.com 443");
            return;
        }
        
        String hostname = args[0];
        int port = (args.length > 1) ? Integer.parseInt(args[1]) : 443;
        
        extractSPKIHashes(hostname, port);
        System.out.println();
        generatePinningCode(hostname, port);
    }
}
```

## OCSP (Online Certificate Status Protocol)

### Implementazione OCSP Stapling

#### OCSPValidator.java
```java
import java.security.cert.*;
import java.security.cert.Certificate;
import java.net.*;
import java.io.*;
import java.util.*;
import javax.net.ssl.*;

/**
 * Validatore OCSP per verificare status certificati in tempo reale
 */
public class OCSPValidator {
    
    private static final int OCSP_TIMEOUT_MS = 5000;
    
    /**
     * Verifica status certificato via OCSP
     */
    public static OCSPStatus validateCertificate(X509Certificate cert, 
            X509Certificate issuerCert) {
        
        try {
            // 1. Estrai URL OCSP dal certificato
            String ocspUrl = extractOCSPUrl(cert);
            if (ocspUrl == null) {
                return new OCSPStatus(OCSPStatusType.NO_OCSP_URL, 
                    "Nessun URL OCSP trovato nel certificato");
            }
            
            System.out.println("URL OCSP: " + ocspUrl);
            
            // 2. Crea richiesta OCSP
            byte[] ocspRequest = createOCSPRequest(cert, issuerCert);
            
            // 3. Invia richiesta OCSP
            byte[] ocspResponse = sendOCSPRequest(ocspUrl, ocspRequest);
            
            // 4. Valida risposta OCSP
            return validateOCSPResponse(ocspResponse, cert);
            
        } catch (Exception e) {
            return new OCSPStatus(OCSPStatusType.ERROR, 
                "Errore validazione OCSP: " + e.getMessage());
        }
    }
    
    private static String extractOCSPUrl(X509Certificate cert) {
        try {
            // Cerca extension Authority Info Access (AIA)
            byte[] aiaBytes = cert.getExtensionValue("1.3.6.1.5.5.7.1.1");
            if (aiaBytes == null) {
                return null;
            }
            
            // Parse ASN.1 per estrarre URL OCSP
            // Implementazione semplificata - in produzione usa libreria ASN.1
            String aiaString = new String(aiaBytes);
            
            // Cerca pattern URL OCSP comune
            if (aiaString.contains("http://") || aiaString.contains("https://")) {
                // Estrazione euristica dell'URL
                // In produzione, usa parser ASN.1 appropriato
                return extractUrlFromAIA(aiaBytes);
            }
            
            return null;
            
        } catch (Exception e) {
            System.err.println("Errore estrazione URL OCSP: " + e.getMessage());
            return null;
        }
    }
    
    private static String extractUrlFromAIA(byte[] aiaBytes) {
        // Implementazione semplificata per demo
        // In produzione, usa BouncyCastle o altra libreria ASN.1
        
        String data = new String(aiaBytes, java.nio.charset.StandardCharsets.ISO_8859_1);
        
        // Cerca pattern URL comuni
        String[] patterns = {"http://", "https://"};
        
        for (String pattern : patterns) {
            int start = data.indexOf(pattern);
            if (start >= 0) {
                int end = data.indexOf('\0', start);
                if (end < 0) end = data.length();
                
                String url = data.substring(start, end).trim();
                if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
                
                return url;
            }
        }
        
        return null;
    }
    
    private static byte[] createOCSPRequest(X509Certificate cert, X509Certificate issuer) 
            throws Exception {
        
        // Per implementazione completa OCSP, serve libreria come BouncyCastle
        // Questo è uno stub che mostra la struttura
        
        /*
         * OCSP Request structure (ASN.1 DER):
         * 
         * OCSPRequest ::= SEQUENCE {
         *     tbsRequest      TBSRequest,
         *     optionalSignature   [0] EXPLICIT Signature OPTIONAL
         * }
         * 
         * TBSRequest ::= SEQUENCE {
         *     version         [0] EXPLICIT Version DEFAULT v1,
         *     requestorName   [1] EXPLICIT GeneralName OPTIONAL,
         *     requestList     SEQUENCE OF Request,
         *     requestExtensions [2] EXPLICIT Extensions OPTIONAL
         * }
         */
        
        throw new UnsupportedOperationException(
            "Implementazione OCSP completa richiede BouncyCastle. " +
            "Questo è un esempio strutturale.");
    }
    
    private static byte[] sendOCSPRequest(String ocspUrl, byte[] request) throws Exception {
        
        URL url = new URL(ocspUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            // Configura connessione
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(OCSP_TIMEOUT_MS);
            connection.setReadTimeout(OCSP_TIMEOUT_MS);
            connection.setRequestProperty("Content-Type", "application/ocsp-request");
            connection.setRequestProperty("Accept", "application/ocsp-response");
            
            // Invia richiesta
            try (OutputStream out = connection.getOutputStream()) {
                out.write(request);
                out.flush();
            }
            
            // Leggi risposta
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("OCSP server error: " + responseCode);
            }
            
            try (InputStream in = connection.getInputStream()) {
                return readAllBytes(in);
            }
            
        } finally {
            connection.disconnect();
        }
    }
    
    private static OCSPStatus validateOCSPResponse(byte[] response, X509Certificate cert) {
        
        try {
            // Parse risposta OCSP
            // Implementazione semplificata - in produzione usa BouncyCastle
            
            /*
             * OCSPResponse structure:
             * 
             * OCSPResponse ::= SEQUENCE {
             *     responseStatus  OCSPResponseStatus,
             *     responseBytes   [0] EXPLICIT ResponseBytes OPTIONAL
             * }
             * 
             * ResponseBytes ::= SEQUENCE {
             *     responseType    OBJECT IDENTIFIER,
             *     response        OCTET STRING
             * }
             */
            
            // Per demo, assumiamo certificato valido
            System.out.println("Risposta OCSP ricevuta: " + response.length + " bytes");
            
            return new OCSPStatus(OCSPStatusType.GOOD, 
                "Certificato valido (simulato - implementare parsing OCSP)");
                
        } catch (Exception e) {
            return new OCSPStatus(OCSPStatusType.ERROR, 
                "Errore parsing risposta OCSP: " + e.getMessage());
        }
    }
    
    private static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int bytesRead;
        
        while ((bytesRead = in.read(data)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        
        return buffer.toByteArray();
    }
    
    // Classe per risultato validazione OCSP
    public static class OCSPStatus {
        public final OCSPStatusType status;
        public final String message;
        public final Date validationTime;
        
        public OCSPStatus(OCSPStatusType status, String message) {
            this.status = status;
            this.message = message;
            this.validationTime = new Date();
        }
        
        @Override
        public String toString() {
            return String.format("OCSP Status: %s - %s (validato: %s)", 
                status, message, validationTime);
        }
    }
    
    public enum OCSPStatusType {
        GOOD,           // Certificato valido
        REVOKED,        // Certificato revocato
        UNKNOWN,        // Status sconosciuto
        NO_OCSP_URL,    // Nessun URL OCSP nel certificato
        ERROR           // Errore durante validazione
    }
    
    // Test validazione OCSP
    public static void main(String[] args) {
        try {
            // Carica certificato da testare
            // In produzione, usa certificato reale
            
            System.out.println("=== TEST VALIDAZIONE OCSP ===");
            System.out.println("NOTA: Implementazione demo - serve BouncyCastle per OCSP completo");
            
            // Per test completo:
            // 1. Carica certificato X.509
            // 2. Trova certificato issuer
            // 3. Esegui validazione OCSP
            // 4. Mostra risultato
            
        } catch (Exception e) {
            System.err.println("Errore test OCSP: " + e.getMessage());
        }
    }
}
```

## Gestione Sicura delle Sessioni SSL

### SSL Session Manager

#### SSLSessionManager.java
```java
import javax.net.ssl.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Gestore avanzato delle sessioni SSL per performance e sicurezza ottimali
 */
public class SSLSessionManager {
    
    private final Map<String, SessionInfo> activeSessions;
    private final ScheduledExecutorService cleanupExecutor;
    private final int maxSessions;
    private final int sessionTimeoutSeconds;
    
    public SSLSessionManager(int maxSessions, int sessionTimeoutSeconds) {
        this.maxSessions = maxSessions;
        this.sessionTimeoutSeconds = sessionTimeoutSeconds;
        this.activeSessions = new ConcurrentHashMap<>();
        
        // Scheduler per pulizia sessioni scadute
        this.cleanupExecutor = Executors.newScheduledThreadPool(1);
        this.cleanupExecutor.scheduleAtFixedRate(
            this::cleanupExpiredSessions, 60, 60, TimeUnit.SECONDS);
    }
    
    /**
     * Configura SSL context con gestione sessioni ottimizzata
     */
    public static SSLContext createOptimizedSSLContext() throws Exception {
        
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, null, new java.security.SecureRandom());
        
        // Configura parametri sessione server
        SSLSessionContext serverSessionCtx = sslContext.getServerSessionContext();
        serverSessionCtx.setSessionCacheSize(10000);    // 10k sessioni
        serverSessionCtx.setSessionTimeout(3600);       // 1 ora
        
        // Configura parametri sessione client  
        SSLSessionContext clientSessionCtx = sslContext.getClientSessionContext();
        clientSessionCtx.setSessionCacheSize(1000);     // 1k sessioni
        clientSessionCtx.setSessionTimeout(1800);       // 30 minuti
        
        return sslContext;
    }
    
    /**
     * Registra nuova sessione SSL
     */
    public void registerSession(SSLSession session) {
        
        String sessionId = bytesToHex(session.getId());
        SessionInfo info = new SessionInfo(session);
        
        synchronized (activeSessions) {
            // Controlla limite massimo sessioni
            if (activeSessions.size() >= maxSessions) {
                evictOldestSession();
            }
            
            activeSessions.put(sessionId, info);
        }
        
        System.out.println("Sessione registrata: " + sessionId + 
                          " (totali: " + activeSessions.size() + ")");
    }
    
    /**
     * Ottieni informazioni sessione
     */
    public SessionInfo getSessionInfo(String sessionId) {
        SessionInfo info = activeSessions.get(sessionId);
        
        if (info != null && !info.isExpired(sessionTimeoutSeconds)) {
            info.updateLastAccess();
            return info;
        }
        
        return null;
    }
    
    /**
     * Invalida sessione specifica
     */
    public boolean invalidateSession(String sessionId) {
        SessionInfo removed = activeSessions.remove(sessionId);
        
        if (removed != null) {
            try {
                removed.session.invalidate();
                System.out.println("Sessione invalidata: " + sessionId);
                return true;
            } catch (Exception e) {
                System.err.println("Errore invalidazione sessione: " + e.getMessage());
            }
        }
        
        return false;
    }
    
    /**
     * Pulizia sessioni scadute
     */
    private void cleanupExpiredSessions() {
        
        int initialCount = activeSessions.size();
        long currentTime = System.currentTimeMillis();
        
        activeSessions.entrySet().removeIf(entry -> {
            SessionInfo info = entry.getValue();
            if (info.isExpired(sessionTimeoutSeconds)) {
                try {
                    info.session.invalidate();
                } catch (Exception e) {
                    // Ignora errori invalidazione (sessione già scaduta)
                }
                return true;
            }
            return false;
        });
        
        int cleanedCount = initialCount - activeSessions.size();
        if (cleanedCount > 0) {
            System.out.println("Pulizia sessioni: " + cleanedCount + 
                              " sessioni scadute rimosse");
        }
    }
    
    /**
     * Rimuove sessione più vecchia quando si raggiunge il limite
     */
    private void evictOldestSession() {
        
        String oldestSessionId = null;
        long oldestTime = Long.MAX_VALUE;
        
        for (Map.Entry<String, SessionInfo> entry : activeSessions.entrySet()) {
            SessionInfo info = entry.getValue();
            if (info.creationTime < oldestTime) {
                oldestTime = info.creationTime;
                oldestSessionId = entry.getKey();
            }
        }
        
        if (oldestSessionId != null) {
            invalidateSession(oldestSessionId);
            System.out.println("Sessione più vecchia rimossa per limite: " + oldestSessionId);
        }
    }
    
    /**
     * Statistiche sessioni attive
     */
    public SessionStatistics getStatistics() {
        
        int totalSessions = activeSessions.size();
        int tls13Sessions = 0;
        int tls12Sessions = 0;
        long oldestSession = Long.MAX_VALUE;
        long newestSession = Long.MIN_VALUE;
        
        for (SessionInfo info : activeSessions.values()) {
            String protocol = info.session.getProtocol();
            
            if ("TLSv1.3".equals(protocol)) {
                tls13Sessions++;
            } else if ("TLSv1.2".equals(protocol)) {
                tls12Sessions++;
            }
            
            if (info.creationTime < oldestSession) {
                oldestSession = info.creationTime;
            }
            if (info.creationTime > newestSession) {
                newestSession = info.creationTime;
            }
        }
        
        return new SessionStatistics(totalSessions, tls13Sessions, tls12Sessions,
                                   oldestSession, newestSession);
    }
    
    /**
     * Shutdown gestore sessioni
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
        }
        
        // Invalida tutte le sessioni attive
        for (SessionInfo info : activeSessions.values()) {
            try {
                info.session.invalidate();
            } catch (Exception e) {
                // Ignora errori
            }
        }
        
        activeSessions.clear();
        System.out.println("SSL Session Manager shutdown completato");
    }
    
    // Helper methods
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
    
    // Classe per informazioni sessione
    public static class SessionInfo {
        public final SSLSession session;
        public final long creationTime;
        public volatile long lastAccessTime;
        
        public SessionInfo(SSLSession session) {
            this.session = session;
            this.creationTime = System.currentTimeMillis();
            this.lastAccessTime = creationTime;
        }
        
        public void updateLastAccess() {
            this.lastAccessTime = System.currentTimeMillis();
        }
        
        public boolean isExpired(int timeoutSeconds) {
            long timeout = timeoutSeconds * 1000L;
            return (System.currentTimeMillis() - lastAccessTime) > timeout;
        }
        
        public long getAgeSeconds() {
            return (System.currentTimeMillis() - creationTime) / 1000;
        }
    }
    
    // Classe per statistiche
    public static class SessionStatistics {
        public final int totalSessions;
        public final int tls13Sessions;
        public final int tls12Sessions;
        public final long oldestSessionAge;
        public final long newestSessionAge;
        
        public SessionStatistics(int total, int tls13, int tls12, 
                               long oldest, long newest) {
            this.totalSessions = total;
            this.tls13Sessions = tls13;
            this.tls12Sessions = tls12;
            
            long currentTime = System.currentTimeMillis();
            this.oldestSessionAge = (oldest != Long.MAX_VALUE) ? 
                (currentTime - oldest) / 1000 : 0;
            this.newestSessionAge = (newest != Long.MIN_VALUE) ? 
                (currentTime - newest) / 1000 : 0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "SSL Sessions: %d totali (TLS 1.3: %d, TLS 1.2: %d) " +
                "- Età: %d-%d secondi",
                totalSessions, tls13Sessions, tls12Sessions,
                newestSessionAge, oldestSessionAge);
        }
    }
    
    // Test session manager
    public static void main(String[] args) throws Exception {
        
        SSLSessionManager manager = new SSLSessionManager(1000, 300); // 5 min timeout
        
        // Simula creazione sessioni
        SSLContext sslContext = createOptimizedSSLContext();
        
        System.out.println("SSL Session Manager configurato:");
        System.out.println("- Max sessioni: 1000");
        System.out.println("- Timeout: 300 secondi");
        System.out.println("- Cleanup automatico: ogni 60 secondi");
        
        // In produzione, registrare sessioni reali tramite callback
        Runtime.getRuntime().addShutdownHook(new Thread(manager::shutdown));
    }
}
```

## Protezione contro Attacchi SSL

### SSL Attack Prevention

#### SSLAttackPrevention.java
```java
import javax.net.ssl.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementa protezioni contro attacchi comuni SSL/TLS
 */
public class SSLAttackPrevention {
    
    // Contatori per rate limiting
    private static final Map<String, AtomicInteger> connectionCounts = new ConcurrentHashMap<>();
    private static final Map<String, Long> lastConnectionTime = new ConcurrentHashMap<>();
    
    /**
     * Configura SSL context con protezioni anti-attacco
     */
    public static SSLContext createHardenedSSLContext() throws Exception {
        
        SSLContext context = SSLContext.getInstance("TLS");
        
        // Usa SecureRandom forte per prevenire attacchi prediction
        SecureRandom secureRandom = SecureRandom.getInstanceStrong();
        
        // Inizializza con default managers sicuri
        context.init(null, null, secureRandom);
        
        return context;
    }
    
    /**
     * Configura SSLSocket con protezioni complete
     */
    public static void hardenSSLSocket(SSLSocket socket) {
        
        // 1. PROTOCOLLI SICURI SOLAMENTE
        socket.setEnabledProtocols(new String[]{"TLSv1.3", "TLSv1.2"});
        
        // 2. CIPHER SUITES SICURE (no export, no NULL, no anon, no RC4)
        String[] secureCiphers = getSecureCipherSuites(socket.getSupportedCipherSuites());
        socket.setEnabledCipherSuites(secureCiphers);
        
        // 3. PERFECT FORWARD SECRECY OBBLIGATORIO
        // Le cipher suites sopra garantiscono PFS
        
        // 4. COMPRESSIONE DISABILITATA (previene CRIME attack)
        // Java disabilita compressione SSL per default
        
        // 5. RENEGOTIATION SICURA
        // Java gestisce renegotiation sicura automaticamente
        
        System.out.println("SSLSocket configurato con protezioni sicurezza:");
        System.out.println("- Protocolli: " + Arrays.toString(socket.getEnabledProtocols()));
        System.out.println("- Cipher suites: " + secureCiphers.length + " sicure su " + 
                          socket.getSupportedCipherSuites().length + " totali");
    }
    
    /**
     * Filtra cipher suites per mantenere solo quelle sicure
     */
    private static String[] getSecureCipherSuites(String[] supportedCiphers) {
        
        List<String> secureCiphers = new ArrayList<>();
        
        // Cipher suites sicure in ordine di preferenza
        String[] preferredCiphers = {
            // TLS 1.3 (sempre sicure)
            "TLS_AES_256_GCM_SHA384",
            "TLS_CHACHA20_POLY1305_SHA256",
            "TLS_AES_128_GCM_SHA256",
            
            // TLS 1.2 con Perfect Forward Secrecy
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
            
            // DHE per compatibility (meno preferite di ECDHE)
            "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256"
        };
        
        Set<String> supported = new HashSet<>(Arrays.asList(supportedCiphers));
        
        // Aggiungi cipher preferite se supportate
        for (String cipher : preferredCiphers) {
            if (supported.contains(cipher)) {
                secureCiphers.add(cipher);
            }
        }
        
        // Se nessuna cipher preferita disponibile, usa fallback sicuri
        if (secureCiphers.isEmpty()) {
            for (String cipher : supportedCiphers) {
                if (isSecureCipher(cipher)) {
                    secureCiphers.add(cipher);
                }
            }
        }
        
        return secureCiphers.toArray(new String[0]);
    }
    
    /**
     * Verifica se cipher suite è sicura
     */
    private static boolean isSecureCipher(String cipher) {
        
        // Blacklist cipher insicure
        String[] insecurePatterns = {
            "_NULL_",           // No encryption
            "_EXPORT_",         // Export grade (weak)
            "_DES_",           // DES (broken)
            "_3DES_",          // 3DES (deprecated)
            "_RC4_",           // RC4 (broken)
            "_MD5",            // MD5 hash (broken)
            "_anon_",          // Anonymous (no authentication)
            "TLS_RSA_",        // No forward secrecy
            "_SHA$"            // SHA-1 (deprecated)
        };
        
        String upperCipher = cipher.toUpperCase();
        
        for (String pattern : insecurePatterns) {
            if (upperCipher.contains(pattern.toUpperCase())) {
                return false;
            }
        }
        
        // Whitelist pattern sicuri
        return upperCipher.contains("_ECDHE_") || 
               upperCipher.contains("_DHE_") ||
               upperCipher.startsWith("TLS_AES_") ||
               upperCipher.startsWith("TLS_CHACHA20_");
    }
    
    /**
     * Rate limiting per prevenire DoS attacks
     */
    public static boolean checkRateLimit(String clientIP) {
        
        final int MAX_CONNECTIONS_PER_MINUTE = 60;
        final long WINDOW_MS = 60000; // 1 minuto
        
        long currentTime = System.currentTimeMillis();
        
        // Pulisci connessioni vecchie
        lastConnectionTime.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue()) > WINDOW_MS);
        connectionCounts.entrySet().removeIf(entry -> 
            !lastConnectionTime.containsKey(entry.getKey()));
        
        // Controlla rate limit per questo IP
        AtomicInteger count = connectionCounts.computeIfAbsent(clientIP, 
            k -> new AtomicInteger(0));
        
        Long lastTime = lastConnectionTime.get(clientIP);
        
        if (lastTime == null || (currentTime - lastTime) > WINDOW_MS) {
            // Nuova finestra temporale
            count.set(1);
            lastConnectionTime.put(clientIP, currentTime);
            return true;
        }
        
        // Incrementa contatore
        int currentCount = count.incrementAndGet();
        
        if (currentCount > MAX_CONNECTIONS_PER_MINUTE) {
            System.out.println("Rate limit exceeded for IP: " + clientIP + 
                              " (" + currentCount + " connections)");
            return false;
        }
        
        return true;
    }
    
    /**
     * Validazione hostname per prevenire certificate mismatch
     */
    public static boolean validateHostname(String expectedHostname, SSLSession session) {
        
        try {
            // Ottieni certificato server
            java.security.cert.Certificate[] certs = session.getPeerCertificates();
            if (certs.length == 0) {
                return false;
            }
            
            java.security.cert.X509Certificate serverCert = 
                (java.security.cert.X509Certificate) certs[0];
            
            // Verifica Common Name
            String cn = extractCN(serverCert.getSubjectX500Principal().getName());
            if (expectedHostname.equalsIgnoreCase(cn)) {
                return true;
            }
            
            // Verifica Subject Alternative Names
            Collection<List<?>> subjectAltNames = serverCert.getSubjectAlternativeNames();
            if (subjectAltNames != null) {
                for (List<?> altName : subjectAltNames) {
                    if (altName.size() >= 2) {
                        Integer type = (Integer) altName.get(0);
                        String name = (String) altName.get(1);
                        
                        // Type 2 = DNS name
                        if (type == 2 && matchesHostname(expectedHostname, name)) {
                            return true;
                        }
                    }
                }
            }
            
            System.out.println("Hostname validation failed:");
            System.out.println("  Expected: " + expectedHostname);
            System.out.println("  CN: " + cn);
            
            return false;
            
        } catch (Exception e) {
            System.err.println("Errore validazione hostname: " + e.getMessage());
            return false;
        }
    }
    
    private static String extractCN(String dn) {
        // Estrae CN da Distinguished Name
        String[] parts = dn.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.toUpperCase().startsWith("CN=")) {
                return part.substring(3);
            }
        }
        return null;
    }
    
    private static boolean matchesHostname(String hostname, String certName) {
        // Gestisce wildcard certificates (*.example.com)
        if (certName.startsWith("*.")) {
            String domain = certName.substring(2);
            return hostname.endsWith("." + domain) || hostname.equals(domain);
        }
        
        return hostname.equalsIgnoreCase(certName);
    }
    
    /**
     * Monitoraggio SSL handshake per rilevare attacchi
     */
    public static class SSLHandshakeMonitor {
        
        private static final Map<String, List<Long>> handshakeTimes = new ConcurrentHashMap<>();
        
        public static void recordHandshake(String clientIP, boolean successful) {
            
            long currentTime = System.currentTimeMillis();
            
            // Mantieni solo ultimi 10 handshake per IP
            handshakeTimes.computeIfAbsent(clientIP, k -> new ArrayList<>())
                          .add(currentTime);
            
            List<Long> times = handshakeTimes.get(clientIP);
            if (times.size() > 10) {
                times.remove(0);
            }
            
            // Rileva possibili attacchi
            if (times.size() >= 5) {
                long windowStart = currentTime - 30000; // 30 secondi
                long recentHandshakes = times.stream()
                    .filter(time -> time > windowStart)
                    .count();
                
                if (recentHandshakes >= 5) {
                    System.out.println("ALERT: Possible SSL handshake flood from " + clientIP + 
                                     " (" + recentHandshakes + " handshakes in 30s)");
                }
            }
            
            if (!successful) {
                System.out.println("SSL handshake failed for " + clientIP);
            }
        }
    }
    
    // Test configurazione sicurezza
    public static void main(String[] args) throws Exception {
        
        System.out.println("=== SSL ATTACK PREVENTION TEST ===");
        
        // Test creazione context sicuro
        SSLContext context = createHardenedSSLContext();
        System.out.println("✓ SSL Context sicuro creato");
        
        // Test cipher suites
        SSLSocketFactory factory = context.getSocketFactory();
        String[] supportedCiphers = factory.getDefaultCipherSuites();
        String[] secureCiphers = getSecureCipherSuites(supportedCiphers);
        
        System.out.println("\nCipher Suites:");
        System.out.println("- Supportate: " + supportedCiphers.length);
        System.out.println("- Sicure: " + secureCiphers.length);
        
        for (String cipher : secureCiphers) {
            System.out.println("  ✓ " + cipher);
        }
        
        // Test rate limiting
        System.out.println("\nTest Rate Limiting:");
        String testIP = "192.168.1.100";
        
        for (int i = 1; i <= 65; i++) {
            boolean allowed = checkRateLimit(testIP);
            if (!allowed) {
                System.out.println("Rate limit attivato alla connessione " + i);
                break;
            }
        }
        
        System.out.println("\n=== TEST COMPLETATO ===");
    }
}
```

## Configurazione Enterprise SSL

### Enterprise SSL Configuration

```java
/**
 * Configurazione SSL/TLS enterprise-grade per applicazioni production
 */
public class EnterpriseSSLConfig {
    
    // Configurazione per ambiente production
    public static Properties getProductionSSLConfig() {
        Properties config = new Properties();
        
        // Protocolli
        config.setProperty("ssl.protocols", "TLSv1.3,TLSv1.2");
        config.setProperty("ssl.protocols.disabled", "SSLv2,SSLv3,TLSv1,TLSv1.1");
        
        // Cipher suites (ordine preferenza)
        config.setProperty("ssl.ciphers.preferred", String.join(",",
            "TLS_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"
        ));
        
        // Sessioni
        config.setProperty("ssl.session.cache.size", "10000");
        config.setProperty("ssl.session.timeout", "3600");
        
        // Sicurezza
        config.setProperty("ssl.hostname.verification", "true");
        config.setProperty("ssl.certificate.pinning", "true");
        config.setProperty("ssl.ocsp.validation", "true");
        
        // Performance
        config.setProperty("ssl.session.reuse", "true");
        config.setProperty("ssl.hardware.acceleration", "true");
        
        return config;
    }
    
    // Configurazione per sviluppo (meno restrittiva)
    public static Properties getDevelopmentSSLConfig() {
        Properties config = new Properties();
        
        config.setProperty("ssl.protocols", "TLSv1.3,TLSv1.2");
        config.setProperty("ssl.hostname.verification", "false");  // Per localhost
        config.setProperty("ssl.certificate.pinning", "false");   // Per cert self-signed
        config.setProperty("ssl.ocsp.validation", "false");       // Per dev offline
        
        return config;
    }
}
```

---
[⬅️ Lezione Precedente](02-Implementazione-SSL-Socket.md) | [🏠 Torna al Modulo](../README.md) | [➡️ Prossimo Modulo](../06-Multithreading-Avanzato/README.md)