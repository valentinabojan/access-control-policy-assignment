package psd.api;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "role")
public class Role implements Serializable {

    @Id
    @Column(name = "role_name")
    private String roleName;

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(
            name="user_role",
            joinColumns=@JoinColumn(name="role_name", referencedColumnName="role_name"),
            inverseJoinColumns=@JoinColumn(name="username", referencedColumnName="username"))
    private List<User> users;

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(
            name="role_permissions",
            joinColumns=@JoinColumn(name="role_name", referencedColumnName="role_name"),
            inverseJoinColumns=@JoinColumn(name="permission_name", referencedColumnName="permission_name"))
    private List<Permission> permissions;

    public Role() {
        this.users = new ArrayList<>();
        this.permissions = new ArrayList<>();
    }

    public Role(String roleName, String rights) {
        this();
        this.roleName = roleName;
    }

    public Role(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public List<User> getUsers() {
        return users;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Role role = (Role) o;

        if (roleName != null ? !roleName.equals(role.roleName) : role.roleName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = roleName != null ? roleName.hashCode() : 0;
        result = 31 * result + (users != null ? users.hashCode() : 0);
        return result;
    }
}
