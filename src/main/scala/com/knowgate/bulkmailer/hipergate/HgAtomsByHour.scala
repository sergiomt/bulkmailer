package com.knowgate.bulkmailer.hipergate

import org.judal.storage.table.TableDataSource
import org.judal.storage.scala.ArrayRecord

class HgAtomsByHour(dts: TableDataSource) extends ArrayRecord(dts,"k_jobs_atoms_by_hour") {
 
  	 def getJobId() : String = getString("gu_job")

  	 def getJobGroup() : String = getString("gu_job_group")
  	 
  	 def getHour() : Short = getShort("dt_hour")

  	 def getMessageCount() : Int = getInt("nu_msgs")

}