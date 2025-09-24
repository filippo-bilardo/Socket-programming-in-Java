import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * Chat Server SSL per comunicazioni sicure multi-client.
 * 
 * CARATTERISTICHE:
 * - Server chat SSL/TLS con crittografia end-to-end
 * - Supporto client multipli simultanei
 * - Broadcast messaggi a tutti i client connessi
 * - Autenticazione SSL client-server
 * - Gestione disconnessioni graceful
 * - Logging sicurezza e connessioni
 * 
 * FUNZIONALITÀ:
 * - Messaggi broadcast in tempo reale
 * - Lista utenti connessi
 * - Comandi chat (/users, /quit, /help)
 * - Timestamping messaggi
 * - Rate limiting per prevenire spam
 * 
 * USO:
 * 1. Genera certificati con CertificateGenerator
 * 2. Compila: javac SecureChatServer.java
 * 3. Avvia: java SecureChatServer [port]
 * 4. Connetti client con SecureChatClient
 */
public class SecureChatServer {
    
    private static final int DEFAULT_PORT = 8443;
    private static final String KEYSTORE_PATH = "server.jks";
    private static final String KEYSTORE_PASSWORD = "serverpass";
    private static final int MAX_CLIENTS = 50;
    
    private final int port;
    private SSLServerSocket serverSocket;
    private volatile boolean running = false;
    
    // Gestione client connessi
    private final Map<String, ClientHandler> connectedClients;
    private final ExecutorService clientThreadPool;
    private final AtomicInteger clientCounter;
    
    public SecureChatServer(int port) {
        this.port = port;
        this.connectedClients = new ConcurrentHashMap<>();
        this.clientThreadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
        this.clientCounter = new AtomicInteger(0);
    }
    
