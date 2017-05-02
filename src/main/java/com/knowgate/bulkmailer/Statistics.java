package com.knowgate.bulkmailer;

/**
 * © Copyright 2016 the original author.
 * This file is licensed under the Apache License version 2.0.
 * You may not use this file except in compliance with the license.
 * You may obtain a copy of the License at:
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.
 */

import java.io.PrintStream;
import java.util.Map;
import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.SortedMap;

import javax.jdo.JDOException;
import javax.jdo.datastore.JDOConnection;

import com.knowgate.debug.StackTraceUtil;

import org.judal.storage.table.TableDataSource;

public abstract class Statistics {

  public static final String STATUS_FINISHED = "0";
  public static final String STATUS_RUNNING = "3";
	
  private TableDataSource oDts;
  private PrintStream oVerbose;
  
  public Statistics(TableDataSource oTableDts, PrintStream oPrntStrm) {
    oDts = oTableDts;
    oVerbose = oPrntStrm;
  }

  public Map<String,Integer> groupClicksByUrl(ClickThrough[] aClcks) {
  	HashMap<String,Integer> mGrp = new HashMap<String,Integer>();
  	if (aClcks!=null) {
    	final int nClcks = aClcks.length;
    	for (int c=0; c<nClcks; c++) {
    		String urlId = aClcks[c].urlId();
    		if (mGrp.containsKey(urlId))
    			mGrp.put(urlId, mGrp.get(urlId)+1);
    		else
    			mGrp.put(urlId, 1);
    	}
  	}
  	return mGrp;
  }

  public Map<String,Integer> groupClicksByEmail(ClickThrough[] aClcks) {
  	HashMap<String,Integer> mGrp = new HashMap<String,Integer>();
  	if (aClcks!=null) {
    	final int nClcks = aClcks.length;
    	for (int c=0; c<nClcks; c++) {
    		String email = aClcks[c].email();
    		if (mGrp.containsKey(email))
    			mGrp.put(email, mGrp.get(email)+1);
    		else
    			mGrp.put(email, 1);
    	}
  	}
  	return mGrp;
  }

  public abstract List<String> listGroups(JDOConnection oConn) throws JDOException;

  public abstract int countClicksForJob(JDOConnection oConn, String sGuJob) throws JDOException;

  public abstract List<EmailMessagesByHour> countMessagesByHourForJob(JDOConnection oConn, String sGuJob) throws JDOException;
  
  public abstract void updateMessagesPerUserAgentForJob(JDOConnection oConn, String sGuJob, String sGuJobGroup, String sGuWorkArea, Map<String,Integer> mMsgByUserAgent) throws JDOException;

  public abstract void updateOpenedMessagesCounterForJob(JDOConnection oConn, String sGuJob, int nSent) throws JDOException;
  
  public abstract void updateUniqueRecipientsCounterForJob(JDOConnection oConn, String sGuJob, int nUnique) throws JDOException;

  public abstract void updateClicksCounterForJob(JDOConnection oConn, String sGuJob, int nClicks) throws JDOException;

  public abstract void updateSentMessagesByHourForJob(JDOConnection oConn, String sGuJob, String sGuJobGroup, String sGuWorkArea, List<EmailMessagesByHour> aMsgsByHour) throws JDOException;

  public abstract void updateSentMessagesByDayForJob(JDOConnection oConn, String sGuJob, String sGuJobGroup, String sGuWorkArea, List<EmailMessagesByDay> aMsgsByDay) throws JDOException;

  public abstract void updateSentMessagesCounterForJob(JDOConnection oConn, String sGuJob, int nSent) throws JDOException;

  public abstract Map<String,String> getWorkareasForJobs(JDOConnection oConn, String sGuJobGroup) throws JDOException;
  
  public abstract List<String> getJobsFromGroup(JDOConnection oConn, String sGuJobGroup) throws JDOException;

  public abstract int blackListed(JDOConnection oConn, String sWorkArea) throws JDOException;

  public abstract int distinctRecipients(JDOConnection oConn, String sWorkArea, Date dtFrom, Date dtTo) throws JDOException;

  public abstract SortedMap<Date,Integer> messagesSentByDay(JDOConnection oConn, String sWorkArea, Date dtFrom, Date dtTo) throws JDOException;

