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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RecipientsList {
	
  private String[] aRecipients;
  private String[] aBlackList;

  private Pattern oAllowPattern;
  private Pattern oDenyPattern;
  
  public RecipientsList() {
  	oAllowPattern = oDenyPattern = null;
  }

  public RecipientsList(String sAllowRegExp, String sDenyRegExp)
  	throws PatternSyntaxException {
  	oAllowPattern = Pattern.compile(sAllowRegExp);
  	oDenyPattern = Pattern.compile(sDenyRegExp);
  }
  
  private String[] concatArrays(String[] a1, String a2[]) {
	  final int l1 = a1.length;
	  final int l2 = a2.length;
	  final int ll = l1+l2;
	  String[] aRetVal = Arrays.copyOf(a1, ll);
    for (int e=0; e<l2; e++) aRetVal[e+l1] = a2[e];
      return aRetVal;
  } // concatArrays
  
  public String getAllowPattern() {
  	if (oAllowPattern==null)
  		return "";
  	else
      return oAllowPattern.toString();
  }

  public void setAllowPattern(String sRegExp) throws PatternSyntaxException {
  	oAllowPattern = Pattern.compile(sRegExp);
  }

  public String getDenyPattern() {
  	if (oDenyPattern==null)
  		return "";
  	else
    return oDenyPattern.toString();
  }

  public void setDenyPattern(String sRegExp) throws PatternSyntaxException {
  	oDenyPattern = Pattern.compile(sRegExp);
  }
  
  public String[] getBlackListedEmails() {
    return aBlackList;
  }

  public void addToBlackList(String[] aEMails) {
    if (aEMails!=null) {
      if (aEMails.length>0) {
      	if (aBlackList==null)
      	  aBlackList = aEMails;
      	else
      	  aBlackList = concatArrays(aBlackList, aEMails);
      	Arrays.sort(aBlackList, String.CASE_INSENSITIVE_ORDER);
      }
    }
  } // addBlackList
 
  public void clearRecipients() {
    aRecipients = null;
  }

  public String[] getRecipients() {
    return aRecipients;
  }

  private boolean isAllowed(String sRecipient) throws ArrayIndexOutOfBoundsException {
    boolean bAllowed = true;
  	if (oAllowPattern!=null)  		      	
    	bAllowed = bAllowed && oAllowPattern.matcher(sRecipient).matches();
    if (oDenyPattern!=null)
  	  bAllowed = bAllowed && !oDenyPattern.matcher(sRecipient).matches();
  	return bAllowed;
  }
  
  private void addRecipientToListWithoutDuplicates(String sRecipient, ArrayList<String> aList) {
    final String sRecipientTrimmed = sRecipient.trim();
  	if (aBlackList==null) {
      if (sRecipientTrimmed.length()>0)
      	aList.add(sRecipientTrimmed);
    } else if (Arrays.binarySearch(aBlackList, sRecipientTrimmed.toLowerCase(), String.CASE_INSENSITIVE_ORDER)<0) {
      if (sRecipientTrimmed.length()>0)
      	aList.add(sRecipientTrimmed);
    } // fi
  }
  
  public void addRecipients(String[] aEMails) throws ArrayIndexOutOfBoundsException {
    ArrayList<String> oRecipientsWithoutDuplicates;
  		
    if (aEMails!=null) {
      if (aEMails.length>0) {
        
      	if (aRecipients==null)
          aRecipients = aEMails;
        else
          aRecipients = concatArrays(aRecipients, aEMails);
        
        final int nRecipients = aRecipients.length;
        
        Arrays.sort(aRecipients, String.CASE_INSENSITIVE_ORDER);
  		
  		  oRecipientsWithoutDuplicates = new ArrayList<String>(nRecipients);
  	  	  		    	  	  
  	    for (int r=0; r<nRecipients-1; r++)  		    
  		    if (isAllowed(aRecipients[r]))
  	  	    if (!aRecipients[r].equalsIgnoreCase(aRecipients[r+1]))
  	  	    	addRecipientToListWithoutDuplicates(aRecipients[r], oRecipientsWithoutDuplicates);
  	    
  		  if (isAllowed(aRecipients[nRecipients-1]))
  	    	addRecipientToListWithoutDuplicates(aRecipients[nRecipients-1], oRecipientsWithoutDuplicates);

  	    aRecipients = oRecipientsWithoutDuplicates.toArray(new String[oRecipientsWithoutDuplicates.size()]);
  	    	  
      } // fi (aEMails != {})
    } // fi (aEMails != null)
  } // addRecipients
  
}