/**
 * Nome dell'Esempio: Echo UDP Server e Client
 * Guida di Riferimento: 01-Socket-UDP-Base.md
 * 
 * Obiettivo: Dimostrare comunicazione UDP bidirezionale con gestione errori.
 * 
 * Spiegazione:
 * 1. Server UDP che rimanda indietro i messaggi ricevuti
 * 2. Client UDP che invia messaggi e riceve risposte
 * 3. Gestione timeout e pacchetti persi
 * 
 * @author Socket Programming Course
 * @version 1.0
 */

import java.io.*;
import java.net.*;

public class EchoUDP {
    
    /**
     * Server UDP Echo
     */
    public static class Server {
        private final int port;
        private DatagramSocket socket;
        private boolean running;
        
        public Server(int port) {
            this.port = port;
        }
        
        public void start() throws IOException {
            socket = new DatagramSocket(port);
            running = true;
            
            System.out.println("🚀 Server UDP Echo avviato su porta " + port);
            System.out.println("📦 Dimensione buffer: 1024 byte");
            System.out.println("🛑 Premi Ctrl+C per fermare");
            System.out.println("=" .repeat(50));
            
            byte[] buffer = new byte[1024];
            
            while (running) {
                try {
                    // Riceve pacchetto
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    
                    // Estrae informazioni
                    String received = new String(packet.getData(), 0, packet.getLength());
                    InetAddress clientAddress = packet.getAddress();
                    int clientPort = packet.getPort();
                    
                    System.out.println("📨 Ricevuto da " + clientAddress + ":" + clientPort + 
                                     " → " + received);
                    
                    // Prepara risposta (echo + timestamp)
                    String response = "ECHO: " + received + " [" + System.currentTimeMillis() + "]";
                    byte[] responseData = response.getBytes();
                    
                    // Invia risposta
                    DatagramPacket responsePacket = new DatagramPacket(
                        responseData, responseData.length, clientAddress, clientPort);
                    socket.send(responsePacket);
                    
                    System.out.println("📤 Risposta inviata: " + response);
                    
                } catch (IOException e) {
                    if (running) {
                        System.err.println("❌ Errore server: " + e.getMessage());
                    }
                }
            }
        }
        
        public void stop() {
            running = false;
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("🔒 Server fermato");
            }
        }
    }
    
    /**
     * Client UDP
     */
    public static class Client {
        private final String serverHost;
        private final int serverPort;
        private DatagramSocket socket;
        
        public Client(String serverHost, int serverPort) {
            this.serverHost = serverHost;
            this.serverPort = serverPort;
        }
        
        public String sendMessage(String message, int timeoutMs) throws IOException {
            if (socket == null) {
                socket = new DatagramSocket();
                socket.setSoTimeout(timeoutMs);
            }
            
            try {
                // Prepara e invia pacchetto
                byte[] data = message.getBytes();
                InetAddress address = InetAddress.getByName(serverHost);
                DatagramPacket packet = new DatagramPacket(data, data.length, address, serverPort);
                
                System.out.println("📤 Invio: " + message);
                socket.send(packet);
                
                // Riceve risposta
                byte[] buffer = new byte[1024];
                DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(responsePacket);
                
                String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
                System.out.println("📨 Risposta: " + response);
                
                return response;
                
            } catch (SocketTimeoutException e) {
                System.err.println("⏰ Timeout - nessuna risposta in " + timeoutMs + "ms");
                throw e;
            } catch (UnknownHostException e) {
                System.err.println("❌ Host sconosciuto: " + serverHost);
                throw e;
            }
        }
        
        public void close() {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
        
        public void interactive() {
            System.out.println("💬 Modalità interattiva avviata");
            System.out.println("💡 Digita 'quit' per uscire");
            System.out.println("-" .repeat(30));
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                String input;
                int messageCount = 0;
                
                while (true) {
                    System.out.print("Tu: ");
                    input = reader.readLine();
                    
                    if (input == null || "quit".equalsIgnoreCase(input.trim())) {
                        break;
                    }
                    
                    if (input.trim().isEmpty()) {
                        continue;
                    }
                    
                    try {
                        messageCount++;
                        String messageWithId = "[#" + messageCount + "] " + input;
                        sendMessage(messageWithId, 5000);
                        
                    } catch (SocketTimeoutException e) {
                        System.err.println("⚠️ Messaggio perso (timeout)");
                    } catch (IOException e) {
                        System.err.println("❌ Errore invio: " + e.getMessage());
                    }
                    
                    System.out.println();
                }
                
                System.out.println("👋 Disconnessione...");
                
            } catch (IOException e) {
                System.err.println("❌ Errore input: " + e.getMessage());
            } finally {
                close();
            }
        }
    }
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("🛠️ Echo UDP - Server e Client");
            System.out.println("Utilizzo:");
            System.out.println("  java EchoUDP server <porta>");
            System.out.println("  java EchoUDP client <host> <porta>");
            System.out.println();
            System.out.println("Esempi:");
            System.out.println("  java EchoUDP server 9999");
            System.out.println("  java EchoUDP client localhost 9999");
            return;
        }
        
        String mode = args[0].toLowerCase();
        
        if ("server".equals(mode)) {
            if (args.length < 2) {
                System.err.println("❌ Porta richiesta per il server");
                return;
            }
            
            try {
                int port = Integer.parseInt(args[1]);
                Server server = new Server(port);
                
                // Shutdown hook per chiusura pulita
                Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
                
                server.start();
                
            } catch (NumberFormatException e) {
                System.err.println("❌ Porta non valida: " + args[1]);
            } catch (IOException e) {
                System.err.println("💥 Errore avvio server: " + e.getMessage());
            }
            
        } else if ("client".equals(mode)) {
            if (args.length < 3) {
                System.err.println("❌ Host e porta richiesti per il client");
                return;
            }
            
            try {
                String host = args[1];
                int port = Integer.parseInt(args[2]);
                
                System.out.println("🔗 Client UDP Echo");
                System.out.println("Target: " + host + ":" + port);
                System.out.println("=" .repeat(30));
                
                Client client = new Client(host, port);
                
                // Test singolo messaggio
                try {
                    client.sendMessage("Test di connettività", 3000);
                    System.out.println("✅ Connessione OK\n");
                    
                    // Modalità interattiva
                    client.interactive();
                    
                } catch (IOException e) {
                    System.err.println("💥 Test connessione fallito: " + e.getMessage());
                }
                
            } catch (NumberFormatException e) {
                System.err.println("❌ Porta non valida: " + args[2]);
            }
            
        } else {
            System.err.println("❌ Modalità non riconosciuta: " + mode);
            System.out.println("Modalità disponibili: server, client");
        }
    }
}