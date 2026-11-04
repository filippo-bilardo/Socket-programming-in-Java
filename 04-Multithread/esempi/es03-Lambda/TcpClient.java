/**
 * Laboratorio: es03-Lambda, TcpClient.java. JDK 11+.
 * Porta 8765; protocollo: righe UTF-8; una risposta Echo per richiesta, EOF chiude.
 * La lambda implementa Runnable; ogni iterazione cattura un socket distinto.
 * Compilare questa cartella separatamente dalle altre varianti.
 *
 * TCP Client che si connette a un server, invia messaggi e riceve risposte.
 * Utilizza PrintWriter e BufferedReader per la comunicazione.
 * 
 * Assicurarsi che il server TCP sia in esecuzione prima di avviare questo client.
 * 
 * javac -encoding UTF-8 TcpClient.java && java TcpClient
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
        try (Socket socket = new Socket(HOST, PORT);  // La connessione TCP è gestita dal sistema operativo.
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8);
             InputStreamReader isr = new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8);
             BufferedReader in = new BufferedReader(isr);
             Scanner scanner = new Scanner(System.in)) 
        {    
            socket.setSoTimeout(30_000); // Evita un'attesa di risposta senza scadenza.
            System.out.println("Connesso al server " + HOST + ":" + PORT);
            
            String userInput;
            while (true) {
                System.out.print("Messaggio (o 'quit'): ");
                if (!scanner.hasNextLine()) break; // EOF della console: uscita ordinata.
                userInput = scanner.nextLine();
                if ("quit".equalsIgnoreCase(userInput)) break;
                
                out.println(userInput);           // Invia al server
                String response = in.readLine();  // Ricevi risposta
                if (response == null) break; // EOF: il server ha chiuso, non inviare ancora.
                System.out.println("Server: " + response);
            }
            
        } catch (IOException e) {
            System.err.println("Errore client: " + e.getMessage());
        } // Il try-with-resources chiude socket e stream anche su errore.
        
        System.out.println("Client disconnesso.");
    }
}
