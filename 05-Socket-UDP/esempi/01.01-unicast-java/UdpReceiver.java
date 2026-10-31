/**
 * UdpReceiver.java
 * Esempio di ricevitore UDP in Java
 * Riceve un messaggio UDP e lo stampa a schermo
 * Versione: 1.0
 * Data: 18/10/25
 * Autore: Filippo Bilardo
 * 
 * javac UdpReceiver.java && java UdpReceiver
 */
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpReceiver {
    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket(9876);
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            System.out.println("In attesa di messaggi...");
            socket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Messaggio ricevuto: " + message);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}