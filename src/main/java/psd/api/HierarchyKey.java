package psd.api;

import java.io.Serializable;

public class HierarchyKey implements Serializable {

    private String parent;
    private String child;

    public HierarchyKey() {
    }

    public HierarchyKey(String parent, String child) {
        this.parent = parent;
        this.child = child;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HierarchyKey that = (HierarchyKey) o;

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
