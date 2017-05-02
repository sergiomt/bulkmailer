package com.knowgate.bulkmailer.hipergate

import java.io.PrintStream

import java.util.Properties

import javax.mail.Message.RecipientType

import org.judal.storage.table.Table
import org.judal.storage.Engine
import org.judal.storage.table.RecordSet
import org.judal.storage.table.TableDataSource
import org.judal.storage.relational.RelationalDataSource

import com.knowgate.bulkmailer.Job
import com.knowgate.bulkmailer.Jobs
import com.knowgate.bulkmailer.Routine
import com.knowgate.bulkmailer.Using._
import com.knowgate.bulkmailer.PENDING
import com.knowgate.bulkmailer.Factory
import com.knowgate.bulkmailer.UrlData
import com.knowgate.bulkmailer.Mailing
import com.knowgate.bulkmailer.BlackList
import com.knowgate.bulkmailer.BlackListedEmail
import com.knowgate.bulkmailer.MailMessage
import com.knowgate.bulkmailer.SingleEmailAtom
import com.knowgate.bulkmailer.RecipientData
import com.knowgate.bulkmailer.EmailAtomArchiver
import com.knowgate.bulkmailer.hipergate._

import scala.collection.mutable.Map

import scala.collection.JavaConverters._

object HgFactory extends Factory {

  def instance : Factory = this
  
  override def getBlackList(d: TableDataSource, n: Int = 2051, w: String = "00000000000000000000000000000000") : BlackList = new HgBlackList(d, n, w)

  override def newJob(d: TableDataSource, p: java.util.Map[String,String]) : Job = new HgJob(d, p.asScala)

  override def newEmailAtom(d: TableDataSource, r: Routine) : SingleEmailAtom = new HgSingleEmailAtom(d,r,RecipientType.TO,"HTML",PENDING)
  
  override def loadJob(d: TableDataSource, p: java.util.Map[String,String], jid: String) : Job = {
    if (null==jid) throw new NullPointerException("HgFactory.loadJob() job id cannot be null")
    val j = new HgJob(d, p.asScala, jid)
    val t = d.openTable(j)
    var loaded : Boolean = false
    using (t) {
      loaded = t.load(jid, j)
    }
    if (loaded) j else null
  }

  override def getJobs(d: TableDataSource, p: java.util.Map[String,String]) : Jobs = new HgJobs(d.asInstanceOf[RelationalDataSource], p.asScala)

  override def loadBlackListedEmail(d: TableDataSource, w: String, e: String) : BlackListedEmail = {
  	var m = new HgBlackListedEmail(d)
  	val t = d.openView(m)
  	using(t) {
  	  val rst : RecordSet[HgBlackListedEmail] = t.fetch(m.fetchGroup, "tx_email", e, java.lang.Integer.MAX_VALUE, 0)
  	  m = null
      for (rec <- rst.asScala) {
        val wrka = rec.workarea
        if (wrka==w || wrka=="00000000000000000000000000000000" || wrka==null)
          m = rec
      } 
  	}
    m
  }
  
  override def newMailMessage(d: TableDataSource, w: String) : MailMessage = new HgMailMessage(d, w)

  override def loadMailMessage(d: TableDataSource, w: String, id: String) : MailMessage = {
    if (null==id) throw new NullPointerException("HgFactory.loadMailMessage() message id cannot be null")
  	val m = new HgMailMessage(d, w)
  	if (m.load(d,id)) m else null
  }
  
  override def getArchiver(src: TableDataSource, tgt: TableDataSource) : EmailAtomArchiver = {
    if (src.getClass.getName.equals("org.judal.jdbc.JDBCTableDataSource"))
      if (src eq tgt)
        new HgEmailAtomArchiverSameDb(src)
      else
        new HgEmailAtomArchiverGeneric(src,tgt)
    else
      new HgEmailAtomArchiverGeneric(src,tgt)
  }

  override def newUrlData(dts: TableDataSource) = new HgUrlData(dts)
  
  override def loadUrlData(dts: TableDataSource, guid: String, workarea: String) : UrlData = {
    var urld = newUrlData(dts)
    val loaded = urld.load(dts, Array[Object](guid,workarea))
    if (loaded) urld else null
  }
  
  override def getBeaconTracker(dts: TableDataSource) = new HgBeaconTrackerDb(dts)

  override def getClickTracker(dts: TableDataSource, workarea: String) = new HgClickTrackerDb(dts.asInstanceOf[RelationalDataSource], workarea)

  override def getStatistics(dts: TableDataSource, prntStrm: PrintStream) = new HgStatistics(dts, prntStrm);

  override def loadAdHocMailing(dts: TableDataSource, id: String) : Mailing = {
    var m = new HgAdHocMailing(dts)
    val tbl = dts.openView(m)
    using(tbl) {
  	if (!tbl.load(id, m))
    	  m = null
    }
    m
  }

  override def loadPageSetMailing(dts: TableDataSource, id: String) : Mailing = {
    var m = new HgPageSetMailing(dts)
    var tbl = dts.openView(m)
    using(tbl) {
    	if (!tbl.load(id, m))
    	  m = null
    }
    m
  }

  override def loadRecipientData(dts: TableDataSource, w: String, e: String) : RecipientData = {
    val d = new HgRecipientData(dts, w, e)
    if (d.load(w))
      d
    else
      null
  }

  override def newRecipientData(dts: TableDataSource, w: String, e: String) = new HgRecipientData(dts,w,e)
  
  override def newClickThrough(dts: TableDataSource) = new HgClickThrough(dts)
  
  override def newTotalAtomsByDay(dts: TableDataSource) = new HgTotalAtomsByDay(dts)

  override def getMailerUsersForWorkArea(dts: TableDataSource, workareaId: String) = new HgMailerUser(dts).forWorkArea(workareaId)
  
}