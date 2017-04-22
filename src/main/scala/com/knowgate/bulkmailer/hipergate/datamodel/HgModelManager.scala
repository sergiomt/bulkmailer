package com.knowgate.bulkmailer.hipergate.datamodel

import java.io.IOException
import java.io.FileNotFoundException

import java.util.LinkedList
import java.util.ListIterator
import java.util.Properties

import java.sql.Statement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

import bsh.Interpreter
import bsh.EvalError

import com.knowgate.stringutils.Uid

import org.judal.jdbc.RDBMS
import org.judal.jdbc.JDBCModelManager
import org.judal.jdbc.jdc.JDCConnection
import org.judal.datacopy.DataStruct

class HgModelManager extends JDBCModelManager {
    // ---------------------------------------------------------------------------

  /**
   * <p>Create a functional module</p>
   * @param sModuleName Name of module to create { kernel | lookups | security |
   * jobs | thesauri | webbuilder | crm | lists | hipermail }
   * @return <b>true</b> if module was successfully created, <b>false</b> if errors
   * occured during module creation. Even if error occur module may still be partially
   * created at database after calling create()
   * @throws IllegalStateException If not connected to database
   * @throws FileNotFoundException If any of the internal files for module is not found
   * @throws SQLException
   * @throws IOException
   */
  
