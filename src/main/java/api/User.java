package api;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
public class User implements Serializable {

    @Id
    private String username;

    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "username", referencedColumnName = "username"),
            inverseJoinColumns = @JoinColumn(name = "role_name", referencedColumnName = "role_name"))
    private List<Role> roles = new ArrayList<>();

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
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

        User user = (User) o;

        if (password != null ? !password.equals(user.password) : user.password != null) return false;
        if (username != null ? !username.equals(user.username) : user.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (roles != null ? roles.hashCode() : 0);
        return result;
    }

    public static class UserBuilder {
        private User user;

        private UserBuilder() {
            user = new User();
        }

        public static UserBuilder user() {
            return new UserBuilder();
        }

        public UserBuilder withUsername(String username) {
            user.username = username;
            return this;
        }

        public UserBuilder withPassword(String password) {
            user.password = password;
            return this;
        }

        public User build() {
            return user;
        }
    }
}
