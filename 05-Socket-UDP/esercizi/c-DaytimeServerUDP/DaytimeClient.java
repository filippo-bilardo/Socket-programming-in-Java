/**
 * 05-Socket-UDP/esercizi/c-DaytimeServerUDP/DaytimeClient.java
 *
 * Obiettivo: Richiedere e validare l’ora UTC del servizio Daytime di laboratorio.
 * Protocollo: Richiesta vuota e risposta ASCII ISO-8601 con CRLF; timeout di 2 secondi.
 * Parametri: host e porta (default: localhost e 1313).
 *
 * Compilazione dalla cartella del file: javac -encoding UTF-8 *.java
 * Avvio:
 *   java DaytimeClient localhost 1313
 *
 * Il peer è esplicito anche con payload vuoto; risposte di altri endpoint non vengono accettate.
 */

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
public class DaytimeClient {
    // Legge i parametri della console e avvia il ruolo previsto da questa classe.
    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 1313;
        // Una porta locale effimera è sufficiente per inviare; try-with-resources chiude il socket anche su errore.
        try (DatagramSocket socket = new DatagramSocket()) {
            InetSocketAddress peer = new InetSocketAddress(InetAddress.getByName(host), port);
            // Invia davvero un datagramma vuoto specificando la destinazione; non è un segnale di EOF.
            socket.send(new DatagramPacket(new byte[0], 0, peer));
            byte[] buffer = new byte[128];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            // L’attesa totale resta di due secondi anche se arrivano datagrammi da endpoint diversi.
            long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(2);
            do {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) throw new SocketTimeoutException();
                socket.setSoTimeout((int)Math.max(1, java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(remaining)));
                reply.setLength(buffer.length); socket.receive(reply);
            // Accetta l’ora soltanto dal peer richiesto; i datagrammi estranei non completano la richiesta.
            } while (!reply.getSocketAddress().equals(peer));
            String text = new String(reply.getData(), 0, reply.getLength(), StandardCharsets.US_ASCII).trim();
            // Controlla il formato concordato prima di presentare l’ora: non viene modificato l’orologio locale.
            Instant.parse(text); // Verifica il formato effettivamente adottato dal laboratorio.
            System.out.println(text);
        }
    }
}
