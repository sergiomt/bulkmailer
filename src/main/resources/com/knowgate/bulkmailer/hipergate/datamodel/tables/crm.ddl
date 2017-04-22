CREATE TABLE k_companies
(
gu_company     CHAR(32)    NOT NULL,
dt_created     DATETIME    DEFAULT CURRENT_TIMESTAMP,
nm_legal       VARCHAR(70) NOT NULL,
gu_workarea    CHAR(32)    NOT NULL,
bo_restricted  SMALLINT    DEFAULT 0,
nm_commercial  VARCHAR(70)     NULL,
dt_modified    DATETIME        NULL,
dt_founded     DATETIME        NULL,
id_batch       VARCHAR(32)     NULL,
id_legal       VARCHAR(16)     NULL,
id_sector      VARCHAR(16)     NULL,
id_status      VARCHAR(30)     NULL,
id_ref         VARCHAR(50)     NULL,
id_fare        VARCHAR(32)     NULL,
id_bpartner    VARCHAR(32)     NULL,
tp_company     VARCHAR(30)     NULL,
gu_geozone     CHAR(32)        NULL,
nu_employees   INTEGER         NULL,
im_revenue     FLOAT           NULL,
gu_sales_man   CHAR(32)        NULL,
tx_franchise   VARCHAR(100)    NULL,
de_company     VARCHAR(254)    NULL,

CONSTRAINT pk_companies PRIMARY KEY(gu_company),
CONSTRAINT u1_companies UNIQUE(gu_workarea,nm_legal),
CONSTRAINT c1_companies CHECK (nm_legal IS NULL OR LENGTH(nm_legal)>0),
CONSTRAINT c2_companies CHECK (id_legal IS NULL OR LENGTH(id_legal)>0),
CONSTRAINT c3_companies CHECK (id_ref IS NULL OR LENGTH(id_ref)>0),
CONSTRAINT c4_companies CHECK (id_sector IS NULL OR LENGTH(id_sector)>0),
CONSTRAINT c5_companies CHECK (tx_franchise IS NULL OR LENGTH(tx_franchise)>0)
)
GO;

CREATE TABLE k_x_company_addr
(
gu_company  CHAR(32) NOT NULL,
gu_address  CHAR(32) NOT NULL,

CONSTRAINT pk_x_company_addr PRIMARY KEY(gu_company,gu_address)
)
GO;

CREATE TABLE k_companies_lookup
(
gu_owner   CHAR(32) NOT NULL,	 /* GUID de la workarea */
id_section CHARACTER VARYING(30) NOT NULL, /* Nombre del campo en la tabla base */
pg_lookup  INTEGER  NOT NULL,    /* Progresivo del valor */
vl_lookup  VARCHAR(255) NULL,    /* Valor real del lookup */
tr_es      VARCHAR(50)  NULL,    /* Valor que se visualiza en pantalla (esp) */
tr_en      VARCHAR(50)  NULL,    /* Valor que se visualiza en pantalla (ing) */
tr_de      VARCHAR(50)  NULL,
tr_it      VARCHAR(50)  NULL,
tr_fr      VARCHAR(50)  NULL,
tr_pt      VARCHAR(50)  NULL,
tr_ca      VARCHAR(50)  NULL,
tr_eu      VARCHAR(50)  NULL,
tr_ja      VARCHAR(50)  NULL,
tr_cn      VARCHAR(50)  NULL,
tr_tw      VARCHAR(50)  NULL,
tr_fi      VARCHAR(50)  NULL,
tr_ru      VARCHAR(50)  NULL,
tr_nl      VARCHAR(50)  NULL,
tr_th      VARCHAR(50)  NULL,
tr_cs      VARCHAR(50)  NULL,
tr_uk      VARCHAR(50)  NULL,
tr_no      VARCHAR(50)  NULL,
tr_ko      VARCHAR(50)  NULL,
tr_sk      VARCHAR(50)  NULL,
tr_pl      VARCHAR(50)  NULL,
tr_vn      VARCHAR(50)  NULL,

CONSTRAINT pk_companies_lookup PRIMARY KEY (gu_owner,id_section,pg_lookup),
CONSTRAINT u1_companies_lookup UNIQUE (gu_owner,id_section,vl_lookup)
)
GO;

