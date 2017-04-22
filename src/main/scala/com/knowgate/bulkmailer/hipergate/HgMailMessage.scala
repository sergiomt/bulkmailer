package com.knowgate.bulkmailer.hipergate

import java.io.File
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream

import java.sql.Types
import java.sql.SQLException
import java.sql.PreparedStatement
import java.sql.CallableStatement

import java.util.List
import java.util.LinkedList
import java.math.BigDecimal

import javax.jdo.JDOException

import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart

import com.knowgate.stringutils.Uid

import org.judal.storage.Param
import org.judal.storage.Bucket
import org.judal.storage.Table
import org.judal.storage.Record
import org.judal.storage.RecordSet
import org.judal.storage.DataSource
import org.judal.storage.TableDataSource
import org.judal.storage.scala.ArrayRecord

import org.judal.jdbc.JDBCDataSource

import com.knowgate.bulkmailer.Using._
import com.knowgate.bulkmailer.MailPart
import com.knowgate.bulkmailer.MailMessage

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions.asScalaBuffer

class HgMailMessage(dts: TableDataSource, val workarea: String = "00000000000000000000000000000000") extends ArrayRecord(dts,"k_mime_msgs") with MailMessage {

  private val PLAIN = "plain"
  private val HTML = "html"
    
  private var parts: LinkedList[MailPart] = new LinkedList[MailPart]

  put("gu_mimemsg", Uid.createUniqueKey())
  
  put("gu_workarea", workarea)

  def getId = getString("id_message", getGuid)
  
  def getGuid = getString("gu_mimemsg")

  def setGuid(uid: String) = { put("gu_mimemsg", uid) }
  
  def getSubject = getString("tx_subject", "")
  
  def setSubject(s: String) { put("tx_subject", s) }
  
  def getFromAddress = getString("tx_email_from", "")

  def setFromAddress(a: String) { put("tx_email_from", a) }

  def getReplyAddress = getString("tx_email_reply", "")

  def setReplyAddress(a: String) { put("tx_email_reply", a) }
  
  def getDisplayName = getString("nm_from", "")

  def setDisplayName(n: String) { put("nm_from", n) }

  def getEncoding = getString("tx_encoding", "UTF-8")

  def setEncoding(e: String) { put("tx_encoding", e) }
  
  def setContent(bya: Array[Byte]) { put ("by_content", bya) }
  
  def setLength(l: Int) { put ("len_mimemsg", new Integer(l)) }

  def getMD5() = getString("tx_md5")
  
  def setMD5(s: String) { put ("tx_md5", s) }

  def getWorkArea() = getString("gu_workarea")
  
  def setWorkArea(w: String) { put ("gu_workarea", w) }
  
  private def fillDefaults() =  {
    if (isNull("gu_mimemsg"))
      put ("gu_mimemsg", Uid.createUniqueKey())

    if (isNull("id_message"))
      put ("id_message", getString("gu_mimemsg"))

    if (isNull("gu_workarea"))
      put ("gu_workarea", workarea)
      
    if (isNull("pg_message"))
      put ("pg_message", new BigDecimal(dts.getSequence("seq_k_mime_messages").nextValue()))
  }

  def getParts : List[MailPart] = parts

  def createPart(txt: String, subType: String, idPart: Int) : MailPart = {
    fillDefaults()
    new HgMailPart (dts, this, txt, subType, idPart)
  }

  def createPart(fle: File, mimeType: String, disposition: String, idPart: Int) : MailPart = {
    fillDefaults()
    new HgMailPart (dts, this, fle, mimeType, disposition, idPart)
  }
  
  def addPart(p: MailPart) = {
    var tbl: Table = null
    try {
      tbl = dts.openTable(this)
      tbl.store(this)
      tbl.close()
      tbl = null
      tbl = dts.openTable(p)
      tbl.store(p)
      tbl.close()
      tbl = null
      parts.add(p)
    } finally {
      if (tbl!=null) tbl.close()
    }
  }

  @throws(classOf[JDOException])
  override def load (d: DataSource, guid: AnyRef) : Boolean = {
    if (!d.equals(dts))
        throw new JDOException("Supplied DataSource is not equal to the one used when instance was created")
    var retval = super.load(dts, guid)
    parts = new LinkedList[MailPart]
    if (retval) {
      var t: Table = null
      using(t) {
        val part = new HgMailPart(dts,this)
        t = dts.openTable(part)
        val parts : RecordSet[Record] = t.fetch(part.fetchGroup(), "gu_mimemsg", getGuid)
        asScalaBuffer(parts).foreach(r => parts.add(new HgMailPart(dts,this,r.getInteger("id_part"))))
      }
    } else {
    	parts = new LinkedList[MailPart]
    }
    retval
  }
  
  @throws(classOf[JDOException])
  override def store (d: DataSource) : Unit = {
    if (!d.equals(dts))
        throw new JDOException("Supplied DataSource is not equal to the one used when instance was created")

    fillDefaults()

    super.store(dts)
    
    var t: Table = null
    using(t) {
      t = dts.openTable(new HgMailPart(dts,this))
      parts.foreach(p => t.store(p))
    }    
  }
  
  @throws(classOf[JDOException])
  override def delete (d: DataSource)  = {
    if (!d.equals(dts))
        throw new JDOException("Supplied DataSource is not equal to the one used when instance was created")
    dts.call("k_sp_del_mime_msg", new Param("gu_msg", Types.CHAR, 1,getKey))
  }

  private def setBody(txt: String, subType: String) {
    val body = new MimeBodyPart()
    val strm = new ByteArrayOutputStream((txt.length()*2)+2)
    body.setText(txt, getEncoding, subType)
    body.writeTo(strm)
    val bya = strm.toByteArray()
    strm.close()
    setContent(bya)
    setLength(bya.length)
    
    val part = parts.find(p => p.getContentId==subType)
    if (part==None)
      parts += createPart (txt, subType, parts.size)
    else
      part.get.setText(txt)
  }

  def setPlainTextBody(txt: String) {
    setBody(txt,PLAIN)
  }

  def getPlainTextBody() : String = {
    val part = parts.find(p => p.getContentId==PLAIN)
    if (part==None) null else part.get.getText
  }
  
  def setHtmlBody(htm: String) {
    setBody(htm,HTML)    
  }

  def getHtmlBody() : String = {
    val part = parts.find(p => p.getContentId==HTML)
    if (part==None) null else part.get.getText
  }

  def setHtmlAndPlainBody(htm: String, txt: String) {
    val body = new MimeBodyPart()
    var alt  = new MimeMultipart("alternative")
    
    val html = new MimeBodyPart()
    html.setDisposition("inline")
    html.setText(htm, getEncoding, "html")
    html.setContentID(HTML)
    alt.addBodyPart(html)

    val plain = new MimeBodyPart()
    plain.setDisposition("inline")
    plain.setText(txt, getEncoding, "plain")
    plain.setContentID(PLAIN)
    alt.addBodyPart(plain)
    
    body.setContent(alt)
    
    val strm = new ByteArrayOutputStream()
    body.writeTo(strm)
    val bya = strm.toByteArray()
    setContent(bya)
    setLength(bya.length)
    strm.close()
              
    var part = parts.find(p => p.getContentId==HTML)
    if (part==None)
      parts.add(createPart (htm, HTML, parts.size))
    else
      part.get.setText(htm)

    part = parts.find(p => p.getContentId==PLAIN)
    if (part==None)
      parts.add(createPart (txt, PLAIN, parts.size))
    else
      part.get.setText(txt)
  }
   
}