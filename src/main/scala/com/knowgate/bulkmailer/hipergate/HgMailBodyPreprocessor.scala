package com.knowgate.bulkmailer.hipergate

import java.io.File
import java.io.IOException

import com.knowgate.stringutils.Str

import com.knowgate.debug.Chronometer

import com.knowgate.bulkmailer.HtmlMimeBodyPart
import com.knowgate.xhtml.FastStreamReplacer

import com.knowgate.bulkmailer.Profiler
import com.knowgate.bulkmailer.Routine
import com.knowgate.bulkmailer.MailMessage
import com.knowgate.bulkmailer.SingleEmailAtom
import com.knowgate.bulkmailer.MailBodyPreprocessor

import scala.collection.mutable.Map

class HgMailBodyPreprocessor extends MailBodyPreprocessor {
  
  def baseDir(msg: MailMessage, environmentProperties: java.util.Map[String,String]) : String = {
     val domain = environmentProperties.getOrDefault("domain","2049")
     val workarea = environmentProperties.getOrDefault("workarea","00000000000000000000000000000000")
     val workareasput = Str.chomp(environmentProperties.getOrDefault("workareasput",""),File.separator)     
     val baseDir = workareasput+List(domain,workarea,"apps","mailwire","html",msg.getGuid).mkString(File.separator)
     val fdir = new File(baseDir)
     if (!fdir.exists()) fdir.mkdirs()
     baseDir + File.separator
  }

  def insertWebBeacon(rtn: Routine, body: String, emm: SingleEmailAtom) : String = {
  	var retval : String = body
    
  	var meter : Chronometer = null
    if (Profiler.enabled) meter = new Chronometer()
    
    val websrv = Str.chomp(rtn.properties.get("webserver"),"/")
    val endbody = Str.indexOfIgnoreCase(body, "</body>", 0)
    if (endbody>0) {
      val websrv = Str.chomp(rtn.properties.get("webserver"),"/")
    	retval = body.substring(0, endbody)+"<!--WEBBEACON SRC=\""+websrv+"webbeacon/?gu_job="+rtn.id+"&pg_atom="+String.valueOf(emm.id)+"&gu_company="+emm.companyId+"&gu_contact="+emm.contactId+"&tx_email="+emm.email+"\"-->"+body.substring(endbody)
    }
    
    if (Profiler.enabled) Profiler.totalInsertWebBeaconTime += meter.stop
    
    retval
  }
  
  def redirectExternalLinks(rtn: Routine, body: String, emm: SingleEmailAtom) : String = {
  	var retval : String = null

  	var meter : Chronometer = null
    if (Profiler.enabled) meter = new Chronometer()
    
  	val websrv = Str.chomp(rtn.properties.get("webserver"),"/")
    retval = new HtmlMimeBodyPart(body, null).addClickThroughRedirector(websrv+"redirect/?gu_job="+rtn.id+"&pg_atom="+emm.id.toString+"&tx_email="+emm.email+"&gu_company="+emm.companyId+"&gu_contact="+emm.contactId+"&url=")
    
    if (Profiler.enabled) Profiler.totalRedirectExternalLinksTime += meter.stop    
    
    retval  
  }

  @throws(classOf[IOException])
  def personalizeBody(rtn: Routine, body: String, emm: SingleEmailAtom) : String = {
  	var retval : String = null

  	var meter : Chronometer = null
    if (Profiler.enabled) meter = new Chronometer()
  	
  	val rplcr = new FastStreamReplacer(body.length()+256)      
    retval = rplcr.replace(new StringBuffer(body), FastStreamReplacer.createMap(
                           Array("Data.Name","Data.Surname","Data.Salutation","Data.Intro","Data.Url","Address.EMail","Job.Guid","Job.Atom","Data.Contact_Guid",
                                 "Datos.Nombre","Datos.Apellidos","Datos.Saludo","Datos.Intro","Datos.Url","Direccion.EMail","Lote.Guid","Lote.Atomo","Datos.Guid_Contacto"),
                           Array(emm.firstName,emm.lastName,emm.salutation,emm.intro,emm.url,emm.email,emm.routine.id,String.valueOf(emm.id),emm.contactId,
                                 emm.firstName,emm.lastName,emm.salutation,emm.intro,emm.url,emm.email,emm.routine.id,String.valueOf(emm.id),emm.contactId)))
    
    if (Profiler.enabled) Profiler.totalPersonalizeBodyTime += meter.stop    

    retval
  }
    
  def linkImages(msg: MailMessage, environmentProperties: java.util.Map[String,String]) : String = {
    val domain = environmentProperties.getOrDefault("domain","2049")
    val workarea = environmentProperties.getOrDefault("workarea","00000000000000000000000000000000")
    val imgsrv = Str.chomp(environmentProperties.get("imageserver"),"/")
    val basePath = List(domain,workarea,"apps","mailwire","html",msg.getGuid).mkString(imgsrv,"/","/")
    val part = new HtmlMimeBodyPart(msg.getHtmlBody, null)
    part.addPreffixToImgSrc(basePath)
  }

}