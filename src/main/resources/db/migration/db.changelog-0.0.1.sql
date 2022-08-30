--liquibase formatted sql

--changeset yemets:20220613114243
CREATE TABLE IF NOT EXISTS users
(
    user_address VARCHAR PRIMARY KEY,
    distance     BIGINT,
    energy       BIGINT
);
CREATE TABLE IF NOT EXISTS nfts
(
    id            INT,
    collection_id INT,
    distance      BIGINT,
    energy        BIGINT,
    PRIMARY KEY(id, collection_id)
);
--rollback DROP TABLE users;
CREATE TABLE IF NOT EXISTS nftmetadata
(
    name VARCHAR,
    description VARCHAR,
    image VARCHAR,
    external_url VARCHAR,
    attributes List,
    collection Collection
);
CREATE TABLE IF NOT EXISTS attributes
(
    trait_type  VARCHAR
    value   VARCHAR
);
