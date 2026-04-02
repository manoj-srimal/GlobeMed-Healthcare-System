package controller;

import model.MedicalRecord;
import model.Patient;

public interface ReportVisitor {
    String visit(Patient patient);
    String visit(MedicalRecord medicalRecord);
}
