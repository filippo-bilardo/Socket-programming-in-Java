/**
 * Laboratorio: es01-Thread, MultithreadedTcpServer.java. JDK 11+.
 * Porta 8698; protocollo: campi binari writeUTF/readUTF; end riceve echo e chiude.
 * Ogni istanza Thread possiede un socket; il listener resta nel main.
 * Compilare questa cartella separatamente dalle altre varianti.
 *
 * Multithreaded TCP Server che accetta connessioni da più client
 * e gestisce ciascuno in un thread separato utilizzando la classe TcpServer.
 *
 *
 * javac -encoding UTF-8 MultithreadedTcpServer.java TcpServer.java;
 * java MultithreadedTcpServer
 *
 * version 1.0 - 18/10/25
 * author Filippo Bilardo
 */
import java.net.ServerSocket;
import java.net.Socket;

public class MultithreadedTcpServer {
    static final int MAX_CONN = 999; // Limite delle accettazioni totali, non dei client attivi.
    static final int SRV_PORT = 8698;

    public static void main(String[] args) throws Exception {
        int count = 0; // Confinato al listener: i gestori ricevono una copia del numero.
        try (ServerSocket server = new ServerSocket(SRV_PORT)) {
            System.out.println("Server in ascolto sulla porta " + SRV_PORT);
            while (count < MAX_CONN) {
                Socket socket = server.accept(); // Un socket distinto a ogni iterazione.
                TcpServer handler = new TcpServer(socket, ++count);
                handler.setName("ServerThread-" + count);
                handler.start(); // start avvia il gestore; il listener torna subito ad accept.
            }
        } // Dopo 999 accettazioni chiude il listener; i gestori avviati proseguono.
    }
}
