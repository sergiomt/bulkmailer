package com.knowgate.bulkmailer.hipergate

import java.util.List
import java.util.Date
import java.util.Arrays
import java.util.Iterator
import java.util.Comparator
import java.util.Properties
import java.util.Collections
import java.util.NoSuchElementException  
import java.sql.Connection
import java.sql.SQLException
import java.sql.PreparedStatement
import java.sql.CallableStatement

import javax.jdo.JDOException

import javax.mail.Message.RecipientType

import com.knowgate.stringutils.Str
import com.knowgate.stringutils.Uid

import org.judal.storage.Table
import org.judal.storage.Param
import org.judal.storage.Record
import org.judal.storage.scala.ArrayRecord
import org.judal.storage.RecordSet
import org.judal.storage.ColumnGroup
import org.judal.storage.DataSource
import org.judal.storage.TableDataSource
import org.judal.metadata.ColumnDef

import org.judal.jdbc.JDBCRelationalTable

import com.knowgate.debug.Chronometer

import com.knowgate.bulkmailer.Using._
import com.knowgate.bulkmailer.Job
import com.knowgate.bulkmailer.Urls
import com.knowgate.bulkmailer.ABORTED
import com.knowgate.bulkmailer.PENDING
import com.knowgate.bulkmailer.RUNNING
import com.knowgate.bulkmailer.FINISHED
import com.knowgate.bulkmailer.SUSPENDED
import com.knowgate.bulkmailer.INTERRUPTED
import com.knowgate.bulkmailer.RoutineStatus
import com.knowgate.bulkmailer.BlackList
import com.knowgate.bulkmailer.Mailing
import com.knowgate.bulkmailer.WebBeacon
import com.knowgate.bulkmailer.ClickThrough
import com.knowgate.bulkmailer.SingleEmailAtom
import com.knowgate.bulkmailer.EmailMessagesByDay
import com.knowgate.bulkmailer.EmailMessagesByHour
import com.knowgate.bulkmailer.MailBodyPreprocessor
import com.knowgate.bulkmailer.Profiler

import com.oreilly.servlet.MailMessage

import scala.collection.mutable.Map
import scala.collection.mutable.HashMap
import scala.collection.mutable.Buffer
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConverters._
import scala.collection.JavaConversions.asScalaBuffer

class HgJob(var dataSource: TableDataSource, props: Map[String,String], jid: String = Uid.createUniqueKey()) extends ArrayRecord(dataSource,"k_jobs") with Job {
  
  var atms : Array[SingleEmailAtom] = null
  var arch : List[Record] = null
  
  if (jid!=null) put("gu_job", jid)
  setSentCount(0)
  setOpenedCount(0)
  setUniqueCount(0)
  setClicksCount(0)

  var parameters : Map[String,String] = new HashMap[String,String]

  var targetlists: ArrayBuffer[String] = new ArrayBuffer[String]()
  
  // Find target lists of this job group
  if (!isNull("gu_job_group")) {
  	var tbl: Table = null
    using(tbl) {
      val adHocMailingList = new HgAdHocMailingList(dataSource)
      val mailings : RecordSet[ArrayRecord] = tbl.fetch(adHocMailingList.fetchGroup(), "gu_mailing",getString("gu_job_group"))
      val pagesets : RecordSet[ArrayRecord] = tbl.fetch(adHocMailingList.fetchGroup(), "gu_pageset",getString("gu_job_group"))
      asScalaBuffer(mailings).foreach(l => targetlists+= l.getString("gu_list"))
      asScalaBuffer(pagesets).foreach(l => targetlists+= l.getString("gu_list"))
    }
  }
  
  def this(dataSource: TableDataSource, props: Map[String,String], rec: Record) {
    this(dataSource, props, rec.getString("gu_job"))
    val i = rec.fetchGroup.getMembers.iterator
    while (i.hasNext) {
      val k = i.next.asInstanceOf[String]
      put(k, rec.apply(k))
    }
  }

  def getId() = getString("gu_job")

  def setId(id: String) : Unit = { put("gu_job", id) }
  
