package api;

import java.io.Serializable;

public enum ResponseType implements Serializable {
    OK,
    ALREADY_EXISTING,
    NOT_AUTHORIZED,
    NOT_EXISTING
}
