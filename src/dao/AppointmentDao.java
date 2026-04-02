package dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;
import model.Appointment;
import org.hibernate.query.Query;
import util.HibernateUtil;

public class AppointmentDao {

    public boolean saveAppointment(Appointment appointment) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(appointment);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAppointment(Appointment appointment) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(appointment);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAppointmentStatus(int appointmentId, String newStatus) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Appointment appointment = session.get(Appointment.class, appointmentId);
            if (appointment != null) {
                appointment.setStatus(newStatus);
                session.update(appointment);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    public List<Appointment> getAllAppointments() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Appointment order by appointmentDateTime desc", Appointment.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Appointment getAppointmentById(int appointmentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Appointment.class, appointmentId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Appointment> getAppointmentsByDoctorId(int doctorId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Appointment a WHERE a.doctor.userId = :doctorId ORDER BY a.appointmentDateTime DESC";
            Query<Appointment> query = session.createQuery(hql, Appointment.class);
            query.setParameter("doctorId", doctorId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean hasConflict(int doctorId, LocalDateTime dateTime, int locationId, int appointmentIdToExclude) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(a) FROM Appointment a WHERE a.doctor.userId = :doctorId "
                    + "AND a.location.locationId = :locationId "
                    + "AND a.appointmentDateTime = :dateTime "
                    + "AND a.status = 'Scheduled' "
                    + "AND a.appointmentId != :appointmentIdToExclude";

            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("doctorId", doctorId);
            query.setParameter("locationId", locationId);
            query.setParameter("dateTime", dateTime);
            query.setParameter("appointmentIdToExclude", appointmentIdToExclude);

            Long count = query.uniqueResult();
            return count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
        
    public List<Appointment> getCompletedAppointmentsByPatientId(int patientId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Appointment a WHERE a.patient.patientId = :patientId AND a.status = 'Completed' ORDER BY a.appointmentDateTime DESC";
            Query<Appointment> query = session.createQuery(hql, Appointment.class);
            query.setParameter("patientId", patientId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public long countAppointmentsForToday() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(a) FROM Appointment a WHERE a.status = 'Scheduled' AND DATE(a.appointmentDateTime) = :today";
            Query<Long> query = session.createQuery(hql, Long.class);
            
            query.setParameter("today", java.sql.Date.valueOf(LocalDate.now()));
            
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
