package com.knowgate.bulkmailer.test

import java.io.File

import java.util.Map

import javax.mail.Message.RecipientType

import com.knowgate.io.StreamPipe
import com.knowgate.stringutils.Str

import com.knowgate.bulkmailer.PENDING
import com.knowgate.bulkmailer.FINISHED
import com.knowgate.bulkmailer.Factory
import com.knowgate.bulkmailer.Profiler
import com.knowgate.bulkmailer.Scheduler
import com.knowgate.bulkmailer.MimeSender
import com.knowgate.bulkmailer.Using._

import scala.collection.mutable.Buffer

class SendEmailUsingJob(factry: Factory, domain: Int, workarea: String, writer: String) extends TestCase {
  
  val rwjobs = new ReadWriteJobs(factry, domain, workarea, writer)
  val rwmail = new ReadWriteMails(factry, domain, workarea, writer)
  
  def send(recipients: Buffer[String], params: Map[String,String], useScheduler: Boolean) = {
    
    println("Begin SendEmailUsingJob to "+recipients.mkString("{",",","}"))

    Profiler.reset

    val m = factry.newMailMessage(TestDAO.datasource, workarea)
    m.setDisplayName("BulkMailer Test Suite")
    m.setFromAddress("mailer@knowgate.com")
    m.setReplyAddress("noreply@knowgate.com")
    if (params.get("attachimages")=="true")
      m.setSubject("This is a test e-mail with attached images")
    else
      m.setSubject("This is a test e-mail without attached images")
    
    val htm = rwmail.getResourceAsString("body.htm", "ISO8859_1")
    val txt = rwmail.getResourceAsString("body.txt", "ISO8859_1")
    
    m.setHtmlAndPlainBody(htm, txt)
    m.store(TestDAO.datasource)
    
    val b = factry.getBlackList(TestDAO.datasource, domain, workarea).load    
    val j = rwjobs.writeJob("MimeSender Test Job", m.getGuid, params)
    j.insertMessages(recipients.toArray, RecipientType.TO, "HTML", PENDING, b)

    val d = Str.chomp(j.preprocessor.baseDir(m, TestDAO.properties), File.separator)
        
    rwmail.copyResourceTo("body.txt", d) 
    rwmail.copyResourceTo("body.htm", d) 
    rwmail.copyResourceTo("kglogo.gif", d) 
    rwmail.copyResourceTo("knowgate.gif", d)
    
    println("wrote job "+j.id+" in workarea "+workarea)
    println("job "+j.id+" has "+String.valueOf(j.atoms.size)+" atoms")

    println("job mail message set to"+j.getMailMessageId)
    
    if (useScheduler) {
    	Scheduler.init("hipergate")
    	Scheduler.start
    } else {
      var r = factry.getArchiver(TestDAO.datasource, TestDAO.datasource)
      using(r) {
        val s = new MimeSender(TestDAO.datasource, j, m, r, b)
        j.atoms.foreach(a => s.process(a))
      }
      j.updateStatus(FINISHED)
    }

    println("End SendEmailUsingJob")
  }
  
  
}