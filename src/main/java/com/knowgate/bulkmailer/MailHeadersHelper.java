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

import java.io.UnsupportedEncodingException;

import java.util.Date;
import java.text.SimpleDateFormat;

import java.sql.Timestamp;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.InternetAddress;

import com.knowgate.debug.DebugFile;
import com.knowgate.debug.StackTraceUtil;
import com.knowgate.io.MD5;
import com.knowgate.stringutils.Html;
import com.knowgate.stringutils.Str;

import org.xml.sax.helpers.DefaultHandler;

/**
 * Set of utility functions for managing MimeMessage headers
 * @author Sergio Montoro Ten
 * @version 8.0
 */

public class MailHeadersHelper extends DefaultHandler {
  private MimeMessage oMsg;
  private final static String EmptyString = "";
  private final static SimpleDateFormat DateFrmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  // ---------------------------------------------------------------------------

  public MailHeadersHelper() {
    oMsg = null;
  }

  // ---------------------------------------------------------------------------

  public MailHeadersHelper(MimeMessage oMimeMsg) {
	  setMessage(oMimeMsg);
  }
  
  // ---------------------------------------------------------------------------

  public void setMessage(MimeMessage oMimeMsg) {
    oMsg = oMimeMsg;
  }

  // ---------------------------------------------------------------------------
  
  /**
   * <p>Get decoded Message Id</p>
   * This method first calls MimeMessage.getMessageID() If nothing is returned
   * then it tries to retrieve header X-Qmail-Scanner-Message-ID or Resent-Message-ID.<br>
   * If neither Message-Id nor X-Qmail-Scanner-Message-ID nor Resent-Message-ID headers are found
   * then a message id is assigned by using the message sent date and from address like:<br>
   * &lt;20050722140022.sender@domain.com&gt;
   * The result is then decoded by calling MimeUtility.decodeText() before being returned
   * @param oMimeMsg MimeMessage
   * @return String Decoded value of Message Id.
   * @throws MessagingException
   * @throws UnsupportedEncodingException
   */
  public static String decodeMessageId (MimeMessage oMimeMsg)
    throws MessagingException,UnsupportedEncodingException {
    if (DebugFile.trace) {
      DebugFile.writeln("Begin HeadersHelper.decodeMessageId([MimeMessage])");
      DebugFile.incIdent();
    }
    String sRetId = oMimeMsg.getMessageID();
    // If Message-Id header is not found try to use X-Qmail-Scanner-Message-ID
    if (sRetId==null || EmptyString.equals(sRetId)) {
      try { sRetId = oMimeMsg.getHeader("X-Qmail-Scanner-Message-ID", null); } catch (Exception ignore) {}
    }
    // If X-Qmail-Scanner-Message-ID header is not found then try to use Resent-Message-ID
    if (sRetId==null || EmptyString.equals(sRetId)) {
      try { sRetId = oMimeMsg.getHeader("Resent-Message-ID", null); } catch (Exception ignore) {}
    }
    // If no valid Message Id, is found then create a default one by using sent date and from address
    if (sRetId==null) {
      Date oDt = oMimeMsg.getSentDate();
      Address[] aFrom = null;
      try {
        aFrom = oMimeMsg.getFrom();
      }  catch (AddressException bypass) { aFrom = null; }
      if (oDt!=null && aFrom!=null) {
        if (aFrom.length > 0)
          if (aFrom[0]!=null)
            sRetId = "<" + String.valueOf(oDt.getYear()) +
                     String.valueOf(oDt.getMonth() + 1) + String.valueOf(oDt.getDate()) +
                     String.valueOf(oDt.getHours()) + String.valueOf(oDt.getMinutes()) +
                     String.valueOf(oDt.getSeconds()) + "." + aFrom[0].toString() + ">";
      } // fi
    } // fi (sRetId)
    if (sRetId!=null) {
      sRetId = MimeUtility.decodeText(sRetId);
      // No quotes allowed on message identifiers
      if (sRetId.indexOf('"')>=0) sRetId = Str.removeChars(sRetId, "\"");
    }
    if (DebugFile.trace) {
      DebugFile.decIdent();
      DebugFile.writeln("End HeadersHelper.decodeMessageId() : " + sRetId);
    }
    return sRetId;
  } // decodeMessageId

