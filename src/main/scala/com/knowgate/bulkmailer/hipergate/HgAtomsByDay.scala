package com.knowgate.bulkmailer.hipergate

import java.util.Date
import java.text.ParseException;
import java.text.SimpleDateFormat

import org.judal.storage.TableDataSource
import org.judal.storage.scala.ArrayRecord

class HgAtomsByDay(dts: TableDataSource) extends ArrayRecord(dts,"k_jobs_atoms_by_day") {

  	 def getJobId() : String = getString("gu_job")

  	 def getJobGroup() : String = getString("gu_job_group")

     @throws(classOf[ParseException])
  	 def getDate() : Date = new SimpleDateFormat("yyyy-MM-dd").parse(getString("dt_execution")) 

  	 def getMessageCount() : Int = getInt("nu_msgs")

}