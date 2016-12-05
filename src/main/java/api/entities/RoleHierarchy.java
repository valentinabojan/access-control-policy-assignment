package api.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "HIERARCHY")
@IdClass(HierarchyId.class)
public class RoleHierarchy implements Serializable {

    @Id
    @Column(name = "PARENT")
    private String parent;

    @Id
    @Column(name = "CHILD")
    private String child;

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

    public static class RoleHierarchyBuilder {
        private RoleHierarchy roleHierarchy;

        private RoleHierarchyBuilder() {
            roleHierarchy = new RoleHierarchy();
        }

        public RoleHierarchyBuilder withParent(String parent) {
            roleHierarchy.parent = parent;
            return this;
        }

        public RoleHierarchyBuilder withChild(String child) {
            roleHierarchy.child = child;
            return this;
        }

        public static RoleHierarchyBuilder roleHierarchy() {
            return new RoleHierarchyBuilder();
        }

        public RoleHierarchy build() {
            return roleHierarchy;
        }
    }
}
