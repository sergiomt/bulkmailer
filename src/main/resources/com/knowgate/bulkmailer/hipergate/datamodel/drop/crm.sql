DROP TABLE k_member_address;

ALTER TABLE k_job_atoms DROP CONSTRAINT f1_job_atoms;

ALTER TABLE k_companies DROP CONSTRAINT f1_companies;
ALTER TABLE k_companies DROP CONSTRAINT f2_companies;

ALTER TABLE k_x_company_addr DROP CONSTRAINT f1_x_company_addr;
ALTER TABLE k_x_company_addr DROP CONSTRAINT f2_x_company_addr;

ALTER TABLE k_companies_lookup DROP CONSTRAINT f1_companies_lookup;

ALTER TABLE k_contacts DROP CONSTRAINT f1_contacts;
ALTER TABLE k_contacts DROP CONSTRAINT f2_contacts;
ALTER TABLE k_contacts DROP CONSTRAINT f3_contacts;
ALTER TABLE k_contacts DROP CONSTRAINT f4_contacts;

ALTER TABLE k_x_contact_addr DROP CONSTRAINT f1_x_contact_addr;
ALTER TABLE k_x_contact_addr DROP CONSTRAINT f2_x_contact_addr;

DROP TABLE k_contacts_lookup;
DROP TABLE k_x_contact_addr;
DROP TABLE k_contacts;
DROP TABLE k_companies_lookup;
DROP TABLE k_x_company_addr;
DROP TABLE k_companies;
