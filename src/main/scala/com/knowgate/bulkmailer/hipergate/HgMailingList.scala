package com.knowgate.bulkmailer.hipergate

import java.util.Map

import javax.jdo.JDOException

import org.judal.storage.table.Table
import org.judal.storage.table.Record
import org.judal.storage.table.TableDataSource
import org.judal.storage.scala.ArrayRecord

import com.knowgate.bulkmailer.MailingList
import com.knowgate.bulkmailer.RecipientData

class HgMailingList(dts: TableDataSource) extends ArrayRecord(dts,"k_lists") with MailingList {

   var mbrs : HgListMembers = null

   def this(dts: TableDataSource, rec: Record) = {
     this(dts)
     putAll(rec)
   }

	 def getId() = getString("gu_list")

	 def setId(id: String) = { put ("gu_list", id) } 

	 def getWorkarea() = getString("gu_workarea")

	 def setWorkarea(wrka: String) = { put ("gu_workarea", wrka) }

	 def getType() = getInt("tp_list").shortValue

	 def setType(tp: Short) = { put ("tp_list", new java.lang.Short(tp)) }

	 def getDescription() = getString("de_list","")

	 def setDescription(desc: String) = { put ("de_list", desc) }
	 
  def putAll(rec: Record) = {
	   val keys = rec.fetchGroup.getMembers.iterator
	   while (keys.hasNext) {
	     val key = keys.next.asInstanceOf[String]
	     put(key, rec.apply(key))
	   }
	}

  @throws(classOf[JDOException])
	 def activeMembers() = {
	   if (null==mbrs)
	     mbrs = new HgListMembers (dts, getId())
	   mbrs
	 }

}