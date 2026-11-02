import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
/** bye termina solo questo client; il server resta disponibile agli altri. */
public class Client {
    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 9876;
        try (DatagramSocket socket = new DatagramSocket()) {
            InetSocketAddress peer = new InetSocketAddress(InetAddress.getByName(host), port);
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = console.readLine()) != null) {
                byte[] data = line.getBytes(StandardCharsets.UTF_8);
                if (data.length > 1024) { System.err.println("Max 1024 byte"); continue; }
                socket.send(new DatagramPacket(data, data.length, peer));
                byte[] buffer = new byte[1025];
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(2);
                try {
                    do {
                        long left = deadline - System.nanoTime();
                        if (left <= 0) throw new SocketTimeoutException();
                        socket.setSoTimeout((int)Math.max(1, java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(left)));
                        reply.setLength(buffer.length); socket.receive(reply);
                    } while (!reply.getSocketAddress().equals(peer));
                } catch (SocketTimeoutException e) { System.err.println("TIMEOUT: esito non noto"); break; }
                if (reply.getLength() > 1024) throw new IOException("Risposta troppo grande");
                System.out.println("ECHO " + new String(reply.getData(), 0, reply.getLength(), StandardCharsets.UTF_8));
                if (line.equals("bye")) break;
            }
        }
    }
}
