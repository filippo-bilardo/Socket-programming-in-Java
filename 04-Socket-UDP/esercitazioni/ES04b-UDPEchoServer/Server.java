/**
 * @(#)Server.java
 * 
 * @brief Server UDP che riceve un messaggio da un client e lo rispedisce indietro
 * 
 * javac Server.java; java Server;
 * 
 * @autor Filippo Bilardo
 * @version 1.00 25/10/23 
 */
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Server 
{
    private static final int UNICAST_PORT = 9876;

    public static void main(String[] args) 
    {
        try 
        {
            //Creo il DatagramSocket per lo scambio dei dati con il client
            DatagramSocket srvSocket = new DatagramSocket(UNICAST_PORT);
            //Mi preparo alla ricezione dei dati
            byte[] rcvData = new byte[1024];
            System.out.println("Server pronto per ricevere messaggi...");

            while (true) {
                //Ricevo il DatagramPacket dal client
				DatagramPacket rcvPacket = new DatagramPacket(rcvData, rcvData.length);
                srvSocket.receive(rcvPacket);
                //Stampo a schermo info e dati ricevuti
				InetAddress clntAddr = rcvPacket.getAddress();
				int clntPort = rcvPacket.getPort();
                String clntHostName = clntAddr.getHostName();
                String rcvMsg = new String(rcvPacket.getData(), 0, rcvPacket.getLength());
				System.out.print("[" + clntHostName + ":" + clntPort + "] ");
				System.out.println(rcvMsg);

                //Fine della comunicazione
				if(rcvMsg.equals("bye")) break;                

                // Invia il messaggio di echo al client
                DatagramPacket sndPacket = new DatagramPacket(rcvPacket.getData(), rcvPacket.getLength(), rcvPacket.getAddress(), rcvPacket.getPort());
                srvSocket.send(sndPacket);
            }

            srvSocket.close();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        } 
    }
}
