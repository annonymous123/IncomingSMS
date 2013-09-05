package org.raxa.module.handlesms;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Cache;

/**
 * Servlet implementation class IncomingSMS
 */
@WebServlet("/incomingsms")
public class IncomingSMS extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(IncomingSMS.class);
	private static Cache<String, Options> cache; 
	private static Map<String,Class> map;
	private static int maxSize;
	private static char alphas[];
	private static int alphasLength;
	protected final static String hello="GET";
	private final static int helloStringID=4856;
	private static Set<String> uncachedID;
	private Language defaultLanguage;
	public static Map<String,Language> languageMap;
	public static PrintWriter out;
	
	
	public IncomingSMS() {
        super();
        
    }
    
    public void init() throws ServletException {
    	
    	 alphas ="ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
         alphasLength=alphas.length;
         maxSize=alphasLength*alphasLength*alphasLength;
         map=new HashMap<String,Class>();
         uncachedID=new HashSet<String>();
         putObjectInMap();
         defaultLanguage=(Language.ENGLISH);
         cache=CacheBuilder.newBuilder().
				maximumSize(maxSize).
				expireAfterAccess(5, TimeUnit.MINUTES).
				build();
         languageMap=new HashMap<String,Language>();
         for(Language l:Language.values()){
        	 languageMap.put(l.getLanguage().toUpperCase(),l);
         }
    }
    
	/**
	 * 
	 * URL
	 * http://userip:port/urlpattern?userid=(userid)&oa=(replynumber)&da=(receipientno)&dtime=(datetime)
		&msgtxt=(message)
 
	 *  *Following Input is Expected from Patient
	 *
	 *<GET> <KEYWORD> <EXTENSION> eg. GET REMINDER TOMORROW or GET REMINDER 4PM or GET REMINDER
	 *OR
	 *<userID> <DIGIT>     eg. PQE 1 or PQE yes
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * 
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.out=response.getWriter();
		PrintWriter out=response.getWriter();
		
		String message=request.getParameter("msgtxt");String pnumber=request.getParameter("da");String time=request.getParameter("dtime");
		String fromNumber=request.getParameter("oa");String userId=request.getParameter("userid");
		Date inDate=getTimeFromString(time);Date outDate=new Date();
		
		if(pnumber==null||pnumber=="" || pnumber==" "){
			out.println("Status=1");
			return;
		}
		
		MessageHandler mHandler=new MessageHandler(message);
		IMessage messageType=mHandler.getMessageFormat();
		
		if(!(messageType.getToContinue())){
			mHandler.sendSMS(outDate, pnumber,mHandler.getErrorMessage(messageType),defaultLanguage);
		}
		
		if(messageType==IMessage.GET)
			handleGet(mHandler,pnumber,message, inDate);
		
		else if(messageType==IMessage.ID)
			handleID(mHandler,pnumber,message, outDate);
		
		
		out.println("Status=0");
	}
	
	private void handleID(MessageHandler mHandler, String pnumber,String message,Date inDate) {
		String[] split=message.split(" ");String reply;
		String userID=split[0].toUpperCase();
		Options option=cache.getIfPresent(userID);
		if(option==null){
			mHandler.sendSMS(inDate,pnumber, mHandler.getErrorMessage(RMessage.INVALIDSESSION),defaultLanguage);
			return;
		}
		else if(!(pnumber.equals(option.getPhoneNumber()))){
			mHandler.sendSMS(inDate, pnumber, mHandler.getErrorMessage(RMessage.DIFFERNETSESSIONID),defaultLanguage);
			return;
		}
		
		reply=option.getReply(message, pnumber);
		
		if(reply==null && !(option.toDeleteSession()))
				mHandler.sendSMS(inDate, pnumber, mHandler.getErrorMessage(RMessage.INVALIDOPTION),option.getLanguage());
		
		else if(reply==null)
			mHandler.sendSMS(inDate, pnumber, mHandler.getErrorMessage(RMessage.NOTHINGTOREPLY),option.getLanguage());
		
		else
			mHandler.sendSMS(inDate, pnumber,reply,option.getLanguage());
		
		Options updatedOption=cache.getIfPresent(userID);
		
		if(updatedOption.toDeleteSession())
			cache.invalidate(userID);
		
					
		//Cache update itself automatically.		 
	}
	

	private void handleGet(MessageHandler mHandler, String pnumber,String message,Date inDate){
		Options option;String userID;String reply;
		String[] split=message.split(" ");
		String keyword=split[1].toUpperCase();
		
		if(!map.containsKey(keyword)){
			mHandler.sendSMS(inDate, pnumber, mHandler.getErrorMessage(RMessage.KEYWORDNOTFOUND),defaultLanguage);
			return;
		}
			
		Class classname=map.get(keyword);
		userID=getUniqueUserID();
		option=getOptionFromClass(pnumber,userID,classname);
		if(option==null)
			return;
		if(!verifyNumber(pnumber,option)){
			mHandler.sendSMS(inDate, pnumber, mHandler.getErrorMessage(RMessage.NUMBERNOTRECOGNISED),defaultLanguage);
			return;
		}
		
		putInCache(userID,option);
		reply=option.getReply(message, pnumber);
		
		if(reply==null && !(option.toDeleteSession())){
				mHandler.sendSMS(inDate, pnumber, mHandler.getErrorMessage(RMessage.INVALIDOPTION),option.getLanguage());
				return;
		}
		
		else if(reply==null){
			mHandler.sendSMS(inDate, pnumber, mHandler.getErrorMessage(RMessage.NOTHINGTOREPLY),option.getLanguage());
			return;
		}
		else
			mHandler.sendSMS(inDate, pnumber,reply,option.getLanguage());
		
		
		
	
	}
	/**
	 * Not implemented
	 * @param pnumber
	 * @param option
	 * @return
	 */
	
	private void putInCache(String userID,Options option){
		cache.put(userID, option);
		if(uncachedID.contains(userID))
			uncachedID.remove(userID);
	}
	/**
	 * DOUBT
	 * @param pnumber
	 * @param option
	 * @return
	 */
	private boolean verifyNumber(String pnumber, Options option) {
				
		return true;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);	
	}
	
	/**
	 * Returns a iterator of map of entrySet.
	 * @return Iterator<Map.Entry<String,Options> >
	 */
	public final static Iterator<Map.Entry<String,Class>> getMapIterator(){
		return map.entrySet().iterator();
	}
	/**
	 * It jumps or redirect from one menu option to other by changing field in cache.
	 * @param sessionID
	 * @param option
	 */
	public final static void redirect(Options me,Options redirect){
			cache.put(me.getUserID(), redirect);
	}
	
	
	private void putObjectInMap(){
		for(Keyword k:Keyword.values()){
			String keyword=k.getKeyword();
			Class classname=k.getOptionClass();
			if(classname!=null && keyword!=null){
				map.put(keyword,classname);
			}
			else
				printE("IMPORTANT:Keyword:"+keyword+" not supported as Class doesnot exist");
		}
	}
	
	private String getUniqueUserID(){
		
		if(cache.size()==maxSize)
			return null;
		String id;Options inCache;
		while(true){
			int random=randInt(1,alphasLength*alphasLength*alphasLength);
			if(random!=helloStringID){
				id=getString(random);
				if(!uncachedID.contains(id) && cache.getIfPresent(id)==null)
					break;
			}
		}
		out.println("ID Generated:"+id);
		uncachedID.add(id);
		return id;
	}
	
	private static String getString(int random){
		
		char thirdChar,secondChar,firstChar;int third,second,first;
		third=random%alphasLength;
		if(third==0)
			third=alphasLength;
		random=random-third;
		
		int quotient=random/alphasLength;
		second=(quotient%alphasLength)+1;
		first=((quotient-(second-1))/alphasLength)+1;
		
		thirdChar=alphas[third-1];secondChar=alphas[second-1];firstChar=alphas[first-1];
		
		char[] charId={firstChar,secondChar,thirdChar}; 
		String id=new String(charId);
		
		return id;
	}
	
	protected final static int randInt(int min, int max) {
		Random rand = new Random();
		int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}
	
	protected static void print(String s){
		logger.info(s);
	}
	protected static void printE(String s){
		logger.error(s);
	}
	
	protected final static Options getOptionFromClass(String pnumber,String userID,Class classObject){
		try{
		return (Options)classObject.getConstructor(String.class,String.class).newInstance(userID,pnumber);
		}
		catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
	}
	
	private Date getTimeFromString(String time){
		Date temp;
		try{
			temp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
		}
		catch(Exception ex){
			temp=null;
		}
		return temp;
	}
	
}
