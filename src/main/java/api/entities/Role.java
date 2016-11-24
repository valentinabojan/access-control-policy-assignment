package api.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ROLE")
public class Role implements Serializable {

    @Id
    @Column(name = "ROLE_NAME")
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "USER_ROLE",
            joinColumns = @JoinColumn(name = "ROLE_NAME", referencedColumnName = "ROLE_NAME"),
            inverseJoinColumns = @JoinColumn(name = "USERNAME", referencedColumnName = "USERNAME"))
    private List<User> users = new ArrayList<>();

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(
            name="ROLE_PERMISSIONS",
            joinColumns=@JoinColumn(name="ROLE_NAME", referencedColumnName="ROLE_NAME"),
            inverseJoinColumns=@JoinColumn(name="PERMISSION_NAME", referencedColumnName="PERMISSION_NAME"))
    private List<Permission> permissions;

    public String getName() {
        return name;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void addPermission(Permission permission) {
        permissions.add(permission);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Role role = (Role) o;

        if (name != null ? !name.equals(role.name) : role.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (users != null ? users.hashCode() : 0);
        return result;
    }

    public static class RoleBuilder {
        private Role role;

        private RoleBuilder() {
            role = new Role();
        }

        public static RoleBuilder role() {
            return new RoleBuilder();
        }

        public RoleBuilder withName(String name) {
            role.name = name;
            return this;
        }

        public Role build() {
            return role;
        }
    }
}