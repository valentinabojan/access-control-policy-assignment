package psd.api;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "hierarchy")
@IdClass(PairKey.class)
public class RoleHierarchy implements Serializable {

    @Id
    @Column(name = "parent")
    private String parent;

    @Id @Column(name = "child")
    private String child;

    public RoleHierarchy() {
    }

    public RoleHierarchy(String parent, String child) {
        this.parent = parent;
        this.child = child;
    }

    public String getParent() {
        return parent;
    }

    public String getChild() {
        return child;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoleHierarchy that = (RoleHierarchy) o;

        if (child != null ? !child.equals(that.child) : that.child != null) return false;
        if (parent != null ? !parent.equals(that.parent) : that.parent != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = parent != null ? parent.hashCode() : 0;
        result = 31 * result + (child != null ? child.hashCode() : 0);
        return result;
    }
}
