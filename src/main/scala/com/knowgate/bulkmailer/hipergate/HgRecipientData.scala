package com.knowgate.bulkmailer.hipergate

import java.util.AbstractMap.SimpleImmutableEntry
import java.util.NoSuchElementException

import javax.jdo.JDOException
import javax.jdo.JDOUserException

import javax.mail.Message.RecipientType

import org.judal.storage.table.Table
import org.judal.storage.table.Record
import org.judal.storage.table.TableDataSource
import org.judal.storage.table.RecordSet

import org.judal.storage.scala.ArrayRecord
import org.judal.storage.scala.IndexableTableOperation

import com.knowgate.bulkmailer.WebBeacon
import com.knowgate.bulkmailer.MailingList
import com.knowgate.bulkmailer.ClickThrough
import com.knowgate.bulkmailer.RecipientData
import com.knowgate.bulkmailer.ArchivedEmailMessage
import com.knowgate.bulkmailer.Using._

import scala.collection.mutable.Map
import scala.collection.mutable.Buffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import collection.JavaConversions.asScalaBuffer

class HgRecipientData(dts: TableDataSource, workarea: String, mail: String) extends ArrayRecord(dts,"k_member_address") with RecipientData {

  val datacols = List ("gu_company","gu_contact","tx_name","tx_surname","tx_salutation","nm_commercial","tp_street","nm_street","nu_street","tx_addr1","tx_addr2","nm_country","nm_state","mn_city","zipcode","work_phone","direct_phone","home_phone","mov_phone","fax_phone","other_phone","po_box")

  private var wrkaMap : HashMap[String,String] = null

  private var activeMap : HashMap[String,Boolean] = null
  
  def workarea = getString("gu_workarea", "")

  def email = mail.toLowerCase().trim()

  override def firstName = getString("tx_name", "")

  override def lastName = getString("tx_surname", "")

  override def salutation = getString("tx_salutation", "")
  
  @throws(classOf[JDOException])
  def load(wrka: String = "00000000000000000000000000000000") : Boolean = {
    var tbl: Table = null
    try {
      tbl = dts.openTable(this)
      val rst : RecordSet[Record] = tbl.fetch(fetchGroup(), "tx_email", email)
      tbl.close()
      tbl=null
      for (m <-0 until rst.size()) {
        var rec = rst.get(m)
        if (rec.getString("gu_workarea")==wrka) {
          for (col <- datacols if !rec.isNull(col) )
            put(col,get(col))
          return true
        }
      }
      return false
    } finally {
      if (tbl!=null) tbl.close()
    }        
  }
  
  private def matchesWorkarea(w:String, v:String) = w==v || w=="00000000000000000000000000000000" || w==null

  @throws(classOf[JDOException])
  def isBlackListed() : Boolean = {
    var blackListed = false
    val ble = new HgBlackListedEmail(dts)
    var op = new IndexableTableOperation[HgBlackListedEmail](dts,ble)
    using(op) {
      val rst = op.fetch(ble.fetchGroup, "tx_email", email)
      blackListed = rst.exists {  r => matchesWorkarea(r.getString("gu_workarea"), workarea) }
    }
    blackListed
  }

  @throws(classOf[JDOException])
  @throws(classOf[NoSuchElementException])
  private def getWorkareaForJob(jid: String) : String = {
    if (null==wrkaMap) wrkaMap = new HashMap[String,String]
    if (!wrkaMap.contains(jid)) {
      val job = new HgJob(dts, new HashMap[String,String])
      val tbl = dts.openTable(job)
      var rst : RecordSet[HgJob] = tbl.fetch(job.fetchGroup, "gu_job", jid)
      tbl.close
      if (rst.size>0)
        wrkaMap += (jid -> rst.get(0).getString("gu_workarea"))
    }
    wrkaMap(jid)
  }

  @throws(classOf[JDOException])
  def messagesSent() : Array[ArchivedEmailMessage] = {
    var rst : Iterable[HgArchivedEmailMessage] = null
    var snt : Array[ArchivedEmailMessage] = new Array[ArchivedEmailMessage](0)
    var op = new IndexableTableOperation[HgArchivedEmailMessage](dts)
    using(op) {
      val arc = new HgArchivedEmailMessage(dts)
      rst = op.fetch(arc.fetchGroup, "tx_email", email).filter(r => (-1!=r.getInt("id_status")) && (4!=r.getInt("id_status")) && (getWorkareaForJob(r.getString("gu_job")).equals(workarea)))
    }    
    rst.toArray[ArchivedEmailMessage]
  }

  @throws(classOf[JDOException])
  def messagesOpened() : Array[WebBeacon] = {
    var rst : Iterable[HgWebBeacon] = null
    val wb = new HgWebBeacon(dts)
    var op = new IndexableTableOperation[HgWebBeacon](dts, wb)
    using(op) {
      var rst = op.fetch(wb.fetchGroup, "tx_email", email)
    }
    rst.filter(r => (-1!=r.getInt("id_status")) && (4!=r.getInt("id_status")) && (getWorkareaForJob(r.getString("gu_job")).equals(workarea))).toArray[WebBeacon]
  }

  @throws(classOf[JDOException])
  def clickThrough() : Array[ClickThrough] = {
    var rst : Iterable[HgClickThrough] = null
    var clk : Array[ClickThrough] = new Array[ClickThrough](0)
    var op = new IndexableTableOperation[HgArchivedEmailMessage](dts)
    using(op) {
      val jac = new HgClickThrough(dts)
      var rst = op.fetch(jac.fetchGroup, "tx_email", email).filter(r => getWorkareaForJob(r.getString("gu_job")).equals(workarea))
    } 
    rst.toArray[ClickThrough]
  }

  @throws(classOf[JDOException])
  @throws(classOf[NoSuchElementException])
  def isActiveAtList(lst: MailingList) : Boolean = {
    if (null==activeMap) throw new JDOUserException("lists() method must be called prior to isActiveAtList")
    activeMap(lst.getId())
  }

  @throws(classOf[JDOException])
  private def lists(tp: Int) : Array[MailingList] = {
    val mbr = new HgListMember(dts)
    var op = new IndexableTableOperation[HgListMember](dts,mbr)
    var mll = new Array[MailingList](0)
    val lsts = new HashMap[String,MailingList]
    if (null==activeMap)
      activeMap = new HashMap[String,Boolean]
    using(op) {
      val rst = op.fetch(mbr.fetchGroup, "tx_email", email)
      var lst = new HgMailingList(dts)
      var tbl : Table = null
      using(tbl) {
        tbl = dts.openTable(lst)
        for (rec <- rst) {
          val lid = rec.getListId
          if (!lsts.contains(lid)) {
            lst = new HgMailingList(dts)
            tbl.load(lid, lst)
            val tpl = rec.getListType
            if (lst.getWorkarea==workarea && ((tp==tpl || (4!=tp && 4!=tpl)))) {
              lsts += (lid -> lst)
              if (!activeMap.contains(lid))
                activeMap += (lid -> rec.isActive)
            }
          }
        }
      }
    }
    lsts.values.toArray
  }

  @throws(classOf[JDOException])
  def lists() : Array[MailingList] = lists(0)

  @throws(classOf[JDOException])
  def exclusionLists() : Array[MailingList] = lists(4)
  
}