  // ---------------------------------------------------------------------------

  /**
   * <p>Get decoded Message Id</p>
   * This method first calls MimeMessage.getMessageID() If nothing is returned
   * then it tries to retrieve header X-Qmail-Scanner-Message-ID or Resent-Message-ID.<br>
   * If neither Message-Id nor X-Qmail-Scanner-Message-ID nor Resent-Message-ID headers are found
   * then a message id is assigned by using the message sent date and from address like:<br>
   * &lt;20050722140022.sender@domain.com&gt;
   * The result is then decoded by calling MimeUtility.decodeText() before being returned
   * @return String Decoded value of Message Id.
   * @throws MessagingException
   * @throws UnsupportedEncodingException
   */
  public String decodeMessageId ()
    throws MessagingException,UnsupportedEncodingException {
    return MailHeadersHelper.decodeMessageId(oMsg);
  }

  // ---------------------------------------------------------------------------

  /**
   * <p>Get decoded Message Id</p>
   * @param sDefault String Default value is a Message Id. cannot be found at message headers
   * @return String
   */
  public String decodeMessageId (String sDefault) {
    String sMessageID;
    try {
      sMessageID = decodeMessageId();
    } catch (Exception xcpt) {
      if (DebugFile.trace) DebugFile.writeln(xcpt.getClass().getName()+" "+xcpt.getMessage());
      sMessageID = sDefault;
    }
    if (sMessageID==null) sMessageID = sDefault; else if (sMessageID.length()==0) sMessageID = sDefault;
    return sMessageID;
  } // decodeMessageId

  // ---------------------------------------------------------------------------

  public static String getContentType(MimeMessage oMsg)
    throws UnsupportedEncodingException,MessagingException {
    String sRetVal = oMsg.getContentType();
    if (sRetVal!=null)
      sRetVal = MimeUtility.decodeText(sRetVal);
    return sRetVal;
  }

  // ---------------------------------------------------------------------------

  public String getContentType()
    throws UnsupportedEncodingException,MessagingException {
    return MailHeadersHelper.getContentType(oMsg);
  }

  // ---------------------------------------------------------------------------

  public static String getContentID(MimeMessage oMsg)
    throws UnsupportedEncodingException,MessagingException {
    String sRetVal = oMsg.getContentID();
    if (sRetVal!=null)
      sRetVal = MimeUtility.decodeText(sRetVal);
    return sRetVal;
  }

  // ---------------------------------------------------------------------------

  public String getContentID()
    throws UnsupportedEncodingException,MessagingException {
    return MailHeadersHelper.getContentID(oMsg);
  }

  // ---------------------------------------------------------------------------

  public static String getMessageID(MimeMessage oMsg)
    throws UnsupportedEncodingException,MessagingException {
    return decodeMessageId(oMsg);
  }

  // ---------------------------------------------------------------------------

  public String getMessageID()
    throws UnsupportedEncodingException,MessagingException {
    return MailHeadersHelper.getMessageID(oMsg);
  }
  
  // ---------------------------------------------------------------------------

  public static String getDisposition(MimeMessage oMsg)
    throws UnsupportedEncodingException,MessagingException {
    String sRetVal = oMsg.getDisposition();
    if (sRetVal!=null)
      sRetVal = MimeUtility.decodeText(sRetVal);
    return sRetVal;
  }

  // ---------------------------------------------------------------------------

  public String getDisposition()
    throws UnsupportedEncodingException,MessagingException {
    return MailHeadersHelper.getDisposition(oMsg);
  }

  // ---------------------------------------------------------------------------

  public static String getContentMD5(MimeMessage oMsg)
    throws UnsupportedEncodingException,MessagingException {
    String sRetVal = oMsg.getContentMD5();
    if (sRetVal!=null)
      sRetVal = MimeUtility.decodeText(sRetVal);
    return sRetVal;
  }

