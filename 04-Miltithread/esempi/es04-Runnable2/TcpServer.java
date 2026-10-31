/** Server echo con gestore Runnable nella stessa classe. Porta 8080.
 * javac TcpServer.java && java TcpServer
 */
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class TcpServer implements Runnable {
    private final Socket clientSocket;
    public TcpServer(Socket socket) { this.clientSocket = socket; }
    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(8080)) {
            System.out.println("Server TCP avviato sulla porta 8080");
            while (true) new Thread(new TcpServer(server.accept())).start();
        } catch (IOException e) {
            System.err.println("Errore server: " + e.getMessage());
        }
    }
    @Override public void run() {
        try (Socket socket = clientSocket;
             BufferedReader in = new BufferedReader(new InputStreamReader(
                 socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {
            String line;
            while ((line = in.readLine()) != null) out.println("Echo: " + line);
        } catch (IOException e) {
            System.err.println("Errore client: " + e.getMessage());
        }
    }
}
