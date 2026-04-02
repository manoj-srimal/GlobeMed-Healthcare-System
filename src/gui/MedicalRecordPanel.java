package gui;

import controller.AuthorizationContext;
import controller.PatientSummaryVisitor;
import controller.ReportVisitor;
import dao.MedicalRecordDao;
import dao.PatientDao;
import model.MedicalRecord;
import model.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.print.PrinterException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MedicalRecordPanel extends JPanel {

    private JComboBox<Patient> patientComboBox;
    private JTextArea historyArea;
    private JTextArea diagnosisArea;
    private JTextArea treatmentArea;
    private JTextArea notesArea;
    private JButton saveRecordButton;
    private JButton generateReportButton;

    private PatientDao patientDao;
    private MedicalRecordDao recordDao;
    private AuthorizationContext authContext;

    public MedicalRecordPanel(AuthorizationContext authContext) {
        this.authContext = authContext;
        this.patientDao = new PatientDao();
        this.recordDao = new MedicalRecordDao();

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(createSelectionPanel(), BorderLayout.NORTH);
        add(createMainContentPanel(), BorderLayout.CENTER);

        loadInitialData();
    }

    private JPanel createSelectionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Select Patient:"));
        patientComboBox = new JComboBox<>();
        panel.add(patientComboBox);
        
        generateReportButton = new JButton("Generate Patient Summary");
        panel.add(generateReportButton);
        
        patientComboBox.addActionListener(e -> loadPatientHistory());
        generateReportButton.addActionListener(e -> generateReport());
        
        return panel;
    }

    private JSplitPane createMainContentPanel() {
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("Patient Medical History"));
        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        historyPanel.add(new JScrollPane(historyArea), BorderLayout.CENTER);
        JPanel newRecordPanel = new JPanel(new GridBagLayout());
        newRecordPanel.setBorder(BorderFactory.createTitledBorder("Add New Record"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.gridx = 0; gbc.gridy = 0; newRecordPanel.add(new JLabel("Diagnosis:"), gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weighty = 0.4;
        diagnosisArea = new JTextArea();
        newRecordPanel.add(new JScrollPane(diagnosisArea), gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.weighty = 0.0; newRecordPanel.add(new JLabel("Treatment Plan:"), gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.weighty = 0.4;
        treatmentArea = new JTextArea();
        newRecordPanel.add(new JScrollPane(treatmentArea), gbc);
        gbc.gridx = 0; gbc.gridy = 4; gbc.weighty = 0.0; newRecordPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 0; gbc.gridy = 5; gbc.weighty = 0.2;
        notesArea = new JTextArea();
        newRecordPanel.add(new JScrollPane(notesArea), gbc);
        gbc.gridx = 0; gbc.gridy = 6; gbc.weighty = 0.0; gbc.fill = GridBagConstraints.NONE;
        saveRecordButton = new JButton("Save New Record");
        newRecordPanel.add(saveRecordButton, gbc);
        saveRecordButton.addActionListener(e -> saveNewRecord());
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, historyPanel, newRecordPanel);
        splitPane.setDividerLocation(500);
        return splitPane;
    }

    private void loadInitialData() {
        List<Patient> patients = patientDao.getAllPatients();
        if (patients != null) {
            for (Patient p : patients) {
                patientComboBox.addItem(p);
            }
        }
        patientComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Patient) {
                    setText(((Patient) value).getFirstName() + " " + ((Patient) value).getLastName());
                }
                return this;
            }
        });
        if (patientComboBox.getItemCount() > 0) {
            patientComboBox.setSelectedIndex(0);
            loadPatientHistory();
        }
    }
    
    private void loadPatientHistory() {
        historyArea.setText("");
        Patient selectedPatient = (Patient) patientComboBox.getSelectedItem();
        if (selectedPatient == null) return;
        List<MedicalRecord> records = recordDao.getRecordsByPatientId(selectedPatient.getPatientId());
        if (records != null && !records.isEmpty()) {
            StringBuilder history = new StringBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (MedicalRecord record : records) {
                history.append("-------------------------------------------------\n");
                history.append("Date: ").append(record.getRecordDate().format(formatter)).append("\n");
                history.append("Doctor: ").append(record.getDoctor().getFullName()).append("\n\n");
                history.append("Diagnosis:\n").append(record.getDiagnosis()).append("\n\n");
                history.append("Treatment Plan:\n").append(record.getTreatmentPlan()).append("\n\n");
                history.append("Notes:\n").append(record.getNotes()).append("\n");
                history.append("-------------------------------------------------\n\n");
            }
            historyArea.setText(history.toString());
        } else {
            historyArea.setText("No medical records found for this patient.");
        }
    }
    
    private void saveNewRecord() {
        // ... (This method's code remains the same) ...
        Patient selectedPatient = (Patient) patientComboBox.getSelectedItem();
        String diagnosis = diagnosisArea.getText();
        String treatment = treatmentArea.getText();
        if (selectedPatient == null || diagnosis.trim().isEmpty() || treatment.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a patient and fill in Diagnosis and Treatment Plan.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        MedicalRecord newRecord = new MedicalRecord();
        newRecord.setPatient(selectedPatient);
        newRecord.setDoctor(authContext.getLoggedInUser());
        newRecord.setRecordDate(LocalDate.now());
        newRecord.setDiagnosis(diagnosis);
        newRecord.setTreatmentPlan(treatment);
        newRecord.setNotes(notesArea.getText());
        if (recordDao.saveRecord(newRecord)) {
            JOptionPane.showMessageDialog(this, "Medical record saved successfully!");
            loadPatientHistory();
            diagnosisArea.setText("");
            treatmentArea.setText("");
            notesArea.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save medical record.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateReport() {
        Patient selectedPatient = (Patient) patientComboBox.getSelectedItem();
        if (selectedPatient == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient to generate a report.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ReportVisitor summaryVisitor = new PatientSummaryVisitor();
        StringBuilder reportContent = new StringBuilder();
        reportContent.append(selectedPatient.accept(summaryVisitor));
        List<MedicalRecord> records = recordDao.getRecordsByPatientId(selectedPatient.getPatientId());
        if (records != null) {
            for (MedicalRecord record : records) {
                reportContent.append(record.accept(summaryVisitor));
            }
        }
        showReportWindow(reportContent.toString(), selectedPatient.getFirstName());
    }
    
    private void showReportWindow(String content, String patientName) {
        JTextArea reportArea = new JTextArea(content);
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setPreferredSize(new Dimension(600, 700));

        JButton printButton = new JButton("Print Report");
        JButton closeButton = new JButton("Close");

        JOptionPane optionPane = new JOptionPane(
            scrollPane,
            JOptionPane.PLAIN_MESSAGE,
            JOptionPane.DEFAULT_OPTION,
            null,
            new Object[]{printButton, closeButton},
            closeButton); 

        JDialog dialog = optionPane.createDialog("Medical Summary for " + patientName);

        printButton.addActionListener(e -> {
            try {
                boolean didPrint = reportArea.print();
                if (didPrint) {
                    JOptionPane.showMessageDialog(dialog, "Report sent to the printer successfully.");
                }
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(dialog, "Printing failed: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        closeButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }
}
