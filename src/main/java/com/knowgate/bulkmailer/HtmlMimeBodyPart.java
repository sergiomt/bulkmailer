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

import java.net.URLEncoder;

/*
Copyright (C) 2003-2014 KnowGate All rights reserved.

Redistribution and use in source and binary forms,
with or without modification, are permitted according
to the terms of GNU Lesser General Public License 3
provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. Neither the name of the author nor the names of its contributors
   may be used to endorse or promote products derived from this software
   without specific prior written permission.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

*/

import java.util.ArrayList;
import java.util.HashMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.htmlparser.Parser;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;
import org.htmlparser.Tag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.TableTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.visitors.NodeVisitor;

import com.knowgate.debug.DebugFile;
import com.knowgate.debug.StackTraceUtil;
import com.knowgate.stringutils.Base64Encoder;

/**
 * <p>Used to perform some maipulations in HTML source code for e-mails</p>
 * @author Sergio Montoro Ten
 * @version 7.0
 */
public class HtmlMimeBodyPart {

  // private Matcher oMatcher;

  private static Pattern oFullHref = null;
  private static Pattern oGoodHref = null;
  private static Pattern oHostHref = null;
  private static Pattern oFullSrc = null;
  private static Pattern oGoodSrc = null;
  private static Pattern oHostSrc = null;

  private String sBody;
  private String sEnc;
  
  private HashMap<String,String> oImgs;
  
  public HtmlMimeBodyPart(String sHtml, String sEncoding) {
    sBody = sHtml;
    sEnc = sEncoding;
    oImgs = new HashMap<String,String>(23);
  }

  public void setHtml(String sHtml) {
  	sBody = sHtml;
  }

  public ArrayList<String> extractHrefs() {

    ArrayList<String> aHrefs = new ArrayList<String>();
	try {
      if (null==oFullHref) oFullHref = Pattern.compile("<a ((accesskey|charset|class|coords|dir|hreflang|id|lang|name|rel|rev|shape|style|tabindex|target|title)\\s*=\\s*[\"']?([^'\"\\r\\n]+)[\"']?)* href\\s*=\\s*[\"']?([^'\"\\r\\n]+)[\"']?", Pattern.CASE_INSENSITIVE);
      Matcher oMatcher = oFullHref.matcher(sBody);
      while (oMatcher.find()) {
	    aHrefs.add(oMatcher.group(4));
      } // wend
    } catch (PatternSyntaxException neverthrown) { } 
    return aHrefs;
  } // extractHrefs()

