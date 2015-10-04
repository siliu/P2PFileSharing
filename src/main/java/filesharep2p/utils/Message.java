package filesharep2p.utils;

import com.google.gson.Gson;

/**
 * @author siliu
 *
 */
 public class Message {

	private Command cmd;
	private Object content;

	public Message(){
		
	}
	public Message(Command cmd, Object content) {
		super();
		this.cmd = cmd;
		this.content = content;
	}

	public Command getCmd() {
		return cmd;
	}

	public void setCmd(Command cmd) {
		this.cmd = cmd;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	@Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this,Message.class);
	}

}
