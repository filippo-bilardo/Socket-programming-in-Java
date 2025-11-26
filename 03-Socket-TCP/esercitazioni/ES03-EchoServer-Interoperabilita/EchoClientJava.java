import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class EchoClientJava {
    private static final String HOST = "localhost";
    private static final int PORTA = 5555;
    
    public static void main(String[] args) {
        System.out.println("=== CLIENT JAVA - ECHO ===");
        
        try (
            Socket socket = new Socket(HOST, PORTA);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("✓ Connesso al server " + HOST + ":" + PORTA);
            System.out.println("Digita 'exit' per disconnetterti\n");
            
            while (true) {
                // Leggi input utente
                System.out.print("Inserisci messaggio: ");
                String messaggio = scanner.nextLine();
                
                // Invia al server
                out.println(messaggio);
                
                // Se exit, chiudi
                if (messaggio.equalsIgnoreCase("exit")) {
                    System.out.println("Disconnessione...");
                    break;
                }
                
                // Ricevi echo dal server
                String risposta = in.readLine();
                System.out.println("← Server: " + risposta + "\n");
            }
            
            System.out.println("Connessione chiusa.");
            
        } catch (IOException e) {
            System.err.println("Errore connessione: " + e.getMessage());
        }
    }
}