  /**
   * Get a list of <img src="..." tags that point to a local resource
   * such as src="/tmp/myfile.gif" or src="http://localhost/imgs/myfile.gif"
   */
  public ArrayList<String> extractLocalUrls()
  	throws ArrayIndexOutOfBoundsException {

    ArrayList<String> aLocalUrls = new ArrayList<String>();
    String sSrcUrl, sHrefUrl;
    
    try {
        if (null==oFullHref) oFullHref = Pattern.compile("<a( (accesskey|charset|class|coords|dir|hreflang|id|lang|name|rel|rev|shape|style|tabindex|target|title)\\s*=\\s*[\"']?([^'\"\\r\\n]+)[\"']?)* href\\s*=\\s*[\"']?([^'\"\\r\\n]+)[\"']?", Pattern.CASE_INSENSITIVE);
        if (null==oGoodHref) oGoodHref = Pattern.compile("<a( (accesskey|charset|class|coords|dir|hreflang|id|lang|name|rel|rev|shape|style|tabindex|target|title)\\s*=\\s*[\"']?([^'\"\\r\\n]+)[\"']?)* href\\s*=\\s*[\"']?(http://|https://|mailto:)\\w+([^'\"\\r\\n]+)[\"']?", Pattern.CASE_INSENSITIVE);
        if (null==oHostHref) oHostHref = Pattern.compile("<a( (accesskey|charset|class|coords|dir|hreflang|id|lang|name|rel|rev|shape|style|tabindex|target|title)\\s*=\\s*[\"']?([^'\"\\r\\n]+)[\"']?)* href\\s*=\\s*[\"']?(http://|https://)localhost([^'\"\\r\\n]+)[\"']?", Pattern.CASE_INSENSITIVE);
        if (null==oFullSrc) oFullSrc = Pattern.compile("<img( (align|alt|border|class|dir|height|hspace|id|ismap|lang|longdesc|style|title|usemap|vspace|width)\\s*=\\s*[\"']?([^'\"\\r\\n]+)[\"']?)* src\\s*=\\s*[\"']?([^'\"\\r\\n]+)[\"']?", Pattern.CASE_INSENSITIVE);
	    if (null==oGoodSrc) oGoodSrc = Pattern.compile("<img( (align|alt|border|class|dir|height|hspace|id|ismap|lang|longdesc|style|title|usemap|vspace|width)\\s*=\\s*[\"']?([^'\"\\r\\n]+)[\"']?)* src\\s*=\\s*[\"']?(cid:|http://|https://)([^'\"\\r\\n]+)[\"']?", Pattern.CASE_INSENSITIVE);
	    if (null==oHostSrc) oHostSrc = Pattern.compile("<img( (align|alt|border|class|dir|height|hspace|id|ismap|lang|longdesc|style|title|usemap|vspace|width)\\s*=\\s*[\"']?([^'\"\\r\\n]+)[\"']?)* src\\s*=\\s*[\"']?(http://localhost|https://localhost)([^'\"\\r\\n]+)[\"']?", Pattern.CASE_INSENSITIVE);

  	  Matcher oFullMatcher = oFullSrc.matcher(sBody);
      while (oFullMatcher.find()) {
	    sSrcUrl = oFullMatcher.group();
	  	Matcher oGoodMatcher = oGoodSrc.matcher(sSrcUrl);
	    if (!oGoodMatcher.matches())
		  aLocalUrls.add(sSrcUrl);
      } // wend

  	  oFullMatcher = oFullSrc.matcher(sBody);
      while (oFullMatcher.find()) {
	    sSrcUrl = oFullMatcher.group();
	  	Matcher oHostMatcher = oHostSrc.matcher(sSrcUrl);
	    if (oHostMatcher.matches())
		  aLocalUrls.add(sSrcUrl);
      } // wend
      
  	  Matcher oHrefMatcher = oFullHref.matcher(sBody);
      while (oHrefMatcher.find()) {
		sHrefUrl = oHrefMatcher.group();
	  	Matcher oGoodMatcher = oGoodHref.matcher(sHrefUrl);
	    if (!oGoodMatcher.matches())
		  aLocalUrls.add(sHrefUrl);
      } // wend

  	  oHrefMatcher = oFullHref.matcher(sBody);
      while (oHrefMatcher.find()) {
		sHrefUrl = oHrefMatcher.group();
	  	Matcher oHostMatcher = oHostHref.matcher(sHrefUrl);
		if (oHostMatcher.matches())
		  aLocalUrls.add(sHrefUrl);		
      } // wend
    } catch (PatternSyntaxException neverthrown) { } 

	return aLocalUrls;
  } // extractLocalUrls
  	
  public HashMap getImagesCids() {
    return oImgs;
  }