  // ---------------------------------------------------------------------------

  public static String computeContentMD5(byte[] byArray) {
    String sContentMD5;
    MD5 oMd5 = new MD5();
    oMd5.Init();
    oMd5.Update(byArray);
    sContentMD5 = oMd5.FinalHex();
    oMd5 = null;
    return sContentMD5;
  }

  // ---------------------------------------------------------------------------

  public String getContentMD5()
    throws UnsupportedEncodingException,MessagingException {
    return MailHeadersHelper.getContentMD5(oMsg);
  }

  // ---------------------------------------------------------------------------

  public static String getDescription(MimeMessage oMsg)
    throws UnsupportedEncodingException,MessagingException {
    String sRetVal = oMsg.getDescription();
    if (sRetVal!=null)
      sRetVal = MimeUtility.decodeText(sRetVal);
    return sRetVal;
  }

  // ---------------------------------------------------------------------------

  public String getDescription()
    throws UnsupportedEncodingException,MessagingException {
    return MailHeadersHelper.getDescription(oMsg);
  }

  // ---------------------------------------------------------------------------

  public static String getFileName(MimeMessage oMsg)
    throws UnsupportedEncodingException,MessagingException {
    String sRetVal = oMsg.getFileName();
    if (sRetVal!=null)
      sRetVal = MimeUtility.decodeText(sRetVal);
    return sRetVal;
  }

  // ---------------------------------------------------------------------------

  public String getFileName()
    throws UnsupportedEncodingException,MessagingException {
    return MailHeadersHelper.getFileName(oMsg);
  }

  // ---------------------------------------------------------------------------

  public static String getEncoding(MimeMessage oMsg)
    throws UnsupportedEncodingException,MessagingException {
    String sRetVal = oMsg.getEncoding();
    if (sRetVal!=null)
      sRetVal = MimeUtility.decodeText(sRetVal);
    return sRetVal;
  }

  // ---------------------------------------------------------------------------

  public String getEncoding()
    throws UnsupportedEncodingException,MessagingException {
    return MailHeadersHelper.getEncoding(oMsg);
  }

  // ---------------------------------------------------------------------------

  public static String getSubject(MimeMessage oMsg)
    throws UnsupportedEncodingException,MessagingException {
    String sRetVal = oMsg.getSubject();
    if (sRetVal!=null)
      sRetVal = MimeUtility.decodeText(sRetVal);
    return sRetVal;
  }

  // ---------------------------------------------------------------------------

  public String getSubject()
    throws UnsupportedEncodingException,MessagingException {
    return MailHeadersHelper.getSubject(oMsg);
  }

  // ---------------------------------------------------------------------------

  public static Timestamp getSentTimestamp(MimeMessage oMsg)
    throws MessagingException {
    Timestamp tsSent;
    if (oMsg.getSentDate()!=null)
      tsSent = new Timestamp(oMsg.getSentDate().getTime());
    else
      tsSent = null;
    return tsSent;
  }

  // ---------------------------------------------------------------------------

  public Timestamp getSentTimestamp()
    throws MessagingException {
    return MailHeadersHelper.getSentTimestamp(oMsg);
  }

  // ---------------------------------------------------------------------------

  public static Timestamp getReceivedTimestamp(MimeMessage oMsg)
    throws MessagingException {
    Timestamp tsReceived;
    if (oMsg.getReceivedDate()!=null)
      tsReceived = new Timestamp(oMsg.getReceivedDate().getTime());
    else
      tsReceived = null;
    return tsReceived;
  }

  // ---------------------------------------------------------------------------

  public Timestamp getReceivedTimestamp()
    throws MessagingException {
    return MailHeadersHelper.getReceivedTimestamp(oMsg);
  }

  // ---------------------------------------------------------------------------

