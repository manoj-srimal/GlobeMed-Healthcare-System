package controller;

import dao.BillDao;
import dao.InsuranceClaimDao;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.swing.JOptionPane;
import model.Bill;
import model.InsuranceClaim;

public class InsuranceHandler extends BillingHandler {

    @Override
    public void processBill(Bill bill) {
        int response = JOptionPane.showConfirmDialog(null,
                "Does the patient have a valid insurance claim for this bill?",
                "Insurance Check",
                JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            String provider = JOptionPane.showInputDialog("Enter Insurance Provider:");
            if (provider == null || provider.trim().isEmpty()) {
                passToNext(bill);
                return;
            }

            String claimAmountStr = JOptionPane.showInputDialog("Enter Claim Amount (LKR):");
            BigDecimal claimAmount;
            try {
                claimAmount = new BigDecimal(claimAmountStr);
                if (claimAmount.compareTo(bill.getAmount()) > 0) {
                    JOptionPane.showMessageDialog(null, "Claim amount cannot be greater than the bill amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException | NullPointerException e) {
                JOptionPane.showMessageDialog(null, "Invalid amount entered. Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return; 
            }

            InsuranceClaim claim = new InsuranceClaim();
            claim.setBill(bill);
            claim.setInsuranceProvider(provider);
            claim.setClaimAmount(claimAmount);
            claim.setClaimStatus("Submitted");
            claim.setSubmittedDate(LocalDate.now());

            InsuranceClaimDao claimDao = new InsuranceClaimDao();
            boolean claimSaved = claimDao.saveClaim(claim);

            if (claimSaved) {
                bill.setStatus("Pending Insurance");
                BillDao billDao = new BillDao();
                billDao.updateBill(bill);
                JOptionPane.showMessageDialog(null, "Insurance claim submitted successfully. Bill status is now 'Pending Insurance'.");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to save the insurance claim.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } else {
            passToNext(bill);
        }
    }
    
    private void passToNext(Bill bill) {
        if (nextHandler != null) {
            nextHandler.processBill(bill);
        }
    }
}
