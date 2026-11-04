/**
 * Gestore di una connessione della chat: legge righe e chiude il proprio socket.
 * I metodi del registro usano il lock del server; outputLock protegge una riga completa.
 * Al mittente torna [TU], agli altri [ClientN]. /list e /quit sono comandi del protocollo.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ClientHandler implements Runnable {
    private final ChatServer server;
    private final Socket socket;
    private final int id;
    private final String username;
    private final Object outputLock = new Object();
    private final AtomicBoolean closed = new AtomicBoolean();
    private BufferedWriter out;

    public ClientHandler(ChatServer server, Socket socket, int id) {
        this.server = server;
        this.socket = socket;
        this.id = id;
        this.username = "Client" + id;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public boolean isClosed() { return closed.get(); }

    public boolean sendMessage(String message) {
        try {
            synchronized (outputLock) {
                if (closed.get() || out == null) return false;
                out.write(message);
                out.write('\n'); // Framing identico in Java e Node.js.
                out.flush();
            }
            return true;
        } catch (IOException e) {
            closeConnection(); // Sblocca il lettore; il finally rimuoverà l'utente.
            return false;
        }
    }

    public void closeConnection() {
        // Può essere chiamato da un mittente, dal proprio gestore o dallo shutdown.
        // Non acquisisce outputLock: deve sbloccare anche uno scrittore fermo sulla rete.
        if (closed.compareAndSet(false, true)) {
            try { socket.close(); } catch (IOException ignored) { }
        }
    }

    @Override
    public void run() {
        try (Socket owned = socket) {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));
            synchronized (outputLock) {
                out = new BufferedWriter(new OutputStreamWriter(
                        socket.getOutputStream(), StandardCharsets.UTF_8));
            }
            // Il benvenuto è la prima riga; il gestore non è ancora visibile ai broadcast.
            if (!sendMessage("[SERVER]: Benvenuto " + username + "! Client connessi: "
                    + (server.getClientCount() + 1))) return;
            if (!server.registerClient(this)) return;
            System.out.println("[CONNESSIONE] " + username + " da " + socket.getRemoteSocketAddress());
            server.broadcast("[SERVER]: " + username + " si è connesso. Client online: "
                    + server.getClientCount(), this);
            server.printStatistics();

            String message;
            while (server.isRunning() && !closed.get() && (message = in.readLine()) != null) {
                if (message.equals("/quit")) {
                    sendMessage("[SERVER]: Arrivederci " + username);
                    break;
                } else if (message.equals("/list")) {
                    sendUserList();
                } else if (message.startsWith("/")) {
                    sendMessage("[SERVER]: Comando non riconosciuto");
                } else if (!message.isEmpty()) {
                    server.chatMessage(this, message);
                }
            }
        } catch (IOException e) {
            if (server.isRunning() && !closed.get())
                System.err.println("Errore " + username + ": " + e.getMessage());
        } finally {
            closeConnection();
            server.disconnected(this);
        }
    }

    private void sendUserList() {
        List<ClientHandler> snapshot = server.getClientsSnapshot();
        snapshot.sort(Comparator.comparingInt(ClientHandler::getId));
        List<String> names = new ArrayList<>();
        for (ClientHandler client : snapshot) names.add(client.getUsername());
        sendMessage("[SERVER]: Utenti online: " + String.join(", ", names));
    }
}
