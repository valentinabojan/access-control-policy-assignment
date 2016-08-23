package psd.server;

import psd.api.Role;
import psd.api.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

public class UserRolesRepository {

    private EntityManagerFactory entityManager;

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

    public List<Role> getRolesForUser(String username) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        User foundUser = em.find(User.class, username);

        endTransaction(em);
        return foundUser.getRoles();
    }

    public List<User> getUsersForRole(String roleName) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        Role foundRole = em.find(Role.class, roleName);

        endTransaction(em);
        return foundRole.getUsers();
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

    public Role updateRole(Role role) {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        beginTransaction(em);

        Role foundRole = em.find(Role.class, role.getRoleName());
        foundRole.setRights(role.getRights());
        em.merge(foundRole);

        endTransaction(em);
        return foundRole;
    }
}
