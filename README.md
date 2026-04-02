# GlobeMed Healthcare Management System 🏥

GlobeMed is a comprehensive, integrated desktop application designed to streamline healthcare operations across multiple facilities. Built using **Java Swing** and **Hibernate**, the system focuses on security, scalability, and efficient data management.

## 🚀 Key Features
- **Patient Management:** Secure handling of patient records and medical history.
- **Appointment Scheduling:** Real-time booking with conflict management across different locations.
- **Billing & Insurance:** Decoupled billing process with automated insurance claim handling.
- **Medical Reporting:** Detailed patient summary reports with printing functionality.
- **Role-Based Security:** Advanced access control for Admins, Doctors, and Nurses.

## 🏗️ Design Patterns Implemented
The core strength of this system lies in its architectural integrity, achieved through the strategic use of 5 major Design Patterns:

1.  **Strategy Pattern:** Used for centralized **Role-Based Access Control (RBAC)**.
2.  **Mediator Pattern:** Handles complex **Appointment Scheduling** logic to decouple the UI from data services.
3.  **Chain of Responsibility:** Processes **Billing and Insurance Claims** through a flexible, multi-step chain.
4.  **Visitor Pattern:** Separates **Report Generation** logic from core medical data objects for better maintainability.
5.  **Decorator Pattern:** Implements transparent **Action Logging** and auditing layers without modifying existing DAOs.

## 🛠️ Tech Stack
- **Language:** Java
- **UI Framework:** Java Swing (with FlatLaf)
- **ORM:** Hibernate
- **Database:** MySQL
- **IDE:** NetBeans

---
Developed as a demonstration of advanced Software Engineering principles and Design Patterns.
