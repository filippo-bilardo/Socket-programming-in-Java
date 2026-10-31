/** Server dell'esempio a: una sessione con framing binario Java. */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {
    public static void main(String[] args) {
        int serverPort = args.length > 0 ? Integer.parseInt(args[0]) : 8765;
        try (ServerSocket server = new ServerSocket(serverPort)) {
            System.out.println("Server in ascolto sulla porta " + serverPort);
            try (Socket client = server.accept();
                 DataInputStream input = new DataInputStream(client.getInputStream());
                 DataOutputStream output = new DataOutputStream(client.getOutputStream())) {
                while (true) {
                    String message = input.readUTF();
                    System.out.println("Ricevuto: " + message);
                    output.writeUTF("Echo from server : " + message);
                    output.flush();
                    if (message.equals("quit")) {
                        break;
                    }
                }
            }
        } catch (EOFException error) {
            System.out.println("Client disconnesso senza inviare quit.");
        } catch (IOException error) {
            System.err.println("Errore server: " + error.getMessage());
        }
    }
}
