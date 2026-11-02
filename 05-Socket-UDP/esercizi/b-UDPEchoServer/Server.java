/**
 * 05-Socket-UDP/esercizi/b-UDPEchoServer/Server.java
 *
 * Obiettivo: Riferimento dell’esercizio b: rispondere a più client con un echo UDP esatto.
 * Protocollo: Datagrammi di massimo 1024 byte; porta predefinita 9876.
 * Parametri: porta del servizio (facoltativa).
 *
 * Compilazione dalla cartella del file: javac -encoding UTF-8 *.java
 * Avvio:
 *   java Server 9876
 *
 * bye riceve l’echo come ogni altro messaggio: il comando di uscita appartiene solo al client.
 */

import java.net.*;
/** Riferimento esercizio echo: ogni datagramma, anche bye, riceve l'echo. */
public class Server {
    // Legge i parametri della console e avvia il ruolo previsto da questa classe.
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9876;
        // Il bind occupa la porta del servizio; il socket può ricevere richieste da mittenti diversi.
        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] data = new byte[1025];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            System.out.println("PRONTO " + port);
            while (true) {
                // Ripristina la capacità prima della nuova ricezione: il datagramma precedente potrebbe essere più corto.
                packet.setLength(data.length);
                socket.receive(packet);
                // L’echo conserva i byte originali e risponde al mittente; i messaggi oltre limite vengono scartati.
                if (packet.getLength() <= 1024)
                    socket.send(new DatagramPacket(packet.getData(), packet.getOffset(), packet.getLength(), packet.getSocketAddress()));
            }
        }
    }
}
