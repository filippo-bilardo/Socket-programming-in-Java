/**
 * 05-Socket-UDP/esercizi/a-UDPSenderReceiver/Sender.java
 *
 * Obiettivo: Inviare un solo messaggio a un ricevitore UDP unicast.
 * Protocollo: Testo UTF-8, massimo 1024 byte; destinazione predefinita localhost:8698.
 * Parametri: host, porta, messaggio (tutti facoltativi).
 *
 * Compilazione dalla cartella del file: javac -encoding UTF-8 *.java
 * Avvio:
 *   java Sender localhost 8698 "Caffè e UDP"
 *
 * Avvia prima il ricevitore. La send conferma l’invio locale, non la consegna al destinatario.
 */

import java.net.*;
import java.nio.charset.StandardCharsets;
/** Un solo datagramma UTF-8. java Sender [host] [porta] [messaggio] */
public class Sender {
    // Legge i parametri della console e avvia il ruolo previsto da questa classe.
    public static void main(String[] args) throws Exception {
        // localhost serve per la prova sullo stesso PC; per due host indica quello che esegue il ricevitore.
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8698;
        byte[] data = (args.length > 2 ? args[2] : "Ciao, UDP!").getBytes(StandardCharsets.UTF_8);
        // Valida i parametri prima di aprire il socket o iniziare il ciclo di invio.
        if (port < 1 || port > 65535 || data.length > 1024) throw new IllegalArgumentException("Porta o messaggio non valido (max 1024 byte)");
        // Una porta locale effimera è sufficiente per inviare; try-with-resources chiude il socket anche su errore.
        try (DatagramSocket socket = new DatagramSocket()) {
            // Spedisce un singolo datagramma: non viene creata una connessione TCP e non è prevista una conferma.
            socket.send(new DatagramPacket(data, data.length, InetAddress.getByName(host), port));
            System.out.println("INVIATO " + data.length + " byte");
        }
    }
}
