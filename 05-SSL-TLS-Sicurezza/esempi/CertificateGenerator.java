import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;

/**
 * Utility per creare KeyStore e TrustStore per demo SSL.
 * 
 * CARATTERISTICHE:
 * - Genera certificati self-signed per testing
 * - Crea keystore server e client
 * - Configura truststore per validazione reciproca
 * - Usa keytool per generazione certificati
 * - Fornisce istruzioni per uso
 * 
 * USO:
 * 1. Compila: javac CertificateGenerator.java
 * 2. Esegui: java CertificateGenerator
 * 3. Usa i file .jks generati con SimpleSSLServer/Client
 * 
 * IMPORTANTE: 
 * Questi certificati sono SOLO per demo/testing!
 * In produzione usa certificati firmati da CA riconosciute!
 */
public class CertificateGenerator {
    
    // Configurazione certificati
    private static final String SERVER_ALIAS = "server";
    private static final String CLIENT_ALIAS = "client";
    private static final String SERVER_KEYSTORE = "server.jks";
    private static final String CLIENT_KEYSTORE = "client.jks";
    private static final String SERVER_TRUSTSTORE = "server-truststore.jks";
    private static final String CLIENT_TRUSTSTORE = "client-truststore.jks";
    
    // Password (in produzione usa password sicure!)
    private static final String SERVER_STORE_PASS = "serverpass";
    private static final String CLIENT_STORE_PASS = "clientpass";
    private static final String TRUST_STORE_PASS = "trustpass";
    
    public static void main(String[] args) {
        
        System.out.println("=== CERTIFICATE GENERATOR ===");
        System.out.println("Generazione certificati SSL per demo...");
        
        CertificateGenerator generator = new CertificateGenerator();
        
        try {
            // 1. Genera certificati e keystore
            generator.generateServerCertificate();
            generator.generateClientCertificate();
            
            // 2. Crea truststore
            generator.createTrustStores();
            
            // 3. Verifica file generati
            generator.verifyGeneratedFiles();
            
            // 4. Mostra istruzioni uso
            generator.printUsageInstructions();
            
            System.out.println("\n✓ Generazione certificati completata con successo!");
            
        } catch (Exception e) {
            System.err.println("ERRORE: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void generateServerCertificate() throws Exception {
        
        System.out.println("\n1. Generazione certificato server...");
        
        // Comando keytool per certificato server
        String[] serverCommand = {
            "keytool", "-genkeypair",
            "-alias", SERVER_ALIAS,
            "-keyalg", "RSA",
            "-keysize", "2048",
            "-validity", "365",
            "-keystore", SERVER_KEYSTORE,
            "-storepass", SERVER_STORE_PASS,
            "-keypass", SERVER_STORE_PASS,
            "-dname", "CN=localhost,OU=SSL Demo,O=Socket Course,L=Roma,ST=Lazio,C=IT",
            "-ext", "SAN=dns:localhost,dns:127.0.0.1,ip:127.0.0.1"
        };
        
        executeCommand(serverCommand, "Certificato server");
        
        // Esporta certificato pubblico server
        String[] exportServerCommand = {
            "keytool", "-exportcert",
            "-alias", SERVER_ALIAS,
            "-keystore", SERVER_KEYSTORE,
            "-storepass", SERVER_STORE_PASS,
            "-file", "server.crt"
        };
        
        executeCommand(exportServerCommand, "Export certificato server");
        
        System.out.println("✓ Certificato server generato: " + SERVER_KEYSTORE);
    }
    
    public void generateClientCertificate() throws Exception {
        
        System.out.println("\n2. Generazione certificato client...");
        
        // Comando keytool per certificato client
        String[] clientCommand = {
            "keytool", "-genkeypair",
            "-alias", CLIENT_ALIAS,
            "-keyalg", "RSA",
            "-keysize", "2048",
            "-validity", "365",
            "-keystore", CLIENT_KEYSTORE,
            "-storepass", CLIENT_STORE_PASS,
            "-keypass", CLIENT_STORE_PASS,
            "-dname", "CN=SSL Client,OU=SSL Demo,O=Socket Course,L=Roma,ST=Lazio,C=IT"
        };
        
        executeCommand(clientCommand, "Certificato client");
        
        // Esporta certificato pubblico client
        String[] exportClientCommand = {
            "keytool", "-exportcert",
            "-alias", CLIENT_ALIAS,
            "-keystore", CLIENT_KEYSTORE,
            "-storepass", CLIENT_STORE_PASS,
            "-file", "client.crt"
        };
        
        executeCommand(exportClientCommand, "Export certificato client");
        
        System.out.println("✓ Certificato client generato: " + CLIENT_KEYSTORE);
    }
    
    public void createTrustStores() throws Exception {
        
        System.out.println("\n3. Creazione truststore...");
        
        // TrustStore per client (contiene certificato server fidato)
        String[] clientTrustCommand = {
            "keytool", "-importcert",
            "-alias", SERVER_ALIAS,
            "-file", "server.crt",
            "-keystore", CLIENT_TRUSTSTORE,
            "-storepass", TRUST_STORE_PASS,
            "-noprompt"
        };
        
        executeCommand(clientTrustCommand, "Client truststore");
        
        // TrustStore per server (contiene certificato client fidato)
        String[] serverTrustCommand = {
            "keytool", "-importcert",
            "-alias", CLIENT_ALIAS,
            "-file", "client.crt",
            "-keystore", SERVER_TRUSTSTORE,
            "-storepass", TRUST_STORE_PASS,
            "-noprompt"
        };
        
        executeCommand(serverTrustCommand, "Server truststore");
        
        System.out.println("✓ TrustStore creati per autenticazione mutua");
    }
    
    private void executeCommand(String[] command, String description) throws Exception {
        
        System.out.println("  Esecuzione: " + description);
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        
        // Leggi output del comando
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("    " + line);
            }
        }
        
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            throw new RuntimeException(description + " fallito con exit code: " + exitCode);
        }
        
