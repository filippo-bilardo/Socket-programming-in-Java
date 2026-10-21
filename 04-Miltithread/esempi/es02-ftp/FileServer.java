// Server per ricevere file

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class FileServer {
    public static void receiveFile(Socket socket, String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName);
             InputStream in = socket.getInputStream()) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            
            System.out.println("File ricevuto: " + fileName);
        } catch (IOException e) {
            System.err.println("Errore ricezione file: " + e.getMessage());
        }
    }
}
