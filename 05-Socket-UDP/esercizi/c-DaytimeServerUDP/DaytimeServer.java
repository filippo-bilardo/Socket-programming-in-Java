import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
/** Daytime di laboratorio, porta 1313 anziché la porta standard 13. */
public class DaytimeServer {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 1313;
        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("PRONTO " + port);
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request); // È valida anche una richiesta con payload vuoto.
                byte[] data = (Instant.now().toString() + "\r\n").getBytes(StandardCharsets.US_ASCII);
                socket.send(new DatagramPacket(data, data.length, request.getSocketAddress()));
            }
        }
    }
}
