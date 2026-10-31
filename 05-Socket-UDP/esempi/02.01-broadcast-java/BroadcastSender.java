import java.net.*;
import java.io.*;

/**
 * Esempio 02.01 - Broadcast UDP Sender (Java)
 * 
 * Questo esempio dimostra come inviare pacchetti broadcast UDP.
 * Il broadcast raggiunge tutti i dispositivi nella rete locale.
 * 
 * Caratteristiche:
 * - Invio di messaggi broadcast periodici
 * - Utilizzo dell'indirizzo di broadcast 255.255.255.255
 * - Configurazione setBroadcast(true) necessaria
 * 
 * Compilazione: javac BroadcastSender.java
 * Esecuzione: java BroadcastSender [porta] [intervallo_ms]
 * Esempio: java BroadcastSender 5000 2000
 */
public class BroadcastSender {
    private static final String BROADCAST_ADDRESS = "255.255.255.255";
    private static final int DEFAULT_PORT = 5000;
    private static final int DEFAULT_INTERVAL = 2000; // 2 secondi
    
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        int interval = DEFAULT_INTERVAL;
        
        // Parsing argomenti da linea di comando
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("❌ Porta non valida. Uso porta di default: " + DEFAULT_PORT);
            }
        }
        
        if (args.length > 1) {
            try {
                interval = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("❌ Intervallo non valido. Uso intervallo di default: " + DEFAULT_INTERVAL);
            }
        }
        
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("📡 BROADCAST UDP SENDER");
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("🔌 Porta: " + port);
        System.out.println("📡 Indirizzo Broadcast: " + BROADCAST_ADDRESS);
        System.out.println("⏱️  Intervallo: " + interval + "ms");
        System.out.println("═══════════════════════════════════════════════════\n");
        
        try (DatagramSocket socket = new DatagramSocket()) {
            // IMPORTANTE: Abilita il broadcast
            socket.setBroadcast(true);
            
            InetAddress broadcastAddr = InetAddress.getByName(BROADCAST_ADDRESS);
            int messageCount = 0;
            
            System.out.println("✅ Socket creato e configurato per broadcast");
            System.out.println("🚀 Inizio invio messaggi broadcast...\n");
            System.out.println("Premi Ctrl+C per terminare\n");
            
            while (true) {
                messageCount++;
                
                // Crea il messaggio
                String message = String.format(
                    "BROADCAST|Messaggio #%d|Timestamp: %d",
                    messageCount,
                    System.currentTimeMillis()
                );
                
                byte[] buffer = message.getBytes();
                
                // Crea e invia il pacchetto
                DatagramPacket packet = new DatagramPacket(
                    buffer,
                    buffer.length,
                    broadcastAddr,
                    port
                );
                
                socket.send(packet);
                
                System.out.printf("📤 [%s] Inviato messaggio #%d (%d bytes)%n",
                    getCurrentTime(),
                    messageCount,
                    buffer.length
                );
                
                // Attendi prima del prossimo invio
                Thread.sleep(interval);
            }
            
        } catch (SocketException e) {
            System.err.println("❌ Errore creazione socket: " + e.getMessage());
        } catch (UnknownHostException e) {
            System.err.println("❌ Indirizzo broadcast non valido: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("❌ Errore invio pacchetto: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("\n⚠️  Invio interrotto");
        }
        
        System.out.println("\n👋 Sender terminato");
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
