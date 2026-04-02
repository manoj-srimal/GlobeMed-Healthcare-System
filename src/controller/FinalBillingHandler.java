package controller;

import dao.BillDao;
import javax.swing.JOptionPane;
import model.Bill;


public class FinalBillingHandler extends BillingHandler {

    @Override
    public void processBill(Bill bill) {
        System.out.println("Processing final payment for Bill ID: " + bill.getBillId());
        
        bill.setStatus("Paid");
        
        BillDao billDao = new BillDao();
        boolean success = billDao.updateBill(bill);

        if (success) {
            JOptionPane.showMessageDialog(null, 
                "Bill ID: " + bill.getBillId() + " has been processed and marked as PAID.",
                "Billing Complete", 
                JOptionPane.INFORMATION_MESSAGE);
        } else {
             JOptionPane.showMessageDialog(null, 
                "Failed to update bill status in the database.",
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
        
    }
}
