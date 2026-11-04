/**
 * Laboratorio: es03-Lambda, TcpServer.java. JDK 11+.
 * Porta 8765; protocollo: righe UTF-8; una risposta Echo per richiesta, EOF chiude.
 * La lambda implementa Runnable; ogni iterazione cattura un socket distinto.
 * Compilare questa cartella separatamente dalle altre varianti.
 *
 * Esempio di server TCP che utilizza lambda expressions per gestire i client.
 *
 * Questo server accetta connessioni da client e crea un nuovo thread
 * per ogni client utilizzando una lambda expression che implementa Runnable.
 * Ogni client può inviare messaggi che verranno rispediti indietro dal server (echo).
 *
 * javac -encoding UTF-8 TcpServer.java && java TcpServer
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
                // accept() restituisce un socket connesso dalla coda del sistema operativo.
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connesso: " + clientSocket.getRemoteSocketAddress());

                new Thread(() -> {
                    try (Socket ignored = clientSocket;
                         InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8);
                         BufferedReader in = new BufferedReader(isr);
                         PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8)
                        ) 
                    {
                        clientSocket.setSoTimeout(30_000); // Limite per le letture di un client inattivo.
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
