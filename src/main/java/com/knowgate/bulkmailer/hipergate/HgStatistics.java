package com.knowgate.bulkmailer.hipergate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;

import javax.jdo.JDOException;
import javax.jdo.datastore.JDOConnection;

import com.knowgate.bulkmailer.EmailMessagesByDay;
import com.knowgate.bulkmailer.EmailMessagesByHour;
import com.knowgate.bulkmailer.Statistics;
import com.knowgate.bulkmailer.UserAgents;

import org.judal.jdbc.jdc.JDCConnection;
import org.judal.storage.table.TableDataSource;
import org.judal.jdbc.RDBMS;

public class HgStatistics extends Statistics {

  public HgStatistics(TableDataSource oTableDts, PrintStream oPrntStrm) {
  	super(oTableDts, oPrntStrm);
  }

  public int countSentMessagesForJob(Connection oConn, String sGuJob) throws SQLException {
  	PreparedStatement oStmt = oConn.prepareStatement("SELECT COUNT(*) FROM k_job_atoms_archived WHERE id_status IN ("+STATUS_FINISHED+","+STATUS_RUNNING+") AND gu_job=?");
  	oStmt.setString(1, sGuJob);
  	ResultSet oRSet = oStmt.executeQuery();
  	int nSent = oRSet.getInt(1);
  	oRSet.close();
  	oStmt.close();
  	return nSent;
  }

  public int countOpenedMessagesForJob(Connection oConn, String sGuJob) throws SQLException {
  	PreparedStatement oStmt = oConn.prepareStatement("SELECT COUNT(*) FROM k_job_atoms_tracking WHERE gu_job=?");
  	oStmt.setString(1, sGuJob);
  	ResultSet oRSet = oStmt.executeQuery();
  	int nOpened = oRSet.getInt(1);
  	oRSet.close();
  	oStmt.close();
  	return nOpened;
  }

  public int countUniqueRecipientsForJob(Connection oConn, String sGuJob) throws SQLException {
  	PreparedStatement oStmt = oConn.prepareStatement("SELECT COUNT(DISTINCT(tx_email)) FROM k_job_atoms_archived WHERE id_status IN ("+STATUS_FINISHED+","+STATUS_RUNNING+") AND gu_job=?");
  	oStmt.setString(1, sGuJob);
  	ResultSet oRSet = oStmt.executeQuery();
  	int nOpened = oRSet.getInt(1);
  	oRSet.close();
  	oStmt.close();
  	return nOpened;
  }

  public int countClicksForJob(Connection oConn, String sGuJob) throws SQLException {
  	PreparedStatement oStmt = oConn.prepareStatement("SELECT COUNT(*) FROM k_job_atoms_clicks WHERE gu_job=?");
  	oStmt.setString(1, sGuJob);
  	ResultSet oRSet = oStmt.executeQuery();
  	int nClicks = oRSet.getInt(1);
  	oRSet.close();
  	oStmt.close();
  	return nClicks;
  }

