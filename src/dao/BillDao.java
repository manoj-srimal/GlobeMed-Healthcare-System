package dao;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import model.Appointment;
import model.Bill;
import org.hibernate.query.Query;
import util.HibernateUtil;

public class BillDao {

    public boolean saveBill(Bill bill) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(bill);
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

    public boolean updateBill(Bill bill) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(bill);
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


    public List<Bill> getAllBills() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Bill order by billDate desc", Bill.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bill getBillById(int billId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Bill.class, billId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
    
     public long countPendingBills() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(b) FROM Bill b WHERE b.status = 'Unpaid' OR b.status = 'Pending Insurance'";
            Query<Long> query = session.createQuery(hql, Long.class);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
