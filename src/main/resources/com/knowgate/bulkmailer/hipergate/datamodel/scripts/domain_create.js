/**
  * Create a Domain Cloned from MODEL (1025) Domain
  * Parameters:
  @param DomainNm Name of Domain to be dropped

  * Return Values:

  * ErrorCode    -> Integer, Native Error Code (0==No Error)
  * ErrorMessage -> String , Error Message
  * ReturnValue  -> Integer, New Domain Id. (null==Domain not created)

**/

  import java.sql.SQLException;
  import java.sql.Connection;
  import java.sql.Statement;
  import java.sql.PreparedStatement;
  import java.sql.ResultSet;

  import java.math.BigDecimal;

  import java.util.Properties;
  import java.util.Vector;

  import com.knowgate.dataobjs.*;
  import com.knowgate.datacopy.DataStruct;
  import com.knowgate.bulkmailer.hipergate.datamodel.HgModelManager;

  // MODEL Domain Id.
  final int MODEL = 1025;

  Integer ReturnValue;
  Integer ErrorCode;
  String  ErrorMessage;
  String  ClonXML;

  String DomainId = String.valueOf(DBBind.nextVal(AlternativeConnection, "seq_k_domains"));

  Object[] oPKOr = { null };
  Object[] oPKTr = { null };

  Properties oParams = new Properties();
  oParams.put("DomainId", DomainId);
  oParams.put("DomainNm", DomainNm);

  com.knowgate.datacopy.DataStruct oDS = new com.knowgate.datacopy.DataStruct();

  String sDBMS = DefaultConnection.getMetaData().getDatabaseProductName();

  HgModelManager oModMan = new HgModelManager();
  
  try {

    if (sDBMS.equals("Microsoft SQL Server")) {
      oPKOr[0] = new Integer(MODEL);
      oPKTr[0] = new Integer(DomainId);

      ClonXML = oModMan.getResourceAsString("scripts/mssql/domain_clon.xml", "ISO8859_1");

      oDS.parse(ClonXML, oParams);
    }
    else if (sDBMS.equals("PostgreSQL")) {
      oPKOr[0] = new Integer(MODEL);
      oPKTr[0] = new Integer(DomainId);

      ClonXML = oModMan.getResourceAsString("scripts/postgresql/domain_clon.xml", "ISO8859_1");

      oDS.parse(ClonXML, oParams);
    }
    else if (sDBMS.equals("MySQL")) {
      oPKOr[0] = new Integer(MODEL);
      oPKTr[0] = new Integer(DomainId);

      ClonXML = oModMan.getResourceAsString("scripts/mysql/domain_clon.xml", "ISO8859_1");

      oDS.parse(ClonXML, oParams);
    }
    else if (sDBMS.equals("Oracle")) {
      oPKOr[0] = new BigDecimal((double) MODEL);
      oPKTr[0] = new BigDecimal(DomainId);

      ClonXML = oModMan.getResourceAsString("scripts/oracle/domain_clon.xml", "ISO8859_1");

      oDS.parse(ClonXML, oParams);
    }

    // ******************
    // DataStruct Cloning

    oDS.setOriginConnection(DefaultConnection);
    oDS.setTargetConnection(AlternativeConnection);

    oDS.insert(oPKOr, oPKTr, 1);

    oDS.clear();

    if (null!=oPKTr[0])
      ReturnValue = new Integer(oPKTr[0].toString());
    else
      ReturnValue = null;

    ErrorCode = new Integer(0);
    ErrorMessage = "Domain (" + DomainId + "," + DomainNm + ") successfully created.";
  }
  catch (java.lang.NullPointerException n) {
    ReturnValue = null;
    ErrorCode = new Integer(-1);
    ErrorMessage = "NullPointerException: " + n.getMessage();
  }
  catch (java.lang.ArrayIndexOutOfBoundsException a) {
    ReturnValue = null;
    ErrorCode = new Integer(-1);
    ErrorMessage = "ArrayIndexOutOfBoundsException: " + a.getMessage();
  }
  catch (java.sql.SQLException e) {
    ReturnValue = null;
    ErrorCode = new Integer(e.getErrorCode());
    ErrorMessage = "SQLException: " + e.getMessage();
  }
  catch (Exception x) {
    ReturnValue = null;
    ErrorCode = new Integer(-1);
    ErrorMessage = x.getClass().getName() + ": " + x.getMessage();
  }
