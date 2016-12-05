package server;

import api.entities.Permission;
import api.entities.Role;
import api.entities.User;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EntityRepository {

    public User addRoleToUser(String username, String roleName) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        User persistedUser = em.find(User.class, username);
        Role persistedRole = em.find(Role.class, roleName);
        if (persistedUser == null || persistedRole == null) {
            endTransaction(em);
            return null;
        }

        if (!persistedUser.getRoles().stream().anyMatch(role -> role.getName().equals(roleName))) {
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

    public <EntityClass, KeyClass> EntityClass getEntity(Class<EntityClass> entityClass, KeyClass key) {
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

    public <T> void createEntity(T entity) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();

        beginTransaction(em);
        em.merge(entity);
        endTransaction(em);
    }

    public Set<Role> getChildrenRoles(String roleName) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        Query query = em.createQuery("SELECT distinct rh.child FROM RoleHierarchy rh WHERE rh.parent = :parent");
        query.setParameter("parent", roleName);

        List<String> result = query.getResultList();

        endTransaction(em);
        return result.stream().map(r -> getEntity(Role.class, r)).collect(Collectors.toSet());
    }

    private void endTransaction(EntityManager em) {
        em.getTransaction().commit();
        em.close();
    }

    private void beginTransaction(EntityManager em) {
        em.getTransaction().begin();
    }
}
