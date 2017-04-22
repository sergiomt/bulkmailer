package com.knowgate.bulkmailer.hipergate

import java.util.concurrent.TimeUnit
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.LinkedBlockingQueue

import com.knowgate.bulkmailer.Log

class SingleStatementExecutor(cnh: SingleStatementConnectionHandler) extends ThreadPoolExecutor(1, 1, 1l, TimeUnit.MINUTES, new LinkedBlockingQueue[Runnable])  {
  	override def afterExecute(runable: Runnable, thrable: Throwable) {
  		if (thrable!=null) {
  		  Log.out.error("StatementExecutor "+thrable.getClass().getName()+" "+thrable.getMessage())
  		  cnh.reconnect
  		}
  	}
  	override def terminated() {
  	  cnh.close
  	}
  }