CREATE TABLE k_contacts
(
gu_contact     CHAR(32) NOT NULL,   /* GUID del individuo */
gu_workarea    CHAR(32) NOT NULL,   /* GUID de la workarea */
dt_created     DATETIME DEFAULT CURRENT_TIMESTAMP,
bo_restricted  SMALLINT DEFAULT 0, /* Si tiene restricciones de acceso por grupo o no */
bo_private     SMALLINT DEFAULT 0, /* Contacto privado del usuario que lo cre� */
nu_notes       INTEGER  DEFAULT 0, /* Cuenta de notas asociadas */
nu_attachs     INTEGER  DEFAULT 0, /* Cuenta de archivos adjuntos */
bo_change_pwd  SMALLINT DEFAULT 1, /* May user change its own password? */
tx_nickname    VARCHAR(100) NULL,  /* New for v2.1 */
tx_pwd         VARCHAR(50)  NULL,  /* New for v2.1 */
tx_challenge   VARCHAR(100) NULL,  /* New for v2.1 */
tx_reply       VARCHAR(100) NULL,  /* New for v2.1 */
dt_pwd_expires DATETIME	    NULL,  /* New for v2.1 */
dt_modified    DATETIME     NULL,  /* Fecha de Modificaci�n del registro */
gu_writer      CHAR(32)     NULL,  /* GUID del usuario propietario del registro */
gu_company     CHAR(32)     NULL,  /* GUID de la compa��a a la que pertenece el individuo */
id_batch       VARCHAR(32)  NULL,  /* Lote de trabajo del cual provenia la carga del registro */
id_status      VARCHAR(30)  NULL,  /* Estado, activo, cambio de trabajo, etc. */
id_ref         VARCHAR(50)  NULL,  /* Identificador externo de registro (para interfaz con otras applicaciones) */
id_fare        VARCHAR(32)  NULL,  /* Tarifa aplicable al contacto */
id_bpartner    VARCHAR(32)  NULL,  /* Identificador de el contacto en Openbravo */
tx_name        VARCHAR(100) NULL,  /* Nombre de Pila */
tx_surname     VARCHAR(100) NULL,  /* Apellidos */
de_title       VARCHAR(70)  NULL,  /* Empleo/Puesto */
id_gender      CHAR(1)      NULL,  /* Sexo */
dt_birth       DATETIME     NULL,  /* Fecha Nacimiento */
ny_age	       SMALLINT     NULL,  /* Edad */
id_nationality CHAR(3)      NULL,  /* Country of nationality */
sn_passport    VARCHAR(16)  NULL,  /* N� doc identidad legal */
tp_passport    CHAR(1)      NULL,  /* Tipo doc identidad legal */
sn_drivelic    VARCHAR(16)  NULL,  /* Permiso de conducir */
dt_drivelic    DATETIME     NULL,  /* Fecha expedicion permiso de conducir */
tx_dept        VARCHAR(70)  NULL,  /* Departamento */
tx_division    VARCHAR(70)  NULL,  /* Divisi�n */
gu_geozone     CHAR(32)     NULL,  /* Zona Geogr�fica */
gu_sales_man   CHAR(32)     NULL,  /* Vendedor */
tx_comments    VARCHAR(254) NULL,  /* Comentarios */
url_linkedin   CHARACTER VARYING(254) NULL,
url_facebook   CHARACTER VARYING(254) NULL,
url_twitter    CHARACTER VARYING(254) NULL,

CONSTRAINT pk_contacts PRIMARY KEY (gu_contact),
CONSTRAINT c1_contacts CHECK (tx_name IS NULL OR LENGTH(tx_name)>0),
CONSTRAINT c2_contacts CHECK (tx_surname IS NULL OR LENGTH(tx_surname)>0),
CONSTRAINT c3_contacts CHECK (id_ref IS NULL OR LENGTH(id_ref)>0),
CONSTRAINT c4_contacts CHECK (de_title IS NULL OR LENGTH(de_title)>0)
)
GO;


CREATE TABLE k_x_contact_addr
(
gu_contact  CHAR(32) NOT NULL,
gu_address  CHAR(32) NOT NULL,

CONSTRAINT pk_x_contact_addr PRIMARY KEY(gu_contact,gu_address)
)
GO;

CREATE TABLE k_contacts_lookup
(
gu_owner   CHAR(32) NOT NULL,
id_section CHARACTER VARYING(30) NOT NULL,
pg_lookup  INTEGER  NOT NULL,
vl_lookup  VARCHAR(255) NULL,
tr_es      VARCHAR(50)  NULL,
tr_en      VARCHAR(50)  NULL,
tr_de      VARCHAR(50)  NULL,
tr_it      VARCHAR(50)  NULL,
tr_fr      VARCHAR(50)  NULL,
tr_pt      VARCHAR(50)  NULL,
tr_ca      VARCHAR(50)  NULL,
tr_eu      VARCHAR(50)  NULL,
tr_ja      VARCHAR(50)  NULL,
tr_cn      VARCHAR(50)  NULL,
tr_tw      VARCHAR(50)  NULL,
tr_fi      VARCHAR(50)  NULL,
tr_ru      VARCHAR(50)  NULL,
tr_nl      VARCHAR(50)  NULL,
tr_th      VARCHAR(50)  NULL,
tr_cs      VARCHAR(50)  NULL,
tr_uk      VARCHAR(50)  NULL,
tr_no      VARCHAR(50)  NULL,
tr_ko      VARCHAR(50)  NULL,
tr_sk      VARCHAR(50)  NULL,
tr_pl      VARCHAR(50)  NULL,
tr_vn      VARCHAR(50)  NULL,

CONSTRAINT pk_contacts_lookup PRIMARY KEY (gu_owner,id_section,pg_lookup),
CONSTRAINT f1_contacts_lookup  FOREIGN KEY(gu_owner) REFERENCES k_workareas(gu_workarea),
CONSTRAINT u1_contacts_lookup UNIQUE (gu_owner,id_section,vl_lookup)
)
GO;

