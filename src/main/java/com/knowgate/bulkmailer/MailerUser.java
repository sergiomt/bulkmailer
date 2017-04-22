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

import javax.jdo.JDOException;

import org.judal.storage.Record;
import org.judal.storage.TableDataSource;

public interface MailerUser extends Record {

	String getId();

	int getDomainId();
	
	String getNickname();

	MailerUser[] forWorkArea(TableDataSource oDts, String sWrkAreaId) throws JDOException, NullPointerException, InstantiationException;

}