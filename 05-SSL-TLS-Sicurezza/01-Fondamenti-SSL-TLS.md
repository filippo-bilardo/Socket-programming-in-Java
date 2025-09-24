# 1. Fondamenti SSL/TLS

## Introduzione
SSL (Secure Sockets Layer) e TLS (Transport Layer Security) sono protocolli crittografici che forniscono **sicurezza delle comunicazioni** su reti non sicure. TLS è l'evoluzione moderna di SSL e garantisce confidenzialità, integrità e autenticazione dei dati trasmessi.

## Teoria

### Evoluzione dei Protocolli

#### Timeline Storica
```java
// Evoluzione protocolli sicurezza:
// SSL 1.0 (1994) - Mai rilasciato pubblicamente
// SSL 2.0 (1995) - Deprecato (vulnerabilità gravi)
// SSL 3.0 (1996) - Deprecato (POODLE attack)
// TLS 1.0 (1999) - Supporto legacy limitato
// TLS 1.1 (2006) - Supporto legacy limitato  
// TLS 1.2 (2008) - Standard attuale ampiamente supportato
// TLS 1.3 (2018) - Standard moderno ad alte performance
```

#### Versioni Consigliate
```java
// Configurazione Java per versioni sicure
System.setProperty("https.protocols", "TLSv1.2,TLSv1.3");
System.setProperty("jdk.tls.client.protocols", "TLSv1.2,TLSv1.3");

// Disabilita protocolli obsoleti
System.setProperty("jdk.tls.disabledAlgorithms", 
    "SSLv3, TLSv1, TLSv1.1, RC4, MD5withRSA, DH keySize < 1024");
```

### Architettura SSL/TLS

#### Layers del Protocollo
```java
/*
 * ARCHITETTURA TLS:
 * 
 * +---------------------------+
 * |    Applicazione (HTTP)    |  <- Layer 7
 * +---------------------------+
 * |    TLS Record Layer       |  <- Crittografia/Compressione  
 * +---------------------------+
 * |   TLS Handshake Layer     |  <- Autenticazione/Key Exchange
 * +---------------------------+
 * |         TCP               |  <- Layer 4 - Trasporto
 * +---------------------------+
 * |         IP                |  <- Layer 3 - Rete
 * +---------------------------+
 */
```

#### Componenti Principali
```java
// TLS Record Protocol
// - Frammentazione messaggi applicazione
// - Compressione (opzionale, spesso disabilitata)
// - Aggiunta MAC (Message Authentication Code)
// - Crittografia simmetrica

// TLS Handshake Protocol  
// - Negoziazione versione protocollo
// - Selezione cipher suite
// - Autenticazione server (e client opzionale)
// - Generazione chiavi sessione

// TLS Alert Protocol
// - Notifiche errori
// - Chiusura connessione sicura

// TLS Change Cipher Spec
// - Attivazione nuove specifiche crittografiche
```

### Processo di Handshake TLS

#### TLS 1.2 Handshake (RSA)
```java
/*
 * CLIENT                                    SERVER
 * 
 * ClientHello                  ------->
 *   - Versioni TLS supportate
 *   - Random client
 *   - Cipher suites supportate
 *   - Extensions (SNI, ALPN, etc.)
 * 
 *                              <-------    ServerHello
 *                                           - Versione TLS scelta
 *                                           - Random server  
 *                                           - Cipher suite scelta
 *                                           - Session ID
 * 
 *                              <-------    Certificate
 *                                           - Certificato server
 *                                           - Catena certificazione
 * 
 *                              <-------    ServerHelloDone
 * 
 * ClientKeyExchange            ------->
 *   - Pre-master secret crittografato
 *   
 * ChangeCipherSpec             ------->
 * Finished                     ------->
 *   - Verifica handshake
 * 
 *                              <-------    ChangeCipherSpec  
 *                              <-------    Finished
 *                                           - Conferma handshake
 * 
 * Application Data             <------>    Application Data
 */
```

