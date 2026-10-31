/**
 * @(#)Sender.java
 * 
 * TPSIT_3/UDPMulticast/Sender application
 * 
 * Questo programma implementa un mittente UDP multicast che invia messaggi
 * a un gruppo multicast. Tutti i receiver che si sono uniti al gruppo
 * riceveranno il messaggio.
 * 
 * NOTA: Per inviare messaggi multicast sarebbe sufficiente un DatagramSocket
 * standard. Questo esempio usa MulticastSocket per coerenza didattica.
 * 
 * Concetti chiave:
 * - MulticastSocket: Socket specializzato per comunicazioni multicast
 * - Gruppo multicast: Indirizzo IP nel range 224.0.0.0 - 239.255.255.255
 * - Un messaggio inviato al gruppo viene ricevuto da tutti i membri
 * - Non richiede conoscere gli indirizzi specifici dei receiver
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

public class _Sender {
    
    // ==================== CONFIGURAZIONE ====================
    
    /** Indirizzo IP dell'interfaccia di rete da utilizzare per il multicast */
    private static final String SENDER_IP = "127.0.0.1";
    
    /** Indirizzo IP del gruppo multicast (deve essere nel range 224.0.0.0 - 239.255.255.255) */
    private static final String MULTICAST_IP = "230.0.0.1";
    
    /** Porta UDP su cui inviare i messaggi multicast */
    private static final int MULTICAST_PORT = 19876;
    
    /** TTL (Time To Live) per i pacchetti multicast (1 = solo rete locale) */
    private static final int MULTICAST_TTL = 1;

    // ==================== MAIN ====================
    
    public static void main(String[] args) {
        MulticastSocket socket = null;
        InetAddress group = null;
        
        try {
            System.out.println("═══════════════════════════════════════════════════");
            System.out.println("📡 MULTICAST UDP SENDER");
            System.out.println("═══════════════════════════════════════════════════");
            System.out.println("🌐 Gruppo Multicast: " + MULTICAST_IP);
            System.out.println("🔌 Porta: " + MULTICAST_PORT);
            System.out.println("📍 Interfaccia: " + SENDER_IP);
            System.out.println("🌍 TTL: " + MULTICAST_TTL + " (rete locale)");
            System.out.println("═══════════════════════════════════════════════════\n");
            
            // ==================== CREAZIONE SOCKET ====================
            
            // Crea un MulticastSocket senza specificare porta
            // Il sistema assegnerà automaticamente una porta disponibile
            socket = new MulticastSocket();
            System.out.println("✅ MulticastSocket creato");
            
            // ==================== CONFIGURAZIONE ====================
            
            // Ottiene l'indirizzo del gruppo multicast
            group = InetAddress.getByName(MULTICAST_IP);
            
            // Imposta l'interfaccia di rete da utilizzare
            // Importante su sistemi con multiple interfacce di rete
            socket.setInterface(InetAddress.getByName(SENDER_IP));
            System.out.println("🔧 Interfaccia di rete configurata: " + SENDER_IP);
            
            // Imposta il TTL (Time To Live) per i pacchetti multicast
            // TTL = 1: pacchetti limitati alla rete locale (non attraversano router)
            // TTL > 1: pacchetti possono attraversare più reti
            socket.setTimeToLive(MULTICAST_TTL);
            System.out.println("⏱️  TTL impostato a: " + MULTICAST_TTL);
            
            // NOTA: joinGroup() non è strettamente necessario per il sender,
            // ma viene usato qui per coerenza con l'esempio didattico
            socket.joinGroup(group);
            System.out.println("🤝 Unito al gruppo multicast: " + MULTICAST_IP);
            
            // ==================== PREPARAZIONE MESSAGGIO ====================
            
            // Messaggio da inviare
            String message = "Messaggio da Sender";
            
            // Converte il messaggio in array di byte
            byte[] messageBytes = message.getBytes();
            
            // Crea il pacchetto DatagramPacket con:
            // - I dati del messaggio
            // - La lunghezza dei dati
            // - L'indirizzo del gruppo multicast
            // - La porta di destinazione
            DatagramPacket packet = new DatagramPacket(
                messageBytes,
                messageBytes.length,
                group,
                MULTICAST_PORT
            );
            
            System.out.println("\n📦 Preparato pacchetto:");
            System.out.println("   └─ Messaggio: \"" + message + "\"");
            System.out.println("   └─ Dimensione: " + messageBytes.length + " bytes");
            System.out.println("   └─ Destinazione: " + MULTICAST_IP + ":" + MULTICAST_PORT);
            
            // ==================== INVIO ====================
            
            System.out.println("\n📤 Invio messaggio al gruppo multicast...");
            
            // Invia il pacchetto al gruppo multicast
            // Tutti i receiver che hanno fatto joinGroup() riceveranno questo messaggio
            socket.send(packet);
            
            System.out.println("✅ Messaggio inviato con successo!");
            System.out.println("   Tutti i membri del gruppo " + MULTICAST_IP + " riceveranno il messaggio");
            
        } catch (UnknownHostException e) {
            System.err.println("❌ Errore: Indirizzo host non valido");
            System.err.println("   Verifica MULTICAST_IP: " + MULTICAST_IP);
            e.printStackTrace();
            
        } catch (SocketException e) {
            System.err.println("❌ Errore socket: " + e.getMessage());
            e.printStackTrace();
            
        } catch (IOException e) {
            System.err.println("❌ Errore I/O durante l'invio");
            e.printStackTrace();
            
        } finally {
            // ==================== CLEANUP ====================
            
            // Assicura la chiusura pulita delle risorse
            if (socket != null && group != null) {
                try {
                    // Lascia il gruppo multicast
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
            
            System.out.println("\n👋 Sender terminato\n");
        }
    }
}