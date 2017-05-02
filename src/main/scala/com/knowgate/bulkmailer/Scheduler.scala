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

import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.routing.RoundRobinRouter

import java.nio.file.Paths

import javax.cache.Cache
import javax.cache.CacheManager
import javax.cache.Caching
import javax.cache.configuration.MutableConfiguration
import javax.cache.spi.CachingProvider

import org.judal.storage.Env
import org.judal.storage.table.Table
import org.judal.storage.table.Record
import org.judal.storage.Engine
import org.judal.storage.EngineFactory
import org.judal.storage.table.RecordManager
import org.judal.storage.table.TableDataSource

import org.judal.ramqueue.RAMQueueProducer

import com.knowgate.bulkmailer.ActMessage

import scala.collection.JavaConverters._

import scala.reflect.runtime.universe

object Scheduler {
  
  var system: ActorSystem = null
  var master: ActorRef = null

  class Sender extends Actor {
    override def receive = {
      case Send(emm: SingleEmailAtom) => {
        emm.send
        sender ! Done (emm)
      }
    }     
  }

  class Master(val properties: Map[String,String]) extends Actor {

     EngineFactory.registerEngine(EngineFactory.NAME_JDBC, properties.getOrElse("engine", "org.judal.jdbc.JDBCEngine"))
    
     val jprops = properties.asJava

     val engine = EngineFactory.getEngine(EngineFactory.NAME_JDBC)
     
     val queue = new RAMQueueProducer(EngineFactory.NAME_JDBC, jprops);

		 val cachingProvider = Caching.getCachingProvider()
		 val cacheManager = cachingProvider.getCacheManager()
		 val config = new MutableConfiguration[Object, Record]().setTypes(classOf[Object], classOf[Record]).setStatisticsEnabled(true)
		 val cache : Cache[Object,Record] = cacheManager.createCache("simpleCache", config)

     val dataSource = engine.getDataSource(jprops).asInstanceOf[TableDataSource]
     
     val storageManager = new RecordManager(dataSource, queue, cache, jprops)
     
     val emailsQueue = new EmailMessagesQueue(dataSource)
     
     val defaultDegreeOfParallelism = "4"
     
     val nrOfWorkers = Integer.parseInt(properties.getOrElse("workers",defaultDegreeOfParallelism))

     var maxParallelJobs = Integer.parseInt(properties.getOrElse("maxparalleljobs",defaultDegreeOfParallelism))
     
     val factoryObjectName = properties.getOrElse("factory","com.knowgate.bulkmailer.hipergate.HgFactory")

     val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)
     val module = runtimeMirror.staticModule(factoryObjectName)
     val obj = runtimeMirror.reflectModule(module)

     val factry = obj.instance.asInstanceOf[Factory]
     
     var domain = Integer.parseInt(properties.getOrElse("domain","2051"))

     var workarea = properties.getOrElse("workarea","c0a8010b145c14fed49100000ea65719")
     
     val blackList = factry.getBlackList(dataSource, domain, workarea).load

     val senderRouter = context.actorOf(Props[Sender].withRouter(RoundRobinRouter(nrOfWorkers)), name = "senderRouter")
     
     val archiver = factry.getArchiver(dataSource, dataSource)
     
     var pendingMessages = 0
     
     private def loadPendingJobs() : Int = {              
       var tbl: Table = null
       try {
      	 
         var jobs = factry.getJobs(dataSource, jprops).pending(workarea)
         jobs = jobs.filter(j => j.command==Job.COMMAND_SEND && j.getMailMessageId!=null)
         
         // If the number of jobs is greater that maxParallelJobs then start executing only the first maxParallelJobs ones
         if (jobs.length>maxParallelJobs)
           jobs = jobs.dropRight(jobs.length-maxParallelJobs)

         // Put single mails messages from each pending job at the emails queue
         jobs.foreach(j => emailsQueue.enqueueProcess(new MimeSender (dataSource, j, factry.loadMailMessage(dataSource, workarea, j.getMailMessageId), archiver, blackList)))
         
         Log.out.debug(emailsQueue.size.toString+" messages queued")       
       } finally {
         if (tbl!=null) tbl.close()
       }       
       emailsQueue.size
     }

     @throws(classOf[NoSuchElementException])
     private def sendOne() = {
       senderRouter ! Send (emailsQueue.dequeue)
    	 pendingMessages += 1
     }
     
     override def postStop (): Unit = {
       storageManager.close()
     }

     override def receive = {
       case Start => {
         
         Log.out.debug("Master ! Start with " + nrOfWorkers.toString +" workers for a maximum of " + maxParallelJobs.toString + " parallel jobs")

         if (loadPendingJobs()>0)
           try {
             for (i <- 1 to nrOfWorkers) sendOne
           } catch {
             case nse: NoSuchElementException =>
           }
         else
           context.stop(self)   
       }
       
       case Done (emm: SingleEmailAtom) => {
         pendingMessages -= 1
         try {
           sendOne              
         } catch {
           case nse: NoSuchElementException => {
             if (loadPendingJobs()>0) sendOne
             if (0==pendingMessages) context.stop(self)
           }
         }
       }
     }
  
  }    

  def init(propertiesPath:String) {
    val profile = "bulkmailer"
    if (system!=null) throw new RuntimeException("Scheduler.init("+profile+") ActorSystem is already initialized")
    system = ActorSystem(profile+"-system")
    master = system.actorOf(Props(new Master(Env.getDataSourceProperties(Paths.get(propertiesPath), "bulkmailer").asScala.toMap)), name = profile+"-master")
    Log.out.info("ActorSystem("+profile+"-system) initialized");
  }
  
  def start() {
    Log.out.info("Staring Master");
    master ! Start
  }
  
  def shutdown() {
    if (system!=null) system.shutdown
    system = null
    Log.out.info("ActorSystem shutted down");
  }
  
  def isTerminated = if (system==null) true else system.isTerminated
}