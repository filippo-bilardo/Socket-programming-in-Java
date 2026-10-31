/**
 * UdpSender.java
 * Esempio di mittente UDP in Java
 * Invia un messaggio UDP a un ricevitore
 * Versione: 1.0
 * Data: 18/10/25
 * Autore: Filippo Bilardo
 *
 * javac UdpSender.java && java UdpSender
 */
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpSender {
    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket();
            String message = "Hello, UDP!";
            byte[] buffer = message.getBytes();
            InetAddress address = InetAddress.getByName("localhost");

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 9876);
            socket.send(packet);
            System.out.println("Messaggio inviato!");
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}