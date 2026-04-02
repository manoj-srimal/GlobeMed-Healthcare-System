package dao;

import java.util.List;
import model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import util.HibernateUtil;
import util.PasswordUtil;

public class UserDao {

    public User authenticateUser(String username, String password) {
        String hashedPassword = PasswordUtil.hashPassword(password);
        
        User user = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM User U WHERE U.username = :username AND U.password = :password";
            Query<User> query = session.createQuery(hql, User.class);
            query.setParameter("username", username);
            query.setParameter("password", hashedPassword);
            user = query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public boolean saveUser(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
            session.save(user);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(User userWithNewData) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            User existingUser = session.get(User.class, userWithNewData.getUserId());
            if (existingUser == null) return false;

            existingUser.setFullName(userWithNewData.getFullName());
            existingUser.setUsername(userWithNewData.getUsername());
            existingUser.setRole(userWithNewData.getRole());
            
            if (userWithNewData.getPassword() != null && !userWithNewData.getPassword().isEmpty()) {
                existingUser.setPassword(PasswordUtil.hashPassword(userWithNewData.getPassword()));
            }
            
            existingUser.getLocations().clear();
            existingUser.getLocations().addAll(userWithNewData.getLocations());
            
            session.update(existingUser);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public List<User> getUsersByRole(String role) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM User U WHERE U.role = :role";
            Query<User> query = session.createQuery(hql, User.class);
            query.setParameter("role", role);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<User> getDoctorsByLocation(int locationId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT u FROM User u JOIN u.locations l WHERE u.role = 'Doctor' AND l.locationId = :locationId";
            Query<User> query = session.createQuery(hql, User.class);
            query.setParameter("locationId", locationId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<User> getAllUsers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from User", User.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public User getUserById(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, userId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public long countDoctors() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(u) FROM User u WHERE u.role = 'Doctor'";
            Query<Long> query = session.createQuery(hql, Long.class);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
