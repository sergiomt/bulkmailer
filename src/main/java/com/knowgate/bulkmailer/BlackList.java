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

public abstract class BlackList {

  public abstract String[] emails();
  
  public abstract void add(String email);
  
  public abstract BlackList load() throws JDOException;

  private boolean contains(String email, int imin, int imax) {
    if (imax < imin) {
      return false;
    } else {
      int imid = imin + ((imax - imin) / 2);
      String emailn = emails()[imid];
      if (emailn.compareTo(email)>0)
        return contains(email, imin, imid-1);
      else if (emailn.compareTo(email)<0)
      	return contains(email, imid+1, imax);
      else
        return true;
    }
  }
  
  public boolean contains(String email) {
  	return contains(email.toLowerCase().trim(), 0, emails().length-1);
  }
  
}