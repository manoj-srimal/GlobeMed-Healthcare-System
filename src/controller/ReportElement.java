package controller;

public interface ReportElement {

    String accept(ReportVisitor visitor);
}