  @throws(classOf[JDOException])
  def atoms : Array[SingleEmailAtom] = {    
    var meter : Chronometer = null
    if (Profiler.enabled) meter = new Chronometer()
  	if (atms==null) {
      var bfr: Buffer[SingleEmailAtom] = null
      var tbl: Table = null
      var rst: RecordSet[SingleEmailAtom] = null;
      using(tbl) {
        val eMailAtom = new ArrayRecord(dataSource,"k_job_atoms")
        tbl = dataSource.openTable(eMailAtom)
        rst = tbl.fetch(eMailAtom.fetchGroup(), "gu_job", jid)
        rst.sort("pg_atom")
        bfr = asScalaBuffer(rst).map(rec => new HgSingleEmailAtom(dataSource, this, rec))
        atms = bfr.toArray
      }
      if (Profiler.enabled) Profiler.totalAtomsRetrievalTime += meter.stop  	      
    }
    atms
  }

  @throws(classOf[JDOException])
  def atom(pg: Int) : SingleEmailAtom = {    
    val atm = new HgSingleEmailAtom(dataSource,null,null,null,null)
    atm.setId(pg)
    if (atms==null) atoms()
    var idx = Arrays.binarySearch(atms, atm, new Comparator[SingleEmailAtom]() {
			                            def compare(a1: SingleEmailAtom, a2: SingleEmailAtom) = a1.id-a2.id })
		if (idx>=0) {
		  atms(idx)
		} else {
			if (arch==null) archived()		  
      idx = Collections.binarySearch(arch, atm.asInstanceOf[Record], new Comparator[Record]() {
			                               def compare(r1: Record, r2: Record) = r1.getInt("pg_atom")-r2.getInt("pg_atom") })
			if (idx>=0) {
			  atm.putAll(arch.get(idx))
			  atm
			} else {
			  null
			}
		}
  }

  @throws(classOf[JDOException])
  def atomCount : Int = {
    var tbl: Table = null
    var cnt = 0
    using(tbl) {
      val jobAtoms = new HgSingleEmailAtom(dataSource, this, RecipientType.TO, "HTML", status())
      tbl = dataSource.openTable(jobAtoms)
      cnt = tbl.count("gu_job", id)
      tbl.close()
      tbl = null;
      val archivedAtoms = new HgArchivedEmailMessage(dataSource)
      tbl = dataSource.openTable(archivedAtoms)
      cnt += tbl.count("gu_job", id)
      tbl.close()
      tbl = null
    }
    cnt
  }

  @throws(classOf[JDOException])
  def archived : List[Record] =  {
  	if (arch==null) {
      var tbl : Table = null
      var rst : RecordSet[Record] = null
      using(tbl) {
        val archivedAtoms = new ArrayRecord(dataSource, "k_job_atoms_archived")
        tbl = dataSource.openTable(archivedAtoms)
        rst = tbl.fetch(archivedAtoms.fetchGroup(), "gu_job", jid)
        rst.sort("pg_atom")
        tbl.close()
        tbl = null
      }
      arch = rst.asInstanceOf[List[Record]]
  	}
    arch
  }

  @throws(classOf[JDOException])
  @throws(classOf[IllegalStateException])
  def clickThrough() : Array[ClickThrough] = {
    if (null==workarea)
      throw new IllegalStateException("Job must be loaded before calling clickThrough() method");
    var tbl : Table = null
    var rst : RecordSet[Record] = null
    var clickt : Array[HgClickThrough] = null
    val urlMap = new Urls(dataSource, workarea, new HgUrlData(dataSource), new HgClickThrough(dataSource))
    try {
      val atomClick = new HgClickThrough(dataSource)
      tbl = dataSource.openTable(atomClick)
      rst = tbl.fetch(atomClick.fetchGroup(), "gu_job", jid)
      rst.sort("dt_action")
      tbl.close()
      tbl = null
      clickt = rst.toArray(new Array[HgClickThrough](rst.size()))
      for (c <- 0 until clickt.length) {
        val rec = clickt(c)
        if (rec.urlId()!=null)
          rec.setUrl(urlMap.getAddressForGuid(rec.urlId()))
      }
    } finally {
      if (tbl!=null) tbl.close()
    }
    clickt.asInstanceOf[Array[ClickThrough]]
  }