  public abstract SortedMap<Short,Integer> messagesReadedByHour(JDOConnection oConn, String sWorkArea, Date dtFrom, Date dtTo) throws JDOException;

  public abstract int countSentMessagesForJob(JDOConnection oConn, String sGuJob) throws JDOException;
  
  public abstract int countOpenedMessagesForJob(JDOConnection oConn, String sGuJob) throws JDOException;

  public abstract int countUniqueRecipientsForJob(JDOConnection oConn, String sGuJob) throws JDOException;

  public abstract List<EmailMessagesByDay> countMessagesByDayForJob(JDOConnection oConn, String sGuJob) throws JDOException;

  public abstract HashMap<String,Integer> countMessagesPerUserAgentForJob(JDOConnection oConn, String sGuJob) throws JDOException;
  
  private void verbose(String sMsg) {
    if (oVerbose!=null)	oVerbose.println(sMsg);
  }
  
  public boolean collect(String sGuJobGroup) {

	  boolean bRetVal = true;
  	  JDOConnection oConn = null;
	  String sJobs = "";
	  List<String> aJobs;
	  Map<String,String> mWrks;
	  verbose ("Begin statistics collection for job group "+sGuJobGroup);

	  try {

    oConn = oDts.getJdoConnection();
	  	  
	    // ****************************************
	    // Get the list of jobs from the job group
	  
		mWrks = getWorkareasForJobs(oConn, sGuJobGroup);
	    aJobs = getJobsFromGroup(oConn, sGuJobGroup);
	    sJobs = "('" + String.join("','", aJobs) + "')";
	  
	    // ****************************************
	  
	    if (sJobs.length()>0) {

	      verbose ("Job list is "+sJobs);
	  	
	      // ****************************************************
	      for (String sGuJob : aJobs) {
	    	  updateSentMessagesCounterForJob(oConn, sGuJob, countSentMessagesForJob(oConn, sGuJob));  
	    	  updateOpenedMessagesCounterForJob(oConn, sGuJob, countOpenedMessagesForJob(oConn, sGuJob));
	    	  updateUniqueRecipientsCounterForJob(oConn, sGuJob, countUniqueRecipientsForJob(oConn, sGuJob));
	    	  updateClicksCounterForJob(oConn, sGuJob, countClicksForJob(oConn, sGuJob));
	        updateSentMessagesByDayForJob(oConn, sGuJob, sGuJobGroup, mWrks.get(sGuJob), countMessagesByDayForJob(oConn, sGuJob));
	        updateSentMessagesByHourForJob(oConn, sGuJob, sGuJobGroup, mWrks.get(sGuJob), countMessagesByHourForJob(oConn, sGuJob));
	        updateMessagesPerUserAgentForJob(oConn, sGuJob, sGuJobGroup, mWrks.get(sGuJob), countMessagesPerUserAgentForJob(oConn, sGuJob));
	      }
	    }
	    
	    oConn.close();
	    oConn = null;
	    
	  } catch (Exception xcpt) {
	    bRetVal = false;
	    verbose (xcpt.getClass().getName()+" "+xcpt.getMessage());
	    try { verbose (StackTraceUtil.getStackTrace(xcpt));
	    } catch (java.io.IOException ignore) {}
	  } finally {
	    verbose ("Closing conection");
	    try { if (oConn!=null) oConn.close(); } catch (Exception ignore) {}
	  }
	  verbose ("Done!");
	  return bRetVal;
  } // collect

  public void collect()  {
  	JDOConnection oConn = null;
	  try {
		oConn = oDts.getJdoConnection();
		List<String> aGroups = listGroups(oConn);
		oConn.close();
		oConn = null;
	    int n = 0;
	    for (String g : aGroups) {
	  	  verbose ("Procesing group "+String.valueOf(++n)+" of "+String.valueOf(aGroups.size()));
	  	  collect(g);
	    }
	  } catch (Exception xcpt) {
	    verbose (xcpt.getClass().getName()+" "+xcpt.getMessage());
	    try { verbose (StackTraceUtil.getStackTrace(xcpt));
	    } catch (java.io.IOException ignore) {}
	  } finally {
	    // verbose ("Closing conection");
	    try { if (oConn!=null) oConn.close(); } catch (Exception ignore) { }
	  }
	} // collect
  
}
