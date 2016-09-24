/* Створення таблиць в БД CSM */

create table linkeds (
id                            bigint identity( 1, 1 ) not null,
user_id                       bigint not null,
user_provider                 varchar( 30 ),
provider_key                  varchar( 30 ),
constraint pk_linkeds primary key ( id )
) go;

create table tokens (
id                            bigint identity( 1, 1 ) not null,
token                         varchar( 70 ),
user_id                       bigint not null,
type                          varchar( 2 ),
created                       datetime not null,
expires                       datetime not null,
constraint ck_tokens_type check ( type in ( 'PR', 'EV' ) ),
constraint uq_tokens_token unique ( token ),
constraint pk_tokens primary key ( id )
) go;

create table users (
id                            bigint identity( 1, 1 ) not null,
email                         varchar( 70 ),
name                          varchar( 30 ),
firstname                     varchar( 30 ),
lastname                      varchar( 30 ),
lastlogin                     datetime null,
active                        bit default 0,
email_validated               bit default 0,
constraint pk_users primary key ( id )
) go;

create table users_roles (
user_id                       bigint not null,
role_id                       bigint not null,
constraint pk_users_roles primary key ( user_id, role_id )
) go;

create table users_permissions (
user_id                       bigint not null,
permission_id                 bigint not null,
constraint pk_users_permissions primary key ( user_id, permission_id )
) go;

create table permissions (
id                            bigint identity( 1, 1 ) not null,
value                         varchar( 50 ),
constraint pk_permissions primary key ( id )
) go;

create table roles (
id                            bigint identity( 1, 1 ) not null,
role_name                     varchar( 5 ),
constraint pk_roles primary key ( id )
) go;

alter table linkeds add constraint fk_linkeds_user_id foreign key ( user_id ) references users ( id ) go;
create index ix_linkeds_user_id on linkeds ( user_id ) go;

alter table tokens add constraint fk_tokens_user_id foreign key ( user_id ) references users ( id ) go;
create index ix_tokens_user_id on tokens ( user_id ) go;

alter table users_roles add constraint fk_users_roles_users foreign key ( user_id ) references users ( id ) go;
create index ix_users_roles_users on users_roles ( user_id ) go;

alter table users_roles add constraint fk_users_roles_roles foreign key ( role_id ) references roles ( id ) go;
create index ix_users_roles_roles on users_roles ( role_id ) go;

alter table users_permissions add constraint fk_users_permissions_users foreign key ( user_id ) references users ( id ) go;
create index ix_users_permissions_users on users_permissions ( user_id ) go;

alter table users_permissions add constraint fk_users_permissions_permissions foreign key ( permission_id ) references permissions ( id ) go;
create index ix_users_permissions_permissions on users_permissions ( permission_id ) go;