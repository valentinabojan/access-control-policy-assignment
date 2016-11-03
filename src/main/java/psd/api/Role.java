package psd.api;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "role")
public class Role implements Serializable {

    @Id
    @Column(name = "role_name")
    private String roleName;
    private String rights;

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(
            name="user_role",
            joinColumns=@JoinColumn(name="role_name", referencedColumnName="role_name"),
            inverseJoinColumns=@JoinColumn(name="username", referencedColumnName="username"))
    private List<User> users;

    public Role() {
        this.users = new ArrayList<>();
    }

    public Role(String roleName, String rights) {
        this(roleName);
        this.rights = rights;
    }

    public Role(String roleName) {
        this();
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
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
        if (roleName != null ? !roleName.equals(role.roleName) : role.roleName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = roleName != null ? roleName.hashCode() : 0;
        result = 31 * result + (rights != null ? rights.hashCode() : 0);
        result = 31 * result + (users != null ? users.hashCode() : 0);
        return result;
    }
}
