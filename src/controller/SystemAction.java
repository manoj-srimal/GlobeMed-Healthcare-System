package controller;

public enum SystemAction {
    // Patient Management 
    CREATE_PATIENT,
    UPDATE_PATIENT,
    DELETE_PATIENT,
    VIEW_PATIENT_LIST,

    // Appointment Management 
    BOOK_APPOINTMENT,
    UPDATE_APPOINTMENT,
    CANCEL_APPOINTMENT,
    VIEW_ALL_APPOINTMENTS,
    VIEW_OWN_APPOINTMENTS,
    COMPLETE_APPOINTMENT,

    // User Management 
    CREATE_USER,
    UPDATE_USER,
    DELETE_USER,
    VIEW_USER_LIST,
    
    // Billing Actions
    CREATE_BILL,
    PROCESS_INSURANCE_CLAIM,
    APPROVE_REJECT_CLAIM
}
