/**
 * @(#)Receiver.java
 * 
 * TPSIT_3/UDPMulticast/Receiver application
 * 
 * @author Filippo Bilardo
 * @version 1.00 11/11/2023
 * @version 1.10 25/01/2026
 */
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Receiver {

    private static final String MULTICAST_IP = "239.0.0.1";
    private static final int MULTICAST_PORT = 9876;

    public static void main(String[] args) {

        // Utilizzo try-with-resources per gestire automaticamente 
        // la chiusura del socket
        try (MulticastSocket socket = new MulticastSocket(MULTICAST_PORT)) 
        {
            // Mi unisco al gruppo multicast per poter ricevere i messaggi
            InetAddress group = InetAddress.getByName(MULTICAST_IP);
            socket.joinGroup(group);

            // Mi preparo alla ricezione dei dati
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            System.out.println("Receiver in ascolto. Ctrl+C per terminare.");
            
            // Ricevo il DatagramPacket dal client e lo stampo a schermo
            // Termino la ricezione quando ricevo il messaggio "bye"
            String message = "";
            while (!message.equals("bye")) {
                socket.receive(packet);
                message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Messaggio ricevuto: " + message);
            } 
            
            //Fine della comunicazione
            System.out.println("Receiver terminato.");
            socket.leaveGroup(group);        
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
}