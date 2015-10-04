package filesharep2p.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;

/**
 * @author siliu
 * Sender class is to accept the file request from the receiver and send the file to the receiver.
 */
public class Sender extends Thread {
	
	//private String filename;
	private Socket socket;
	private String peerDir;
	
	public Sender(Socket socket, String peerDir) {
		//super();
		// this.filename = filename;
		this.socket = socket;
		this.peerDir = peerDir;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 * 1. Get the request from the socket inputstream.
	 * 2. Parse the inputstream to get the filename.
	 * 3. Put the file to output stream for sending.
	 */
	public void run () {
		
		try {
			
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			Gson gson = new Gson();
			Message msg = gson.fromJson(br.readLine(), Message.class);
			String filename = msg.getContent().toString();
			
			System.out.println("Sending file: " + filename);
			
			FileUtils.copyFile(new File(peerDir + "/" + filename), os);
			os.flush();
			socket.shutdownOutput();
			is.close();
			os.close();
			socket.close();
			
			System.out.print("Done sending file: " + filename);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, e);
		}
		
		
		
		
	} 
	

}
