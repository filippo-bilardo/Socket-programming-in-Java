/**
 * Laboratorio: es01-Thread, TcpServer.java. JDK 11+.
 * Porta 8698; protocollo: campi binari writeUTF/readUTF; end riceve echo e chiude.
 * Ogni istanza Thread possiede un socket; il listener resta nel main.
 * Compilare questa cartella separatamente dalle altre varianti.
 *
 * Class TcpServer che estende Thread per gestire la comunicazione con un client TCP.
 * Utilizza DataInputStream e DataOutputStream per la comunicazione.
 * 
 * Ogni istanza di TcpServer gestisce un singolo client in un thread separato.
 * Il server riceve messaggi dal client e li rispedisce indietro (echo) fino a quando il client non chiude la connessione.
 * 
 * version 1.0 - 18/10/25
 * author Filippo Bilardo
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

class TcpServer extends Thread {
	private final Socket serverClientSocket;
	private final int clientNo;
	
	TcpServer(Socket inSocket, int ClientNo) {
		serverClientSocket = inSocket;
		clientNo = ClientNo;
	}

	public void run() {
		try (Socket owned = serverClientSocket) {
			serverClientSocket.setSoTimeout(30000);
			// Leggi e scrivi campi UTF modificato Java: non sono righe testuali.
			DataInputStream inStream = new DataInputStream(serverClientSocket.getInputStream());
			DataOutputStream outStream = new DataOutputStream(serverClientSocket.getOutputStream());

			String cMsg = "";
			String sMsg = "";

			while (!cMsg.equals("end")) {

				//Leggiamo il messaggio proveniente dal client, e lo stampiamo a schermo
				cMsg = inStream.readUTF();
				System.out.println("Server.Thread " + clientNo + " Ricevuto messaggio " + cMsg );

				//Echo
				sMsg=cMsg;
				System.out.println("Server.Thread " + clientNo + " Invio messaggio " + sMsg );
				outStream.writeUTF(sMsg);
				outStream.flush();
			}
			
			// Il client legge l'echo di end; nessun ulteriore messaggio non richiesto.
            // Il try-with-resources chiude socket e stream anche in caso di errore.

		} catch (java.io.EOFException ex) {
            System.out.println("Client " + clientNo + " disconnesso prima di end");
		} catch (java.io.IOException ex) {
			System.out.println(ex);
		} finally {
			System.out.println("Client -" + clientNo + " exit!! ");
		}
	}
}
