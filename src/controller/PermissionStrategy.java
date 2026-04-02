package controller;

public interface PermissionStrategy {
   
    boolean canPerform(SystemAction action);
}
