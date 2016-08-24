package psd.api;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "user_constraint")
@IdClass(PairKey.class)
public class Constraint implements Serializable {

    @Id @Column(name = "rolename1")
    private String roleName1;

    @Id @Column(name = "rolename2")
    private String roleName2;

    public Constraint() {
    }

    public Constraint(String roleName1, String roleName2) {
        this.roleName1 = roleName1;
        this.roleName2 = roleName2;
    }

    public String getRoleName1() {
        return roleName1;
    }

    public String getRoleName2() {
        return roleName2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Constraint that = (Constraint) o;

        if (roleName1 != null ? !roleName1.equals(that.roleName1) : that.roleName1 != null) return false;
        if (roleName2 != null ? !roleName2.equals(that.roleName2) : that.roleName2 != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = roleName1 != null ? roleName1.hashCode() : 0;
        result = 31 * result + (roleName2 != null ? roleName2.hashCode() : 0);
        return result;
    }
}
