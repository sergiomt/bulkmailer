package com.knowgate.bulkmailer.hipergate

import java.io.File

import java.nio.file.Files
import java.nio.file.Paths

import javax.jdo.JDOException

import org.judal.storage.Table
import org.judal.storage.Record
import org.judal.storage.TableDataSource
import org.judal.storage.scala.ArrayRecord

import com.knowgate.bulkmailer.Using._
import com.knowgate.bulkmailer.MailPart
import com.knowgate.bulkmailer.MailMessage

class HgMailPart(dts: TableDataSource, val message: MailMessage) extends ArrayRecord(dts,"k_mime_parts") with MailPart {

  val ENCODING = "UTF-8"

  put("gu_mimemsg", message.getGuid)
  put("id_message", message.getId)
  if (!message.isNull("pg_message"))
    put("pg_message", message.getDecimal("pg_message"))
    
  def this (dts: TableDataSource, message: MailMessage, idPart: Integer) {
    this(dts, message)
    var tbl: Table = null
    var pk: Array[Object] = Array(message.getGuid, idPart)
    using(tbl) {
      tbl = dts.openTable(this)
      tbl.load(pk, this)
    }
  }

  def this (dts: TableDataSource, message: MailMessage, txt: String, subType: String, idPart: Int) {
    this(dts, message)
    setText(txt)
    setContentId(subType)
    put("id_part", new Integer(idPart))
    put("id_type", "text/"+subType)
    put("id_disposition", "inline")
    put("id_encoding", ENCODING)
  }

  def this (dts: TableDataSource, message: MailMessage, fle: File, mimeType: String, disposition: String, idPart: Int) {
    this(dts, message)
    if (disposition!="reference")
      setBytes(Files.readAllBytes(Paths.get(fle.getAbsolutePath())))
    setContentId(fle.getName())
    put("id_part", new Integer(idPart))
    put("id_type", mimeType)
    put("id_disposition", disposition)
    put("file_name", fle.getName())
  }

  def putAll(rec: Record) : Unit = {
    val keys = rec.fetchGroup.getMembers.iterator
    while (keys.hasNext) {
      val key = keys.next.asInstanceOf[String]
      put(key, rec.apply(key))
    }
  }

  def id = getInt("id_part")
  
  def getContentId = getString("id_content")

  def setContentId(str: String) { put("id_content", str) }

  def getDisposition = getString("id_disposition")

  def getMimeType : String = getString("id_type","")

  def getFileName = getString("file_name")
  
  def getBytes : Array[Byte] = {
    if (isNull("by_content"))
      null
    else
      getBytes("by_content")
  }
  
  def setBytes(bya: Array[Byte]) = {
    put("by_content", bya)
    put("len_part", new Integer(bya.length))
  }
  
  def getText : String = {

    if (isNull("by_content"))
      null
    else
      new String(getBytes,ENCODING)
  }

  override def setText(txt: String) {
    setBytes(txt.getBytes(ENCODING))
  }
  
}