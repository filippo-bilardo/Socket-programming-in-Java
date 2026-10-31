/** Client dell'esempio a: protocollo binario Java writeUTF/readUTF. */
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TcpClient {
    public static void main(String[] args) {
        String serverAddress = args.length > 0 ? args[0] : "127.0.0.1";
        int serverPort = args.length > 1 ? Integer.parseInt(args[1]) : 8765;

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(serverAddress, serverPort), 5000);
            socket.setSoTimeout(10000);
            try (DataInputStream input = new DataInputStream(socket.getInputStream());
                 DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                 BufferedReader keyboard = new BufferedReader(
                         new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
                System.out.println("Client connesso a " + serverAddress + ":" + serverPort);
                while (true) {
                    System.out.print("Messaggio (quit per uscire)> ");
                    String message = keyboard.readLine();
                    if (message == null) {
                        break; // EOF da tastiera: non inviare null a writeUTF.
                    }
                    output.writeUTF(message);
                    output.flush();
                    System.out.println(input.readUTF());
                    if (message.equals("quit")) {
                        break;
                    }
                }
            }
        } catch (EOFException error) {
            System.err.println("Il server ha chiuso la connessione.");
        } catch (IOException error) {
            System.err.println("Errore client: " + error.getMessage());
        }
    }
}
