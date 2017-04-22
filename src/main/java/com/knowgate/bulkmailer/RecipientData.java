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

import java.util.NoSuchElementException;

import javax.jdo.JDOException;

public interface RecipientData {
  
  String email();
  
  String firstName();

  String lastName();

  String salutation();

  String workarea();

  boolean isActiveAtList(MailingList oLst) throws JDOException, NoSuchElementException;

  boolean isBlackListed() throws JDOException;
  
  MailingList[] lists() throws JDOException;

  MailingList[] exclusionLists() throws JDOException;
  
  ArchivedEmailMessage[] messagesSent() throws JDOException;

  WebBeacon[] messagesOpened() throws JDOException;

  ClickThrough[] clickThrough() throws JDOException;
   
}