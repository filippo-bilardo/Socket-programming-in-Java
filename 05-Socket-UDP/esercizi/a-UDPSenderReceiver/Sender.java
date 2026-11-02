import java.net.*;
import java.nio.charset.StandardCharsets;
/** Un solo datagramma UTF-8. java Sender [host] [porta] [messaggio] */
public class Sender {
    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8698;
        byte[] data = (args.length > 2 ? args[2] : "Ciao, UDP!").getBytes(StandardCharsets.UTF_8);
        if (port < 1 || port > 65535 || data.length > 1024) throw new IllegalArgumentException("Porta o messaggio non valido (max 1024 byte)");
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.send(new DatagramPacket(data, data.length, InetAddress.getByName(host), port));
            System.out.println("INVIATO " + data.length + " byte");
        }
    }
}