#### TLS 1.3 Handshake (Migliorato)
```java
/*
 * TLS 1.3 - Handshake più veloce (1-RTT):
 * 
 * CLIENT                                    SERVER
 * 
 * ClientHello                  ------->
 *   + Key Share (ECDHE)
 *   + Signature Algorithms
 * 
 *                              <-------    ServerHello
 *                                         + Key Share
 *                                         + Certificate
 *                                         + CertificateVerify  
 *                                         + Finished
 * 
 * Finished                     ------->
 * 
 * Application Data             <------>    Application Data
 * 
 * Vantaggi TLS 1.3:
 * - 1 RTT vs 2 RTT (50% più veloce)
 * - Perfect Forward Secrecy obbligatorio
 * - Cipher suites semplificate
 * - Rimozione algoritmi deboli
 */
```

### Certificati Digitali e PKI

#### Struttura Certificato X.509
```java
// Componenti principali di un certificato X.509:
public class X509CertificateInfo {
    
    // Subject - Identità del possessore
    private String commonName;      // CN=www.example.com
    private String organization;    // O=Example Corp
    private String country;         // C=IT
    
    // Issuer - Autorità che ha firmato  
    private String issuerCN;        // CN=DigiCert Global Root CA
    private String issuerO;         // O=DigiCert Inc
    
    // Validità temporale
    private Date notBefore;         // Inizio validità
    private Date notAfter;          // Fine validità
    
    // Chiave pubblica
    private PublicKey publicKey;    // Chiave RSA/ECDSA
    private String algorithm;       // RSA 2048-bit, ECDSA P-256
    
    // Signature
    private byte[] signature;       // Firma digitale CA
    private String signAlgorithm;   // SHA256withRSA
    
    // Extensions
    private List<String> subjectAltNames;  // DNS alternativi
    private KeyUsage keyUsage;             // Usi permessi chiave
    private ExtendedKeyUsage extKeyUsage;  // Server/client auth
}
```

#### Catena di Certificazione
```java
// Esempio catena tipica:
/*
 * Root CA Certificate (self-signed)
 *     ↓ firma
 * Intermediate CA Certificate  
 *     ↓ firma
 * Server Certificate (www.example.com)
 * 
 * Validazione:
 * 1. Verifica firma server cert con chiave intermediate CA
 * 2. Verifica firma intermediate cert con chiave root CA
 * 3. Verifica root CA è nel trust store
 * 4. Verifica validità temporale tutti i certificati
 * 5. Verifica hostname matching
 */

public boolean validateCertificateChain(X509Certificate[] chain) {
    try {
        // Crea CertPath per validazione
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        CertPath certPath = cf.generateCertPath(Arrays.asList(chain));
        
        // Configura validatore
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
        tmf.init((KeyStore) null); // Use default trust store
        
        PKIXParameters params = new PKIXParameters(getDefaultTrustStore());
        params.setRevocationEnabled(false); // Per semplicità
        
        CertPathValidator validator = CertPathValidator.getInstance("PKIX");
        validator.validate(certPath, params);
        
        return true;
        
    } catch (Exception e) {
        System.err.println("Validazione certificato fallita: " + e.getMessage());
        return false;
    }
}
```

### Cipher Suites

#### Componenti Cipher Suite
```java
/*
 * Formato: TLS_KEYEXCHANGE_WITH_CIPHER_MAC
 * 
 * Esempio: TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384
 * 
 * TLS           - Protocollo
 * ECDHE         - Key exchange (Elliptic Curve Diffie-Hellman Ephemeral)  
 * RSA           - Autenticazione server (firma certificato)
 * AES_256_GCM   - Cifratura simmetrica (Advanced Encryption Standard 256-bit, Galois Counter Mode)
 * SHA384        - Hash per chiavi (non usato in GCM)
 */

// Cipher suites consigliate (ordine di preferenza)
public class RecommendedCipherSuites {
    
    // TLS 1.3 (preferite)
    public static final String[] TLS13_SUITES = {
        "TLS_AES_256_GCM_SHA384",           // AES-256 GCM
        "TLS_CHACHA20_POLY1305_SHA256",     // ChaCha20-Poly1305  
        "TLS_AES_128_GCM_SHA256"            // AES-128 GCM
    };
    
    // TLS 1.2 (backward compatibility)
    public static final String[] TLS12_SUITES = {
        "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",       // Perfect Forward Secrecy
        "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256", // Mobile-friendly
        "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",       // Performance balance
        "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384"          // Fallback PFS
    };
    
    // Da evitare (insicure)
    public static final String[] DEPRECATED_SUITES = {
        ".*_RC4_.*",           // RC4 cipher (broken)
        ".*_DES_.*",           // DES (weak)
        ".*_3DES_.*",          // 3DES (deprecated)
        ".*_MD5",              // MD5 hash (collision)
        ".*_SHA$",             // SHA-1 (deprecated)
        "TLS_RSA_.*"           // No forward secrecy
    };
}
```

