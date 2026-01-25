import java.net.*;
import java.io.*;

/**
 * Esempio 02.03 - Multicast UDP Sender (Java)
 * 
 * Questo esempio dimostra come inviare pacchetti multicast UDP.
 * Il multicast permette di inviare dati a un gruppo specifico di destinatari
 * che si sono uniti al gruppo multicast.
 * 
 * Caratteristiche:
 * - Invio di messaggi multicast periodici
 * - Utilizzo di indirizzi multicast (224.0.0.0 - 239.255.255.255)
 * - Configurazione TTL (Time To Live)
 * - MulticastSocket per la gestione del multicast
 * 
 * Compilazione: javac MulticastSender.java
 * Esecuzione: java MulticastSender [indirizzo_multicast] [porta] [intervallo_ms] [ttl]
 * Esempio: java MulticastSender 239.255.0.1 5000 2000 1
 */
public class MulticastSender {
    private static final String DEFAULT_MULTICAST_ADDRESS = "239.255.0.1";
    private static final int DEFAULT_PORT = 5000;
    private static final int DEFAULT_INTERVAL = 2000; // 2 secondi
    private static final int DEFAULT_TTL = 1; // Solo rete locale
    
    public static void main(String[] args) {
        String multicastAddress = DEFAULT_MULTICAST_ADDRESS;
        int port = DEFAULT_PORT;
        int interval = DEFAULT_INTERVAL;
        int ttl = DEFAULT_TTL;
        
        // Parsing argomenti da linea di comando
        if (args.length > 0) {
            multicastAddress = args[0];
        }
        
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("❌ Porta non valida. Uso porta di default: " + DEFAULT_PORT);
            }
        }
        
        if (args.length > 2) {
            try {
                interval = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.err.println("❌ Intervallo non valido. Uso intervallo di default: " + DEFAULT_INTERVAL);
            }
        }
        
        if (args.length > 3) {
            try {
                ttl = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                System.err.println("❌ TTL non valido. Uso TTL di default: " + DEFAULT_TTL);
            }
        }
        
        // Validazione indirizzo multicast
        if (!isValidMulticastAddress(multicastAddress)) {
            System.err.println("❌ Indirizzo multicast non valido: " + multicastAddress);
            System.err.println("   Deve essere nel range 224.0.0.0 - 239.255.255.255");
            System.exit(1);
        }
        
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("📡 MULTICAST UDP SENDER");
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("🔌 Porta: " + port);
        System.out.println("📡 Indirizzo Multicast: " + multicastAddress);
        System.out.println("⏱️  Intervallo: " + interval + "ms");
        System.out.println("🌐 TTL: " + ttl + " (" + getTTLDescription(ttl) + ")");
        System.out.println("═══════════════════════════════════════════════════\n");
        
        try {
            // Crea MulticastSocket
            MulticastSocket socket = new MulticastSocket();
            
            // Configura TTL
            socket.setTimeToLive(ttl);
            
            InetAddress group = InetAddress.getByName(multicastAddress);
            int messageCount = 0;
            
            System.out.println("✅ MulticastSocket creato");
            System.out.println("🚀 Inizio invio messaggi multicast...\n");
            System.out.println("Premi Ctrl+C per terminare\n");
            
            while (true) {
                messageCount++;
                
                // Crea il messaggio
                String message = String.format(
                    "MULTICAST|Messaggio #%d|Timestamp: %d|TTL: %d",
                    messageCount,
                    System.currentTimeMillis(),
                    ttl
                );
                
                byte[] buffer = message.getBytes();
                
                // Crea e invia il pacchetto multicast
                DatagramPacket packet = new DatagramPacket(
                    buffer,
                    buffer.length,
                    group,
                    port
                );
                
                socket.send(packet);
                
                System.out.printf("📤 [%s] Inviato messaggio #%d a %s:%d (%d bytes)%n",
                    getCurrentTime(),
                    messageCount,
                    multicastAddress,
                    port,
                    buffer.length
                );
                
                // Attendi prima del prossimo invio
                Thread.sleep(interval);
            }
            
        } catch (SocketException e) {
            System.err.println("❌ Errore creazione socket: " + e.getMessage());
        } catch (UnknownHostException e) {
            System.err.println("❌ Indirizzo multicast non valido: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("❌ Errore invio pacchetto: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("\n⚠️  Invio interrotto");
        }
        
        System.out.println("\n👋 Sender terminato");
    }
    
    /**
     * Valida se l'indirizzo è un indirizzo multicast valido
     */
    private static boolean isValidMulticastAddress(String address) {
        try {
            InetAddress addr = InetAddress.getByName(address);
            return addr.isMulticastAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }
    
    /**
     * Restituisce una descrizione del TTL
     */
    private static String getTTLDescription(int ttl) {
        if (ttl == 0) return "Solo questo host";
        if (ttl == 1) return "Rete locale";
        if (ttl <= 32) return "Stesso sito/organizzazione";
        if (ttl <= 64) return "Stessa regione";
        if (ttl <= 128) return "Stesso continente";
        return "Globale";
    }
    
    /**
     * Restituisce l'ora corrente formattata
     */
    private static String getCurrentTime() {
        java.time.LocalTime now = java.time.LocalTime.now();
        return String.format("%02d:%02d:%02d",
            now.getHour(),
            now.getMinute(),
            now.getSecond()
        );
    }
}