    public static void main(String[] args) {
        
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Porta non valida: " + args[0]);
                System.exit(1);
            }
        }
        
        System.out.println("=== SECURE CHAT SERVER SSL ===");
        System.out.println("Avvio chat server sicuro sulla porta " + port);
        
        SecureChatServer server = new SecureChatServer(port);
        
        // Shutdown hook per pulizia
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutdown chat server...");
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
            serverSocket = (SSLServerSocket) factory.createServerSocket(port);
            
            // Configura sicurezza
            configureSSLServerSocket(serverSocket);
            
            System.out.println("✓ Chat server SSL avviato su porta " + port);
            System.out.println("✓ Protocolli: " + Arrays.toString(serverSocket.getEnabledProtocols()));
            System.out.println("✓ Max client: " + MAX_CLIENTS);
            System.out.println("\nIn attesa di connessioni...");
            
            running = true;
            
            // Loop principale accettazione client
            while (running) {
                try {
                    SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                    handleNewClient(clientSocket);
                    
                } catch (SocketException e) {
                    if (running) {
                        System.err.println("Errore accettazione client: " + e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("ERRORE CHAT SERVER: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleNewClient(SSLSocket clientSocket) {
        
        String clientAddress = clientSocket.getRemoteSocketAddress().toString();
        
        try {
            // Controlla limite client
            if (connectedClients.size() >= MAX_CLIENTS) {
                System.out.println("Connessione rifiutata (limite raggiunto): " + clientAddress);
                sendMessage(clientSocket, "SERVER: Limite client raggiunto. Riprova più tardi.");
                clientSocket.close();
                return;
            }
            
            // Configura socket client
            configureSSLClientSocket(clientSocket);
            
            // Esegui handshake SSL
            clientSocket.startHandshake();
            
            // Crea handler client
            String clientId = "Client-" + clientCounter.incrementAndGet();
            ClientHandler handler = new ClientHandler(clientSocket, clientId);
            
            // Registra client
            connectedClients.put(clientId, handler);
            
            // Avvia thread gestione client
            clientThreadPool.submit(handler);
            
            System.out.println("✓ Nuovo client connesso: " + clientId + " da " + clientAddress);
            System.out.println("  Client totali: " + connectedClients.size());
            
            // Notifica agli altri client
            broadcastMessage("SERVER", clientId + " si è unito alla chat", clientId);
            
        } catch (Exception e) {
            System.err.println("Errore configurazione client " + clientAddress + ": " + e.getMessage());
            try {
                clientSocket.close();
            } catch (IOException ex) {
                // Ignora errori chiusura
            }
        }
    }
    
    public void stop() {
        running = false;
        
        // Disconnetti tutti i client
        System.out.println("Disconnessione client...");
        for (ClientHandler client : connectedClients.values()) {
            client.disconnect();
        }
        connectedClients.clear();
        
        // Shutdown thread pool
        clientThreadPool.shutdown();
        try {
            if (!clientThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                clientThreadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            clientThreadPool.shutdownNow();
        }
        
        // Chiudi server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Errore chiusura server socket: " + e.getMessage());
            }
        }
        
        System.out.println("Chat server fermato");
    }
    
    private SSLContext createSSLContext() throws Exception {
        
        // Carica keystore server
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(KEYSTORE_PATH)) {
            keyStore.load(fis, KEYSTORE_PASSWORD.toCharArray());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Keystore non trovato: " + KEYSTORE_PATH + 
                "\nEsegui CertificateGenerator per creare i certificati.");
        }
        
        // Inizializza KeyManager
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, KEYSTORE_PASSWORD.toCharArray());
        
        // Usa default TrustManager
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init((KeyStore) null);
        
        // Crea SSL context
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        
        return sslContext;
    }
    
    private void configureSSLServerSocket(SSLServerSocket serverSocket) {
        // Abilita solo protocolli sicuri
        serverSocket.setEnabledProtocols(new String[]{"TLSv1.3", "TLSv1.2"});
        
        // Cipher suites sicure
        String[] secureCiphers = getSecureCipherSuites(serverSocket.getSupportedCipherSuites());
        serverSocket.setEnabledCipherSuites(secureCiphers);
        
        // Non richiedere certificato client (per semplicità)
        serverSocket.setWantClientAuth(false);
    }
    
    private void configureSSLClientSocket(SSLSocket socket) {
        socket.setEnabledProtocols(new String[]{"TLSv1.3", "TLSv1.2"});
        
        String[] secureCiphers = getSecureCipherSuites(socket.getSupportedCipherSuites());
        socket.setEnabledCipherSuites(secureCiphers);
    }
    
    private String[] getSecureCipherSuites(String[] supportedCiphers) {
        String[] preferredCiphers = {
            "TLS_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"
        };
        
        List<String> enabledCiphers = new ArrayList<>();
        for (String cipher : preferredCiphers) {
            if (Arrays.asList(supportedCiphers).contains(cipher)) {
                enabledCiphers.add(cipher);
            }
        }
        
        return enabledCiphers.isEmpty() ? 
            new String[]{supportedCiphers[0]} : 
            enabledCiphers.toArray(new String[0]);
    }
    
    // Broadcast messaggio a tutti i client (tranne sender)
    public void broadcastMessage(String sender, String message, String excludeClientId) {
        
        String timestamp = LocalTime.now().toString().substring(0, 8);
        String fullMessage = String.format("[%s] %s: %s", timestamp, sender, message);
        
        System.out.println("BROADCAST: " + fullMessage);
        
        List<String> disconnectedClients = new ArrayList<>();
        
        for (Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()) {
            String clientId = entry.getKey();
            ClientHandler client = entry.getValue();
            
            // Non inviare al sender
            if (!clientId.equals(excludeClientId)) {
                if (!client.sendMessage(fullMessage)) {
                    disconnectedClients.add(clientId);
                }
            }
        }
        
        // Rimuovi client disconnessi
        for (String clientId : disconnectedClients) {
            removeClient(clientId);
        }
    }
    
    // Invia messaggio privato a client specifico
    public boolean sendPrivateMessage(String targetClientId, String sender, String message) {
        
        ClientHandler target = connectedClients.get(targetClientId);
        if (target != null) {
            String timestamp = LocalTime.now().toString().substring(0, 8);
            String privateMsg = String.format("[%s] PRIVATE %s: %s", timestamp, sender, message);
            return target.sendMessage(privateMsg);
        }
        
        return false;
    }
    
    // Rimuovi client dalla lista
    public void removeClient(String clientId) {
        ClientHandler client = connectedClients.remove(clientId);
        if (client != null) {
            client.disconnect();
            System.out.println("✗ Client disconnesso: " + clientId);
            System.out.println("  Client rimanenti: " + connectedClients.size());
            
            // Notifica agli altri client
            broadcastMessage("SERVER", clientId + " ha lasciato la chat", clientId);
        }
    }
    
    // Ottieni lista client connessi
    public String getClientList() {
        if (connectedClients.isEmpty()) {
            return "Nessun client connesso";
        }
        
        StringBuilder sb = new StringBuilder("Client connessi (" + connectedClients.size() + "):\n");
        for (String clientId : connectedClients.keySet()) {
            sb.append("  - ").append(clientId).append("\n");
        }
        
        return sb.toString();
    }
    
    private static void sendMessage(SSLSocket socket, String message) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
        } catch (IOException e) {
            // Ignora errori invio
        }
    }
    
    // Handler per ogni client connesso
    private class ClientHandler implements Runnable {
        
        private final SSLSocket socket;
        private final String clientId;
        private BufferedReader in;
        private PrintWriter out;
        private volatile boolean connected = false;
        
        // Rate limiting
        private final Map<Long, Integer> messageCount = new ConcurrentHashMap<>();
        private static final int MAX_MESSAGES_PER_MINUTE = 30;
        
        public ClientHandler(SSLSocket socket, String clientId) {
            this.socket = socket;
            this.clientId = clientId;
        }
        
        @Override
        public void run() {
            try {
                // Inizializza stream
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                connected = true;
                
                // Messaggio benvenuto
                sendMessage("=== SECURE CHAT SERVER ===");
                sendMessage("Benvenuto " + clientId + "!");
                sendMessage("Connessione SSL stabilita: " + socket.getSession().getProtocol());
                sendMessage("Digita /help per comandi disponibili");
                sendMessage("========================");
                
                // Loop messaggi client
                String message;
                while (connected && (message = in.readLine()) != null) {
                    
                    message = message.trim();
                    if (message.isEmpty()) continue;
                    
                    // Rate limiting
                    if (!checkRateLimit()) {
                        sendMessage("SERVER: Troppi messaggi! Rallenta.");
                        continue;
                    }
                    
                    // Processa messaggio
                    processMessage(message);
                }
                
            } catch (IOException e) {
                if (connected) {
                    System.err.println("Errore comunicazione " + clientId + ": " + e.getMessage());
                }
            } finally {
                disconnect();
                removeClient(clientId);
            }
        }
        
        private void processMessage(String message) {
            
            if (message.startsWith("/")) {
                // Comandi chat
                processCommand(message);
            } else {
                // Messaggio normale - broadcast
                broadcastMessage(clientId, message, clientId);
            }
        }
        
        private void processCommand(String command) {
            
            String[] parts = command.split(" ", 2);
            String cmd = parts[0].toLowerCase();
            
            switch (cmd) {
                case "/help":
                    sendMessage("Comandi disponibili:");
                    sendMessage("  /help - Mostra questo aiuto");
                    sendMessage("  /users - Lista utenti connessi");
                    sendMessage("  /private <id> <msg> - Messaggio privato");
                    sendMessage("  /quit - Disconnetti");
                    break;
                    
                case "/users":
                    sendMessage(getClientList());
                    break;
                    
                case "/private":
                    if (parts.length > 1) {
                        String[] msgParts = parts[1].split(" ", 2);
                        if (msgParts.length == 2) {
                            String targetId = msgParts[0];
                            String privateMsg = msgParts[1];
                            
                            if (sendPrivateMessage(targetId, clientId, privateMsg)) {
                                sendMessage("Messaggio privato inviato a " + targetId);
                            } else {
                                sendMessage("Utente " + targetId + " non trovato");
                            }
                        } else {
                            sendMessage("Uso: /private <id> <messaggio>");
                        }
                    } else {
                        sendMessage("Uso: /private <id> <messaggio>");
                    }
                    break;
                    
                case "/quit":
                    sendMessage("Arrivederci!");
                    disconnect();
                    break;
                    
                default:
                    sendMessage("Comando sconosciuto: " + cmd + " (usa /help)");
            }
        }
        
        private boolean checkRateLimit() {
            long currentMinute = System.currentTimeMillis() / 60000;
            
            // Pulisci messaggi vecchi
            messageCount.entrySet().removeIf(entry -> 
                (currentMinute - entry.getKey()) > 1);
            
            // Incrementa contatore per minuto corrente
            int count = messageCount.merge(currentMinute, 1, Integer::sum);
            
            return count <= MAX_MESSAGES_PER_MINUTE;
        }
        
        public boolean sendMessage(String message) {
            if (connected && out != null) {
                try {
                    out.println(message);
                    return !out.checkError();
                } catch (Exception e) {
                    return false;
                }
            }
            return false;
        }
        
        public void disconnect() {
            connected = false;
            
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                // Ignora errori chiusura
            }
        }
    }
}