#### Configurazione Cipher Suites
```java
public class SSLContextConfiguration {
    
    public static SSLContext createSecureContext() throws Exception {
        // Crea contesto SSL sicuro
        SSLContext context = SSLContext.getInstance("TLSv1.3");
        
        // Usa default key/trust managers (per ora)
        context.init(null, null, new SecureRandom());
        
        return context;
    }
    
    public static void configureSSLSocket(SSLSocket socket) {
        // Abilita solo protocolli sicuri  
        socket.setEnabledProtocols(new String[]{"TLSv1.3", "TLSv1.2"});
        
        // Configura cipher suites sicure
        String[] supportedCiphers = socket.getSupportedCipherSuites();
        List<String> enabledCiphers = new ArrayList<>();
        
        // Aggiungi cipher sicure in ordine di preferenza
        for (String preferred : RecommendedCipherSuites.TLS13_SUITES) {
            if (Arrays.asList(supportedCiphers).contains(preferred)) {
                enabledCiphers.add(preferred);
            }
        }
        
        for (String preferred : RecommendedCipherSuites.TLS12_SUITES) {
            if (Arrays.asList(supportedCiphers).contains(preferred)) {
                enabledCiphers.add(preferred);
            }
        }
        
        socket.setEnabledCipherSuites(enabledCiphers.toArray(new String[0]));
        
        // Preferenza server per cipher suite selection
        socket.setUseClientMode(false);
        
        System.out.println("Cipher suites abilitate: " + enabledCiphers.size());
    }
}
```

### Java Secure Socket Extension (JSSE)

#### Architettura JSSE
```java
/*
 * JSSE Stack:
 * 
 * +------------------------+
 * |   SSLSocket/Channel    |  <- API applicazione
 * +------------------------+
 * |     SSLEngine          |  <- Core SSL/TLS logic
 * +------------------------+  
 * |   TrustManager         |  <- Validazione certificati
 * |   KeyManager           |  <- Gestione chiavi private
 * +------------------------+
 * |   KeyStore/TrustStore  |  <- Storage certificati
 * +------------------------+
 * |   Provider (SunJSSE)   |  <- Implementazione crittografica
 * +------------------------+
 */

// Componenti principali JSSE
public class JSSEComponents {
    
    // SSLContext - Factory per SSL objects
    private SSLContext sslContext;
    
    // KeyManager - Gestisce chiavi private e certificati client
    private KeyManager[] keyManagers;
    
    // TrustManager - Valida certificati server
    private TrustManager[] trustManagers;
    
    // SecureRandom - Generatore numeri casuali crittografici
    private SecureRandom secureRandom;
    
    public void initializeJSSE() throws Exception {
        // 1. Carica KeyStore (chiavi private)
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream("client.jks"), "password".toCharArray());
        
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, "password".toCharArray());
        keyManagers = kmf.getKeyManagers();
        
        // 2. Carica TrustStore (certificati CA fidati)
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream("truststore.jks"), "password".toCharArray());
        
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(trustStore);
        trustManagers = tmf.getTrustManagers();
        
        // 3. Inizializza SecureRandom
        secureRandom = SecureRandom.getInstanceStrong();
        
        // 4. Crea SSLContext
        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagers, trustManagers, secureRandom);
    }
}
```

