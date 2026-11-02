/**
 * 06-Socket-Multicast-Broadcast/esempi/NetworkDiscoveryBroadcast.java
 *
 * Obiettivo: Cercare i responder dell’applicazione mediante richiesta broadcast e risposta unicast.
 * Protocollo: DISCOVER|UUID e DEVICE|UUID|nome|OS; campi testuali codificati, porta predefinita 8891.
 * Parametri: respond [porta] oppure scan [broadcast-IPv4|CIDR] [porta].
 *
 * Compilazione dalla cartella del file: javac -encoding UTF-8 *.java
 * Avvio:
 *   java NetworkDiscoveryBroadcast respond
 *   java NetworkDiscoveryBroadcast scan 127.0.0.1
 *
 * La scansione dura al massimo circa 2 secondi. Il target locale prova il protocollo senza verificare la LAN.
 */

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Discovery applicativo: trova soltanto i dispositivi che eseguono il responder. */
public class NetworkDiscoveryBroadcast {
    private static final int PORT = 8891;
    // Calcola il broadcast IPv4 dal CIDR reale: il suffisso .255 non è corretto per ogni prefisso.
    public static String broadcastAddress(String cidr) throws IOException {
        String[] parts = cidr.split("/", -1);
        if (parts.length != 2) throw new IOException("Richiesto IPv4/prefisso");
        String[] octets = parts[0].split("\\.", -1);
        if (octets.length != 4) throw new IOException("IPv4 non valido");
        int ip = 0;
        try {
            for (String octet : octets) {
                int n = Integer.parseInt(octet);
                if (n < 0 || n > 255) throw new NumberFormatException();
                // Accoda ciascun ottetto all’intero di 32 bit, spostando a sinistra quelli già letti.
                ip = (ip << 8) | n;
            }
            int prefix = Integer.parseInt(parts[1]);
            if (prefix < 0 || prefix > 30) throw new NumberFormatException();
            // Il caso /0 evita lo shift di 32 bit in Java; il complemento della maschera individua i bit host.
            int mask = prefix == 0 ? 0 : -1 << (32 - prefix);
            // Imposta a 1 tutti i bit host conservando quelli di rete: questo è l’indirizzo di broadcast.
            int value = ip | ~mask;
            return InetAddress.getByAddress(new byte[]{(byte)(value >>> 24), (byte)(value >>> 16), (byte)(value >>> 8), (byte)value}).getHostAddress();
        } catch (NumberFormatException e) { throw new IOException("IPv4/prefisso non valido (0..30)", e); }
    }
    // Risponde solo al protocollo DISCOVER dell’esempio; non rappresenta un servizio di scansione universale.
    public static class NetworkResponder {
        private final int port;
        private volatile boolean running = true;
        private volatile DatagramSocket socket;
        public NetworkResponder() { this(PORT); }
        public NetworkResponder(int port) { this.port = port; }
        public void start() throws IOException {
            try (DatagramSocket owned = new DatagramSocket(port)) {
                socket = owned;
                if (!running) return;
                System.out.println("PRONTO responder " + port);
                byte[] data = new byte[1401];
                while (running) {
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    try { owned.receive(packet); }
                    catch (SocketException e) { if (!running) break; throw e; }
                    if (packet.getLength() > 1400) continue;
                    String request = new String(data, 0, packet.getLength(), StandardCharsets.UTF_8);
                    // Valida struttura e UUID prima di generare una risposta: dati casuali non sono richieste di discovery.
                    String[] fields = request.split("\\|", -1);
                    if (fields.length != 2 || !fields[0].equals("DISCOVER")) continue;
                    try { UUID.fromString(fields[1]); } catch (IllegalArgumentException e) { continue; }
                    // Codifica i campi testuali per non confondere un carattere del contenuto con il separatore verticale.
                    String name = URLEncoder.encode(InetAddress.getLocalHost().getHostName(), StandardCharsets.UTF_8);
                    String os = URLEncoder.encode(System.getProperty("os.name", "unknown"), StandardCharsets.UTF_8);
                    byte[] reply = ("DEVICE|" + fields[1] + "|" + name + "|" + os).getBytes(StandardCharsets.UTF_8);
                    // La richiesta può essere broadcast, ma la risposta torna in unicast all’endpoint effettivo del richiedente.
                    owned.send(new DatagramPacket(reply, reply.length, packet.getSocketAddress()));
                }
            } finally { socket = null; }
        }
        public void stop() { running = false; DatagramSocket current = socket; if (current != null) current.close(); }
    }
    // Raccoglie più risposte entro una sola finestra temporale e deduplica i responder per IP sorgente.
    public static class NetworkScanner {
        public List<String> scanNetwork(String target, int port) throws IOException {
            String destination = target.contains("/") ? broadcastAddress(target) : target;
            InetAddress address = InetAddress.getByName(destination);
            if (!(address instanceof Inet4Address)) throw new IOException("Richiesto IPv4");
            // Il nonce identifica questa scansione: risposte di una scansione precedente non devono essere accettate.
            String nonce = UUID.randomUUID().toString();
            byte[] query = ("DISCOVER|" + nonce).getBytes(StandardCharsets.UTF_8);
            Map<String, String> found = new TreeMap<>();
            // Una porta locale effimera è sufficiente per inviare; try-with-resources chiude il socket anche su errore.
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);
                // La deadline si calcola una sola volta: traffico estraneo non può prolungare la scansione indefinitamente.
                long start = System.nanoTime(), deadline = start + TimeUnit.SECONDS.toNanos(2);
                socket.send(new DatagramPacket(query, query.length, address, port));
                byte[] data = new byte[1401];
                while (System.nanoTime() < deadline) {
                    // Ogni receive usa soltanto il tempo residuo della finestra complessiva.
                    int remainingMs = (int)Math.max(1, TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime()));
                    socket.setSoTimeout(remainingMs);
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    try { socket.receive(packet); } catch (SocketTimeoutException e) { break; }
                    if (packet.getPort() != port || packet.getLength() > 1400) continue;
                    String[] parts = new String(data, 0, packet.getLength(), StandardCharsets.UTF_8).split("\\|", -1);
                    // Controlla anche il nonce, dopo aver verificato porta sorgente e dimensione del datagramma.
                    if (parts.length != 4 || !parts[0].equals("DEVICE") || !parts[1].equals(nonce)) continue;
                    try {
                        String host = URLDecoder.decode(parts[2], StandardCharsets.UTF_8);
                        String os = URLDecoder.decode(parts[3], StandardCharsets.UTF_8);
                        // Il tempo osservato parte dall’invio, non dall’ultima receive: include rete e lavoro del responder.
                        long rtt = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                        String ip = packet.getAddress().getHostAddress();
                        // Un duplicato aggiorna la voce dello stesso IP, senza aumentare il numero dei dispositivi trovati.
                        found.put(ip, ip + " nome=" + host + " os=" + os + " risposta_ms=" + rtt);
                    } catch (IllegalArgumentException ignored) { }
                }
            }
            List<String> result = new ArrayList<>(found.values());
            for (String device : result) System.out.println("TROVATO " + device);
            System.out.println("TOTALE " + result.size());
            return result;
        }
    }
    // Legge i parametri della console e avvia il ruolo previsto da questa classe.
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("respond")) {
            NetworkResponder responder = new NetworkResponder(args.length > 1 ? Integer.parseInt(args[1]) : PORT);
            Runtime.getRuntime().addShutdownHook(new Thread(responder::stop)); responder.start();
        } else if (args.length > 0 && args[0].equals("scan")) {
            new NetworkScanner().scanNetwork(args.length > 1 ? args[1] : "255.255.255.255", args.length > 2 ? Integer.parseInt(args[2]) : PORT);
        } else System.out.println("java NetworkDiscoveryBroadcast respond [porta] | scan [broadcast-IPv4|CIDR] [porta]");
    }
}
