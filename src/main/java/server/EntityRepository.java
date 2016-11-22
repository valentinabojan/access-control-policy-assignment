package server;

import api.Role;
import api.User;

import javax.persistence.EntityManager;

public class EntityRepository {

    public<E> void createEntity(E entity) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();

        beginTransaction(em);
        em.persist(entity);
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

    public Role getRole(String roleName) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        Role foundRole = em.find(Role.class, roleName);

        endTransaction(em);
        return foundRole;
    }

    public Role updateRole(Role role) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        Role foundRole = em.find(Role.class, role.getName());
        foundRole.setRights(role.getRights());
        em.merge(foundRole);

        endTransaction(em);
        return foundRole;
    }

    private void beginTransaction(EntityManager em) {
        em.getTransaction().begin();
    }

    private void endTransaction(EntityManager em) {
        em.getTransaction().commit();
        em.close();
    }
}
