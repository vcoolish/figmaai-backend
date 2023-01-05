--liquibase formatted sql

--changeset yemets:20221027210742
ALTER TABLE image_nfts
  DROP IF EXISTS image,
  ADD IF NOT EXISTS image_id BIGINT REFERENCES images (id);
--rollback

--changeset yemets:20221028174326
TRUNCATE image_nfts;
ALTER TABLE image_nfts
  ALTER image_id SET NOT NULL;
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
