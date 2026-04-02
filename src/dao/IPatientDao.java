package dao;

import java.util.List;
import model.Patient;

public interface IPatientDao {
    boolean savePatient(Patient patient);
    boolean updatePatient(Patient patient);
    boolean deletePatient(int id);
    Patient getPatient(int id);
    List<Patient> getAllPatients();
}