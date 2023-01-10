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
CREATE TABLE IF NOT EXISTS image_nfts
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
  prompt          VARCHAR(255),
  is_minted        BOOLEAN,
  min_speed       INTEGER NOT NULL,
  max_speed       INTEGER NOT NULL,
  odometer        FLOAT   NOT NULL,
  efficiency      FLOAT   NOT NULL,
  luck            FLOAT   NOT NULL,
  comfortability  FLOAT   NOT NULL,
  resilience      FLOAT   NOT NULL,
  durability      FLOAT   NOT NULL,
  max_durability  FLOAT   NOT NULL,
  CONSTRAINT pk_image_nfts PRIMARY KEY (id, collection_id)
);
--rollback DROP TABLE image_nfts;

--changeset yemets:20220831161927
ALTER TABLE users
  ALTER distance TYPE FLOAT,
  ALTER energy TYPE FLOAT,
  ALTER tokens_to_claim SET DEFAULT 0;

ALTER TABLE users
  ADD COLUMN max_energy FLOAT DEFAULT 30,
  ALTER tokens_to_claim SET NOT NULL;

ALTER TABLE image_nfts
  ALTER efficiency TYPE SMALLINT,
  ALTER luck TYPE SMALLINT,
  ALTER comfortability TYPE SMALLINT,
  ALTER resilience TYPE SMALLINT;

ALTER TABLE image_nfts
  RENAME resilience TO economy;

-- yes, there's no rollback
--rollback

--changeset yemets:20220831203122
ALTER TABLE image_nfts
  ADD mint INTEGER NOT NULL DEFAULT 0;

--rollback ALTER TABLE image_nfts DROP mint;


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
ALTER TABLE image_nfts
  ALTER level TYPE SMALLINT;
--rollback ALTER TABLE image_nfts ALTER level TYPE INT;

--changeset vcoolish:20221017100000
ALTER TABLE users
  ADD COLUMN balance DECIMAL(30, 18) NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS balance_history
(
  id           UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
  user_address VARCHAR(255)             NOT NULL REFERENCES users,
  balance      DECIMAL(30, 18)          NOT NULL DEFAULT 0,
  tx_id        VARCHAR(255)             NOT NULL,
  created_at   TIMESTAMP with time zone NOT NULL
);
--rollback ALTER TABLE users DROP COLUMN balance;
--rollback DROP TABLE balance_history;

--changeset vcoolish:20221017180000
ALTER TABLE users
  ALTER COLUMN tokens_to_claim TYPE DECIMAL(30, 8) USING (tokens_to_claim::DECIMAL(30, 18)),
  ALTER COLUMN tokens_limit_per_day TYPE DECIMAL(30, 8) USING (tokens_limit_per_day::DECIMAL(30, 18));
--rollback ALTER TABLE users DROP COLUMN balance;

--changeset vcoolish:20221018180000
ALTER TABLE users
  ADD COLUMN sign_message VARCHAR(255) NOT NULL DEFAULT '';
--rollback ALTER TABLE users DROP COLUMN sign_message;

--changeset vcoolish:20221019100000
CREATE TABLE IF NOT EXISTS transactions
(
  id        UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
  address   VARCHAR(255)    NOT NULL REFERENCES users,
  amount    DECIMAL(30, 18) NOT NULL DEFAULT 0,
  tx_type   VARCHAR(255)    NOT NULL,
  direction VARCHAR(255)    NOT NULL
);
CREATE TABLE IF NOT EXISTS blockchain_state
(
  transaction_id       UUID NOT NULL REFERENCES transactions,
  last_processed_block VARCHAR(255) NOT NULL
);
--rollback DROP TABLE transactions;
--rollback DROP TABLE blockchain_state;

--changeset yemets:20221023171535
ALTER TABLE blockchain_state
  DROP COLUMN transaction_id,
  ADD CONSTRAINT pk_blockchain_state PRIMARY KEY (last_processed_block);

ALTER TABLE IF EXISTS transactions
  ALTER tx_type TYPE VARCHAR(20),
  ALTER direction TYPE VARCHAR(20),
  ADD block_id VARCHAR(255) NOT NULL REFERENCES blockchain_state(last_processed_block);
--rollback

--changeset vcoolish:20221028184326
ALTER TABLE image_nfts
  ADD user_address VARCHAR(255) NOT NULL REFERENCES users(address);
--rollback ALTER TABLE image_nfts DROP COLUMN user_address;

--changeset yemets:20221102220511
CREATE SEQUENCE IF NOT EXISTS image_nfts_id_sequence START WITH 1000000 INCREMENT BY 1;

ALTER TABLE image_nfts
  ALTER id SET DEFAULT nextval('image_nfts_id_sequence');

ALTER SEQUENCE image_nfts_id_sequence OWNED BY image_nfts.id;

--rollback ALTER TABLE image_nfts
--rollback   ALTER id DROP DEFAULT;
--rollback DROP SEQUENCE IF EXISTS image_nfts_id_sequence;


--changeset yemets:20230110182140
ALTER TABLE IF EXISTS image_nfts
  ADD created_at TIMESTAMP with time zone NOT NULL DEFAULT now() WITH TIME ZONE 'UTC',
  ADD updated_at TIMESTAMP with time zone;

--rollback ALTER TABLE IF EXISTS image_nfts
--rollback   DROP created_at,
--rollback   DROP updated_at;
