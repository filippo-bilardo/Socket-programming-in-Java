/**
 * Nome dell'Esempio: Client Base
 * Guida di Riferimento: 03-Architetture-Client-Server.md
 * 
 * Obiettivo: Client generico per testare i server echo iterativo e concorrente.
 * 
 * Spiegazione:
 * 1. Si connette a un server specificato
 * 2. Permette l'invio interattivo di messaggi
 * 3. Mostra le risposte del server
 * 4. Gestisce la disconnessione graceful
 * 
 * @author Socket Programming Course
 * @version 1.0
 */

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientBase {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;
    private static final String EXIT_COMMAND = "quit";
    
    /**
     * Avvia una sessione interattiva con il server
     * 
     * @param host Indirizzo del server
     * @param port Porta del server
     */
    public static void startSession(String host, int port) {
        System.out.println("🔗 Connessione al server " + host + ":" + port + "...");
        
        try (
            Socket socket = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("✅ Connessione stabilita!");
            System.out.println("📋 Informazioni connessione:");
            System.out.println("   Local: " + socket.getLocalSocketAddress());
            System.out.println("   Remote: " + socket.getRemoteSocketAddress());
            System.out.println();
            
            // Legge i messaggi di benvenuto dal server
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                System.out.println("📨 Server: " + serverMessage);
                
                // Se il messaggio contiene "Digita", inizia la sessione interattiva
                if (serverMessage.contains("Digita") || serverMessage.contains("quit")) {
                    break;
                }
            }
            
            System.out.println("\n💬 Sessione interattiva avviata (digita 'quit' per uscire)");
            System.out.println("💡 Suggerimenti:");
            System.out.println("   - Prova 'slow messaggio' per testare elaborazione lenta");
            System.out.println("   - Apri più client per testare la concorrenza");
            System.out.println("-" .repeat(50));
            
            // Thread per ricevere messaggi dal server
            Thread receiveThread = new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println("📨 Server: " + response);
                        
                        // Se il server dice arrivederci, termina
                        if (response.contains("Arrivederci")) {
                            System.out.println("\n👋 Sessione terminata dal server");
                            break;
                        }
                    }
                } catch (IOException e) {
                    if (!socket.isClosed()) {
                        System.err.println("❌ Errore nella ricezione: " + e.getMessage());
                    }
                }
            });
            
            receiveThread.start();
            
            // Loop principale per l'invio di messaggi
            System.out.print("Tu: ");
            while (scanner.hasNextLine()) {
                String message = scanner.nextLine();
                
                if (message.isEmpty()) {
                    System.out.print("Tu: ");
                    continue;
                }
                
                // Invia il messaggio al server
                out.println(message);
                
                // Se è il comando di uscita, termina
                if (EXIT_COMMAND.equalsIgnoreCase(message.trim())) {
                    break;
                }
                
                // Attende un momento per permettere al server di rispondere
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                
                System.out.print("Tu: ");
            }
            
            receiveThread.interrupt();
            
        } catch (ConnectException e) {
            System.err.println("❌ Impossibile connettersi al server " + host + ":" + port);
            System.err.println("💡 Assicurati che il server sia avviato e raggiungibile");
        } catch (UnknownHostException e) {
            System.err.println("❌ Host sconosciuto: " + host);
        } catch (IOException e) {
            System.err.println("❌ Errore di comunicazione: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        System.out.println("🚀 Client Echo - Test Server");
        System.out.println("=" .repeat(30));
        
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        
        // Parsing argomenti da riga di comando
        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("❌ Porta non valida: " + args[1]);
                System.err.println("💡 Uso: java ClientBase [host] [porta]");
                return;
            }
        }
        
        // Mostra le opzioni disponibili
        System.out.println("🎯 Server disponibili per il test:");
        System.out.println("   • Server Iterativo: localhost:8080");
        System.out.println("   • Server Concorrente: localhost:8081");
        System.out.println();
        System.out.println("📝 Parametri attuali:");
        System.out.println("   Host: " + host);
        System.out.println("   Porta: " + port);
        System.out.println();
        
        // Avvia la sessione
        startSession(host, port);
    }
}