  @throws(classOf[IllegalStateException])
  @throws(classOf[SQLException])
  @throws(classOf[FileNotFoundException])
  @throws(classOf[IOException])
  def create(sModuleName: String) : Boolean = {

    var bRetVal = true

    if (null==getConnection())
      throw new IllegalStateException("Not connected to database");

    try {
      if (sModuleName.equals("kernel")) {

      executeBulk("tables/kernel.ddl", JDBCModelManager.BULK_STATEMENTS)
      executeBulk("data/k_sequences.sql", JDBCModelManager.BULK_STATEMENTS)
      executeBulk("data/k_version.sql", JDBCModelManager.BULK_STATEMENTS)

      if (iDbms==RDBMS.MSSQL.intValue)
          executeBulk("procedures/mssql/kernel.ddl", JDBCModelManager.BULK_PLSQL);
      else if (iDbms==RDBMS.MYSQL.intValue)
          executeBulk("procedures/mysql/kernel.ddl", JDBCModelManager.BULK_PLSQL)
      else if (iDbms==RDBMS.POSTGRESQL.intValue)
          executeBulk("procedures/postgresql/kernel.sql", JDBCModelManager.BULK_STATEMENTS)

    } else if (sModuleName.equals("lookups")) {

      executeBulk("tables/lookups.ddl", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("data/k_lu_languages.sql"  , JDBCModelManager.BULK_STATEMENTS)
      executeBulk("data/k_lu_countries.sql"  , JDBCModelManager.BULK_STATEMENTS)
      executeBulk("data/k_lu_status.sql"     , JDBCModelManager.BULK_BATCH)
      executeBulk("data/k_lu_cont_types.sql" , JDBCModelManager.BULK_BATCH)

    } else if (sModuleName.equals("security")) {

      executeBulk("tables/security.ddl", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("data/k_lu_permissions.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("data/k_apps.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("data/k_domains.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("data/k_acl_groups.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("data/k_users.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("data/k_x_group_user.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("data/k_workareas.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("data/k_x_app_workarea.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("constraints/security.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("views/security.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("procedures/" + sDbms + "/security.ddl", JDBCModelManager.BULK_PLSQL)

      if (iDbms==RDBMS.MSSQL.intValue)
        executeBulk("triggers/mssql/security.ddl", JDBCModelManager.BULK_PLSQL)

    } else if (sModuleName.equals("jobs")) {

      executeBulk("tables/jobs.ddl", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("views/jobs.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("procedures/" + sDbms + "/jobs.ddl", JDBCModelManager.BULK_PLSQL)

      executeBulk("data/k_lu_job_status.sql" , JDBCModelManager.BULK_BATCH)

      executeBulk("data/k_lu_job_commands.sql" , JDBCModelManager.BULK_BATCH)

      executeBulk("constraints/jobs.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("indexes/jobs.sql", JDBCModelManager.BULK_STATEMENTS)

    } else if (sModuleName.equals("thesauri")) {

      executeBulk("tables/thesauri.ddl", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("indexes/thesauri.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("constraints/thesauri.sql", JDBCModelManager.BULK_STATEMENTS)

    } else if (sModuleName.equals("crm")) {

      executeBulk("tables/crm.ddl", JDBCModelManager.BULK_STATEMENTS)

	  // Do not change order between constraints and procedures.
	  // For Micorosft SQL Server, constraints/crm.sql must be
	  // executed before procedures/mssql/crm.ddl
	  
      executeBulk("constraints/crm.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("procedures/" + sDbms + "/crm.ddl", JDBCModelManager.BULK_PLSQL)

      executeBulk("data/k_companies_lookup.sql" , JDBCModelManager.BULK_STATEMENTS)

      executeBulk("data/k_contacts_lookup.sql" , JDBCModelManager.BULK_STATEMENTS)

      executeBulk("indexes/crm.sql", JDBCModelManager.BULK_STATEMENTS)

    } else if (sModuleName.equals("lists")) {

      executeBulk("tables/lists.ddl", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("procedures/" + sDbms + "/lists.ddl", JDBCModelManager.BULK_PLSQL)

      executeBulk("indexes/lists.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("constraints/lists.sql", JDBCModelManager.BULK_STATEMENTS)

    } else if (sModuleName.equals("webbuilder")) {

      executeBulk("tables/webbuilder.ddl", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("constraints/webbuilder.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("data/k_microsites.sql" , JDBCModelManager.BULK_STATEMENTS)

      executeBulk("indexes/webbuilder.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("procedures/" + sDbms + "/webbuilder.ddl", JDBCModelManager.BULK_PLSQL)

    } else if (sModuleName.equals("hipermail")) {

      executeBulk("tables/hipermail.ddl", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("constraints/hipermail.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("indexes/hipermail.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("procedures/" + sDbms + "/hipermail.ddl", JDBCModelManager.BULK_PLSQL)

      executeBulk("views/hipermail.sql", JDBCModelManager.BULK_STATEMENTS)

    }
  } catch {
    case ie: InterruptedException => {
      if (null!=oStrLog) oStrLog.append("STOP ON ERROR SET TO ON: SCRIPT INTERRUPTED\n")
      bRetVal = false }
  }

    bRetVal
  } // create

  // ---------------------------------------------------------------------------

  /**
   * <p>Drop a functional module</p>
   * @param sModuleName Name of module to drop { kernel | lookups | security |
   * jobs | thesauri | webbuilder | crm | lists | hipermail }
   * @return <b>true</b> if module was successfully droped, <b>false</b> if errors
   * occured during droping module.
   * Even if error occur module may still be partially droped at database after calling drop()
   * @throws IllegalStateException
   * @throws SQLException
   * @throws FileNotFoundException
   * @throws IOException
   */
  @throws(classOf[IllegalStateException])
  @throws(classOf[SQLException])
  @throws(classOf[FileNotFoundException])
  @throws(classOf[IOException])
  def drop(sModuleName: String) : Boolean = {

    var bRetVal = true;

    if (null==getConnection())
      throw new IllegalStateException("Not connected to database");

    try {
    if (sModuleName.equals("kernel")) {

      executeBulk("drop/" + sDbms + "/kernel.sql", JDBCModelManager.BULK_STATEMENTS)
      executeBulk("drop/kernel.sql", JDBCModelManager.BULK_STATEMENTS)

    } else if (sModuleName.equals("lookups")) {

      executeBulk("drop/lookups.sql", JDBCModelManager.BULK_STATEMENTS)

    } else if (sModuleName.equals("security")) {

      executeBulk("drop/" + sDbms + "/security.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("drop/security.sql", JDBCModelManager.BULK_STATEMENTS)

    } else if (sModuleName.equals("jobs")) {

      executeBulk("drop/" + sDbms + "/jobs.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("drop/jobs.sql", JDBCModelManager.BULK_STATEMENTS)

    } else if (sModuleName.equals("thesauri")) {

      executeBulk("drop/thesauri.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("drop/" + sDbms + "/thesauri.sql", JDBCModelManager.BULK_STATEMENTS)

    } else if (sModuleName.equals("crm")) {

      executeBulk("drop/" + sDbms + "/crm.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("drop/crm.sql", JDBCModelManager.BULK_STATEMENTS)

    } else if (sModuleName.equals("lists")) {

      executeBulk("drop/" + sDbms + "/lists.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("drop/lists.sql", JDBCModelManager.BULK_STATEMENTS)

    } else if (sModuleName.equals("webbuilder")) {

      executeBulk("drop/" + sDbms + "/webbuilder.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("drop/webbuilder.sql", JDBCModelManager.BULK_STATEMENTS)

    } else if (sModuleName.equals("hipermail")) {

      executeBulk("drop/" + sDbms + "/hipermail.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("drop/hipermail.sql", JDBCModelManager.BULK_STATEMENTS)

    } else if (sModuleName.equals("training")) {

      executeBulk("drop/training.sql", JDBCModelManager.BULK_STATEMENTS)

      executeBulk("drop/" + sDbms + "/training.sql", JDBCModelManager.BULK_STATEMENTS)

    }
    } catch {
      case ie: InterruptedException => {
        if (null!=oStrLog) oStrLog.append("STOP ON ERROR SET TO ON: SCRIPT INTERRUPTED\n")
        bRetVal = false }
    }

    bRetVal
  } // drop
  
  // ---------------------------------------------------------------------------

  /**
   * <p>Create all modules</p>
   * The created modules will be (in order):
   * kernel, lookups, security, jobs, categories, thesauri, products, addrbook,
   * forums, crm, projtrack, lists, webbuilder, shops, billing, hipermail, training
   * @throws FileNotFoundException If any of the internal files for modules are not found
   * @throws IllegalStateException
   * @throws SQLException
   * @throws IOException
   */
  @throws(classOf[IllegalStateException])
  @throws(classOf[SQLException])
  @throws(classOf[FileNotFoundException])
  @throws(classOf[IOException])
  def createAll() : Boolean = {

    if (!create ("kernel") && bStopOnError) return false
    if (!create ("lookups") && bStopOnError) return false
    if (!create ("security") && bStopOnError) return false
    if (!create ("jobs") && bStopOnError) return false
    if (!create ("thesauri") && bStopOnError) return false
    if (!create ("crm") && bStopOnError) return false
    if (!create ("lists") && bStopOnError) return false
    if (!create ("webbuilder") && bStopOnError) return false
    if (!create ("hipermail") && bStopOnError) return false

    if (RDBMS.ORACLE.intValue==iDbms) {
      try {
        recompileOrcl();
      } catch {
        case sqle: SQLException =>
          if (bStopOnError) throw new SQLException ("SQLException: " + sqle.getMessage(), sqle.getSQLState(), sqle.getErrorCode())
      }

      var oStmt : Statement = null
      var oRSet : ResultSet  = null

      try {

        oStmt = getConnection().createStatement();
        oRSet = oStmt.executeQuery("SELECT OBJECT_TYPE,OBJECT_NAME FROM USER_OBJECTS WHERE STATUS='INVALID' AND OBJECT_TYPE IN ('PROCEDURE','VIEW','TRIGGER')")

        while (oRSet.next()) {
          iErrors += 1
          if (null!=oStrLog) oStrLog.append(oRSet.getString(1) + " " + oRSet.getString(2) + " is invalid after recompile\n")
        } // wend

      } catch {
        case sqle: SQLException => {
          iErrors += 1;
          if (null!=oStrLog) oStrLog.append(sqle + "\n");
          if (bStopOnError)
            throw new SQLException("SQLException: " + sqle.getMessage(), sqle.getSQLState(), sqle.getErrorCode())
        }
      }
      finally {
        if (null!=oRSet) oRSet.close();
        oRSet = null;
        if (null!=oStmt) oStmt.close();
        oStmt = null;
      }
    }
    true
  } // createAll

  // ---------------------------------------------------------------------------

  /**
   * <p>Create a default database ready for use</p>
   * Modules for the bulkmailer will be created at the new database.<br>
   * The new database will contain 4 domains and 4 workareas:<br>
   * SYSTEM, MODEL, TEST, REAL<br>
   * SYSTEM and MODEL domains are for administrative purposed only and should
   * not be used by programmers.<br>
   * Domains TEST and REAL are intended for development/testing,
   * aceptance/demostration and real/production usage.<br>
   * Error messages are written to internal ModelManager log and can be inspected by
   * calling report() method after createDefaultDatabase()
   * @throws FileNotFoundException If any of the internal files for modules are not found
   * @throws EvalError Java BeanShell script domain_create.js as a syntax error
   * @throws org.xml.sax.SAXException Parsing error at file workarea_clon.xml
   * @throws InstantiationException SAX parser is not properly installed
   * @throws IllegalAccessException SAX parser is not properly installed
   * @throws ClassNotFoundException SAX parser is not properly installed
   * @throws IOException
   * @throws SQLException
   */  
  def createDefaultDatabase() : Boolean = {

    var oStmt : Statement = null
    
    val SAXParserClass = Class.forName("org.apache.xerces.parsers.SAXParser")

    if (RDBMS.MSSQL.intValue==iDbms) {
      try {
        oStmt = getConnection().createStatement()

        oStmt.execute("ALTER DATABASE " + getConnection().getCatalog() + " SET ARITHABORT ON")
        oStmt.close();

        if (null!=oStrLog) oStrLog.append("ALTER DATABASE " + getConnection().getCatalog() + " SET ARITHABORT ON\n")
      } catch {
        case sqle: SQLException => {
         iErrors += 1
         if (null!=oStrLog) oStrLog.append("SQLException: " + sqle.getMessage() + "\n") }
      }
    }

    var bRetVal = createAll()

    createDomain("TEST")
    createDomain("REAL")

    cloneWorkArea("MODEL.model_default", "TEST.test_default")
    cloneWorkArea("MODEL.model_default", "REAL.real_default")

    if (System.getProperty("os.name").startsWith("Windows")) {
      oStmt = getConnection().createStatement()
      try {
        if (iDbms==RDBMS.POSTGRESQL.intValue) {
            oStmt.executeUpdate(
                "UPDATE k_microsites SET path_metadata=translate(path_metadata,'/','\\\\')")
            oStmt.executeUpdate(
                "UPDATE k_pagesets SET path_data=translate(path_data,'/','\\\\')")          
        } else {
            oStmt.executeUpdate(
                "UPDATE k_microsites SET path_metadata=REPLACE(path_metadata,'/','\\\\')")
            oStmt.executeUpdate(
                "UPDATE k_pagesets SET path_data=REPLACE(path_data,'/','\\\\')")          
        }        
        oStmt.close()
      } catch {
        case sqle: SQLException => {}
          iErrors +=1
          if (null!=oStrLog) oStrLog.append("SQLException: " + sqle.getMessage() + "\n")
          oStmt.close()
      }
    }
    bRetVal
  }
  
  // ---------------------------------------------------------------------------

  /**
   * <p>Drop all modules</p>
   * The created modules will be (in order):
   * hipermail, webbuilder, lists, crm, thesauri, jobs, security, lookups, kernel
   * @throws IllegalStateException
   * @throws SQLException
   * @throws FileNotFoundException
   * @throws IOException
   */
  @throws(classOf[IllegalStateException])
  @throws(classOf[SQLException])
  @throws(classOf[FileNotFoundException])
  @throws(classOf[IOException])
  def dropAll() : Boolean = {

    if (!drop ("hipermail") && bStopOnError) return false
    if (!drop ("webbuilder") && bStopOnError) return false
    if (!drop ("lists") && bStopOnError) return false
    if (!drop ("crm") && bStopOnError) return false
    if (!drop ("addrbook") && bStopOnError) return false
    if (!drop ("thesauri") && bStopOnError) return false
    if (!drop ("jobs") && bStopOnError) return false
    if (!drop ("security") && bStopOnError) return false
    if (!drop ("lookups") && bStopOnError) return false
    if (!drop ("kernel") && bStopOnError) return false

    true
  } // dropAll

  // ----------------------------------------------------------

  /**
   * <p>Create New Domain</p>
   * Internally executes scripts/domain_create.js Java BeanShell Script contained
   * inside JAR file under com/knowgate/hipergate/datamodel.<br>
   * @param sDomainNm New Domain Name
   * @return Autogenerated unique numeric identifier for new domain
   * @throws EvalError Java BeanShell script domain_create.js as a syntax error
   * @throws IOException
   * @throws FileNotFoundException
   * @throws SQLException
   */
  
  @throws(classOf[EvalError])
  @throws(classOf[SQLException])
  @throws(classOf[FileNotFoundException])
  @throws(classOf[IOException])
  def createDomain(sDomainNm: String) : Int = {
    var sErrMsg : String = ""

    var iDominId = 0
    var iRetVal : Int = 0

    val oInterpreter = new Interpreter()

    oInterpreter.set ("DomainNm", sDomainNm)
    oInterpreter.set ("DefaultConnection", getConnection())
    oInterpreter.set ("AlternativeConnection", getConnection())

    oInterpreter.eval(getResourceAsString("scripts/domain_create.js", sEncoding))

    var obj = oInterpreter.get("ErrorCode")

    val oCodError = oInterpreter.get("ErrorCode").asInstanceOf[Integer]

    if (oCodError.intValue()!=0) {
      sErrMsg = oInterpreter.get("ErrorMessage").asInstanceOf[String]
      iErrors += 1
      if (null!=oStrLog) oStrLog.append("EvalError: " + sErrMsg + "\n")
      throw new SQLException(sErrMsg)
    } // fi ()

    obj = oInterpreter.get("ReturnValue")

    if ( null != obj ) {
      iDominId = obj.asInstanceOf[Integer].intValue()

      val oStmt = getConnection().createStatement()
      oStmt.executeUpdate("UPDATE k_workareas SET nm_workarea='" + sDomainNm.toLowerCase() + "_default' WHERE id_domain=" + String.valueOf(iDominId) + " AND nm_workarea='model_default'")
      oStmt.close()

      if (null!=oStrLog) oStrLog.append("New Domain " + oInterpreter.get("ReturnValue") + " created successfully\n")
      iRetVal = iDominId
    }
    else {
      if (null!=oStrLog) oStrLog.append( oInterpreter.get("ErrorMessage") + ": Domain not created.")
      iRetVal = 0
    }

    iRetVal
  } // createDomain

  // ----------------------------------------------------------

  /**
   * <p>Create a clone of a WorkArea</p>
   * WorkAreas are cloned by following instructions contained in
   * com/knowgate/hipergate/datamodel/scripts/<i>dbms</i>/workarea_clon.xml file.
   * @param sOriginWorkArea String of the form domain_name.workarea_name,
   * for example "MODEL.default_workarea"
   * @param sTargetWorkArea String of the form domain_name.workarea_name,
   * for example "TEST1.devel_workarea"
   * @return GUID of new WorkArea or <b>null</b> if clone could not be created.<br>
   * Error messages are written to internal ModelManager log and can be inspected by
   * calling report() method after cloneWorkArea()
   * @throws SQLException Most probably raised because data at model_default workarea is corrupted
   * @throws InstantiationException SAX parser is not properly installed
   * @throws IllegalAccessException SAX parser is not properly installed
   * @throws ClassNotFoundException SAX parser is not properly installed
   * @throws org.xml.sax.SAXException Parsing error at file workarea_clon.xml
   * @throws IOException
   * @see com.knowgate.workareas.WorkArea#delete(JDCConnection,String)
   */
  
  @throws(classOf[SQLException])
  @throws(classOf[IOException])  
  @throws(classOf[InstantiationException])
  @throws(classOf[IllegalAccessException])
  @throws(classOf[IllegalStateException])
  @throws(classOf[ClassNotFoundException])
  def cloneWorkArea(sOriginWorkArea: String, sTargetWorkArea: String) : String = {

    var oRSet : ResultSet = null
    var oStmt : Statement = null

    if (null==getConnection())
      throw new IllegalStateException("Not connected to database")

    // Split Domain and WorkArea names
    val aOriginWrkA = sOriginWorkArea.split(".")
    val aTargetWrkA = sTargetWorkArea.split(".")

    var iSourceDomainId :  Int = 0
    oStmt = getConnection().createStatement()
    oRSet = oStmt.executeQuery("SELECT id_domain FROM k_domains WHERE nm_domain='"+aOriginWrkA(0)+"'")
    if (oRSet.next())
      iSourceDomainId = oRSet.getInt(1)
    oRSet.close()
    oStmt.close()

    if (0==iSourceDomainId) {
      iErrors += 1;
      if (null!=oStrLog) oStrLog.append("Domain " + aOriginWrkA(0) + " not found\n")
      return null
    }

    var iTargetDomainId : Int = 0
    oStmt = getConnection().createStatement()
    oRSet = oStmt.executeQuery("SELECT id_domain FROM k_domains WHERE nm_domain='"+aTargetWrkA(0)+"'")
    if (oRSet.next())
      iTargetDomainId = oRSet.getInt(1)
    oRSet.close()
    oStmt.close()

    if (0==iTargetDomainId) {
      iErrors += 1
      if (null!=oStrLog) oStrLog.append("Domain " + aTargetWrkA(0) + " not found\n")
      return null
    }

    var sSourceWorkAreaId : String = null
    oStmt = getConnection().createStatement()
    oRSet = oStmt.executeQuery("SELECT gu_workarea FROM k_workareas WHERE id_domain="+String.valueOf(iSourceDomainId)+" AND gu_workarea='"+aOriginWrkA(1)+"'")
    if (oRSet.next())
      sSourceWorkAreaId = oRSet.getString(1)
    oRSet.close()
    oStmt.close()

    if (null==sSourceWorkAreaId) {
      iErrors += 1
      if (null!=oStrLog) oStrLog.append("WorkArea " + aOriginWrkA(1) + " not found at Domain " + aOriginWrkA(0) + "\n")
      return null
    }

    var sTargetWorkAreaId : String = null
    oStmt = getConnection().createStatement()
    oRSet = oStmt.executeQuery("SELECT gu_workarea FROM k_workareas WHERE id_domain="+String.valueOf(iTargetDomainId)+" AND gu_workarea='"+aTargetWrkA(1)+"'")
    if (oRSet.next())
      sTargetWorkAreaId = oRSet.getString(1)
    oRSet.close()
    oStmt.close()

    if (null==sTargetWorkAreaId)
      sTargetWorkAreaId = Uid.createUniqueKey()

    if (null!=oStrLog) oStrLog.append("SELECT gu_owner,gu_admins FROM k_domains WHERE id_domain=" + String.valueOf(iTargetDomainId))

    oStmt = getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)

    oRSet = oStmt.executeQuery("SELECT gu_owner,gu_admins FROM k_domains WHERE id_domain=" + String.valueOf(iTargetDomainId))
    oRSet.next()
    val sOwnerId = oRSet.getString(1)
    val sAdminId = oRSet.getString(2)
    oRSet.close()
    oStmt.close()

    val oParams = new Properties()
    oParams.put("SourceWorkAreaId", sSourceWorkAreaId)
    oParams.put("TargetDomainId", String.valueOf(iTargetDomainId))
    oParams.put("TargetWorkAreaId", sTargetWorkAreaId)
    oParams.put("TargetWorkAreaNm", String.valueOf(aTargetWrkA(1)))
    oParams.put("OwnerId", sOwnerId)

    val oDS = new DataStruct()

    oDS.setOriginConnection(getConnection())
    oDS.setTargetConnection(getConnection())

    if (iDbms==RDBMS.MSSQL.intValue)
        oDS.parse (getResourceAsString("scripts/mssql/workarea_clon.xml", sEncoding), oParams)

    if (iDbms==RDBMS.MYSQL.intValue)
        oDS.parse (getResourceAsString("scripts/mysql/workarea_clon.xml", sEncoding), oParams)

    if (iDbms==RDBMS.ORACLE.intValue)
        oDS.parse (getResourceAsString("scripts/oracle/workarea_clon.xml", sEncoding), oParams)

    if (iDbms==RDBMS.POSTGRESQL.intValue)
        oDS.parse (getResourceAsString("scripts/postgresql/workarea_clon.xml", sEncoding), oParams)

    val oPKOr = new Array[Object](0)
    val oPKTr = new Array[Object](0)

    oDS.update(oPKOr, oPKTr, 0)
    oDS.clear()

    if (null!=oStrLog) oStrLog.append("New WorkArea " + sTargetWorkAreaId + " created successfully\n")

    // ***********************************************************
    // Give permissions to domain administrators over applications

    oStmt = getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
    var oApps = new LinkedList[Object]()
    var sSQL = "SELECT id_app FROM k_x_app_workarea WHERE gu_workarea='" + sSourceWorkAreaId + "'"
    oRSet = oStmt.executeQuery(sSQL)
    while (oRSet.next()) oApps.add(oRSet.getObject(1))
    oRSet.close()
    oStmt.close()

    val oIter = oApps.listIterator()
    val oPrep = getConnection().prepareStatement("DELETE FROM k_x_app_workarea WHERE gu_workarea='" + sTargetWorkAreaId + "' AND id_app=?")
    while (oIter.hasNext()) {
      oPrep.setObject(1, oIter.next())
      oPrep.executeUpdate()
    }
    oPrep.close()

    oStmt = getConnection().createStatement()
    sSQL = "INSERT INTO k_x_app_workarea (id_app,gu_workarea,gu_admins,path_files) SELECT id_app,'" + sTargetWorkAreaId + "','" + sAdminId + "','" + aTargetWrkA(1).toLowerCase() + "' FROM k_x_app_workarea WHERE gu_workarea='" + sSourceWorkAreaId + "'"

    if (null!=oStrLog) oStrLog.append("Statement.executeUpdate(" + sSQL + ")\n")

    oStmt.executeUpdate(sSQL)

    oStmt.close()

    sTargetWorkAreaId
  } // cloneWorkArea()

  // ----------------------------------------------------------
  
}