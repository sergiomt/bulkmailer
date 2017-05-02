package com.knowgate.bulkmailer.hipergate

import java.util.Date
import java.util.Arrays
import java.util.Comparator
import java.util.AbstractMap.SimpleImmutableEntry

import java.sql.Timestamp
import java.sql.SQLException
import java.sql.ResultSet
import java.sql.PreparedStatement

import javax.jdo.JDOException

import com.knowgate.bulkmailer.Log
import com.knowgate.bulkmailer.Job
import com.knowgate.bulkmailer.Jobs
import com.knowgate.bulkmailer.PENDING
import com.knowgate.bulkmailer.Mailing
import com.knowgate.bulkmailer.Using._
import com.knowgate.bulkmailer.EmailMessagesByDay
import com.knowgate.bulkmailer.EmailMessagesByHour
import com.knowgate.bulkmailer.EmailMessagesByAgent

import org.judal.storage.table.Table
import org.judal.storage.table.IndexableView
import org.judal.storage.table.Record
import org.judal.storage.table.RecordSet
import org.judal.storage.StorageObjectFactory
import org.judal.storage.relational.RelationalView
import org.judal.storage.relational.RelationalDataSource
import org.judal.storage.scala.ArrayRecord

import org.judal.storage.table.ColumnGroup

import org.judal.storage.query.Term
import org.judal.storage.query.Operator
import org.judal.storage.query.Connective

import org.judal.storage.query.sql.SQLTerm

import scala.collection.mutable.Map
import scala.collection.mutable.HashMap
import scala.collection.mutable.Buffer

import scala.collection.JavaConversions.asScalaBuffer

class HgJobs(dts: RelationalDataSource, props: Map[String,String]) extends Jobs {

    @throws(classOf[JDOException])
    def pending(workarea: String) : Array[Job] = {
      var rst: Buffer[Job] = null
      val now = new Date()
      
      Log.out.debug("HgJobs.pending(workarea="+workarea+")")
      
      val job = new HgJob(dts, props)

      var tbl = dts.openTable(job)
      using(tbl) {
        val rst : RecordSet[HgJob] = tbl.fetch(job.fetchGroup(), "id_status", String.valueOf(PENDING.shortValue))
        for (rec <- rst)
          rec.parameters = props
      }

      Log.out.debug(String.valueOf(rst.size)+" jobs pending of execution at all workareas")
      
      // If sending for just a single workarea then discard all jobs belonging to other workareas
      if (workarea.length()>0)
        rst = rst.filter(j => j.workarea==workarea)

      Log.out.debug(String.valueOf(rst.size)+" jobs pending of execution at workarea "+workarea)
        
      // Replace null execution dates by now
        rst.foreach(j => if (j.executionDate==null) j.setExecutionDate(now))
           
      // Process only jobs which execution date is greater than or equals to now
       rst = rst.filter(j => j.executionDate.compareTo(now)>=0)

      Log.out.debug(String.valueOf(rst.size)+" ready for start executing")
       
      // Sort jobs by execution date
      rst.sortWith((r1,r2) => r1.executionDate.compareTo(r2.executionDate) < 0)

      rst.toArray
    }

    @throws(classOf[JDOException])
    def group(groupId: String) : Array[Job] = {
      var rst: RecordSet[HgJob] = null
      val job = new HgJob(dts, props)
      var tbl = dts.openTable(job)
      using(tbl) {
        rst = tbl.fetch(job.fetchGroup(), "gu_job_group", groupId)
        for (rec <- rst)
          rec.parameters = props
      }
      rst.toArray(new Array[Job](rst.size()))
    }

    @throws(classOf[JDOException])
    def between(workarea: String, from:Date, to:Date) : Array[Job] = {
        val key = workarea+from+to
        var rst: RecordSet[HgJob] = null
        val job = new HgJob(dts, props)
        var tbl = dts.openRelationalView(job)
        using(tbl) {
          val qry = tbl.newQuery()
          qry.setResultClass(job.getClass, dts.getClass, props.getClass)
          qry.setFilter(qry.newPredicate(Connective.AND).add("dt_execution", Operator.BETWEEN, Array[Date](from,to)).add("gu_workarea", Operator.EQ, workarea));
          rst = tbl.fetch(qry)
          for (rec <- rst)
            rec.parameters = props
        }
        rst.toArray(new Array[Job](rst.size))
    }