  private String doSubstitution(final String sBase, final String sAttributeName,
		                        final String sFormerValue, final String sNewValue)
  	throws ParserException {
  	
  	String sMatch = "";
  	
  	if (DebugFile.trace) DebugFile.writeln("HtmlMomeBodyPart.doSubstitution(..., "+sAttributeName+","+sFormerValue+","+sNewValue+")");

    final String sPattern = "("+sAttributeName.toLowerCase()+"|"+sAttributeName.toUpperCase()+"|"+sAttributeName+")\\s*=\\s*(\"|')?" + sFormerValue + "(\"|')?";

    try {

      if (DebugFile.trace) DebugFile.writeln("Pattern.compile(\""+sPattern+"\")");
      java.util.regex.Pattern oPattrn = java.util.regex.Pattern.compile(sPattern);
      Matcher oMatchr = oPattrn.matcher(sBase);
      
      if (oMatchr.find()) {
    	sMatch = oMatchr.group();

    	if (sMatch.length()==0) throw new ParserException("Match could not be retrieved for pattern " + sPattern);
      	else if (DebugFile.trace) DebugFile.writeln("match found "+sMatch);
      	
    	final int iDquote = sMatch.indexOf('"');
      	final int iSquote = sMatch.indexOf("'");
      	char cQuote = (char) 0;
      	if (iDquote>0 && iSquote>0)
      	  cQuote = iDquote<iSquote ? (char)34 : (char)39;
      	else if (iDquote>0)
      	  cQuote = (char)34;
      	else if (iSquote>0)
      	  cQuote = (char)39;
		try {
          if (cQuote==(char)0) {
		    if (DebugFile.trace) DebugFile.writeln("Matcher.replaceAll("+sMatch.substring(0,sAttributeName.length())+"="+sNewValue+")");
		    return oMatchr.replaceAll(sMatch.substring(0,sAttributeName.length())+"="+sNewValue);
		  } else {
	        if (DebugFile.trace) DebugFile.writeln("Matcher.replaceAll("+sMatch.substring(0,sAttributeName.length())+"="+cQuote+sNewValue+cQuote+")");            
	        return oMatchr.replaceAll(sMatch.substring(0,sAttributeName.length())+"="+cQuote+sNewValue+cQuote);
		  }
      	} catch (Exception xcpt) { throw new ParserException(xcpt.getMessage()); }
      } else {
      	return sBase;
      } // fi (oMatcher.contains())
    } catch (PatternSyntaxException mpe) {
      if (DebugFile.trace) {
        DebugFile.writeln("PatternSyntaxException " + mpe.getMessage());
        try { DebugFile.writeln(StackTraceUtil.getStackTrace(mpe)); } catch (Exception ignore) { }
      }
      throw new ParserException("PatternSyntaxException " + mpe.getMessage()+ " pattern " + sPattern + " substitution " + sNewValue, mpe);        
    }
    catch (ArrayIndexOutOfBoundsException aiob) {
      String sStack = "";
      try { sStack =  StackTraceUtil.getStackTrace(aiob); } catch (Exception ignore) { }
      if (DebugFile.trace) {
        DebugFile.writeln("ArrayIndexOutOfBoundsException " + aiob.getMessage());
        DebugFile.writeln(sStack);
      }
      int iAt = sStack.indexOf("at "); 
      if (iAt>0) {
      	int iLf = sStack.indexOf("\n",iAt);
      	if (iLf>iAt)
          throw new ParserException("ArrayIndexOutOfBoundsException " + sStack.substring(iAt,iLf) + " " + aiob.getMessage()+ " attribute " + sAttributeName + " pattern " + sPattern + " match " + sMatch + " former value " + sFormerValue + " substitution " + sNewValue, aiob);
        else
          throw new ParserException("ArrayIndexOutOfBoundsException " + sStack.substring(iAt) + " " + aiob.getMessage()+ " attribute " + sAttributeName + " pattern " + sPattern + " match " + sMatch + " former value " + sFormerValue + " substitution " + sNewValue, aiob);        	
      } else {
        throw new ParserException("ArrayIndexOutOfBoundsException " + aiob.getMessage()+ " attribute " + sAttributeName + " pattern " + sPattern + " match " + sMatch + " former value " + sFormerValue + " substitution " + sNewValue, aiob);
      }
    }
  } // doSubstitution

