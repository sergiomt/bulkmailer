INSERT INTO k_sequences (nm_table,nu_initial,nu_maxval,nu_increase,nu_current) VALUES ('seq_k_contact_refs', 10000, 2147483647, 1, 10000)
GO;

CREATE PROCEDURE k_sp_del_contact @ContactId CHAR(32) AS

  DECLARE @GuWorkArea CHAR(32)

  DELETE k_x_list_members WHERE gu_contact=@ContactId
  
  DELETE k_member_address WHERE gu_contact=@ContactId
  
  SELECT @GuWorkArea=gu_workarea FROM k_contacts WHERE gu_contact=@ContactId


  /* Borrar primero las direcciones asociadas al contacto */
  SELECT gu_address INTO #k_tmp_del_addr FROM k_x_contact_addr WHERE gu_contact=@ContactId
  DELETE k_x_contact_addr WHERE gu_contact=@ContactId
  UPDATE k_x_activity_audience SET gu_address=NULL WHERE gu_address IN (SELECT gu_address FROM #k_tmp_del_addr)
  DELETE k_addresses WHERE gu_address IN (SELECT gu_address FROM #k_tmp_del_addr)
  DROP TABLE #k_tmp_del_addr

  DELETE k_contacts WHERE gu_contact=@ContactId
GO;

CREATE PROCEDURE k_sp_del_company @CompanyId CHAR(32) AS

  DECLARE @GuWorkArea CHAR(32)

  DELETE k_x_list_members WHERE gu_company=@CompanyId

  DELETE k_member_address WHERE gu_company=@CompanyId
  
  SELECT @GuWorkArea=gu_workarea FROM k_companies WHERE gu_company=@CompanyId


  /* Borrar las direcciones de la compa�ia */
  SELECT gu_address INTO #k_tmp_del_addr FROM k_x_company_addr WHERE gu_company=@CompanyId
  DELETE k_x_company_addr WHERE gu_company=@CompanyId
  DELETE k_addresses WHERE gu_address IN (SELECT gu_address FROM #k_tmp_del_addr)
  DROP TABLE #k_tmp_del_addr

  /* Borrar las referencias de PageSets */
  UPDATE k_pagesets SET gu_company=NULL WHERE gu_company=@CompanyId

  DELETE k_companies WHERE gu_company=@CompanyId
GO;
