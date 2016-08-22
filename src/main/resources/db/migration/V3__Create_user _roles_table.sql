create table USER_ROLE (
  USERNAME varchar(100) not null,
  ROLE_NAME varchar(100) not null,
  primary key (USERNAME, ROLE_NAME)
);