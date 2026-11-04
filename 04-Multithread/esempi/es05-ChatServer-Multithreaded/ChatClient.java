/**
 * Client interattivo per entrambi i server della chat, Java e Node.js.
 * Il main riceve dal socket; un thread daemon legge la console e invia le righe.
 * Così una chiusura remota termina il programma anche con la tastiera ancora in attesa.
 * Avvio: java ChatClient [host] [porta]. Default: localhost, 9999. JDK 11+.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChatClient {
    private static final Pattern WELCOME = Pattern.compile("^\\[SERVER\\]: Benvenuto (Client\\d+)!.*$");
    private final String host;
    private final int port;
    private final AtomicBoolean connected = new AtomicBoolean();

    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 5000);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream(), StandardCharsets.UTF_8));
            socket.setSoTimeout(5000); // Il benvenuto deve arrivare; nessun timeout sulla chat inattiva.
            String welcome = in.readLine();
            Matcher match = WELCOME.matcher(welcome == null ? "" : welcome);
            if (!match.matches()) throw new IOException("Benvenuto assente o non valido");
            socket.setSoTimeout(0);
            System.out.println("Connessione al server riuscita!");
            System.out.println("Sei stato registrato come: " + match.group(1));
            System.out.println(welcome);
            System.out.println("Comandi: /list, /quit. Scrivi un messaggio e premi Invio.");
            connected.set(true);

            Thread sender = new Thread(() -> sendFromConsole(socket, out), "Console-client");
            sender.setDaemon(true);
            sender.start();
            // Unico lettore del socket, anche dopo l'invio di /quit: consuma la risposta finale.
            String message;
            while ((message = in.readLine()) != null) System.out.println(message);
            System.out.println("Connessione chiusa dal server.");
        } finally {
            connected.set(false);
        }
    }

    private void sendFromConsole(Socket socket, BufferedWriter out) {
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        try {
            String message;
            while (connected.get() && (message = keyboard.readLine()) != null) {
                if (!connected.get()) return;
                sendLine(out, message);
                if (message.equals("/quit")) return;
            }
            // EOF della console richiede una disconnessione pulita, come /quit.
            if (connected.get()) sendLine(out, "/quit");
        } catch (IOException e) {
            if (connected.get()) System.err.println("Errore invio: " + e.getMessage());
            try { socket.close(); } catch (IOException ignored) { }
        }
    }

    private static void sendLine(BufferedWriter out, String message) throws IOException {
        out.write(message);
        out.write('\n');
        out.flush();
    }

    public static void main(String[] args) {
        if (args.length > 2) {
            System.err.println("Uso: java ChatClient [host] [porta]");
            System.exit(1);
        }
        try {
            String host = args.length > 0 ? args[0] : "localhost";
            int port = args.length > 1 ? Integer.parseInt(args[1]) : 9999;
            if (port < 1 || port > 65535) throw new IllegalArgumentException("Porta non valida");
            new ChatClient(host, port).start();
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Errore client: " + e.getMessage());
            System.exit(1);
        }
    }
}
