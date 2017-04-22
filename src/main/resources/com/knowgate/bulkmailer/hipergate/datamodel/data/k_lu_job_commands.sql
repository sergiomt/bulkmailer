INSERT INTO k_lu_job_commands (id_command,tx_command,nm_class) VALUES ('SMS','SEND SMS PUSH TEXT MESSAGE','com.knowgate.scheduler.jobs.SMSSender');
INSERT INTO k_lu_job_commands (id_command,tx_command,nm_class) VALUES ('SEND','SEND MIME MESSAGES BY SMTP','com.knowgate.scheduler.jobs.MimeSender');
INSERT INTO k_lu_job_commands (id_command,tx_command,nm_class) VALUES ('DUMY','DUMMY TESTING JOB','com.knowgate.scheduler.jobs.DummyJob');

INSERT INTO k_lu_job_commands (id_command,tx_command,nm_class) VALUES ('INVT','INVITE','com.clocial.webutils.InvitationJob');

INSERT INTO k_lu_job_commands (id_command,tx_command,nm_class) VALUES ('VOID','DO NOTHING','com.knowgate.scheduler.events.DoNothing');
INSERT INTO k_lu_job_commands (id_command,tx_command,nm_class) VALUES ('BEAN','EXECUTE BEAN SHELL SCRIPT','com.knowgate.scheduler.events.ExecuteBeanShell');
