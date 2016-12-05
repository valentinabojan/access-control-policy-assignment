create table USER (
  USERNAME varchar(100) not null primary key,
  PASSWORD varchar(100) not null
);

insert into USER values("alice", "alice");
insert into USER values("bob", "bob");