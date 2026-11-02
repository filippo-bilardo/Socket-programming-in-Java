import java.net.*;
import java.nio.charset.StandardCharsets;
/** Un datagramma al gruppo. java Sender [messaggio] [porta] [gruppo] */
public class Sender {
    public static void main(String[] args) throws Exception {
        String message = args.length > 0 ? args[0] : "Messaggio da Sender: è UDP";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 19876;
        InetAddress group = MulticastSupport.group(args.length > 2 ? args[2] : "239.255.0.1");
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        if (port < 1 || port > 65535 || data.length > 1024) throw new IllegalArgumentException("Porta o lunghezza non valida");
        NetworkInterface ni = MulticastSupport.networkInterface();
        try (MulticastSocket socket = new MulticastSocket()) {
            socket.setNetworkInterface(ni); socket.setTimeToLive(1);
            socket.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
            socket.send(new DatagramPacket(data, data.length, group, port));
            System.out.println("INVIATO " + data.length + " byte su " + ni.getName());
        }
    }
}
