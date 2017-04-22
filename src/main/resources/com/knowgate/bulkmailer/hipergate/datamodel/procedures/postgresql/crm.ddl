CREATE SEQUENCE seq_k_contact_refs INCREMENT 1 START 10000
GO;

CREATE FUNCTION k_sp_del_contact (CHAR) RETURNS INTEGER AS '
DECLARE
  addr RECORD;
  addrs text;
  aCount INTEGER := 0;
  GuWorkArea CHAR(32);
BEGIN
  DELETE FROM k_x_list_members WHERE gu_contact=$1;
  DELETE FROM k_member_address WHERE gu_contact=$1;

  SELECT gu_workarea INTO GuWorkArea FROM k_contacts WHERE gu_contact=$1;

  FOR addr IN SELECT * FROM k_x_contact_addr WHERE gu_contact=$1 LOOP
    aCount := aCount + 1;
    IF 1=aCount THEN
      addrs := quote_literal(addr.gu_address);
    ELSE
      addrs := addrs || chr(44) || quote_literal(addr.gu_address);
    END IF;
  END LOOP;

  DELETE FROM k_x_contact_addr WHERE gu_contact=$1;
  
  DELETE FROM k_contacts WHERE gu_contact=$1;
  RETURN 0;
END;
' LANGUAGE 'plpgsql';
GO;

CREATE FUNCTION k_sp_del_company (CHAR) RETURNS INTEGER AS '
DECLARE
  addr RECORD;
  addrs text;
  aCount INTEGER := 0;
BEGIN

  DELETE FROM k_x_list_members WHERE gu_company=$1;
  DELETE FROM k_member_address WHERE gu_company=$1;

  FOR addr IN SELECT * FROM k_x_company_addr WHERE gu_company=$1 LOOP
    aCount := aCount + 1;
    IF 1=aCount THEN
      addrs := quote_literal(addr.gu_address);
    ELSE
      addrs := addrs || chr(44) || quote_literal(addr.gu_address);
    END IF;
  END LOOP;

  DELETE FROM k_x_company_addr WHERE gu_company=$1;

  IF char_length(addrs)>0 THEN
    EXECUTE ''DELETE FROM '' || quote_ident(''k_addresses'') || '' WHERE gu_address IN ('' || addrs || '')'';
  END IF;

  /* Borrar las referencias de PageSets */
  UPDATE k_pagesets SET gu_company=NULL WHERE gu_company=$1;

  DELETE FROM k_companies WHERE gu_company=$1;
  RETURN 0;
END;
' LANGUAGE 'plpgsql';
GO;

CREATE FUNCTION k_sp_dedup_email_contacts (CHAR) RETURNS INTEGER AS '
DECLARE
  addr RECORD;
  addrs text;
  Dummy INTEGER;
  aCount INTEGER := 0;
  TxPreve VARCHAR(100);
  GuContact CHAR(32);
  Emails VARCHAR[] := ARRAY(SELECT a.tx_email FROM k_member_address a, k_member_address b WHERE a.tx_email=b.tx_email AND a.gu_contact<>b.gu_contact AND a.gu_contact IS NOT NULL and b.gu_contact IS NOT NULL AND NOT EXISTS (SELECT i.gu_bill_addr FROM k_invoices i WHERE i.gu_bill_addr=a.gu_address OR i.gu_ship_addr=b.gu_address) AND a.gu_workarea=b.gu_workarea AND a.gu_workarea=$1 ORDER BY 1);
  NMails INTEGER := array_upper(Emails, 1);
  GuActivity CHAR(32);
  Activs VARCHAR[];
  NActiv INTEGER;
