package com.knowgate.bulkmailer.hipergate

import java.util.Map

import org.judal.storage.table.TableDataSource
import org.judal.storage.scala.ArrayRecord

import com.knowgate.bulkmailer.PENDING
import com.knowgate.bulkmailer.RUNNING
import com.knowgate.bulkmailer.ABORTED
import com.knowgate.bulkmailer.FINISHED
import com.knowgate.bulkmailer.SUSPENDED
import com.knowgate.bulkmailer.INTERRUPTED
import com.knowgate.bulkmailer.RoutineStatus
import com.knowgate.bulkmailer.ArchivedEmailMessage

class HgArchivedEmailMessage(dts: TableDataSource) extends ArrayRecord(dts,"k_job_atoms_archived") with ArchivedEmailMessage {

  def id = getInt("pg_atom")
  
  def setId(pg: Int) = { put("pg_atom", new Integer(pg)) }

  def jobId = getString("gu_job")
  
  def email = getString("tx_email")

  def companyId = getString("gu_company","")

  def contactId = getString("gu_contact","")

  def executionDate() = getDate("dt_execution")

  def firstName = getString("tx_name", "")

  def lastName = getString("tx_surname", "")

  def salutation = getString("tx_salutation", "")

  def intro = getString("tx_intro", "")
  
  def url = getString("tx_url", "")
  
  def getLog : String = getString("tx_log","")
  
  def setLog(l: String) = { put ("tx_log",l) }
  
  def status() : RoutineStatus = {
    if (isNull("id_status")) return null;
    getInt("id_status") match {
      case -1 => ABORTED
      case 0 => FINISHED
      case 1 => PENDING
      case 2 => SUSPENDED
      case 3 => RUNNING
      case 4 => INTERRUPTED
    }
  }
}