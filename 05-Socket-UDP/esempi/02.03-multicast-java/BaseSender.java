/**
 * @file BaseSender.java
 * 
 * TPSIT_3/UDPMulticast/Sender application
 * version 1.1 non serve il MulticastSocket per inviare un messaggio
 * è sufficiente un DatagramSocket
 *  

echo "------- Test Multicast UDP in Java -------" > log
javac BaseSender.java BaseReceiver.java 
java BaseReceiver >> log 2>&1 & 
java BaseSender >> log 2>&1
echo "------- Fine Test -------" >> log
echo
cat log

* 
 * @author Filippo Bilardo
 * @version 1.00 11/11/2023
 * @version 1.10 29/01/2026
 */
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class BaseSender 
{
    private static final String MULTICAST_IP = "230.0.0.1";
    private static final int MULTICAST_PORT = 19876;

    public static void main(String[] args) 
    {
        // Se voglio sia trasmettere che ricevere devo usare il MulticastSocket
        // per inviare i messaggi, anche multicast, altrimenti basta un DatagramSocket
        try (MulticastSocket socket = new MulticastSocket())
        {
            //Abilita il loopback per testare su localhost  
            socket.setOption(java.net.StandardSocketOptions.IP_MULTICAST_LOOP, true);
            
            //Creo il gruppo multicast di destinazione
            InetAddress group = InetAddress.getByName(MULTICAST_IP);

            //Invio tre messaggi al gruppo di multicast
            String[] messages = {"messaggio 1", "messaggio 2", "bye"};
            for (String message : messages) {
                DatagramPacket packet = new DatagramPacket(
                    message.getBytes(),
                    message.length(),
                    group,
                    MULTICAST_PORT
                );
                socket.send(packet);
                System.out.println("SENDER: Messaggio inviato: " + message);
                Thread.sleep(500); // Pausa di 500ms tra un messaggio e l'altro
            }

            System.out.println("SENDER: Tutti i messaggi sono stati inviati al gruppo di multicast.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}