package com.knowgate.bulkmailer.hipergate

import javax.jdo.JDOException

import javax.mail.Session
import javax.mail.Multipart
import javax.mail.Message.RecipientType

import com.sun.mail.smtp.SMTPMessage;

import com.knowgate.debug.Chronometer

import org.judal.storage.table.Table
import org.judal.storage.table.Record
import org.judal.storage.DataSource
import org.judal.storage.table.TableDataSource
import org.judal.storage.scala.ArrayRecord

import com.knowgate.bulkmailer.Using._
import com.knowgate.bulkmailer.Job
import com.knowgate.bulkmailer.Profiler
import com.knowgate.bulkmailer.Routine
import com.knowgate.bulkmailer.RoutineStatus
import com.knowgate.bulkmailer.PENDING
import com.knowgate.bulkmailer.RecType
import com.knowgate.bulkmailer.MimeSender
import com.knowgate.bulkmailer.SingleEmailAtom
import com.knowgate.bulkmailer.ArchivedEmailMessage

import collection.JavaConversions.asScalaBuffer

class HgSingleEmailAtom (dts: TableDataSource, proc: Routine, var rectype: RecipientType, var format: String, var status: RoutineStatus) extends ArrayRecord(dts,"k_job_atoms") with SingleEmailAtom {
  
  def this(dts: TableDataSource, proc: Routine, rec: Record) {
    this(dts, proc, RecType.valueOf(rec.getString("tp_recipient","TO")), rec.getString("id_format","HTML"), PENDING)
    if (null!=rec) {
      val keys = rec.fetchGroup.getMembers.iterator
      while (keys.hasNext) {
        val key = keys.next.asInstanceOf[String]
        put(key, rec.apply(key))
      }
    }
  }

  def routine = proc
    
  def id = getInt("pg_atom")

  def setId(pg: Int) = { put("pg_atom", new Integer(pg)) }

  def email = getString("tx_email")

  def companyId = getString("gu_company","")

  def contactId = getString("gu_contact","")

  def executionDate() = getDate("dt_execution")

  def firstName = getString("tx_name", "")

  def lastName = getString("tx_surname", "")

  def intro = getString("tx_intro", "")
  
  def salutation = getString("tx_salutation", "")
  
  def getLog : String = getString("tx_log","")
  
  def setLog(l: String) = { put ("tx_log",l) }

  def url = getString("tx_url", "")
  
  @throws(classOf[JDOException])
  override def store(d: DataSource) = {
    if (!d.equals(dts))
        throw new JDOException("Supplied DataSource is not equal to the one used when instance was created")
    if (isNull("gu_job"))
      put ("gu_job", routine.id)
    if (isNull("tp_recipient"))
      put("tp_recipient", "to")
    if (isNull("id_status"))
      put("id_status", new java.lang.Short( PENDING.shortValue))
    if (isNull("id_format"))
      put("id_format", "HTML")
    super.store(dts)
  }
  
  @throws(classOf[JDOException])
  override def send() = {
    routine.process(this)
  }

  def putAll(rec: Record) : Unit = {
    val keys = rec.fetchGroup.getMembers.iterator
    while (keys.hasNext) {
      val key = keys.next.asInstanceOf[String]
      put(key, rec.apply(key))
    }
  }
  
}