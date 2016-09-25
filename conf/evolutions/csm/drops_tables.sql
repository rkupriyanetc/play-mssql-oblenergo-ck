/* Знищення таблиць в БД CSM */

if object_id( 'fk_linkeds_user_id', 'F' ) is not null
  alter table linkeds drop constraint fk_linkeds_user_id go;
drop index if exists ix_linkeds_user_id go;

if object_id( 'fk_tokens_user_id', 'F' ) is not null
  alter table tokens drop constraint fk_tokens_user_id go;
drop index if exists ix_tokens_user_id go;

if object_id( 'fk_users_roles_users', 'F' ) is not null
  alter table users_roles drop constraint fk_users_roles_users go;
drop index if exists ix_users_roles_users go;

if object_id( 'fk_users_roles_roles', 'F' ) is not null
  alter table users_roles drop constraint fk_users_roles_roles go;
drop index if exists ix_users_roles_roles go;

if object_id( 'fk_users_permissions_users', 'F' ) is not null
  alter table users_permissions drop constraint fk_users_permissions_users go;
drop index if exists ix_users_permissions_users go;

if object_id( 'fk_users_permissions_permissions', 'F' ) is not null
  alter table users_permissions drop constraint fk_users_permissions_permissions go;
drop index if exists ix_users_permissions_permissions go;

if object_id( 'linkeds', 'U' ) is not null
  drop table linkeds go;

if object_id( 'tokens', 'U' ) is not null
  drop table tokens go;

if object_id( 'users', 'U' ) is not null
  drop table users go;

if object_id( 'users_roles', 'U' ) is not null
  drop table users_roles go;

if object_id( 'users_permissions', 'U' ) is not null
  drop table users_permissions go;

if object_id( 'permissions', 'U' ) is not null
  drop table permissions go;

if object_id( 'roles', 'U' ) is not null
  drop table roles go;