        System.out.println("  ✓ " + description + " completato");
    }
    
    public void verifyGeneratedFiles() throws Exception {
        
        System.out.println("\n4. Verifica file generati...");
        
        String[] expectedFiles = {
            SERVER_KEYSTORE,
            CLIENT_KEYSTORE,
            SERVER_TRUSTSTORE,
            CLIENT_TRUSTSTORE,
            "server.crt",
            "client.crt"
        };
        
        boolean allFilesExist = true;
        
        for (String fileName : expectedFiles) {
            File file = new File(fileName);
            if (file.exists()) {
                long size = file.length();
                System.out.println("  ✓ " + fileName + " (" + size + " bytes)");
            } else {
                System.err.println("  ✗ " + fileName + " NON trovato");
                allFilesExist = false;
            }
        }
        
        if (!allFilesExist) {
            throw new RuntimeException("Alcuni file non sono stati generati correttamente");
        }
        
        // Verifica contenuto keystore
        verifyKeyStoreContent();
    }
    
    private void verifyKeyStoreContent() throws Exception {
        
        System.out.println("\n  Verifica contenuto keystore:");
        
        // Verifica server keystore
        KeyStore serverKS = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(SERVER_KEYSTORE)) {
            serverKS.load(fis, SERVER_STORE_PASS.toCharArray());
        }
        
        if (serverKS.containsAlias(SERVER_ALIAS)) {
            Certificate cert = serverKS.getCertificate(SERVER_ALIAS);
            System.out.println("    ✓ Server keystore contiene alias '" + SERVER_ALIAS + "'");
            System.out.println("      Tipo: " + cert.getType());
        }
        
        // Verifica client keystore
        KeyStore clientKS = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(CLIENT_KEYSTORE)) {
            clientKS.load(fis, CLIENT_STORE_PASS.toCharArray());
        }
        
        if (clientKS.containsAlias(CLIENT_ALIAS)) {
            Certificate cert = clientKS.getCertificate(CLIENT_ALIAS);
            System.out.println("    ✓ Client keystore contiene alias '" + CLIENT_ALIAS + "'");
            System.out.println("      Tipo: " + cert.getType());
        }
        
        // Verifica truststore
        KeyStore trustKS = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(CLIENT_TRUSTSTORE)) {
            trustKS.load(fis, TRUST_STORE_PASS.toCharArray());
        }
        
        if (trustKS.containsAlias(SERVER_ALIAS)) {
            System.out.println("    ✓ Client truststore contiene certificato server fidato");
        }
    }
    
    public void printUsageInstructions() {
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("ISTRUZIONI USO CERTIFICATI GENERATI");
        System.out.println("=".repeat(80));
        
        System.out.println("\nFILE GENERATI:");
        System.out.println("  " + SERVER_KEYSTORE + "       - Keystore server (chiave privata + certificato)");
        System.out.println("  " + CLIENT_KEYSTORE + "       - Keystore client (chiave privata + certificato)");
        System.out.println("  " + SERVER_TRUSTSTORE + " - Truststore server (certificati client fidati)");
        System.out.println("  " + CLIENT_TRUSTSTORE + " - Truststore client (certificati server fidati)");
        System.out.println("  server.crt         - Certificato pubblico server");
        System.out.println("  client.crt         - Certificato pubblico client");
        
        System.out.println("\nPASSWORD:");
        System.out.println("  Server keystore: " + SERVER_STORE_PASS);
        System.out.println("  Client keystore: " + CLIENT_STORE_PASS);
        System.out.println("  TrustStore:      " + TRUST_STORE_PASS);
        
        System.out.println("\nAVVIO SERVER SSL:");
        System.out.println("  java SimpleSSLServer");
        System.out.println("  (Il server caricherà automaticamente " + SERVER_KEYSTORE + ")");
        
        System.out.println("\nCONNESSIONE CLIENT SSL:");
        System.out.println("  java SimpleSSLClient                    (localhost con bypass validazione)");
        System.out.println("  java SimpleSSLClient www.google.com 443 (server reale con validazione)");
        
        System.out.println("\nAUTENTICAZIONE MUTUA (avanzato):");
        System.out.println("  Server con validazione client:");
        System.out.println("    java -Djavax.net.ssl.trustStore=" + SERVER_TRUSTSTORE + 
                          " -Djavax.net.ssl.trustStorePassword=" + TRUST_STORE_PASS + " SimpleSSLServer");
        
        System.out.println("\n  Client con certificato:");
        System.out.println("    java -Djavax.net.ssl.keyStore=" + CLIENT_KEYSTORE + 
                          " -Djavax.net.ssl.keyStorePassword=" + CLIENT_STORE_PASS + 
                          " -Djavax.net.ssl.trustStore=" + CLIENT_TRUSTSTORE + 
                          " -Djavax.net.ssl.trustStorePassword=" + TRUST_STORE_PASS + " SimpleSSLClient");
        
        System.out.println("\nTEST BROWSER:");
        System.out.println("  1. Avvia SimpleSSLServer");
        System.out.println("  2. Apri browser su https://localhost:8443");
        System.out.println("  3. Accetta certificato self-signed (solo per demo!)");
        
        System.out.println("\nATTENZIONE:");
        System.out.println("  ⚠️  Questi certificati sono SELF-SIGNED e validi SOLO per demo!");
        System.out.println("  ⚠️  In produzione usa certificati firmati da CA riconosciute!");
        System.out.println("  ⚠️  Non utilizzare mai certificati self-signed in ambienti production!");
        
        System.out.println("\nCOMANDI UTILI:");
        System.out.println("  Visualizza certificato:");
        System.out.println("    keytool -list -v -keystore " + SERVER_KEYSTORE + " -storepass " + SERVER_STORE_PASS);
        
        System.out.println("\n  Test connessione SSL:");
        System.out.println("    openssl s_client -connect localhost:8443 -servername localhost");
        
        System.out.println("\n" + "=".repeat(80));
    }
    
    // Utility per pulire file generati
    public static void cleanup() {
        
        System.out.println("Pulizia file generati...");
        
        String[] filesToDelete = {
            SERVER_KEYSTORE,
            CLIENT_KEYSTORE,
            SERVER_TRUSTSTORE,
            CLIENT_TRUSTSTORE,
            "server.crt",
            "client.crt"
        };
        
        int deletedCount = 0;
        
        for (String fileName : filesToDelete) {
            File file = new File(fileName);
            if (file.exists() && file.delete()) {
                System.out.println("  ✓ Eliminato: " + fileName);
                deletedCount++;
            }
        }
        
        System.out.println("Pulizia completata: " + deletedCount + " file eliminati");
    }
    
    // Test validità certificati
    public static void testCertificates() throws Exception {
        
        System.out.println("=== TEST CERTIFICATI ===");
        
        // Carica e testa certificato server
        KeyStore serverKS = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(SERVER_KEYSTORE)) {
            serverKS.load(fis, SERVER_STORE_PASS.toCharArray());
        }
        
        X509Certificate serverCert = (X509Certificate) serverKS.getCertificate(SERVER_ALIAS);
        if (serverCert != null) {
            System.out.println("Certificato Server:");
            System.out.println("  Subject: " + serverCert.getSubjectX500Principal());
            System.out.println("  Issuer: " + serverCert.getIssuerX500Principal());
            System.out.println("  Valido da: " + serverCert.getNotBefore());
            System.out.println("  Valido fino: " + serverCert.getNotAfter());
            
            try {
                serverCert.checkValidity();
                System.out.println("  ✓ Certificato valido");
            } catch (Exception e) {
                System.out.println("  ✗ Certificato non valido: " + e.getMessage());
            }
        }
    }
}