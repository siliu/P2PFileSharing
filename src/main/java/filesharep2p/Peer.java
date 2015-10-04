package filesharep2p;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;

import com.google.gson.internal.StringMap;



import filesharep2p.utils.Address;
import filesharep2p.utils.Command;
import filesharep2p.utils.Message;
import filesharep2p.utils.Receiver;
import filesharep2p.utils.Sender;
import filesharep2p.utils.Transfer;

/**
 * @author siliu
 * 
 */
public class Peer {
	
	private long peerId;
	private int port;
	private FileDownloadServer server;
	private String indexServerIP;
	private int indexServerPort;
	private Socket socket = null;
	private String peerDir;
	
	/**
	 * Peer constructor.
	 * The default IndexServer IP is localhost.
	 * The default IndexServer port to listen new request is 5800. 
	 * @param port: Peer download server listening port.
	 */
	public Peer(int port){
		
		this(port, "localhost", 5800);
		this.peerId = -1;
		this.server = new FileDownloadServer(this.port, this.peerDir);
		this.server.setDaemon(true);
		this.peerDir = Integer.toString(port);      //Use the port number as the name of local directory for peer
		File shared = new File(peerDir);
		shared.mkdir();
	}
	
	/**
	 * Peer constructor.
	 * The IP and Port of the IndexServer can be specified.
	 * @param port: Peer download server listening port.
	 * @param indexServerIP
	 * @param indexServerPort
	 */
	
	public Peer(int port, String indexServerIP, int indexServerPort){
		
		this.peerId = -1;
		this.port = port;
		this.server = new FileDownloadServer(this.port,this.peerDir);
		this.server.setDaemon(true);
		this.indexServerIP = indexServerIP;
		this.indexServerPort = indexServerPort;
		this.peerDir = Integer.toString(port);
		File shared = new File(peerDir);
		shared.mkdir();
	}
	
	public void setPort(int port){
		this.port = port;
	}
	
	public int getPort(){
		return port;
	}
	
	public void setPeerId(long peerId){
		this.peerId = peerId;
	}
	
	public long getPeerId(){
		return peerId;
	}
	
	/**
	 * Get the file list in the folder of this peer. The folder name is the same as the peer port.
	 * @return
			OutputStream os = socket.getOutputStream();
	 */
	
	public ArrayList<String> getSharedFiles(){
		
		ArrayList<String> sharedFileList = new ArrayList<String>();
		
		File sharedDir = new File(peerDir);
		
		Iterator<File> iter = FileUtils.iterateFiles(sharedDir, null, false);
		
		while(iter.hasNext()){
			sharedFileList.add(iter.next().getName());
		}
		
		return sharedFileList;
	}
	
	/**
	 * checkMessage function is to check if the IndexServer returend OK command to the peer.
	 * @param msgIn : The input message to check
	 * @return
	 */
	private boolean checkMessage(Message msgIn){
		
		if(msgIn != null && msgIn.getCmd() != null && msgIn.getCmd().equals(Command.OK))
			return true;
		
		return false;
	}
	