  /**
   * <p>Add a preffix to &lt;IMG SRC="..."&gt; &lt;TABLE BACKGROUND="..."&gt; and &lt;TD BACKGROUND="..."&gt; tags</p>
   * @param sPreffix String preffix to be added to &lt;img&gt; src attribute and &lt;table&gt; and &lt;td&gt; background
   * @return New HTML source with preffixed attributes
   * @throws ParserException
   */
  public String addPreffixToImgSrc(String sPreffix)
  	throws ParserException {

    if (DebugFile.trace) {
      DebugFile.writeln("Begin HtmlMimeBodyPart.addPreffixToImgSrc("+sPreffix+")");
      DebugFile.incIdent();
    }
    
    int iSlash;
    Parser oPrsr;
    String sCid, sSrc;
    String sBodyCid = sBody;
    NodeList oCollectionList;
    TagNameFilter oImgFilter;

	// **********************************************************************
	// Replace <IMG SRC="..." >

    oPrsr = Parser.createParser(sBodyCid, sEnc);
		
    oCollectionList = new NodeList();
    oImgFilter = new TagNameFilter ("IMG");
    for (NodeIterator e = oPrsr.elements(); e.hasMoreNodes();)
      e.nextNode().collectInto(oCollectionList, oImgFilter);

    int nImgs = oCollectionList.size();

    if (DebugFile.trace) DebugFile.writeln("Images NodeList.size() = " + String.valueOf(nImgs));

    for (int i=0; i<nImgs; i++) {
		ImageTag oImgTag = (ImageTag) oCollectionList.elementAt(i);
			
        sSrc = oImgTag.extractImageLocn().replace('\\','/');
        
        if (sSrc.length()==0) throw new ParserException("image src is empty for tag "+oImgTag.toHtml());
		
		if (DebugFile.trace) DebugFile.writeln("Processing image location "+sSrc);
		
        // Keep a reference to every related image name so that the same image is not included twice in the message
        if (!oImgs.containsKey(sSrc)) {

          // Find last slash from image url
          iSlash = sSrc.lastIndexOf('/');
		  
          // Take image name
          if (iSlash>=0) {
            while (sSrc.charAt(iSlash)=='/') { if (++iSlash==sSrc.length()) break; }
              sCid = sSrc.substring(iSlash);
          }
          else {
            sCid = sSrc;
          }
          if (DebugFile.trace) DebugFile.writeln("HashMap.put("+sSrc+","+sCid+")");

          if  (sCid.length()>0) {
            oImgs.put(sSrc, sCid);            
            sBodyCid = doSubstitution (sBodyCid, "Src", oImgTag.extractImageLocn().replace("\\", "\\\\").replace(".","\\x2E"), sPreffix+oImgs.get(sSrc));        
          }
        } // fi (!oImgs.containsKey(sSrc))
    } // next

	// **********************************************************************
	// Replace <TABLE BACKGROUND="..." >
	  
    oCollectionList = new NodeList();
    TagNameFilter oTableFilter = new TagNameFilter("TABLE");
    oPrsr = Parser.createParser(sBodyCid, sEnc);
    for (NodeIterator e = oPrsr.elements(); e.hasMoreNodes();)
      e.nextNode().collectInto(oCollectionList, oTableFilter);
          
    nImgs = oCollectionList.size();

    if (DebugFile.trace) DebugFile.writeln("Tables NodeList.size() = " + String.valueOf(nImgs));

    for (int i=0; i<nImgs; i++) {

      sSrc = ((TableTag) oCollectionList.elementAt(i)).getAttribute("background");
      if (sSrc!=null) {
        if (sSrc.length()>0) {
          sSrc = sSrc.replace('\\','/');

		  if (DebugFile.trace) DebugFile.writeln("Processing background location "+sSrc);

          // Keep a reference to every related image name so that the same image is not included twice in the message
          if (!oImgs.containsKey(sSrc)) {

            // Find last slash from image url
            iSlash = sSrc.lastIndexOf('/');

            // Take image name
            if (iSlash>=0) {
              while (sSrc.charAt(iSlash)=='/') { if (++iSlash==sSrc.length()) break; }
                sCid = sSrc.substring(iSlash);
            } // fi
            else {
              sCid = sSrc;
            }

            if (DebugFile.trace) DebugFile.writeln("HashMap.put("+sSrc+","+sCid+")");

            oImgs.put(sSrc, sCid);
          } // fi (!oImgs.containsKey(sSrc))          
		  sBodyCid = doSubstitution (sBodyCid, "Background", ((TableTag) oCollectionList.elementAt(i)).getAttribute("background").replace("\\","\\\\").replace(".","\\x2E"), sPreffix+oImgs.get(sSrc));
        } // fi
      } // fi
    } // next

	// **********************************************************************
	// Replace <TD BACKGROUND="..." >
	  
    oCollectionList = new NodeList();
    TagNameFilter oTDFilter = new TagNameFilter("TD");
    oPrsr = Parser.createParser(sBodyCid, sEnc);
    for (NodeIterator e = oPrsr.elements(); e.hasMoreNodes();)
      e.nextNode().collectInto(oCollectionList, oTDFilter);
          
    nImgs = oCollectionList.size();

    if (DebugFile.trace) DebugFile.writeln("TD NodeList.size() = " + String.valueOf(nImgs));

    for (int i=0; i<nImgs; i++) {

      sSrc = ((TableColumn) oCollectionList.elementAt(i)).getAttribute("background");
      if (sSrc!=null) {
        if (sSrc.length()>0) {
          sSrc = sSrc.replace('\\','/');

		  if (DebugFile.trace) DebugFile.writeln("Processing td bg location "+sSrc);

          // Keep a reference to every related image name so that the same image is not included twice in the message
          if (!oImgs.containsKey(sSrc)) {

            // Find last slash from image url
            iSlash = sSrc.lastIndexOf('/');

            // Take image name
            if (iSlash>=0) {
              while (sSrc.charAt(iSlash)=='/') { if (++iSlash==sSrc.length()) break; }
                sCid = sSrc.substring(iSlash);
            } // fi
            else {
              sCid = sSrc;
            }

            if (DebugFile.trace) DebugFile.writeln("HashMap.put("+sSrc+","+sCid+")");

            oImgs.put(sSrc, sCid);
          } // fi (!oImgs.containsKey(sSrc))
		  sBodyCid = doSubstitution(sBodyCid, "Background", ((TableColumn) oCollectionList.elementAt(i)).getAttribute("background").replace("\\","\\\\").replace(".","\\x2E"), sPreffix+oImgs.get(sSrc));
        } // fi
      } // fi
    } // next

    if (DebugFile.trace) {
	  DebugFile.write(sBodyCid);
      DebugFile.decIdent();
      DebugFile.writeln("End HtmlMimeBodyPart.addPreffixToImgSrc()");
    }

    return sBodyCid;
  } // addPreffixToImgSrcs


