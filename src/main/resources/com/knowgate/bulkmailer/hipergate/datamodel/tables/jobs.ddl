
CREATE TABLE k_lu_job_commands
(
id_command    CHAR(4) NOT NULL,			/* Comando de ejecuci�n */
tx_command    VARCHAR(254) NULL,		/* Descripci�n de la acci�n */
nm_class      CHARACTER VARYING(254) NULL,      /* Nombre de la clase Java que ejecuta la accion */

CONSTRAINT pk_lu_job_commands PRIMARY KEY(id_command)
)
GO;

CREATE TABLE k_lu_job_status
(
    id_status SMALLINT NOT NULL,
    tr_en VARCHAR(30)  NULL,
    tr_es VARCHAR(30)  NULL,
    tr_de VARCHAR(30)  NULL,
    tr_it VARCHAR(30)  NULL,
    tr_fr VARCHAR(30)  NULL,
    tr_pt VARCHAR(30)  NULL,
    tr_ca VARCHAR(30)  NULL,
    tr_eu VARCHAR(30)  NULL,
    tr_ja VARCHAR(30)  NULL,
    tr_cn VARCHAR(30)  NULL,
    tr_tw VARCHAR(30)  NULL,
    tr_ru VARCHAR(30)  NULL,
    tr_nl VARCHAR(30)  NULL,
    tr_th VARCHAR(30)  NULL,
    tr_cs VARCHAR(30)  NULL,
    tr_uk VARCHAR(30)  NULL,
    tr_no VARCHAR(30)  NULL,
    tr_sk VARCHAR(30)  NULL,
    tr_pl VARCHAR(30)  NULL,
    tr_vn VARCHAR(30)  NULL,
    tr_u1 VARCHAR(30)  NULL,
    tr_u2 VARCHAR(30)  NULL,
    tr_u3 VARCHAR(30)  NULL,
    tr_u4 VARCHAR(30)  NULL,

    CONSTRAINT pk_lu_job_status PRIMARY KEY (id_status)
)
GO;

CREATE TABLE k_jobs
(
gu_job	      CHAR(32)    NOT NULL,	        /* GUID del proceso */
gu_workarea   CHAR(32)    NOT NULL,		/* GUID de la workarea */
gu_writer     CHAR(32)    NOT NULL,		/* GUID del usuario que cre� el job */
id_command    CHAR(4)     NOT NULL,		/* Comando de ejecuci�n */
id_status     SMALLINT    NOT NULL,  		/* Estado, pendiente, suspendido, abortado, terminado. */
dt_created    DATETIME    DEFAULT CURRENT_TIMESTAMP, /* Fecha de creaci�n del registro */
tl_job        VARCHAR(100)    NULL,	        /* Titulo descriptivo del job */
gu_job_group  CHAR(32)        NULL,             /* GUID del lote de proceso */
tx_parameters VARCHAR(2000)   NULL,		/* Par�metros de ejecuci�n (atributo=valor,atributo=valor,...) */
dt_execution  DATETIME        NULL,		/* Fecha programada de ejecuci�n */
dt_finished   DATETIME        NULL,  	        /* Fecha de terminaci�n */
dt_modified   DATETIME        NULL,	        /* Fecha de modificaci�n del registro */
nu_sent       INTEGER    DEFAULT 0,
nu_opened     INTEGER    DEFAULT 0,
nu_unique     INTEGER    DEFAULT 0,
nu_clicks     INTEGER    DEFAULT 0,
CONSTRAINT pk_jobs PRIMARY KEY(gu_job)
)
GO;

CREATE TABLE k_job_atoms
(
gu_job         CHAR(32)   NOT NULL,
pg_atom        SERIAL,
dt_execution   DATETIME   DEFAULT CURRENT_TIMESTAMP,
id_status      SMALLINT   DEFAULT 1,
id_format      CHARACTER VARYING(4)   DEFAULT 'TXT',
tp_recipient   CHARACTER VARYING(4)   NULL,
gu_company     CHAR(32)     NULL,
gu_contact     CHAR(32)     NULL,
tx_email       CHARACTER VARYING(100) NULL,
tx_name        VARCHAR(200) NULL,
tx_surname     VARCHAR(200) NULL,
tx_salutation  VARCHAR(16)  NULL,
nm_commercial  VARCHAR(70)  NULL,
tp_street      VARCHAR(16)  NULL,
nm_street      VARCHAR(100) NULL,
nu_street      VARCHAR(16)  NULL,
tx_addr1       VARCHAR(100) NULL,
tx_addr2       VARCHAR(100) NULL,
nm_country     VARCHAR(50)  NULL,
nm_state       VARCHAR(30)  NULL,
mn_city	       VARCHAR(50)  NULL,
zipcode	       VARCHAR(30)  NULL,
work_phone     VARCHAR(16)  NULL,
direct_phone   VARCHAR(16)  NULL,
home_phone     VARCHAR(16)  NULL,
mov_phone      VARCHAR(16)  NULL,
fax_phone      VARCHAR(16)  NULL,
other_phone    VARCHAR(16)  NULL,
po_box         VARCHAR(50)  NULL,
tx_intro       VARCHAR(254) NULL,
tx_url         VARCHAR(254) NULL,
tx_log         VARCHAR(254) NULL,

CONSTRAINT pk_job_atoms PRIMARY KEY(pg_atom)
)
GO;

