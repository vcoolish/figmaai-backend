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

--changeset yemets:20220831161927
ALTER TABLE users
  ALTER distance TYPE FLOAT,
  ALTER energy TYPE FLOAT,
  ALTER tokens_to_claim SET DEFAULT 0;

ALTER TABLE users
  ADD COLUMN max_energy FLOAT DEFAULT 30,
  ALTER tokens_to_claim SET NOT NULL;

ALTER TABLE car_nfts
  ALTER efficiency TYPE SMALLINT,
  ALTER luck TYPE SMALLINT,
  ALTER comfortability TYPE SMALLINT,
  ALTER resilience TYPE SMALLINT;

ALTER TABLE car_nfts
  RENAME resilience TO economy;

-- yes, there's no rollback
--rollback

--changeset yemets:20220831203122
ALTER TABLE car_nfts
  ADD mint INTEGER NOT NULL DEFAULT 0;

--rollback ALTER TABLE car_nfts DROP mint;


--changeset yemets:20220904163107
ALTER TABLE users
  ADD tokens_limit_per_day DECIMAL NOT NULL DEFAULT 10;

CREATE TABLE IF NOT EXISTS user_earned_token_records
(
  address      VARCHAR(255)             NOT NULL,
  created_at   TIMESTAMP with time zone NOT NULL,
  token_amount DECIMAL                  NOT NULL DEFAULT 0,
  CONSTRAINT pk_user_earned_token_records PRIMARY KEY (address, created_at)
);
--rollback ALTER TABLE users DROP tokens_limit_per_day;
--rollback DROP TABLE user_earned_token_records;


--changeset yemets:20220911164814
ALTER TABLE users
  ADD next_energy_renew TIMESTAMP with time zone;

UPDATE users
SET next_energy_renew = now() AT TIME ZONE 'UTC'
WHERE energy <> max_energy;
--rollback ALTER TABLE users DROP next_energy_renew;


--changeset yemets:20220919213522
ALTER TABLE user_earned_token_records
  ALTER token_amount TYPE DECIMAL(30, 18);

ALTER TABLE users
  ALTER COLUMN distance TYPE DECIMAL(12, 2) USING (distance::DECIMAL(12, 2)),
  ALTER COLUMN tokens_limit_per_day TYPE DECIMAL(30, 18) USING (tokens_limit_per_day::DECIMAL(30, 18)),
  ALTER COLUMN tokens_to_claim TYPE DECIMAL(30, 18) USING (tokens_to_claim::DECIMAL(30, 18)),
  ALTER COLUMN max_energy TYPE DECIMAL(12, 2) USING (max_energy::DECIMAL(12, 2)),
  ALTER COLUMN energy TYPE DECIMAL(12, 2) USING (energy::DECIMAL(12, 2));

ALTER TABLE users
  ALTER COLUMN distance SET DEFAULT 0;
--rollback

--changeset yemets:20220928150421
ALTER TABLE users
  ADD COLUMN donation SMALLINT NOT NULL DEFAULT 5;
--rollback ALTER TABLE users DROP COLUMN donation;


--changeset yemets:20220928152606
ALTER TABLE car_nfts
  ALTER level TYPE SMALLINT;
--rollback ALTER TABLE car_nfts ALTER level TYPE INT;

--changeset vcoolish:20221017100000
ALTER TABLE users
  ADD COLUMN balance DECIMAL(30, 18);
--rollback ALTER TABLE users DROP COLUMN balance;

--changeset vcoolish:20221017110000
ALTER TABLE users
  DROP COLUMN balance,
  ALTER COLUMN balance SET DEFAULT 0,
  ALTER COLUMN balance SET NOT NULL;
--rollback ALTER TABLE users DROP COLUMN balance;