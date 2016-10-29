package api;

import java.io.Serializable;

public enum CommandType implements Serializable {
    READ_RESOURCE,
    WRITE_RESOURCE,
    CHANGE_RIGHTS,
    CREATE_RESOURCE
}
