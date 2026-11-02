import java.net.*;
import java.nio.charset.StandardCharsets;
/** Un ricevitore per host/porta; java BroadcastReceiver [porta] [numero-messaggi; 0=illimitato] */
public class BroadcastReceiver {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 5000;
        int limit = args.length > 1 ? Integer.parseInt(args[1]) : 0;
        if (port < 1 || port > 65535 || limit < 0) throw new IllegalArgumentException("Parametri non validi");
        try (DatagramSocket socket = new DatagramSocket(port)) {
            if (limit > 0) socket.setSoTimeout(5000);
            System.out.println("PRONTO " + port);
            byte[] data = new byte[1025];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            for (int i = 0; limit == 0 || i < limit; i++) {
                packet.setLength(data.length); socket.receive(packet);
                if (packet.getLength() > 1024) { i--; continue; }
                System.out.println("RICEVUTO " + new String(data, 0, packet.getLength(), StandardCharsets.UTF_8));
            }
        }
    }
}
