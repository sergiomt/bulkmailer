package com.knowgate.bulkmailer.hipergate

import java.util.AbstractMap.SimpleImmutableEntry

import javax.jdo.JDOException
import javax.jdo.JDOUserException

import org.judal.storage.Record
import org.judal.storage.RecordSet
import org.judal.storage.Table
import org.judal.storage.RelationalView
import org.judal.storage.DataSource
import org.judal.storage.TableDataSource
import org.judal.storage.scala.ArrayRecord
import org.judal.storage.query.Operator
import org.judal.storage.query.Connective
import com.knowgate.stringutils.Uid

import com.knowgate.bulkmailer.UrlData
import com.knowgate.bulkmailer.ClickThrough
import com.knowgate.bulkmailer.Using._

import scala.collection.mutable.HashMap

import scala.collection.JavaConversions.asScalaBuffer

class HgUrlData(dts: TableDataSource) extends ArrayRecord(dts,"k_urls") with UrlData {

	def getGuid() = getString("gu_url")
	
	def setGuid(g: String) = { put("gu_url", g) }

	def getClicks() = if (isNull("nu_clicks")) 0 else getInt("nu_clicks")
	
	def setClicks(n: Int) = { put("nu_clicks", new java.lang.Integer(n)) }

	def getWorkarea() = getString("gu_workarea")

	def setWorkarea(w: String) = { put("gu_workarea", w) }
	
	def getAddress() = getString("url_addr")

	def setAddress(a: String) = {
	  if (a==null) throw new NullPointerException("HgUrlData.setAddress() URL address cannot be null")
	  val maxLength = getTableDef.getColumnByName("url_addr").getLength
	  put("url_addr", if (a.length<=maxLength) a else a.substring(0,maxLength))
	}
	
	def getTitle() = getString("tx_title","")

	def setTitle(t: String) = {
	  val maxLength = getTableDef.getColumnByName("tx_title").getLength
	  if (null!=t)
	    put("tx_title", if (t.length<=maxLength) t else t.substring(0,maxLength))
	  else
	    remove("tx_title")
	}
	
  @throws(classOf[JDOException])
	def clickThrough() : Array[ClickThrough] = {
    var clt: Array[ClickThrough] = new Array[ClickThrough](0)
    val job = new HgJob(dts, new HashMap[String,String])
    val clk = new HgClickThrough(dts)
    var clks : RecordSet[HgClickThrough] = null
    
    val clicks = dts.openInnerJoinView(job, clk.getTableName+" c", new SimpleImmutableEntry[String,String]("gu_job","gu_job")).asInstanceOf[RelationalView]
    using (clicks) {
      val qry = clicks.newQuery
      qry.setResultClass(clk.getClass, dts.getClass)
      qry.setResult("c.gu_company,c.gu_contact,c.tx_email,c.dt_action,c.pg_atom,j.tl_job,j.gu_job")
      val where = qry.newPredicate(Connective.AND)
      where.add("j.gu_workarea", Operator.EQ, getWorkarea)
      where.add("c.gu_url", Operator.EQ, getGuid)
      clks = clicks.fetch(qry)
    }
    asScalaBuffer(clks).toArray[ClickThrough]
  }

  @throws(classOf[JDOUserException])
  override def store(d: DataSource) : Unit = {
    if (!d.equals(dts))
        throw new JDOException("Supplied DataSource is not equal to the one used when instance was created")
    if (isNull("gu_url"))
      setGuid(Uid.createUniqueKey())
    super.store(dts)
  }
}