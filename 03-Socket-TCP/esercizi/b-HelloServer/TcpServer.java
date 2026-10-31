/** Esempio b: server HTTP didattico GET/HEAD, da provare con browser o curl. */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TcpServer {
    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 2223;
        try (ServerSocket server = new ServerSocket(
                port, 50, InetAddress.getByName("127.0.0.1"))) {
            System.out.println("Server HTTP in ascolto su http://127.0.0.1:" + port);
            while (true) {
                try (Socket client = server.accept()) {
                    client.setSoTimeout(5000);
                    handleRequest(client);
                } catch (IOException error) {
                    System.err.println("Errore richiesta: " + error.getMessage());
                }
            }
        }
    }

    private static void handleRequest(Socket client) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(
                client.getInputStream(), StandardCharsets.ISO_8859_1));
        String requestLine = input.readLine();
        if (requestLine == null) {
            return;
        }
        System.out.println("Richiesta: " + requestLine);
        String line;
        while ((line = input.readLine()) != null && !line.isEmpty()) {
            System.out.println(line);
        }
        if (line == null) {
            return; // Header incompleti: il client ha chiuso.
        }

        String method = requestLine.split(" ", 2)[0];
        boolean supported = method.equals("GET") || method.equals("HEAD");
        byte[] body = (supported ? "Hello from TCP Server!\n" : "Metodo non supportato.\n")
                .getBytes(StandardCharsets.UTF_8);
        String headers = "HTTP/1.1 " + (supported ? "200 OK" : "405 Method Not Allowed") + "\r\n"
                + "Content-Type: text/plain; charset=utf-8\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + (supported ? "" : "Allow: GET, HEAD\r\n")
                + "Connection: close\r\n\r\n";
        OutputStream output = client.getOutputStream();
        output.write(headers.getBytes(StandardCharsets.US_ASCII));
        if (!method.equals("HEAD")) {
            output.write(body);
        }
        output.flush();
    }
}
