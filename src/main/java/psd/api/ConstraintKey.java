package psd.api;

import java.io.Serializable;

public class ConstraintKey implements Serializable{

    private String roleName1;
    private String roleName2;

    public ConstraintKey() {
    }

    public ConstraintKey(String roleName1, String roleName2) {
        this.roleName1 = roleName1;
        this.roleName2 = roleName2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConstraintKey that = (ConstraintKey) o;

        if (!roleName1.equals(that.roleName1)) return false;
        if (!roleName2.equals(that.roleName2)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = roleName1.hashCode();
        result = 31 * result + roleName2.hashCode();
        return result;
    }
}
