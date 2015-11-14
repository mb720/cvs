# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "MESSAGE" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"content" VARCHAR(254) NOT NULL);

# --- !Downs

drop table "MESSAGE";
