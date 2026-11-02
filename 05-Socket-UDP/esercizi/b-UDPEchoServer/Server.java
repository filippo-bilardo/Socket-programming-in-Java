import java.net.*;
/** Riferimento esercizio echo: ogni datagramma, anche bye, riceve l'echo. */
public class Server {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9876;
        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] data = new byte[1025];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            System.out.println("PRONTO " + port);
            while (true) {
                packet.setLength(data.length);
                socket.receive(packet);
                if (packet.getLength() <= 1024)
                    socket.send(new DatagramPacket(packet.getData(), packet.getOffset(), packet.getLength(), packet.getSocketAddress()));
            }
        }
    }
}