CREATE TABLE k_job_atoms_archived
(
gu_job         CHAR(32)   NOT NULL,
pg_atom        INTEGER    NOT NULL,
dt_execution   DATETIME   DEFAULT CURRENT_TIMESTAMP,
id_status      SMALLINT   DEFAULT 1,
id_format      VARCHAR(4) DEFAULT 'TXT',
gu_company     CHAR(32)     NULL,
gu_contact     CHAR(32)     NULL,
tx_email       CHARACTER VARYING(100) NULL,
tx_name        VARCHAR(200) NULL,
tx_surname     VARCHAR(200) NULL,
tx_salutation  VARCHAR(16)  NULL,
nm_commercial  VARCHAR(70)  NULL,
tp_street      VARCHAR(16)  NULL,
nm_street      VARCHAR(100) NULL,
nu_street      VARCHAR(16)  NULL,
tx_addr1       VARCHAR(100) NULL,
tx_addr2       VARCHAR(100) NULL,
nm_country     VARCHAR(50)  NULL,
nm_state       VARCHAR(30)  NULL,
mn_city	       VARCHAR(50)  NULL,
zipcode	       VARCHAR(30)  NULL,
work_phone     VARCHAR(16)  NULL,
direct_phone   VARCHAR(16)  NULL,
home_phone     VARCHAR(16)  NULL,
mov_phone      VARCHAR(16)  NULL,
fax_phone      VARCHAR(16)  NULL,
other_phone    VARCHAR(16)  NULL,
po_box         VARCHAR(50)  NULL,
tx_intro       VARCHAR(254) NULL,
tx_url         VARCHAR(254) NULL,
tx_log         VARCHAR(254) NULL,

CONSTRAINT pk_job_atoms_archived PRIMARY KEY(gu_job, pg_atom)
)
GO;

CREATE TABLE k_job_atoms_tracking
(
gu_job         CHAR(32)   NOT NULL,
pg_atom        INTEGER    NOT NULL,
dt_action      DATETIME   DEFAULT CURRENT_TIMESTAMP,
id_status      SMALLINT   DEFAULT 1,
gu_company     CHAR(32)     NULL,
gu_contact     CHAR(32)     NULL,
ip_addr        CHARACTER VARYING(16) NULL,
tx_email       CHARACTER VARYING(100) NULL,
user_agent     VARCHAR(254) NULL
)
GO;

CREATE TABLE k_jobs_atoms_by_day
(
dt_execution  CHAR(10)    NOT NULL,
gu_job        CHAR(32)    NOT NULL,
gu_workarea   CHAR(32)    NOT NULL,
gu_job_group  CHAR(32)        NULL,
nu_msgs       INTEGER    DEFAULT 0,
nu_docs       INTEGER    DEFAULT 0,
CONSTRAINT pk_jobs_atoms_by_day PRIMARY KEY(dt_execution,gu_job)
)  
GO;

CREATE TABLE k_jobs_atoms_by_hour
(
dt_hour       SMALLINT    NOT NULL,
gu_job        CHAR(32)    NOT NULL,
gu_workarea   CHAR(32)    NOT NULL,
gu_job_group  CHAR(32)        NULL,
nu_msgs       INTEGER    DEFAULT 0,
CONSTRAINT pk_jobs_atoms_by_hour PRIMARY KEY(dt_hour,gu_job)
)  
GO;

CREATE TABLE k_jobs_atoms_by_agent
(
id_agent      VARCHAR(50) NOT NULL,
gu_job        CHAR(32)    NOT NULL,
gu_workarea   CHAR(32)    NOT NULL,
gu_job_group  CHAR(32)        NULL,
nu_msgs       INTEGER    DEFAULT 0,
CONSTRAINT pk_jobs_atoms_by_agent PRIMARY KEY(id_agent,gu_job)
)  
GO;

CREATE TABLE k_job_atoms_clicks
(
gu_job         CHAR(32)   NOT NULL,
pg_atom        INTEGER    NOT NULL,
gu_url         CHAR(32)   NOT NULL,
dt_action      DATETIME   DEFAULT CURRENT_TIMESTAMP,
id_status      SMALLINT   DEFAULT 1,
gu_company     CHAR(32)     NULL,
gu_contact     CHAR(32)     NULL,
ip_addr        CHARACTER VARYING(16) NULL,
tx_email       CHARACTER VARYING(100) NULL
)
GO;
