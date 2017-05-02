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

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;

import javax.jdo.JDOException;

import com.knowgate.http.HttpRequest;

import com.knowgate.stringutils.Uid;

import org.judal.storage.Param;
import org.judal.storage.table.View;
import org.judal.storage.table.Table;
import org.judal.storage.table.TableDataSource;
import org.judal.storage.table.ColumnGroup;
import org.judal.storage.table.Record;
import org.judal.storage.table.RecordSet;
import org.judal.storage.relational.RelationalDataSource;
import org.judal.storage.relational.RelationalTable;
import org.judal.storage.relational.RelationalView;
import org.judal.storage.query.AbstractQuery;
import org.judal.storage.query.Predicate;

import static org.judal.storage.query.Operator.*;

import org.judal.jdbc.metadata.SQLFunctions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

import static org.judal.storage.query.Connective.*;

public class Urls {
	
	private RelationalDataSource dts;
	private String wrk;
	private UrlData udt;
	private ClickThrough clt;
	
	private MutableConfiguration<String, String> cacheConfig;
	private Cache<String, String> addrToGuid = null;
	private Cache<String, String> guidToAddr = null;
	
    // private final static String BASE_TABLE = "k_urls b";
    // private final static String COLUMNS_LIST = "b.gu_url,b.url_addr,b.tx_title,b.nu_clicks,b.dt_last_visit,b.de_url";

	public Urls(TableDataSource dts, String wrk, UrlData udt, ClickThrough clt) {
		this.dts = (RelationalDataSource) dts;
		this.wrk = wrk;
		this.udt = udt;
		this.clt = clt; 
		cacheConfig = new MutableConfiguration<String, String>().setTypes(String.class, String.class).setStatisticsEnabled(true);
	}

  private String getPageTitle(String url) {
	  HttpRequest req = null;
	  String title;
	  try {
		req = new HttpRequest(url);
		req.get();
		title = req.getTitle();
	} catch (IOException | URISyntaxException e) {
		title = null;
	}
	return title;
  }

  public String getGuidForAddress(final String addr, final String wrka) throws JDOException {
  	String guid;
  	Table tbl = null;
  	if (addrToGuid==null) {
  	  try {
  	  	addrToGuid = Caching.getCachingProvider().getCacheManager().createCache("addrToGuid", cacheConfig);
  	  } catch (Exception xcpt) {
  	  	throw new JDOException(xcpt.getMessage(),xcpt);
  	  }
  	}
    try {
  	  guid = addrToGuid.get(addr);
  	  if (null==guid) {
	  		tbl = dts.openTable(udt);
  	  	synchronized (this) {
	  		  RecordSet<UrlData> rst = tbl.fetch(udt.fetchGroup(), "url_addr", addr);
	  		  if (rst.size()==0) {
	  	  		guid = Uid.createUniqueKey();
	  	  		addrToGuid.put(addr, guid);
	  	        udt.setGuid(guid);
	  	        udt.setClicks(0);
	  	  		udt.setWorkarea(wrk);
	  	  		udt.setAddress(addr);
	  	  		udt.setTitle(getPageTitle(addr));
	  	  		tbl.store(udt);
	  		  } else {
	  		  	for (Record rec : rst) {
	  		  		if (rec.getString("gu_workarea").equals(wrka)) {
	  	  			  guid = rst.get(0).getString("gu_url");
	  	  			  break;
	  		  		}
	  		  	}
	  		  	if (null==guid) {
		  	  		guid = Uid.createUniqueKey();
		  	  		addrToGuid.put(addr, guid);
		  	      udt.setGuid(guid);
		  	      udt.setClicks(0);
		  	  		udt.setWorkarea(wrk);
		  	  		udt.setAddress(addr);
		  	  		udt.setTitle(getPageTitle(addr));
		  	  		tbl.store(udt);
	  		  	}
	  		  }
  	  	}
	  		tbl.close();
	  		tbl = null;
  	  }
    } catch (Exception xcpt) {
  	  if (tbl!=null) { try { tbl.close(); } catch (Exception ignore) { } }
    	throw new JDOException(xcpt.getMessage(), xcpt);
  	}
    return guid;
  }

