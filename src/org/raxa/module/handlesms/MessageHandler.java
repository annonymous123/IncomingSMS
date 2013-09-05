package org.raxa.module.handlesms;
import java.util.Date;
import org.raxa.module.sms.SMSResponse;
import org.raxa.module.sms.SMSSender;

public class MessageHandler {
	
	String message;
	
	public static int getMessageOption(String message) {
		String[] split=message.split(" ");
		int option=0;
		try{
			option=Integer.parseInt(split[1]);
		}
		catch(Exception ex){}
		return option;
	}
	
	public MessageHandler(String message){
		this.message=message;
	}
	
	public MessageHandler(){}
	
	public IMessage getMessageFormat(){
		return getMessageFormat(message);
	}
	
	public IMessage getMessageFormat(String message){
		if(message==null)
			return IMessage.NULL;
		String[] split=message.split(" ");
		if(!(split.length>=2))
			return IMessage.WRONGFORMAT;
		String firstWord=split[0].toUpperCase();
		if(firstWord.equals(IncomingSMS.hello))
			return IMessage.GET;
		if((firstWord.length()==3 && isAlpha(firstWord)))
			return IMessage.ID;
		return IMessage.UNKNOWN;
	}
	
	
	public boolean isAlpha(String firstword) {
	    return firstword.matches("[a-zA-Z]+");
	}
	
	
	//Langauge may come as null.Take care
	public void sendSMS(Date inDate,String pnumber, String message, Language language) {
		String senderId="TEST SMS"; Date outDate=new Date();
		IncomingSMS.out.println("Sending SMS:"+message+"\n \nReceiver PhoneNumber:"+pnumber+" in Language:"+language.getLanguage());
		SMSResponse response=new SMSSender().sendSMSThroughGateway(message, pnumber, senderId,language.getLanguage());
	//	updateDatabase(inDate,outDate,pnumber,message,language.getLanguage(),response.getIsSuccess(),response.getTransID());
		
	}

	public String getErrorMessage(RMessage message) {
		
		return message.getMessage();
	}

	public String getErrorMessage(){
		return "Sorry Invalid Input.Try Again.Type GET MENU to get menu option";
	}
	
	public String getErrorMessage(IMessage messageType) {
		
		return "Sorry Invalid Input.Try Again.Type GET MENU to get menu option";
	}
	
	
}
