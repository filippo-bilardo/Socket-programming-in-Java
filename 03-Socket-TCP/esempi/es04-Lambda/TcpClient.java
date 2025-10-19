/**
 * TCP Client che si connette a un server, invia messaggi e riceve risposte.
 * Utilizza PrintWriter e BufferedReader per la comunicazione.
 * 
 * Assicurarsi che il server TCP sia in esecuzione prima di avviare questo client.
 * 
 * javac TcpClient.java && java TcpClient
 * 
 * version 1.0 - 18/10/25
 * author Filippo Bilardo
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TcpClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8765;
    
    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);  // 3-way handshake
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             InputStreamReader isr = new InputStreamReader(socket.getInputStream());
             BufferedReader in = new BufferedReader(isr);
             Scanner scanner = new Scanner(System.in)) 
        {    
            System.out.println("Connesso al server " + HOST + ":" + PORT);
            
            String userInput;
            while (true) {
                System.out.print("Messaggio (o 'quit'): ");
                userInput = scanner.nextLine();
                if ("quit".equalsIgnoreCase(userInput)) break;
                
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