CREATE TABLE k_member_address
(
gu_address      CHAR(32) NOT NULL,
ix_address      INTEGER  NOT NULL,
gu_workarea     CHAR(32) NOT NULL,
gu_company      CHAR(32) NULL,
gu_contact      CHAR(32) NULL,
dt_created      DATETIME NULL,
dt_modified     DATETIME NULL,
bo_private      SMALLINT DEFAULT 0,
gu_writer       CHAR(32) NULL,
tx_name         VARCHAR(100) NULL,
tx_surname      VARCHAR(100) NULL,
nm_commercial   VARCHAR(70)  NULL,
nm_legal        VARCHAR(70)  NULL,
id_legal        VARCHAR(16)  NULL,
id_sector       VARCHAR(16)  NULL,
de_title        VARCHAR(70)  NULL,
tr_title        VARCHAR(50)  NULL,
id_status       VARCHAR(30)  NULL,
id_ref          VARCHAR(50)  NULL,
dt_birth        DATETIME NULL,
sn_passport     VARCHAR(16) NULL,
tx_comments     VARCHAR(254) NULL,
id_gender       CHAR(1) NULL,
tp_company      VARCHAR(30) NULL,
nu_employees    INTEGER NULL,
im_revenue      FLOAT NULL,
gu_sales_man    CHAR(32) NULL,
tx_franchise    VARCHAR(100) NULL,
gu_geozone      CHAR(32) NULL,
ny_age          SMALLINT NULL,
id_nationality  CHAR(3)      NULL,
tx_dept         VARCHAR(70)  NULL,
tx_division     VARCHAR(70)  NULL,
tp_location     VARCHAR(16)  NULL,
tp_street       VARCHAR(16)  NULL,
nm_street       VARCHAR(100) NULL,
nu_street       VARCHAR(16)  NULL,
tx_addr1        VARCHAR(100) NULL,
tx_addr2        VARCHAR(100) NULL,
full_addr       VARCHAR(200) NULL,
id_country      CHAR(3) NULL,
nm_country      VARCHAR(50) NULL,
id_state        VARCHAR(16) NULL,
nm_state        VARCHAR(30) NULL,
mn_city         VARCHAR(50) NULL,
zipcode         VARCHAR(30) NULL,
work_phone      VARCHAR(16) NULL,
direct_phone    VARCHAR(16) NULL,
home_phone      VARCHAR(16) NULL,
mov_phone       VARCHAR(16) NULL,
fax_phone       VARCHAR(16) NULL,
other_phone     VARCHAR(16) NULL,
po_box          VARCHAR(50) NULL,
tx_email        CHARACTER VARYING(100) NULL,
url_addr        CHARACTER VARYING(254) NULL,
url_linkedin    CHARACTER VARYING(254) NULL,
url_facebook    CHARACTER VARYING(254) NULL,
url_twitter     CHARACTER VARYING(254) NULL,
contact_person  VARCHAR(100) NULL,
tx_salutation   VARCHAR(16)  NULL,
tx_remarks      VARCHAR(254) NULL,

CONSTRAINT pk_member_address PRIMARY KEY (gu_address),
CONSTRAINT c2_member_address CHECK (tx_name IS NULL OR LENGTH(tx_name)>0),
CONSTRAINT c3_member_address CHECK (tx_surname IS NULL OR LENGTH(tx_surname)>0),
CONSTRAINT c4_member_address CHECK (id_ref IS NULL OR LENGTH(id_ref)>0),
CONSTRAINT c5_member_address CHECK (nm_legal IS NULL OR LENGTH(nm_legal)>0),
CONSTRAINT c6_member_address CHECK (id_legal IS NULL OR LENGTH(id_legal)>0),
CONSTRAINT c7_member_address CHECK (id_sector IS NULL OR LENGTH(id_sector)>0),
CONSTRAINT c8_member_address CHECK (tx_franchise IS NULL OR LENGTH(tx_franchise)>0)
)
GO;