package com.knowgate.bulkmailer.test

import java.util.HashMap
import java.util.Map

import javax.cache.Cache
import javax.cache.CacheManager
import javax.cache.Caching
import javax.cache.configuration.MutableConfiguration
import javax.cache.spi.CachingProvider

import org.judal.jdbc.JDBCEngine

import org.judal.storage.Record
import org.judal.storage.EngineFactory
import org.judal.storage.RecordManager

import org.judal.storage.Env

import org.judal.ramqueue.RAMQueueProducer

import scala.collection.JavaConverters._

object TestDAO {

  val inStrm = getClass.getResourceAsStream("datasource.properties")
  val props = Env.getDataSourceProperties(inStrm, "test")

	val jdbc = new JDBCEngine();
  EngineFactory.registerEngine(jdbc.name(), jdbc.getClass().getName());    
  val engine = EngineFactory.getEngine(jdbc.name())
  val datasource = jdbc.getDataSource(props)
  
	val cachingProvider = Caching.getCachingProvider()
	val cacheManager = cachingProvider.getCacheManager()
	val config : MutableConfiguration[AnyRef, Record] = new MutableConfiguration[AnyRef, Record]().setTypes(classOf[AnyRef], classOf[Record]).setStatisticsEnabled(true)
	val cache : Cache[AnyRef, Record] = cacheManager.createCache("simpleCache", config)

	val que = new RAMQueueProducer(jdbc.name(), props)
	
  val manager = new RecordManager(datasource, que, cache, props)
  val properties = new HashMap[String,String]()
  
  for (e <- manager.getProperties().entrySet().asScala)
    properties.put(e.getKey, e.getValue.toString)
}