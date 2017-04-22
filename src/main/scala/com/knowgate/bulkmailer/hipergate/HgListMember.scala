package com.knowgate.bulkmailer.hipergate

import org.judal.storage.TableDataSource
import org.judal.storage.scala.ArrayRecord

import com.knowgate.bulkmailer.ListMember

class HgListMember (dts: TableDataSource) extends ArrayRecord(dts,"k_x_list_members") with ListMember {

  def getListId() = getString("gu_list")

  def getListType() = if (isNull("tp_list")) 3 else getInt("tp_list")

  def isActive() = getInt("bo_active")!=0
  
}