package psd.api;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "permission")
public class Permission implements Serializable {

    @Id
    @Column(name = "permission_name")
    private String permissionName;
    private String rights;

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(
            name="role_permissions",
            joinColumns=@JoinColumn(name="permission_name", referencedColumnName="permission_name"),
            inverseJoinColumns=@JoinColumn(name="role_name", referencedColumnName="role_name"))
    private List<Role> roles;

    public Permission() {
        this.roles = new ArrayList<>();
    }

    public Permission(String permissionName, String rights) {
        this();
        this.permissionName = permissionName;
        this.rights = rights;
    }

    public Permission(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public String getRights() {
        return rights;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public void setRights(String rights) {
        this.rights = rights;
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
}
