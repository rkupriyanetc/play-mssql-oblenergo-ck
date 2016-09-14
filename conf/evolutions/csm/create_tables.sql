/* Створення таблиць в БД CSM */

if exists ( select so.name from sysobjects so
     where so.name = 'users_security_role' and so.xtype = 'U' )
  drop table users_security_role
go;

create table users_security_role (
  users_id                      bigint not null,
  security_role_id              bigint not null,
  constraint pk_users_security_role primary key ( users_id, security_role_id )
) go;

if exists ( select so.name from sysobjects so
     where so.name = 'users_user_permission' and so.xtype = 'U' )
  drop table users_user_permission
go;

create table users_user_permission (
  users_id                      bigint not null,
  user_permission_id            bigint not null,
  constraint pk_users_user_permission primary key ( users_id, user_permission_id )
) go;

if exists ( select so.name from sysobjects so
     where so.name = 'user_permission' and so.xtype = 'U' )
  drop table user_permission
go;

create table user_permission (
  id                            bigint identity( 1, 1 ) not null,
  value                         varchar( 100 ),
  constraint pk_user_permission primary key ( id )
) go;

if exists ( select so.name from sysobjects so
     where so.name = 'security_role' and so.xtype = 'U' )
  drop table security_role
go;

create table security_role (
  id                            bigint identity( 1, 1 ) not null,
  role_name                     varchar( 5 ),
  constraint pk_security_role primary key ( id )
) go;

if exists ( select so.name from sysobjects so
     where so.name = 'token_action' and so.xtype = 'U' )
  drop table token_action
go;

create table token_action (
  id                            bigint identity( 1, 1 ) not null,
  token                         varchar( 50 ),
  target_user_id                bigint,
  type                          varchar( 2 ),
  created                       datetime,
  expires                       datetime,
  constraint ck_token_action_type check ( type in ( 'PR', 'EV' ) ),
  constraint uq_token_action_token unique ( token ),
  constraint pk_token_action primary key ( id )
) go;

if exists ( select so.name from sysobjects so
     where so.name = 'linked_account' and so.xtype = 'U' )
  drop table linked_account
go;

create table linked_account (
  id                            bigint identity( 1, 1 ) not null,
  user_id                       bigint,
  provider_user_id              varchar( 100 ),
  provider_key                  varchar( 100 ),
  constraint pk_linked_account primary key ( id )
) go;

if exists ( select so.name from sysobjects so
     where so.name = 'users' and so.xtype = 'U' )
  drop table users
go;

create table users (
  id                            bigint identity( 1, 1 ) not null,
  email                         varchar( 70 ),
  name                          varchar( 20 ),
  first_name                    varchar( 25 ),
  last_name                     varchar( 25 ),
  last_login                    datetime,
  active                        bit default 0,
  email_validated               bit default 0,
  constraint pk_users primary key ( id )
) go;

/* Створення індексів таблиць в БД CSM */

alter table linked_account add constraint fk_linked_account_user_id foreign key ( user_id ) references users ( id );
create index ix_linked_account_user_id on linked_account ( user_id ) go;

alter table token_action add constraint fk_token_action_target_user_id foreign key ( target_user_id ) references users ( id );
create index ix_token_action_target_user_id on token_action ( target_user_id ) go;

alter table users_security_role add constraint fk_users_security_role_users foreign key ( users_id ) references users ( id );
create index ix_users_security_role_users on users_security_role ( users_id ) go;

alter table users_security_role add constraint fk_users_security_role_security_role foreign key ( security_role_id ) references security_role ( id );
create index ix_users_security_role_security_role on users_security_role ( security_role_id ) go;

alter table users_user_permission add constraint fk_users_user_permission_users foreign key ( users_id ) references users ( id );
create index ix_users_user_permission_users on users_user_permission ( users_id ) go;

alter table users_user_permission add constraint fk_users_user_permission_user_permission foreign key ( user_permission_id ) references user_permission ( id );
create index ix_users_user_permission_user_permission on users_user_permission ( user_permission_id ) go;
