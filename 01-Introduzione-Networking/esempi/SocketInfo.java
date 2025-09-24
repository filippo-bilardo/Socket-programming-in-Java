/**
 * Nome dell'Esempio: Socket Info
 * Guida di Riferimento: 02-Introduzione-ai-Socket.md
 * 
 * Obiettivo: Mostrare informazioni dettagliate sui socket e la connessione.
 * 
 * Spiegazione:
 * 1. Crea una connessione socket verso un server
 * 2. Estrae e visualizza tutte le informazioni disponibili
 * 3. Dimostra come accedere agli indirizzi locali e remoti
 * 
 * @author Socket Programming Course
 * @version 1.0
 */

import java.io.IOException;
import java.net.*;

public class SocketInfo {
    
    /**
     * Mostra informazioni dettagliate su un socket
     * 
     * @param socket Il socket di cui mostrare le informazioni
     */
    public static void displaySocketInfo(Socket socket) {
        System.out.println("📋 Informazioni Socket");
        System.out.println("=" .repeat(30));
        
        try {
            // Informazioni locali (client)
            System.out.println("🏠 Informazioni Locali:");
            System.out.println("  Indirizzo locale: " + socket.getLocalAddress().getHostAddress());
            System.out.println("  Porta locale: " + socket.getLocalPort());
            System.out.println("  Endpoint locale: " + socket.getLocalSocketAddress());
            
            // Informazioni remote (server)
            System.out.println("\n🌐 Informazioni Remote:");
            System.out.println("  Indirizzo remoto: " + socket.getInetAddress().getHostAddress());
            System.out.println("  Nome host remoto: " + socket.getInetAddress().getHostName());
            System.out.println("  Porta remota: " + socket.getPort());
            System.out.println("  Endpoint remoto: " + socket.getRemoteSocketAddress());
            
            // Informazioni sulla connessione
            System.out.println("\n🔗 Stato Connessione:");
            System.out.println("  Connesso: " + socket.isConnected());
            System.out.println("  Chiuso: " + socket.isClosed());
            System.out.println("  Bound: " + socket.isBound());
            System.out.println("  Input shutdown: " + socket.isInputShutdown());
            System.out.println("  Output shutdown: " + socket.isOutputShutdown());
            
            // Opzioni socket
            System.out.println("\n⚙️ Opzioni Socket:");
            System.out.println("  Keep-Alive: " + socket.getKeepAlive());
            System.out.println("  TCP NoDelay: " + socket.getTcpNoDelay());
            System.out.println("  SO Linger: " + socket.getSoLinger());
            System.out.println("  SO Timeout: " + socket.getSoTimeout());
            System.out.println("  Receive Buffer Size: " + socket.getReceiveBufferSize());
            System.out.println("  Send Buffer Size: " + socket.getSendBufferSize());
            
        } catch (IOException e) {
            System.err.println("Errore nel recupero informazioni: " + e.getMessage());
        }
    }
    
    /**
     * Mostra informazioni sulla rete locale
     */
    public static void displayNetworkInfo() {
        System.out.println("\n🌍 Informazioni Rete Locale");
        System.out.println("=" .repeat(30));
        
        try {
            // Hostname locale
            String hostname = InetAddress.getLocalHost().getHostName();
            System.out.println("Hostname: " + hostname);
            
            // Indirizzo localhost
            InetAddress localhost = InetAddress.getLocalHost();
            System.out.println("Localhost: " + localhost.getHostAddress());
            
            // Tutte le interfacce di rete
            System.out.println("\n🔌 Interfacce di Rete:");
            java.util.Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isLoopback() && ni.isUp()) {
                    System.out.println("  " + ni.getName() + " (" + ni.getDisplayName() + "):");
                    
                    java.util.Enumeration<InetAddress> addresses = ni.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        System.out.println("    " + addr.getHostAddress());
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Errore nel recupero informazioni di rete: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        // Mostra informazioni sulla rete locale
        displayNetworkInfo();
        
        // Test con diversi server
        String[] testServers = {
            "google.com:80",
            "github.com:443"
        };
        
        for (String serverInfo : testServers) {
            String[] parts = serverInfo.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            
            System.out.println("\n🔍 Test connessione a " + serverInfo);
            System.out.println("=" .repeat(50));
            
            try (Socket socket = new Socket()) {
                // Imposta timeout per la connessione
                socket.connect(new InetSocketAddress(host, port), 3000);
                
                // Mostra informazioni del socket
                displaySocketInfo(socket);
                
            } catch (IOException e) {
                System.err.println("❌ Impossibile connettersi a " + serverInfo + ": " + e.getMessage());
            }
        }
    }
}