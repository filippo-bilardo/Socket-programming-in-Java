/**
 * 06-Socket-Multicast-Broadcast/esempi/01.01-broadcast-java/BroadcastSender.java
 *
 * Obiettivo: Inviare datagrammi broadcast sulla rete IPv4 selezionata.
 * Protocollo: BROADCAST|numero messaggio|timestamp in UTF-8; default 255.255.255.255:5000.
 * Parametri: porta, intervallo-ms, indirizzo, conteggio (facoltativi; conteggio 0 = continuo).
 *
 * Compilazione dalla cartella del file: javac -encoding UTF-8 *.java
 * Avvio:
 *   java BroadcastSender 5000 500 255.255.255.255 3
 *
 * Il broadcast della subnet dipende dal prefisso. Un invio riuscito non garantisce la ricezione da tutti gli host.
 */

import java.net.*;
import java.nio.charset.StandardCharsets;
/** java BroadcastSender [porta] [intervallo-ms] [indirizzo] [conteggio; 0=illimitato] */
public class BroadcastSender {
    // Legge i parametri della console e avvia il ruolo previsto da questa classe.
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 5000;
        int interval = args.length > 1 ? Integer.parseInt(args[1]) : 2000;
        InetAddress target = InetAddress.getByName(args.length > 2 ? args[2] : "255.255.255.255");
        int limit = args.length > 3 ? Integer.parseInt(args[3]) : 0;
        if (!(target instanceof Inet4Address) || port < 1 || port > 65535 || interval < 1 || limit < 0)
            throw new IllegalArgumentException("IPv4, porta 1..65535, intervallo positivo, conteggio >= 0");
        // Una porta locale effimera è sufficiente per inviare; try-with-resources chiude il socket anche su errore.
        try (DatagramSocket socket = new DatagramSocket()) {
            // Abilita l’invio a indirizzi broadcast IPv4; non è necessario aderire a un gruppo.
            socket.setBroadcast(true);
            for (int i = 1; limit == 0 || i <= limit; i++) {
                String message = "BROADCAST|Messaggio #" + i + "|Timestamp: " + System.currentTimeMillis();
                // La codifica produce i byte del datagramma: la loro quantità può superare il numero di caratteri.
                byte[] data = message.getBytes(StandardCharsets.UTF_8);
                socket.send(new DatagramPacket(data, data.length, target, port));
                System.out.println("INVIATO " + message);
                // La pausa limita il traffico; l’ultimo invio di una prova finita non richiede un’ulteriore attesa.
                if (limit == 0 || i < limit) Thread.sleep(interval);
            }
        }
    }
}
