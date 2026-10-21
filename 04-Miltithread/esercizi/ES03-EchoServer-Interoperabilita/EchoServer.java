import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer {
    private static final int PORTA = 5555;
    private static int clientCounter = 0;
    
    public static void main(String[] args) {
        System.out.println("=== SERVER ECHO MULTITHREADING ===");
        System.out.println("Server avviato sulla porta " + PORTA);
        System.out.println("In attesa di connessioni...\n");
        
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            
            while (true) {
                // Accetta nuova connessione
                Socket clientSocket = serverSocket.accept();
                
                // Crea thread per gestire il client
                clientCounter++;
                Thread clientThread = new Thread(new ClientHandler(clientSocket, clientCounter));
                clientThread.start();
            }
            
        } catch (IOException e) {
            System.err.println("Errore server: " + e.getMessage());
        }
    }
}

/**
 * Thread per gestire un singolo client
 */
class ClientHandler implements Runnable {
    private Socket socket;
    private int clientId;
    
    public ClientHandler(Socket socket, int clientId) {
        this.socket = socket;
        this.clientId = clientId;
    }
    
    @Override
    public void run() {
        String clientAddress = socket.getInetAddress().getHostAddress();
        System.out.println("[CONNESSIONE] Client #" + clientId + " da " + clientAddress);
        
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String messaggio;
            
            // Loop di ricezione messaggi
            while ((messaggio = in.readLine()) != null) {
                
                // Se riceve "exit", chiude la connessione
                if (messaggio.equalsIgnoreCase("exit")) {
                    System.out.println("[CLIENT #" + clientId + "] Disconnessione");
                    break;
                }
                
                // Log del messaggio ricevuto
                System.out.println("[CLIENT #" + clientId + "] Ricevuto: " + messaggio);
                
                // Invia echo al client
                out.println("ECHO: " + messaggio);
                System.out.println("[CLIENT #" + clientId + "] Inviato echo");
            }
            
            socket.close();
            System.out.println("[CLIENT #" + clientId + "] Connessione chiusa\n");
            
        } catch (IOException e) {
            System.err.println("[CLIENT #" + clientId + "] Errore: " + e.getMessage());
        }
    }
}