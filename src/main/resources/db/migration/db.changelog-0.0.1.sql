--liquibase formatted sql

--changeset yemets:20220613114243
CREATE TABLE IF NOT EXISTS users
(
  address         VARCHAR(255) PRIMARY KEY,
  distance        BIGINT NOT NULL,
  energy          BIGINT NOT NULL,
  tokens_to_claim DECIMAL
);
--rollback DROP TABLE users;


--changeset yemets:20220831123804
CREATE TABLE IF NOT EXISTS car_nfts
(
  id              BIGINT  NOT NULL,
  collection_id   BIGINT  NOT NULL,
  name            VARCHAR(255),
  description     VARCHAR(512),
  image           VARCHAR(2048),
  external_url    VARCHAR(2048),
  creator_address VARCHAR(255),
  level           INTEGER NOT NULL,
  quality         VARCHAR(255),
  body            VARCHAR(255),
  min_speed       INTEGER NOT NULL,
  max_speed       INTEGER NOT NULL,
  odometer        FLOAT   NOT NULL,
  efficiency      FLOAT   NOT NULL,
  luck            FLOAT   NOT NULL,
  comfortability  FLOAT   NOT NULL,
  resilience      FLOAT   NOT NULL,
  durability      FLOAT   NOT NULL,
  max_durability  FLOAT   NOT NULL,
  CONSTRAINT pk_car_nfts PRIMARY KEY (id, collection_id)
);
--rollback DROP TABLE car_nfts;
