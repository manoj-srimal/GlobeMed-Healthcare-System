package controller;

import model.Bill;

public abstract class BillingHandler {
    
    protected BillingHandler nextHandler;

    public void setNext(BillingHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
    public abstract void processBill(Bill bill);
}
