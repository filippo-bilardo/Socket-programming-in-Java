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

public class TcpServer_v2 {
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
