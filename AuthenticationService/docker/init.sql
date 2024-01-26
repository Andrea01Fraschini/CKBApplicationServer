CREATE DATABASE IF NOT EXISTS AUTH;
USE AUTH;

CREATE TABLE keyValue(
    key_ VARCHAR(255) not null,
    value_ VARCHAR(255) not null,
    PRIMARY KEY(key_)
);

