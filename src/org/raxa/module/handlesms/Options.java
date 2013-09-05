package org.raxa.module.handlesms;
/**
 * CAUTION:Following Things should be Kept in Mind while creating any option
 * 1.The class itself has to handle changing of state.So before returning from the class ensure that you have changed your state accordingly so that the
 * next user query will go to that state directly.
 * 2.If the class has answered the user query and does not expect a reply from user be sure to make deleteSession to true such that no further query from that session will be entertain
 * 
 * 
 * If the option entered by the user is not expected return null and set deleteSession to false
 * 
 * If there is nothing to send return null.
 * 
 * If state=-1 that means it is redirected from main menu.
 * 
 * @author atul
 *
 */
public abstract class Options {
		protected int state=0;
		protected boolean deleteSession=false;
		private final String userID;
		protected Language language=Language.ENGLISH;
		private final String pnumber;
		protected String pid=null;
		protected String pname;
		private boolean ifVerifyNumber=false;
		/**
		 * KeyWord associated with the option.
		 * ex. REM for medicine Reminder
		 * 
		 * @return
		 */
		public abstract String getKeyWord();
		
		/**
		 * Gets the description of the option which will be visible in main-menu.
		 * 1.Medicine Reminder
		 * @return
		 */
		protected void setState(int i){
			state=i;
		}
		protected boolean ifVerifyNumber(){
			return ifVerifyNumber;
		}
		
		protected boolean toDeleteSession(){
			return deleteSession;
		}
		
		protected void setNumberVerification(boolean b){
			ifVerifyNumber=true;
		}
		
		protected abstract String getDescription();
		
		protected String getPid(){
			return pid;
		}
		
		protected void setPid(String s){
			pid=s;
		}
		
		protected int getState(){
			return state;
		}
		
		protected void setPname(String s){
			this.pname=s;
		}
		
		protected String getPname(){
			return pname;
		}
		
		protected String getLastLine(){
			return "Type "+userID+" (space)"+"(your option)"+"to send your option.";
		}
		
		public Language getLanguage(){
			return language;
		}
		
		
		
		public void setLanguage(Language l){
			this.language=l;
		}
		
		public String getPhoneNumber(){
			return pnumber;
		}
		
		public abstract String getReply(String mesasge,String pnumber);
		
				
		public String getUserID(){
			return userID;
		}
		
		public Options(int state,boolean deleteSession,String userID,String pnumber){
			this.state=state;
			this.deleteSession=deleteSession;
			this.userID=userID;
			this.pnumber=pnumber;
		}
		
		public Options(int state,boolean deleteSession,String pnumber){
			this(state,deleteSession,null,pnumber);
		}
		/**
		 * First step(accessed via keyword or through main menu) i.e head is 0;
		 */
		public Options(String userID,String pnumber){
			this(0,false,userID,pnumber);
		}
		
}