  public String getAddressForGuid(final String guid) throws JDOException {
  	String addr;
  	Table tbl = null;
  	if (guidToAddr==null) {
  	  try {
  	  	guidToAddr = Caching.getCachingProvider().getCacheManager().createCache("addrToGuid", cacheConfig);
  	  } catch (Exception xcpt) {
  	  	throw new JDOException(xcpt.getMessage(),xcpt);
  	  }
  	}
    try {
  	  addr = guidToAddr.get(guid);
  	  if (null==addr) {
	  		tbl = dts.openTable(udt);
  	  	synchronized (this) {
	  		  RecordSet<UrlData> rst = tbl.fetch(udt.fetchGroup(), "gu_url", guid);
	  		  if (rst.size()!=0)
	  			  addr = rst.get(0).getString("url_addr");
  	  	}
	  		tbl.close();
	  		tbl = null;
  	  }
    } catch (Exception xcpt) {
  	  if (tbl!=null) { try { tbl.close(); } catch (Exception ignore) { } }
    	throw new JDOException(xcpt.getMessage(), xcpt);
  	}
    return addr;
  }
  
  public UrlData[] getAddressesLike(String sWorkArea, String sLike, String sSortBy, int iMaxRows, int iSkip) throws JDOException {
  	UrlData[] urls = null;
  	RelationalView con = null;
  	try {
  		con = dts.openRelationalView(udt);
  		AbstractQuery qry = con.newQuery();
  		qry.setResultClass(udt.getClass(), TableDataSource.class);
  	    qry.setFilter("b.gu_workarea=? AND (url_addr LIKE ? OR tx_title LIKE ?)");
  	    qry.setRange(iSkip, iSkip+(iMaxRows>0 ? iMaxRows : Integer.MAX_VALUE));
  	    RecordSet<UrlData> dbs = (RecordSet<UrlData>) qry.execute(sWorkArea, "%"+sLike+"%", "%"+sLike+"%");
  	    int nurls = dbs.size();      
        con.close();
        con = null;
        if (sSortBy!=null) if (sSortBy.length()>0) { if (sSortBy.equalsIgnoreCase("nu_clicks") || sSortBy.equalsIgnoreCase("dt_last_visit")) dbs.sortDesc(sSortBy); else dbs.sort(sSortBy); }
        urls = new UrlData[nurls];
        for (int u=0; u<nurls; u++)
      	  urls[u] = dbs.get(u);      
  	} finally {
  		if (con!=null) { try { con.close(); } catch (Exception ignore) { } }
  	}
  	return urls;
  }

  public UrlData[] getAddresses(String sWorkArea, String sSortBy, int iMaxRows, int iSkip) throws JDOException {
  	UrlData[] urls = null;
  	View con = null;
  	try {
  		con = dts.openView(udt);
  		RecordSet<UrlData> dbs = con.fetch(udt.fetchGroup(), "gu_workarea", sWorkArea, iMaxRows, iSkip);
        int nurls = dbs.size();
        con.close();
        con = null;
        if (sSortBy!=null) if (sSortBy.length()>0) { if (sSortBy.equalsIgnoreCase("nu_clicks") || sSortBy.equalsIgnoreCase("dt_last_visit")) dbs.sortDesc(sSortBy); else dbs.sort(sSortBy); }
        urls = new UrlData[nurls];
      for (int u=0; u<nurls; u++)
      	urls[u] = dbs.get(u);
      
  	} finally {
  		if (con!=null) { try { con.close(); } catch (Exception ignore) { } }
  	}
  	return urls;
  }

  public void delete(String[] aIds) throws JDOException {
	  for (int c=0; c<aIds.length; c++)
		  dts.call("k_sp_del_url", new Param("gu_url",1,aIds[c]));
  }
  
