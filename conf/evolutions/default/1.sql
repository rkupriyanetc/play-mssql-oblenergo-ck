# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table linkeds (
  id                            numeric(19) identity(1,1) not null,
  user_id                       numeric(19) not null,
  user_provider                 varchar(30),
  provider_key                  varchar(30),
  constraint pk_linkeds primary key (id)
);

create table tokens (
  id                            numeric(19) identity(1,1) not null,
  token                         varchar(70),
  user_id                       numeric(19) not null,
  type                          varchar(2),
  created                       datetime not null,
  expires                       datetime not null,
  constraint ck_tokens_type check (type in ('PR','EV')),
  constraint uq_tokens_token unique (token),
  constraint pk_tokens primary key (id)
);

create table users (
  id                            numeric(19) identity(1,1) not null,
  email                         varchar(70),
  name                          varchar(30),
  firstname                     varchar(30),
  lastname                      varchar(30),
  lastlogin                     datetime null,
  active                        bit default 0,
  email_validated               bit default 0,
  constraint pk_users primary key (id)
);

create table users_roles (
  user_id                       numeric(19) not null,
  role_id                       numeric(19) not null,
  constraint pk_users_roles primary key (user_id,role_id)
);

create table users_permissions (
  user_id                       numeric(19) not null,
  permission_id                 numeric(19) not null,
  constraint pk_users_permissions primary key (user_id,permission_id)
);

create table permissions (
  id                            numeric(19) identity(1,1) not null,
  value                         varchar(50),
  constraint pk_permissions primary key (id)
);

create table roles (
  id                            numeric(19) identity(1,1) not null,
  role_name                     varchar(5),
  constraint pk_roles primary key (id)
);

alter table linkeds add constraint fk_linkeds_user_id foreign key (user_id) references users (id);
create index ix_linkeds_user_id on linkeds (user_id);

alter table tokens add constraint fk_tokens_user_id foreign key (user_id) references users (id);
create index ix_tokens_user_id on tokens (user_id);

alter table users_roles add constraint fk_users_roles_users foreign key (user_id) references users (id);
create index ix_users_roles_users on users_roles (user_id);

alter table users_roles add constraint fk_users_roles_roles foreign key (role_id) references roles (id);
create index ix_users_roles_roles on users_roles (role_id);

alter table users_permissions add constraint fk_users_permissions_users foreign key (user_id) references users (id);
create index ix_users_permissions_users on users_permissions (user_id);

alter table users_permissions add constraint fk_users_permissions_permissions foreign key (permission_id) references permissions (id);
create index ix_users_permissions_permissions on users_permissions (permission_id);


# --- !Downs

IF OBJECT_ID('fk_linkeds_user_id', 'F') IS NOT NULL alter table linkeds drop constraint fk_linkeds_user_id;
drop index if exists ix_linkeds_user_id;

IF OBJECT_ID('fk_tokens_user_id', 'F') IS NOT NULL alter table tokens drop constraint fk_tokens_user_id;
drop index if exists ix_tokens_user_id;

IF OBJECT_ID('fk_users_roles_users', 'F') IS NOT NULL alter table users_roles drop constraint fk_users_roles_users;
drop index if exists ix_users_roles_users;

IF OBJECT_ID('fk_users_roles_roles', 'F') IS NOT NULL alter table users_roles drop constraint fk_users_roles_roles;
drop index if exists ix_users_roles_roles;

IF OBJECT_ID('fk_users_permissions_users', 'F') IS NOT NULL alter table users_permissions drop constraint fk_users_permissions_users;
drop index if exists ix_users_permissions_users;

IF OBJECT_ID('fk_users_permissions_permissions', 'F') IS NOT NULL alter table users_permissions drop constraint fk_users_permissions_permissions;
drop index if exists ix_users_permissions_permissions;

IF OBJECT_ID('linkeds', 'U') IS NOT NULL drop table linkeds;

IF OBJECT_ID('tokens', 'U') IS NOT NULL drop table tokens;

IF OBJECT_ID('users', 'U') IS NOT NULL drop table users;

IF OBJECT_ID('users_roles', 'U') IS NOT NULL drop table users_roles;

IF OBJECT_ID('users_permissions', 'U') IS NOT NULL drop table users_permissions;

IF OBJECT_ID('permissions', 'U') IS NOT NULL drop table permissions;

IF OBJECT_ID('roles', 'U') IS NOT NULL drop table roles;

