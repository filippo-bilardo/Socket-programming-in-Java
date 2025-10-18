import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TCPClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    
    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);  // 3-way handshake
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {
            
            System.out.println("Connesso al server " + HOST + ":" + PORT);
            
            String userInput;
            while (true) {
                System.out.print("Messaggio (o 'quit'): ");
                userInput = scanner.nextLine();
                
                if ("quit".equalsIgnoreCase(userInput)) {
                    break;
                }
                
                out.println(userInput);           // Invia al server
                String response = in.readLine();  // Ricevi risposta
                System.out.println("Server: " + response);
            }
            
        } catch (IOException e) {
            System.err.println("Errore client: " + e.getMessage());
        } // close() automatico → 4-way handshake
        
        System.out.println("Client disconnesso.");
    }
}

