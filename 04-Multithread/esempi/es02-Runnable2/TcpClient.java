/**
 * Laboratorio: es02-Runnable2, TcpClient.java. JDK 11+.
 * Porta 8080; protocollo: righe UTF-8; una risposta Echo per richiesta, EOF chiude.
 * La stessa classe contiene main e run, ma ogni connessione ha una nuova istanza.
 * Compilare questa cartella separatamente dalle altre varianti.
 *
 *
 * TCPClient.java - Esempio di un semplice client TCP che invia messaggi a un server TCP
 * e riceve risposte echo dal server. 
 *
 * javac -encoding UTF-8 TcpClient.java && java TcpClient
 *
 * versione: 1.0 - 18/10/25
 * autore: Filippo Bilardo
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TcpClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    
    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);  // La connessione TCP è gestita dal sistema operativo.
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8);
             BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
             Scanner scanner = new Scanner(System.in)) {
            
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
