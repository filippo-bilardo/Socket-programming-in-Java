/**
 * Esempio di client TCP in Java.
 * Si connette a un server sulla porta 9876 e invia un messaggio.
 * 
 * javac Client.java && java Client
 * 
 * @version 1.0 - 18/10/25
 * @author Filippo Bilardo 
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        String srvAddr = "localhost"; // Indirizzo IP o nome del server
        int srvPort = 9876; // Porta su cui il server ascolta

        try {
            Socket socket = new Socket(srvAddr, srvPort);
            System.out.println("Connesso al server " + srvAddr + ":" + srvPort);

            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();

            // Invia dati al server
            String message = "Ciao, server!";
            output.write(message.getBytes());

            // Ricevi dati dal server
            byte[] buffer = new byte[1024];
            int bytesRead = input.read(buffer);
            String response = new String(buffer, 0, bytesRead);
            System.out.println("Risposta dal server: " + response);

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
