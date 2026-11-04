/**
 * Laboratorio: es05-ServerWeb, TcpServer.java. JDK 11+.
 * Porta 8765; protocollo: HTTP con header ASCII e corpo UTF-8.
 * Content-Length conta byte; la riga vuota conclude gli header.
 * Compilare questa cartella separatamente dalle altre varianti.
 */
package network;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
public class TcpServer {
    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(8765)) {
            System.out.println("Server HTTP in ascolto sulla porta 8765");
            while (true) {
                Socket socket = server.accept();
                new Thread(() -> handle(socket)).start();
            }
        }
    }
    private static void handle(Socket socket) {
        try (Socket owned = socket) {
            socket.setSoTimeout(5000);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
            String line;
            // Gli header terminano con una riga vuota, non con EOF della connessione.
            while ((line = in.readLine()) != null && !line.isEmpty()) System.out.println(line);
            if (line == null) return;
            // Content-Length conta i byte dopo la codifica, non i caratteri della String.
            byte[] body = "Saluti dal web server Java".getBytes(StandardCharsets.UTF_8);
            OutputStream out = socket.getOutputStream();
            out.write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Length: " + body.length + "\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
            out.write(body); out.flush();
        } catch (IOException e) { System.err.println(e.getMessage()); }
    }
}
