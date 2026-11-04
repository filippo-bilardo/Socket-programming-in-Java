/**
 * Laboratorio: es01-Thread, TcpClient.java. JDK 11+.
 * Porta 8698; protocollo: campi binari writeUTF/readUTF; end riceve echo e chiude.
 * Ogni istanza Thread possiede un socket; il listener resta nel main.
 * Compilare questa cartella separatamente dalle altre varianti.
 *
 * TcpClient.java - Esempio di un semplice client TCP che invia messaggi a un server TCP
 * e riceve risposte echo dal server.
 *
 * javac -encoding UTF-8 TcpClient.java && java TcpClient
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class TcpClient {
	public static void main(String[] args) throws Exception {
        String serverAddress = "127.0.0.1";
        int serverPort = 8698;
        try (Socket socket = new Socket(serverAddress, serverPort);
             Scanner scanner = new Scanner(System.in)) {
            socket.setSoTimeout(30_000);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            while (true) {
                System.out.print("Messaggio (end per uscire)> ");
                if (!scanner.hasNextLine()) break; // EOF della console.
                String message = scanner.nextLine();
                // writeUTF include la lunghezza: readUTF ricostruisce il campo completo.
                out.writeUTF(message);
                out.flush();
                System.out.println("Server: " + in.readUTF());
                if (message.equals("end")) break; // Leggere anche l'echo del terminatore.
            }
        } catch (java.io.EOFException e) {
            System.out.println("Il server ha chiuso la connessione");
        } catch (java.io.IOException e) {
            System.err.println("Errore client: " + e.getMessage());
        }
    }
}
