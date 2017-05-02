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

import org.judal.storage.table.Record;

public interface Mailing extends Record, SumCounter {

	String getId();

	void setId(String id);

	Date lastExecutionDate() throws JDOException;

	int getNumber();

	void setNumber(int n);

	String getName();

	void setName(String n);

	String getSubject();

	void setSubject(String s);

	String getFromAddress();

	void setFromAddress(String f);

	String getReplyTo();

	void setReplyTo(String r);

	String getDisplayName();

	void setDisplayName(String n);

	String getAllowPattern();

	void setAllowPattern(String p);

	String getDenyPattern();

	void setDenyPattern(String p);

	RecipientsList recipientsList();

	Job[] jobs() throws JDOException;

	MailingList[] lists() throws JDOException;

	ClickThrough[] clickThrough() throws JDOException;

}