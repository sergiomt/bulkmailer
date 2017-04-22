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

public class UserAgents {
  private static final String[] aAgents = { "Unknown", "Internet Explorer 6.0", "Internet Explorer 7.0", "Internet Explorer 8.0",
                                            "Firefox 2.0", "Firefox 3.0", "Firefox 3.5", "iPhone", "Android", "Minimo",
                                            "Lotus Notes", "Thunderbird", "Safari", "Google Chrome", "Outlook", "Evolution",
                                            "Other Gecko", "Other" };

  public static String[] list() {
  return aAgents;
  }

  public static String getName(int a) {
    return aAgents[a];
  } 

  public static String identify(String sUserAgent) {
    if (sUserAgent==null) {
      return aAgents[0];
    } else if (sUserAgent.length()==0) {
      return aAgents[0];    
    } else if (sUserAgent.indexOf("Outlook")>0) {
		  return aAgents[14]; 
    } else if (sUserAgent.startsWith("Mozilla/4.0 (compatible; MSIE 6.0;")) {
		  return aAgents[1];    
    } else if (sUserAgent.startsWith("Mozilla/4.0 (compatible; MSIE 7.0;")) {
		  return aAgents[2];    
    } else if (sUserAgent.startsWith("Mozilla/4.0 (compatible; MSIE 8.0;")) {
		  return aAgents[3];    
    } else if (sUserAgent.indexOf("Firefox/2.0")>0) {
		  return aAgents[4];    
    } else if (sUserAgent.indexOf("Firefox/3.0")>0) {
		  return aAgents[5];    
    } else if (sUserAgent.indexOf("Firefox/3.5")>0) {
		  return aAgents[6];    
    } else if (sUserAgent.indexOf("iPhone")>0) {
		  return aAgents[7];    
    } else if (sUserAgent.indexOf("Android")>0) {
		  return aAgents[8];    
    } else if (sUserAgent.indexOf("Minimo")>0) {
		  return aAgents[9];    
    } else if (sUserAgent.indexOf("Lotus-Notes")>0) {
		  return aAgents[10];    
    } else if (sUserAgent.indexOf("Thunderbird")>0) {
		  return aAgents[11];    
    } else if (sUserAgent.indexOf("Safari")>0) {
		  return aAgents[12];  
    } else if (sUserAgent.indexOf("Chrome")>0) {
		  return aAgents[13]; 
    } else if (sUserAgent.indexOf("Evolution")>0) {
		  return aAgents[15];
    } else if (sUserAgent.indexOf("Gecko")>0) {
		  return aAgents[16];
    } else {
		  return aAgents[17];
    }    
  }

}