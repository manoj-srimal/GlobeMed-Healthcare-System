package gui;

import controller.AuthorizationContext;
import controller.BillingHandler;
import controller.FinalBillingHandler;
import controller.InsuranceHandler;
import controller.SystemAction;
import dao.AppointmentDao;
import dao.BillDao;
import dao.InsuranceClaimDao;
import dao.PatientDao;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;
import model.Appointment;
import model.Bill;
import model.InsuranceClaim;
import model.Patient;
import model.User;

public class BillingPanel extends JPanel {

    private JComboBox<Patient> patientComboBox;
    private JComboBox<Appointment> appointmentComboBox;
    private JTextField serviceField;
    private JTextField amountField;
    private JButton createBillButton;
    private JTable billsTable;
    private DefaultTableModel tableModel;
    private JLabel lblClaimProvider, lblClaimAmount, lblClaimStatus;
    private JButton processInsuranceButton;
    private JButton payDirectlyButton;

    private BillDao billDao;
    private PatientDao patientDao;
    private AppointmentDao appointmentDao;
    private InsuranceClaimDao claimDao;
    private AuthorizationContext authContext; 
    
    private InsuranceClaimPanel insuranceClaimPanel;
    private BillingHandler billingChain;

    public BillingPanel(AuthorizationContext authContext) { 
        this.authContext = authContext;
        this.billDao = new BillDao();
        this.patientDao = new PatientDao();
        this.appointmentDao = new AppointmentDao();
        this.claimDao = new InsuranceClaimDao();

        setupBillingChain();

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel createBillPanel = createMainBillPanel();
        this.insuranceClaimPanel = new InsuranceClaimPanel(this, authContext); 

        tabbedPane.addTab("Create & View Bills", createBillPanel);
        tabbedPane.addTab("Process Insurance Claims", this.insuranceClaimPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void setupBillingChain() {
        BillingHandler insuranceHandler = new InsuranceHandler();
        BillingHandler finalBillingHandler = new FinalBillingHandler();
        insuranceHandler.setNext(finalBillingHandler);
        this.billingChain = insuranceHandler;
    }

    private JPanel createMainBillPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.add(createFormPanel(), BorderLayout.NORTH);
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);
        mainPanel.add(createDetailsAndActionsPanel(), BorderLayout.SOUTH);
        loadInitialData();
        refreshBillsTable();
        return mainPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Create New Bill"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Select Patient:"), gbc);
        gbc.gridx = 1; patientComboBox = new JComboBox<>(); formPanel.add(patientComboBox, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Link to Appointment (Optional):"), gbc);
        gbc.gridx = 3; appointmentComboBox = new JComboBox<>(); formPanel.add(appointmentComboBox, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Service Description:"), gbc);
        gbc.gridx = 1; serviceField = new JTextField(30); formPanel.add(serviceField, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Amount (LKR):"), gbc);
        gbc.gridx = 3; amountField = new JTextField(10); formPanel.add(amountField, gbc);
        gbc.gridx = 3; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        createBillButton = new JButton("Create Bill");
        formPanel.add(createBillButton, gbc);
        createBillButton.addActionListener(e -> createBill());
        patientComboBox.addActionListener(e -> updateAppointmentList());
        return formPanel;
    }

    private JScrollPane createTablePanel() {
        String[] columnNames = {"Bill ID", "Patient Name", "Service", "Amount", "Date", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        billsTable = new JTable(tableModel);
        billsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showClaimDetailsForSelectedBill();
                updateActionButtonsState();
            }
        });
        return new JScrollPane(billsTable);
    }
    
    private JPanel createDetailsAndActionsPanel() {
        JPanel southPanel = new JPanel(new BorderLayout());
        JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Insurance Claim Details for Selected Bill"));
        detailsPanel.add(new JLabel("Provider:"));
        lblClaimProvider = new JLabel("N/A");
        detailsPanel.add(lblClaimProvider);
        detailsPanel.add(new JSeparator(SwingConstants.VERTICAL));
        detailsPanel.add(new JLabel("Claimed Amount:"));
        lblClaimAmount = new JLabel("N/A");
        detailsPanel.add(lblClaimAmount);
        detailsPanel.add(new JSeparator(SwingConstants.VERTICAL));
        detailsPanel.add(new JLabel("Claim Status:"));
        lblClaimStatus = new JLabel("N/A");
        detailsPanel.add(lblClaimStatus);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        processInsuranceButton = new JButton("Process with Insurance");
        payDirectlyButton = new JButton("Pay Bill Directly");
        processInsuranceButton.addActionListener(e -> processWithInsurance());
        payDirectlyButton.addActionListener(e -> payBillDirectly());
        buttonPanel.add(processInsuranceButton);
        buttonPanel.add(payDirectlyButton);
        southPanel.add(detailsPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.EAST);
        return southPanel;
    }

    private void loadInitialData() {
        List<Patient> patients = patientDao.getAllPatients();
        if (patients != null) {
            for (Patient p : patients) { patientComboBox.addItem(p); }
        }
        patientComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Patient) { setText(((Patient) value).getFirstName() + " " + ((Patient) value).getLastName()); }
                return this;
            }
        });
        updateAppointmentList();
    }
    
    private void updateAppointmentList() {
        appointmentComboBox.removeAllItems();
        appointmentComboBox.addItem(null);
        Patient selectedPatient = (Patient) patientComboBox.getSelectedItem();
        if (selectedPatient != null) {
            List<Appointment> appointments = appointmentDao.getCompletedAppointmentsByPatientId(selectedPatient.getPatientId());
            if (appointments != null) {
                for (Appointment app : appointments) {
                    appointmentComboBox.addItem(app);
                }
            }
        }
        appointmentComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Appointment) {
                    Appointment app = (Appointment) value;
                    setText(app.getAppointmentDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " with Dr. " + app.getDoctor().getFullName());
                } else {
                    setText("General Bill (No Appointment)");
                }
                return this;
            }
        });
    }

    public void refreshBillsTable() {
        tableModel.setRowCount(0);
        List<Bill> bills = billDao.getAllBills();
        if (bills != null) {
            for (Bill bill : bills) {
                Vector<Object> row = new Vector<>();
                row.add(bill.getBillId());
                row.add(bill.getPatient().getFirstName() + " " + bill.getPatient().getLastName());
                row.add(bill.getServiceDescription());
                row.add(bill.getAmount().toString());
                row.add(bill.getBillDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                row.add(bill.getStatus());
                tableModel.addRow(row);
            }
        }
        updateActionButtonsState();
    }

    private void createBill() {
        if (!authContext.checkPermission(SystemAction.CREATE_BILL)) return;

        Patient selectedPatient = (Patient) patientComboBox.getSelectedItem();
        Appointment selectedAppointment = (Appointment) appointmentComboBox.getSelectedItem();
        String service = serviceField.getText();
        String amountStr = amountField.getText();
        if (selectedPatient == null || service.trim().isEmpty() || amountStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            BigDecimal amount = new BigDecimal(amountStr);
            Bill newBill = new Bill();
            newBill.setPatient(selectedPatient);
            newBill.setAppointment(selectedAppointment);
            newBill.setServiceDescription(service);
            newBill.setAmount(amount);
            newBill.setBillDate(LocalDate.now());
            newBill.setStatus("Unpaid");
            if (billDao.saveBill(newBill)) {
                JOptionPane.showMessageDialog(this, "Bill created successfully!");
                refreshBillsTable();
                serviceField.setText("");
                amountField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create bill.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount. Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showClaimDetailsForSelectedBill() {
        int selectedRow = billsTable.getSelectedRow();
        if (selectedRow == -1) {
            lblClaimProvider.setText("N/A");
            lblClaimAmount.setText("N/A");
            lblClaimStatus.setText("N/A");
            return;
        }
        int billId = (int) tableModel.getValueAt(selectedRow, 0);
        InsuranceClaim claim = claimDao.getClaimByBillId(billId);
        if (claim != null) {
            lblClaimProvider.setText(claim.getInsuranceProvider());
            lblClaimAmount.setText("LKR " + claim.getClaimAmount().toString());
            lblClaimStatus.setText(claim.getClaimStatus());
        } else {
            lblClaimProvider.setText("No Claim Submitted");
            lblClaimAmount.setText("N/A");
            lblClaimStatus.setText("N/A");
        }
    }

    private void updateActionButtonsState() {
        int selectedRow = billsTable.getSelectedRow();
        if (selectedRow == -1) {
            processInsuranceButton.setEnabled(false);
            payDirectlyButton.setEnabled(false);
            return;
        }
        String status = (String) tableModel.getValueAt(selectedRow, 5);
        boolean isUnpaid = "Unpaid".equalsIgnoreCase(status);
        processInsuranceButton.setEnabled(isUnpaid);
        payDirectlyButton.setEnabled(isUnpaid);
    }

    private void processWithInsurance() {
        if (!authContext.checkPermission(SystemAction.PROCESS_INSURANCE_CLAIM)) return;

        int selectedRow = billsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an unpaid bill to process.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int billId = (int) tableModel.getValueAt(selectedRow, 0);
        Bill selectedBill = billDao.getBillById(billId);
        if (selectedBill != null) {
            billingChain.processBill(selectedBill);
            refreshBillsTable();
            insuranceClaimPanel.refreshClaimsTable();
        }
    }

    private void payBillDirectly() {
        int selectedRow = billsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an unpaid bill to pay.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int billId = (int) tableModel.getValueAt(selectedRow, 0);
        Bill selectedBill = billDao.getBillById(billId);
        if (selectedBill != null) {
            int response = JOptionPane.showConfirmDialog(this, 
                "Confirm payment for Bill ID: " + billId + " amounting to LKR " + selectedBill.getAmount() + "?",
                "Confirm Direct Payment", 
                JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                selectedBill.setStatus("Paid");
                if (billDao.updateBill(selectedBill)) {
                    JOptionPane.showMessageDialog(this, "Bill marked as paid successfully!");
                    refreshBillsTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update bill status.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}