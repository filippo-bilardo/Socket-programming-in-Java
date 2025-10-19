/**
 * Esempio di un semplice server TCP che riceve un messaggio da un client,
 * lo elabora e restituisce una risposta con il messaggio in maiuscolo.
 *
 * javac TcpServer.java && java TcpServer
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {
    public static void main(String[] args) {
        try (
            ServerSocket serverSocket = new ServerSocket(8765);
            Socket clientSocket = serverSocket.accept(); // Attende la connessione di un client
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            System.out.println("Server in ascolto sulla porta 8765...");
            System.out.println("Connessione stabilita con il client!");
            String message = in.readLine(); // Legge il messaggio dal client
            System.out.println("Messaggio ricevuto: " + message);
            out.println("Risposta dal server: " + message.toUpperCase());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