    @throws(classOf[JDOException])
    def mailings(workarea: String, from: Date, to: Date) : Array[Mailing] = {
      val tsFrom = new Timestamp(from.getTime())
      val tsTo = new Timestamp(to.getTime())
      var zero = new Integer(0)
      var mailing : Mailing = null
      var mailings : Array[Mailing] = null
      var tbl : RelationalView = null

      val adhocCols = new ColumnGroup("gu_mailing","pg_mailing","nm_mailing","tx_subject","0 AS nu_messages","0 AS nu_opened","dt_execution","0 AS nu_clicks")

      val pagesetCols = new ColumnGroup("gu_pageset AS gu_mailing","-1 AS pg_mailing","nm_pageset AS nm_mailing","tx_subject","0 AS nu_messages","0 AS nu_opened","dt_created AS dt_execution","0 AS nu_clicks")

      val ahm = new HgAdHocMailing(dts)
      
      try {
        
        val ahm = new HgAdHocMailing(dts)
        tbl = dts.openRelationalView(ahm)        

        var qry = tbl.newQuery
        qry.setResult(adhocCols.getMembers)
        qry.setResultClass(ahm.getClass, dts.getClass)
        
        val datefilter = qry.newPredicate(Connective.OR)
        datefilter.add("dt_execution", Operator.BETWEEN, Array[Timestamp](tsFrom,tsTo))
        datefilter.add("dt_finished", Operator.BETWEEN, Array[Timestamp](tsFrom,tsTo))
        
        val subselect = qry.newPredicate(Connective.AND)
        subselect.add("gu_workarea", Operator.EQ, workarea)
        subselect.add(datefilter)
      
        qry.setFilter(qry.newPredicate(Connective.AND).add("gu_mailing", Operator.IN, "k_jobs", "gu_job_group", subselect))
        
        val adhocs = tbl.fetch(qry)
		    
        tbl.close
		    tbl = null

        val pgs = new HgPageSetMailing(dts)

        tbl = dts.openRelationalView(pgs)        
        qry = tbl.newQuery()
        qry.setResult(pagesetCols.getMembers)
        qry.setResultClass(pgs.getClass, dts.getClass)
        qry.setFilter(qry.newPredicate(Connective.AND).add("gu_pageset", Operator.IN, "k_jobs", "gu_job_group", subselect))
        
        val pagesets = tbl.fetch(qry)

		    tbl.close
		    tbl = null
		    
		    var both : Array[Mailing] = null
		    if (adhocs.size==0) {
		      both = pagesets.toArray(new Array[Mailing](pagesets.size))
		    } else if (pagesets.size==0) {
		      both = adhocs.toArray(new Array[Mailing](adhocs.size))
		    } else {
		      both = adhocs.toArray(new Array[Mailing](adhocs.size + pagesets.size))
          System.arraycopy(pagesets, 0, both, adhocs.size, pagesets.size)		      
		    }
		    
        Arrays.sort(both, new Comparator[Mailing]() { def compare(a: Mailing, b: Mailing) = a.lastExecutionDate.compareTo(b.lastExecutionDate) } )
		    
		    val docCount = both.length

		    mailings = new Array[Mailing](docCount)
		    val sumcntr = new HgGroupSumCounter().open(dts)
		    
		    using(sumcntr) {
		      for (d <- 0 until docCount) {
		        if (-1==both(d).getNumber) {
		          mailing = new HgPageSetMailing(dts)
		        } else {
		          var adhocml = new HgAdHocMailing(dts)
		          adhocml.setLastExecutionDate(both(d).lastExecutionDate)
		          adhocml.setNumber(both(d).getNumber)
		          mailing = adhocml
		        }
		        mailing.setId(both(d).getId)
		        mailing.setName(both(d).getName)
		        mailing.setSubject(both(d).getSubject)
		        sumcntr.sum(mailing)
		        mailings(d) = mailing
		      }
		    }

      } catch {
        case sqle: SQLException => {
          throw new JDOException(sqle.getMessage(),sqle)  
        }
      } finally {
        if (tbl!=null) tbl.close
      }
		  mailings
    }
    
