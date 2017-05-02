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

import org.judal.storage.table.TableDataSource

import javax.jdo.JDOException

import org.judal.storage.scala.ArrayRecord
import org.judal.storage.table.RecordSet
import org.judal.storage.table.Table

import com.knowgate.bulkmailer.Using._

class WorkArea(dts: TableDataSource) extends ArrayRecord(dts, "k_workareas") {

  val tableName = "k_workareas";
  
	def getId() : String = getString("gu_workarea")

	def getDomainId() : Int = getInt("id_domain")

	def getName() : String = getString("nm_workarea")
	
	def forName(wrkAreaName: String) : Boolean = {
		var tbl : Table = null
		var loaded = false
		using (tbl) {
			tbl = dts.openTable(this)
			val rst : RecordSet[WorkArea] = tbl.fetch(fetchGroup(), "nm_workarea", wrkAreaName)
			if (rst.size()>0) {
			  val r = rst.get(0)
			  put("gu_workarea", r.getId())
			  put("id_domain", r.getId())
			  put("nm_workarea", r.getName())
			  loaded = true
			} else {
			  clear()
			  loaded = true
			}
		}
	  loaded
	}
}
