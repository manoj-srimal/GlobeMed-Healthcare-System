package gui;

import com.formdev.flatlaf.FlatDarkLaf;
import dao.UserDao;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import model.User;

public class LoginUI extends JFrame {
    private int pX, pY;

    public LoginUI() {
        super("GlobeMed Login");
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Failed to initialize LaF.");
        }
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        mainPanel.add(createTitleBar(), BorderLayout.NORTH);
        mainPanel.add(createContentPanel(), BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(45, 45, 45));
        titleBar.setBorder(new EmptyBorder(5, 10, 5, 5));
        JLabel titleLabel = new JLabel("GlobeMed Login");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JButton closeButton = new JButton("X");
        closeButton.setForeground(Color.WHITE);
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.addActionListener(e -> System.exit(0));
        titleBar.add(titleLabel, BorderLayout.WEST);
        titleBar.add(closeButton, BorderLayout.EAST);
        titleBar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                pX = me.getX();
                pY = me.getY();
            }
        });
        titleBar.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent me) {
                setLocation(getLocation().x + me.getX() - pX, getLocation().y + me.getY() - pY);
            }
        });
        return titleBar;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 50, 40, 50));
        panel.setBackground(new Color(51, 51, 51));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        JLabel mainTitle = new JLabel("GlobeMed Healthcare");
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        mainTitle.setForeground(Color.WHITE);
        mainTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.insets = new Insets(0, 0, 5, 0);
        panel.add(mainTitle, gbc);
        JLabel subTitle = new JLabel("Please sign in to continue");
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subTitle.setForeground(Color.LIGHT_GRAY);
        subTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.insets = new Insets(0, 0, 30, 0);
        panel.add(subTitle, gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 0, 0, 0);
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userLabel.setForeground(Color.LIGHT_GRAY);
        panel.add(userLabel, gbc);
        JTextField userText = new JTextField();
        userText.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.insets = new Insets(2, 0, 10, 0);
        panel.add(userText, gbc);
        gbc.insets = new Insets(10, 0, 0, 0);
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        passLabel.setForeground(Color.LIGHT_GRAY);
        panel.add(passLabel, gbc);
        JPasswordField passText = new JPasswordField();
        passText.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.insets = new Insets(2, 0, 10, 0);
        panel.add(passText, gbc);
        gbc.insets = new Insets(20, 0, 0, 0);
        gbc.ipady = 10;
        JButton loginButton = new JButton("Sign In");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginButton.setBackground(new Color(66, 139, 202));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        
        loginButton.addActionListener(e -> {
            String username = userText.getText();
            String password = new String(passText.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and Password cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            UserDao userDao = new UserDao();
            User authenticatedUser = userDao.authenticateUser(username, password);

            if (authenticatedUser != null) {
                this.dispose();

                SwingUtilities.invokeLater(() -> {
                    new DashboardUI(authenticatedUser).setVisible(true);
                });

            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(loginButton, gbc);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}