  /**
   * <p>Remove a preffix from &lt;IMG SRC="..."&gt; &lt;TABLE BACKGROUND="..."&gt; and &lt;TD BACKGROUND="..."&gt; tags</p>
   * @param sPreffix String preffix to be removed from &lt;img&gt; src attribute and &lt;table&gt; and &lt;td&gt; background
   * @return New HTML source with unpreffixed attributes
   * @throws ParserException
   */
  public String removePreffixFromImgSrcs(String sPreffix)
  	throws ParserException {

    if (DebugFile.trace) {
      DebugFile.writeln("Begin HtmlMimeBodyPart.removePreffixFromImgSrcs("+sPreffix+")");
      DebugFile.incIdent();
    }

    int iSlash;
    Parser oPrsr;
    String sCid, sSrc;
    String sBodyCid = sBody;
    NodeList oCollectionList;
    TagNameFilter oImgFilter;

	// **********************************************************************
	// Replace <IMG SRC="..." >

    oPrsr = Parser.createParser(sBodyCid, sEnc);
		
    oCollectionList = new NodeList();
    oImgFilter = new TagNameFilter ("IMG");
    for (NodeIterator e = oPrsr.elements(); e.hasMoreNodes();)
      e.nextNode().collectInto(oCollectionList, oImgFilter);

    int nImgs = oCollectionList.size();

    if (DebugFile.trace) DebugFile.writeln("Images NodeList.size() = " + String.valueOf(nImgs));

    for (int i=0; i<nImgs; i++) {

        sSrc = (((ImageTag) oCollectionList.elementAt(i)).extractImageLocn()).replace('\\','/');

        // Keep a reference to every related image name so that the same image is not included twice in the message
        if (!oImgs.containsKey(sSrc)) {

          // Find last slash from image url
          iSlash = sSrc.lastIndexOf('/');
		  
          // Take image name
          if (iSlash>=0) {
            while (sSrc.charAt(iSlash)=='/') { if (++iSlash==sSrc.length()) break; }
              sCid = sSrc.substring(iSlash);
          }
          else {
            sCid = sSrc;
          }

          // String sUid = Gadgets.generateUUID();
          // sCid = sUid.substring(0,12)+"$"+sUid.substring(12,20)+"$"+sUid.substring(20,28)+"@hipergate.org";

          if (DebugFile.trace) DebugFile.writeln("HashMap.put("+sSrc+","+sCid+")");

          oImgs.put(sSrc, sCid);
        } // fi (!oImgs.containsKey(sSrc))
        
        String sImgSrc = ((ImageTag) oCollectionList.elementAt(i)).extractImageLocn();
        if (sImgSrc.startsWith(sPreffix)) {
          sBodyCid = doSubstitution(sBodyCid, "Src", sImgSrc.replace("\\","\\\\").replace(".","\\x2E"), sImgSrc.substring(sPreffix.length()));
        }
        
    } // next

	// **********************************************************************
	// Replace <TABLE BACKGROUND="..." >
	  
    oCollectionList = new NodeList();
    TagNameFilter oTableFilter = new TagNameFilter("TABLE");
    oPrsr = Parser.createParser(sBodyCid, sEnc);
    for (NodeIterator e = oPrsr.elements(); e.hasMoreNodes();)
      e.nextNode().collectInto(oCollectionList, oTableFilter);
          
    nImgs = oCollectionList.size();

    if (DebugFile.trace) DebugFile.writeln("Tables NodeList.size() = " + String.valueOf(nImgs));

    for (int i=0; i<nImgs; i++) {

      sSrc = ((TableTag) oCollectionList.elementAt(i)).getAttribute("background");
      if (sSrc!=null) {
        if (sSrc.length()>0) {
          sSrc = sSrc.replace('\\','/');

          // Keep a reference to every related image name so that the same image is not included twice in the message
          if (!oImgs.containsKey(sSrc)) {

            // Find last slash from image url
            iSlash = sSrc.lastIndexOf('/');

            // Take image name
            if (iSlash>=0) {
              while (sSrc.charAt(iSlash)=='/') { if (++iSlash==sSrc.length()) break; }
                sCid = sSrc.substring(iSlash);
            } // fi
            else {
              sCid = sSrc;
            }

            if (DebugFile.trace) DebugFile.writeln("HashMap.put("+sSrc+","+sCid+")");

            oImgs.put(sSrc, sCid);
          } // fi (!oImgs.containsKey(sSrc))

          String sBckGrnd = ((TableTag) oCollectionList.elementAt(i)).getAttribute("background");
          if (sBckGrnd.startsWith(sPreffix)) {
            sBodyCid = doSubstitution(sBodyCid, "Background", sBckGrnd.replace("\\","\\\\").replace(".","\\x2E"), sBckGrnd.substring(sPreffix.length()));
          }

        } // fi
      } // fi
    } // next

	// **********************************************************************
	// Replace <TD BACKGROUND="..." >
	  
    oCollectionList = new NodeList();
    TagNameFilter oTDFilter = new TagNameFilter("TD");
    oPrsr = Parser.createParser(sBodyCid, sEnc);
    for (NodeIterator e = oPrsr.elements(); e.hasMoreNodes();)
      e.nextNode().collectInto(oCollectionList, oTDFilter);
          
    nImgs = oCollectionList.size();

    if (DebugFile.trace) DebugFile.writeln("TD NodeList.size() = " + String.valueOf(nImgs));

    for (int i=0; i<nImgs; i++) {

      sSrc = ((TableColumn) oCollectionList.elementAt(i)).getAttribute("background");
      if (sSrc!=null) {
        if (sSrc.length()>0) {
          sSrc = sSrc.replace('\\','/');

          // Keep a reference to every related image name so that the same image is not included twice in the message
          if (!oImgs.containsKey(sSrc)) {

            // Find last slash from image url
            iSlash = sSrc.lastIndexOf('/');

            // Take image name
            if (iSlash>=0) {
              while (sSrc.charAt(iSlash)=='/') { if (++iSlash==sSrc.length()) break; }
                sCid = sSrc.substring(iSlash);
            } // fi
            else {
              sCid = sSrc;
            }

            if (DebugFile.trace) DebugFile.writeln("HashMap.put("+sSrc+","+sCid+")");

            oImgs.put(sSrc, sCid);

            String sTdBckg = ((TableColumn) oCollectionList.elementAt(i)).getAttribute("background");
            if (sTdBckg.startsWith(sPreffix)) {
            	sBodyCid = doSubstitution(sBodyCid, "Background", sTdBckg.replace("\\","\\\\").replace(".","\\x2E"), sTdBckg.substring(sPreffix.length()));
            }
          } // fi (!oImgs.containsKey(sSrc))
        } // fi
      } // fi
    } // next

    if (DebugFile.trace) {
	  DebugFile.write(sBodyCid);
      DebugFile.decIdent();
      DebugFile.writeln("End HtmlMimeBodyPart.removePreffixFromImgSrcs()");
    }

    return sBodyCid;
  } // removePreffixFromImgSrcs

