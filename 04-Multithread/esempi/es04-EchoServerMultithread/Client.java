/**
 * Laboratorio: es04-EchoServerMultithread, Client.java. JDK 11+.
 * Porta 9876; protocollo: byte fino a EOF; nessun broadcast.
 * Il client usa shutdownOutput e continua a leggere; una read non è un messaggio.
 * Compilare questa cartella separatamente dalle altre varianti.
 *
 * Esempio di client TCP in Java.
 * Si connette a un server sulla porta 9876 e invia un messaggio.
 * 
 * javac -encoding UTF-8 Client.java && java Client
 * 
 * @version 1.0 - 18/10/25
 * @author Filippo Bilardo 
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        String srvAddr = "localhost"; // Indirizzo IP o nome del server
        int srvPort = 9876; // Porta su cui il server ascolta

        try (Socket socket = new Socket(srvAddr, srvPort)) {
            socket.setSoTimeout(5000);
            System.out.println("Connesso al server " + srvAddr + ":" + srvPort);

            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();

            // Invia dati al server
            String message = "Ciao, server!";
            output.write(message.getBytes(StandardCharsets.UTF_8));
            socket.shutdownOutput(); // EOF della richiesta; si può ancora leggere la risposta.

            // Ricevi dati dal server
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream received = new ByteArrayOutputStream();
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) received.write(buffer, 0, bytesRead);
            String response = received.toString(StandardCharsets.UTF_8);
            System.out.println("Risposta dal server: " + response);

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
