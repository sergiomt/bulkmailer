
CREATE TABLE k_lu_languages
(
    id_language  CHAR(2) NOT NULL, /* Language code */
    tr_lang_en   VARCHAR(50) NULL, /* English Translation */
    tr_lang_es   VARCHAR(50) NULL, /* Spanish Translation */
    tr_lang_fr   VARCHAR(50) NULL, /* French  Translation */
    tr_lang_de   VARCHAR(50) NULL, /* Deutch  Translation */
    tr_lang_it   VARCHAR(50) NULL, /* Italian Translation */
    tr_lang_pt   VARCHAR(50) NULL, /* Portuguese Translation */
    tr_lang_ca   VARCHAR(50) NULL, /* Catalan Translation */
    tr_lang_eu   VARCHAR(50) NULL, /* Basque  Translation */
    tr_lang_ja   VARCHAR(50) NULL, /* Japanese Translation */
    tr_lang_cn   VARCHAR(50) NULL, /* Simplified Chinese Translation */
    tr_lang_tw   VARCHAR(50) NULL, /* Traditional Chinese Translation */
    tr_lang_fi   VARCHAR(50) NULL, /* Finnish Translation */
    tr_lang_ru   VARCHAR(50) NULL, /* Russian Translation */
    tr_lang_pl   VARCHAR(50) NULL, /* Polish Translation */
    tr_lang_nl   VARCHAR(50) NULL, /* Dutch Translation */
    tr_lang_th   VARCHAR(50) NULL, /* Thai Translation */
    tr_lang_cs   VARCHAR(50) NULL, /* Czech Translation */
    tr_lang_uk   VARCHAR(50) NULL, /* Ukranian Translation */
    tr_lang_no   VARCHAR(50) NULL, /* Norwegian Translation */
    tr_lang_u1   VARCHAR(50) NULL, /* User Defined Translation 1 */
    tr_lang_u2   VARCHAR(50) NULL, /* User Defined Translation 2 */
    tr_lang_u3   VARCHAR(50) NULL, /* User Defined Translation 3 */
    tr_lang_u4   VARCHAR(50) NULL, /* User Defined Translation 4 */

    CONSTRAINT pk_lu_languages PRIMARY KEY (id_language)
)
GO;

CREATE TABLE k_lu_countries
(
    id_country      CHAR(3) NOT NULL,
    tr_country_en   VARCHAR(50) NULL, /* English Translation */
    tr_country_es   VARCHAR(50) NULL, /* Spanish Translation */
    tr_country_fr   VARCHAR(50) NULL, /* French  Translation */
    tr_country_de   VARCHAR(50) NULL, /* Deutch  Translation */
    tr_country_it   VARCHAR(50) NULL, /* Italian Translation */
    tr_country_pt   VARCHAR(50) NULL, /* Portuguese Translation */
    tr_country_ca   VARCHAR(50) NULL, /* Catalan Translation */
    tr_country_eu   VARCHAR(50) NULL, /* Basque Translation */
    tr_country_ja   VARCHAR(50) NULL, /* Japanese Translation */
    tr_country_cn   VARCHAR(50) NULL, /* Simplified Chinese Translation */
    tr_country_tw   VARCHAR(50) NULL, /* Traditional Chinese Translation */
    tr_country_fi   VARCHAR(50) NULL, /* Finnish Translation */
    tr_country_ru   VARCHAR(50) NULL, /* Russian Translation */
    tr_country_pl   VARCHAR(50) NULL, /* Polish Translation */
    tr_country_nl   VARCHAR(50) NULL, /* Dutch Translation */
    tr_country_th   VARCHAR(50) NULL, /* Thai Translation */
    tr_country_cs   VARCHAR(50) NULL, /* Czech Translation */
    tr_country_uk   VARCHAR(50) NULL, /* Ukranian Translation */
    tr_country_no   VARCHAR(50) NULL, /* Norwegian Translation */
    tr_country_u1   VARCHAR(50) NULL, /* User Defined Translation 1 */
    tr_country_u2   VARCHAR(50) NULL, /* User Defined Translation 2 */
    tr_country_u3   VARCHAR(50) NULL, /* User Defined Translation 3 */
    tr_country_u4   VARCHAR(50) NULL, /* User Defined Translation 4 */

    CONSTRAINT pk_lu_countries PRIMARY KEY (id_country)
)
GO;


