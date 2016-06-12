/**
 * Created by valen_000 on 6/12/2016.
 */
public enum FileType {
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
