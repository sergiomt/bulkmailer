package com.knowgate.bulkmailer.hipergate

import java.util.Date

import com.knowgate.bulkmailer.TotalAtomsByDay

import org.judal.storage.TableDataSource
import org.judal.storage.scala.ArrayRecord

class HgTotalAtomsByDay(dts: TableDataSource) extends ArrayRecord(dts, "k_jobs_atoms_by_day", "nu_msgs", "dt_execution" ) with TotalAtomsByDay {

  	 def getDate() : Date = getDate("dt_execution")

  	 def getMessageCount() : Int = getInt("nu_msgs")

}