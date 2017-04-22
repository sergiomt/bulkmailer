package com.knowgate.bulkmailer.hipergate

import java.sql.Connection
import java.sql.ResultSet
import java.sql.PreparedStatement

import javax.jdo.JDOException

import org.judal.storage.DataSource

import com.knowgate.bulkmailer.SumCounter

class HgGroupSumCounter extends HgSumCounter with AutoCloseable {
  
  private var id : String = null
  
  private var con : Connection = null
  private var stm : PreparedStatement = null

  def getId() = id
  
  def sum (sc: SumCounter) : Unit = {
  	sumCountersForJobGroup(sc.getId())
    sc.setSentCount(sentCount())
    sc.setOpenedCount(openedCount())
    sc.setClicksCount(clicksCount())
    sc.setUniqueCount(uniqueCount())
  }

  @throws(classOf[JDOException])
  @throws(classOf[IllegalStateException])
  def open(dts: DataSource) : HgGroupSumCounter = {
    if (null==con) {
      con = dts.getJdoConnection.getNativeConnection.asInstanceOf[Connection]
		  stm = con.prepareStatement("SELECT SUM(nu_sent),SUM(nu_opened),SUM(nu_clicks) FROM k_jobs WHERE gu_job_group=?")      
    } else {
      throw new IllegalStateException("HgGroupSumCounter is already open")
    }
    this
  }
  
  @throws(classOf[JDOException])
  @throws(classOf[IllegalStateException])
  private def sumCountersForJobGroup (group: String) : Unit = {
    id = group
    if (null==con)
      throw new IllegalStateException("HgGroupSumCounter must be opened before calling sum() method")
    stm.setString(1, group)
    var rst = stm.executeQuery()
		rst.next()
		setSentCount(rst.getInt(1))
		setOpenedCount(rst.getInt(2))
		setClicksCount(rst.getInt(3))		
		rst.close()
  }

  def close() = {
    if (stm!=null) stm.close()
    if (con!=null) { if (!con.isClosed()) con.close() }    
  }
}