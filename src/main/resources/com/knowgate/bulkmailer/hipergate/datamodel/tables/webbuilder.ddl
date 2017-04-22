CREATE TABLE k_microsites
(
gu_microsite   CHAR(32)     NOT NULL ,
dt_created     DATETIME     DEFAULT CURRENT_TIMESTAMP,
tp_microsite   SMALLINT     DEFAULT 1,
nm_microsite   VARCHAR(128) NOT NULL ,
path_metadata  VARCHAR(254) NOT NULL ,
id_app         INTEGER          NULL ,
gu_workarea    CHAR(32)         NULL ,

CONSTRAINT pk_microsites PRIMARY KEY (gu_microsite)
)
GO;

CREATE TABLE k_pagesets
(
gu_pageset     CHAR(32)     NOT NULL,
dt_created     DATETIME     DEFAULT CURRENT_TIMESTAMP,
gu_workarea    CHAR(32)     NOT NULL,
nm_pageset     VARCHAR(100) NOT NULL,
vs_stamp       VARCHAR(16)  DEFAULT '1.0.0',
id_language    CHAR(2)      DEFAULT 'xx',
bo_urgent      SMALLINT     DEFAULT 0,
path_data      VARCHAR(254) NOT NULL,
gu_microsite   CHAR(32)         NULL,
tp_pageset     VARCHAR(30)      NULL,
id_status      VARCHAR(30)      NULL,
dt_modified    DATETIME         NULL,
gu_company     CHAR(32)         NULL,
gu_project     CHAR(32)         NULL,
tx_email_from  CHARACTER VARYING(254) NULL,
tx_email_reply CHARACTER VARYING(254) NULL,  
nm_from        VARCHAR(254)     NULL,
tx_subject     VARCHAR(254)     NULL,
tx_comments    VARCHAR(255)     NULL,

CONSTRAINT pk_pagesets PRIMARY KEY(gu_pageset),
CONSTRAINT u1_pagesets UNIQUE (gu_workarea,nm_pageset,vs_stamp,id_language)
)
GO;

CREATE TABLE k_pagesets_lookup
(
gu_owner   CHAR(32)    NOT NULL, /* GUID de la workarea */
id_section CHARACTER VARYING(30) NOT NULL, /* Nombre del campo en la tabla base */
pg_lookup  INTEGER     NOT NULL, /* Progresivo del valor */
vl_lookup  VARCHAR(255) NULL,    /* Valor real del lookup */
tr_es      VARCHAR(50)  NULL,    /* Valor que se visualiza en pantalla (esp) */
tr_en      VARCHAR(50)  NULL,    /* Valor que se visualiza en pantalla (ing) */
tr_de      VARCHAR(50)  NULL,
tr_it      VARCHAR(50)  NULL,
tr_fr      VARCHAR(50)  NULL,
tr_pt      VARCHAR(50)  NULL,
tr_ca      VARCHAR(50)  NULL,
tr_gl      VARCHAR(50)  NULL,
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

CONSTRAINT pk_pagesets_lookup PRIMARY KEY (gu_owner,id_section,pg_lookup)
)
GO;

CREATE TABLE k_x_pageset_list (
  gu_list CHAR(32)    NOT NULL,
  gu_pageset CHAR(32) NOT NULL,
  CONSTRAINT pk_x_pageset_list PRIMARY KEY (gu_list,gu_pageset)
)
GO;

CREATE TABLE k_pageset_pages
(
gu_page      CHAR(32)     NOT NULL,
pg_page      INTEGER      NOT NULL,
gu_pageset   CHAR(32)     NOT NULL,
dt_created   DATETIME     DEFAULT CURRENT_TIMESTAMP,
dt_modified  DATETIME         NULL,
tl_page      VARCHAR(255)     NULL,
path_page    VARCHAR(254)     NULL,
path_publish VARCHAR(254)     NULL,

CONSTRAINT pk_pageset_pages PRIMARY KEY(gu_page),
CONSTRAINT u1_pageset_pages UNIQUE(gu_pageset,pg_page)
)
GO;