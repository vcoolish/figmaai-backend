--liquibase formatted sql

--changeset yemets:20221027210742
ALTER TABLE car_nfts
  DROP IF EXISTS image,
  ADD IF NOT EXISTS image_id BIGINT REFERENCES images (id);
--rollback

--changeset yemets:20221028174326
TRUNCATE car_nfts;
ALTER TABLE car_nfts
  ALTER image_id SET NOT NULL;
--rollback

--changeset vcoolish:20221028184326
ALTER TABLE car_nfts
  ADD user_address VARCHAR(255) NOT NULL REFERENCES users(address);
--rollback ALTER TABLE car_nfts DROP COLUMN user_address;

--changeset yemets:20221102220511
CREATE SEQUENCE IF NOT EXISTS car_nfts_id_sequence START WITH 1000000 INCREMENT BY 1;

ALTER TABLE car_nfts
  ALTER id SET DEFAULT nextval('car_nfts_id_sequence');

ALTER SEQUENCE car_nfts_id_sequence OWNED BY car_nfts.id;

--rollback ALTER TABLE car_nfts
--rollback   ALTER id DROP DEFAULT;
--rollback DROP SEQUENCE IF EXISTS car_nfts_id_sequence;
