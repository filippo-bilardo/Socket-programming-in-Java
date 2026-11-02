/**
 * 05-Socket-UDP/esempi/01.01-unicast-java/UdpReceiver.java
 *
 * Obiettivo: Ricevere e visualizzare un datagramma UDP, poi terminare.
 * Protocollo: Testo UTF-8, massimo 1024 byte; porta predefinita 9876.
 * Parametri: porta di ricezione (facoltativa).
 *
 * Compilazione dalla cartella del file: javac -encoding UTF-8 *.java
 * Avvio:
 *   java UdpReceiver 9876
 *
 * Il ricevitore è monouso: riavvialo per il messaggio successivo. Un payload vuoto è valido.
 */

import java.net.*;
import java.nio.charset.StandardCharsets;
/** Riceve un datagramma, poi termina. java UdpReceiver [porta] */
public class UdpReceiver {
    // Legge i parametri della console e avvia il ruolo previsto da questa classe.
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9876;
        // Il bind occupa la porta del servizio; il socket può ricevere richieste da mittenti diversi.
        try (DatagramSocket socket = new DatagramSocket(port)) {
            // Il byte aggiuntivo permette di riconoscere un payload che supera il limite applicativo di 1024 byte.
            byte[] data = new byte[1025]; // Un byte sentinella per rifiutare oltre 1024 byte.
            DatagramPacket packet = new DatagramPacket(data, data.length);
            System.out.println("PRONTO " + socket.getLocalPort());
            // Attesa bloccante di un datagramma; il pacchetto riceve anche indirizzo e porta del mittente.
            socket.receive(packet);
            if (packet.getLength() > 1024) throw new IllegalArgumentException("Datagramma troppo grande");
            // Decodifica solo i byte effettivamente ricevuti, senza leggere la parte inutilizzata del buffer.
            System.out.println("RICEVUTO " + new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8));
        }
    }
}
