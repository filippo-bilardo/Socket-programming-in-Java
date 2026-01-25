import java.net.*;
import java.io.*;

/**
 * Esempio 02.02 - Broadcast UDP Receiver (Java)
 * 
 * Questo esempio dimostra come ricevere pacchetti broadcast UDP.
 * Il receiver ascolta su una porta specifica e riceve tutti i messaggi
 * broadcast inviati a quella porta sulla rete locale.
 * 
 * Caratteristiche:
 * - Ricezione di messaggi broadcast
 * - Visualizzazione informazioni mittente
 * - Parsing e validazione messaggi
 * 
 * Compilazione: javac BroadcastReceiver.java
 * Esecuzione: java BroadcastReceiver [porta]
 * Esempio: java BroadcastReceiver 5000
 */
public class BroadcastReceiver {
    private static final int DEFAULT_PORT = 5000;
    private static final int BUFFER_SIZE = 1024;
    
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        
        // Parsing argomenti da linea di comando
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("❌ Porta non valida. Uso porta di default: " + DEFAULT_PORT);
            }
        }
        
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("📡 BROADCAST UDP RECEIVER");
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("🔌 Porta: " + port);
        System.out.println("═══════════════════════════════════════════════════\n");
        
        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("✅ Socket in ascolto sulla porta " + port);
            System.out.println("👂 In attesa di messaggi broadcast...\n");
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
                System.out.printf("║ 📥 MESSAGGIO #%d RICEVUTO%n", messageCount);
                System.out.println("╠═══════════════════════════════════════════════════");
                System.out.printf("║ ⏰ Ora: %s%n", getCurrentTime());
                System.out.printf("║ 👤 Mittente: %s:%d%n", senderAddress.getHostAddress(), senderPort);
                System.out.printf("║ 📦 Dimensione: %d bytes%n", packet.getLength());
                System.out.println("╠═══════════════════════════════════════════════════");
                
                // Parsing del messaggio
                parseBroadcastMessage(message);
                
                System.out.println("╚═══════════════════════════════════════════════════\n");
            }
            
        } catch (SocketException e) {
            System.err.println("❌ Errore creazione socket: " + e.getMessage());
            System.err.println("   Verifica che la porta non sia già in uso");
        } catch (IOException e) {
            System.err.println("❌ Errore ricezione pacchetto: " + e.getMessage());
        }
        
        System.out.println("\n👋 Receiver terminato");
    }
    
    /**
     * Parsing del messaggio broadcast
     * Formato atteso: BROADCAST|Messaggio #N|Timestamp: T
     */
    private static void parseBroadcastMessage(String message) {
        String[] parts = message.split("\\|");
        
        if (parts.length >= 3 && parts[0].equals("BROADCAST")) {
            System.out.println("║ 📝 Tipo: BROADCAST");
            System.out.println("║ 💬 Contenuto: " + parts[1]);
            System.out.println("║ 🕐 " + parts[2]);
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
