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

object Profiler {

  var enabled = true
  
  @volatile var totalInsertMessagesTime = 0l

  @volatile var totalAtomsRetrievalTime = 0l

  @volatile var totalAtomsArchivingTime = 0l
  
  @volatile var totalInsertWebBeaconTime = 0l

  @volatile var totalRedirectExternalLinksTime = 0l
  
  @volatile var totalPersonalizeBodyTime = 0l
  
  @volatile var totalSendMessageTime = 0l
  
  def reset() =  {
    totalInsertMessagesTime = 0l
    totalAtomsRetrievalTime = 0l
    totalAtomsArchivingTime = 0l
    totalInsertWebBeaconTime = 0l
    totalRedirectExternalLinksTime = 0l
    totalPersonalizeBodyTime = 0l
    totalSendMessageTime = 0l
  }
  
  def report() : String = {
    var retval = ""
    retval += "totalInsertMessagesTime "+String.valueOf(totalInsertMessagesTime)+" ms\n"
    retval += "totalAtomsRetrievalTime "+String.valueOf(totalAtomsRetrievalTime)+" ms\n"
    retval += "totalAtomsArchivingTime "+String.valueOf(totalAtomsArchivingTime)+" ms\n"
    retval += "totalInsertWebBeaconTime "+String.valueOf(totalInsertWebBeaconTime)+" ms\n"
    retval += "totalRedirectExternalLinksTime "+String.valueOf(totalRedirectExternalLinksTime)+" ms\n"
    retval += "totalPersonalizeBodyTime "+String.valueOf(totalPersonalizeBodyTime)+" ms\n"
    retval += "totalSendMessageTime "+String.valueOf(totalSendMessageTime)+" ms\n"
    retval
  }
}