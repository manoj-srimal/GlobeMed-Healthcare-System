package dao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import model.Patient;
import model.User;

public class LoggingPatientDaoDecorator implements IPatientDao {

    private final IPatientDao wrappedDao;
    private final User loggedInUser;

    public LoggingPatientDaoDecorator(IPatientDao dao, User loggedInUser) {
        this.wrappedDao = dao;
        this.loggedInUser = loggedInUser;
    }

    @Override
    public boolean savePatient(Patient patient) {
        logAction("Attempting to SAVE new patient: " + patient.getFirstName());
        boolean result = wrappedDao.savePatient(patient);
        if (result) {
            logAction("SUCCESS: Saved new patient with ID: " + patient.getPatientId());
        } else {
            logAction("FAILURE: Failed to save patient: " + patient.getFirstName());
        }
        return result;
    }

    @Override
    public boolean updatePatient(Patient patient) {
        logAction("Attempting to UPDATE patient ID: " + patient.getPatientId());
        boolean result = wrappedDao.updatePatient(patient);
        if (result) {
            logAction("SUCCESS: Updated patient ID: " + patient.getPatientId());
        } else {
            logAction("FAILURE: Failed to update patient ID: " + patient.getPatientId());
        }
        return result;
    }

    @Override
    public boolean deletePatient(int id) {
        logAction("Attempting to DELETE patient ID: " + id);
        boolean result = wrappedDao.deletePatient(id);
        if (result) {
            logAction("SUCCESS: Deleted patient ID: " + id);
        } else {
            logAction("FAILURE: Failed to delete patient ID: " + id);
        }
        return result;
    }

    @Override
    public Patient getPatient(int id) {
        return wrappedDao.getPatient(id);
    }

    @Override
    public List<Patient> getAllPatients() {
        return wrappedDao.getAllPatients();
    }

    private void logAction(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("[LOG - " + timestamp + "] User: " + loggedInUser.getUsername() + " | Action: " + message);
    }
}