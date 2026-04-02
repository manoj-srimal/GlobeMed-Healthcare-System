package controller;

public class NursePermissions implements PermissionStrategy {
    @Override
    public boolean canPerform(SystemAction action) {
        switch (action) {
            case VIEW_PATIENT_LIST:
            case BOOK_APPOINTMENT:
            case UPDATE_APPOINTMENT:
            case VIEW_ALL_APPOINTMENTS:
            case COMPLETE_APPOINTMENT:
            case CREATE_BILL:
                return true;
            default:
                return false;
        }
    }
}
