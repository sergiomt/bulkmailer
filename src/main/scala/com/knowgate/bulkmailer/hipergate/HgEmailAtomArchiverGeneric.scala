package com.knowgate.bulkmailer.hipergate

import com.knowgate.debug.Chronometer

import javax.jdo.JDOException

import org.judal.storage.Table
import org.judal.storage.Record
import org.judal.storage.TableDataSource

import com.knowgate.bulkmailer.Profiler
import com.knowgate.bulkmailer.RoutineStatus
import com.knowgate.bulkmailer.SingleEmailAtom
import com.knowgate.bulkmailer.EmailAtomArchiver

import collection.JavaConversions.asScalaBuffer
import collection.JavaConversions.collectionAsScalaIterable

class HgEmailAtomArchiverGeneric(dts: TableDataSource, dt2: TableDataSource) extends EmailAtomArchiver {

  @throws(classOf[JDOException])
  def archive (sea: SingleEmailAtom, sts: RoutineStatus, txtlog: String) = {
    var meter : Chronometer = null
    if (Profiler.enabled) meter = new Chronometer()

    var tbl: Table = null
    val aem = new HgArchivedEmailMessage(dts)
    val cols = aem.columns().map(_.getName())
    for (col <- cols if !sea.isNull(col))
      aem.put(col, sea.apply(col))
    aem.put("id_status", new java.lang.Short(sts.shortValue))
    if (txtlog!=null)
      aem.put("tx_log", if (txtlog.length()<=254) txtlog else txtlog.substring(0, 254))
    try {
      tbl = dt2.openTable(aem)
      tbl.store(aem)
      tbl.close()
      tbl = null
      tbl = dts.openTable(sea)
      tbl.delete(sea.getKey)
      tbl.close()
      tbl = null
    } finally {
      if (tbl!=null) tbl.close()
      if (Profiler.enabled) Profiler.totalAtomsArchivingTime += meter.stop
    }
  }

  def close: Unit = {
  }
}