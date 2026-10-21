/**
 * Esempio di server TCP multithread in Java.
 * Ogni connessione client viene gestita in un thread separato.
 * Il server ascolta sulla porta 9876.
 * 
 * javac Server.java && java Server
 * 
 * @version 1.0 - 18/10/25
 * @author Filippo Bilardo 
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        int srvPort = 9876; // Porta su cui il server ascolta le connessioni

        try (ServerSocket srvSocket = new ServerSocket(srvPort)) {
            
            System.out.println("Server in ascolto sulla porta " + srvPort);

            while (true) {
                Socket clntSocket = srvSocket.accept();
                System.out.println("Nuova connessione da: " + clntSocket.getInetAddress());

                // Crea un nuovo thread per gestire la connessione
                Thread clientHandler = new ClientHandler(clntSocket);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler extends Thread {
    private final Socket clntSocket;

    public ClientHandler(Socket clntSocket) {
        this.clntSocket = clntSocket;
    }

    @Override
    public void run() {
        try {
            try (clntSocket) {
                InputStream clntInStream = clntSocket.getInputStream();
                OutputStream clntOutStream = clntSocket.getOutputStream();
                
                // Gestisci la comunicazione con il client
                // Esempio: leggi dati dal client e inviali indietro
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = clntInStream.read(buffer)) != -1) {
                    clntOutStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
