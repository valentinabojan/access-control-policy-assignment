package api.entities;

import java.io.Serializable;

public class HierarchyId implements Serializable {

    private String parent;
    private String child;

    public HierarchyId() {
    }

    public HierarchyId(String parent, String child) {
        this.parent = parent;
        this.child = child;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HierarchyId that = (HierarchyId) o;

        if (!parent.equals(that.parent)) return false;
        if (!child.equals(that.child)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = parent.hashCode();
        result = 31 * result + child.hashCode();
        return result;
    }
}
