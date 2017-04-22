package com.knowgate.bulkmailer.hipergate

import java.util.Date

import java.sql.Types
import java.sql.Timestamp

import java.util.concurrent.TimeUnit
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.LinkedBlockingQueue

import javax.jdo.JDOException

import org.judal.storage.TableDataSource

import com.knowgate.bulkmailer.Log
import com.knowgate.bulkmailer.Close
import com.knowgate.bulkmailer.Tracker

class HgBeaconTrackerDb(dts: TableDataSource) extends Tracker {

  class BeaconInsertor(cnh: SingleStatementConnectionHandler, job: String, atm: Int, company: String, contact: String, ip: String, email: String, userAgent: String) extends Runnable {
    def run() = {
      val stm = cnh.statement    
      if (cnh.isClosed) throw new IllegalStateException("ClickInsertor.run() ConnectionHandler is closed")
      stm.setString(1, job)
      stm.setInt(2, atm)
      if (null==company)
        stm.setNull(3, Types.CHAR);
      else
        stm.setString(3, company);
      if (null==contact)
        stm.setNull(4, Types.CHAR);
      else
        stm.setString(4, contact);
      if (null==ip)
        stm.setNull(5, Types.VARCHAR);
      else
        stm.setString(5, ip);
      if (null==email)
        stm.setNull(6, Types.VARCHAR);
      else
        stm.setString(6, email);
      if (userAgent==null)
        stm.setNull(7, Types.VARCHAR)
      else if (userAgent.length()<=254)
        stm.setString(7, userAgent);
      else
        stm.setString(7, userAgent.substring(0, 254));
      stm.executeUpdate();      
    }
  }

  private val sqli = "INSERT INTO k_job_atoms_tracking (gu_job,pg_atom,gu_company,gu_contact,ip_addr,tx_email,user_agent) VALUES (?,?,?,?,?,?,?)"
  
  private val chi = new SingleStatementConnectionHandler(dts, sqli)
  private val exi = new SingleStatementExecutor(chi)

  @throws(classOf[JDOException])
  def track (job: String, atm: Int, company: String, contact: String, ip: String, email: String, addr: String, userAgent: String) = {
  	val cli = new BeaconInsertor(chi, job, atm, company, contact, ip, email, userAgent)
  	exi.execute(cli)
  }
 
  def close : Unit = {
    exi.shutdown()
  }
  
}