  /**
   * <p>Replace a preffix from &lt;IMG SRC="..."&gt; &lt;TABLE BACKGROUND="..."&gt; and &lt;TD BACKGROUND="..."&gt; tags with another one</p>
   * @param sFormerPreffix String
   * @param sNewPreffix String
   * @return New HTML source with replaced preffixed attributes
   * @throws ParserException
   */

  public String replacePreffixFromImgSrcs(String sFormerPreffix, String sNewPreffix)
  	throws ParserException {
	HtmlMimeBodyPart oHtml = new HtmlMimeBodyPart(removePreffixFromImgSrcs(sFormerPreffix), sEnc);
	return oHtml.addPreffixToImgSrc(sNewPreffix);
  } // replacePreffixFromImgSrcs


  /**
   * <p>Replace HREF targets with an intermediate page for tracking click through</p>
   * @param sRedirectorUrl String Full HTTP path where all HREF URLs must be redirected
   * @return New HTML source with replaced HREF attributes
   * @throws ParserException
   */

  public String addClickThroughRedirector(final String sRedirectorUrl)
  	throws ParserException {

    if (DebugFile.trace) {
      DebugFile.writeln("Begin HtmlMimeBodyPart.addClickThroughRedirector("+sRedirectorUrl+")");
      DebugFile.incIdent();
    }
    
    final NodeVisitor linkVisitor = new NodeVisitor() {

        public void visitTag(Tag tag) {
            // Process any tag/node in your HTML 
            String name = tag.getTagName();
            // Set the Link's target to _blank if the href is external
            if ("a".equalsIgnoreCase(name)) {
            	LinkTag lnk = (LinkTag) tag;
            	String sUrl = lnk.extractLink();
                if(sUrl.startsWith("http://") || sUrl.startsWith("https://")) {
                    lnk.setLink(sRedirectorUrl+URLEncoder.encode(Base64Encoder.encode(sUrl)));
                }
            }
        }
    };

    Parser parser = Parser.createParser(sBody, sEnc);
    NodeList list = parser.parse(null);
    list.visitAllNodesWith(linkVisitor);

    if (DebugFile.trace) {
      DebugFile.decIdent();
      DebugFile.writeln("End HtmlMimeBodyPart.addClickThroughRedirector()");
    }

    return list.toHtml();
  } // addClickThroughRedirector

}