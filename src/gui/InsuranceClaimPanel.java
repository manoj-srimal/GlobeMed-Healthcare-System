package gui;

import controller.AuthorizationContext;
import controller.SystemAction;
import dao.InsuranceClaimDao;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;
import model.InsuranceClaim;
import model.Patient;

public class InsuranceClaimPanel extends JPanel {

    private JTable claimsTable;
    private DefaultTableModel tableModel;
    private JButton approveButton;
    private JButton rejectButton;

    private InsuranceClaimDao claimDao;
    private BillingPanel mainBillingPanel;
    private AuthorizationContext authContext; 

    public InsuranceClaimPanel(BillingPanel mainBillingPanel, AuthorizationContext authContext) {
        this.mainBillingPanel = mainBillingPanel;
        this.authContext = authContext;
        this.claimDao = new InsuranceClaimDao();
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(createTablePanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        refreshClaimsTable();
    }

    private JScrollPane createTablePanel() {
        String[] columnNames = {"Claim ID", "Bill ID", "Patient", "Provider", "Claim Amount", "Submitted Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        claimsTable = new JTable(tableModel);
        return new JScrollPane(claimsTable);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        approveButton = new JButton("Approve Selected Claim");
        rejectButton = new JButton("Reject Selected Claim");

        approveButton.addActionListener(e -> processClaim("Approved"));
        rejectButton.addActionListener(e -> processClaim("Rejected"));

        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        return buttonPanel;
    }

    public void refreshClaimsTable() {
        tableModel.setRowCount(0);
        List<InsuranceClaim> claims = claimDao.getAllSubmittedClaims();
        if (claims != null) {
            for (InsuranceClaim claim : claims) {
                Vector<Object> row = new Vector<>();
                row.add(claim.getClaimId());
                row.add(claim.getBill().getBillId());
                Patient patient = claim.getBill().getPatient();
                row.add(patient.getFirstName() + " " + patient.getLastName());
                row.add(claim.getInsuranceProvider());
                row.add(claim.getClaimAmount().toString());
                row.add(claim.getSubmittedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                tableModel.addRow(row);
            }
        }
    }

    private void processClaim(String status) {
        if (!authContext.checkPermission(SystemAction.APPROVE_REJECT_CLAIM)) return;

        int selectedRow = claimsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a claim from the table to process.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int claimId = (int) tableModel.getValueAt(selectedRow, 0);
        String patientName = (String) tableModel.getValueAt(selectedRow, 2);
        
        String message = "Are you sure you want to '" + status + "' the claim for " + patientName + "?";
        int response = JOptionPane.showConfirmDialog(this, message, "Confirm Action", JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            boolean success = claimDao.updateClaimStatus(claimId, status);
            if (success) {
                JOptionPane.showMessageDialog(this, "Claim status updated successfully!");
                refreshClaimsTable();
                mainBillingPanel.refreshBillsTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update claim status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
