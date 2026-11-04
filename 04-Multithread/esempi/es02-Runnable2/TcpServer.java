/**
 * Laboratorio: es02-Runnable2, TcpServer.java. JDK 11+.
 * Porta 8080; protocollo: righe UTF-8; una risposta Echo per richiesta, EOF chiude.
 * La stessa classe contiene main e run, ma ogni connessione ha una nuova istanza.
 * Compilare questa cartella separatamente dalle altre varianti.
 *
 * Server echo con gestore Runnable nella stessa classe. Porta 8080.
 * javac -encoding UTF-8 TcpServer.java && java TcpServer
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TcpServer implements Runnable {
    private final Socket clientSocket;
    public TcpServer(Socket socket) { this.clientSocket = socket; }
    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(8080)) {
            System.out.println("Server TCP avviato sulla porta 8080");
            while (true) {
                // Un accept per connessione: ogni Runnable riceve un socket diverso.
                Socket clSocket = server.accept();
                new Thread(new TcpServer(clSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Errore server: " + e.getMessage());
        }
    }
    @Override public void run() {
        try (Socket socket = clientSocket;
             BufferedReader in = new BufferedReader(new InputStreamReader(
                 socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {
            socket.setSoTimeout(30_000); // Ogni client ha un timeout indipendente.
            String line;
            while ((line = in.readLine()) != null) out.println("Echo: " + line);
        } catch (IOException e) {
            System.err.println("Errore client: " + e.getMessage());
        }
    }
}
