/**
 * from network/..
 * javac network/TcpClient.java
 * java network.TcpClient 
 */
package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
//import java.util.Scanner;

public class TcpClient {
	public static void main(String[] args) throws Exception {
		
		String severAddress="127.0.0.1";  // localhost
		int severPort=8765;
		String clientMsg = "";
		String serverMsg = "";
		
		try {
			// Create connection to server socket
			System.out.print("Client: Connessione al server=" + severAddress + ":" + severPort + " ... ");
			Socket socket = new Socket(severAddress, severPort); 
			System.out.println("Connected");

			// Create input and output streams to read/write data
			// Input stream per i dati provenienti dal socket 
			DataInputStream inSocketStream = new DataInputStream(socket.getInputStream());
			// Output stream 
			DataOutputStream outSocketStream = new DataOutputStream(socket.getOutputStream());
			
			clientMsg = "GET /ACCENDI HTTP/9.9\r\n"; 	
            clientMsg += "Host: fake:8765\r\n";
            //clientMsg += "\r\n";
            
            // Send the entered text to server
			System.out.println(clientMsg);
            outSocketStream.writeChars(clientMsg);
            outSocketStream.flush();

            // Read data from socket input stream
            serverMsg = inSocketStream.readUTF();
            System.out.println(serverMsg);
			
			// Close resources
			outSocketStream.close();
			inSocketStream.close();
			socket.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