	/**
	 * registry function is called when peer is started and file folder is changed
	 * @param peerId
	 * @param fileList
	 */
	public void registry(long peerId, ArrayList<String> fileList){
		
		try {
			socket = new Socket(this.indexServerIP, this.indexServerPort);

			/*
			Map<String, Object> filePeerMap = new HashMap<String,Object>();
			Set<Long> peerSet = new HashSet<Long>();
			peerSet.add(peerId);
			
			Iterator fileIterator = fileList.iterator();
			
			while(fileIterator.hasNext()){
				
				String file = (String) fileIterator.next();
				filePeerMap.put(file, peerSet);
			}
			Message msgOut = new Message(Command.REGISTER,filePeerMap);
			*/
			
			// Send the peer port, peerId and file list to register to the index server.
			Map<String, Object> registryParameters = new HashMap<String,Object>();
			
			
			if(peerId < 0){
				registryParameters.put("peerId", "-1");
			}else{
				registryParameters.put("peerId", peerId);
			}
			
			registryParameters.put("peerPort", port);
			registryParameters.put("fileList", fileList);
			
			Message msgOut = new Message(Command.REGISTER, registryParameters);
			Transfer.sendMessage(msgOut, this.socket.getOutputStream());
			socket.shutdownOutput();
			
			//Set the peerId for this peer
			Message msgIn = Transfer.receiveMessage(this.socket.getInputStream());
			if(checkMessage(msgIn)){
				//this.peerId = (Long) msgIn.getContent();
				this.peerId = Math.round((Double)msgIn.getContent());
			}
			socket.shutdownInput();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Set<Long> listToSet(List list){
		Set<Long> set = new HashSet<Long>();
		for(int i=0; i<list.size(); i++)
			set.add(Math.round((Double)list.get(i)));
		return set;
	}
	/*
	 * search function is called when "SEARCH" command is executed
	 * It is to search the peers where a file is located.
	 */
	public Set<Long> search(String filename){
		
		Set<Long> peerSet = new HashSet<Long>();
		try {
			socket = new Socket(this.indexServerIP, this.indexServerPort);
			Message msgOut = new Message(Command.SEARCH,filename);
			Transfer.sendMessage(msgOut, this.socket.getOutputStream());
			socket.shutdownOutput();
			
			Message msgIn = Transfer.receiveMessage(this.socket.getInputStream());
			if(checkMessage(msgIn)){
				// System.out.println("msgIn.getContent()" + msgIn.getContent());
				peerSet = this.listToSet((List) msgIn.getContent()) ;
				//System.out.println(filename + " is located on peers: " + peerSet);
			}else{
				System.out.println("This file is not in the cluster.");
			}
			socket.shutdownInput();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return peerSet;
	}
	
	/*
	 * delete function is called when "DELETE" command is executed
	 * It is to delete a file record from the fileMap
	 */
	public void delete(long peerId, ArrayList<String> fileList){
		
		try {
			socket = new Socket(this.indexServerIP, this.indexServerPort);
			
			Map<String, Object> filePeerMap = new HashMap<String,Object>();
			Set<Long> peerSet = new HashSet<Long>();
			peerSet.add(peerId);
			
			Iterator fileIterator = fileList.iterator();
			
			while(fileIterator.hasNext()){
				
				String file = (String) fileIterator.next();
				filePeerMap.put(file, peerSet);
			}
			
			Message msgOut = new Message(Command.DELETE,filePeerMap);
			Transfer.sendMessage(msgOut, this.socket.getOutputStream());
			socket.shutdownOutput();
			
			//Set the peerId for this peer
			Message msgIn = Transfer.receiveMessage(this.socket.getInputStream());
			if(checkMessage(msgIn)){
				Command cmd = msgIn.getCmd();
				System.out.println("Delete " + cmd + ".");
			}
			socket.shutdownInput();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * lookup function is to send LOOKUP request to the Index Server to query the address of the corresponding peerId
	 */
	public Address lookup(long peerId){
		
		Address peerAddress = new Address();
		try {
			socket = new Socket(this.indexServerIP, this.indexServerPort);
			Message msgOut = new Message(Command.LOOKUP, peerId);
			Transfer.sendMessage(msgOut, this.socket.getOutputStream());
			socket.shutdownOutput(); 
			
			Message msgIn = Transfer.receiveMessage(this.socket.getInputStream());
			if( checkMessage(msgIn)){
				StringMap msgMap = (StringMap) msgIn.getContent();
				String hostname = (String) msgMap.get("hostname");
				double portD = (Double)msgMap.get("port");
				peerAddress.setHostname(hostname);
				peerAddress.setPort((int)portD);
				
				System.out.println("The address of the peer " + peerId + " is :" + peerAddress.getHostname() +  ":" + peerAddress.getPort());
			}else{
				System.out.println("This peer is not registered at the index server.");
			}
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return peerAddress;
	}
	
	/**
	 * obtain function is to download a file from other peer by providing the remotePeerId and filename
	 * 1. Get the remote peer address from index server using peerId
	 * 2. Download the file from the remote peer
	 */
	public void obtain(String filename, long remotePeerId){
		/*
		long remotePeerId = -1;
		Set<Long> peerSet = this.search(filename);
		
		if(peerSet.isEmpty()){
			return;
		}
		
		Iterator peerIter = peerSet.iterator();
		
		while( peerIter.hasNext()){
			long pid = (Long) peerIter.next();
			if(pid != this.peerId){
				remotePeerId = pid;
			    break;
			}
		}
		
		if(remotePeerId == -1){
			return;
		}
		*/
		Address remoteAddress = this.lookup(remotePeerId);
		if(remoteAddress != null){
			String remoteHost = remoteAddress.getHostname();
			int remotePort = remoteAddress.getPort();
			
			try {
				socket = new Socket(remoteHost, remotePort);
				//Message msgOut = new Message(Command.OBTAIN, filename);
				
				Receiver receiver = new Receiver(socket,filename, peerDir);
				receiver.start();
				
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
	}
	
	
	/**
	 * Stop the FileDownloadServer on this peer.
	 */
	public void exit(){
		this.server.close();
	}
	
	/**
	 * startup function implements the function when a peer starts up
	 * 1. monitor changes in the file "shared" folder 
	 * 2. start file registration when changes happened
	 */
	public void startup(){

		try {
			//Register all files in the shared folder named as the port number when peer starts up
			registry(peerId, getSharedFiles());
			
			this.server.start();
			
			FileSystemManager fsManager = VFS.getManager();
			
			FileObject listenFolder = fsManager.resolveFile(new File(peerDir).getAbsolutePath());
			
			DefaultFileMonitor fm = new DefaultFileMonitor(new FileListener() {
				private synchronized void updateRegistry(){
					registry(peerId, getSharedFiles());
				}
				
				public void fileCreated(FileChangeEvent fce){
					this.updateRegistry();
				}
				
				public void fileDeleted(FileChangeEvent fce){
					this.updateRegistry();
				}
				
				public void fileChanged(FileChangeEvent fce){
					this.updateRegistry();
				}
				
			});
			
			fm.setRecursive(false);
			fm.addFile(listenFolder);
			fm.start();
			
		} catch (FileSystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	public class FileDownloadServer extends Thread {

        private final int port;
        private boolean running;
        private ServerSocket listener;
        private String peerDir;

        private FileDownloadServer(int port,String peerDir) {
            this.port = port;
            this.peerDir = peerDir;
        }

        @Override
        public void run() {
            this.running = true;
            try {
                listener = new ServerSocket(port);
                while (running) {
                    try {
                        Socket sock = listener.accept();
                        Sender sender = new Sender(sock,peerDir);
                        sender.start();
                    } catch (SocketException socketException) {
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                this.running = false;
            }

        }

        public void close() {
            this.running = false;
            if (this.listener != null) {
                try {
                    this.listener.close();
                } catch (IOException ex) {
                    Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
