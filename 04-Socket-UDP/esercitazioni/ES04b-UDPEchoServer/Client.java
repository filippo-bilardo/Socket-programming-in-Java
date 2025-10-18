/**
 * Client UDP che invia una stringa al server e attende la risposta.
 * 
 * javac Client; java Client;
 * 
 * @autor Filippo Bilardo 
 * @version 1.00 11/11/2023
 */ 
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Client 
{
    private static final String UNICAST_IP = "127.0.0.1";
    private static final int UNICAST_PORT = 9876;

    public static void main(String[] args) 
    {
        try 
        {
            //Creo lo scanner per leggere da tastiera
            Scanner scanner = new Scanner(System.in);

            //Creo il DatagramSocket per lo scambio dei dati con il server
            DatagramSocket clntSocket = new DatagramSocket();
            InetAddress srvAddr = InetAddress.getByName(UNICAST_IP);
            int srvPort = UNICAST_PORT;

            while (true) 
            {
				//Leggo da tastiera una stringa e la invio al server
				System.out.print("> "); //prompt
				String sndMsg = scanner.nextLine();
				byte[] sndData = sndMsg.getBytes();
				DatagramPacket sndPacket = new DatagramPacket(sndData, sndData.length, srvAddr, srvPort); 
				clntSocket.send(sndPacket);
				
				//Fine della comunicazione
				if(sndMsg.equals("bye")) break;

				//Attendo la ricezione del DatagramPacket dal server 
				byte[] rcvData = new byte[1024];
				DatagramPacket rcvPacket = new DatagramPacket(rcvData, rcvData.length);																									
				clntSocket.receive(rcvPacket);
				//Visualizzo a schermo i dati del pacchetto ricevuto
				String rcvMsg = new String(rcvPacket.getData(), 0, rcvPacket.getLength());
				System.out.print("[" + rcvPacket.getAddress().getHostName() + ":" + rcvPacket.getPort() + "] ");
				System.out.println(rcvMsg);

				//Fine della comunicazione
				if(rcvMsg.equals("bye")) break;
            }
            scanner.close();
            clntSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
