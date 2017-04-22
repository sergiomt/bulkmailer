package com.knowgate.bulkmailer.hipergate

import javax.jdo.JDOException

import org.judal.storage.scala.ArrayRecord
import org.judal.storage.TableDataSource
import org.judal.storage.RecordSet
import org.judal.storage.Table

import com.knowgate.bulkmailer.Using._
import com.knowgate.bulkmailer.MailerUser

class HgMailerUser(dts: TableDataSource) extends ArrayRecord(dts,"k_users") with MailerUser  {

	override def getId() = getString("gu_user")

	override def getDomainId() = getInt("id_domain")
	
	override def getNickname() = getString("tx_nickname","")

  @throws(classOf[JDOException])
	override def forWorkArea(dts: TableDataSource, workAreaId: String) = {
			val tbl = dts.openTable(this)
			var retval: Array[MailerUser] = null
			using (tbl) {
				val rst: RecordSet[MailerUser] = tbl.fetch(fetchGroup(), "gu_workarea", workAreaId, Int.MaxValue, 0)
		    retval = rst.toArray(new Array[MailerUser](rst.size()))
			}
			retval
	}

}
