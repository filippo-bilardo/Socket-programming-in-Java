import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
public class DaytimeClient {
    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 1313;
        try (DatagramSocket socket = new DatagramSocket()) {
            InetSocketAddress peer = new InetSocketAddress(InetAddress.getByName(host), port);
            socket.send(new DatagramPacket(new byte[0], 0, peer));
            byte[] buffer = new byte[128];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(2);
            do {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) throw new SocketTimeoutException();
                socket.setSoTimeout((int)Math.max(1, java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(remaining)));
                reply.setLength(buffer.length); socket.receive(reply);
            } while (!reply.getSocketAddress().equals(peer));
            String text = new String(reply.getData(), 0, reply.getLength(), StandardCharsets.US_ASCII).trim();
            Instant.parse(text); // Verifica il formato effettivamente adottato dal laboratorio.
            System.out.println(text);
        }
    }
}
