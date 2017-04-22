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
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class EmailMessagesByDay {
	
	public EmailMessagesByDay(int n, long d) {
		numessages = n;
		daymillis = d;
	}

	public EmailMessagesByDay(int n, String d) throws ParseException {
		numessages = n;
		daymillis = ymd.parse(d).getTime();
	}

	public EmailMessagesByDay(int n, Date d) throws ParseException {
		numessages = n;
		daymillis = d.getTime();
	}
	
	public String daystr() {
		return ymd.format(new Date(daymillis));
	}

	public int numessages;
	public long daymillis;
	
	public static SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");

}