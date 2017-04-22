
CREATE TABLE k_version
(
vs_stamp     VARCHAR(16)  NOT NULL,
dt_created   DATETIME     DEFAULT CURRENT_TIMESTAMP,
dt_modified  DATETIME     NULL,
bo_register  SMALLINT     DEFAULT 0,
bo_allow_stats SMALLINT   DEFAULT 0,
gu_support   CHAR(32)     NULL,
gu_contact   CHAR(32)     NULL,
tx_name      VARCHAR(100) NULL,
tx_surname   VARCHAR(100) NULL,
nu_employees INTEGER      NULL,
nm_company   VARCHAR(70)  NULL,
id_sector    VARCHAR(16)  NULL,
id_country   CHAR(3)      NULL,
nm_state     VARCHAR(30)  NULL,
mn_city	     VARCHAR(50)  NULL,
zipcode	     VARCHAR(30)  NULL,
work_phone   VARCHAR(16)  NULL,
tx_email     VARCHAR(70)  NULL,

CONSTRAINT pk_version PRIMARY KEY (vs_stamp)
)
GO;
