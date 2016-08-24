create table HIERARCHY (
  PARENT varchar(100) not null,
  CHILD varchar(100) not null,
  PRIMARY KEY (PARENT, CHILD)
);