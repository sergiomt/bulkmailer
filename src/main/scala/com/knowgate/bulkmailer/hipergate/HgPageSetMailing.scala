package com.knowgate.bulkmailer.hipergate

import java.util.Date

import javax.jdo.JDOException

import com.knowgate.bulkmailer.Job
import com.knowgate.bulkmailer.Mailing
import com.knowgate.bulkmailer.MailingList
import com.knowgate.bulkmailer.ClickThrough
import com.knowgate.bulkmailer.RecipientsList

import org.judal.storage.Table
import org.judal.storage.Record
import org.judal.storage.RecordSet
import org.judal.storage.TableDataSource
import org.judal.storage.scala.ArrayRecord

import scala.collection.mutable.HashMap
import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions.asScalaBuffer

import com.knowgate.bulkmailer.Using._

class HgPageSetMailing(dts: TableDataSource) extends ArrayRecord(dts,"k_pagesets") with Mailing with HgSumCounter {

   val recplist = new RecipientsList
   
	 def getId() : String = getString("gu_pageset")

	 def setId(id: String) = { put("gu_pageset", id) }
	 
   def getNumber() : Int = {
     throw new UnsupportedOperationException("Method getNumber() is not implemented by HgPageSetMailing")
   }

   def setNumber(n: Int) = {
     throw new UnsupportedOperationException("Method setNumber(Int) is not implemented by HgPageSetMailing")     
   }
   
   def getName() = getString("nm_pageset","")
   
   def setName(n: String) = { put ("nm_pageset", n) }
   
   @throws(classOf[JDOException])
   def lastExecutionDate() : Date = {
     var tbl: Table = null
     var led: Date = null
     using(tbl) {
       val job = new ArrayRecord(dts, "k_jobs")
       tbl = dts.openTable(job)
       val jobs : RecordSet[Record] = tbl.fetch(job.fetchGroup(), "gu_job_group", getId())
       for (rec <- asScalaBuffer(jobs))
         if (!rec.isNull("dt_execution"))
           if (null==led)
             led = rec.getDate("dt_execution")
           else if (rec.getDate("dt_execution").compareTo(led)>0)
             led = rec.getDate("dt_execution")
       tbl.close
       tbl=null
     }
     led
   }

   def getSubject() = getString("tx_subject","")
   
   def setSubject(s: String ) = { put ("tx_subject", s) }
   
   def getFromAddress() = getString("tx_email_from","")
   
   def setFromAddress(f: String) = { put ("tx_email_from", f) } 

   def getReplyTo() = getString("tx_email_reply")
   
   def setReplyTo(r: String) = { put ("tx_email_reply", r) } 
   
   def getDisplayName() = getString("nm_from")
   
   def setDisplayName(n: String) = { put ("nm_from", n) }

   def getAllowPattern() = recplist.getAllowPattern()
   
   def setAllowPattern(regexp: String) = {
     recplist.setAllowPattern(regexp)
   }
   
   def getDenyPattern() = recplist.getDenyPattern()
   
   def setDenyPattern(regexp: String) = {
     recplist.setDenyPattern(regexp)
   }
 
   def recipientsList() = recplist

   override def sentCount() : Int = {
     var n = super.sentCount
     if (-1==n) sumCounters(dts)
     super.sentCount
   }

   override def openedCount() : Int = {
     var n = super.sentCount
     if (-1==n) sumCounters(dts)
     super.openedCount
   }
  
   override def clicksCount() : Int = {
     var n = super.clicksCount
     if (-1==n) sumCounters(dts)
     super.clicksCount
   }
  
   override def uniqueCount() : Int = {
     var n = super.uniqueCount
     if (-1==n) sumCounters(dts)
     super.uniqueCount
   }
   
   @throws(classOf[JDOException])
   def jobs() : Array[Job] = {
     var tbl: Table = null
     var rst: Buffer[Job] = null
     val now = new Date()
     val prp = new HashMap[String,String]
     using(tbl) {
       val job = new ArrayRecord(dts, "k_jobs")
       tbl = dts.openTable(job)
       val jobs : RecordSet[Record] = tbl.fetch(job.fetchGroup(), "gu_job_group", getId)
       rst = asScalaBuffer(jobs).map(j => new HgJob(dts, prp, j.asInstanceOf[Record]))
       tbl.close()
       tbl = null
     }
     rst.toArray
   }

   @throws(classOf[JDOException])
   def lists() : Array[MailingList] = {
     var tbl : Table = null
     var lsts = new Array[MailingList](0)
     try {
       val pagesetList = new ArrayRecord(dts, "k_x_pageset_list")
       tbl = dts.openTable(pagesetList)
       val rst = tbl.fetch(pagesetList.fetchGroup(), "gu_pageset", getId())
       if (rst.size>0) {
         lsts = new Array[MailingList](rst.size)
         for (r <- 0 until rst.size()) {
           lsts(0) = new HgMailingList(dts, rst.get(r))
         }
       }
     } finally {
       if (tbl!=null) tbl.close()
     }
     lsts
   }
   
   @throws(classOf[JDOException])
   def clickThrough() : Array[ClickThrough] = {
     val lst = new ListBuffer[Array[ClickThrough]]
     for (j <- jobs())
       lst += j.clickThrough()       
     mergeClicks(lst)
   }

}