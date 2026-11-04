/**
 * Chat TCP a righe UTF-8, porta 9999. JDK 11+, nessuna libreria esterna.
 * Il listener assegna Client1, Client2, ... e avvia un Runnable per socket.
 * Il registro è protetto da stateLock; gli invii avvengono su una copia fuori dal lock.
 * Avvio: javac -encoding UTF-8 *.java; java ChatServer [porta]
 * Console locale: STATS stampa le metriche; SHUTDOWN chiude listener e connessioni.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ChatServer {
    private final ServerSocket listener;
    private final Object stateLock = new Object();
    private final Set<ClientHandler> clients = new LinkedHashSet<>();
    // Include anche gestori che non hanno ancora inviato il benvenuto.
    private final Set<ClientHandler> connections = new HashSet<>();
    private volatile boolean running = true;
    private int nextClientId; // Scritto soltanto dal thread di accept.
    private long messages; // Messaggi chat accettati; esclusi comandi e notifiche.

    public ChatServer(int port) throws IOException {
        listener = new ServerSocket(port);
    }

    public boolean isRunning() {
        return running;
    }

    public int getClientCount() {
        synchronized (stateLock) {
            return clients.size();
        }
    }

    public List<ClientHandler> getClientsSnapshot() {
        synchronized (stateLock) {
            return new ArrayList<>(clients);
        }
    }

    public boolean registerClient(ClientHandler client) {
        synchronized (stateLock) {
            if (!running || client.isClosed()) return false;
            return clients.add(client); // Pubblicato solo dopo stream e benvenuto.
        }
    }

    public void broadcast(String message, ClientHandler sender) {
        // Non mantenere stateLock durante le scritture: un destinatario può essere lento.
        for (ClientHandler client : getClientsSnapshot()) {
            if (client != sender) client.sendMessage(message);
        }
    }

    public void chatMessage(ClientHandler sender, String text) {
        synchronized (stateLock) {
            if (!running) return;
            messages++;
        }
        sender.sendMessage("[TU]: " + text);
        broadcast("[" + sender.getUsername() + "]: " + text, sender);
        System.out.println("[" + sender.getUsername() + "] " + text);
        printStatistics();
    }

    public void disconnected(ClientHandler client) {
        boolean wasRegistered;
        int count;
        synchronized (stateLock) {
            connections.remove(client);
            wasRegistered = clients.remove(client);
            count = clients.size();
        }
        // La rimozione riuscita identifica l'unico cleanup che deve notificare l'uscita.
        if (wasRegistered && running) {
            System.out.println("[DISCONNESSIONE] " + client.getUsername());
            broadcast("[SERVER]: " + client.getUsername()
                    + " si è disconnesso. Client online: " + count, client);
            printStatistics();
        }
    }

    public void printStatistics() {
        String snapshot;
        synchronized (stateLock) {
            snapshot = "[STATISTICHE] Client attivi: " + clients.size()
                    + " | Messaggi chat: " + messages;
        }
        System.out.println(snapshot); // Console fuori dal monitor del registro.
    }

    public void runServer() throws IOException {
        System.out.println("Server in ascolto sulla porta " + listener.getLocalPort());
        System.out.println("Console: STATS oppure SHUTDOWN");
        try {
            while (running) {
                Socket socket = listener.accept();
                synchronized (stateLock) {
                    if (!running) {
                        socket.close();
                        break;
                    }
                    ClientHandler handler = new ClientHandler(this, socket, ++nextClientId);
                    connections.add(handler);
                    new Thread(handler, "ClientHandler-" + handler.getUsername()).start();
                }
            }
        } catch (SocketException e) {
            if (running) throw e; // Chiudere il listener durante shutdown sblocca accept().
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        List<ClientHandler> accepted;
        synchronized (stateLock) {
            if (!running) return;
            running = false;
            accepted = new ArrayList<>(connections);
            clients.clear();
            connections.clear();
        }
        try { listener.close(); } catch (IOException ignored) { }
        // Nessun invio sotto lock: chiudere il socket sblocca anche gestori in read/write.
        for (ClientHandler client : accepted) client.closeConnection();
        System.out.println("[SERVER] Arresto completato");
        printStatistics();
    }

    private void startConsole() {
        Thread console = new Thread(() -> {
            BufferedReader keyboard = new BufferedReader(
                    new InputStreamReader(System.in, StandardCharsets.UTF_8));
            try {
                String command;
                while (running && (command = keyboard.readLine()) != null) {
                    if (command.trim().equalsIgnoreCase("SHUTDOWN")) {
                        shutdown();
                        return;
                    }
                    if (command.trim().equalsIgnoreCase("STATS")) printStatistics();
                    else System.out.println("Console: STATS oppure SHUTDOWN");
                }
            } catch (IOException e) {
                if (running) System.err.println("Errore console: " + e.getMessage());
            }
        }, "Console-server");
        // La console può restare in readLine; non deve impedire l'uscita dopo Ctrl+C.
        console.setDaemon(true);
        console.start();
    }

    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Uso: java ChatServer [porta]");
            System.exit(1);
        }
        try {
            int port = args.length == 0 ? 9999 : Integer.parseInt(args[0]);
            if (port < 1 || port > 65535) throw new IllegalArgumentException("Porta non valida");
            ChatServer server = new ChatServer(port);
            Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown, "Arresto-server"));
            server.startConsole();
            server.runServer();
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Errore server: " + e.getMessage());
            System.exit(1);
        }
    }
}
