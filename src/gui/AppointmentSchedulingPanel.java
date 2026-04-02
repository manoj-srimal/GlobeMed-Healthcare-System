package gui;

import controller.AppointmentMediator;
import controller.AuthorizationContext;
import controller.SystemAction;
import dao.AppointmentDao;
import dao.LocationDao;
import dao.PatientDao;
import dao.UserDao;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Vector;
import model.Appointment;
import model.Location;
import model.Patient;
import model.User;

public class AppointmentSchedulingPanel extends JPanel {

    private JComboBox<Location> locationComboBox;
    private JComboBox<Patient> patientComboBox;
    private JComboBox<User> doctorComboBox;
    private JTextField dateField;
    private JTextField timeField;
    private JButton actionButton;
    private JButton completeButton;
    private JButton cancelButton;
    private JButton clearButton;
    private JTable appointmentsTable;
    private DefaultTableModel tableModel;
    private int selectedAppointmentId = 0;

    private PatientDao patientDao;
    private UserDao userDao;
    private LocationDao locationDao;
    private AppointmentMediator mediator;
    private AuthorizationContext authContext;

    public AppointmentSchedulingPanel(AuthorizationContext authContext) {
        this.authContext = authContext;
        this.patientDao = new PatientDao();
        this.userDao = new UserDao();
        this.locationDao = new LocationDao();
        this.mediator = new AppointmentMediator(this);

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(createFormPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadInitialData();
        refreshAppointmentsTable();
        updateButtonStates();
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Manage Appointments"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Select Location:"), gbc);
        gbc.gridx = 1; locationComboBox = new JComboBox<>(); formPanel.add(locationComboBox, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Select Patient:"), gbc);
        gbc.gridx = 3; patientComboBox = new JComboBox<>(); formPanel.add(patientComboBox, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Select Doctor:"), gbc);
        gbc.gridx = 1; doctorComboBox = new JComboBox<>(); formPanel.add(doctorComboBox, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 3; dateField = new JTextField(10); formPanel.add(dateField, gbc);
        gbc.gridx = 4; formPanel.add(new JLabel("Time (HH:MM):"), gbc);
        gbc.gridx = 5; timeField = new JTextField(5); formPanel.add(timeField, gbc);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actionButton = new JButton("Book Appointment");
        completeButton = new JButton("Mark as Completed");
        cancelButton = new JButton("Cancel Selected");
        clearButton = new JButton("Clear Form");
        buttonPanel.add(actionButton);
        buttonPanel.add(completeButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(clearButton);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 6;
        formPanel.add(buttonPanel, gbc);
        actionButton.addActionListener(e -> performMainAction());
        completeButton.addActionListener(e -> completeAppointment());
        cancelButton.addActionListener(e -> cancelAppointment());
        clearButton.addActionListener(e -> clearForm());
        locationComboBox.addActionListener(e -> updateDoctorList());
        return formPanel;
    }

    private JScrollPane createTablePanel() {
        String[] columnNames = {"ID", "Location", "Patient Name", "Doctor Name", "Date & Time", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        appointmentsTable = new JTable(tableModel);
        appointmentsTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                if (appointmentsTable.getSelectedRow() > -1) {
                    populateFormFromSelectedRow();
                }
                updateButtonStates();
            }
        });
        return new JScrollPane(appointmentsTable);
    }

    private void loadInitialData() {
        List<Location> locations = locationDao.getAllLocations();
        if (locations != null) {
            for (Location loc : locations) { locationComboBox.addItem(loc); }
        }
        locationComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Location) { setText(((Location) value).getName()); }
                return this;
            }
        });
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
        updateDoctorList();
    }
    
    private void updateDoctorList() {
        doctorComboBox.removeAllItems();
        Location selectedLocation = (Location) locationComboBox.getSelectedItem();
        if (selectedLocation != null) {
            List<User> doctors = userDao.getDoctorsByLocation(selectedLocation.getLocationId());
            if (doctors != null) {
                for (User d : doctors) { doctorComboBox.addItem(d); }
            }
        }
        doctorComboBox.setRenderer(new DefaultListCellRenderer() {
             @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User) { setText(((User) value).getFullName()); }
                return this;
            }
        });
    }

    public void refreshAppointmentsTable() {
        tableModel.setRowCount(0);
        AppointmentDao dao = new AppointmentDao();
        List<Appointment> appointments = null;

        if (authContext.hasPermission(SystemAction.VIEW_ALL_APPOINTMENTS)) {
            appointments = dao.getAllAppointments();
        } else if (authContext.hasPermission(SystemAction.VIEW_OWN_APPOINTMENTS)) {
            appointments = dao.getAppointmentsByDoctorId(authContext.getLoggedInUser().getUserId());
        }
        
        if (appointments != null) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (Appointment app : appointments) {
                Vector<Object> row = new Vector<>();
                row.add(app.getAppointmentId());
                row.add(app.getLocation() != null ? app.getLocation().getName() : "N/A");
                row.add(app.getPatient().getFirstName() + " " + app.getPatient().getLastName());
                row.add(app.getDoctor().getFullName());
                row.add(app.getAppointmentDateTime().format(formatter));
                row.add(app.getStatus());
                tableModel.addRow(row);
            }
        }
    }
    
    public void clearForm() {

        selectedAppointmentId = 0;
        locationComboBox.setSelectedIndex(0);
        patientComboBox.setSelectedIndex(-1);
        dateField.setText("");
        timeField.setText("");
        appointmentsTable.clearSelection();
        actionButton.setText("Book Appointment");
        updateButtonStates();
    }

    private void populateFormFromSelectedRow() {

        int selectedRow = appointmentsTable.getSelectedRow();
        selectedAppointmentId = (int) tableModel.getValueAt(selectedRow, 0);
        String locationName = (String) tableModel.getValueAt(selectedRow, 1);
        for (int i = 0; i < locationComboBox.getItemCount(); i++) {
            if (locationComboBox.getItemAt(i).getName().equals(locationName)) {
                locationComboBox.setSelectedIndex(i);
                break;
            }
        }
        SwingUtilities.invokeLater(() -> {
            String patientName = (String) tableModel.getValueAt(selectedRow, 2);
            for (int i = 0; i < patientComboBox.getItemCount(); i++) {
                Patient p = patientComboBox.getItemAt(i);
                if ((p.getFirstName() + " " + p.getLastName()).equals(patientName)) {
                    patientComboBox.setSelectedIndex(i);
                    break;
                }
            }
            String doctorName = (String) tableModel.getValueAt(selectedRow, 3);
            for (int i = 0; i < doctorComboBox.getItemCount(); i++) {
                User u = doctorComboBox.getItemAt(i);
                if (u.getFullName().equals(doctorName)) {
                    doctorComboBox.setSelectedIndex(i);
                    break;
                }
            }
        });
        String dateTimeStr = (String) tableModel.getValueAt(selectedRow, 4);
        dateField.setText(dateTimeStr.substring(0, 10));
        timeField.setText(dateTimeStr.substring(11));
        actionButton.setText("Update Appointment");
    }
    
    private void performMainAction() {
        try {

            SystemAction action = (selectedAppointmentId == 0) ? SystemAction.BOOK_APPOINTMENT : SystemAction.UPDATE_APPOINTMENT;
            if (!authContext.checkPermission(action)) return;

            Location selectedLocation = (Location) locationComboBox.getSelectedItem();
            Patient selectedPatient = (Patient) patientComboBox.getSelectedItem();
            User selectedDoctor = (User) doctorComboBox.getSelectedItem();
            String dateTimeString = dateField.getText() + " " + timeField.getText();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime appointmentDateTime = LocalDateTime.parse(dateTimeString, formatter);
            if (selectedLocation == null || selectedPatient == null || selectedDoctor == null) {
                JOptionPane.showMessageDialog(this, "Please select a location, patient, and doctor.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            LocalDateTime now = LocalDateTime.now();
            if (appointmentDateTime.isBefore(now.plusHours(2))) {
                JOptionPane.showMessageDialog(this, "Invalid Time. Appointment must be scheduled at least 2 hours in advance.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean success;
            if (selectedAppointmentId == 0) {
                success = mediator.createAppointment(selectedPatient, selectedDoctor, selectedLocation, appointmentDateTime);
            } else {
                success = mediator.updateAppointment(selectedAppointmentId, selectedPatient, selectedDoctor, selectedLocation, appointmentDateTime);
            }
            if (success) {
                JOptionPane.showMessageDialog(this, "Operation successful!");
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date or time format. Please use YYYY-MM-DD and HH:MM.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void completeAppointment() {
        if (!authContext.checkPermission(SystemAction.COMPLETE_APPOINTMENT)) return;
        
        if (selectedAppointmentId == 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to mark as completed.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean success = mediator.completeAppointment(selectedAppointmentId);
        if (success) {
            JOptionPane.showMessageDialog(this, "Appointment marked as completed!");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update appointment status.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cancelAppointment() {
        if (!authContext.checkPermission(SystemAction.CANCEL_APPOINTMENT)) return;
        
        if (selectedAppointmentId == 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to cancel.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel this appointment?", "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            boolean success = mediator.cancelAppointment(selectedAppointmentId, authContext.getLoggedInUser());
            if (success) {
                JOptionPane.showMessageDialog(this, "Appointment cancelled successfully!");
            }
        }
    }
    
    private void updateButtonStates() {
        int selectedRow = appointmentsTable.getSelectedRow();
        
        if (selectedRow == -1) {
            cancelButton.setEnabled(false);
            completeButton.setEnabled(false);
            return;
        }

        String status = (String) tableModel.getValueAt(selectedRow, 5);
        if (!"Scheduled".equalsIgnoreCase(status)) {
            cancelButton.setEnabled(false);
            completeButton.setEnabled(false);
            return;
        }
        
        completeButton.setEnabled(true);
        cancelButton.setEnabled(true);
    }
}