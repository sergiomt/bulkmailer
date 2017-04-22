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

import javax.jdo.JDOException;

public interface Jobs {

  public Job[] pending(String workarea) throws JDOException;

  public Job[] group(String groupId) throws JDOException;

  public Job[] between(String workarea, Date from, Date to) throws JDOException;

  public Mailing[] mailings(String workarea, Date from, Date to) throws JDOException;

  public EmailMessagesByDay[] messagesSentByDay(String workarea, Date from, Date to) throws JDOException;
  
  public EmailMessagesByHour[] messagesReadedByHour(String workarea, Date from, Date to) throws JDOException;

  public EmailMessagesByAgent[] messagesReadedByAgent(String workarea, Date from, Date to) throws JDOException;
  
}