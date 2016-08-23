create table USER_CONSTRAINT (
  ROLENAME1 varchar(100) not null,
  ROLENAME2 varchar(100) not null,
  PRIMARY KEY (ROLENAME1, ROLENAME2)
);