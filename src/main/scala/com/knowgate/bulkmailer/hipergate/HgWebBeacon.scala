package com.knowgate.bulkmailer.hipergate

import java.util.Map

import com.knowgate.bulkmailer.WebBeacon

import org.judal.storage.table.Record
import org.judal.storage.scala.ArrayRecord
import org.judal.storage.table.TableDataSource

class HgWebBeacon(dts: TableDataSource) extends ArrayRecord(dts, "k_job_atoms_tracking") with WebBeacon {

	def jobId() = getString("gu_job")

	def atomId() = getInt("pg_atom")
	
	def ip() = getString("ip_addr")

	def date() = getDate("dt_action")

	def email() = getString("tx_email")
		
	def userAgent() = getString("user_agent","")
 
  def putAll(rec: Record) = {
	   val keys = rec.fetchGroup.getMembers.iterator
	   while (keys.hasNext) {
	     val key = keys.next.asInstanceOf[String]
	     put(key, rec.apply(key))
	   }
	}
	
}