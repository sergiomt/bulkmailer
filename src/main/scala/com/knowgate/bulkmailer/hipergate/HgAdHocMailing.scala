package com.knowgate.bulkmailer.hipergate

import java.util.Date

import javax.jdo.JDOException

import com.knowgate.bulkmailer.Job
import com.knowgate.bulkmailer.Using._
import com.knowgate.bulkmailer.Mailing
import com.knowgate.bulkmailer.MailingList
import com.knowgate.bulkmailer.ClickThrough
import com.knowgate.bulkmailer.RecipientsList

import org.judal.storage.table.Table
import org.judal.storage.table.TableDataSource
import org.judal.storage.table.Record
import org.judal.storage.table.RecordSet
import org.judal.storage.scala.ArrayRecord

import scala.collection.mutable.HashMap
import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer

import scala.collection.JavaConverters._
import scala.collection.JavaConversions.asScalaBuffer

class HgAdHocMailing(dts: TableDataSource) extends ArrayRecord(dts,"k_adhoc_mailings") with Mailing with HgSumCounter {

   val recplist = new RecipientsList
   
	 def getId() : String = getString("gu_mailing")

	 def setId(id: String) = { put("gu_mailing", id) }

   @throws(classOf[JDOException])
   def lastExecutionDate() : Date = {
     var led: Date = null
     var rec: Record = null
     if (isNull("dt_execution")) {
       val job = new HgJob(dts, new HashMap[String,String])
       var tbl = dts.openTable(job)
       using(tbl) {
         val jobs : RecordSet[HgJob] = tbl.fetch(job.fetchGroup(), "gu_job_group", getId())
         val iter = jobs.iterator
         while (iter.hasNext)
           rec = iter.next
           if (!rec.isNull("dt_execution"))
             if (null==led)
               led = rec.getDate("dt_execution")
             else if (rec.getDate("dt_execution").compareTo(led)>0)
               led = rec.getDate("dt_execution")
       } 
       if (null!=led) setLastExecutionDate(led)       
     } else {
       led = getDate("dt_execution")
     }
     led
   }

   def setLastExecutionDate(dt: Date) = { put("dt_execution", dt) }

   def getNumber() : Int = getInt("pg_mailing")

   def setNumber(n: Int) = { put ("pg_mailing", new Integer(n)) }
   
   def getName() = getString("nm_mailing","")
   
   def setName(n: String) = { put ("nm_mailing", n) }
   
   def getSubject() = getString("tx_subject","")
   
   def setSubject(s: String ) = { put ("tx_subject", s) }
   
   def getFromAddress() = getString("tx_email_from","")
   
   def setFromAddress(f: String) = { put ("tx_email_from", f) } 

   def getReplyTo() = getString("tx_email_reply")
   
   def setReplyTo(r: String) = { put ("tx_email_reply", r) } 
   
   def getDisplayName() = getString("nm_from")
   
   def setDisplayName(n: String) = { put ("nm_from", n) }

   def getAllowPattern() = getString("tx_allow_regexp","")
   
   def setAllowPattern(regexp: String) = {
     put ("tx_allow_regexp", regexp)
     recplist.setAllowPattern(regexp)
   }
   
   def getDenyPattern() = getString("tx_deny_regexp","")
   
   def setDenyPattern(regexp: String) = {
     put ("tx_deny_regexp", regexp)
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
     var jbs : Array[Job] = null
     using(tbl) {
       val job = new HgJob(dts, new HashMap[String,String])
       tbl = dts.openTable(job)
       val jobRecs : RecordSet[HgJob] = tbl.fetch(job.fetchGroup, "gu_job_group", getId)       
       jbs = new Array[Job](jobRecs.size)
       var j = 0
       val iter = jobRecs.iterator
       while (iter.hasNext)
         jbs(j) = new HgJob(dts, prp, iter.next.asInstanceOf[Record])
     }
     rst.toArray
   }
   
   @throws(classOf[JDOException])
   def lists() : Array[MailingList] = {
     var lsts = new Array[MailingList](0)
     val mailingList = new HgAdHocMailingList(dts)
     var tbl : Table = dts.openTable(mailingList)
     using(tbl) {
       val rst = tbl.fetch(mailingList.fetchGroup, "gu_mailing", getId())
       if (rst.size>0) {
         lsts = new Array[MailingList](rst.size)
         Range(0, rst.size()).foreach { r => lsts(r) = new HgMailingList(dts,rst.get(r)) }
       }
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