package com.knowgate.bulkmailer.hipergate

import java.util.Map
import java.util.Date

import org.judal.storage.table.Table
import org.judal.storage.table.TableDataSource
import org.judal.storage.scala.ArrayRecord

import com.knowgate.bulkmailer.BlackListedEmail

class HgBlackListedEmail(dts: TableDataSource) extends ArrayRecord(dts,"k_global_black_list") with BlackListedEmail {

  def email = getString("tx_email")

  def workarea = getString("gu_workarea")
  
  def since : Date = getDate("dt_created")
  
}
