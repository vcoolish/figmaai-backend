--liquibase formatted sql

--changeset yemets:20220613114243
CREATE TABLE IF NOT EXISTS users
(
    user_address UUID PRIMARY KEY,
    distance     BIGINT,
    energy       BIGINT
);
--rollback DROP TABLE users;
