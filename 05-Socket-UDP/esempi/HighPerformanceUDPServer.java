/**
 * 05-Socket-UDP/esempi/HighPerformanceUDPServer.java
 *
 * Obiettivo: Studiare un event loop UDP non bloccante e un benchmark con risposte correlate.
 * Protocollo: Comandi UTF-8 PING/ECHO/TIME/STATS/LOAD/QUIT, massimo 1400 byte.
 * Parametri: server <porta> oppure loadtest <host> <porta> <worker> <pps-totali> <secondi>.
 *
 * Compilazione dalla cartella del file: javac -encoding UTF-8 *.java
 * Avvio:
 *   java HighPerformanceUDPServer server 8888
 *   java HighPerformanceUDPServer loadtest localhost 8888 2 20 3
 *
 * I pps indicano il rate totale desiderato: il benchmark attende le risposte e misura RTT, non sola latenza di rete.
 */

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/** Server UDP con Selector e buffer diretto riusato; benchmark request/response.
 * Il nome dell'esempio non implica un throughput garantito. JDK 11+.
 */
public class HighPerformanceUDPServer {
    private static final int MAX_MESSAGE = 1400;
    public static class Server {
        private final int port;
        // Stato condiviso con stop: il loop di rete deve osservare subito una richiesta di arresto.
        private volatile boolean running = true;
        private volatile DatagramChannel channel;
        private volatile Selector selector;
        private long received, sent, discarded;
        public Server(int port) { this.port = port; }
        public void start() throws IOException {
            // Il canale gestisce datagrammi IPv4; il Selector permette attese senza polling continuo.
            try (DatagramChannel owned = DatagramChannel.open(StandardProtocolFamily.INET);
                 Selector ready = Selector.open()) {
                channel = owned; selector = ready;
                if (!running) return;
                // Il buffer del kernel è distinto dal ByteBuffer applicativo; la dimensione richiesta può essere limitata dall’OS.
                owned.setOption(StandardSocketOptions.SO_RCVBUF, 1024 * 1024);
                owned.bind(new InetSocketAddress(port));
                // receive può restituire null: significa nessun pacchetto disponibile, non datagramma vuoto.
                owned.configureBlocking(false);
                owned.register(ready, SelectionKey.OP_READ);
                // Il buffer diretto viene riusato dal canale; non richiede, e può non offrire, un array Java sottostante.
                ByteBuffer input = ByteBuffer.allocateDirect(MAX_MESSAGE + 1);
                System.out.println("PRONTO " + ((InetSocketAddress) owned.getLocalAddress()).getPort());
                long lastStats = System.nanoTime();
                while (running) {
                    // Attende dati o la scadenza periodica; wakeup permette a stop di interrompere questa attesa.
                    ready.select(1000);
                    ready.selectedKeys().clear();
                    // Batch limitato: il loop torna periodicamente a controllare running.
                    for (int i = 0; running && i < 50; i++) {
                        // Prepara tutta la capacità per la nuova ricezione; flip selezionerà poi soltanto i byte arrivati.
                        input.clear();
                        SocketAddress sender = owned.receive(input);
                        if (sender == null) break;
                        received++;
                        if (input.position() > MAX_MESSAGE) { discarded++; continue; }
                        // Passa dalla modalità scrittura alla lettura: limit diventa il numero di byte ricevuti.
                        input.flip();
                        String request = StandardCharsets.UTF_8.decode(input).toString();
                        String response = reply(request);
                        ByteBuffer output = StandardCharsets.UTF_8.encode(response);
                        int length = output.remaining();
                        if (length > MAX_MESSAGE) { discarded++; continue; }
                        // Un canale non bloccante può restituire 0: la risposta viene contata come scartata.
                        if (owned.send(output, sender) == length) sent++; else discarded++;
                    }
                    if (System.nanoTime() - lastStats >= TimeUnit.SECONDS.toNanos(5)) {
                        System.out.println(stats()); lastStats = System.nanoTime();
                    }
                }
            } catch (ClosedChannelException e) { if (running) throw e; }
            finally { channel = null; selector = null; }
        }
        private String stats() { return "STATS rx=" + received + " tx=" + sent + " scartati=" + discarded; }
        // Separa il comando dal suo argomento mantenendo quest’ultimo intero, inclusi gli spazi interni.
        private String reply(String request) {
            String[] parts = request.trim().split("\\s+", 2);
            switch (parts[0].toUpperCase(Locale.ROOT)) {
                case "PING": return "PONG";
                case "TIME": return "TIME " + System.currentTimeMillis();
                case "ECHO": return "ECHO: " + (parts.length > 1 ? parts[1] : "");
                case "STATS": return stats();
                case "QUIT": return "BYE"; // Non arresta il server condiviso.
                case "LOAD":
                    try {
                        int n = parts.length > 1 ? Integer.parseInt(parts[1]) : 1000;
                        if (n < 0 || n > 100000) return "ERROR LOAD 0..100000";
                        double value = 0;
                        for (int i = 0; i < n; i++) value += Math.sqrt(i);
                        return "LOAD " + value;
                    } catch (NumberFormatException e) { return "ERROR LOAD non numerico"; }
                default: return "ERROR comando sconosciuto";
            }
        }
        // Sblocca Selector e canale; un flag da solo non interromperebbe necessariamente una chiamata bloccata.
        public void stop() {
            running = false;
            Selector current = selector;
            if (current != null) current.wakeup();
            DatagramChannel currentChannel = channel;
            if (currentChannel != null) try { currentChannel.close(); } catch (IOException ignored) { }
        }
    }
    // Generatore request/response: il rate è un obiettivo massimo, non una promessa sotto perdita o RTT elevato.
    public static class LoadTestClient {
        public static void runLoadTest(String host, int port, int threads, int rate, int seconds) throws Exception {
            if (threads < 1 || rate < threads || seconds < 1)
                throw new IllegalArgumentException("Richiesti thread >= 1, rate totale >= thread, durata >= 1");
            InetAddress target = InetAddress.getByName(host); // Risolve prima di avviare i worker.
            ExecutorService pool = Executors.newFixedThreadPool(threads);
            // I worker aggiornano contatori condivisi: gli incrementi devono essere atomici.
            AtomicLong sent = new AtomicLong(), received = new AtomicLong(), timedOut = new AtomicLong();
            AtomicLong totalRtt = new AtomicLong();
            List<Future<?>> workers = new ArrayList<>();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds);
            try {
                for (int i = 0; i < threads; i++) {
                    final int worker = i;
                    // Distribuisce anche il resto della divisione, così la somma dei rate coincide con quello totale.
                    final int ownRate = rate / threads + (i < rate % threads ? 1 : 0);
                    workers.add(pool.submit(() -> {
                        // Una porta locale effimera è sufficiente per inviare; try-with-resources chiude il socket anche su errore.
                        try (DatagramSocket socket = new DatagramSocket()) {
                            // Ogni worker usa un socket proprio e un unico server; le richieste ECHO includono un identificativo.
                            socket.connect(target, port);
                            socket.setSoTimeout(250);
                            long sequence = 0, next = System.nanoTime();
                            long interval = Math.max(1, TimeUnit.SECONDS.toNanos(1) / ownRate);
                            while (!Thread.currentThread().isInterrupted() && System.nanoTime() < deadline) {
                                long wait = next - System.nanoTime();
                                if (wait > 0) LockSupport.parkNanos(wait);
                                if (Thread.currentThread().isInterrupted() || System.nanoTime() >= deadline) break;
                                // Worker e sequenza distinguono le richieste: una risposta tardiva non diventa un successo per la successiva.
                                String id = worker + ":" + sequence++;
                                byte[] data = ("ECHO " + id).getBytes(StandardCharsets.UTF_8);
                                long start = System.nanoTime();
                                socket.send(new DatagramPacket(data, data.length)); sent.incrementAndGet();
                                byte[] buffer = new byte[MAX_MESSAGE + 1];
                                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                                try {
                                    socket.receive(packet);
                                    String response = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                                    // Misura RTT soltanto per il contenuto correlato, non per un datagramma qualsiasi arrivato sulla porta.
                                    if (response.equals("ECHO: " + id)) {
                                        received.incrementAndGet(); totalRtt.addAndGet(System.nanoTime() - start);
                                    } else timedOut.incrementAndGet();
                                } catch (SocketTimeoutException | PortUnreachableException e) { timedOut.incrementAndGet(); }
                                // Non recupera il ritardo con una raffica di pacchetti.
                                next = Math.max(next + interval, System.nanoTime());
                            }
                        } catch (IOException e) { throw new UncheckedIOException(e); }
                    }));
                }
                // Attende risultati ed eventuali errori dei worker, con un limite anche nell’attesa dei task.
                for (Future<?> worker : workers) worker.get(seconds + 3L, TimeUnit.SECONDS);
            } finally {
                // Chiede l’interruzione; le receive dei worker hanno un timeout breve per poter concludere e chiudere i socket.
                pool.shutdownNow();
                if (!pool.awaitTermination(2, TimeUnit.SECONDS)) throw new IOException("Worker non terminati");
            }
            // La media usa soltanto le risposte valide: timeout e risposte estranee restano conteggi separati.
            long ok = received.get();
            System.out.printf(Locale.ROOT, "RISULTATO inviati=%d ricevuti=%d senza_risposta_valida=%d rtt_medio_us=%.1f%n",
                sent.get(), ok, timedOut.get(), ok == 0 ? 0.0 : totalRtt.get() / (ok * 1000.0));
        }
    }
    // Legge i parametri della console e avvia il ruolo previsto da questa classe.
    public static void main(String[] args) throws Exception {
        if (args.length == 2 && args[0].equals("server")) {
            Server server = new Server(Integer.parseInt(args[1]));
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop)); server.start();
        } else if (args.length == 6 && args[0].equals("loadtest")) {
            LoadTestClient.runLoadTest(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
        } else System.out.println("java HighPerformanceUDPServer server <porta> | loadtest <host> <porta> <thread> <pps-totali> <secondi>");
    }
}
