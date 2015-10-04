package filesharep2p;


import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import filesharep2p.utils.Address;
import filesharep2p.utils.Command;
import filesharep2p.utils.FileMap;
import filesharep2p.utils.Message;
import filesharep2p.utils.Transfer;

/**
 * @author siliu
 *
 */
public class IndexServer implements Runnable {
	
	private static final int port = 5800;
	private Socket socket;
	private static Map<Long, Address> addressTable = new ConcurrentHashMap<Long, Address>();
	private static Map<String, Object> fileMap = new ConcurrentHashMap<String, Object>();
	
	public IndexServer(Socket socket){
		this.socket = socket;
	}
	
	//Search addressTable if the peerId has already registered for a peer
	public synchronized static Address lookupAddressTable(long peerId){
		
		Address peerAddress = null;
		if(addressTable.containsKey(peerId))
			peerAddress = addressTable.get(peerId);
		return peerAddress;
	}
	
	
	public synchronized void registry(Map<String, Object> newPeerFileMap){
		
		FileMap.executeRegistry(newPeerFileMap, fileMap);
		System.out.println("The current file-peer registration table is: ");
		FileMap.printMap(fileMap);
	}
	
	public synchronized void search(Map<String, Object> resultPeerFileMap){
		
		FileMap.executeSearch(resultPeerFileMap, fileMap);
		// FileMap.printMap(fileMap);
	}
	
	public synchronized void delete(Map<String, Object> deletePeerFileMap){
		
		FileMap.executeDelete(deletePeerFileMap, fileMap);
		FileMap.printMap(fileMap);
		
	}
	
	public void run(){
		
		try {
			InputStream is = socket.getInputStream();
			
			/*
			OutputStream os = socket.getOutputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
			
			Gson gson = new Gson();
			Message msg = gson.fromJson(br, Message.class);
			Command cmd = msg.getCmd();
			*/
			Message msg = Transfer.receiveMessage(is);
			Command cmd = msg.getCmd();
			
			if(cmd == Command.REGISTER) {
				
				Map<String, Object> receivedMap = new HashMap<String, Object>(); 
				receivedMap = (Map<String, Object>) msg.getContent();
				//System.out.println("receivedMap: " + receivedMap);
				
				//1.Register the peerId into the addressTable
				//If it is a new peer(peerId=null), generate a new peerId for it and registered it into the addressTable. Otherwise, skip this step.
				
				long newPeerId = 0;
				
				if(receivedMap.get("peerId").equals("-1")){      // The default peerId is -1 if the peer is not registered at the indexServer yet.
					newPeerId = 10001;
					while(lookupAddressTable(newPeerId) != null){
						newPeerId++;
					}
					Address newPeerAddress = new Address();
					String hostname = socket.getInetAddress().getHostAddress();  
					newPeerAddress.setHostname(hostname);
					
					double  peerPort = (Double) receivedMap.get("peerPort"); // peerPort is used for the peer download server to accept download request.
					newPeerAddress.setPort((int)peerPort);
					addressTable.put(newPeerId, newPeerAddress);     //register the peer into the addressTable
					
				}else{
					newPeerId = Math.round((Double)receivedMap.get("peerId"));
				}
				
				ArrayList<String> fileList = (ArrayList<String>) receivedMap.get("fileList");
				
				//2.Register the file into the FileMap
				//The filePeerMap is a map between  one file and its peerId set. 
				Map<String, Object> filePeerMap = new HashMap<String,Object>();
				Set<Long> peerSet = new HashSet<Long>();
				peerSet.add(newPeerId);
				
				Iterator fileIterator = fileList.iterator();
				
				while(fileIterator.hasNext()){
					
					String file = (String) fileIterator.next();
					filePeerMap.put(file, peerSet);
				}
				
				this.registry(filePeerMap);  
				
				Message returnMsg = new Message(Command.OK, newPeerId);
				Transfer.sendMessage(returnMsg,this.socket.getOutputStream());
				socket.shutdownOutput();
				
			}if(cmd == Command.SEARCH){
				
				Map<String, Object> resultPeerFileMap = new HashMap<String, Object>();
				String filename = (String) msg.getContent();
				Set<Long> peerSet = new HashSet<Long>();
				resultPeerFileMap.put(filename, peerSet);
				this.search(resultPeerFileMap);
				peerSet = (Set<Long>) resultPeerFileMap.get(filename);
				
				//System.out.println("(1) peerSet: " + peerSet);
				Message returnMsg = new Message(Command.OK, peerSet);
				Transfer.sendMessage(returnMsg, this.socket.getOutputStream());
				socket.shutdownOutput();
				
			}if(cmd == Command.DELETE){
				
				Map<String, Object> deletePeerFileMap = new HashMap<String, Object>(); 
				deletePeerFileMap = (Map<String, Object>) msg.getContent();
				
				this.delete(deletePeerFileMap);
				
				Message returnMsg = new Message(Command.OK,null);
				Transfer.sendMessage(returnMsg, this.socket.getOutputStream());
				socket.shutdownOutput();
				
			}if(cmd == Command.LOOKUP){
				
				long peerId = Math.round((Double) msg.getContent());
				Address peerAddress = this.lookupAddressTable(peerId);
				
				Message returnMsg = new Message(Command.OK, peerAddress);
				Transfer.sendMessage(returnMsg, this.socket.getOutputStream());
				socket.shutdownOutput();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		
		boolean listening = true;
		try {
			ServerSocket ss = new ServerSocket(port);
			System.out.println("Index Server is running...");
			System.out.println("Listening on port " + port +".");
			
			while(listening){
				new Thread(new IndexServer(ss.accept())).start();
			}
			ss.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Could not listen on port: " + port + ".");
			System.exit(1);
		}
		
	}

}
