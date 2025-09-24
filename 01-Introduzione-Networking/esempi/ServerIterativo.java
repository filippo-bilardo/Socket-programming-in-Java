/**
 * Nome dell'Esempio: Server Iterativo
 * Guida di Riferimento: 03-Architetture-Client-Server.md
 * 
 * Obiettivo: Implementare un server echo iterativo che gestisce un client alla volta.
 * 
 * Spiegazione:
 * 1. Crea un ServerSocket in ascolto su una porta
 * 2. Accetta una connessione client alla volta
 * 3. Legge il messaggio e lo invia indietro (echo)
 * 4. Chiude la connessione e passa al client successivo
 * 
 * @author Socket Programming Course
 * @version 1.0
 */

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerIterativo {
    private static final int PORT = 8080;
    private static final String EXIT_COMMAND = "quit";
    
    /**
     * Gestisce un singolo client
     * 
     * @param clientSocket Socket del client connesso
     * @param clientNumber Numero progressivo del client
     */
    private static void handleClient(Socket clientSocket, int clientNumber) {
        String clientAddress = clientSocket.getRemoteSocketAddress().toString();
        System.out.println("📞 Client #" + clientNumber + " connesso da: " + clientAddress);
        
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            // Invia messaggio di benvenuto
            out.println("🎉 Benvenuto nel Server Echo! (Client #" + clientNumber + ")");
            out.println("💡 Digita 'quit' per disconnetterti");
            
            String inputLine;
            int messageCount = 0;
            
            // Loop di comunicazione con il client
            while ((inputLine = in.readLine()) != null) {
                messageCount++;
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                
                System.out.println("[" + timestamp + "] Client #" + clientNumber + " → " + inputLine);
                
                // Controlla comando di uscita
                if (EXIT_COMMAND.equalsIgnoreCase(inputLine.trim())) {
                    out.println("👋 Arrivederci! Hai inviato " + (messageCount - 1) + " messaggi.");
                    break;
                }
                
                // Echo del messaggio con informazioni aggiuntive
                String response = String.format("📡 Echo #%d [%s]: %s", 
                                               messageCount, timestamp, inputLine);
                out.println(response);
            }
            
        } catch (IOException e) {
            System.err.println("❌ Errore nella gestione del client #" + clientNumber + ": " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("📞 Client #" + clientNumber + " disconnesso");
            } catch (IOException e) {
                System.err.println("❌ Errore nella chiusura del socket: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("🚀 Avvio Server Echo Iterativo");
        System.out.println("👂 In ascolto sulla porta " + PORT);
        System.out.println("⚠️  Gestione sequenziale: un client alla volta");
        System.out.println("🛑 Premi Ctrl+C per fermare il server");
        System.out.println("=" .repeat(50));
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            int clientCounter = 0;
            
            // Loop principale del server
            while (true) {
                try {
                    System.out.println("⏳ In attesa di connessioni...");
                    
                    // Accetta una connessione (operazione bloccante)
                    Socket clientSocket = serverSocket.accept();
                    clientCounter++;
                    
                    // Gestisce il client (bloccante - gestisce solo questo client)
                    handleClient(clientSocket, clientCounter);
                    
                } catch (IOException e) {
                    System.err.println("❌ Errore nell'accettare connessione: " + e.getMessage());
                    // Continua ad accettare altre connessioni
                }
            }
            
        } catch (IOException e) {
            System.err.println("💥 Impossibile avviare il server: " + e.getMessage());
            System.err.println("💡 Assicurati che la porta " + PORT + " sia disponibile");
        }
    }
}