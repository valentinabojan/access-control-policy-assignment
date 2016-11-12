package api;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "role")
public class Role implements Serializable {

    @Id
    @Column(name = "role_name")
    private String name;
    private String rights;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "role_name", referencedColumnName = "role_name"),
            inverseJoinColumns = @JoinColumn(name = "username", referencedColumnName = "username"))
    private List<User> users = new ArrayList<>();

    public String getName() {
        return name;
    }

    public String getRights() {
        return rights;
    }

    public void setRights(String rights) {
        this.rights = rights;
    }

    public List<User> getUsers() {
        return users;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Role role = (Role) o;

        if (rights != null ? !rights.equals(role.rights) : role.rights != null) return false;
        if (name != null ? !name.equals(role.name) : role.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (rights != null ? rights.hashCode() : 0);
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

        public RoleBuilder withRights(String rights) {
            role.rights = rights;
            return this;
        }

        public Role build() {
            return role;
        }
    }
}