  public void cleanUp() throws JDOException {
	  RelationalTable atoms = null;
	  RelationalTable urls = null;
	  AbstractQuery qry;
	  Predicate prd;
	  String guid;
	  SQLFunctions sqlf = new SQLFunctions(dts.getRdbmsId());
	  try {
		  
		  atoms = dts.openRelationalTable(clt);
		  qry = atoms.newQuery();
		  
		  // DELETE FROM k_job_atoms_clicks WHERE gu_url in (SELECT gu_url FROM k_urls WHERE url_addr LIKE '%&pg_atom=%')
		  qry.setFilter(qry.newPredicate(AND).add("gu_url", IN, udt.getTableName(), "gu_url", qry.newPredicate(AND).add("url_addr", LIKE, "%&pg_atom=%")));
		  atoms.delete(qry);
		  
		  // DELETE FROM k_job_atoms_clicks WHERE gu_url IN (SELECT gu_url FROM k_urls WHERE length(url_addr)<12 OR url_addr IS NULL)
		  qry.setFilter(qry.newPredicate(AND).add("gu_url", IN, udt.getTableName(), "gu_url", qry.newPredicate(OR).add(sqlf.apply(sqlf.LENGTH,"url_addr"), LT, 12).add("url_addr", IS, null)));
		  atoms.delete(qry);

		  urls = dts.openRelationalTable(udt);
		  qry = urls.newQuery();
		  
		  // DELETE FROM k_urls WHERE url_addr like '%&pg_atom=%'
		  qry.setFilter(qry.newPredicate().add("url_addr", LIKE, "%&pg_atom=%"));
		  urls.delete(qry);
		  
		  // DELETE FROM k_urls WHERE length(url_addr)<12 OR url_addr IS NULL
		  qry.setFilter(qry.newPredicate(OR).add(sqlf.apply(sqlf.LENGTH, "url_addr"), LT, 12).add("url_addr", IS, null));
		  urls.delete(qry);
		  
		  // SELECT DISTINCT(url_addr),gu_workarea FROM k_urls
		  RecordSet<UrlData> dist = urls.fetch(new ColumnGroup("DISTINCT(url_addr) AS url_addr","gu_workarea"), "1", 1);
		  
		  for (UrlData urlAddr : dist) {
			  RecordSet<UrlData> url = urls.fetch(new ColumnGroup("gu_url"), 1, 0, new Param("gu_workarea", 1, urlAddr.getWorkarea()), new Param("url_addr", 2, urlAddr.getAddress()));
			  if (url.size()>0)
				  guid = url.get(0).getGuid();
			  else
				  guid = null;
		      
			  qry = atoms.newQuery();
			  prd = qry.newPredicate(OR).add("url_addr", EQ, urlAddr.getAddress());
			  int jSesId = urlAddr.getAddress().indexOf(";jsessionid="); 
		      if (jSesId>0)
		    	  prd.add("url_addr", LIKE, urlAddr.getAddress().substring(0,jSesId)+"%");
			  
		      // UPDATE k_job_atoms_clicks SET gu_url=? WHERE gu_job IN (SELECT gu_job FROM k_jobs WHERE gu_workarea=?) AND gu_url IN (SELECT gu_url FROM k_urls WHERE gu_workarea=? AND (url_addr=? OR url_addr LIKE ?))
			  qry.setFilter(qry.newPredicate(AND)
				.add("gu_job", IN, clt.getTableName(), "gu_job", qry.newPredicate().add("gu_workarea", EQ, urlAddr.getWorkarea()))
				.add("gu_url", IN, udt.getTableName(), "gu_url", qry.newPredicate(AND).add("gu_workarea", EQ, urlAddr.getWorkarea()).add(prd)));
		      atoms.update(new Param[]{new Param("gu_url", 1, guid)}, qry);
		      
		      // DELETE FROM k_urls WHERE gu_workarea=? AND gu_url<>? AND (url_addr=? OR url_addr LIKE ?)
		      qry.setFilter(qry.newPredicate(AND).add("gu_workarea", EQ, urlAddr.getWorkarea()).add("gu_url", NEQ, guid).add(prd));
		      atoms.delete(qry);
		  } // next urlAddr
	  } catch (UnsupportedOperationException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException xcpt) {
		  throw new JDOException(xcpt.getMessage(), xcpt);
	  } finally {
		  if (atoms!=null) atoms.close();
		  if (urls!=null) urls.close();
	  }	  
  }

}