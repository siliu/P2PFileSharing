package filesharep2p.utils;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.io.FileUtils;


/**
 * @author siliu
 * Receiver class is to send the "OBTAIN" command to the peer to ask for file, and receive the file from the sender.
 */
public class Receiver extends Thread {
	
	private Socket socket;
	private String filename;
	private String peerDir;
	
	public Receiver(Socket socket, String filename, String peerDir) {
		super();
		this.socket = socket;
		this.filename = filename;
		this.peerDir = peerDir;
	}
	
	public void run() {

		try {
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			
			//Send the "OBTAIN" command to the peer having the file
			System.out.println("Ask for file: " + filename);
			Message msg = new Message();
			msg.setCmd(Command.OBTAIN);
			msg.setContent(filename);
			Transfer.sendMessage(msg, os);
			
			//Obtain the file from sender and save it to the local "received" directory
			FileUtils.copyInputStreamToFile(is, new File(peerDir + "/" + filename));
			is.close();
			os.close();
			socket.close();
			
			System.out.println("Done receiving file: " + filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Cannot connect to the sender peer.");
		}
		
	}

}
