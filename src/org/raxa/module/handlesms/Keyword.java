package org.raxa.module.handlesms;

/**
 * Contains Keyword which user will send like
 * GET MENU
 * GET REM
 * GET UNREG
 * 
 *Enum shouldnot be change while runtime.Will break Menu.class
 * @author atul
 *
 */
public enum Keyword {
	REMINDER("REM","org.raxa.module.handlesms.Reminder","Get your Medicine Reminder"),
	MENU("MENU","org.raxa.module.handlesms.Menu","Get Menu"),
	REGISTER("REG","org.raxa.module.handlesms.Registration","Register yourself for the service"),
	UNREGISTER("UNREG","org.raxa.module.handlesms.UnRegister","Unregister yourself from the service");
	
	private final String keyword;
	private final String classname;
	private final String description;
	Keyword(String keyword,String classname,String description){
		this.keyword=keyword;
		this.classname=classname;
		this.description=description;
	}
	
	public Class getOptionClass(){
		try{
			return Class.forName(classname);
		}
		catch(Exception ex){
			return null;
		}
	}
	
	public String getKeyword(){
		return keyword;
	}
	
	public String getDescription(){
		return description;
	}
}
