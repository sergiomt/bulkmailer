package com.knowgate.bulkmailer.hipergate

import java.util.AbstractMap.SimpleImmutableEntry

import javax.jdo.JDOException
import javax.jdo.JDOUserException

import org.judal.metadata.JoinType
import org.judal.metadata.NameAlias.AS

import org.judal.storage.Param
import org.judal.storage.DataSource
import org.judal.storage.table.Record
import org.judal.storage.table.RecordSet
import org.judal.storage.table.Table
import org.judal.storage.table.ColumnGroup
import org.judal.storage.table.TableDataSource
import org.judal.storage.scala.ArrayRecord
import org.judal.storage.query.Operator
import org.judal.storage.query.Connective
import org.judal.storage.relational.RelationalView

import com.knowgate.stringutils.Uid
import com.knowgate.tuples.Pair

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
    val job = new HgJob(dts, new HashMap[String,String])
    val clk = new HgClickThrough(dts)
    var clks : RecordSet[HgClickThrough] = null
    val fetchCols = new ColumnGroup("c.gu_company","c.gu_contact","c.tx_email","c.dt_action","c.pg_atom","j.tl_job","j.gu_job")
    
    val clicks = dts.openJoinView(JoinType.INNER, clk, AS(clk.getTableName,"c"), AS(job.getTableName,"j"), new Pair[String,String]("gu_job","gu_job"))
    using (clicks) {
      clks = clicks.fetch(fetchCols, Integer.MAX_VALUE, 0, new Param("j.gu_workarea", 1, getWorkarea), new Param("c.gu_url", 2, getGuid))
    }
    val clt = new Array[ClickThrough](clks.size)
    clks.toArray(clt)
    clt
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