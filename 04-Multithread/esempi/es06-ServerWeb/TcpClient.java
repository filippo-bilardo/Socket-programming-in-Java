/**
 * Laboratorio: es05-ServerWeb, TcpClient.java. JDK 11+.
 * Porta 8765; protocollo: HTTP con header ASCII e corpo UTF-8.
 * Content-Length conta byte; la riga vuota conclude gli header.
 * Compilare questa cartella separatamente dalle altre varianti.
 */
package network;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
public class TcpClient {
    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("localhost", 8765)) {
            socket.setSoTimeout(5000);
            OutputStream out = socket.getOutputStream();
            // CRLF separa le righe HTTP; la doppia CRLF completa gli header.
            out.write("GET /ACCENDI HTTP/1.1\r\nHost: localhost:8765\r\nConnection: close\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
            out.flush();
            // Connection: close permette di leggere la risposta fino a EOF.
            socket.getInputStream().transferTo(System.out);
        }
    }
}
