package dao;

import org.hibernate.Session;
import java.util.List;
import model.Location;
import util.HibernateUtil;

public class LocationDao {
    public List<Location> getAllLocations() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Location", Location.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
