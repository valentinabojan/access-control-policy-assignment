package psd.server;

import psd.api.Permission;
import psd.api.Role;
import psd.api.User;

import javax.persistence.EntityManager;

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

    public Permission updatePermission(Permission permission) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        Permission foundPermission = em.find(Permission.class, permission.getPermissionName());
        foundPermission.setRights(permission.getRights());
        em.merge(foundPermission);

        endTransaction(em);
        return foundPermission;
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
}
