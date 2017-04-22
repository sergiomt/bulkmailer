package com.knowgate.bulkmailer.test

import java.io.File
import java.io.InputStream
import java.io.FileOutputStream
import java.io.ByteArrayOutputStream

import org.judal.storage.Table
import com.knowgate.io.StreamPipe
import com.knowgate.stringutils.Str

import com.knowgate.bulkmailer.Factory
import com.knowgate.bulkmailer.MailMessage

class ReadWriteMails(factry: Factory, domain: Int, workarea: String, writer: String) extends TestCase {

   def getResourceAsString(resName: String, enc: String) : String = {
     val outstrm = new ByteArrayOutputStream()
     val instrm = getClass().getResourceAsStream(resName)
     val pipe = new StreamPipe()
     pipe.between(instrm, outstrm)
     instrm.close()
     val retval = new String(outstrm.toByteArray(), enc)
     outstrm.close()
     retval
   }

   def copyResourceTo(resName: String, targetDir: String) : File = {
     val fle = new File(Str.chomp(targetDir,File.separator)+resName)
     if (fle.exists()) fle.delete()
     val outstrm = new FileOutputStream(fle)
     val instrm = getClass().getResourceAsStream(resName)
     val pipe = new StreamPipe()
     pipe.between(instrm, outstrm)
     instrm.close()
     outstrm.close()
     fle
   }
   
   def writeTextEmails(baseDir: String) : MailMessage = {
     var uid: String = null
     var msg: MailMessage = null
     val plainbody = "Plain text message body test"
     val htmlbody = "<html><body><table><tr><td><img src=\"kglogo.gif\"></td><td><img src=\"knowgate.gif\"></td><td>HTML text message body test</td></tr></table></body></html>"

     println("Begin MailMessage Write/Read test")
     
       msg = factry.newMailMessage(TestDAO.datasource, workarea)
       msg.setPlainTextBody(plainbody)
       msg.store(TestDAO.datasource)
       uid = msg.getGuid
     
       msg = factry.loadMailMessage(TestDAO.datasource, workarea, uid)
     
       assert(msg.getPlainTextBody==plainbody, "Plain text message not properly written or readed")
     
       msg = factry.newMailMessage(TestDAO.datasource, workarea)
       msg.setHtmlBody(htmlbody)
       msg.store(TestDAO.datasource)
       uid = msg.getGuid
     
       msg = factry.loadMailMessage(TestDAO.datasource, workarea, uid)

       assert(msg.getHtmlBody==htmlbody, "HTML text message not properly written or readed")

       msg = factry.newMailMessage(TestDAO.datasource, workarea)
       msg.setHtmlAndPlainBody(htmlbody,plainbody)
       msg.store(TestDAO.datasource)
       uid = msg.getGuid
     
       msg = factry.loadMailMessage(TestDAO.datasource, workarea, uid)

       assert(msg.getPlainTextBody==plainbody, "Plain text message not properly written or readed")
       assert(msg.getHtmlBody==htmlbody, "HTML text message not properly written or readed")
     println("End MailMessage Write/Read test")
     msg
   }
   
   def addImagesToEmail(msg: MailMessage, baseDir: String) = {

     println("Begin MailMessage add images")

     var fle = copyResourceTo("knowgate.gif",baseDir)
     msg.addPart(msg.createPart(fle, "image/gif", "reference", msg.getParts().size()+1))
     
     fle = copyResourceTo("kglogo.gif",baseDir)
     msg.addPart(msg.createPart(fle, "image/gif", "reference", msg.getParts().size()+1))
     
     var uid = msg.getGuid
     
     var ms2 = factry.loadMailMessage(TestDAO.datasource, workarea, uid)
 
     println("message has "+String.valueOf(ms2.getParts().size())+" parts")

     val parts = ms2.getParts()
     
     for (p <-0 until ms2.getParts().size())
       println("part "+String.valueOf(p+1)+" is "+parts.get(p).getMimeType+" file "+parts.get(p).getFileName)

     var found = false
     for (p <-0 until ms2.getParts().size())
       found ||=  parts.get(p).getFileName=="knowgate.gif"     
     assert (found, "Image part knowgate.gif not found")
     found = false
     for (p <-0 until ms2.getParts().size())
       found ||=  parts.get(p).getFileName=="kglogo.gif"     
     assert (found, "Image part kglogo.gif not found")

     println("End MailMessage add images")
     
   }
   
}