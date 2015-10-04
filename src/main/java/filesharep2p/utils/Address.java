package filesharep2p.utils;

/**
 * @author siliu
 *
 */
public class Address {
	
	private String hostname;
	private int port;
	
	public void setHostname(String hostname){
		this.hostname = hostname;
	}
	
	public String getHostname(){
		return this.hostname;
	}
	
	public void setPort(int port){
		this.port = port;
	}
	
	public int getPort(){
		return this.port;
	}
}
