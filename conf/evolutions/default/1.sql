# --- !Ups

create table "COMPETITOR" (
  "ID" BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,
  "NAME" VARCHAR NOT NULL,
  "URL" VARCHAR NOT NULL);

create table "REVIEW" (
  "ID" BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,
  "COMPETITOR_ID" BIGINT NOT NULL,
  "AUTHOR" VARCHAR NOT NULL,
  "TEXT" VARCHAR NOT NULL,
  "DATE" DATE NOT NULL,
  FOREIGN KEY (COMPETITOR_ID) REFERENCES COMPETITOR(ID));

# --- !Downs
drop table "REVIEWS";
drop table "COMPETITOR";

