ALTER TABLE k_job_atoms ADD CONSTRAINT f1_job_atoms FOREIGN KEY(gu_company) REFERENCES k_companies(gu_company);

ALTER TABLE k_companies ADD CONSTRAINT f1_companies FOREIGN KEY(gu_workarea) REFERENCES k_workareas(gu_workarea);

ALTER TABLE k_x_company_addr ADD CONSTRAINT f1_x_company_addr FOREIGN KEY(gu_company) REFERENCES k_companies(gu_company);
ALTER TABLE k_x_company_addr ADD CONSTRAINT f2_x_company_addr FOREIGN KEY(gu_address) REFERENCES k_addresses(gu_address);

ALTER TABLE k_companies_lookup ADD CONSTRAINT f1_companies_lookup  FOREIGN KEY(gu_owner) REFERENCES k_workareas(gu_workarea);

ALTER TABLE k_contacts ADD CONSTRAINT f1_contacts FOREIGN KEY(gu_workarea) REFERENCES k_workareas(gu_workarea);
ALTER TABLE k_contacts ADD CONSTRAINT f2_contacts FOREIGN KEY(gu_company) REFERENCES k_companies(gu_company);
ALTER TABLE k_contacts ADD CONSTRAINT f3_contacts FOREIGN KEY(gu_writer) REFERENCES k_users(gu_user);

ALTER TABLE k_x_contact_addr ADD CONSTRAINT f1_x_contact_addr FOREIGN KEY(gu_contact) REFERENCES k_contacts(gu_contact);
ALTER TABLE k_x_contact_addr ADD CONSTRAINT f2_x_contact_addr FOREIGN KEY(gu_address) REFERENCES k_addresses(gu_address);

ALTER TABLE k_member_address ADD CONSTRAINT f1_member_address FOREIGN KEY(gu_address) REFERENCES k_addresses(gu_address);
ALTER TABLE k_member_address ADD CONSTRAINT f2_member_address FOREIGN KEY(gu_workarea) REFERENCES k_workareas(gu_workarea);
ALTER TABLE k_member_address ADD CONSTRAINT f3_member_address FOREIGN KEY(gu_company) REFERENCES k_companies(gu_company);
ALTER TABLE k_member_address ADD CONSTRAINT f4_member_address FOREIGN KEY(gu_contact) REFERENCES k_contacts(gu_contact);
ALTER TABLE k_member_address ADD CONSTRAINT f5_member_address FOREIGN KEY(gu_writer) REFERENCES k_users(gu_user);

