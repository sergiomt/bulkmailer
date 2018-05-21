package com.knowgate.bulkmailer

/**
 * © Copyright 2016 the original author.
 * This file is licensed under the Apache License version 2.0.
 * You may not use this file except in compliance with the license.
 * You may obtain a copy of the License at:
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.
 */

import java.io.IOException
import java.io.PrintStream
import java.io.ByteArrayOutputStream

import java.util.Date
import java.util.Properties

import javax.jdo.JDOException

import javax.mail.Message;
import javax.mail.URLName;
import javax.mail.Folder;
import javax.mail.Message.RecipientType
import javax.mail.MessagingException
import javax.mail.StoreClosedException
import javax.mail.internet.InternetAddress

import com.knowgate.debug.Chronometer

import com.knowgate.debug.DebugFile

import org.judal.storage.table.Table
import org.judal.storage.table.Record
import org.judal.storage.DataSource

import com.knowgate.stringutils.Uid
import com.knowgate.mail.MailSessionHandler
import com.knowgate.xhtml.FastStreamReplacer

import scala.collection.mutable.Map
import scala.collection.mutable.Buffer
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions.asScalaBuffer

import akka.actor.ActorRef

class MimeSender(dts: DataSource, val job: Job, mimemsg: MailMessage, archiver: EmailAtomArchiver, blcklst: BlackList) extends Routine {

  if (null==job) throw new NullPointerException("MimeSender.<init> Job cannot be null")
  if (null==mimemsg) throw new NullPointerException("MimeSender.<init> MailMessage cannot be null")
  
  val jprops = job.properties()
  
  var mailsession = new MailSessionHandler(jprops)

  val preproc = job.preprocessor

  val attachimages = job.is ("attachimages", Job.YEAH, "true")
  val clickthrough = job.is ("clickthrough", Job.YEAH, "false")
  val personalized = job.is ("personalized", Job.YEAH, "false")
  val webbeacon = job.is ("webbeacon", Job.YEAH, "false")

  val bodyHtml = if (attachimages) mimemsg.getHtmlBody else preproc.linkImages(mimemsg, properties)
  val bodyText = mimemsg.getPlainTextBody
 
  def id = job.id
  
  def atoms = job.atoms

  def atom(n: Int) = job.atom(n)
  
  def archived() = job.archived()
  
  def atomCount() = job.atomCount()
  
  def properties = job.properties

  def group() = job.group()

  def setGroup(g: String) = { job.setGroup(g)}
  
  def status = job.status
  
  def setStatus(s: RoutineStatus) = { job.setStatus(s) }
  
  def updateStatus(s: RoutineStatus) = { job.updateStatus(s) }
    
  def process(emm: SingleEmailAtom) = {
    
    if (blcklst!=null) {
      if (!blcklst.contains(emm.email)) {
        var html : String = bodyHtml
        var text : String = bodyText
    
        if (clickthrough) {
          html = preproc.redirectExternalLinks(this, html, emm)
          text = preproc.redirectExternalLinks(this, text, emm)
        }
    
        if (personalized) {
          html = preproc.personalizeBody(this, html, emm)
          text = preproc.personalizeBody(this, text, emm)
        }
  
        if (webbeacon)
          html = preproc.insertWebBeacon(this, html, emm)

        val bao = new ByteArrayOutputStream()
        val prt = new PrintStream(bao)

        var replyAddr = mimemsg.getReplyAddress
        if (null==replyAddr)
          replyAddr = mimemsg.getFromAddress
        else if (replyAddr.length()==0)
          replyAddr = mimemsg.getFromAddress

        var meter : Chronometer = null
        if (Profiler.enabled) meter = new Chronometer()
          
        println("sending mimemsg "+mimemsg.getGuid)
        
        mailsession.sendMessage(
          mimemsg.getSubject,
          mimemsg.getDisplayName,
          mimemsg.getFromAddress,
          replyAddr,
          Array(emm.email),
          Array(RecipientType.TO),
          text, html, "UTF-8",
          Uid.createUniqueKey(),
          mimemsg.getParts().filter(p => p.getDisposition=="reference").map(_.getFileName).toArray,
          preproc.baseDir(mimemsg, job.properties),
          attachimages,
          prt
        )

        println("mimemsg "+mimemsg.getGuid+" sent")
        
        if (Profiler.enabled) Profiler.totalSendMessageTime += meter.stop

        prt.close

        println("archiving "+String.valueOf(emm.id))
       
        archiver.archive(emm, FINISHED, new String(bao.toByteArray(),"UTF-8"))

        bao.close

        println(String.valueOf(emm.id)+" archived")
        
      }
    }
  }

}