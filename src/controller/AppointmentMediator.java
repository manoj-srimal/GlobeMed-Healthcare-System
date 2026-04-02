package controller;

import dao.AppointmentDao;
import gui.AppointmentSchedulingPanel;
import java.time.LocalDateTime;
import javax.swing.JOptionPane;
import model.Appointment;
import model.Location;
import model.Patient;
import model.User;

public class AppointmentMediator {

    private AppointmentDao appointmentDao;
    private AppointmentSchedulingPanel uiPanel;

    public AppointmentMediator(AppointmentSchedulingPanel uiPanel) {
        this.uiPanel = uiPanel;
        this.appointmentDao = new AppointmentDao();
    }

    public boolean createAppointment(Patient patient, User doctor, Location location, LocalDateTime dateTime) {
        if (appointmentDao.hasConflict(doctor.getUserId(), dateTime, location.getLocationId(), 0)) {
            JOptionPane.showMessageDialog(uiPanel, "Booking Conflict: The selected doctor already has an appointment at this time and location.", "Conflict Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        Appointment newAppointment = new Appointment();
        newAppointment.setPatient(patient);
        newAppointment.setDoctor(doctor);
        newAppointment.setLocation(location);
        newAppointment.setAppointmentDateTime(dateTime);
        newAppointment.setStatus("Scheduled");
        boolean success = appointmentDao.saveAppointment(newAppointment);
        if (success) {
            uiPanel.refreshAppointmentsTable();
            uiPanel.clearForm();
        }
        return success;
    }

    public boolean updateAppointment(int appointmentId, Patient patient, User doctor, Location location, LocalDateTime dateTime) {
        if (appointmentDao.hasConflict(doctor.getUserId(), dateTime, location.getLocationId(), appointmentId)) {
            JOptionPane.showMessageDialog(uiPanel, "Booking Conflict: The selected doctor already has an appointment at this time and location.", "Conflict Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        Appointment appointmentToUpdate = new Appointment();
        appointmentToUpdate.setAppointmentId(appointmentId);
        appointmentToUpdate.setPatient(patient);
        appointmentToUpdate.setDoctor(doctor);
        appointmentToUpdate.setLocation(location);
        appointmentToUpdate.setAppointmentDateTime(dateTime);
        appointmentToUpdate.setStatus("Scheduled");
        boolean success = appointmentDao.updateAppointment(appointmentToUpdate);
        if (success) {
            uiPanel.refreshAppointmentsTable();
            uiPanel.clearForm();
        }
        return success;
    }

    public boolean completeAppointment(int appointmentId) {
        boolean success = appointmentDao.updateAppointmentStatus(appointmentId, "Completed");
        if (success) {
            uiPanel.refreshAppointmentsTable();
            uiPanel.clearForm();
        }
        return success;
    }

    public boolean cancelAppointment(int appointmentId, User loggedInUser) {
        
        Appointment appointmentToCancel = appointmentDao.getAppointmentById(appointmentId);
        if (appointmentToCancel == null) {
            JOptionPane.showMessageDialog(uiPanel, "Could not find the selected appointment.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        boolean hasPermission = false;
        if ("Admin".equalsIgnoreCase(loggedInUser.getRole())) {
            hasPermission = true;
        } else if (loggedInUser.getUserId() == appointmentToCancel.getDoctor().getUserId()) {
            hasPermission = true;
        }
        if (!hasPermission) {
            JOptionPane.showMessageDialog(uiPanel, "Access Denied: You do not have permission to cancel this appointment.", "Security Warning", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        boolean success = appointmentDao.updateAppointmentStatus(appointmentId, "Cancelled");
        if (success) {
            uiPanel.refreshAppointmentsTable();
            uiPanel.clearForm();
        }
        return success;
    }
}