  @throws(classOf[JDOException])
  @throws(classOf[IllegalStateException])
  def webBeacons() : Array[WebBeacon] = {
    if (null==workarea)
      throw new IllegalStateException("Job must be loaded before calling webBeacons() method");
    var tbl : Table = null
    var rst : RecordSet[HgWebBeacon] = null
    var webcns : Array[WebBeacon] = null
    using(tbl) {
      val wbc = new HgWebBeacon(dataSource)
      tbl = dataSource.openTable(wbc)
      rst = tbl.fetch(wbc.fetchGroup(), "gu_job", jid)
      rst.sort("pg_atom")
      tbl.close()
      tbl = null
      webcns = rst.toArray(new Array[WebBeacon](rst.size()))
      webcns = new Array[WebBeacon](rst.size())
    }
    webcns
  }

  private def compare(dt1: Date, dt2: Date) : Int = {
    if (dt1==null)
      -1
    else if (dt2==null)
      1
    else
      dt1.compareTo(dt2)
  }
    
  @throws(classOf[JDOException])
  def mailing : Mailing =  {
    var tbl : Table = null
    var mlng : Mailing = null
    using(tbl) {
      mlng = new HgAdHocMailing(dataSource)
      tbl = dataSource.openTable(mlng)
      var found = tbl.load(group, mlng)
      tbl.close()
      tbl = null
      if (!found) {
        mlng = new HgPageSetMailing(dataSource)
        tbl = dataSource.openTable(mlng)
        found = tbl.load(group, mlng)
        tbl.close()      
        tbl = null        
        if (!found) mlng = null
      }
    }
    mlng
  }
  
  def preprocessor : MailBodyPreprocessor = new HgMailBodyPreprocessor()

  def process (emm: SingleEmailAtom) = { }
  
  def id = getString("gu_job")
  
  def command: String = getString("id_command","")

  def setCommand(c: String) = { put("id_command", c) }

  def getMailMessageId: String = getString("gu_job_group", "")

  def setMailMessageId(g: String) = { put("gu_job_group", g) }
  
  def is (param: String, values: Array[String], default: String) : Boolean = {
    var v = getParameter(param, default)
    for (n <- 0 until values.length)
      if (values(n)==v) return true
    false
  }

  @throws(classOf[NoSuchElementException])
  def getParameter(p: String) = parameters(p)

  def getParameter(p: String, d: String) : String = {
    try {
      parameters(p)
    } catch {
      case xcpt: NoSuchElementException => d
    }
  }

  def setParameter(p: String, v: String) = {
    parameters.put(p, v)
  }

  def setParameters(params: java.util.Map[String,String]) = {
    params.asScala.foreach(p => setParameter(p._1, p._2))
  }

  def properties = props.asJava

  def executionDate : Date = getDate("dt_execution")

  def setExecutionDate(dt: Date) = { put("dt_execution", dt) } 
  
  def finishDate : Date = getDate("dt_finished")

  def setFinishDate(dt: Date) = { put("dt_finished", dt) }

  def group : String = getString("gu_job_group")

  def setGroup(g: String) = { put("gu_job_group", g) }
  
  def status: RoutineStatus = {
    getInt("id_status") match {
      case -1 => ABORTED
      case 0 => FINISHED
      case 1 => PENDING
      case 2 => SUSPENDED
      case 3 => RUNNING
      case 4 => INTERRUPTED
    }
  }
  
  def setStatus(s: RoutineStatus) = { put("id_status", new java.lang.Short(s.shortValue)) }

  def setStatus(s: Short) = { put("id_status", new java.lang.Short(s)) }
  
  def updateStatus(s: RoutineStatus) = {
    var tbl: Table = null
    val now = new Date()
    setStatus(s)
    put ("dt_modified", now)
    if (s==FINISHED)
      setFinishDate(now)
    store(dataSource)
  }

  def title: String = getString("tl_job", "")

  def setTitle(t: String) = { put ("tl_job", t) }
  
  def sentCount : Int = if (isNull("nu_sent")) 0 else getInt("nu_sent")

  def setSentCount(n: Int) = { put ("nu_sent", new Integer(n)) } 

  def openedCount : Int = if (isNull("nu_opened")) 0 else getInt("nu_opened")

  def setOpenedCount(n: Int) = { put ("nu_opened", new Integer(n)) } 

  def uniqueCount : Int = if (isNull("nu_unique")) 0 else getInt("nu_unique")

  def setUniqueCount(n: Int) = { put ("nu_unique", new Integer(n)) }

  def clicksCount : Int = if (isNull("nu_clicks")) 0 else getInt("nu_clicks")

