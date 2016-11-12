package api;

import java.io.Serializable;

public enum CommandType implements Serializable {
    READ_RESOURCE,
    WRITE_RESOURCE,
    ADD_RIGHTS,
    CREATE_RESOURCE,
    CREATE_ROLE,
    CREATE_USER,
    CHANGE_RIGHTS,
    ASSIGN_ROLE
}
