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

--changeset vcoolish:20230329153804
ALTER TABLE refresh_tokens
  ALTER COLUMN token TYPE VARCHAR(500);
--rollback ALTER TABLE refresh_tokens;


--changeset vcoolish:20230411143804
CREATE SEQUENCE IF NOT EXISTS oauth_tokens_id_seq
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;

CREATE TABLE oauth_tokens
(
  id          BIGINT PRIMARY KEY DEFAULT nextval('oauth_tokens_id_seq'),
  read_token  VARCHAR(500),
  write_token VARCHAR(500),
  figma       VARCHAR(500)
);
--rollback DROP TABLE oauth_tokens;
--rollback DROP SEQUENCE IF EXISTS oauth_tokens_id_seq;

--changeset vcoolish:20230412143804
CREATE TABLE credentials
(
  id BIGSERIAL PRIMARY KEY NOT NULL,
  active_profile character varying(50) COLLATE pg_catalog."default",
  active_social_networks VARCHAR(100) NOT NULL DEFAULT 'GOOGLE',
  google_client_id       VARCHAR(500) NULL DEFAULT NULL,
  google_client_secret   VARCHAR(500) NULL DEFAULT NULL,
  google_scope           VARCHAR(300) NULL DEFAULT NULL
);
--rollback DROP TABLE credentials;

--changeset vcoolish:20230414143804
INSERT INTO credentials (active_profile)
VALUES ('dev'),
       ('prod');

UPDATE credentials
SET (
     google_client_id,
     google_client_secret,
     google_scope
      ) = (
           'fHr5+08ZeHFdWT+66ysGJ432kbUF3kyvAhw2gjY3cblP6BTMEjLyPx9nso+nVrb/j3btMVbPSHZq3IKYwb9Lp03RxzPyZbddWC+oJaJIYDbqDSc7xB18YkYS2XI84nilRwptRZw6MvdZZLa9MVcZ4KexHg4JA6v4OI3EUWF7Jmgdicz4WdjmYhjjfceojfTzR6bjHMhNdzoNstdLgmqxf8ZOCv/iQcPKC9cY2i0MCE6JnyRQ6RVBMaXwzYEZ0NZDgx1/8C2oPKsDCiGi6LHK6qnRR1RTar4d536zMQT9gKUHWdOCX/zk242bcMlR+QR62R+ah1eifJ2s/QjcqSRzRQ==',
           'Dc6FROZEU3d9J8HAodrwoJBen4y/wGJ5ZDVAHAI4IqL7tLswuiL7qJaAAas4TWWG9B9LuukyVUHhq1csypUpu2u1kP1IV9eZbcIx8NJ0iZ9P8dBsQUg/HmynXgdLhm80trUr4NU2/csaL56bL1/cNxlGcJboBIUrd3NT3SlxTqz3X94cadSJenh5u8wbkQlVlw9pR82XhzyAPtGegoD9wQyJqx/mk6krfKK7y5w1/+Lyth8wActKXRnAzYCryc4bOuzyvttImzXEs3pi725RsakJLeikV6An3oCgCSyQcucG/fxy5ioXWwOq/OI5h0ZX9qV0ZnKs/LbjHjabnVigOA==',
           'email profile https://www.googleapis.com/auth/user.birthday.read https://www.googleapis.com/auth/user.gender.read'
  );
--rollback ALTER TABLE credentials;

--changeset vcoolish:20230415143804
CREATE TABLE "userconnection"
(
  userId         VARCHAR(255)  NOT NULL,
  providerId     VARCHAR(255)  NOT NULL,
  providerUserId VARCHAR(255)  NOT NULL,
  "rank"         BIGINT        NOT NULL,
  displayName    VARCHAR(255)  NULL DEFAULT NULL,
  profileUrl     VARCHAR(512)  NULL DEFAULT NULL,
  imageUrl       VARCHAR(512)  NULL DEFAULT NULL,
  accessToken    VARCHAR(1024) NOT NULL,
  secret         VARCHAR(255)  NULL DEFAULT NULL,
  refreshToken   VARCHAR(255)  NULL DEFAULT NULL,
  expireTime     BIGINT        NULL DEFAULT NULL,
  PRIMARY KEY (userId, providerId, providerUserId),
  UNIQUE (userId, providerId, "rank")
);
--rollback CREATE TABLE userconnection;

--changeset vcoolish:20230427143804
ALTER TABLE oauth_tokens
  ADD logged_in BOOLEAN DEFAULT FALSE;