  def setClicksCount(n: Int) = { put ("nu_clicks", new Integer(n)) }

  def workarea = getString("gu_workarea", "")

  def setWorkarea(w: String) = { put("gu_workarea", w) }

  def writer = getString("gu_writer", "")

  def setWriter(w: String) = { put("gu_writer", w) }
    
  private def parseParameters() {
    if (!isNull("tx_parameters")) {
      val params = getString("tx_parameters").split(",")
      for (p <- 0 until params.length) {
          var par = params(p)
      		val dot = params(p).indexOf(':')
          if (dot <= 0 || dot == par.length()-1)
            parameters += (par -> "")
          else
            parameters += (par.substring(0,dot) -> par.substring(dot+1));        
      }
    }
  }

  @throws(classOf[JDOException])
  override def store(d: DataSource) : Unit = {
    if (!d.equals(dataSource))
        throw new JDOException("Supplied DataSource is not equal to the one used when instance was created")
    var tbl: Table = null
    val now = new Date()
    var params = ""
    parameters.keySet.foreach(k => params += k+":"+parameters(k)+",")
    put ("tx_parameters", Str.dechomp(params, ','))
    put ("dt_modified", now)
    if (status==FINISHED)
      setFinishDate(now)
    super.store(dataSource)
  }

  @throws(classOf[JDOException])
  override def delete(d: DataSource) = {
    if (!d.equals(dataSource))
        throw new JDOException("Supplied DataSource is not equal to the one used when instance was created")
    dataSource.call("k_sp_del_job", new Param("gu_job", 1, id))
  }
  
  @throws(classOf[JDOException])
  private def jdbcInsert(tbl: Table, addresses: ArrayBuffer[String], personalized: Boolean, rectype: RecipientType, format: String, status: RoutineStatus) = {
    val con = tbl.asInstanceOf[JDBCRelationalTable].getConnection()
    var stm : PreparedStatement = null
    var upd : PreparedStatement = null
    var cal : CallableStatement = null
    try {
      stm = con.prepareStatement("INSERT INTO k_job_atoms (gu_job,id_status,id_format,tp_recipient,tx_email) VALUES (?,?,?,?,?)")
      stm.setString(1,id)
      stm.setShort(2, status.shortValue)
      stm.setString(3,format)
      stm.setString(4,rectype.toString())
      for (addr <- addresses) {
        stm.setString(5,addr)
        stm.executeUpdate()
      }
      stm.close()
      stm = null
    	if (personalized) {
        val listsmbrs = targetlists.map(lst => new HgListMembers(dataSource, lst))
        val cols = new ColumnGroup("pg_atom","tx_email")
        val dbs : RecordSet[Record] = tbl.fetch(cols, "gu_job", id, addresses.size, 0)
    	  val count = dbs.size()
        cal = con.prepareCall("{ call k_sp_resolve_atom(?,?,?) ");
        cal.setString(1, id)
        cal.setString(3, workarea)
        upd = con.prepareStatement("UPDATE k_job_atoms SET tx_name=?,tx_surname=?,tx_salutation=? WHERE gu_job="+id+" AND pg_atom=?")       
        for (rec <- dbs) {
          cal.setInt(2, rec.getInt("pg_atom"))
          cal.execute()
          val mail = rec.getString("tx_email")
          for (mbrs <- listsmbrs) {
    	      val mbr = mbrs.getMemberByEmail(mail)
    	      if (mbr!=null) {
    	        upd.setString(1,mbr.getString("tx_name",""))
    	        upd.setString(2,mbr.getString("tx_surname",""))
    	        upd.setString(3,mbr.getString("tx_salutation",""))
    	        upd.executeUpdate()
    	      } // fi
    	    } // next        
        }
    	}
    } catch {
      case sqle : SQLException => throw new JDOException(sqle.getMessage(), sqle)
    } finally {
      if (upd!=null) upd.close()
      if (cal!=null) cal.close()
      if (stm!=null) stm.close()
    }
  }

