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

import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.jdo.JDOException;

import javax.mail.Message.RecipientType;

import org.judal.storage.Record;

public interface Job extends Record,Routine,SumCounter {

  public static final String COMMAND_SEND = "SEND";
  public static final String COMMAND_INVITE = "INVT";

  public static final String[] YEAH = new String[]{"1","true","yes","Yes","Y","y"};

  String command();

  MailBodyPreprocessor preprocessor();

  void setCommand(String c);

  String getMailMessageId();

  void setMailMessageId(String g);
  
  boolean is(String param, String[] values, String defval);
  
  String getParameter(String p) throws NoSuchElementException;

  String getParameter(String p, String d);

  void setParameter(String p, String v);

  void setParameters(Map<String,String> params);
  
  Date executionDate();

  void setExecutionDate(Date dt);

  Date finishDate();

  void setFinishDate(Date dt);
    
  String title();

  void setTitle(String t);
    
  String workarea();

  void setWorkarea(String w);

  String writer();

  void setWriter(String w);
  
  void insertMessages(String[] recs, RecipientType rectype, String format, RoutineStatus status, BlackList blck) throws JDOException;
  
  ClickThrough[] clickThrough() throws JDOException;
  
  WebBeacon[] webBeacons() throws JDOException;
  
  Mailing mailing() throws JDOException;
  
  EmailMessagesByDay[] messagesByDay() throws JDOException;
  
  EmailMessagesByHour[] messagesByHour() throws JDOException;
  
}