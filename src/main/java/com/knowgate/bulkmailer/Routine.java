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

import java.util.List;
import java.util.Map;

import javax.jdo.JDOException;

import org.judal.storage.Record;

public interface Routine {

  public String id();

  public SingleEmailAtom[] atoms() throws JDOException;

  public SingleEmailAtom atom(int n) throws JDOException;
  
  public int atomCount() throws JDOException;
  
  public List<Record> archived() throws JDOException;
  
  public void process (SingleEmailAtom emm);

  public Map<String,String> properties();

  public String group();

  public void setGroup(String g);
  
  public RoutineStatus status();
  
  public void setStatus(RoutineStatus s);

  public void updateStatus(RoutineStatus s);

}