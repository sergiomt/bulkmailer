package com.knowgate.bulkmailer.hipergate

import javax.jdo.JDOException

import org.judal.storage.table.Table
import org.judal.storage.table.Record
import org.judal.storage.table.RecordSet
import org.judal.storage.table.TableDataSource

import com.knowgate.bulkmailer.Using._
import com.knowgate.bulkmailer.ListMembers

class HgListMembers (dts: TableDataSource, lst: String) extends ListMembers {

  var mbrs : Array[Record] = null

  val mbr = new HgListMember(dts)
  val tbl = dts.openTable(mbr)
  
  using(tbl) {
    val rst : RecordSet[Record] = tbl.fetch(mbr.fetchGroup, "gu_list", lst)
    rst.sort("tx_email")
    mbrs = rst.toArray(new Array[Record](rst.size()));
  }
  
  def members = mbrs

  private def getMemberByEmail(email: String, imin: Int, imax: Int) : Record = {
    if (imax < imin) {
      null
    } else {
      val imid = imin + ((imax - imin) / 2);
      val emailn = mbrs(imid).getString("tx_email")
      if (emailn > email)
        getMemberByEmail(email, imin, imid-1);
      else if (emailn < email)
        getMemberByEmail(email, imid+1, imax);
      else
        mbrs(imid)
    }
  }
  
  override def getMemberByEmail(email: String) : Record = getMemberByEmail(email, 0, mbrs.size-1)
  
}