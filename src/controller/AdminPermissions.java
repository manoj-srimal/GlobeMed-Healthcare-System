package controller;

public class AdminPermissions implements PermissionStrategy {
    @Override
    public boolean canPerform(SystemAction action) {
        return true;
    }
}