  @throws(classOf[JDOException])
  private def noSQLInsert(tbl: Table, addresses: ArrayBuffer[String], personalized: Boolean, rectype: RecipientType, format: String, status: RoutineStatus) = {
    var emm = new HgSingleEmailAtom(dataSource, this, rectype, format, status)
    
    if (personalized) {
    	  
    	  val listsmbrs = targetlists.map(lst => new HgListMembers(dataSource, lst))
    	  
    	  val cols = new ColumnGroup("gu_job")

    	  for (addr <- addresses) {
    	    val jobIds = tbl.fetch(cols, "tx_email", addr)
    	    val found = jobIds.findFirst("gu_job", jid)!=null    	    
    	    if (!found) {
    	      emm = new HgSingleEmailAtom(dataSource, this, rectype, format, status)
    	      var rec = new HgRecipientData(dataSource, workarea, addr)
    	      if (rec.load())
    	        rec.datacols.foreach(colname => emm.replace(colname, rec.apply(colname)))
    	      for (mbrs <- listsmbrs) {
    	        val mbr = mbrs.getMemberByEmail(addr)
    	        if (mbr!=null) {
    	          emm.replace("tx_name", mbr.getString("tx_name",""))
    	          emm.replace("tx_surname", mbr.getString("tx_surname",""))
    	          emm.replace("tx_salutation", mbr.getString("tx_salutation",""))
    	        } // fi
    	      } // next
    	      tbl.store(emm)
    	    } // fi
    	  } // next
    	
    	} else {
        
    	  for (addr <- addresses) {
    	    emm.remove("pg_atom")
    	    emm.replace("tx_email", addr)
    	    tbl.store(emm)
    	  } 
    }
  }
  
  @throws(classOf[JDOException])
  def insertMessages(recs: Array[String], rectype: RecipientType = RecipientType.TO, format: String = "AUTO", status: RoutineStatus = PENDING, blck: BlackList) = {
    
    var meter : Chronometer = null
    if (Profiler.enabled) meter = new Chronometer()
    
    var addresses = new ArrayBuffer[String]
    val nrec = recs.length
    for (r <- 0 until nrec)
      if (!blck.contains(recs(r)))
        addresses += recs(r).trim().toLowerCase()
    addresses = addresses.filter(addr => addr.length()>0)
    addresses.map(a => MailMessage.sanitizeAddress(a))

    val personalized = is ("personalized", Job.YEAH, "false")

    var emm = new HgSingleEmailAtom(dataSource, this, rectype, format, status)
    
    var tbl : Table = null
    
    using(tbl) {
    	
      tbl = dataSource.openTable(emm)
    	
    	if (dataSource.getClass.getName.equals("org.judal.jdbc.JDBCTableDataSource"))
    		jdbcInsert(tbl, addresses, personalized, rectype, format, status)
    	else
    		noSQLInsert(tbl, addresses, personalized, rectype, format, status)

    	tbl.close()
    	tbl = null
    	
    }
    if (Profiler.enabled) Profiler.totalInsertMessagesTime += meter.stop
  }

  @throws(classOf[JDOException])
  def messagesByDay() : Array[EmailMessagesByDay] = {
    var tbl : Table = null
    var mbd : Array[EmailMessagesByDay] = null
    using(tbl) {
      val atomsByDay = new HgAtomsByDay(dataSource)
      tbl = dataSource.openTable(atomsByDay)
      var rst : RecordSet[HgAtomsByDay] = tbl.fetch(atomsByDay.fetchGroup, "gu_job", getId)
      tbl.close
      tbl=null
      rst.sort("dt_execution")
      val days = rst.size()
      mbd = new Array[EmailMessagesByDay](days)
      for (d <- 0 until days) {
        val rec = rst.get(d)
        mbd(d) = new EmailMessagesByDay(rec.getMessageCount,rec.getDate.getTime)
      }
    }
    mbd
  }

  @throws(classOf[JDOException])
  def messagesByHour() : Array[EmailMessagesByHour] = {
    var tbl : Table = null
    var mbh : Array[EmailMessagesByHour] = null
    using(tbl) {
      val atomsByHour = new HgAtomsByHour(dataSource)
      tbl = dataSource.openTable(atomsByHour)
      var rst : RecordSet[HgAtomsByHour] = tbl.fetch(atomsByHour.fetchGroup, "gu_job", getId)
      tbl.close
      tbl=null
      rst.sort("dt_hour")
      val hours = rst.size()
      mbh = new Array[EmailMessagesByHour](hours)
      for (d <- 0 until hours) {
        val rec = rst.get(d)
        mbh(d) = new EmailMessagesByHour(rec.getMessageCount,rec.getHour)
      }
    }
    mbh
  }
  
}