  public static String getPriority(MimeMessage oMsg)
    throws MessagingException {
    String sPriority;
    String sXPriority = oMsg.getHeader("X-Priority",null);
    if (sXPriority==null) {
      sPriority = null;
    } else {
      sPriority = "";
      for (int x=0; x<sXPriority.length(); x++) {
        char cAt = sXPriority.charAt(x);
        if (cAt>=(char)48 && cAt<=(char)57) sPriority += cAt;
      } // next
      if (sPriority.length()>10)
    	  sPriority = sPriority.substring(0,10);
    } // fi
    return sPriority;
  }

  // ---------------------------------------------------------------------------

  public String getPriority()
    throws MessagingException {
    return MailHeadersHelper.getPriority(oMsg);
  }

  // ---------------------------------------------------------------------------

  public static Flags getFlags(MimeMessage oMsg)
    throws MessagingException {
    Flags oFlgs = oMsg.getFlags();
    if (oFlgs==null) oFlgs = new Flags();
    return oFlgs;
  }

  // ---------------------------------------------------------------------------

  public Flags getFlags() throws MessagingException {
    return MailHeadersHelper.getFlags(oMsg);
  }

  // ---------------------------------------------------------------------------

  public InternetAddress getFrom () throws MessagingException {
    InternetAddress oAddr = null;
    Address[] aAddrs = oMsg.getFrom();    
    
    if (aAddrs!=null)
      if (aAddrs.length>0)
        oAddr = (InternetAddress) aAddrs[0];

    return oAddr;
  } // getFrom

  // ---------------------------------------------------------------------------

  public static boolean isSpam(MimeMessage oMsg)
    throws MessagingException {
    boolean bIsSpam;
    String sXSpam = oMsg.getHeader("X-Spam-Flag",null);
    if (sXSpam!=null)
      bIsSpam = (sXSpam.toUpperCase().indexOf("YES")>=0 || sXSpam.toUpperCase().indexOf("TRUE")>=0 || sXSpam.indexOf("1")>=0);
    else
      bIsSpam = false;
    return bIsSpam;
  }

  // ---------------------------------------------------------------------------

  public boolean isSpam()
    throws MessagingException {
    return MailHeadersHelper.isSpam(oMsg);
  }

  // ---------------------------------------------------------------------------