--rollback DROP TABLE oauth_tokens;

--changeset vcoolish:20230527143804
ALTER TABLE users
  ADD COLUMN subscription_provider VARCHAR(255) NOT NULL DEFAULT 'paypal';
--rollback ALTER TABLE users;

--changeset vcoolish:20230528143804
ALTER TABLE users
  ADD COLUMN generations BIGINT NOT NULL DEFAULT 0,
  ADD COLUMN next_subscription_validation TIMESTAMP WITH TIME ZONE;
--rollback ALTER TABLE users;

--changeset vcoolish:20230529143804
ALTER TABLE users
  ALTER COLUMN subscription_provider DROP NOT NULL;
--rollback ALTER TABLE users;

--changeset vcoolish:20230530143804
ALTER TABLE users
  ADD COLUMN max_generations BIGINT NOT NULL DEFAULT 0;
--rollback ALTER TABLE users;

--changeset vcoolish:20230630143804
ALTER TABLE users
  ADD COLUMN credits BIGINT NOT NULL DEFAULT 0,
  ADD COLUMN max_credits BIGINT NOT NULL DEFAULT 0;
--rollback ALTER TABLE users;

--changeset vcoolish:20230701143804
ALTER TABLE users
  ADD COLUMN ux_credits BIGINT NOT NULL DEFAULT 0;
--rollback ALTER TABLE users;

--changeset vcoolish:20230702143804
ALTER TABLE images
  ADD COLUMN gif VARCHAR(2048);
--rollback ALTER TABLE images;

--changeset vcoolish:20230703143804
CREATE TABLE subscriptions
(
  subscription_id BIGSERIAL PRIMARY KEY NOT NULL,
  user_id BIGINT NOT NULL,
  status VARCHAR(255) NOT NULL,
  provider VARCHAR(255) NOT NULL,
  subscription_name VARCHAR(255) NOT NULL,
  generations BIGINT NOT NULL,
  tokens BIGINT NOT NULL,
  renews_at TIMESTAMP WITH TIME ZONE,
  ends_at TIMESTAMP WITH TIME ZONE,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  trial_ends_at TIMESTAMP WITH TIME ZONE,
  order_id VARCHAR(255) NOT NULL,
  variant_id VARCHAR(255) NOT NULL,
  update_payment_method_url VARCHAR(2048) NOT NULL
);
--rollback CREATE TABLE subscriptions;

--changeset vcoolish:20230704143804
DROP TABLE subscriptions;
CREATE TABLE subscriptions
(
  subscription_id VARCHAR(255) PRIMARY KEY NOT NULL,
  user_id         BIGSERIAL NOT NULL REFERENCES users (id),
  status                    VARCHAR(255)             NOT NULL,
  provider                  VARCHAR(255)             NOT NULL,
  subscription_name         VARCHAR(255)             NOT NULL,
  generations               BIGINT                   NOT NULL,
  tokens                    BIGINT                   NOT NULL,
  renews_at                 TIMESTAMP WITH TIME ZONE,
  ends_at                   TIMESTAMP WITH TIME ZONE,
  created_at                TIMESTAMP WITH TIME ZONE NOT NULL,
  trial_ends_at             TIMESTAMP WITH TIME ZONE,
  order_id                  VARCHAR(255)             NOT NULL,
  variant_id                VARCHAR(255)             NOT NULL,
  update_payment_method_url VARCHAR(2048)            NOT NULL
);
--rollback CREATE TABLE subscriptions;

--changeset vcoolish:20230804143804
ALTER TABLE users
  ADD COLUMN animations BIGINT NOT NULL DEFAULT 0,
  ADD COLUMN max_animations BIGINT NOT NULL DEFAULT 0;
--rollback ALTER TABLE users;

--changeset vcoolish:20230904143804
CREATE SEQUENCE IF NOT EXISTS recovery_tokens_id_seq
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;

CREATE TABLE recovery_tokens
(
  id          BIGINT PRIMARY KEY DEFAULT nextval('recovery_tokens_id_seq'),
  write_token VARCHAR(500),
  email       VARCHAR(500),
  redeemed    BOOLEAN            DEFAULT FALSE
);
--rollback CREATE TABLE recovery_tokens;

--changeset vcoolish:20230905143804
ALTER TABLE images
  ADD COLUMN video VARCHAR(2048);
--rollback ALTER TABLE images;