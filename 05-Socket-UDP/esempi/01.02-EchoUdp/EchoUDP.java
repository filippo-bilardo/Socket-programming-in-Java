/**
 * 05-Socket-UDP/esempi/01.02-EchoUdp/EchoUDP.java
 *
 * Obiettivo: Confrontare server echo e client request/response sullo stesso protocollo UDP.
 * Protocollo: Un datagramma UTF-8 di massimo 1024 byte; la risposta contiene gli stessi byte.
 * Parametri: server <porta> oppure client <host> <porta> [messaggio].
 *
 * Compilazione dalla cartella del file: javac -encoding UTF-8 *.java
 * Avvio:
 *   java EchoUDP server 9999
 *   java EchoUDP client localhost 9999 "Caffè"
 *
 * Avvia server e client in terminali diversi. Senza messaggio il client legge la console; quit è locale.
 */

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
/** Echo esatto di un datagramma (max 1024 byte). JDK 11+. */
public class EchoUDP {
    public static final int MAX_MESSAGE = 1024;
    // Il server non conserva una sessione per client: ogni datagramma contiene la sorgente a cui rispondere.
    public static class Server {
        private final int port;
        // volatile rende visibile al thread ricevitore la richiesta di arresto inviata da un altro thread.
        private volatile boolean running = true;
        private volatile DatagramSocket socket;
        public Server(int port) { this.port = port; }
        public void start() throws IOException {
            try (DatagramSocket owned = new DatagramSocket(port)) {
                socket = owned;
                if (!running) return;
                System.out.println("PRONTO " + owned.getLocalPort());
                // Un byte sentinella distingue il massimo valido da un datagramma da rifiutare.
                byte[] buffer = new byte[MAX_MESSAGE + 1];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                while (running) {
                    // Ripristina la capacità prima della nuova ricezione: il datagramma precedente potrebbe essere più corto.
                    packet.setLength(buffer.length); // receive modifica la lunghezza del pacchetto.
                    // receive blocca il thread; stop chiude il socket per sbloccarlo senza busy loop.
                    try { owned.receive(packet); }
                    catch (SocketException e) { if (!running) break; throw e; }
                    if (packet.getLength() > MAX_MESSAGE) {
                        System.err.println("SCARTATO datagramma oltre " + MAX_MESSAGE + " byte");
                        continue;
                    }
                    // Riutilizza solo offset e lunghezza validi, preservando esattamente il payload e rispondendo al mittente.
                    owned.send(new DatagramPacket(packet.getData(), packet.getOffset(), packet.getLength(), packet.getSocketAddress()));
                }
            } finally { socket = null; }
        }
        // Imposta prima lo stato e poi chiude il socket, così l’errore di receive viene riconosciuto come arresto previsto.
        public void stop() {
            running = false;
            DatagramSocket current = socket;
            if (current != null) current.close();
        }
    }
    // Il client conserva un solo peer e gestisce una richiesta alla volta; la porta locale è assegnata dal sistema.
    public static class Client implements AutoCloseable {
        private final DatagramSocket socket;
        private final InetSocketAddress peer;
        public Client(String host, int port) throws IOException {
            peer = new InetSocketAddress(InetAddress.getByName(host), port);
            socket = new DatagramSocket();
        }
        // Invia una richiesta e attende la risposta valida entro la deadline; su errore il chiamante decide la chiusura.
        public String sendMessage(String message, int timeoutMs) throws IOException {
            // La codifica produce i byte del datagramma: la loro quantità può superare il numero di caratteri.
            byte[] data = message.getBytes(StandardCharsets.UTF_8);
            if (data.length > MAX_MESSAGE || timeoutMs <= 0) throw new IllegalArgumentException("Max 1024 byte; timeout positivo");
            // La destinazione esplicita consente anche datagrammi vuoti senza usare il percorso write di un socket connesso.
            socket.send(new DatagramPacket(data, data.length, peer));
            // nanoTime misura durate: un cambio dell’ora di sistema non allunga il timeout.
            long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(timeoutMs);
            byte[] buffer = new byte[MAX_MESSAGE + 1];
            while (true) {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) throw new SocketTimeoutException("Timeout risposta");
                socket.setSoTimeout((int)Math.max(1, java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(remaining)));
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                socket.receive(reply);
                // Ignora sorgenti diverse dal server scelto senza riavviare la durata complessiva di attesa.
                if (!reply.getSocketAddress().equals(peer)) continue;
                if (reply.getLength() > MAX_MESSAGE) throw new IOException("Risposta troppo grande");
                return new String(reply.getData(), reply.getOffset(), reply.getLength(), StandardCharsets.UTF_8);
            }
        }
        // quit riguarda solo la console. Dopo timeout non riusare una sessione che non identifica le richieste.
        public void interactive() throws IOException {
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = console.readLine()) != null && !line.equals("quit")) {
                try { System.out.println("ECHO " + sendMessage(line, 2000)); }
                catch (SocketTimeoutException e) { System.err.println("TIMEOUT: esito non noto; termina la sessione senza riusare il peer"); break; }
            }
        }
        @Override public void close() { socket.close(); }
    }
    // Legge i parametri della console e avvia il ruolo previsto da questa classe.
    public static void main(String[] args) throws Exception {
        if (args.length == 2 && args[0].equals("server")) {
            Server server = new Server(Integer.parseInt(args[1]));
            // Ctrl+C richiama stop e libera il socket bloccato nella receive.
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
            server.start();
        } else if (args.length >= 3 && args[0].equals("client")) {
            try (Client client = new Client(args[1], Integer.parseInt(args[2]))) {
                if (args.length > 3) System.out.println("ECHO " + client.sendMessage(args[3], 2000));
                else client.interactive();
            }
        } else System.out.println("java EchoUDP server <porta> | client <host> <porta> [messaggio]");
    }
}
