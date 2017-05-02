package com.knowgate.bulkmailer.hipergate

import java.util.Date

import java.sql.Types
import java.sql.Timestamp
import java.sql.Connection
import java.sql.SQLException
import java.sql.PreparedStatement

import javax.jdo.JDOException

import com.knowgate.debug.Chronometer

import org.judal.storage.DataSource
import org.judal.storage.table.TableDataSource

import com.knowgate.bulkmailer.Archive
import com.knowgate.bulkmailer.Close
import com.knowgate.bulkmailer.Profiler
import com.knowgate.bulkmailer.RoutineStatus
import com.knowgate.bulkmailer.SingleEmailAtom
import com.knowgate.bulkmailer.EmailAtomArchiver

class HgEmailAtomArchiverSameDb(dts: TableDataSource) extends EmailAtomArchiver {

  private val sqld = "DELETE FROM k_job_atoms WHERE gu_job=? AND pg_atom=?"
  private val sqli = "INSERT INTO k_job_atoms_archived (gu_job,pg_atom,dt_execution,id_status,gu_company,gu_contact,tx_email,tx_name,tx_surname,tx_salutation,tx_url,tx_intro,tx_log) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)"
    
  var con : Connection = null
  var del : PreparedStatement = null
  var ins : PreparedStatement = null

  open
  
  @throws(classOf[JDOException])
  @throws(classOf[IllegalStateException])
  private def archiveWithoutRetry (sea: SingleEmailAtom, sts: RoutineStatus, txtlog: String) = {
    val  now = new Timestamp(new Date().getTime())
    con.setAutoCommit(false)
    try {
      ins.setString(1, sea.routine.id)
      ins.setInt(2, sea.id)
      ins.setTimestamp(3, now)
      ins.setShort (4, sts.shortValue)
      ins.setString(5, sea.companyId)
      ins.setString(6, sea.contactId)
      ins.setString(7, sea.email)
      ins.setString(8, sea.firstName)
      ins.setString(9, sea.lastName)
      ins.setString(10, sea.salutation)
      ins.setString(11, sea.url)
      ins.setString(12, sea.intro)
      if (txtlog==null)
        ins.setNull(13, Types.VARCHAR)
      else
        ins.setString(13, if (txtlog.length<=254) txtlog else txtlog.substring(0, 254))
      ins.executeUpdate()
      del.setString(1, sea.routine.id)
      del.setInt(2, sea.id)
      del.executeUpdate()
      con.commit()
    } catch {
      case sqle: SQLException => {
        if (con!=null) if (!con.isClosed()) con.rollback()
        if (sqle.getSQLState()=="23505")
          throw new IllegalStateException(sqle.getMessage())
        else  
          throw new JDOException(sqle.getMessage(),sqle)
      }
    }
  }

  @throws(classOf[JDOException])
  private def reconnectAndRetry (sea: SingleEmailAtom, sts: RoutineStatus, txtlog: String) = {
    close
    open
    archiveWithoutRetry (sea,sts,txtlog)
  }
  
  @throws(classOf[JDOException])
  @throws(classOf[IllegalStateException])
  def archive (sea: SingleEmailAtom, sts: RoutineStatus, txtlog: String) = {
  	this.synchronized {
      var meter : Chronometer = null
      if (Profiler.enabled) meter = new Chronometer()
      try {
        archiveWithoutRetry (sea, sts, txtlog)
      } catch {
        case stge: JDOException => {
          reconnectAndRetry(sea,sts,txtlog)
        }
      }
      if (Profiler.enabled) Profiler.totalAtomsArchivingTime += meter.stop
  	}
  }

  private def open : EmailAtomArchiver = {
    con = dts.getJdoConnection.getNativeConnection.asInstanceOf[Connection]
    del = con.prepareStatement(sqld)
    ins = con.prepareStatement(sqli)
    this
  }
  
  def close: Unit = {
    try { if (ins!=null) ins.close } catch { case sqle: SQLException => Unit }
    try { if (del!=null) del.close } catch { case sqle: SQLException => Unit }
    try { if (con!=null) if (!con.isClosed()) con.close } catch { case sqle: SQLException => Unit }
    ins=null
    del=null
    con=null
  }

 
}