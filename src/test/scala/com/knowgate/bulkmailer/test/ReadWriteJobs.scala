package com.knowgate.bulkmailer.test

import java.util.Map
import javax.mail.Message.RecipientType

import com.knowgate.bulkmailer.Job
import com.knowgate.bulkmailer.PENDING
import com.knowgate.bulkmailer.Factory
import org.judal.storage.DataSource

import scala.collection.mutable.ArrayBuffer

class ReadWriteJobs(factry: Factory, domain: Int, workarea: String, writer: String) extends TestCase {

  def writeJob(title: String, msgid: String, params: Map[String,String]) : Job = {
    println("Instantiating job "+title)
    var j = factry.newJob(TestDAO.datasource, TestDAO.properties)
    j.setCommand(Job.COMMAND_SEND)
    j.setMailMessageId(msgid)
    j.setParameters(params)
    j.setStatus(PENDING)
    j.setTitle(title)
    j.setWorkarea(workarea)
    j.setWriter(writer)
    println("Storing job")
    j.store(TestDAO.datasource)
    val id = j.id
    println("Loading job")
    j = factry.loadJob(TestDAO.datasource, TestDAO.properties, id)
    assert(j!=null, "Could not reload job "+id)
    assert(j.id==id, "Job not properly readed")
    assert(j.title==title, "Job not properly written")
    println("Reloaded job "+j.title)
    j
  }

  def writeBlackList() {    
    var blck = factry.getBlackList(TestDAO.datasource, domain, workarea)
    for (r <- 10 until 20)
      blck.add("blacklisted"+String.valueOf(r)+"@testdomain.com")
    blck = factry.getBlackList(TestDAO.datasource, domain, workarea).load
    for (r <- 10 until 20)
      assert(blck.contains("blacklisted"+String.valueOf(r)+"@testdomain.com"), "Black List not properly written")
  }

  def writeMessages(j: Job) {
    var recs = new ArrayBuffer[String]()
    for (r <- 10 until 20) {
      recs += "whitelisted"+String.valueOf(r)+"@testdomain.com"
      recs += "blacklisted"+String.valueOf(r)+"@testdomain.com"
    }
    val blck = factry.getBlackList(TestDAO.datasource, domain, workarea).load
    j.insertMessages(recs.toArray, RecipientType.TO, "AUTO", PENDING, blck)
    var a = j.atoms
    a.foreach(e => println("Readed e-mail atom "+e.email))
    for (r <- 10 until 20)
      assert(a.find(e => e.email=="whitelisted"+String.valueOf(r)+"@testdomain.com")!=None, "Atom not properly written")
    for (r <- 10 until 20)
      assert(a.find(e => e.email=="blacklisted"+String.valueOf(r)+"@testdomain.com")==None, "Black List not properly filtering incoming atoms")
    println(String.valueOf(a.size)+" atoms written")
  }
}