BEGIN
  CREATE TABLE k_discard_contacts (gu_contact CHAR(32));
  CREATE TABLE k_newer_contacts (gu_contact CHAR(32));

  TxPreve := chr(32);
  IF NMails IS NOT NULL THEN
  FOR m IN 1..NMails LOOP

	IF Emails[m]<>TxPreve THEN
      TxPreve:=Emails[m];
	    --
      -- SELECT the oldest contact of the duplicated set INTO GuContact variable
      --
      SELECT gu_contact INTO GuContact FROM k_member_address WHERE gu_contact IS NOT NULL AND tx_email=Emails[m] AND gu_workarea=$1 ORDER BY dt_created LIMIT 1;      
      --
      -- Insert the newer duplicates INTO k_newer_contacts temporary table
	    --
	    INSERT INTO k_newer_contacts (SELECT gu_contact FROM k_member_address WHERE gu_contact IS NOT NULL AND tx_email=Emails[m] AND gu_workarea=$1 AND gu_contact<>GuContact);
	    --
	    -- Insert the duplicates INTO k_discard_contacts temporary table
	    --
      INSERT INTO k_discard_contacts (SELECT gu_contact FROM k_newer_contacts);
      
      -- UPDATE all gu_contact FROM k_newer_contacts SET gu_contact TO GuContact the oldest contact of the set
	    --
    
      UPDATE k_x_list_members SET gu_contact=GuContact WHERE gu_contact IN (SELECT gu_contact FROM k_newer_contacts);
      UPDATE k_inet_addrs SET gu_contact=GuContact WHERE gu_contact IN (SELECT gu_contact FROM k_newer_contacts);
      UPDATE k_job_atoms_archived SET gu_contact=GuContact WHERE gu_contact IN (SELECT gu_contact FROM k_newer_contacts);
      UPDATE k_job_atoms_tracking SET gu_contact=GuContact WHERE gu_contact IN (SELECT gu_contact FROM k_newer_contacts);
      UPDATE k_job_atoms_clicks SET gu_contact=GuContact WHERE gu_contact IN (SELECT gu_contact FROM k_newer_contacts);

      INSERT INTO k_contacts_deduplicated (dt_dedup,gu_dup,gu_contact,gu_workarea,dt_created,bo_restricted,bo_private,nu_notes,nu_attachs,bo_change_pwd,tx_nickname,tx_pwd,tx_challenge,tx_reply,dt_pwd_expires,dt_modified,gu_writer,gu_company,id_batch,id_status,id_ref,id_fare,tx_name,tx_surname,de_title,id_gender,dt_birth,ny_age,id_nationality,sn_passport,tp_passport,sn_drivelic,dt_drivelic,tx_dept,tx_division,gu_geozone,gu_sales_man,tx_comments,id_bpartner,url_linkedin,url_facebook,id_persona) (SELECT current_timestamp AS dt_dedup,GuContact AS gu_dup,gu_contact,gu_workarea,dt_created,bo_restricted,bo_private,nu_notes,nu_attachs,bo_change_pwd,tx_nickname,tx_pwd,tx_challenge,tx_reply,dt_pwd_expires,dt_modified,gu_writer,gu_company,id_batch,id_status,id_ref,id_fare,tx_name,tx_surname,de_title,id_gender,dt_birth,ny_age,id_nationality,sn_passport,tp_passport,sn_drivelic,dt_drivelic,tx_dept,tx_division,gu_geozone,gu_sales_man,tx_comments,id_bpartner,url_linkedin,url_facebook,id_persona FROM k_contacts WHERE gu_contact IN (SELECT gu_contact FROM k_newer_contacts));

      DELETE FROM k_newer_contacts;
    END IF;
  END LOOP;
  END IF;

  DELETE FROM k_discard_contacts d WHERE EXISTS (SELECT gu_contact FROM k_invoices i WHERE i.gu_contact=d.gu_contact);

  SELECT SUM(k_sp_del_contact(gu_contact)) INTO Dummy FROM k_discard_contacts;
  SELECT COUNT(*) INTO aCount FROM k_discard_contacts;
  DELETE FROM k_discard_contacts;

  DROP TABLE k_discard_contacts;
  DROP TABLE k_newer_contacts;

  RETURN aCount;
END;
' LANGUAGE 'plpgsql';
GO;