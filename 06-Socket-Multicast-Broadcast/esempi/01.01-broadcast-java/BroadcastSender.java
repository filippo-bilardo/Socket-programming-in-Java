import java.net.*;
import java.nio.charset.StandardCharsets;
/** java BroadcastSender [porta] [intervallo-ms] [indirizzo] [conteggio; 0=illimitato] */
public class BroadcastSender {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 5000;
        int interval = args.length > 1 ? Integer.parseInt(args[1]) : 2000;
        InetAddress target = InetAddress.getByName(args.length > 2 ? args[2] : "255.255.255.255");
        int limit = args.length > 3 ? Integer.parseInt(args[3]) : 0;
        if (!(target instanceof Inet4Address) || port < 1 || port > 65535 || interval < 1 || limit < 0)
            throw new IllegalArgumentException("IPv4, porta 1..65535, intervallo positivo, conteggio >= 0");
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            for (int i = 1; limit == 0 || i <= limit; i++) {
                String message = "BROADCAST|Messaggio #" + i + "|Timestamp: " + System.currentTimeMillis();
                byte[] data = message.getBytes(StandardCharsets.UTF_8);
                socket.send(new DatagramPacket(data, data.length, target, port));
                System.out.println("INVIATO " + message);
                if (limit == 0 || i < limit) Thread.sleep(interval);
            }
        }
    }
}
