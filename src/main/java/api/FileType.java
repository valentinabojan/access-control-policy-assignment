package api;

import java.io.Serializable;

public enum FileType implements Serializable {

    DIRECTORY, FILE;

    public static FileType fromInteger(int numVal) {
        switch (numVal) {
            case 0:
                return DIRECTORY;
            case 1:
                return FILE;
            default:
                return null;
        }
    }
}