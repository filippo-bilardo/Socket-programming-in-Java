/**
 * 05-Socket-UDP/esercizi/c-DaytimeServerUDP/DaytimeServer.java
 *
 * Obiettivo: Rispondere alle richieste UDP con data e ora UTC, senza stato per client.
 * Protocollo: Risposta ASCII ISO-8601 con CRLF; porta di laboratorio 1313 (standard Daytime: 13).
 * Parametri: porta del servizio (facoltativa).
 *
 * Compilazione dalla cartella del file: javac -encoding UTF-8 *.java
 * Avvio:
 *   java DaytimeServer 1313
 *
 * Anche un datagramma di richiesta vuoto è valido. Il servizio mostra l’ora e non sincronizza gli orologi.
 */

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
/** Daytime di laboratorio, porta 1313 anziché la porta standard 13. */
public class DaytimeServer {
    // Legge i parametri della console e avvia il ruolo previsto da questa classe.
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 1313;
        // Il bind occupa la porta del servizio; il socket può ricevere richieste da mittenti diversi.
        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("PRONTO " + port);
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                // Il contenuto della richiesta non serve: anche un datagramma di zero byte richiede una risposta.
                socket.receive(request); // È valida anche una richiesta con payload vuoto.
                // Instant usa UTC; CRLF termina la riga del formato Daytime scelto per il laboratorio.
                byte[] data = (Instant.now().toString() + "\r\n").getBytes(StandardCharsets.US_ASCII);
                // Risponde alla porta sorgente del singolo client, non alla porta del servizio sul client.
                socket.send(new DatagramPacket(data, data.length, request.getSocketAddress()));
            }
        }
    }
}
