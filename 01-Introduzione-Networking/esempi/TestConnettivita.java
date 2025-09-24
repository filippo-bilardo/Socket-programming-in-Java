/**
 * Nome dell'Esempio: Test Connettività
 * Guida di Riferimento: 02-Introduzione-ai-Socket.md
 * 
 * Obiettivo: Verificare se una specifica porta è aperta su un host.
 * 
 * Spiegazione:
 * 1. Tenta di creare una connessione socket verso l'host e porta specificati
 * 2. Se la connessione ha successo, la porta è aperta
 * 3. Gestisce le eccezioni per determinare lo stato della porta
 * 
 * @author Socket Programming Course
 * @version 1.0
 */

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class TestConnettivita {
    
    /**
     * Testa se una porta è aperta su un host specifico
     * 
     * @param host L'indirizzo dell'host da testare
     * @param port La porta da verificare
     * @param timeout Timeout in millisecondi
     * @return true se la porta è aperta, false altrimenti
     */
    public static boolean isPortOpen(String host, int port, int timeout) {
        try {
            // Tenta di creare una connessione socket
            Socket socket = new Socket();
            socket.connect(new java.net.InetSocketAddress(host, port), timeout);
            socket.close();
            return true;
        } catch (SocketTimeoutException e) {
            // Timeout raggiunto - porta probabilmente chiusa o filtrata
            System.out.println("Timeout: " + host + ":" + port + " non risponde");
            return false;
        } catch (UnknownHostException e) {
            // Host non risolto
            System.out.println("Host sconosciuto: " + host);
            return false;
        } catch (IOException e) {
            // Connessione rifiutata o altri errori di I/O
            System.out.println("Connessione fallita: " + e.getMessage());
            return false;
        }
    }
    
    public static void main(String[] args) {
        // Test di connettività su diverse porte comuni
        String host = "localhost";
        int[] portsToTest = {22, 80, 443, 8080, 3306, 5432};
        int timeout = 1000; // 1 secondo
        
        System.out.println("🔍 Test connettività su " + host);
        System.out.println("=" .repeat(40));
        
        for (int port : portsToTest) {
            boolean isOpen = isPortOpen(host, port, timeout);
            String status = isOpen ? "✅ APERTA" : "❌ CHIUSA";
            System.out.printf("Porta %d: %s%n", port, status);
        }
        
        // Test su host remoto
        System.out.println("\n🌐 Test su host remoto");
        System.out.println("=" .repeat(40));
        
        String remoteHost = "google.com";
        int[] remotePorts = {80, 443};
        
        for (int port : remotePorts) {
            boolean isOpen = isPortOpen(remoteHost, port, timeout);
            String status = isOpen ? "✅ APERTA" : "❌ CHIUSA";
            System.out.printf("%s:%d %s%n", remoteHost, port, status);
        }
    }
}