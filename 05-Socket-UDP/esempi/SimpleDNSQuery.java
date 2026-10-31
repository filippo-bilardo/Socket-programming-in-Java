import java.net.*;
import java.nio.ByteBuffer;

/**
 * Esempio SEMPLICE di query DNS usando socket UDP
 * Risolve un nome di dominio (es. google.com) in indirizzo IP
 * 
 * DNS usa porta 53 e formato binario per le query
 */
public class SimpleDNSQuery {
    
    // Server DNS pubblico di Google
    private static final String DNS_SERVER = "8.8.8.8";
    private static final int DNS_PORT = 53;
    
    public static void main(String[] args) {
        try {
            // Dominio da risolvere
            String domain = "google.com";
            
            System.out.println("=== DNS Query Example ===");
            System.out.println("Dominio da risolvere: " + domain);
            System.out.println("DNS Server: " + DNS_SERVER);
            System.out.println();
            
            // 1. Crea il socket UDP
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(5000); // Timeout 5 secondi
            
            // 2. Costruisci la query DNS
            byte[] queryData = buildDNSQuery(domain);
            
            // 3. Invia la query al server DNS
            InetAddress dnsAddress = InetAddress.getByName(DNS_SERVER);
            DatagramPacket queryPacket = new DatagramPacket(
                queryData, 
                queryData.length, 
                dnsAddress, 
                DNS_PORT
            );
            
            System.out.println("Invio query DNS...");
            socket.send(queryPacket);
            
            // 4. Ricevi la risposta
            byte[] responseData = new byte[512]; // DNS usa max 512 byte per UDP
            DatagramPacket responsePacket = new DatagramPacket(
                responseData, 
                responseData.length
            );
            
            System.out.println("Attendo risposta...");
            socket.receive(responsePacket);
            
            // 5. Parsa la risposta per estrarre l'IP
            String ip = parseDNSResponse(responseData);
            
            System.out.println("\n=== Risultato ===");
            System.out.println(domain + " -> " + ip);
            
            // 6. Chiudi il socket
            socket.close();
            
        } catch (SocketTimeoutException e) {
            System.err.println("Timeout: nessuna risposta dal server DNS");
        } catch (Exception e) {
            System.err.println("Errore: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Costruisce un pacchetto DNS query in formato binario
     * 
     * Struttura semplificata:
     * - Header (12 byte): ID, flags, contatori
     * - Question: nome dominio + tipo query
     * 
     * @param domain il dominio da risolvere (es. "google.com")
     * @return array di byte con la query DNS
     */
    private static byte[] buildDNSQuery(String domain) {
        ByteBuffer buffer = ByteBuffer.allocate(512);
        
        // === HEADER (12 byte) ===
        
        // Transaction ID (2 byte) - numero casuale per identificare la richiesta
        buffer.putShort((short) 0x1234);
        
        // Flags (2 byte) - 0x0100 = query standard con ricorsione
        buffer.putShort((short) 0x0100);
        
        // QDCOUNT (2 byte) - numero di domande (1)
        buffer.putShort((short) 1);
        
        // ANCOUNT (2 byte) - numero di risposte (0 nella query)
        buffer.putShort((short) 0);
        
        // NSCOUNT (2 byte) - numero di authority records (0)
        buffer.putShort((short) 0);
        
        // ARCOUNT (2 byte) - numero di additional records (0)
        buffer.putShort((short) 0);
        
        // === QUESTION SECTION ===
        
        // Nome dominio codificato (es. "google.com" -> 6google3com0)
        // Formato: [lunghezza]label[lunghezza]label...[0]
        String[] labels = domain.split("\\.");
        for (String label : labels) {
            buffer.put((byte) label.length());  // Lunghezza del label
            buffer.put(label.getBytes());        // Bytes del label
        }
        buffer.put((byte) 0); // Terminatore (fine nome)
        
        // QTYPE (2 byte) - tipo di query: 0x0001 = A (indirizzo IPv4)
        buffer.putShort((short) 1);
        
        // QCLASS (2 byte) - classe: 0x0001 = IN (Internet)
        buffer.putShort((short) 1);
        
        // Ritorna solo i byte utilizzati
        byte[] query = new byte[buffer.position()];
        buffer.rewind();
        buffer.get(query);
        
        return query;
    }
    
    /**
     * Parsa la risposta DNS per estrarre l'indirizzo IP
     * 
     * Legge header + question + risposta per trovare il primo IP
     * 
     * @param response array di byte con la risposta DNS
     * @return stringa con l'indirizzo IP (es. "142.250.180.46")
     */
    private static String parseDNSResponse(byte[] response) {
        ByteBuffer buffer = ByteBuffer.wrap(response);
        
        // === SALTA HEADER (12 byte) ===
        buffer.position(12);
        
        // === SALTA QUESTION SECTION ===
        // Leggi il nome (salta fino allo 0)
        while (buffer.get() != 0) {
            // Continua fino al terminatore
        }
        // Salta QTYPE (2 byte) e QCLASS (2 byte)
        buffer.getShort();
        buffer.getShort();
        
        // === LEGGI ANSWER SECTION ===
        
        // Salta il nome (compressed pointer: 2 byte che iniziano con 11)
        buffer.getShort();
        
        // TYPE (2 byte) - dovrebbe essere 1 (A record)
        short type = buffer.getShort();
        
        // CLASS (2 byte) - dovrebbe essere 1 (IN)
        buffer.getShort();
        
        // TTL (4 byte) - time to live (ignoriamo)
        buffer.getInt();
        
        // RDLENGTH (2 byte) - lunghezza dei dati (4 per IPv4)
        short dataLength = buffer.getShort();
        
        // RDATA - i dati veri (4 byte = indirizzo IP)
        if (type == 1 && dataLength == 4) {
            // Leggi i 4 byte dell'IP
            int ip1 = buffer.get() & 0xFF;  // & 0xFF per convertire byte signed in unsigned
            int ip2 = buffer.get() & 0xFF;
            int ip3 = buffer.get() & 0xFF;
            int ip4 = buffer.get() & 0xFF;
            
            return ip1 + "." + ip2 + "." + ip3 + "." + ip4;
        }
        
        return "Indirizzo non trovato";
    }
}
