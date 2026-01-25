import java.net.*;
import java.io.*;

/**
 * Esempio 02.04 - Multicast UDP Receiver (Java)
 * 
 * Questo esempio dimostra come ricevere pacchetti multicast UDP.
 * Il receiver si unisce a un gruppo multicast e riceve tutti i messaggi
 * inviati a quel gruppo.
 * 
 * Caratteristiche:
 * - Ricezione di messaggi multicast
 * - Join e leave da gruppi multicast
 * - Visualizzazione informazioni mittente
 * - Parsing e validazione messaggi
 * 
 * Compilazione: javac MulticastReceiver.java
 * Esecuzione: java MulticastReceiver [indirizzo_multicast] [porta]
 * Esempio: java MulticastReceiver 239.255.0.1 5000
 */
public class MulticastReceiver {
    private static final String DEFAULT_MULTICAST_ADDRESS = "239.255.0.1";
    private static final int DEFAULT_PORT = 5000;
    private static final int BUFFER_SIZE = 1024;
    
    public static void main(String[] args) {
        String multicastAddress = DEFAULT_MULTICAST_ADDRESS;
        int port = DEFAULT_PORT;
        
        // Parsing argomenti da linea di comando
        if (args.length > 0) {
            multicastAddress = args[0];
        }
        
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("❌ Porta non valida. Uso porta di default: " + DEFAULT_PORT);
            }
        }
        
        // Validazione indirizzo multicast
        if (!isValidMulticastAddress(multicastAddress)) {
            System.err.println("❌ Indirizzo multicast non valido: " + multicastAddress);
            System.err.println("   Deve essere nel range 224.0.0.0 - 239.255.255.255");
            System.exit(1);
        }
        
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("📡 MULTICAST UDP RECEIVER");
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("🔌 Porta: " + port);
        System.out.println("📡 Gruppo Multicast: " + multicastAddress);
        System.out.println("═══════════════════════════════════════════════════\n");
        
        MulticastSocket socket = null;
        InetAddress group = null;
        
        try {
            // Crea MulticastSocket e si unisce al gruppo
            socket = new MulticastSocket(port);
            group = InetAddress.getByName(multicastAddress);
            
            // IMPORTANTE: Join al gruppo multicast
            socket.joinGroup(group);
            
            System.out.println("✅ MulticastSocket creato sulla porta " + port);
            System.out.println("🤝 Unito al gruppo multicast " + multicastAddress);
            System.out.println("👂 In attesa di messaggi multicast...\n");
            System.out.println("Premi Ctrl+C per terminare\n");
            
            byte[] buffer = new byte[BUFFER_SIZE];
            int messageCount = 0;
            
            while (true) {
                // Crea il pacchetto per la ricezione
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                
                // Riceve il pacchetto (bloccante)
                socket.receive(packet);
                messageCount++;
                
                // Estrae i dati dal pacchetto
                String message = new String(
                    packet.getData(),
                    0,
                    packet.getLength()
                );
                
                InetAddress senderAddress = packet.getAddress();
                int senderPort = packet.getPort();
                
                // Visualizza informazioni sul messaggio ricevuto
                System.out.println("╔═══════════════════════════════════════════════════");
                System.out.printf("║ 📥 MESSAGGIO MULTICAST #%d RICEVUTO%n", messageCount);
                System.out.println("╠═══════════════════════════════════════════════════");
                System.out.printf("║ ⏰ Ora: %s%n", getCurrentTime());
                System.out.printf("║ 👤 Mittente: %s:%d%n", senderAddress.getHostAddress(), senderPort);
                System.out.printf("║ 📦 Dimensione: %d bytes%n", packet.getLength());
                System.out.println("╠═══════════════════════════════════════════════════");
                
                // Parsing del messaggio
                parseMulticastMessage(message);
                
                System.out.println("╚═══════════════════════════════════════════════════\n");
            }
            
        } catch (SocketException e) {
            System.err.println("❌ Errore creazione socket: " + e.getMessage());
            System.err.println("   Verifica che la porta non sia già in uso");
        } catch (UnknownHostException e) {
            System.err.println("❌ Indirizzo multicast non valido: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("❌ Errore join/ricezione: " + e.getMessage());
        } finally {
            // Cleanup: lascia il gruppo e chiude il socket
            if (socket != null && group != null) {
                try {
                    socket.leaveGroup(group);
                    System.out.println("\n👋 Lasciato gruppo multicast " + group.getHostAddress());
                } catch (IOException e) {
                    System.err.println("⚠️  Errore nel lasciare il gruppo: " + e.getMessage());
                }
                socket.close();
            }
        }
        
        System.out.println("👋 Receiver terminato");
    }
    
    /**
     * Valida se l'indirizzo è un indirizzo multicast valido
     */
    private static boolean isValidMulticastAddress(String address) {
        try {
            InetAddress addr = InetAddress.getByName(address);
            return addr.isMulticastAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }
    
    /**
     * Parsing del messaggio multicast
     * Formato atteso: MULTICAST|Messaggio #N|Timestamp: T|TTL: X
     */
    private static void parseMulticastMessage(String message) {
        String[] parts = message.split("\\|");
        
        if (parts.length >= 4 && parts[0].equals("MULTICAST")) {
            System.out.println("║ 📝 Tipo: MULTICAST");
            System.out.println("║ 💬 Contenuto: " + parts[1]);
            System.out.println("║ 🕐 " + parts[2]);
            System.out.println("║ 🌐 " + parts[3]);
        } else {
            System.out.println("║ 💬 Contenuto: " + message);
        }
    }
    
    /**
     * Restituisce l'ora corrente formattata
     */
    private static String getCurrentTime() {
        java.time.LocalTime now = java.time.LocalTime.now();
        return String.format("%02d:%02d:%02d",
            now.getHour(),
            now.getMinute(),
            now.getSecond()
        );
    }
}