  /**
   * <p>Get headers in XML format</p>
   * @return String with syntax:<br/>
   * &lt;msg&gt;
   * &lt;num&gt;[1..n]&lt;/num&gt;
   * &lt;id&gt;message unique identifier&lt;/id&gt;
   * &lt;len&gt;message length in bytes&lt;/len&gt;
   * &lt;type&gt;message content-type&lt;/type&gt;
   * &lt;disposition&gt;message content-disposition&lt;/disposition&gt;
   * &lt;priority&gt;X-Priority header&lt;/priority&gt;
   * &lt;spam&gt;X-Spam-Flag header&lt;/spam&gt;
   * &lt;subject&gt;&lt;![CDATA[message subject]]&gt;&lt;/subject&gt;
   * &lt;sent&gt;yyy-mm-dd hh:mi:ss&lt;/sent&gt;
   * &lt;received&gt;yyy-mm-dd hh:mi:ss&lt;/received&gt;
   * &lt;from&gt;&lt;![CDATA[personal name or e-mail of sender]]&gt;&lt;/from&gt;
   * &lt;to&gt;&lt;![CDATA[personal name or e-mail of receiver]]&gt;&lt;/to&gt;
   * &lt;size&gt;integer size in kilobytes&lt;/size&gt;
   * &lt;err&gt;error description (if any)&lt;/err&gt;
   * &lt;/msg&gt;
   * @since 4.0
   */
  public String toXML() {
	String sHeader;
	StringBuffer oXBuffer = new StringBuffer(1024);
	
	if (DebugFile.trace) {
	  DebugFile.writeln("Begin HeadersHelper.toXML()");
	  DebugFile.incIdent();
	}

	try {
      oXBuffer.append("<msg>");
      oXBuffer.append("<num>"+String.valueOf(oMsg.getMessageNumber())+"</num>");
      sHeader = decodeMessageId(oMsg);
      if (null!=sHeader)
        oXBuffer.append("<id><![CDATA["+sHeader.replace('\n',' ')+"]]></id>");
      else
        oXBuffer.append("<id><![CDATA[]]></id>");
      oXBuffer.append("<len>"+String.valueOf(oMsg.getSize())+"</len>");
      String sCType = oMsg.getContentType();
      int iCType = sCType.indexOf(';');
      if (iCType>0) sCType = sCType.substring(0, iCType);
      oXBuffer.append("<type>"+sCType+"</type>");
      String sDisposition = oMsg.getContentType().substring(iCType+1).trim();
      int iDisposition = sDisposition.indexOf(';');
      if (iDisposition>0) sDisposition = sDisposition.substring(0, iDisposition);
      int iEq = sDisposition.indexOf('=');
      if (iEq>0) sDisposition = sDisposition.substring(iEq+1);
      oXBuffer.append("<disposition>"+sDisposition+"</disposition>");
      sHeader = oMsg.getHeader("X-Priority","");
      oXBuffer.append("<priority>"+(sHeader==null ? "" : sHeader)+"</priority>");
      sHeader = oMsg.getHeader("X-Spam-Flag","");    
      oXBuffer.append("<spam>"+(sHeader==null ? "" : sHeader)+"</spam>");
      sHeader = oMsg.getSubject();
      if (null==sHeader) sHeader = "no subject"; else sHeader = sHeader.replace('\n',' ');
      oXBuffer.append("<subject><![CDATA["+Html.encode(sHeader)+"]]></subject>");
      if (oMsg.getSentDate()==null)
        oXBuffer.append("<sent></sent>");
      else
        oXBuffer.append("<sent>"+DateFrmt.format(oMsg.getSentDate())+"</sent>");      
      if (oMsg.getReceivedDate()==null)
        oXBuffer.append("<received></received>");
      else
        oXBuffer.append("<received>"+DateFrmt.format(oMsg.getReceivedDate())+"</received>");      
      if (!oMsg.isSet(Flags.Flag.DELETED)) {
        try {
      	  InternetAddress oAdr = getFrom();
          if (oAdr!=null) {
          	sHeader = oAdr.getPersonal();
          	if (null==sHeader) sHeader = oAdr.getAddress(); else sHeader = MimeUtility.decodeText(sHeader);
          	if (null==sHeader) sHeader = oMsg.getHeader("Return-Path"," ");
          	if (null==sHeader) sHeader = "";          	
      	    oXBuffer.append("<from><![CDATA["+Html.encode(sHeader)+"]]></from>");
		    oAdr = null;
          }
          else {
            sHeader = oMsg.getHeader("Return-Path"," ");
            if (null==sHeader) sHeader = "";
            oXBuffer.append("<from><![CDATA["+Html.encode(sHeader)+"]]></from>");
          }
        } catch (AddressException adr) {
          oXBuffer.append("<from></from>");
        }
      } // fi (!DELETED)
      int iLen = oMsg.getSize();            
      if (iLen<=1024)
        oXBuffer.append("<kb>1</kb>");
      else
        oXBuffer.append("<kb>"+String.valueOf(iLen/1024)+"</kb>");
      oXBuffer.append("<err/>");
    } catch (Exception err) {
	  try { if (DebugFile.trace) DebugFile.writeln(err.getClass().getName()+" "+err.getMessage()+"\n"+StackTraceUtil.getStackTrace(err)); } catch (java.io.IOException ignore) { }
      if (err.getMessage()!=null)
        oXBuffer.append("<err><![CDATA["+err.getClass().getName()+" "+err.getMessage().replace('\n',' ')+"]]></err>");
      else
        oXBuffer.append("<err><![CDATA["+err.getClass().getName()+"]]></err>");
    }
    oXBuffer.append("</msg>");      

	if (DebugFile.trace) {
	  DebugFile.decIdent();
	  String sCType = null;
	  try { sCType = oMsg.getContentType(); } catch (Exception ignore) {}
	  DebugFile.writeln("End HeadersHelper.toXML() : "+sCType+" "+String.valueOf(oXBuffer.length())+" bytes");
	}

    return oXBuffer.toString();
  } // toXML

}