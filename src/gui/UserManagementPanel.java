package gui;

import dao.LocationDao;
import dao.UserDao;
import javax.swing.JPanel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import model.Location;
import model.User;


public class UserManagementPanel extends JPanel {

    private JTextField txtFullName, txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> roleComboBox;
    private JList<Location> locationList; 
    private JButton btnSave, btnUpdate, btnClear;
    private JTable userTable;
    private DefaultTableModel tableModel;

    private UserDao userDao;
    private LocationDao locationDao; 
    private int selectedUserId = 0;

    public UserManagementPanel() {
        this.userDao = new UserDao();
        this.locationDao = new LocationDao(); 
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(createFormPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadLocations(); 
        refreshUsersTable();
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("User Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; txtFullName = new JTextField(20); formPanel.add(txtFullName, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 3; txtUsername = new JTextField(20); formPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; txtPassword = new JPasswordField(20); formPanel.add(txtPassword, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 3; 
        roleComboBox = new JComboBox<>(new String[]{"Doctor", "Nurse", "Admin"});
        formPanel.add(roleComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Assigned Locations:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        locationList = new JList<>();
        locationList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // Allow multiple selections
        JScrollPane locationScrollPane = new JScrollPane(locationList);
        locationScrollPane.setPreferredSize(new Dimension(200, 80));
        formPanel.add(locationScrollPane, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnSave = new JButton("Save New User");
        btnUpdate = new JButton("Update Selected User");
        btnClear = new JButton("Clear Form");
        buttonPanel.add(btnSave);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);

        btnSave.addActionListener(e -> saveUser());
        btnUpdate.addActionListener(e -> updateUser());
        btnClear.addActionListener(e -> clearForm());

        return formPanel;
    }

    private JScrollPane createTablePanel() {
        String[] columnNames = {"User ID", "Full Name", "Username", "Role"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        userTable = new JTable(tableModel);

        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && userTable.getSelectedRow() > -1) {
                populateFormFromSelectedRow();
            }
        });

        return new JScrollPane(userTable);
    }
    
    private void loadLocations() {
        List<Location> locations = locationDao.getAllLocations();
        if (locations != null) {
            DefaultListModel<Location> listModel = new DefaultListModel<>();
            for (Location loc : locations) {
                listModel.addElement(loc);
            }
            locationList.setModel(listModel);
            locationList.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Location) {
                        setText(((Location) value).getName());
                    }
                    return this;
                }
            });
        }
    }

    private void refreshUsersTable() {
        tableModel.setRowCount(0);
        List<User> users = userDao.getAllUsers();
        if (users != null) {
            for (User user : users) {
                Vector<Object> row = new Vector<>();
                row.add(user.getUserId());
                row.add(user.getFullName());
                row.add(user.getUsername());
                row.add(user.getRole());
                tableModel.addRow(row);
            }
        }
    }

    private void populateFormFromSelectedRow() {
        int selectedRow = userTable.getSelectedRow();
        selectedUserId = (int) tableModel.getValueAt(selectedRow, 0);
        txtFullName.setText((String) tableModel.getValueAt(selectedRow, 1));
        txtUsername.setText((String) tableModel.getValueAt(selectedRow, 2));
        roleComboBox.setSelectedItem(tableModel.getValueAt(selectedRow, 3));
        txtPassword.setText("");
        btnSave.setEnabled(false);

        User selectedUser = userDao.getUserById(selectedUserId);
        locationList.clearSelection();
        if (selectedUser != null) {
            ListModel<Location> model = locationList.getModel();
            for (int i = 0; i < model.getSize(); i++) {
                Location listLocation = model.getElementAt(i);
                for (Location userLocation : selectedUser.getLocations()) {
                    if (listLocation.getLocationId() == userLocation.getLocationId()) {
                        locationList.addSelectionInterval(i, i);
                        break;
                    }
                }
            }
        }
    }

    private void clearForm() {
        selectedUserId = 0;
        txtFullName.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        roleComboBox.setSelectedIndex(0);
        locationList.clearSelection();
        userTable.clearSelection();
        btnSave.setEnabled(true);
    }

    private void saveUser() {
        String fullName = txtFullName.getText();
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        if (fullName.trim().isEmpty() || username.trim().isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setRole(role);

        Set<Location> selectedLocations = new HashSet<>(locationList.getSelectedValuesList());
        newUser.setLocations(selectedLocations);

        if (userDao.saveUser(newUser)) {
            JOptionPane.showMessageDialog(this, "User created successfully!");
            refreshUsersTable();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create user. Username might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUser() {
        if (selectedUserId == 0) {
            JOptionPane.showMessageDialog(this, "Please select a user from the table to update.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String fullName = txtFullName.getText();
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        User userToUpdate = new User();
        userToUpdate.setUserId(selectedUserId);
        userToUpdate.setFullName(fullName);
        userToUpdate.setUsername(username);
        userToUpdate.setRole(role);
        
        if (!password.isEmpty()) {
            userToUpdate.setPassword(password);
        }

        Set<Location> selectedLocations = new HashSet<>(locationList.getSelectedValuesList());
        userToUpdate.setLocations(selectedLocations);

        if (userDao.updateUser(userToUpdate)) {
            JOptionPane.showMessageDialog(this, "User updated successfully!");
            refreshUsersTable();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update user.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
    