  public List<EmailMessagesByDay> countMessagesByDayForJob(Connection oConn, String sGuJob) throws SQLException {
	  Timestamp tsNow = new Timestamp(new Date().getTime());
  	ArrayList<EmailMessagesByDay> aMsgsByDay = new ArrayList<EmailMessagesByDay>();
		PreparedStatement oStmt;
		ResultSet oRSet;
		switch (JDCConnection.getDataBaseProduct(oConn)) {
		  case 1: // MYSQL
			  	oStmt = oConn.prepareStatement("SELECT COUNT(*) AS nu_messages,DATEDIFF(NOW(),dt_execution) FROM k_job_atoms_archived WHERE id_status IN ("+STATUS_FINISHED+","+STATUS_RUNNING+") AND gu_job=? GROUP BY 2");
					oStmt.setString(1, sGuJob);
					oRSet = oStmt.executeQuery();
					while (oRSet.next())
						aMsgsByDay.add(new EmailMessagesByDay(oRSet.getInt(1), tsNow.getTime()-((long)oRSet.getInt(2))*86400000l));
			  	break;
		  case 2: // POSTGRESQL
		  	oStmt = oConn.prepareStatement("SELECT COUNT(*) AS nu_messages,justify_days(age(current_timestamp,date_trunc('day',dt_execution))) FROM k_job_atoms_archived WHERE id_status IN ("+STATUS_FINISHED+","+STATUS_RUNNING+") AND gu_job=? GROUP BY 2");
				oStmt.setString(1, sGuJob);
				oRSet = oStmt.executeQuery();
				while (oRSet.next())
					aMsgsByDay.add(new EmailMessagesByDay(oRSet.getInt(1), tsNow.getTime()-((getIntervalMilis(oRSet.getObject(2))/86400000l)*86400000l)));
		    break;
		  case 3: // MSSQL
		  	oStmt = oConn.prepareStatement("SELECT COUNT(*) AS nu_messages,DATEDIFF(day,dt_execution,GETDATE()) FROM k_job_atoms_archived WHERE id_status IN ("+STATUS_FINISHED+","+STATUS_RUNNING+") AND gu_job=? GROUP BY 2");
				oStmt.setString(1, sGuJob);
				oRSet = oStmt.executeQuery();
				while (oRSet.next())
					aMsgsByDay.add(new EmailMessagesByDay(oRSet.getInt(1), tsNow.getTime()-((long)oRSet.getInt(2))*86400000l));
		  	break; 
		  case 5:// ORACLE
		  	oStmt = oConn.prepareStatement("SELECT COUNT(*) AS nu_messages,TRUNC(SYSDATE-dt_execution) FROM k_job_atoms_archived WHERE id_status IN ("+STATUS_FINISHED+","+STATUS_RUNNING+") AND gu_job=? GROUP BY 2");
				oStmt.setString(1, sGuJob);
				oRSet = oStmt.executeQuery();
				while (oRSet.next())
					aMsgsByDay.add(new EmailMessagesByDay(oRSet.getInt(1), tsNow.getTime()-(oRSet.getBigDecimal(2).longValue()*86400000l)));
		  	break;
		  default:
		  	throw new SQLException ("Unsupported RDBMS");
		}
		oRSet.close();
		oStmt.close();
		return aMsgsByDay;
  }

  public List<EmailMessagesByHour> countMessagesByHourForJob(Connection oConn, String sGuJob) throws SQLException {
  	ArrayList<EmailMessagesByHour> aMsgsByHour = new ArrayList<EmailMessagesByHour>();
		PreparedStatement oStmt;
		ResultSet oRSet;
		switch (JDCConnection.getDataBaseProduct(oConn)) {
		  case 1: // MYSQL
			  	oStmt = oConn.prepareStatement("SELECT COUNT(*),EXTRACT(HOUR FROM dt_action) FROM k_job_atoms_tracking WHERE DB.gu_job=? GROUP BY 2");
					oStmt.setString(1, sGuJob);
			    oRSet = oStmt.executeQuery();
					while (oRSet.next())
						aMsgsByHour.add(new EmailMessagesByHour(oRSet.getInt(1), (short) oRSet.getDouble(2)));
			    break;
		  case 2: // POSTGRESQL
	      oStmt = oConn.prepareStatement("SELECT COUNT(*),date_part('hour',dt_action) FROM k_job_atoms_tracking WHERE gu_job=? GROUP BY 2");
	  		oStmt.setString(1, sGuJob);
	      oRSet = oStmt.executeQuery();
	  		while (oRSet.next())
	  			aMsgsByHour.add(new EmailMessagesByHour(oRSet.getInt(1), (short) oRSet.getDouble(2)));
		    break;
		  case 3: // MSSQL
		  	oStmt = oConn.prepareStatement("SELECT COUNT(*),DATEPART(hour,dt_action) FROM k_job_atoms_tracking WHERE DB.gu_job=? GROUP BY 2");
				oStmt.setString(1, sGuJob);
		    oRSet = oStmt.executeQuery();
				while (oRSet.next())
					aMsgsByHour.add(new EmailMessagesByHour(oRSet.getInt(1), (short) oRSet.getDouble(2)));
		    break;
		  case 5: // ORACLE
		  	oStmt = oConn.prepareStatement("SELECT COUNT(*),DATEPART(hour,dt_action) FROM k_job_atoms_tracking WHERE DB.gu_job=? GROUP BY 2");
				oStmt.setString(1, sGuJob);
		    oRSet = oStmt.executeQuery();
				while (oRSet.next())
					aMsgsByHour.add(new EmailMessagesByHour(oRSet.getInt(1), oRSet.getBigDecimal(2).shortValue()));
		    break;
		  default:
		  	throw new SQLException ("Unsupported RDBMS");
		}
    oRSet.close();
    oStmt.close();
    return aMsgsByHour;
  }

