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

import java.io.File;
import java.util.List;

import org.judal.storage.Record;

public interface MailMessage extends Record {
      
  String getId();
  
  String getGuid();

  void setGuid(String uid);
    
  String getSubject();
  
  void setSubject(String s);
  
  String getFromAddress();

  void setFromAddress(String a); 

  String getReplyAddress();

  void setReplyAddress(String a); 
  
  String getDisplayName();

  void setDisplayName(String n);

  String getEncoding();

  void setEncoding(String e);

  void setContent(byte[] bya);

  void setLength(int l);

  String getMD5();
  
  void setMD5(String s);

  String getWorkArea();
  
  void setWorkArea(String w);
  
  void addPart(MailPart p);
  
  MailPart createPart(String txt, String subType, int idPart);

  MailPart createPart(File fle, String mimeType, String disposition, int idPart);

  List<MailPart> getParts();
  
  void setPlainTextBody(String txt);

  String getPlainTextBody();
  
  void setHtmlBody(String htm);

  String getHtmlBody();

  void setHtmlAndPlainBody(String htm, String txt);

}