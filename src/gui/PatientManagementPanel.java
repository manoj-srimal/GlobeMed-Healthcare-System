package gui;

import controller.AuthorizationContext;
import controller.SystemAction;
import dao.IPatientDao;
import dao.LoggingPatientDaoDecorator;
import dao.PatientDao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Vector;
import model.Patient;

public class PatientManagementPanel extends JPanel {

    private JTextField txtFirstName, txtLastName, txtDob, txtContact, txtAddress;
    private JButton btnSave, btnUpdate, btnDelete, btnClear;
    private JTable patientTable;
    private DefaultTableModel tableModel;

    private IPatientDao patientDao;
    private AuthorizationContext authContext;
    private int selectedPatientId = -1;

    public PatientManagementPanel(AuthorizationContext authContext) {
        this.authContext = authContext;

        IPatientDao realDao = new PatientDao();
        this.patientDao = new LoggingPatientDaoDecorator(realDao, authContext.getLoggedInUser());
        
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(createFormPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadPatientData();
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Patient Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1; txtFirstName = new JTextField(20); formPanel.add(txtFirstName, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 3; txtLastName = new JTextField(20); formPanel.add(txtLastName, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Date of Birth (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; txtDob = new JTextField(20); formPanel.add(txtDob, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Contact Number:"), gbc);
        gbc.gridx = 3; txtContact = new JTextField(20); formPanel.add(txtContact, gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL; txtAddress = new JTextField(); formPanel.add(txtAddress, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnSave = new JButton("Save");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear");
        buttonPanel.add(btnSave);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);
        addFormActions();
        return formPanel;
    }

    private JScrollPane createTablePanel() {
        String[] columnNames = {"ID", "First Name", "Last Name", "DoB", "Contact", "Address"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        patientTable = new JTable(tableModel);
        patientTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && patientTable.getSelectedRow() > -1) {
                populateFormFromSelectedRow();
            }
        });
        return new JScrollPane(patientTable);
    }

    private void addFormActions() {
        btnSave.addActionListener(e -> savePatient());
        btnUpdate.addActionListener(e -> updatePatient());
        btnDelete.addActionListener(e -> deletePatient());
        btnClear.addActionListener(e -> clearForm());
    }

    private void loadPatientData() {
        tableModel.setRowCount(0);
        List<Patient> patients = patientDao.getAllPatients();
        if (patients != null) {
            for (Patient p : patients) {
                Vector<Object> row = new Vector<>();
                row.add(p.getPatientId());
                row.add(p.getFirstName());
                row.add(p.getLastName());
                row.add(p.getDateOfBirth());
                row.add(p.getContactNumber());
                row.add(p.getAddress());
                tableModel.addRow(row);
            }
        }
    }
    
    private boolean validateInput() {
        if (txtFirstName.getText().trim().isEmpty() || txtLastName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "First Name and Last Name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        String contact = txtContact.getText();
        if (!contact.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Invalid Contact Number.\nPlease enter exactly 10 digits.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        String dobString = txtDob.getText();
        try {
            LocalDate dob = LocalDate.parse(dobString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (!dob.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Invalid Date of Birth.\nDate must be in the past.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid Date Format.\nPlease use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void populateFormFromSelectedRow() {
        int selectedRow = patientTable.getSelectedRow();
        selectedPatientId = (int) tableModel.getValueAt(selectedRow, 0);
        txtFirstName.setText((String) tableModel.getValueAt(selectedRow, 1));
        txtLastName.setText((String) tableModel.getValueAt(selectedRow, 2));
        txtDob.setText((String) tableModel.getValueAt(selectedRow, 3));
        txtContact.setText((String) tableModel.getValueAt(selectedRow, 4));
        txtAddress.setText((String) tableModel.getValueAt(selectedRow, 5));
        btnSave.setEnabled(false);
    }

    private void clearForm() {
        selectedPatientId = -1;
        txtFirstName.setText("");
        txtLastName.setText("");
        txtDob.setText("");
        txtContact.setText("");
        txtAddress.setText("");
        patientTable.clearSelection();
        btnSave.setEnabled(true);
    }

    private void savePatient() {
        if (!authContext.checkPermission(SystemAction.CREATE_PATIENT)) return;
        if (!validateInput()) return;
        
        Patient p = new Patient();
        p.setFirstName(txtFirstName.getText());
        p.setLastName(txtLastName.getText());
        p.setDateOfBirth(txtDob.getText());
        p.setContactNumber(txtContact.getText());
        p.setAddress(txtAddress.getText());
        
        if (patientDao.savePatient(p)) {
            JOptionPane.showMessageDialog(this, "Patient Saved Successfully!");
            loadPatientData();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save patient.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePatient() {
        if (!authContext.checkPermission(SystemAction.UPDATE_PATIENT)) return;
        if (selectedPatientId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient from the table to update.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!validateInput()) return;
        
        Patient p = new Patient();
        p.setPatientId(selectedPatientId);
        p.setFirstName(txtFirstName.getText());
        p.setLastName(txtLastName.getText());
        p.setDateOfBirth(txtDob.getText());
        p.setContactNumber(txtContact.getText());
        p.setAddress(txtAddress.getText());
        
        if (patientDao.updatePatient(p)) {
            JOptionPane.showMessageDialog(this, "Patient Updated Successfully!");
            loadPatientData();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update patient.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePatient() {
        if (!authContext.checkPermission(SystemAction.DELETE_PATIENT)) return;
        if (selectedPatientId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient from the table to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this patient?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            if (patientDao.deletePatient(selectedPatientId)) {
                JOptionPane.showMessageDialog(this, "Patient Deleted Successfully!");
                loadPatientData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete patient.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}