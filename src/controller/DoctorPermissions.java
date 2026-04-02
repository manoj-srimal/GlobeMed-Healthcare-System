package controller;

public class DoctorPermissions implements PermissionStrategy {
    @Override
    public boolean canPerform(SystemAction action) {
        switch (action) {
            
            case CREATE_PATIENT:
            case UPDATE_PATIENT:
            case VIEW_PATIENT_LIST:
            case BOOK_APPOINTMENT:
            case UPDATE_APPOINTMENT:
            case CANCEL_APPOINTMENT: 
            case VIEW_OWN_APPOINTMENTS:
            case COMPLETE_APPOINTMENT:
                return true;
            default:
                return false;
        }
    }
}
