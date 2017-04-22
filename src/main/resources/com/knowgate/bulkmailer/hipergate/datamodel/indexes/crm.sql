CREATE INDEX i1_companies ON k_companies(gu_workarea);
CREATE INDEX i2_companies ON k_companies(gu_workarea,nm_commercial);
CREATE INDEX i3_companies ON k_companies(gu_workarea,id_legal);
CREATE INDEX i4_companies ON k_companies(gu_workarea,id_sector);
CREATE INDEX i5_companies ON k_companies(gu_workarea,id_ref);

CREATE INDEX i1_contacts ON k_contacts(gu_workarea);
CREATE INDEX i2_contacts ON k_contacts(gu_company);
CREATE INDEX i3_contacts ON k_contacts(tx_name);
CREATE INDEX i4_contacts ON k_contacts(tx_surname);
CREATE INDEX i5_contacts ON k_contacts(gu_workarea,dt_birth);
CREATE INDEX i6_contacts ON k_contacts(gu_workarea,ny_age);
CREATE INDEX i7_contacts ON k_contacts(gu_writer);
CREATE INDEX i8_contacts ON k_contacts(sn_passport);

CREATE INDEX i1_x_contact_addr ON k_x_contact_addr(gu_contact);
CREATE INDEX i2_x_contact_addr ON k_x_contact_addr(gu_address);

