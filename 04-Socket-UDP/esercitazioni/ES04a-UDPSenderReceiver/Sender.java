/**
 * from network/..
 * javac network/Sender.java; java network.Sender 
 * javac Sender.java; java network.Sender 
 * 
 * @Filippo Bilardo
 * @version 1.00 11/11/2023
*/
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Sender {

	private static final String DEST_IP = "127.0.0.1";
    private static final int DEST_PORT = 8698;

	public static void main(String[] args) throws IOException 
	{	
		//Create Datagram socket that binds to any available port in localhost
		DatagramSocket socket = new DatagramSocket();

		//Create Datagram packet and send
		String message = "Hello Message using UDP";
		System.out.println("Sender: starting on port " + DEST_PORT);
		InetAddress destIpAddr = InetAddress.getByName(DEST_IP);
		DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(), destIpAddr, DEST_PORT);
		socket.send(sendPacket);
		System.out.println("Sender: ho inviato il messaggio " + message);
		
		//Close the socket
		socket.close();
	}

}
