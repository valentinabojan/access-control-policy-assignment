package psd.server;

import psd.api.Permission;
import psd.api.Role;
import psd.api.User;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserRolesRepository {

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

    public <T> T createEntity(T entity) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        em.persist(entity);

        endTransaction(em);
        return entity;
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

    private void endTransaction(EntityManager em) {
        em.getTransaction().commit();
        em.close();
    }

    private void beginTransaction(EntityManager em) {
        em.getTransaction().begin();
    }
}
