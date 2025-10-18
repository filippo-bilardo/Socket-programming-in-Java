import java.io.*;
import java.net.*;

public class TCPServer {
    private static final int PORT = 8080;
    
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server TCP avviato sulla porta " + PORT);
            
            while (true) {
                // Accept() esegue 3-way handshake automaticamente
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connesso: " + 
                    clientSocket.getRemoteSocketAddress());
                
                // Gestisci client (in questo esempio: thread separato)
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Errore server: " + e.getMessage());
        }
    }
    
    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(
                clientSocket.getOutputStream(), true)) {
            
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Ricevuto: " + inputLine);
                out.println("Echo: " + inputLine);  // Echo back
            }
        } catch (IOException e) {
            System.err.println("Errore gestione client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close(); // 4-way handshake automatico
            } catch (IOException e) {
                System.err.println("Errore chiusura: " + e.getMessage());
            }
        }
    }
}
