package controller;

import java.time.format.DateTimeFormatter;
import model.MedicalRecord;
import model.Patient;

public class PatientSummaryVisitor implements ReportVisitor {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public String visit(Patient patient) {
        StringBuilder sb = new StringBuilder();
        sb.append("=================================================\n");
        sb.append("         PATIENT MEDICAL SUMMARY REPORT\n");
        sb.append("=================================================\n\n");
        sb.append("Patient ID:      ").append(patient.getPatientId()).append("\n");
        sb.append("Full Name:       ").append(patient.getFirstName()).append(" ").append(patient.getLastName()).append("\n");
        sb.append("Date of Birth:   ").append(patient.getDateOfBirth()).append("\n");
        sb.append("Contact Number:  ").append(patient.getContactNumber()).append("\n");
        sb.append("Address:         ").append(patient.getAddress()).append("\n\n");
        sb.append("----------------- MEDICAL RECORDS -----------------\n\n");
        return sb.toString();
    }

    @Override
    public String visit(MedicalRecord medicalRecord) {
        StringBuilder sb = new StringBuilder();
        sb.append("Record Date: ").append(medicalRecord.getRecordDate().format(dateFormatter)).append("\n");
        sb.append("Consulting Doctor: ").append(medicalRecord.getDoctor().getFullName()).append("\n\n");
        sb.append("  Diagnosis:\n");
        sb.append("    ").append(medicalRecord.getDiagnosis().replaceAll("\n", "\n    ")).append("\n\n");
        sb.append("  Treatment Plan:\n");
        sb.append("    ").append(medicalRecord.getTreatmentPlan().replaceAll("\n", "\n    ")).append("\n\n");
        if (medicalRecord.getNotes() != null && !medicalRecord.getNotes().isEmpty()) {
            sb.append("  Notes:\n");
            sb.append("    ").append(medicalRecord.getNotes().replaceAll("\n", "\n    ")).append("\n");
        }
        sb.append("-------------------------------------------------\n\n");
        return sb.toString();
    }
}