CREATE TABLE k_lu_states
(
    id_state      CHAR(9) NOT NULL,
    id_country    CHAR(3) NOT NULL,
    nm_region     VARCHAR(32) NULL,
    id_parent     VARCHAR(32) NULL,
    nm_state      VARCHAR(32) NULL,
    zip_code      VARCHAR(16) NULL,
    tr_state_en   VARCHAR(50) NULL, /* English Translation */
    tr_state_es   VARCHAR(50) NULL, /* Spanish Translation */
    tr_state_de   VARCHAR(50) NULL, /* Deutch  Translation */
    tr_state_it   VARCHAR(50) NULL, /* Italian Translation */
    tr_state_fr   VARCHAR(50) NULL, /* French  Translation */
    tr_state_pt   VARCHAR(50) NULL, /* Portuguese Translation */
    tr_state_eu   VARCHAR(50) NULL, /* Basque Translation */
    tr_state_gl   VARCHAR(50) NULL, /* Galician Translation */
    tr_state_ca   VARCHAR(50) NULL, /* Catalan Translation */
    tr_state_ja   VARCHAR(50) NULL, /* Japanese Translation */
    tr_state_cn   VARCHAR(50) NULL, /* Chinese Translation */
    tr_state_tw   VARCHAR(50) NULL, /* Chinese Translation */
    tr_state_fi   VARCHAR(50) NULL, /* Finnish Translation */
    tr_state_ru   VARCHAR(50) NULL, /* Russian Translation */
    tr_state_nl  VARCHAR(100)  NULL,
    tr_state_th  VARCHAR(100)  NULL,
    tr_state_cs  VARCHAR(100)  NULL,
    tr_state_uk  VARCHAR(100)  NULL,
    tr_state_no  VARCHAR(100)  NULL,
    tr_state_sk  VARCHAR(100)  NULL,
    tr_state_u1   VARCHAR(50) NULL, /* User Defined Translation 1 */
    tr_state_u2   VARCHAR(50) NULL, /* User Defined Translation 2 */
    tr_state_u3   VARCHAR(50) NULL, /* User Defined Translation 3 */
    tr_state_u4   VARCHAR(50) NULL, /* User Defined Translation 4 */

    CONSTRAINT pk_lu_states PRIMARY KEY (id_state),
    CONSTRAINT u1_lu_states UNIQUE (id_country,nm_state),
    CONSTRAINT u2_lu_states UNIQUE (id_country,zip_code)
)
GO;
  
CREATE TABLE k_lu_status
(
    id_status SMALLINT NOT NULL,
    tr_en VARCHAR(30)  NULL,
    tr_es VARCHAR(30)  NULL,
    tr_de VARCHAR(30)  NULL,
    tr_it VARCHAR(30)  NULL,
    tr_fr VARCHAR(30)  NULL,
    tr_nl VARCHAR(30)  NULL,
    tr_pt VARCHAR(30)  NULL,
    tr_ca VARCHAR(30)  NULL,
    tr_gl VARCHAR(30)  NULL,
    tr_eu VARCHAR(30)  NULL,
    tr_ja VARCHAR(30)  NULL,
    tr_cn VARCHAR(30)  NULL,
    tr_cs VARCHAR(30)  NULL,
    tr_tw VARCHAR(30)  NULL,
    tr_th VARCHAR(30)  NULL,
    tr_fi VARCHAR(30)  NULL,
    tr_no VARCHAR(30)  NULL,
    tr_ru VARCHAR(30)  NULL,
    tr_sk VARCHAR(30)  NULL,
    tr_pl VARCHAR(30)  NULL,
    tr_uk VARCHAR(30)  NULL,
    tr_vn VARCHAR(30)  NULL,
    
    tr_u1 VARCHAR(30)  NULL,
    tr_u2 VARCHAR(30)  NULL,
    tr_u3 VARCHAR(30)  NULL,
    tr_u4 VARCHAR(30)  NULL,

    CONSTRAINT pk_lu_status PRIMARY KEY (id_status)
)
GO;


CREATE TABLE k_lu_cont_types
(

    id_container_type INTEGER NOT NULL,
    nm_container      VARCHAR(50) NOT NULL,

    CONSTRAINT pk_lu_cont_types PRIMARY KEY (id_container_type)
)
GO;

CREATE TABLE k_lu_prod_types
(
    id_prod_type   VARCHAR(5) NOT NULL,
    de_prod_type   VARCHAR(254) NULL,
    nm_icon	       VARCHAR(254) NULL,
    mime_type      VARCHAR(100) NULL,

    CONSTRAINT pk_lu_prod_types PRIMARY KEY (id_prod_type)
)
GO;
