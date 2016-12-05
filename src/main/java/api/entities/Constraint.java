package api.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "USER_CONSTRAINT")
@IdClass(ConstraintId.class)
public class Constraint implements Serializable {

    @Id
    @Column(name = "ROLENAME1")
    private String roleName1;

    @Id
    @Column(name = "ROLENAME2")
    private String roleName2;

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

    public static class ConstraintBuilder {
        private Constraint constraint;

        private ConstraintBuilder() {
            constraint = new Constraint();
        }

        public static ConstraintBuilder constraint() {
            return new ConstraintBuilder();
        }

        public ConstraintBuilder withRoleName1(String roleName1) {
            constraint.roleName1 = roleName1;
            return this;
        }

        public ConstraintBuilder withRoleName2(String roleName2) {
            constraint.roleName2 = roleName2;
            return this;
        }

        public Constraint build() {
            return constraint;
        }
    }
}