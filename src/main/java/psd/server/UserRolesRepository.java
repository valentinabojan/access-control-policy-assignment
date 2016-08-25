package psd.server;

import psd.api.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserRolesRepository {

    public User createUser(User user) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        em.persist(user);

        endTransaction(em);
        return user;
    }

    public Role createRole(Role role) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        em.persist(role);

        endTransaction(em);
        return role;
    }

    public Permission createPermission(Permission permission) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        em.persist(permission);

        endTransaction(em);
        return permission;
    }

    public User addRoleToUser(String username, String roleName) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        User persistedUser = em.find(User.class, username);
        Role persistedRole = em.find(Role.class, roleName);
        if (persistedUser == null || persistedRole == null) {
            endTransaction(em);
            return null;
        }

        if (!persistedUser.getRoles().stream().anyMatch(role -> role.getRoleName().equals(roleName))) {
            persistedUser.addRole(persistedRole);
            em.merge(persistedUser);
        }

        endTransaction(em);
        return persistedUser;
    }

    public Role addPermissionToRole(String permissionName, String roleName) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        Permission persistedPermission = em.find(Permission.class, permissionName);
        Role persistedRole = em.find(Role.class, roleName);
        if (permissionName == null || persistedRole == null) {
            endTransaction(em);
            return null;
        }

        if (!persistedRole.getPermissions().stream().anyMatch(permission -> permission.getPermissionName().equals(permissionName))) {
            persistedRole.addPermission(persistedPermission);
            em.merge(persistedRole);
        }

        endTransaction(em);
        return persistedRole;
    }

    public Role getRole(String roleName) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        Role foundRole = em.find(Role.class, roleName);

        endTransaction(em);
        return foundRole;
    }

    private void endTransaction(EntityManager em) {
        em.getTransaction().commit();
        em.close();
    }

    private void beginTransaction(EntityManager em) {
        em.getTransaction().begin();
    }

    public Permission getPermission(String permissionName) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        Permission foundPermission = em.find(Permission.class, permissionName);

        endTransaction(em);
        return foundPermission;
    }

    public User getUser(String userName) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        User foundUser = em.find(User.class, userName);

        endTransaction(em);
        return foundUser;
    }

    public void deleteRoleForUser(String userName, String roleName) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        User foundUser = em.find(User.class, userName);
        Role foundRole = em.find(Role.class,roleName);
        foundUser.getRoles().remove(foundRole);
        em.merge(foundUser);

        endTransaction(em);
    }

    public Constraint createConstraint(Constraint constraint) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        em.persist(constraint);

        endTransaction(em);
        return constraint;
    }

    public Constraint getConstraint(String roleName1, String roleName2) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        Constraint foundConstraint = em.find(Constraint.class, new PairKey(roleName1, roleName2));

        endTransaction(em);
        return foundConstraint;
    }

    public RoleHierarchy createHierarchy(RoleHierarchy roleHierarchy) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        em.persist(roleHierarchy);

        endTransaction(em);
        return roleHierarchy;
    }

    public Set<String> getChildrenRoles(String roleName) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        Query query = em.createQuery("SELECT distinct c.child FROM RoleHierarchy rh WHERE rh.parent = :parent");
        query.setParameter("parent", roleName);

        List<String> result = query.getResultList();

        endTransaction(em);
        return result.stream().collect(Collectors.toSet());
    }
}
