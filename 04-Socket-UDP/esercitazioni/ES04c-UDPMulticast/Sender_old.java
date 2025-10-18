/**
 * @(#)Sender.java
 * 
 * TPSIT_3/UDPMulticast/Sender application
 * 
 * @author Filippo Bilardo
 * @version 1.00 11/11/2023
 */
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Sender 
{
    private static final String SENDER_IP = "127.0.0.1";
    private static final String MULTICAST_IP = "230.0.0.1";
    private static final int MULTICAST_PORT = 19876;

    public static void main(String[] args) 
    {
        try 
        {
            //Creo il MulticastSocket per lo scambio dei dati con il client
            MulticastSocket socket = new MulticastSocket();
            InetAddress group = InetAddress.getByName(MULTICAST_IP);
            socket.setInterface(InetAddress.getByName(SENDER_IP));
            socket.joinGroup(group); 

            //Invio il DatagramPacket al gruppo di multicast
            String message = "Messaggio da Sender";
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), group, MULTICAST_PORT);
            socket.send(packet);

            //Fine della comunicazione
            socket.leaveGroup(group);
            socket.close();

            System.out.println("Messaggio inviato al gruppo di multicast.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
