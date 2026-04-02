package gui;

import com.formdev.flatlaf.FlatDarkLaf;
import controller.AuthorizationContext;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import model.User;

public class DashboardUI extends JFrame {

    private User loggedInUser;
    private AuthorizationContext authContext;
    
    private JPanel mainContentPanel;
    private final Map<String, JButton> sidebarButtons = new HashMap<>();
    private final Color defaultBgColor = new Color(60, 63, 65);
    private final Color highlightBgColor = new Color(0, 123, 255);

    public DashboardUI(User loggedInUser) {
        this.loggedInUser = loggedInUser;
        this.authContext = new AuthorizationContext(loggedInUser);

        setTitle("GlobeMed - Main Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Failed to initialize LaF.");
        }

        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createMainContentPanel(), BorderLayout.CENTER);
        
        highlightSidebarButton(sidebarButtons.get("Dashboard"));
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(45, 45, 45));
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("GlobeMed Healthcare Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        String welcomeText = "Welcome, " + loggedInUser.getFullName() + " (" + loggedInUser.getRole() + ")";
        JLabel userLabel = new JLabel(welcomeText);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        userLabel.setForeground(Color.LIGHT_GRAY);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(new Color(51, 51, 51));
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        addSidebarButton("Dashboard", new DashboardIcon(), topPanel);
        addSidebarButton("Patient Management", new PatientIcon(), topPanel);
        addSidebarButton("Appointments", new AppointmentIcon(), topPanel);
        addSidebarButton("Billing", new BillingIcon(), topPanel);
        addSidebarButton("Reports", new ReportIcon(), topPanel);

        if ("Admin".equalsIgnoreCase(loggedInUser.getRole())) {
            topPanel.add(Box.createVerticalStrut(20));
            JSeparator separator = new JSeparator();
            topPanel.add(separator);
            topPanel.add(Box.createVerticalStrut(20));
            addSidebarButton("User Management", new UserManagementIcon(), topPanel);
        }
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton logoutButton = new JButton("Logout");
        logoutButton.setIcon(new LogoutIcon());
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.setIconTextGap(10);
        logoutButton.addActionListener(e -> {
            this.dispose();
            new LoginUI().setVisible(true);
        });
        bottomPanel.add(logoutButton);

        sidebar.add(topPanel, BorderLayout.NORTH);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addSidebarButton(String text, Icon icon, JPanel panel) {
        JButton button = new JButton(text);
        button.setIcon(icon);
        styleSidebarButton(button);
        
        button.addActionListener(e -> {
            highlightSidebarButton(button);
            CardLayout cl = (CardLayout)(mainContentPanel.getLayout());
            cl.show(mainContentPanel, text);
        });
        
        sidebarButtons.put(text, button);
        panel.add(button);
        panel.add(Box.createVerticalStrut(5));
    }
    
    private void styleSidebarButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(defaultBgColor);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setIconTextGap(15);
    }

    private void highlightSidebarButton(JButton selectedButton) {
        for (JButton button : sidebarButtons.values()) {
            button.setBackground(defaultBgColor);
        }
        if (selectedButton != null) {
            selectedButton.setBackground(highlightBgColor);
        }
    }

    private JPanel createMainContentPanel() {
        mainContentPanel = new JPanel(new CardLayout());
        
        JPanel dashboardView = new DashboardPanel();
        JPanel patientView = new PatientManagementPanel(this.authContext);
        JPanel appointmentView = new AppointmentSchedulingPanel(this.authContext);
        JPanel billingView = new BillingPanel(this.authContext);
        JPanel userManagementView = new UserManagementPanel();       
        JPanel reportsView = new MedicalRecordPanel(this.authContext);
        
        mainContentPanel.add(dashboardView, "Dashboard");
        mainContentPanel.add(patientView, "Patient Management");
        mainContentPanel.add(appointmentView, "Appointments");
        mainContentPanel.add(billingView, "Billing");
        mainContentPanel.add(reportsView, "Reports");
        mainContentPanel.add(userManagementView, "User Management");
        
        return mainContentPanel;
    }
    
    private abstract static class BaseIcon implements Icon {
        @Override
        public int getIconWidth() { return 18; } 
        @Override
        public int getIconHeight() { return 18; } 
    }

    private static class DashboardIcon extends BaseIcon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(x + 3, y + 8, 12, 8);
            int[] xPoints = {x + 1, x + 9, x + 17};
            int[] yPoints = {y + 8, y + 1, y + 8};
            g2.drawPolyline(xPoints, yPoints, 3);
            g2.dispose();
        }
    }

    private static class PatientIcon extends BaseIcon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(x + 5, y + 2, 8, 8);
            g2.drawArc(x + 2, y + 10, 14, 10, 180, 180);
            g2.dispose();
        }
    }
    
    private static class AppointmentIcon extends BaseIcon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(x + 2, y + 3, 14, 13);
            g2.drawLine(x + 2, y + 7, x + 16, y + 7);
            g2.drawLine(x + 9, y + 10, x + 9, y + 14);
            g2.drawLine(x + 9, y + 10, x + 13, y + 10);
            g2.dispose();
        }
    }
    
    private static class BillingIcon extends BaseIcon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(x + 3, y + 2, 12, 15);
            g2.drawLine(x + 6, y + 6, x + 9, y + 6);
            g2.drawLine(x + 6, y + 10, x + 12, y + 10);
            g2.drawLine(x + 6, y + 14, x + 12, y + 14);
            g2.dispose();
        }
    }
    
    private static class ReportIcon extends BaseIcon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3));
            g2.drawLine(x + 4, y + 15, x + 4, y + 8);
            g2.drawLine(x + 9, y + 15, x + 9, y + 4);
            g2.drawLine(x + 14, y + 15, x + 14, y + 11);
            g2.dispose();
        }
    }
    
    private static class UserManagementIcon extends BaseIcon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(x + 2, y + 3, 6, 6);
            g2.drawArc(x, y + 9, 10, 8, 180, 180);
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawOval(x + 9, y + 3, 6, 6);
            g2.drawArc(x + 7, y + 9, 10, 8, 180, 180);
            g2.dispose();
        }
    }

    private static class LogoutIcon extends BaseIcon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawArc(x + 3, y + 3, 12, 12, 45, 270);
            g2.drawLine(x + 9, y + 3, x + 9, y + 9);
            g2.dispose();
        }
    }
}

