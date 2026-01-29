/**
 * @file BaseReceiver.java
 * 
 * TPSIT_3/UDPMulticast/Receiver application
 * 
 * @author Filippo Bilardo
 * @version 1.00 11/11/2023
 * @version 1.10 29/01/2026
 */
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

public class BaseReceiver {

    private static final String MULTICAST_IP = "239.255.0.1";
    private static final int MULTICAST_PORT = 19876;

    public static void main(String[] args) {
        try 
        {
            // Creo il MulticastSocket per lo scambio dei dati con il client
            try (MulticastSocket socket = new MulticastSocket(MULTICAST_PORT))
            {
                // Creo il gruppo multicast di destinazione
                InetAddress group = InetAddress.getByName(MULTICAST_IP);
                InetSocketAddress groupAddress = new InetSocketAddress(group, MULTICAST_PORT);
                // Abilito il loopback per testare su localhost
                socket.setOption(java.net.StandardSocketOptions.IP_MULTICAST_LOOP, true);
                // Aderisco al gruppo multicast, null per usare l'interfaccia di default
                socket.joinGroup(groupAddress, null);
                
                System.out.println("RECEIVER: Gruppo multicast: " + MULTICAST_IP + ":" + MULTICAST_PORT);
                System.out.println("RECEIVER: Socket bound to: " + socket.getLocalSocketAddress());

                //Mi preparo alla ricezione dei dati
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                System.out.println("RECEIVER: Receiver in ascolto. Ctrl+C per terminare.");
                
                try {
                    //Ricevo il DatagramPacket dal client e lo stampo a schermo
                    String message = "";
                    while (!message.equals("bye")) {
                        System.out.println("RECEIVER: In attesa di messaggi...");
                        socket.receive(packet);
                        message = new String(packet.getData(), 0, packet.getLength());
                        System.out.println("RECEIVER: Messaggio ricevuto: " + message);
                        System.out.println("RECEIVER: Da: " + packet.getAddress() + ":" + packet.getPort());
                    }
                } finally {
                    //Fine della comunicazione - abbandono il gruppo multicast
                    socket.leaveGroup(groupAddress, null);
                    System.out.println("RECEIVER: Abbandonato il gruppo multicast. Termino il receiver.");
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}