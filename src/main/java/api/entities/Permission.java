package api.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "PERMISSION")
public class Permission implements Serializable {

    @Id
    @Column(name = "PERMISSION_NAME")
    private String permissionName;
    private String rights;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "ROLE_PERMISSIONS",
            joinColumns = @JoinColumn(name = "PERMISSION_NAME", referencedColumnName = "PERMISSION_NAME"),
            inverseJoinColumns = @JoinColumn(name = "ROLE_NAME", referencedColumnName = "ROLE_NAME"))
    private List<Role> roles = new ArrayList<>();

    public String getPermissionName() {
        return permissionName;
    }

    public String getRights() {
        return rights;
    }

    public void setRights(String rights) {
        this.rights = rights;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return Objects.equals(permissionName, that.permissionName) &&
                Objects.equals(rights, that.rights) &&
                Objects.equals(roles, that.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(permissionName, rights, roles);
    }

    public static class PermissionBuilder {
        private Permission permission;

        private PermissionBuilder() {
            permission = new Permission();
        }

        public static PermissionBuilder permission() {
            return new PermissionBuilder();
        }

        public PermissionBuilder withPermissionName(String permissionName) {
            permission.permissionName = permissionName;
            return this;
        }

        public PermissionBuilder withRights(String rights) {
            permission.rights = rights;
            return this;
        }

        public PermissionBuilder withRoles(List<Role> roles) {
            permission.roles = roles;
            return this;
        }

        public Permission build() {
            return permission;
        }
    }
}