#### System Properties SSL/TLS
```java
// Proprietà di sistema per debugging e configurazione
public class SSLSystemProperties {
    
    public static void enableSSLDebugging() {
        // Debug completo SSL/TLS (verbose)
        System.setProperty("javax.net.debug", "ssl:handshake:verbose");
        
        // Debug specifico
        System.setProperty("javax.net.debug", "ssl:handshake:data");
        
        // Altre opzioni utili:
        // "ssl" - tutto SSL/TLS
        // "keymanager" - gestione chiavi
        // "trustmanager" - validazione certificati  
        // "session" - gestione sessioni
        // "record" - record protocol
    }
    
    public static void configureSecureDefaults() {
        // Protocolli supportati
        System.setProperty("https.protocols", "TLSv1.2,TLSv1.3");
        
        // Cipher suites
        System.setProperty("https.cipherSuites", 
            "TLS_AES_256_GCM_SHA384," +
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384");
        
        // Trust store personalizzato
        System.setProperty("javax.net.ssl.trustStore", "/path/to/truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "password");
        
        // Key store per client authentication
        System.setProperty("javax.net.ssl.keyStore", "/path/to/keystore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        
        // Disabilita algoritmi insicuri
        System.setProperty("jdk.tls.disabledAlgorithms", 
            "SSLv3, RC4, MD5withRSA, DH keySize < 2048, " +
            "EC keySize < 224, 3DES_EDE_CBC, anon, NULL");
    }
}
```

## Performance e Considerazioni

### SSL/TLS Performance Impact
```java
// Fattori che influenzano performance SSL:

public class SSLPerformanceFactors {
    
    // 1. Handshake overhead
    // TLS 1.2: 2 RTT (Round Trip Time)
    // TLS 1.3: 1 RTT (50% più veloce)
    // Session resumption: 1 RTT -> 0 RTT
    
    // 2. Crittografia simmetrica
    // AES-NI (hardware): ~1-2% CPU overhead
    // Software AES: ~10-20% CPU overhead  
    // ChaCha20: Ottimo per mobile/ARM
    
    // 3. Signature verification
    // RSA 2048: ~1000 verifications/sec
    // ECDSA P-256: ~8000 verifications/sec (8x più veloce)
    
    // 4. Key exchange
    // RSA: Deprecato (no forward secrecy)
    // ECDHE P-256: Raccomandato
    // X25519: Più veloce, supporto limitato
    
    public void measureSSLOverhead() {
        // Benchmark connessioni SSL vs plain socket
        long startTime = System.nanoTime();
        
        // ... esegui operazioni SSL
        
        long sslTime = System.nanoTime() - startTime;
        
        // Confronta con plain socket per calcolare overhead
    }
}
```

### Ottimizzazioni SSL
```java
public class SSLOptimizations {
    
    // Session resumption per evitare handshake completi
    public static void configureSessionCaching(SSLContext context) {
        SSLSessionContext sessionContext = context.getServerSessionContext();
        
        // Cache 10000 sessioni per 24 ore
        sessionContext.setSessionCacheSize(10000);
        sessionContext.setSessionTimeout(24 * 3600); // 24 ore
    }
    
    // Connection pooling per riutilizzo connessioni
    public static void enableKeepAlive() {
        System.setProperty("http.keepAlive", "true");
        System.setProperty("http.maxConnections", "20");
        System.setProperty("http.maxRedirects", "10");
    }
    
    // Hardware acceleration
    public static void enableHardwareAcceleration() {
        // AES-NI per processori Intel/AMD moderni (automatico)
        // Verifica supporto:
        
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            System.out.println("Provider AES: " + cipher.getProvider().getName());
            
            // SunJCE con AES-NI mostra performance migliori
        } catch (Exception e) {
            System.err.println("Errore verifica AES: " + e.getMessage());
        }
    }
}
```

## Security Best Practices

### ✅ Raccomandazioni di Sicurezza
1. **Usa TLS 1.2+ esclusivamente** - Disabilita SSL 3.0/TLS 1.0/1.1
2. **Perfect Forward Secrecy** - Solo cipher suites ECDHE/DHE
3. **Certificati forti** - RSA 2048+ o ECDSA P-256+
4. **Validazione completa** - Hostname, catena, revocation
5. **Regular updates** - Certificati, cipher suites, JVM

### ❌ Vulnerabilità Comuni
1. **Weak cipher suites** - RC4, DES, export ciphers
2. **Certificate validation bypass** - Accept all certs
3. **Hostname verification disabled** - MITM attacks
4. **Mixed content** - HTTP su pagine HTTPS
5. **Outdated protocols** - SSL 3.0, TLS 1.0

---
[🏠 Torna al Modulo](../README.md) | [➡️ Prossima Lezione](02-Implementazione-SSL-Socket.md)