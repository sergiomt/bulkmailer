package com.knowgate.bulkmailer.hipergate

import javax.jdo.JDOException

import org.judal.storage.Table
import org.judal.storage.Record
import org.judal.storage.RecordSet
import org.judal.storage.TableDataSource

import com.knowgate.bulkmailer.Using._
import com.knowgate.bulkmailer.BlackList

import scala.collection.mutable.Buffer
import scala.collection.mutable.ArrayBuffer
import collection.JavaConversions._

@throws(classOf[JDOException])
class HgBlackList(dts: TableDataSource, domain: Int = 2051, var workarea: String = "00000000000000000000000000000000") extends BlackList {

  var addrs: Array[String] = null
  
  override def emails = addrs
  
  if (workarea==null) workarea = "00000000000000000000000000000000"
  if (workarea.length()==0) workarea = "00000000000000000000000000000000"

  @throws(classOf[JDOException])
  def add (email: String) {
    val blm = new HgBlackListedEmail(dts)
    blm.put("id_domain", new Integer(domain))
    blm.put("gu_workarea", workarea)
    blm.put("tx_email", email.toLowerCase().trim())
    blm.store(dts)
  }

  @throws(classOf[JDOException])
  def load () : BlackList = {
    val blm = new HgBlackListedEmail(dts)
    var tbl = dts.openTable(blm)
    using(tbl) {
      var rst : RecordSet[HgBlackListedEmail] = tbl.fetch(blm.fetchGroup(), "gu_workarea", workarea)
      rst.sort("tx_email")
      addrs = new Array[String](rst.size())
      var a = 1
      for (rec <- rst) {
        addrs(a) = rec.email()
        a += 1        
      }
    }
    this
  }
}