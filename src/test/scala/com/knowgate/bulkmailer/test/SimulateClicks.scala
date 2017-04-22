package com.knowgate.bulkmailer.test

import java.util.Random
import java.util.HashMap

import org.judal.storage.TableDataSource
import org.judal.storage.Record
import org.judal.storage.RecordSet

import com.knowgate.stringutils.Uid
import com.knowgate.bulkmailer.ClickThrough
import com.knowgate.bulkmailer.Job
import com.knowgate.bulkmailer.Tracker
import com.knowgate.bulkmailer.Factory

import scala.collection.mutable.ArrayBuffer

class SimulateClicks(factry: Factory, dts: TableDataSource, domain: Int, workarea: String, writer: String) extends TestCase {

  val maxUrls = 20
  
  var urls = new ArrayBuffer[String]

  for (u <-0 until maxUrls)
    urls += "http://www."+Uid.generateRandomId(20, null, Character.LOWERCASE_LETTER)+"/"+Uid.generateRandomId(10, null, Character.LOWERCASE_LETTER)+".htm"
  
  val rwmails = new ReadWriteMails(factry, domain, workarea, writer)
  val rwjobs = new ReadWriteJobs(factry, domain, workarea, writer)
  
  class ClickWriter(t: Tracker, j: Job) extends Thread {
    override def run() = {
      val rnd = new Random()
      val atms = j.atoms()
      for (a <- 0 until atms.length) {
        val surl = urls(rnd.nextInt(maxUrls))
        println("simulating click in "+surl+" by "+atms(a).email()+" for atom "+String.valueOf(atms(a).id()))
        t.track(j.id(), atms(a).id(), null, null, "127.0.0.1", atms(a).email(), surl, null)
      }
    }
  }
  
  def trackClicks(d: String) = {
    val p = new HashMap[String,String]();
    p.put("attachimages", "false")
    
    val t = factry.getClickTracker(dts, workarea)
    
    val m1 = rwmails.writeTextEmails(d)
    val j1 = rwjobs.writeJob("Clicks test Job 1", m1.getGuid(), p)
    rwjobs.writeMessages(j1)
    val c1 = new ClickWriter(t, j1)
    
    val m2 = rwmails.writeTextEmails(d)
    val j2 = rwjobs.writeJob("Clicks test Job 2", m2.getGuid(), p)    
    rwjobs.writeMessages(j2)
    val c2 = new ClickWriter(t, j2)

    val m3 = rwmails.writeTextEmails(d)
    val j3 = rwjobs.writeJob("Clicks test Job 3", m3.getGuid(), p)
    rwjobs.writeMessages(j3)
    val c3 = new ClickWriter(t, j3)

    val m4 = rwmails.writeTextEmails(d)
    val j4 = rwjobs.writeJob("Clicks test Job 4", m4.getGuid(), p)
    rwjobs.writeMessages(j4)
    val c4 = new ClickWriter(t, j4)
    
    val m5 = rwmails.writeTextEmails(d)
    val j5 = rwjobs.writeJob("Clicks test Job 5", m4.getGuid(), p)
    rwjobs.writeMessages(j5)
    val c5 = new ClickWriter(t, j5)
    
    c1.start()
    c2.start()
    c3.start()
    c4.start()
    c5.start()

    println("Waiting for clicker threads to complete")
    
    List(c1,c2,c3,c4,c5).foreach(c => c.join())
    
    println("Clicker threads completed")
    
    t.close()
    
  }  

}