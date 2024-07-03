
CREATE TABLE IF NOT EXISTS public.workflowprocess
(
    workflow_id integer,
    uuid uuid NOT NULL DEFAULT public.gen_random_uuid(),
    subject character varying COLLATE pg_catalog."default",
    init_date timestamp with time zone,
    item uuid,
    submitter_id uuid,
    assignduedate timestamp with time zone,
    priority character varying COLLATE pg_catalog."default",
    workflowprocesssenderdiary uuid,
    workflowprocesscorrespondence uuid,
    dispatchmode_id uuid,
    eligible_for_filing_id uuid,
    workflow_type_id uuid,
    workflow_status_id uuid,
    workflowprocessoutwarddetails_idf uuid,
    workflowprocessinwarddetails_idf uuid,
    workflow_priority_id uuid,
    workflowprocessdraftdetails_idf uuid,
    workflowprocessnote_idf uuid,
    isdelete boolean,
    ismode boolean DEFAULT 'false',
    isread boolean DEFAULT 'false',
    remark character varying COLLATE pg_catalog."default",
    action_id uuid,
    isreplydraft boolean,
    isacknowledgement boolean,
    CONSTRAINT workflowprocess_pkey PRIMARY KEY (uuid)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.workflowprocess
    OWNER to dspace;




	CREATE TABLE IF NOT EXISTS public.workflowprocesstemplate
(
    uuid uuid NOT NULL,
    workflowprocesstemplate_id integer,
    init_date timestamp with time zone,
    template uuid,
    index integer,
    eperson uuid,
    CONSTRAINT workflowprocesstemplate_pkey PRIMARY KEY (uuid)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.workflowprocesstemplate
    OWNER to dspace;



	-- FUNCTION: public.gen_random_uuid()

-- DROP FUNCTION IF EXISTS public.gen_random_uuid();

CREATE OR REPLACE FUNCTION public.gen_random_uuid(
	)
    RETURNS uuid
    LANGUAGE 'c'
    COST 1
    VOLATILE PARALLEL SAFE
AS '$libdir/pgcrypto', 'pg_random_uuid'
;

ALTER FUNCTION public.gen_random_uuid()
    OWNER TO postgres;


	CREATE TABLE IF NOT EXISTS public.workflowprocesscomment
(
    uuid uuid NOT NULL,
    workflowprocesscomment_id integer,
    comment character varying COLLATE pg_catalog."default",
    workflowprocessreferencedoc_idf uuid,
    workflowprocesshistory_idf uuid,
    submitter_id uuid,
    workflowprocess_fid uuid,
    note uuid,
    actiondate timestamp with time zone,
    isdraftsave boolean,
    CONSTRAINT workflowprocesscomment_pkey PRIMARY KEY (uuid)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.workflowprocesscomment
    OWNER to dspace;


	-- Table: public.workflowprocesscorrespondence

-- DROP TABLE IF EXISTS public.workflowprocesscorrespondence;

CREATE TABLE IF NOT EXISTS public.workflowprocesscorrespondence
(
    workflowprocesscorrespondence_id integer,
    uuid uuid NOT NULL DEFAULT public.gen_random_uuid(),
    diarynumber character varying COLLATE pg_catalog."default",
    officelocation character varying COLLATE pg_catalog."default",
    diarydate timestamp with time zone,
    filenumber character varying COLLATE pg_catalog."default",
    filetype character varying COLLATE pg_catalog."default",
    CONSTRAINT workflowprocesscorrespondence_pkey PRIMARY KEY (uuid)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.workflowprocesscorrespondence
    OWNER to dspace;


	-- Table: public.workflowprocesseperson

-- DROP TABLE IF EXISTS public.workflowprocesseperson;

CREATE TABLE IF NOT EXISTS public.workflowprocesseperson
(
    uuid uuid NOT NULL DEFAULT public.gen_random_uuid(),
    workflowprocessdefinitioneperson_id integer,
    eperson uuid,
    workflowprocessdefinition uuid,
    index integer,
    assign_date timestamp with time zone,
    workflowprocess_id uuid,
    department_id uuid,
    office_id uuid,
    usetype_id uuid,
    isowner boolean,
    issender boolean,
    initiator boolean,
    sequence integer,
    issequence boolean,
    isrefer boolean,
    responsebyallusers uuid,
    isapproved boolean,
    isdelete boolean,
    isacknowledgement boolean,
    remark character varying COLLATE pg_catalog."default",
    CONSTRAINT workflowprocesseperson_pkey PRIMARY KEY (uuid)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.workflowprocesseperson
    OWNER to dspace;

	-- Table: public.workflowprocesshistory

-- DROP TABLE IF EXISTS public.workflowprocesshistory;

CREATE TABLE IF NOT EXISTS public.workflowprocesshistory
(
    uuid uuid NOT NULL,
    workflowprocessepeople uuid,
    actiondate timestamp with time zone,
    workflowhistory_id integer,
    action uuid,
    workflowprocess_id uuid,
    comment character varying COLLATE pg_catalog."default",
    receiveddate timestamp with time zone,
    sentto uuid,
    sentbyname character varying COLLATE pg_catalog."default",
    senttoname character varying COLLATE pg_catalog."default",
    CONSTRAINT workflowprocesshistory_pkey PRIMARY KEY (uuid)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.workflowprocesshistory
    OWNER to dspace;

	-- Table: public.workflowprocessinwarddetails

-- DROP TABLE IF EXISTS public.workflowprocessinwarddetails;

CREATE TABLE IF NOT EXISTS public.workflowprocessinwarddetails
(
    uuid uuid NOT NULL,
    workflowprocessinwarddetails_id integer,
    inwardnumber character varying COLLATE pg_catalog."default",
    inwarddate timestamp with time zone,
    receiveddate timestamp with time zone,
    lettercategory uuid,
    subcategory uuid,
    category uuid,
    inwardmode uuid,
    vip uuid,
    vipname uuid,
    language uuid,
    latterdate timestamp with time zone,
    filereferencenumber character varying COLLATE pg_catalog."default",
    letterrefno character varying COLLATE pg_catalog."default",
    CONSTRAINT workflowprocessinwarddetails_pkey PRIMARY KEY (uuid)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.workflowprocessinwarddetails
    OWNER to dspace;


	-- Table: public.workflowprocessmaster

-- DROP TABLE IF EXISTS public.workflowprocessmaster;

CREATE TABLE IF NOT EXISTS public.workflowprocessmaster
(
    workflowprocessmaster_lid integer,
    uuid uuid NOT NULL,
    mastername character varying COLLATE pg_catalog."default",
    CONSTRAINT workflowprocessmaster_pkey PRIMARY KEY (uuid)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.workflowprocessmaster
    OWNER to dspace;


	CREATE TABLE IF NOT EXISTS public.workflowprocessmastervalue
(
    primaryvalue character varying COLLATE pg_catalog."default",
    secondaryvalue character varying COLLATE pg_catalog."default",
    workflowprocessmaster_id uuid,
    uuid uuid NOT NULL DEFAULT public.gen_random_uuid(),
    workflowprocessmastervalue_id integer,
    isdelete boolean,
    CONSTRAINT workflowprocessmastervalue_pkey PRIMARY KEY (uuid)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.workflowprocessmastervalue
    OWNER to dspace;

	-- DROP TABLE IF EXISTS public.workflowprocessnote;

CREATE TABLE IF NOT EXISTS public.workflowprocessnote
(
    workflowprocessnote_id integer,
    uuid uuid DEFAULT 'public.gen_random_uuid()',
    subject character varying COLLATE pg_catalog."default",
    description character varying COLLATE pg_catalog."default",
    init_date timestamp with time zone,
    submitter_id uuid,
    workflowprocess_fk uuid
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.workflowprocessnote
    OWNER to dspace;



	-- Table: public.workflowprocessoutwarddetails

-- DROP TABLE IF EXISTS public.workflowprocessoutwarddetails;

CREATE TABLE IF NOT EXISTS public.workflowprocessoutwarddetails
(
    uuid uuid NOT NULL,
    workflowprocessoutwarddetails_id integer,
    outwardnumber character varying COLLATE pg_catalog."default",
    outwarddate timestamp with time zone,
    outwardmedium_id uuid,
    outwarddepartment_id uuid,
    outwardmode_id uuid,
    serviceprovider character varying COLLATE pg_catalog."default",
    awbno character varying COLLATE pg_catalog."default",
    dispatchdate timestamp with time zone,
    subcategory uuid,
    category uuid,
    CONSTRAINT workflowprocessoutwarddetails_pkey PRIMARY KEY (uuid)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.workflowprocessoutwarddetails
    OWNER to dspace;


	-- Table: public.workflowprocessreferencedoc

-- DROP TABLE IF EXISTS public.workflowprocessreferencedoc;

CREATE TABLE IF NOT EXISTS public.workflowprocessreferencedoc
(
    uuid uuid NOT NULL DEFAULT 'public.gen_random_uuid()',
    workflowreference_id integer,
    bitstream uuid,
    workflowprocess uuid,
    documenttype uuid,
    subject character varying COLLATE pg_catalog."default",
    referencenumber character varying COLLATE pg_catalog."default",
    lattercategory uuid,
    initdate timestamp with time zone,
    editortext character varying COLLATE pg_catalog."default",
    draft_type_id uuid,
    workflowprocessnote uuid,
    description character varying COLLATE pg_catalog."default",
    workflowprocesscomment uuid,
    item_fid uuid,
    itemname character varying COLLATE pg_catalog."default",
    createdate timestamp with time zone,
    documentsignator_id uuid,
    "isSignature" boolean,
    issignature boolean,
    page integer,
    filetype character varying COLLATE pg_catalog."default",
    filenumber character varying COLLATE pg_catalog."default",
    CONSTRAINT workflowprocessreferencedoc_pkey PRIMARY KEY (uuid)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.workflowprocessreferencedoc
    OWNER to dspace;

	-- Table: public.workflowprocessreferencedocversion

-- DROP TABLE IF EXISTS public.workflowprocessreferencedocversion;

CREATE TABLE IF NOT EXISTS public.workflowprocessreferencedocversion
(
    workflowprocessreferencedocversion_id integer,
    uuid uuid NOT NULL,
    creator uuid,
    workflowprocessreferencedoc_fid uuid,
    creationdatetime timestamp with time zone,
    remark character varying COLLATE pg_catalog."default",
    versionnumber double precision,
    bitstream uuid,
    isactive boolean,
    editortext character varying COLLATE pg_catalog."default",
    issign boolean,
    CONSTRAINT workflowprocessreferencedocversion_pkey PRIMARY KEY (uuid)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.workflowprocessreferencedocversion
    OWNER to dspace;

	-- Table: public.workflowprocesssenderdiary

-- DROP TABLE IF EXISTS public.workflowprocesssenderdiary;

CREATE TABLE IF NOT EXISTS public.workflowprocesssenderdiary
(
    uuid uuid NOT NULL DEFAULT 'public.gen_random_uuid()',
    workflowprocesssenderdiary_id integer,
    sendername character varying COLLATE pg_catalog."default",
    designation character varying COLLATE pg_catalog."default",
    contactnumber character varying COLLATE pg_catalog."default",
    email character varying COLLATE pg_catalog."default",
    organization character varying COLLATE pg_catalog."default",
    address character varying COLLATE pg_catalog."default",
    city character varying COLLATE pg_catalog."default",
    country character varying COLLATE pg_catalog."default",
    state character varying COLLATE pg_catalog."default",
    pincode character varying COLLATE pg_catalog."default",
    fax character varying COLLATE pg_catalog."default",
    landline character varying COLLATE pg_catalog."default",
    workflowprocessdraftdetails uuid,
    vip uuid,
    vipname uuid,
    status integer,
    workflowprocess uuid,
    CONSTRAINT workflowprocesssenderdiary_pkey PRIMARY KEY (uuid)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.workflowprocesssenderdiary
    OWNER to dspace;

	CREATE TABLE IF NOT EXISTS public.pdfannotation
(
    pdfannotation_id integer,
    bitstream_uuid uuid,
    eperson_uuid uuid,
    uuid uuid NOT NULL,
    annotation_str character varying COLLATE pg_catalog."default",
    created_date timestamp with time zone,
    noteannotatiostr character varying COLLATE pg_catalog."default",
    item uuid,
    CONSTRAINT pdfannotation_pkey PRIMARY KEY (uuid)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.pdfannotation
    OWNER to dspace;
	ALTER TABLE IF EXISTS public.item
    ADD COLUMN cino character varying COLLATE pg_catalog."default";