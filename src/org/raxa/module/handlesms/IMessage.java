package org.raxa.module.handlesms;
/**
 * Message type for both incoming and outgoing.
 * @author atul
 *
 */
public enum IMessage {
	NULL(false,0),
	GET(true,3),
	WRONGFORMAT(false,1),
	UNKNOWN(false,2),
	ID(true,4);
	
	
	private boolean toContinue;
	private int status;
	
	IMessage(boolean toContinue,int status){
		this.toContinue=toContinue;
		this.status=status;
	}
	
	public boolean getToContinue(){
		return toContinue;
	}
	
	public int getStatus(){
		return status;
	}
	
	
}
