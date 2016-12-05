package api;

import java.io.Serializable;

public enum ResponseType implements Serializable {

    OK,
    ALREADY_EXISTING,
    NOT_AUTHORIZED,
    INVALID,
    FORBIDDEN,
    NOT_EXISTING,
    LOGIN_FAILED
}