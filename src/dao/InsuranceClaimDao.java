package dao;

import java.time.LocalDate;
import java.util.List;
import model.Bill;
import model.InsuranceClaim;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import util.HibernateUtil;

public class InsuranceClaimDao {

    public boolean saveClaim(InsuranceClaim claim) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(claim);
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

    public List<InsuranceClaim> getAllSubmittedClaims() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM InsuranceClaim WHERE claimStatus = 'Submitted'", InsuranceClaim.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateClaimStatus(int claimId, String newStatus) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            InsuranceClaim claim = session.get(InsuranceClaim.class, claimId);
            if (claim == null) {
                return false;
            }
            claim.setClaimStatus(newStatus);
            claim.setResolvedDate(LocalDate.now());
            session.update(claim);
            Bill bill = claim.getBill();
            if (bill != null) {
                if ("Approved".equalsIgnoreCase(newStatus)) {
                    bill.setStatus("Paid");
                } else if ("Rejected".equalsIgnoreCase(newStatus)) {
                    bill.setStatus("Unpaid");
                }
                session.update(bill);
            }
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

    /**
     * --- NEW METHOD ---
     * Finds an insurance claim associated with a specific bill ID.
     * @param billId The ID of the bill.
     * @return The InsuranceClaim object if found, otherwise null.
     */
    public InsuranceClaim getClaimByBillId(int billId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM InsuranceClaim ic WHERE ic.bill.billId = :billId";
            Query<InsuranceClaim> query = session.createQuery(hql, InsuranceClaim.class);
            query.setParameter("billId", billId);
            return query.uniqueResult(); 
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}