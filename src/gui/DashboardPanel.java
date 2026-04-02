package gui;

import dao.AppointmentDao;
import dao.BillDao;
import dao.PatientDao;
import dao.UserDao;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Vector;
import model.Patient;

public class DashboardPanel extends JPanel {

    public DashboardPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel mainTitle = new JLabel("System Overview");
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        mainTitle.setBorder(new EmptyBorder(0, 5, 15, 0));
        add(mainTitle, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;

        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        
        PatientDao patientDao = new PatientDao();
        AppointmentDao appointmentDao = new AppointmentDao();
        BillDao billDao = new BillDao();
        UserDao userDao = new UserDao();

        List<Patient> allPatients = patientDao.getAllPatients();
        int totalPatients = allPatients != null ? allPatients.size() : 0;
        long appointmentsToday = appointmentDao.countAppointmentsForToday();
        long pendingBills = billDao.countPendingBills();
        long totalDoctors = userDao.countDoctors();
        
        cardsPanel.add(createInfoCard("Total Patients", String.valueOf(totalPatients), new Color(0, 123, 255), new Color(4, 88, 180), new PatientIcon()));
        cardsPanel.add(createInfoCard("Appointments Today", String.valueOf(appointmentsToday), new Color(23, 162, 184), new Color(15, 125, 142), new AppointmentIcon()));
        cardsPanel.add(createInfoCard("Pending Bills", String.valueOf(pendingBills), new Color(220, 53, 69), new Color(172, 41, 54), new BillingIcon()));
        cardsPanel.add(createInfoCard("Available Doctors", String.valueOf(totalDoctors), new Color(40, 167, 69), new Color(31, 130, 54), new DoctorIcon()));
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.2; 
        mainContent.add(cardsPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 0.8; 
        mainContent.add(createRecentPatientsPanel(allPatients), gbc);

        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createInfoCard(String title, String value, Color startColor, Color endColor, Icon icon) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setOpaque(false);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        valueLabel.setForeground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(valueLabel);
        textPanel.add(titleLabel);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(iconLabel, BorderLayout.NORTH);

        return card;
    }

    private JPanel createRecentPatientsPanel(List<Patient> patients) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Recent Patients"));

        String[] columnNames = {"ID", "First Name", "Last Name", "Contact Number"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable recentPatientsTable = new JTable(tableModel);

        if (patients != null) {
            int count = 0;
            for (int i = patients.size() - 1; i >= 0 && count < 5; i--, count++) {
                Patient p = patients.get(i);
                Vector<Object> row = new Vector<>();
                row.add(p.getPatientId());
                row.add(p.getFirstName());
                row.add(p.getLastName());
                row.add(p.getContactNumber());
                tableModel.addRow(row);
            }
        }
        
        panel.add(new JScrollPane(recentPatientsTable), BorderLayout.CENTER);
        return panel;
    }

    private interface CardIcon extends Icon {
        @Override default int getIconWidth() { return 32; }
        @Override default int getIconHeight() { return 32; }
    }
    
    private static class PatientIcon implements CardIcon {
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 150));
            g2.setStroke(new BasicStroke(3));
            g2.drawOval(x + 8, y + 4, 16, 16);
            g2.drawArc(x + 2, y + 20, 28, 20, 180, 180);
            g2.dispose();
        }
    }
    
    private static class AppointmentIcon implements CardIcon {
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 150));
            g2.setStroke(new BasicStroke(3));
            g2.drawRect(x + 4, y + 6, 24, 22);
            g2.drawLine(x + 4, y + 13, x + 28, y + 13);
            g2.fillRect(x + 8, y + 17, 4, 4);
            g2.fillRect(x + 16, y + 17, 4, 4);
            g2.fillRect(x + 8, y + 23, 4, 4);
            g2.dispose();
        }
    }
    
    private static class BillingIcon implements CardIcon {
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 150));
            g2.setStroke(new BasicStroke(3));
            g2.drawRect(x + 6, y + 4, 20, 26);
            g2.drawLine(x + 10, y + 10, x + 22, y + 10);
            g2.drawLine(x + 10, y + 16, x + 22, y + 16);
            g2.drawLine(x + 10, y + 22, x + 16, y + 22);
            g2.dispose();
        }
    }
    
    private static class DoctorIcon implements CardIcon {
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 150));
            g2.setStroke(new BasicStroke(3));
            g2.drawOval(x + 8, y + 4, 16, 16);
            g2.drawLine(x + 16, y + 20, x + 16, y + 28);
            g2.drawLine(x + 10, y + 22, x + 22, y + 22);
            g2.dispose();
        }
    }
}
