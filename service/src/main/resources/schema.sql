CREATE TABLE IF NOT EXISTS interpol_warrant (
    id                  BIGSERIAL PRIMARY KEY,
    document_number     VARCHAR(50)  NOT NULL,
    full_name           VARCHAR(200),
    date_of_birth       DATE,
    nationality         VARCHAR(3),
    reason              VARCHAR(500),
    issued_at           DATE         NOT NULL DEFAULT CURRENT_DATE,
    expires_at          DATE,
    active              BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_interpol_doc UNIQUE (document_number)
    );

CREATE INDEX IF NOT EXISTS idx_interpol_doc ON interpol_warrant(document_number);
CREATE INDEX IF NOT EXISTS idx_interpol_active ON interpol_warrant(active);


CREATE TABLE IF NOT EXISTS domestic_warrant (
    id                  BIGSERIAL PRIMARY KEY,
    document_number     VARCHAR(50)  NOT NULL,
    full_name           VARCHAR(200),
    date_of_birth       DATE,
    nationality         VARCHAR(3),
    reason              VARCHAR(500),
    issuing_authority   VARCHAR(200),
    issued_at           DATE         NOT NULL DEFAULT CURRENT_DATE,
    expires_at          DATE,
    active              BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_domestic_doc UNIQUE (document_number)
    );

CREATE INDEX IF NOT EXISTS idx_domestic_doc ON domestic_warrant(document_number);
CREATE INDEX IF NOT EXISTS idx_domestic_active ON domestic_warrant(active);


CREATE TABLE IF NOT EXISTS stolen_lost_document (
    id                  BIGSERIAL PRIMARY KEY,
    document_number     VARCHAR(50)  NOT NULL,
    document_type       VARCHAR(20)  NOT NULL CHECK (document_type IN ('PASSPORT','ID_CARD')),
    full_name           VARCHAR(200),
    reported_by         VARCHAR(200),
    reported_at         DATE         NOT NULL DEFAULT CURRENT_DATE,
    reason              VARCHAR(20)  NOT NULL CHECK (reason IN ('STOLEN','LOST')),
    active              BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_stolen_doc UNIQUE (document_number)
    );

CREATE INDEX IF NOT EXISTS idx_stolen_doc ON stolen_lost_document(document_number);
CREATE INDEX IF NOT EXISTS idx_stolen_active ON stolen_lost_document(active);
