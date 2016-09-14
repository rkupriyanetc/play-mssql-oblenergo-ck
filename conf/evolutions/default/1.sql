# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table linked_account (
  id                            numeric(19) identity(1,1) not null,
  user_id                       numeric(19),
  provider_user_id              varchar(255),
  provider_key                  varchar(255),
  constraint pk_linked_account primary key (id)
);

create table security_role (
  id                            numeric(19) identity(1,1) not null,
  role_name                     varchar(255),
  constraint pk_security_role primary key (id)
);

create table token_action (
  id                            numeric(19) identity(1,1) not null,
  token                         varchar(255),
  target_user_id                numeric(19),
  type                          varchar(2),
  created                       datetime2,
  expires                       datetime2,
  constraint ck_token_action_type check (type in ('PR','EV')),
  constraint uq_token_action_token unique (token),
  constraint pk_token_action primary key (id)
);

create table users (
  id                            numeric(19) identity(1,1) not null,
  email                         varchar(255),
  name                          varchar(255),
  first_name                    varchar(255),
  last_name                     varchar(255),
  last_login                    datetime2,
  active                        bit default 0,
  email_validated               bit default 0,
  constraint pk_users primary key (id)
);

create table users_security_role (
  users_id                      numeric(19) not null,
  security_role_id              numeric(19) not null,
  constraint pk_users_security_role primary key (users_id,security_role_id)
);

create table users_user_permission (
  users_id                      numeric(19) not null,
  user_permission_id            numeric(19) not null,
  constraint pk_users_user_permission primary key (users_id,user_permission_id)
);

create table user_permission (
  id                            numeric(19) identity(1,1) not null,
  value                         varchar(255),
  constraint pk_user_permission primary key (id)
);

alter table linked_account add constraint fk_linked_account_user_id foreign key (user_id) references users (id);
create index ix_linked_account_user_id on linked_account (user_id);

alter table token_action add constraint fk_token_action_target_user_id foreign key (target_user_id) references users (id);
create index ix_token_action_target_user_id on token_action (target_user_id);

alter table users_security_role add constraint fk_users_security_role_users foreign key (users_id) references users (id);
create index ix_users_security_role_users on users_security_role (users_id);

alter table users_security_role add constraint fk_users_security_role_security_role foreign key (security_role_id) references security_role (id);
create index ix_users_security_role_security_role on users_security_role (security_role_id);

alter table users_user_permission add constraint fk_users_user_permission_users foreign key (users_id) references users (id);
create index ix_users_user_permission_users on users_user_permission (users_id);

alter table users_user_permission add constraint fk_users_user_permission_user_permission foreign key (user_permission_id) references user_permission (id);
create index ix_users_user_permission_user_permission on users_user_permission (user_permission_id);


# --- !Downs

IF OBJECT_ID('fk_linked_account_user_id', 'F') IS NOT NULL alter table linked_account drop constraint fk_linked_account_user_id;
drop index if exists ix_linked_account_user_id;

IF OBJECT_ID('fk_token_action_target_user_id', 'F') IS NOT NULL alter table token_action drop constraint fk_token_action_target_user_id;
drop index if exists ix_token_action_target_user_id;

IF OBJECT_ID('fk_users_security_role_users', 'F') IS NOT NULL alter table users_security_role drop constraint fk_users_security_role_users;
drop index if exists ix_users_security_role_users;

IF OBJECT_ID('fk_users_security_role_security_role', 'F') IS NOT NULL alter table users_security_role drop constraint fk_users_security_role_security_role;
drop index if exists ix_users_security_role_security_role;

IF OBJECT_ID('fk_users_user_permission_users', 'F') IS NOT NULL alter table users_user_permission drop constraint fk_users_user_permission_users;
drop index if exists ix_users_user_permission_users;

IF OBJECT_ID('fk_users_user_permission_user_permission', 'F') IS NOT NULL alter table users_user_permission drop constraint fk_users_user_permission_user_permission;
drop index if exists ix_users_user_permission_user_permission;

IF OBJECT_ID('linked_account', 'U') IS NOT NULL drop table linked_account;

IF OBJECT_ID('security_role', 'U') IS NOT NULL drop table security_role;

IF OBJECT_ID('token_action', 'U') IS NOT NULL drop table token_action;

IF OBJECT_ID('users', 'U') IS NOT NULL drop table users;

IF OBJECT_ID('users_security_role', 'U') IS NOT NULL drop table users_security_role;

IF OBJECT_ID('users_user_permission', 'U') IS NOT NULL drop table users_user_permission;

IF OBJECT_ID('user_permission', 'U') IS NOT NULL drop table user_permission;

