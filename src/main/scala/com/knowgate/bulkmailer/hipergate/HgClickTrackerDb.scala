package com.knowgate.bulkmailer.hipergate

import java.util.Map
import java.util.Date

import java.sql.Types
import java.sql.Timestamp

import java.util.concurrent.TimeUnit
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.LinkedBlockingQueue

import javax.jdo.JDOException

import org.judal.storage.RelationalDataSource

import com.knowgate.bulkmailer.Log
import com.knowgate.bulkmailer.Urls
import com.knowgate.bulkmailer.Close
import com.knowgate.bulkmailer.Tracker

class HgClickTrackerDb(dts: RelationalDataSource, workarea: String) extends Tracker {
  
  class ClickInsertor(cnh: SingleStatementConnectionHandler, job: String, atm: Int, company: String, contact: String, ip: String, email: String, guid: String) extends Runnable {
    def run() = {
      val stm = cnh.statement    
      if (cnh.isClosed) throw new IllegalStateException("ClickInsertor.run() ConnectionHandler is closed")
  	  stm.setString(1, job);
      stm.setInt(2, atm);
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
      stm.setString(7, guid);
      stm.executeUpdate();
    }
  }

  class CounterUpdater(cnh: SingleStatementConnectionHandler, guid: String) extends Runnable {
    def run() = {
      val stm = cnh.statement    
      stm.setTimestamp(1, new Timestamp(new Date().getTime()))
      stm.setString(2, guid)
      stm.executeUpdate()
    }
  }
  
  private val sqli = "INSERT INTO k_job_atoms_clicks (gu_job,pg_atom,gu_company,gu_contact,ip_addr,tx_email,gu_url) VALUES (?,?,?,?,?,?,?)"
  private val sqlu = "UPDATE k_urls SET nu_clicks=nu_clicks+1,dt_last_visit=? WHERE gu_url=?"
  
  private val chi = new SingleStatementConnectionHandler(dts, sqli)
  private val chu = new SingleStatementConnectionHandler(dts, sqlu)
  private val exi = new SingleStatementExecutor(chi)
  private val exu = new SingleStatementExecutor(chu)
  private val cch = new Urls(dts, workarea, new HgUrlData(dts), new HgClickThrough(dts))
  
  @throws(classOf[JDOException])
  def track (job: String, atm: Int, company: String, contact: String, ip: String, email: String, addr: String, userAgent: String) = {
  	val guid = cch.getGuidForAddress(addr, workarea)
  	val cli = new ClickInsertor(chi, job, atm, company, contact, ip, email, guid)
  	val clu = new CounterUpdater(chu, guid)
  	exi.execute(cli)
  	exu.execute(clu)
  }
 
  def close : Unit = {
    exi.shutdown()
    exu.shutdown()
  }

}