create table ROLE_PERMISSIONS (
  ROLE_NAME varchar(100) not null,
  PERMISSION_NAME varchar(100) not null,
  primary key (ROLE_NAME, PERMISSION_NAME),
  FOREIGN KEY (ROLE_NAME) REFERENCES ROLE(ROLE_NAME) ON DELETE CASCADE,
   FOREIGN KEY (PERMISSION_NAME) REFERENCES PERMISSION(PERMISSION_NAME) ON DELETE CASCADE
);