// Client per inviare file  

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class FileClient {
    public static void sendFile(Socket socket, String fileName) {
        try (FileInputStream fis = new FileInputStream(fileName);
             OutputStream out = socket.getOutputStream()) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            
            out.flush();
            System.out.println("File inviato: " + fileName);
        } catch (IOException e) {
            System.err.println("Errore invio file: " + e.getMessage());
        }
    }
}