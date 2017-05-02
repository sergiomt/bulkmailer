package com.knowgate.bulkmailer.hipergate

import java.sql.Connection
import java.sql.SQLException
import java.sql.PreparedStatement

import org.judal.jdbc.jdc.JDCConnection
import org.judal.jdbc.jdc.JDCConnectionPool

import org.judal.storage.table.TableDataSource

class SingleStatementConnectionHandler(val dts: TableDataSource, val sql: String) extends AutoCloseable {
    
    private val pol = dts.asInstanceOf[JDCConnectionPool]
    private var con : Connection = null
    private var stm : PreparedStatement = null

    open
    
    def connection = con

    def statement = stm
    
    @throws(classOf[SQLException])
    private def open = {
      con = dts.getJdoConnection.getNativeConnection.asInstanceOf[Connection]
      con.setAutoCommit(true)
      stm = con.prepareStatement(sql)
    }
  
    def close: Unit = {
      try { if (stm!=null) stm.close } catch { case sqle: SQLException => Unit }
      try { if (con!=null) if (!con.isClosed()) con.close } catch { case sqle: SQLException => Unit }
      stm=null
      con=null
    }

    def isClosed = (con==null || stm==null)

    @throws(classOf[SQLException])
    def reconnect = {
      close
      open
    }

    def getDataSource() = dts

}
