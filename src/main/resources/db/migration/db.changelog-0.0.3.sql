--liquibase formatted sql

--changeset yemets:20221027210742
ALTER TABLE car_nfts
  DROP IF EXISTS image,
  ADD IF NOT EXISTS image_id BIGINT REFERENCES images(id);
--rollback
