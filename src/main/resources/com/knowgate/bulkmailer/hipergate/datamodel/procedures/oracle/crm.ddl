CREATE SEQUENCE seq_k_contact_refs INCREMENT BY 1 START WITH 10000
GO;

CREATE OR REPLACE PROCEDURE k_sp_del_contact (ContactId CHAR) IS
  TYPE GUIDS IS TABLE OF k_addresses.gu_address%TYPE;

  k_tmp_del_addr GUIDS := GUIDS();

  GuWorkArea CHAR(32);

BEGIN
  DELETE k_x_list_members WHERE gu_contact=ContactId;
  DELETE k_member_address WHERE gu_contact=ContactId;

  SELECT gu_workarea INTO GuWorkArea FROM k_contacts WHERE gu_contact=ContactId;

  FOR addr IN ( SELECT gu_address FROM k_x_contact_addr WHERE gu_contact=ContactId) LOOP
    k_tmp_del_addr.extend;
    k_tmp_del_addr(k_tmp_del_addr.count) := addr.gu_address;
  END LOOP;

  DELETE k_x_contact_addr WHERE gu_contact=ContactId;

  DELETE k_contacts WHERE gu_contact=ContactId;
END k_sp_del_contact;
GO;

CREATE OR REPLACE PROCEDURE k_sp_del_company (CompanyId CHAR) IS
  TYPE GUIDS IS TABLE OF k_addresses.gu_address%TYPE;

  k_tmp_del_addr GUIDS := GUIDS();

  GuWorkArea CHAR(32);

BEGIN
  DELETE k_x_list_members WHERE gu_company=CompanyId;
  DELETE k_member_address WHERE gu_company=CompanyId;
  
  SELECT gu_workarea INTO GuWorkArea FROM k_companies WHERE gu_company=CompanyId;

  /* Borrar las direcciones de la compa�ia */
  FOR addr IN ( SELECT gu_address FROM k_x_company_addr WHERE gu_company=CompanyId) LOOP
    k_tmp_del_addr.extend;
    k_tmp_del_addr(k_tmp_del_addr.count) := addr.gu_address;
  END LOOP;

  DELETE k_x_company_addr WHERE gu_company=CompanyId;

  FOR a IN 1..k_tmp_del_addr.COUNT LOOP
    DELETE k_addresses WHERE gu_address=k_tmp_del_addr(a);
  END LOOP;

  /* Borrar las referencias de PageSets */
  
  UPDATE k_pagesets SET gu_company=NULL WHERE gu_company=CompanyId;
  /* Borrar el enlace con categor�as */
  
  DELETE k_companies WHERE gu_company=CompanyId;
END k_sp_del_company;
GO;
