/**
 * 06-Socket-Multicast-Broadcast/esempi/02.01-multicast-base/Receiver.java
 *
 * Obiettivo: Ricevere datagrammi aderendo al gruppo sulla corretta interfaccia locale.
 * Protocollo: Testo UTF-8, massimo 1024 byte; default 239.255.0.1:9876.
 * Parametri: porta, gruppo, conteggio (facoltativi; conteggio 0 = continuo).
 *
 * Compilazione dalla cartella del file: javac -encoding UTF-8 *.java
 * Avvio:
 *   java Receiver 9876 239.255.0.1 3
 *
 * bye termina questo ricevitore. Un conteggio finito attiva il timeout; UDP può perdere messaggi.
 */

import java.net.*;
import java.nio.charset.StandardCharsets;
/** java Receiver [porta] [gruppo] [numero-messaggi; 0=illimitato] */
public class Receiver {
    // Legge i parametri della console e avvia il ruolo previsto da questa classe.
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9876;
        InetAddress group = MulticastSupport.group(args.length > 1 ? args[1] : "239.255.0.1");
        int limit = args.length > 2 ? Integer.parseInt(args[2]) : 0;
        // Valida i parametri prima di aprire il socket o iniziare il ciclo di invio.
        if (port < 1 || port > 65535 || limit < 0) throw new IllegalArgumentException("Parametri non validi");
        NetworkInterface ni = MulticastSupport.networkInterface();
        // L’utility prepara riuso della porta, bind e join del gruppo sull’interfaccia scelta.
        try (MulticastSocket socket = MulticastSupport.receiver(group, port, ni)) {
            // Una prova con conteggio finito deve terminare anche se UDP perde uno dei messaggi attesi.
            if (limit > 0) socket.setSoTimeout(5000);
            System.out.println("PRONTO " + group.getHostAddress() + ":" + port + " interfaccia=" + ni.getName());
            // Il byte aggiuntivo permette di riconoscere un payload che supera il limite applicativo di 1024 byte.
            byte[] data = new byte[1025];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                for (int count = 0; limit == 0 || count < limit; count++) {
                    // Ripristina la capacità prima della nuova ricezione: il datagramma precedente potrebbe essere più corto.
                    packet.setLength(data.length);
                    // Attesa bloccante di un datagramma; il pacchetto riceve anche indirizzo e porta del mittente.
                    socket.receive(packet);
                    if (packet.getLength() > 1024) { System.err.println("SCARTATO oltre 1024 byte"); count--; continue; }
                    String message = new String(data, 0, packet.getLength(), StandardCharsets.UTF_8);
                    // Decodifica solo i byte effettivamente ricevuti, senza leggere la parte inutilizzata del buffer.
                    System.out.println("RICEVUTO " + message);
                    if (message.equals("bye")) break;
                }
            // Il leave usa la stessa coppia gruppo/interfaccia del join; la chiusura del socket segue automaticamente.
            } finally { socket.leaveGroup(new InetSocketAddress(group, port), ni); }
        }
    }
}
