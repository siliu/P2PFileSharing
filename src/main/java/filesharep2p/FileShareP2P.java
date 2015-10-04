package filesharep2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;

import filesharep2p.utils.Address;

/**
 * @author siliu
 *
 */

public class FileShareP2P {
	
    public static void printUsage() {
    	System.out.println("* * * * * * * * * * * * * * * * * *");
        System.out.println("*  CS550 PA1: File Sharing System *");
        System.out.println("*                                 *");
        System.out.println("*      Name: Si Liu               *");
        System.out.println("*      CWID: A20334820            *");
        System.out.println("* * * * * * * * * * * * * * * * * *");
        System.out.println("Brief Instruction:");
        System.out.println("(IndexServer and all client peers are on localhost. Port number is specified to distiguish different peers.)");
        System.out.println("Commands: REGISTER, SEARCH, LOOKUP, OBTAIN, DELETE, EXIT ");
        System.out.println("[REGISTER]: Register all files on this peer to the index server.");
        System.out.println("  [SEARCH]: Search the location of a specific file from the index server. The index server return the list of peer IDs having the file.");
        System.out.println("  [LOOKUP]: Look up the IP address and port number of a peer through its peer ID.");
        System.out.println("  [OBTAIN]: Download a file to the current peer from a remote peer.");
        System.out.println("  [DELETE]: Delete a file from the peer specified.");
        System.out.println("  [EXIT]: Exit this client.");
        System.out.println("Usage: Input the command or parameter as each promot says.");
    }
    
    public static void main(String[] args){
    	
    	printUsage();
    	System.out.println("IndexServer is on localhost, and listening on port 5800.");
    	System.out.println("Please input the client port for this peer: ");
    	Scanner inputScanner = new Scanner(System.in);
    	String inputRaw = inputScanner.nextLine();
    
    	Peer peer = new Peer(Integer.parseInt(inputRaw));
    	peer.startup();
    	System.out.println("This peer is running ... ");
    	//System.out.println("The peerId for this peer is: " + peer.getPeerId());
    	
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	String userInput;
    	System.out.println("Please input command:  ");
    	
    	try {
			while((userInput = br.readLine()) != null){
				
				if(userInput.equalsIgnoreCase("REGISTER")){
					
					peer.registry(peer.getPeerId(), peer.getSharedFiles());
					System.out.println("The registration of shared files on this peer is finished!");
					
				}else if(userInput.equalsIgnoreCase("SEARCH")){
					
					System.out.println("Please input the filename to search: ");
					String filename = br.readLine();
					Set<Long> peerList = peer.search(filename);
					System.out.println("This file is located at peers: " + peerList);
					
				}else if(userInput.equalsIgnoreCase("LOOKUP")){
					
					System.out.println("Please input the peerId to look up: ");
					long peerId = Long.parseLong(br.readLine());
					Address peerAddress = peer.lookup(peerId);
					peer.setPort(peerAddress.getPort());
					
				}else if(userInput.equalsIgnoreCase("OBTAIN")){
					
					System.out.println("Please input the filename to obtain: ");
					String obtainfilename = br.readLine();
					System.out.println("Please pick up one peerId from its peer list to download the file: ");
					long obtainPeerId = Long.parseLong(br.readLine());
				    peer.obtain(obtainfilename,obtainPeerId);
					
				}else if(userInput.equalsIgnoreCase("DELETE")){
					
					System.out.println("Please input the peerId to delete files from : ");
					long peerId = Long.parseLong(br.readLine());
					System.out.println("Please input the file to delete from the peer " + peerId + " : ");
					ArrayList<String> fileList = new ArrayList<String>();
					fileList.add(br.readLine());
					peer.delete(peerId, fileList);

				}else if(userInput.equalsIgnoreCase("EXIT")){
					
					System.out.println("Exit this client.");
					peer.exit();
					
				}else{
					
					System.out.println("This command is not supported yet! ");
					System.out.println("Commands supported:  REGISTER, SEARCH, LOOKUP, OBTAIN, DELETE, EXIT");
	                System.out.println("Please input command:  ");
	
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	
    	
    }
    

}
