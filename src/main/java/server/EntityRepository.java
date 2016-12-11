package server;

import api.entities.*;

import javax.persistence.EntityManager;

public class EntityRepository {

    public<E> void createEntity(E entity) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();

        beginTransaction(em);
        em.merge(entity);
        endTransaction(em);
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

        if (persistedUser.getRoles().stream().noneMatch(role -> role.getName().equals(roleName))) {
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

        if (persistedRole.getPermissions().stream().noneMatch(permission -> permission.getPermissionName().equals(permissionName))) {
            persistedRole.addPermission(persistedPermission);
            em.merge(persistedRole);
        }

        endTransaction(em);
        return persistedRole;
    }

    public <EntityClass, KeyClass> EntityClass getEntity(KeyClass key, Class<EntityClass> entityClass) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        EntityClass entity = em.find(entityClass, key);

        endTransaction(em);
        return entity;
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

    private void endTransaction(EntityManager em) {
        em.getTransaction().commit();
        em.close();
    }

    private void beginTransaction(EntityManager em) {
        em.getTransaction().begin();
    }
}