  public HashMap<String,Integer> countMessagesPerUserAgentForJob(Connection oConn, String sGuJob) throws SQLException {
  	HashMap<String,Integer> mMsgsPerAgent = new HashMap<String,Integer>();
  	for (int a=UserAgents.list().length-1; a>=0; a--) mMsgsPerAgent.put(UserAgents.getName(a), new Integer(0));
    PreparedStatement oStmt = oConn.prepareStatement("SELECT user_agent FROM k_job_atoms_tracking WHERE gu_job=?");
  	oStmt.setString(1, sGuJob);
    ResultSet oRSet = oStmt.executeQuery();
    while (oRSet.next()) {
    	String sName = UserAgents.identify(oRSet.getString(1));
    	mMsgsPerAgent.put(sName, new Integer(mMsgsPerAgent.get(mMsgsPerAgent).intValue()+1));
    }
    oRSet.close();
    oStmt.close();
    return mMsgsPerAgent;
  }

  private long getIntervalMilis (Object obj)
      throws ArrayIndexOutOfBoundsException,ClassCastException {
     
      if (null==obj)
        return 0l;
      else if (obj.getClass().getName().equals("org.postgresql.util.PGInterval")) {
        final float SecMilis = 1000f;
        final long MinMilis = 60000l, HourMilis=3600000l, DayMilis=86400000l;
        long lInterval = 0;
        String[] aParts = obj.toString().trim().split("\\s");
  	  for (int p=0; p<aParts.length-1; p+=2) {
  	  	Float fPart = new Float(aParts[p]);
  	  	if (fPart.floatValue()!=0f) {
  	  	  if (aParts[p+1].startsWith("year"))
  	  	  	lInterval += fPart.longValue()*DayMilis*365l;
  	  	  else if (aParts[p+1].startsWith("mon"))
  	  	  	lInterval += fPart.longValue()*DayMilis*30l;
  	  	  else if (aParts[p+1].startsWith("day"))
  	  	  	lInterval += fPart.longValue()*DayMilis;
  	  	  else if (aParts[p+1].startsWith("hour"))
  	  	  	lInterval += fPart.longValue()*HourMilis;
  	  	  else if (aParts[p+1].startsWith("min"))
  	  	  	lInterval += fPart.longValue()*MinMilis;
  	  	  else if (aParts[p+1].startsWith("sec"))
  	  	  	lInterval += new Float(fPart.floatValue()*SecMilis).longValue();	  	  	
  	  	}
  	  }
        return lInterval;
      }
      else
        throw new ClassCastException("Cannot cast "+obj.getClass().getName()+" to Timestamp");
  } // getIntervalMilis
  
  
@Override
public List<String> listGroups(JDOConnection oConn) throws JDOException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public int countClicksForJob(JDOConnection oConn, String sGuJob) throws JDOException {
	// TODO Auto-generated method stub
	return 0;
}

@Override
public List<EmailMessagesByHour> countMessagesByHourForJob(JDOConnection oConn, String sGuJob) throws JDOException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public void updateMessagesPerUserAgentForJob(JDOConnection oConn, String sGuJob, String sGuJobGroup, String sGuWorkArea,
		Map<String, Integer> mMsgByUserAgent) throws JDOException {
	// TODO Auto-generated method stub
	
}

@Override
public void updateOpenedMessagesCounterForJob(JDOConnection oConn, String sGuJob, int nSent) throws JDOException {
	// TODO Auto-generated method stub
	
}

@Override
public void updateUniqueRecipientsCounterForJob(JDOConnection oConn, String sGuJob, int nUnique) throws JDOException {
	// TODO Auto-generated method stub
	
}

@Override
public void updateClicksCounterForJob(JDOConnection oConn, String sGuJob, int nClicks) throws JDOException {
	// TODO Auto-generated method stub
	
}

@Override
public void updateSentMessagesByHourForJob(JDOConnection oConn, String sGuJob, String sGuJobGroup, String sGuWorkArea,
		List<EmailMessagesByHour> aMsgsByHour) throws JDOException {
	// TODO Auto-generated method stub
	
}

@Override
public void updateSentMessagesByDayForJob(JDOConnection oConn, String sGuJob, String sGuJobGroup, String sGuWorkArea,
		List<EmailMessagesByDay> aMsgsByDay) throws JDOException {
	// TODO Auto-generated method stub
	
}

@Override
public void updateSentMessagesCounterForJob(JDOConnection oConn, String sGuJob, int nSent) throws JDOException {
	// TODO Auto-generated method stub
	
}

@Override
public Map<String, String> getWorkareasForJobs(JDOConnection oConn, String sGuJobGroup) throws JDOException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public List<String> getJobsFromGroup(JDOConnection oConn, String sGuJobGroup) throws JDOException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public int blackListed(JDOConnection oConn, String sWorkArea) throws JDOException {
	// TODO Auto-generated method stub
	return 0;
}

@Override
public int distinctRecipients(JDOConnection oConn, String sWorkArea, Date dtFrom, Date dtTo) throws JDOException {
	// TODO Auto-generated method stub
	return 0;
}

@Override
public SortedMap<Date, Integer> messagesSentByDay(JDOConnection oConn, String sWorkArea, Date dtFrom, Date dtTo)
		throws JDOException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public SortedMap<Short, Integer> messagesReadedByHour(JDOConnection oConn, String sWorkArea, Date dtFrom, Date dtTo)
		throws JDOException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public int countSentMessagesForJob(JDOConnection oConn, String sGuJob) throws JDOException {
	// TODO Auto-generated method stub
	return 0;
}

@Override
public int countOpenedMessagesForJob(JDOConnection oConn, String sGuJob) throws JDOException {
	// TODO Auto-generated method stub
	return 0;
}

@Override
public int countUniqueRecipientsForJob(JDOConnection oConn, String sGuJob) throws JDOException {
	// TODO Auto-generated method stub
	return 0;
}

@Override
public List<EmailMessagesByDay> countMessagesByDayForJob(JDOConnection oConn, String sGuJob) throws JDOException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public HashMap<String, Integer> countMessagesPerUserAgentForJob(JDOConnection oConn, String sGuJob)
		throws JDOException {
	// TODO Auto-generated method stub
	return null;
}


/*
public static void main(String[] args) throws FileNotFoundException, IOException {
	  String sProfile = "hipergate";
	  if (args!=null) {
	  	if (args.length>0) sProfile = args[0];
	  }
	  Properties oProps = new Properties();
	  oProps.load(new FileInputStream(sProfile));
	  HashMap<>
	  Statistics oStats = new HgStatistics(oProps, System.out);
	  if (args==null) {
      oStats.collect();
	  } else {
	  	if (args.length>1)
	  	  oStats.collect(args[1]);
	    else
	      oStats.collect();
	  }
} // main	
*/

}
