import java.net.*;
import java.nio.charset.StandardCharsets;
/** java MulticastSender [gruppo] [porta] [intervallo-ms] [ttl] [conteggio; 0=illimitato] */
public class MulticastSender {
    public static void main(String[] args) throws Exception {
        InetAddress group = MulticastSupport.group(args.length > 0 ? args[0] : "239.255.0.1");
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5000;
        int interval = args.length > 2 ? Integer.parseInt(args[2]) : 2000;
        int ttl = args.length > 3 ? Integer.parseInt(args[3]) : 1;
        int limit = args.length > 4 ? Integer.parseInt(args[4]) : 0;
        if (port < 1 || port > 65535 || interval < 1 || ttl < 0 || ttl > 255 || limit < 0)
            throw new IllegalArgumentException("Porta 1..65535, intervallo positivo, TTL 0..255, conteggio >= 0");
        try (MulticastSocket socket = new MulticastSocket()) {
            socket.setNetworkInterface(MulticastSupport.networkInterface());
            socket.setTimeToLive(ttl); socket.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
            for (int i = 1; limit == 0 || i <= limit; i++) {
                String text = "MULTICAST|Messaggio #" + i + "|Timestamp: " + System.currentTimeMillis() + "|TTL: " + ttl;
                byte[] data = text.getBytes(StandardCharsets.UTF_8);
                socket.send(new DatagramPacket(data, data.length, group, port));
                System.out.println("INVIATO " + text);
                if (limit == 0 || i < limit) Thread.sleep(interval);
            }
        }
    }
}
