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

import javax.mail.Message.RecipientType

import org.judal.storage.table.Table
import org.judal.storage.table.TableDataSource

import com.knowgate.bulkmailer.hipergate.HgSingleEmailAtom

import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap
import scala.collection.mutable.SynchronizedSet
import scala.collection.mutable.SynchronizedMap
import scala.collection.mutable.SynchronizedQueue

import scala.collection.JavaConversions.asScalaBuffer

class EmailMessagesQueue(dts: TableDataSource) extends SynchronizedQueue[SingleEmailAtom] {

  val pendingAtomsForRoutine = new HashMap[String,Int] with SynchronizedMap[String,Int]
  val routineInstanceFromId = new HashSet[Routine] with SynchronizedSet[Routine]
    
  def enqueueProcess(rtn: Routine) {
    var tbl: Table = null
    
    if (pendingAtomsForRoutine.contains(rtn.id))
      throw new IllegalStateException("Job "+rtn.id+" is already enqueued")

    try {
      
      routineInstanceFromId += rtn

      rtn.updateStatus(RUNNING)

      var rst = rtn.atoms.filter(a => a.status == PENDING)
      
      pendingAtomsForRoutine += (rtn.id -> rst.size)

      rst.foreach(a => enqueue(new HgSingleEmailAtom(dts, rtn, a)))

    } finally {
      if (tbl!=null) tbl.close()
    }    
  }
  
  def dequeueProcess(id: String) {
    dequeueAll(_.routine.id == id)
  }

  override def dequeue() : SingleEmailAtom = {
    var emm = super.dequeue
    val cnt = pendingAtomsForRoutine(emm.routine.id) - 1
    if (cnt==0) {
      emm.routine.updateStatus(FINISHED)
      routineInstanceFromId.remove(emm.routine)
      pendingAtomsForRoutine.remove(emm.routine.id)
    } else {
      pendingAtomsForRoutine.update(emm.routine.id, cnt)
    }
    return emm
  }

}