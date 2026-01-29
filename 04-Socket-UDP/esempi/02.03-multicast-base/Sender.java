/**
 * @(#)Sender.java
 * 
 * TPSIT_3/UDPMulticast/Sender application
 * versione 1.2 con try-with-resources e DatagramSocket
 * 
 * @author Filippo Bilardo
 * @version 1.00 11/11/2023
 * @version 1.10 25/01/2026
 * @version 1.20 25/01/2026
 */
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Sender {
    
    private static final String MULTICAST_IP = "239.0.0.1";
    private static final int MULTICAST_PORT = 9876;

    public static void main(String[] args) {
        
        // Utilizzo try-with-resources per gestire automaticamente 
        // la chiusura del socket
        try (DatagramSocket socket = new DatagramSocket()) {
            
            // Preparo l'indirizzo del gruppo multicast
            InetAddress group = InetAddress.getByName(MULTICAST_IP);

            // Invio il messaggio al gruppo multicast
            String message = "Messaggio da Sender";
            DatagramPacket packet = new DatagramPacket(
                message.getBytes(), 
                message.length(), 
                group, 
                MULTICAST_PORT
            );
            socket.send(packet);

            System.out.println("Messaggio inviato al gruppo di multicast.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}