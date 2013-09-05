

package org.raxa.module.handlesms;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.raxa.alertmessage.MedicineInformation;
import org.raxa.alertmessage.MessageTemplate;
import org.raxa.database.Alert;
import org.raxa.database.HibernateUtil;
import org.raxa.database.IvrMsg;
import org.raxa.database.Patient;
import org.raxa.database.SmsMsg;
import org.raxa.database.VariableSetter;
import org.raxa.registration.Register;



public class DatabaseService implements VariableSetter {
		

		private static Logger logger = Logger.getLogger(DatabaseService.class);
		
		private String pnumber;
		private String pid;
		private boolean isErrorWhileRetreiving=false;
	
		public DatabaseService(String pnumber){
			this.pnumber=pnumber;
		}
		
		public DatabaseService(){}
		 
		/**
		 * Return all patient medicine info,whose id is pid.and duration between startTime
		 * 
		 *The two query used can be merged into one..
		 * @param pid
		 * @return
		 */
		protected List<MedicineInformation> getMedicineInfo(String pid,Date today,Date startTime,Date endTime,int alertType){
			String hql;Date time1,time2;time1=null;time2=null;
			List<MedicineInformation> list=new ArrayList<MedicineInformation>();
			Session session=HibernateUtil.getSessionFactory().openSession();
			try{
			if(startTime==null && endTime==null && today==null)
				return null;
			else if(startTime==null && endTime==null){
				today.setHours(0);
				today.setMinutes(1);
				time1=today;
			}
			else if(startTime==null && !(endTime==null)){
				return null;
			}
			else if(endTime==null && startTime!=null){
				time1=startTime;
			}
			else{
				time1=startTime;time2=endTime;
			}
			session.beginTransaction();
			if(time2==null)
				hql="from Alert where scheduleTime>=:time1 and pid=:pid and alertType=:alertType order by scheduleTime";
			else
				hql="from Alert where scheduleTime>=:time1 and scheduleTime<=:time2 and pid=:pid and alertType=:alertType order by scheduleTime";
			Query query=session.createQuery(hql);
			query.setString("pid", pid);
			query.setTimestamp("time1",time1);
			query.setInteger("alertType",alertType);
			if(time2!=null)
				query.setTimestamp("time2",time2);
			
			String hql2=null;
			if(alertType==IVR_TYPE)
				hql2="from IvrMsg where ivrId=:msgId order by itemNumber";
			else if(alertType==SMS_TYPE)
				hql2="from SmsMsg where smsId=:msgId order by itemNumber";
			
			List<Alert> content=(List<Alert>)query.list();
			for(Alert alert: content){
				List<String> itemContents=new ArrayList<String>();
	    		Query query2=session.createQuery(hql2);
	    		query2.setInteger("msgId",alert.getMessageId());
	    		List<SmsMsg> results=(List<SmsMsg>)query2.list();	
	    		for(SmsMsg s:results){
	    			itemContents.add(s.getContent());
	    		}
	    			MedicineInformation m=new MessageTemplate().getMedicineInformation(itemContents);
	    			m.setScheduleTime(alert.getScheduleTime());
	    			list.add(m);
	    	}
			
			
			}
			catch(Exception ex){
				ex.printStackTrace();
				logger.info("Some error occured while in incoming-call and fetching information about patient with id "+pid);
				list=null;
				isErrorWhileRetreiving=true;
			}
			session.getTransaction().commit();
			session.close();
			return list;
		}
		
		protected String getReminder(String pid,Date today,Date startTime,Date endTime,int alertType){
			List<MedicineInformation> info=getMedicineInfo(pid, today, startTime, endTime, alertType);
			if(info==null && isErrorWhileRetreiving)
				return RMessage.NOTHINGTOREPLY.getMessage();
			else
				return new MessageTemplate().getTextToconvertToSMSReminder(info);
		}
		
		  protected static List<Patient> getAllRegisteredPatientWithNumber(String pnumber){
		    	List<Patient> nameAndId=new ArrayList<Patient>();
		    	try{
			    	Session session = HibernateUtil.getSessionFactory().openSession();
			    	session.beginTransaction();
			    	String hql="from Patient where (pnumber=:pnumber or snumber=:pnumber) and pid in (select pid from PAlert where alertType=:alertType)";
			    	Query query=session.createQuery(hql);
			    	query.setString("pnumber", pnumber);
			    	query.setInteger("alertType", SMS_TYPE);
			    	List patientList=query.list();
			    	if(patientList==null || patientList.size()<1)
			    		return null;
			    	for(int i=0;i<patientList.size();i++){
			    		Patient p=(Patient)patientList.get(i);
			    		nameAndId.add(p);
			    	}
			    	
		    	}
		    	catch(Exception ex){
		    		logger.error("unable to retrieve data for patient with phone number:"+pnumber);
		    		ex.printStackTrace();
		    		nameAndId=null;
		    	}
		    	return nameAndId;
		    }
		  
		  
		  protected  List<Patient> getAllUnRegisteredPatientWithNumber(String pnumber,String language){
			   return new Register().getpatientFromNumber(pnumber,language);
		  }
		  
		 
		  
		  
		  
		  
		  
		  

}
