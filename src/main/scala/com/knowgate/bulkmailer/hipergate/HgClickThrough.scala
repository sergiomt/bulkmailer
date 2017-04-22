package com.knowgate.bulkmailer.hipergate

import java.util.Map
import java.util.Date

import org.judal.storage.scala.ArrayRecord
import org.judal.storage.TableDataSource

import com.knowgate.bulkmailer.ClickThrough

class HgClickThrough(dts: TableDataSource) extends ArrayRecord(dts, "k_job_atoms_clicks") with ClickThrough {

  def ip() = getString("ip_addr","")

	def date() = getDate("dt_action")

	def email() = getString("tx_email")
	
	def url() = getString("tx_url")
	
	def urlId() = getString("gu_url")
	
	def title() = getString("tx_title")
	
	def jobId() = getString("gu_job")

	def atomId() = getInt("pg_atom")

	def setUrl(url: String) = put("tx_url", url)
	
}