package api;

import java.io.Serializable;

public enum CommandType implements Serializable {
    READ_RESOURCE,
    WRITE_RESOURCE,
    ASSIGN_PERMISSION,
    CREATE_RESOURCE,
    CREATE_ROLE,
    CREATE_USER,
    CHANGE_RIGHTS,
    CREATE_PERMISSION,
    ADD_PERMISSION_TO_ROLE,
    REVOKE_ROLE,
    CREATE_CONSTRAINT,
    CREATE_HIERARCHY,
    ASSIGN_ROLE
}