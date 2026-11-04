/**
 * Laboratorio: es02-Runnable, TcpServer.java. JDK 11+.
 * Porta 8080; protocollo: righe UTF-8; una risposta Echo per richiesta, EOF chiude.
 * Il Runnable descrive il lavoro, start() avvia il thread.
 * Compilare questa cartella separatamente dalle altre varianti.
 *
 *
 * TCPServer.java - Esempio di un server TCP che gestisce più client
 * utilizzando la programmazione basata su thread.
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
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server TCP avviato sulla porta " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connesso: " + clientSocket.getRemoteSocketAddress());

                // Crea un Runnable per gestire il client
                Runnable clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Errore server: " + e.getMessage());
        }
    }

    // Classe interna che implementa Runnable per gestire il client
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
                 PrintWriter out = new PrintWriter(
                    clientSocket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8)) {

                clientSocket.setSoTimeout(30_000); // Limite per le letture di un client inattivo.
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Ricevuto: " + inputLine);
                    out.println("Echo: " + inputLine); // Una risposta per riga UTF-8.
                }
            } catch (IOException e) {
                System.err.println("Errore gestione client: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Errore chiusura: " + e.getMessage());
                }
            }
        }
    }
}
