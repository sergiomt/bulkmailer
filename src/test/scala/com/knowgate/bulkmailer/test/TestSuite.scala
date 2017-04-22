package com.knowgate.bulkmailer.test

import java.util.Date
import java.util.HashMap
import java.text.SimpleDateFormat

import org.judal.storage.Engine
import org.judal.storage.RecordManager

import com.knowgate.bulkmailer.Using._
import com.knowgate.bulkmailer.Factory

import scala.reflect.runtime.universe
import scala.collection.mutable.ArrayBuffer

object TestSuite {

  def main(args: Array[String]): Unit = {
    val now = new Date()
    val fmt = new SimpleDateFormat("yyyyMMddHHmmss")
    val ymd = fmt.format(now)
    val domain = 2049
    val workarea = "c0a8010d142761fa5c8100000f471f07"
    val writer = "c0a8010d142761fa5e7100001f3d36d7"
    val tempdir = "C:\\Temp\\Debug\\"    

    val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)
    val module = runtimeMirror.staticModule("com.knowgate.bulkmailer.hipergate.HgFactory")
    val obj = runtimeMirror.reflectModule(module)
    val factry = obj.instance.asInstanceOf[Factory]

    val cdm = new CreateDataModel
    
    cdm.create
    
    val p = new HashMap[String,String]();
    
    /*
    val dts = TestDAO.datasource
    
    val rcp = factry.newRecipientData(dts, "ac1263a41235762fe5b1000c49e09610", "sergiom@knowgate.com")
    
    val lsts = rcp.lists()
  
    println(String.valueOf(lsts.length))
    */
    
      /*
      var t = new ReadWriteJobs(factry, domain, workarea, writer)
      var j = t.writeJob("Test Job"+ymd, null, p)
      t.writeBlackList
      t.writeMessages(j)
      
      var m = new ReadWriteMails(factry, domain, workarea, writer)
      var e = m.writeTextEmails(tempdir)
      m.addImagesToEmail(e, tempdir)

      var r = new ArrayBuffer[String]()
      r += "smontoroten@gmail.com"
      r += "sergio.montoro@knowgate.es"
      r += "sergiom@knowgate.es"
      r += "sergiom@knowgate.com"
      r += "paulnts@yahoo.es"

      var s = new SendEmailUsingJob(factry, domain, workarea, writer)
      p.put("attachimages", "true")
      s.send(r, p, true)

      s = new SendEmailUsingJob(factry, domain, workarea, writer)
      p.put("attachimages", "false")
      s.send(r, p, true)
      
      var c = new SimulateClicks(factry, TestDAO.datasource, domain, workarea, writer)
      c.trackClicks(tempdir)
      val idl = new CreateDataModel
      idl.create
      var c = new SimulateClicks(factry, TestDAO.datasource, domain, workarea, writer)
      c.checkClicks()
      */

  }

}