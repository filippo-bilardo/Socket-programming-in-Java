/**
 * from network/..
 * javac network/Receiver.java; java network.Receiver 
 * javac Receiver.java; java network.Receiver 
 * 
 * @Filippo Bilardo
 * @version 1.00 11/11/2023
 */
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Receiver {

    private static final int RECEIVE_PORT = 8698;

	public static void main(String[] args) throws IOException 
	{
		//Opens a datagram socket on the specified port
		DatagramSocket socket = new DatagramSocket(RECEIVE_PORT);
		
		//Constructs a datagram packet for receiving the packets of specified length
		byte[] buf = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buf, 1024);
		//Receives a datagram packet from this socket
		System.out.println("Receiver: starting on port " + RECEIVE_PORT);
		socket.receive(packet);
		String msg = new String(packet.getData(), 0, packet.getLength());
		System.out.println("Receiver, ho ricevuto il seguente messaggio "+ msg);
		
		//Closing the Datagram socket
		socket.close();
	}
}
