/**
 * 06-Socket-Multicast-Broadcast/esempi/02.01-multicast-base/Sender.java
 *
 * Obiettivo: Inviare un singolo datagramma a un gruppo multicast IPv4.
 * Protocollo: Testo UTF-8, massimo 1024 byte; default 239.255.0.1:9876, TTL 1.
 * Parametri: messaggio, porta, gruppo (facoltativi).
 *
 * Compilazione dalla cartella del file: javac -encoding UTF-8 *.java
 * Avvio:
 *   java Sender "Caffè e multicast" 9876 239.255.0.1
 *
 * Avvia prima i ricevitori; imposta MULTICAST_IFACE se necessario. Per inviare non occorre il join.
 */

import java.net.*;
import java.nio.charset.StandardCharsets;
/** Un datagramma al gruppo. java Sender [messaggio] [porta] [gruppo] */
public class Sender {
    // Legge i parametri della console e avvia il ruolo previsto da questa classe.
    public static void main(String[] args) throws Exception {
        String message = args.length > 0 ? args[0] : "Messaggio da Sender: è UDP";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 9876;
        InetAddress group = MulticastSupport.group(args.length > 2 ? args[2] : "239.255.0.1");
        // La codifica produce i byte del datagramma: la loro quantità può superare il numero di caratteri.
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        // Valida i parametri prima di aprire il socket o iniziare il ciclo di invio.
        if (port < 1 || port > 65535 || data.length > 1024) throw new IllegalArgumentException("Porta o lunghezza non valida");
        // Seleziona la scheda locale di uscita; il gruppo multicast è invece la destinazione del messaggio.
        NetworkInterface ni = MulticastSupport.networkInterface();
        try (MulticastSocket socket = new MulticastSocket()) {
            // La scheda di uscita deve corrispondere a quella usata per la prova, soprattutto con VPN o più adattatori.
            socket.setNetworkInterface(ni); socket.setTimeToLive(1);
            socket.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
            // Spedisce un singolo datagramma: non viene creata una connessione TCP e non è prevista una conferma.
            socket.send(new DatagramPacket(data, data.length, group, port));
            System.out.println("INVIATO " + data.length + " byte su " + ni.getName());
        }
    }
}
