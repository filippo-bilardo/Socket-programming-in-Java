/**
 * Esempio di server TCP che utilizza lambda expressions per gestire i client.
 *
 * Questo server accetta connessioni da client e crea un nuovo thread
 * per ogni client utilizzando una lambda expression che implementa Runnable.
 * Ogni client può inviare messaggi che verranno rispediti indietro dal server (echo).
 *
 * javac TcpServer.java && java TcpServer
 *
 * versione: 1.0 - 18/10/25
 * autore: Filippo Bilardo
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {

    private static final int PORT = 8765;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server TCP avviato sulla porta " + PORT);

            while (true) {
                // Accept() esegue 3-way handshake automaticamente
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connesso: " + clientSocket.getRemoteSocketAddress());

                new Thread(() -> {
                    try (InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream());
                         BufferedReader in = new BufferedReader(isr);
                         PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                         Socket ignored = clientSocket // Aggiunto per chiudere il socket automaticamente
                        ) 
                    {
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            System.out.println("Ricevuto: " + inputLine);
                            out.println("Echo: " + inputLine);
                        }
                    } catch (IOException e) {
                        System.err.println("Errore gestione client: " + e.getMessage());
                    }
                    // Nessun finally necessario: le risorse vengono chiuse automaticamente
                }).start();
            }
        } catch (IOException e) {
            System.err.println("Errore server: " + e.getMessage());
        }
    }
}
