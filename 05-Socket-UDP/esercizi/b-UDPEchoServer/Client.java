/**
 * 05-Socket-UDP/esercizi/b-UDPEchoServer/Client.java
 *
 * Obiettivo: Riferimento dell’esercizio b: inviare righe e verificare le risposte del peer scelto.
 * Protocollo: Testo UTF-8, massimo 1024 byte; deadline di 2 secondi per risposta.
 * Parametri: host e porta (default: localhost e 9876).
 *
 * Compilazione dalla cartella del file: javac -encoding UTF-8 *.java
 * Avvio:
 *   java Client localhost 9876
 *
 * bye conclude il client dopo l’echo; EOF chiude senza inviare. Un timeout lascia l’esito della richiesta non noto.
 */

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
/** bye termina solo questo client; il server resta disponibile agli altri. */
public class Client {
    // Legge i parametri della console e avvia il ruolo previsto da questa classe.
    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 9876;
        // Una porta locale effimera è sufficiente per inviare; try-with-resources chiude il socket anche su errore.
        try (DatagramSocket socket = new DatagramSocket()) {
            // Conserva la destinazione esplicita per inviare anche payload vuoti e riconoscere le risposte attese.
            InetSocketAddress peer = new InetSocketAddress(InetAddress.getByName(host), port);
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = console.readLine()) != null) {
                byte[] data = line.getBytes(StandardCharsets.UTF_8);
                if (data.length > 1024) { System.err.println("Max 1024 byte"); continue; }
                // Una riga della console diventa un datagramma; anche la riga vuota è un messaggio valido.
                socket.send(new DatagramPacket(data, data.length, peer));
                byte[] buffer = new byte[1025];
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                // Misura un limite complessivo con un orologio monotono; non ripartire da due secondi per ogni pacchetto estraneo.
                long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(2);
                try {
                    do {
                        long left = deadline - System.nanoTime();
                        if (left <= 0) throw new SocketTimeoutException();
                        socket.setSoTimeout((int)Math.max(1, java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(left)));
                        reply.setLength(buffer.length); socket.receive(reply);
                    // La verifica del peer comprende sia l’indirizzo IP sia la porta UDP.
                    } while (!reply.getSocketAddress().equals(peer));
                } catch (SocketTimeoutException e) { System.err.println("TIMEOUT: esito non noto"); break; }
                if (reply.getLength() > 1024) throw new IOException("Risposta troppo grande");
                System.out.println("ECHO " + new String(reply.getData(), 0, reply.getLength(), StandardCharsets.UTF_8));
                // bye chiude il client soltanto dopo la risposta; il server resta disponibile per gli altri utenti.
                if (line.equals("bye")) break;
            }
        }
    }
}
