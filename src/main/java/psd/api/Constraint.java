package psd.api;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "user_constraint")
@IdClass(ConstraintId.class)
public class Constraint implements Serializable {

    @Id @Column(name = "rolename1")
    private String roleName1;

    @Id @Column(name = "rolename2")
    private String roleName2;

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
}