  @throws(classOf[JDOException])
  def messagesSentByDay(workarea: String, from: Date , to: Date) : Array[EmailMessagesByDay] = {
    var msgsbyday : Array[EmailMessagesByDay] = null
    var nmsgs : RecordSet[Record] = null
    val tsFrom = new Timestamp(from.getTime)
    val tsTo = new Timestamp(to.getTime)
    val job = new HgJob(dts, new HashMap[String,String])
    val abd = new HgAtomsByDay(dts)
    val j = job.getTableName
    val d = abd.getTableName

    val atomsbyday = dts.openInnerJoinView(job, d, new SimpleImmutableEntry[String,String]("gu_job","gu_job")).asInstanceOf[RelationalView]
    using (atomsbyday) {
      
      val qry = atomsbyday.newQuery
      qry.setResult("SUM("+d+".nu_msgs) AS message_count,"+d+".dt_execution AS exec_date")
      qry.setResultClass(abd.getClass, dts.getClass)
      val where = qry.newPredicate(Connective.AND)
      where.add(j+".dt_execution", Operator.BETWEEN, Array[Timestamp](tsFrom,tsTo))
      where.add(j+".gu_job", Operator.IN, j, "gu_job", new SQLTerm("gu_workarea", Operator.EQ, workarea))
      qry.setFilter(where)
      qry.setOrdering("2")
      qry.setGrouping("2")
      nmsgs = atomsbyday.fetch(qry)
    }
    msgsbyday = new Array[EmailMessagesByDay](nmsgs.size)            
    for (m <- 0 until nmsgs.size) {
      val rec = nmsgs.get(m)
      msgsbyday(m) = new EmailMessagesByDay(rec.getInt("message_count"), rec.getString("exec_date"))
    }
    msgsbyday
  }
  
  @throws(classOf[JDOException])
  def messagesReadedByHour(workarea: String, from:Date, to:Date) : Array[EmailMessagesByHour] = {
    var msgsbyhour : Array[EmailMessagesByHour] = null
    var nmsgs : RecordSet[Record] = null
    val tsFrom = new Timestamp(from.getTime)
    val tsTo = new Timestamp(to.getTime)
    val job = new HgJob(dts, new HashMap[String,String])
    val abh = new HgAtomsByHour(dts)
    val j = job.getTableName
    val h = abh.getTableName
        
    val atomsbyhour = dts.openInnerJoinView(job, h, new SimpleImmutableEntry[String,String]("gu_job","gu_job")).asInstanceOf[RelationalView]
    using(atomsbyhour) {
      val qry = atomsbyhour.newQuery
      qry.setResult("SUM(h.nu_msgs) AS message_count,h.dt_hour AS hour")
      val where = qry.newPredicate(Connective.AND)
      where.add(j+".dt_execution", Operator.BETWEEN, Array[Timestamp](tsFrom,tsTo))
      where.add(j+".gu_job", Operator.IN, j, "gu_job", new SQLTerm("gu_workarea", Operator.EQ, workarea))
      qry.setFilter(where)
      qry.setOrdering("2")
      qry.setGrouping("2")
      nmsgs = atomsbyhour.fetch(qry)
    }
    msgsbyhour = new Array[EmailMessagesByHour](nmsgs.size)
    for (m <- 0 until nmsgs.size) {
      val rec = nmsgs.get(m)
      msgsbyhour(m) = new EmailMessagesByHour(rec.getInt("message_count"), rec.getShort("hour"))
    }
    msgsbyhour    
  }

  @throws(classOf[JDOException])
  def messagesReadedByAgent(workarea: String, from:Date, to:Date) : Array[EmailMessagesByAgent] = {
    var msgsbyagent : Array[EmailMessagesByAgent] = null
    var nmsgs : RecordSet[HgAtomsByAgent] = null
    val tsFrom = new Timestamp(from.getTime)
    val tsTo = new Timestamp(to.getTime)
    val job = new HgJob(dts, new HashMap[String,String])
    val aba = new HgAtomsByAgent(dts)
    val j = job.getTableName
    val h = aba.getTableName

    val atomsbyagent = dts.openInnerJoinView(job, h, new SimpleImmutableEntry[String,String]("gu_job","gu_job")).asInstanceOf[RelationalView]
    
    using (atomsbyagent) {
      val qry = atomsbyagent.newQuery
      qry.setResult("a.id_agent AS id_agent,SUM(a.nu_msgs) AS nu_msgs")
      qry.setResultClass(aba.getClass, dts.getClass)
      val where = qry.newPredicate(Connective.AND)
      where.add("j.dt_execution", Operator.BETWEEN, Array[Timestamp](tsFrom,tsTo))
      where.add("j.gu_job", Operator.IN, j, "gu_job", new SQLTerm("gu_workarea", Operator.EQ, workarea))
      qry.setFilter(where)
      qry.setGrouping("1")
      nmsgs = atomsbyagent.fetch(qry)
    }
    msgsbyagent = new Array[EmailMessagesByAgent](nmsgs.size)
    for (m <- 0 until nmsgs.size) {
      val rec = nmsgs.get(m)
      msgsbyagent(m) = new EmailMessagesByAgent(rec.getMessageCount, rec.getAgent)
    }

    msgsbyagent
  }
  
}