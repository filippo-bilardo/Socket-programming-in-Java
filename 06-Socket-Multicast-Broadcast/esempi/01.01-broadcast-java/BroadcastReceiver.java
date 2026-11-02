/**
 * 06-Socket-Multicast-Broadcast/esempi/01.01-broadcast-java/BroadcastReceiver.java
 *
 * Obiettivo: Ricevere messaggi UDP, inclusi quelli broadcast diretti alla porta di laboratorio.
 * Protocollo: Payload UTF-8 di massimo 1024 byte; porta predefinita 5000.
 * Parametri: porta e conteggio (facoltativi; conteggio 0 = continuo).
 *
 * Compilazione dalla cartella del file: javac -encoding UTF-8 *.java
 * Avvio:
 *   java BroadcastReceiver 5000 3
 *
 * Avvia un ricevitore per host/porta; per più ricevitori locali usa gli esempi multicast.
 */

import java.net.*;
import java.nio.charset.StandardCharsets;
/** Un ricevitore per host/porta; java BroadcastReceiver [porta] [numero-messaggi; 0=illimitato] */
public class BroadcastReceiver {
    // Legge i parametri della console e avvia il ruolo previsto da questa classe.
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 5000;
        int limit = args.length > 1 ? Integer.parseInt(args[1]) : 0;
        // Valida i parametri prima di aprire il socket o iniziare il ciclo di invio.
        if (port < 1 || port > 65535 || limit < 0) throw new IllegalArgumentException("Parametri non validi");
        // Il bind occupa la porta del servizio; il socket può ricevere richieste da mittenti diversi.
        try (DatagramSocket socket = new DatagramSocket(port)) {
            if (limit > 0) socket.setSoTimeout(5000);
            System.out.println("PRONTO " + port);
            // Il byte aggiuntivo permette di riconoscere un payload che supera il limite applicativo di 1024 byte.
            byte[] data = new byte[1025];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            for (int i = 0; limit == 0 || i < limit; i++) {
                // Ripristina la capacità prima della nuova ricezione: il datagramma precedente potrebbe essere più corto.
                // Attesa bloccante di un datagramma; il pacchetto riceve anche indirizzo e porta del mittente.
                packet.setLength(data.length); socket.receive(packet);
                if (packet.getLength() > 1024) { i--; continue; }
                // Decodifica solo i byte effettivamente ricevuti, senza leggere la parte inutilizzata del buffer.
                System.out.println("RICEVUTO " + new String(data, 0, packet.getLength(), StandardCharsets.UTF_8));
            }
        }
    }
}
