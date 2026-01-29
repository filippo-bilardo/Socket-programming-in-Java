/**
 * @(#)Receiver.java
 * 
 * TPSIT_3/UDPMulticast/Receiver application
 * 
 * Questo programma implementa un ricevitore UDP multicast che si unisce
 * a un gruppo multicast e riceve messaggi inviati a quel gruppo.
 * 
 * Concetti chiave:
 * - MulticastSocket: Socket specializzato per comunicazioni multicast
 * - joinGroup(): Unisce il socket a un gruppo multicast
 * - leaveGroup(): Lascia il gruppo multicast
 * - Indirizzo multicast: 230.0.0.1 (range 224.0.0.0 - 239.255.255.255)
 * 
 * @author Filippo Bilardo
 * @version 1.00 11/11/2023
 * @version 1.10 25/01/2026
 */
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class _Receiver {

    // ==================== CONFIGURAZIONE ====================
    
    /** Indirizzo IP dell'interfaccia di rete da utilizzare per il multicast */
    private static final String RECEIVER_IP = "127.0.0.1";
    
    /** Indirizzo IP del gruppo multicast (deve essere nel range 224.0.0.0 - 239.255.255.255) */
    private static final String MULTICAST_IP = "230.0.0.1";
    
    /** Porta UDP su cui ricevere i messaggi multicast */
    private static final int MULTICAST_PORT = 19876;
    
    /** Dimensione del buffer per la ricezione dei pacchetti */
    private static final int BUFFER_SIZE = 1024;
    
    /** Messaggio che indica la fine della comunicazione */
    private static final String EXIT_MESSAGE = "bye";

    // ==================== MAIN ====================
    
    public static void main(String[] args) {
        MulticastSocket socket = null;
        InetAddress group = null;
        
        try {
            System.out.println("═══════════════════════════════════════════════════");
            System.out.println("📡 MULTICAST UDP RECEIVER");
            System.out.println("═══════════════════════════════════════════════════");
            System.out.println("🌐 Gruppo Multicast: " + MULTICAST_IP);
            System.out.println("🔌 Porta: " + MULTICAST_PORT);
            System.out.println("📍 Interfaccia: " + RECEIVER_IP);
            System.out.println("═══════════════════════════════════════════════════\n");
            
            // ==================== CREAZIONE SOCKET ====================
            
            // Crea un MulticastSocket sulla porta specificata
            // Il MulticastSocket estende DatagramSocket con funzionalità multicast
            socket = new MulticastSocket(MULTICAST_PORT);
            System.out.println("✅ MulticastSocket creato sulla porta " + MULTICAST_PORT);
            
            // ==================== CONFIGURAZIONE GRUPPO ====================
            
            // Ottiene l'indirizzo del gruppo multicast
            group = InetAddress.getByName(MULTICAST_IP);
            
            // Imposta l'interfaccia di rete da utilizzare per il multicast
            // Utile su sistemi con multiple interfacce di rete
            socket.setInterface(InetAddress.getByName(RECEIVER_IP));
            System.out.println("🔧 Interfaccia di rete configurata: " + RECEIVER_IP);
            
            // IMPORTANTE: Unisce il socket al gruppo multicast
            // Solo dopo joinGroup() il socket riceverà i pacchetti multicast
            socket.joinGroup(group);
            System.out.println("🤝 Unito al gruppo multicast: " + MULTICAST_IP);
            
            // ==================== PREPARAZIONE RICEZIONE ====================
            
            // Buffer per ricevere i dati
            byte[] buffer = new byte[BUFFER_SIZE];
            
            // Pacchetto che conterrà i dati ricevuti
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            
            System.out.println("\n👂 Receiver in ascolto...");
            System.out.println("💡 Invia 'bye' per terminare la comunicazione\n");
            System.out.println("Ctrl+C per forzare la chiusura\n");
            
            // ==================== LOOP RICEZIONE ====================
            
            String message = "";
            int messageCount = 0;
            
            // Continua a ricevere messaggi fino a quando non arriva "bye"
            // NOTA: Usa equals() per confrontare stringhe, non ==
            while (!message.equals(EXIT_MESSAGE)) {
                // Riceve un pacchetto (chiamata bloccante)
                socket.receive(packet);
                
                // Estrae il messaggio dal pacchetto ricevuto
                // Importante: usa getLength() per evitare caratteri spuri
                message = new String(packet.getData(), 0, packet.getLength());
                
                // Ottiene informazioni sul mittente
                InetAddress senderAddress = packet.getAddress();
                int senderPort = packet.getPort();
                
                messageCount++;
                
                // Stampa il messaggio ricevuto con informazioni dettagliate
                System.out.println("╔═══════════════════════════════════════════════════");
                System.out.printf("║ 📥 MESSAGGIO #%d RICEVUTO%n", messageCount);
                System.out.println("╠═══════════════════════════════════════════════════");
                System.out.printf("║ 👤 Da: %s:%d%n", senderAddress.getHostAddress(), senderPort);
                System.out.printf("║ 📦 Dimensione: %d bytes%n", packet.getLength());
                System.out.printf("║ 💬 Contenuto: %s%n", message);
                System.out.println("╚═══════════════════════════════════════════════════\n");
                
                // Verifica se è il messaggio di uscita
                if (message.equals(EXIT_MESSAGE)) {
                    System.out.println("🛑 Ricevuto messaggio di terminazione");
                    break;
                }
            }
            
        } catch (UnknownHostException e) {
            System.err.println("❌ Errore: Indirizzo host non valido");
            System.err.println("   Verifica MULTICAST_IP: " + MULTICAST_IP);
            e.printStackTrace();
            
        } catch (SocketException e) {
            System.err.println("❌ Errore socket: " + e.getMessage());
            System.err.println("   Verifica che la porta " + MULTICAST_PORT + " non sia già in uso");
            e.printStackTrace();
            
        } catch (IOException e) {
            System.err.println("❌ Errore I/O durante la comunicazione");
            e.printStackTrace();
            
        } finally {
            // ==================== CLEANUP ====================
            
            // Assicura la chiusura pulita delle risorse
            if (socket != null && group != null) {
                try {
                    // IMPORTANTE: Lascia il gruppo multicast prima di chiudere
                    socket.leaveGroup(group);
                    System.out.println("\n👋 Lasciato gruppo multicast: " + group.getHostAddress());
                    
                } catch (IOException e) {
                    System.err.println("⚠️  Errore nel lasciare il gruppo multicast");
                }
            }
            
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("🔒 Socket chiuso");
            }
            
            System.out.println("\n👋 Receiver terminato\n");
        }
    }
}