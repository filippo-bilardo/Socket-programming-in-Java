import java.net.*;
import java.nio.charset.StandardCharsets;
/** java Receiver [porta] [gruppo] [numero-messaggi; 0=illimitato] */
public class Receiver {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9876;
        InetAddress group = MulticastSupport.group(args.length > 1 ? args[1] : "239.255.0.1");
        int limit = args.length > 2 ? Integer.parseInt(args[2]) : 0;
        if (port < 1 || port > 65535 || limit < 0) throw new IllegalArgumentException("Parametri non validi");
        NetworkInterface ni = MulticastSupport.networkInterface();
        try (MulticastSocket socket = MulticastSupport.receiver(group, port, ni)) {
            if (limit > 0) socket.setSoTimeout(5000);
            System.out.println("PRONTO " + group.getHostAddress() + ":" + port + " interfaccia=" + ni.getName());
            byte[] data = new byte[1025];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                for (int count = 0; limit == 0 || count < limit; count++) {
                    packet.setLength(data.length);
                    socket.receive(packet);
                    if (packet.getLength() > 1024) { System.err.println("SCARTATO oltre 1024 byte"); count--; continue; }
                    String message = new String(data, 0, packet.getLength(), StandardCharsets.UTF_8);
                    System.out.println("RICEVUTO " + message);
                    if (message.equals("bye")) break;
                }
            } finally { socket.leaveGroup(new InetSocketAddress(group, port), ni); }
        }
    }
}
