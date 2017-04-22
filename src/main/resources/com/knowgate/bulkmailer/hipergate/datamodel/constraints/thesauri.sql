ALTER TABLE k_images ADD CONSTRAINT f1_images FOREIGN KEY (gu_writer) REFERENCES k_users(gu_user);
ALTER TABLE k_images ADD CONSTRAINT f2_images FOREIGN KEY (gu_workarea) REFERENCES k_workareas(gu_workarea);

ALTER TABLE k_addresses ADD CONSTRAINT f1_addresses FOREIGN KEY(id_country) REFERENCES k_lu_countries(id_country);
ALTER TABLE k_addresses ADD CONSTRAINT f2_addresses FOREIGN KEY(gu_workarea) REFERENCES k_workareas(gu_workarea);

ALTER TABLE k_addresses_lookup ADD CONSTRAINT f1_addresses_lookup FOREIGN KEY(gu_owner) REFERENCES k_workareas(gu_workarea);


