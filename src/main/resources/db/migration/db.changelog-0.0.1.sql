--liquibase formatted sql

--changeset vcoolish:20230328123804
CREATE TABLE users
(
  figma                         VARCHAR(255),
  token                         VARCHAR(255),
  energy                        DECIMAL(12, 2)           NOT NULL,
  max_energy                    DECIMAL(12, 2)                    DEFAULT 30,
  next_energy_renew             TIMESTAMP with time zone          DEFAULT (now() AT TIME ZONE 'UTC'),
  created_at                    TIMESTAMP with time zone NOT NULL DEFAULT (now() AT TIME ZONE 'UTC'),
  id                            BIGSERIAL                NOT NULL PRIMARY KEY,
  email                         VARCHAR(255),
  password                      VARCHAR(255),
  google_id                     VARCHAR(255),
  provider                      VARCHAR(255)             NOT NULL,
  user_uuid                     VARCHAR(255) UNIQUE      NOT NULL,

  last_subscription_expire_date TIMESTAMP WITH TIME ZONE,
  userUUID                      VARCHAR(255),
  ancestor_id                   BIGINT,
  profile_type                  VARCHAR(45),
  title                         VARCHAR(45),

  enabled                       BOOLEAN                           DEFAULT FALSE,
  verified                      BOOLEAN                           DEFAULT FALSE,
  subscription_id               VARCHAR(255),
  is_subscribed                 BOOLEAN                           DEFAULT FALSE,
  last_subscription_data        TIMESTAMP WITH TIME ZONE,
  deleted                       BOOLEAN                  NOT NULL DEFAULT 'false',
  deleted_date                  TIMESTAMP WITH TIME ZONE,
  method                        TEXT                     NOT NULL,

  FOREIGN KEY (id) REFERENCES users (id),
  CONSTRAINT ancestor_id FOREIGN KEY (ancestor_id) REFERENCES users (id)
);

CREATE SEQUENCE IF NOT EXISTS users_id_seq MINVALUE 1 START WITH 1 INCREMENT BY 1;

ALTER TABLE users
  ALTER COLUMN id SET DEFAULT nextval('users_id_seq');
ALTER TABLE users
  ALTER COLUMN id SET NOT NULL;

CREATE INDEX idx_ancestor_id ON users (ancestor_id);
--rollback DROP TABLE users;
--rollback DROP SEQUENCE IF EXISTS users_id_seq;

--changeset vcoolish:20230329123804
CREATE SEQUENCE IF NOT EXISTS images_id_sequence START WITH 1000000 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS images
(
  id          BIGINT                   NOT NULL PRIMARY KEY DEFAULT nextval('images_id_sequence'),
  name        VARCHAR(255),
  description VARCHAR(512),
  image       VARCHAR(2048),
  prompt      VARCHAR(1024),
  user_id     BIGSERIAL                NOT NULL REFERENCES users (id),
  created_at  TIMESTAMP with time zone NOT NULL             DEFAULT (now() AT TIME ZONE 'UTC'),
  updated_at  TIMESTAMP with time zone
);

ALTER SEQUENCE images_id_sequence OWNED BY images.id;
--rollback DROP TABLE images;
--rollback DROP SEQUENCE IF EXISTS images_id_sequence;

--changeset vcoolish:20230329133804
CREATE SEQUENCE IF NOT EXISTS social_connection_id_seq
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;

CREATE TABLE social_connection
(
  id           BIGINT PRIMARY KEY DEFAULT nextval('social_connection_id_seq'),
  state        VARCHAR(255) NOT NULL,
  provider     VARCHAR(255) NOT NULL,
  redirect_url VARCHAR(255) NOT NULL,
  prod_api_key VARCHAR(255)
);

--rollback DROP TABLE social_connection;
--rollback DROP SEQUENCE IF EXISTS social_connection_id_seq;

--changeset vcoolish:20230329143804
CREATE SEQUENCE IF NOT EXISTS refresh_tokens_id_seq
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;

CREATE TABLE refresh_tokens
(
  id              BIGINT PRIMARY KEY DEFAULT nextval('refresh_tokens_id_seq'),
  token           VARCHAR(255),
  user_id         BIGSERIAL NOT NULL REFERENCES users (id),
  expiration_date TIMESTAMP,
  hash            VARCHAR(255),

  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
--rollback DROP TABLE refresh_tokens;
--rollback DROP SEQUENCE IF EXISTS refresh_tokens_id_seq;
