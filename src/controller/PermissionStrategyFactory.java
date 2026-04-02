package controller;

public class PermissionStrategyFactory {

    public static PermissionStrategy getStrategy(String role) {
        if (role == null) {
            return null;
        }

        switch (role.toLowerCase()) {
            case "admin":
                return new AdminPermissions();
            case "doctor":
                return new DoctorPermissions();
            case "nurse":
                return new NursePermissions();
            default:
                return action -> false;
        }
    }
}
