// Simulatore DNS usando UDP

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SimpleDNSResolver {
    private static final String DNS_SERVER = "8.8.8.8"; // Google DNS
    private static final int DNS_PORT = 53;
    
    public static String resolveDomain(String domain) {
        try (DatagramSocket socket = new DatagramSocket()) {
            
            // Crea query DNS semplificata (normalmente molto più complessa)
            String query = "QUERY:" + domain;
            byte[] queryData = query.getBytes();
            
            InetAddress dnsServer = InetAddress.getByName(DNS_SERVER);
            DatagramPacket queryPacket = new DatagramPacket(
                queryData, queryData.length, dnsServer, DNS_PORT);
            
            // Invia query
            socket.send(queryPacket);
            
            // Ricevi risposta con timeout
            byte[] responseBuffer = new byte[512];
            DatagramPacket responsePacket = new DatagramPacket(
                responseBuffer, responseBuffer.length);
            
            socket.setSoTimeout(5000); // 5 secondi timeout
            socket.receive(responsePacket);
            
            String response = new String(responsePacket.getData(), 
                0, responsePacket.getLength());
            
            return response;
            
        } catch (IOException e) {
            return "Errore DNS: " + e.getMessage();
        }
    }
    
    public static void main(String[] args) {
        String[] domains = {"google.com", "github.com", "stackoverflow.com"};
        
        for (String domain : domains) {
            System.out.println("Risoluzione " + domain + "...");
            String result = resolveDomain(domain);
            System.out.println("Risultato: " + result);
            System.out.println();
        }
    }
}