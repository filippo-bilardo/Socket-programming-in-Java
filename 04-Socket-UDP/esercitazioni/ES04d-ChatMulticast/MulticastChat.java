/**
 * Completare il programma in modo che:
 * 1) Il programma crei un gruppo multicast
 * 2) Il programma crei una socket per la comunicazione multicast
 * 3) Il programma associ la socket all'interfaccia di rete
 * 4) Il programma associ la socket al gruppo multicast
 * 5) Il programma crei un thread per la ricezione dei messaggi
 * 6) Il programma crei un thread per l'invio dei messaggi
 * 7) Il programma attenda la terminazione dei thread
 * 8) Il programma disassocia la socket dal gruppo multicast
 * 9) Il programma chiuda la socket
 * 
 * Il programma deve essere eseguito con 2 o più istanze per poter testare la comunicazione
 * 
 * Prendere spunto da ES04-01-UDPSenderReceiver, ES04-02-UDOEchoServer e ES04-03-UDPMulticast 
 */
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

public class MulticastChat {

    private static final String LOCAL_IP = "127.0.0.1";
    private static final String MULTICAST_IP = "230.0.0.1";
    private static final int MULTICAST_PORT = 8888;

    public static void main(String[] args) {
        try {
            // Creazione del gruppo multicast
            // Creazione della socket per la comunicazione multicast
            // Associazione della socket all'interfaccia di rete
            // Associazione della socket al gruppo multicast

            // Thread per la ricezione dei messaggi multicast
            Thread receiverThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            // Creazione del pacchetto per la ricezione dei dati

                            // Ricezione del pacchetto
                            
                            //Stampo a schermo info e dati ricevuti

                            // Se il messaggio è "exit" termina il programma
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            receiverThread.start();

            // Thread per l'invio dei messaggi
            Thread senderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Scanner scanner = new Scanner(System.in);
                        while (true) {
                            String msg = scanner.nextLine();
                            // Creazione del pacchetto per l'invio dei dati

                            // Invio del pacchetto

                            // Se il messaggio è "exit" termina il programma

                        }
                        scanner.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            senderThread.start();

            // Attendi la terminazione dei thread
            receiverThread.join();
            senderThread.join();

            // Fine della comunicazione
            // Disassociazione della socket dal gruppo multicast
            // Chiusura della socket
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
