package controller;

import javax.swing.JOptionPane;
import model.User;


public class AuthorizationContext {

    private final PermissionStrategy strategy;
    private final User loggedInUser;

    public AuthorizationContext(User loggedInUser) {
        this.loggedInUser = loggedInUser;
        this.strategy = PermissionStrategyFactory.getStrategy(loggedInUser.getRole());
    }

    public boolean checkPermission(SystemAction action) {
        if (strategy != null && strategy.canPerform(action)) {
            return true;
        } else {
            JOptionPane.showMessageDialog(null,
                "Access Denied: Your role ('" + loggedInUser.getRole() + "') does not have permission to perform this action.",
                "Security Warning",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public boolean hasPermission(SystemAction action) {
        if (strategy != null) {
            return strategy.canPerform(action);
        }
        return false;
    }
    
    public User getLoggedInUser() {
        return loggedInUser;
    }
}
