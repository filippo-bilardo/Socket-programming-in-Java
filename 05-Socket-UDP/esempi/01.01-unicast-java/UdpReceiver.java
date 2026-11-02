import java.net.*;
import java.nio.charset.StandardCharsets;
/** Riceve un datagramma, poi termina. java UdpReceiver [porta] */
public class UdpReceiver {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9876;
        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] data = new byte[1025]; // Un byte sentinella per rifiutare oltre 1024 byte.
            DatagramPacket packet = new DatagramPacket(data, data.length);
            System.out.println("PRONTO " + socket.getLocalPort());
            socket.receive(packet);
            if (packet.getLength() > 1024) throw new IllegalArgumentException("Datagramma troppo grande");
            System.out.println("RICEVUTO " + new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8));
        }
    }
}
