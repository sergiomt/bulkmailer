package com.knowgate.bulkmailer;

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

import java.io.PrintStream;
import java.util.Map;

import org.judal.storage.table.TableDataSource;

public interface Factory {

  BlackList getBlackList(TableDataSource d, int n, String w);

  Job newJob(TableDataSource d, Map<String,String> p);

  SingleEmailAtom newEmailAtom(TableDataSource d, Routine r);
  
  Job loadJob(TableDataSource d, Map<String,String> p, String j);

  Jobs getJobs(TableDataSource d, Map<String,String> p);

  BlackListedEmail loadBlackListedEmail(TableDataSource d, String w, String e);
  
  MailMessage newMailMessage(TableDataSource d, String w);
 
  MailMessage loadMailMessage(TableDataSource d, String w, String id);

  Mailing loadAdHocMailing(TableDataSource d, String id);

  Mailing loadPageSetMailing(TableDataSource d, String id);

  RecipientData loadRecipientData(TableDataSource d, String w, String e);

  RecipientData newRecipientData(TableDataSource d, String w, String e);
  
  EmailAtomArchiver getArchiver(TableDataSource src, TableDataSource tgt);

  UrlData newUrlData(TableDataSource src);
  
  UrlData loadUrlData(TableDataSource src, String g, String w);

  ClickThrough newClickThrough(TableDataSource d);
  
  Tracker getBeaconTracker(TableDataSource d);

  Tracker getClickTracker(TableDataSource d, String w);

  TotalAtomsByDay newTotalAtomsByDay(TableDataSource d);

  Statistics getStatistics(TableDataSource d, PrintStream p);
  
  MailerUser[] getMailerUsersForWorkArea(TableDataSource d, String w);

}