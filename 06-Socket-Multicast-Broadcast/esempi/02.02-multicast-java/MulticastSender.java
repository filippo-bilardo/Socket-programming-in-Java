/**
 * 06-Socket-Multicast-Broadcast/esempi/02.02-multicast-java/MulticastSender.java
 *
 * Obiettivo: Inviare periodicamente messaggi multicast con parametri configurabili.
 * Protocollo: MULTICAST|numero messaggio|timestamp|TTL in UTF-8; default 239.255.0.1:5000.
 * Parametri: gruppo, porta, intervallo-ms, TTL, conteggio (facoltativi; conteggio 0 = continuo).
 *
 * Compilazione dalla cartella del file: javac -encoding UTF-8 *.java
 * Avvio:
 *   java MulticastSender 239.255.0.1 5000 100 1 3
 *
 * TTL limita gli hop, non una regione geografica. Sender e receiver devono usare lo stesso gruppo e la stessa porta.
 */

import java.net.*;
import java.nio.charset.StandardCharsets;
/** java MulticastSender [gruppo] [porta] [intervallo-ms] [ttl] [conteggio; 0=illimitato] */
public class MulticastSender {
    // Legge i parametri della console e avvia il ruolo previsto da questa classe.
    public static void main(String[] args) throws Exception {
        InetAddress group = MulticastSupport.group(args.length > 0 ? args[0] : "239.255.0.1");
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5000;
        int interval = args.length > 2 ? Integer.parseInt(args[2]) : 2000;
        int ttl = args.length > 3 ? Integer.parseInt(args[3]) : 1;
        int limit = args.length > 4 ? Integer.parseInt(args[4]) : 0;
        // Valida i parametri prima di aprire il socket o iniziare il ciclo di invio.
        if (port < 1 || port > 65535 || interval < 1 || ttl < 0 || ttl > 255 || limit < 0)
            throw new IllegalArgumentException("Porta 1..65535, intervallo positivo, TTL 0..255, conteggio >= 0");
        try (MulticastSocket socket = new MulticastSocket()) {
            // La scheda di uscita deve corrispondere a quella usata per la prova, soprattutto con VPN o più adattatori.
            socket.setNetworkInterface(MulticastSupport.networkInterface());
            socket.setTimeToLive(ttl); socket.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
            for (int i = 1; limit == 0 || i <= limit; i++) {
                String text = "MULTICAST|Messaggio #" + i + "|Timestamp: " + System.currentTimeMillis() + "|TTL: " + ttl;
                byte[] data = text.getBytes(StandardCharsets.UTF_8);
                socket.send(new DatagramPacket(data, data.length, group, port));
                System.out.println("INVIATO " + text);
                // La pausa limita il traffico; l’ultimo invio di una prova finita non richiede un’ulteriore attesa.
                if (limit == 0 || i < limit) Thread.sleep(interval);
            }
        }
    }
}
