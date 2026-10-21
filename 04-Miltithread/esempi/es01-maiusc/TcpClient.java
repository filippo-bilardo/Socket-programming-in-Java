/**
 * Esempio di un semplice client TCP che si connette a un server,
 * invia un messaggio, e riceve una risposta.
 * 
 * Compilo ed eseguo il client:
 * javac TcpClient.java && java TcpClient
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8765)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("Ciao dal client!");
            String response = in.readLine();
            System.out.println("Risposta dal server: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}