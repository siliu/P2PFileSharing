/**
 * 
 */
package filesharep2p.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.google.gson.Gson;

/**
 * @author siliu
 * Transfer class is to deal with the message transfer
 * Message is composed by two parts: command and filename
 * Messgae is tranferred through Gson format
 */
public class Transfer {
	
	//Receive message from Gson stream
	public static Message receiveMessage(InputStream is){
		Message msg = new Message();
		try{
			
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			Gson gson = new Gson();
			msg = gson.fromJson(br, Message.class);
			
		}catch (Exception ex){
			System.out.println(" receive Exception: " + ex);
		}
		
		return msg;
	
	}
	
	//Send message by converting it to Gson stream
	public static void sendMessage(Message msg, OutputStream os) {
		/*
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
            Gson gson = new Gson();
            gson.toJson(msg, bw);
            bw.newLine();
            bw.flush();
        } catch (JsonIOException jsonIOException) {
        	System.out.println(" send JsonIOException: " + jsonIOException);
        } catch (IOException iOException) {
        }
        */
		 BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
         Gson gson = new Gson();
         gson.toJson(msg, bw);
         try {
			bw.newLine();
			bw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
}
