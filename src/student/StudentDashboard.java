package student;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import misc.Dashboard;
import misc.UserDatabase;

public class StudentDashboard extends Dashboard {
    
    private JTabbedPane tabbedPane;
    private JTable scheduleTable;
    private DefaultTableModel scheduleModel;
    
    // Registration tab components
    private JTextField idField, nameField, titleField, supervisorField, boardIdField;
    private JTextArea abstractArea;
    private JComboBox<String> sessionBox, typeBox;
    private JButton uploadBtn, submitBtn;
    private JLabel filePathLabel;
    private Student currentStudent;
    private String uploadedFilePath;

    public StudentDashboard(String studentId) {
        super(studentId, "Student Dashboard - FCSIT Seminar System");
        setSize(900, 600);
        setLocationRelativeTo(null);
    }

    @Override
    protected void buildDashboard() {
        currentStudent = UserDatabase.getStudentById(userId);
        if (currentStudent == null) {
            JOptionPane.showMessageDialog(this, "Error: Student not found.");
            dispose();
            return;
        }
        
        tabbedPane = new JTabbedPane();
        initViewScheduleTab();
        initRegistrationTab();
        
        contentPanel.add(tabbedPane, BorderLayout.CENTER);
    }
    
    private void initViewScheduleTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create table for schedule
        String[] columns = {"Session ID", "Session Name", "Date", "Time", "Venue", "Type", "Track"};
        scheduleModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        scheduleTable = new JTable(scheduleModel);
        scheduleTable.setRowHeight(25);
        
        // Load schedule
        loadSchedule();
        
        panel.add(new JScrollPane(scheduleTable), BorderLayout.CENTER);
        
        // Add refresh button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh Schedule");
        refreshBtn.addActionListener(e -> loadSchedule());
        buttonPanel.add(refreshBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("View Schedule", panel);
    }
    
    private void loadSchedule() {
        scheduleModel.setRowCount(0);
        
        File file = new File("./src/saved/sessions.txt");
        if (!file.exists()) {
            scheduleModel.addRow(new Object[]{"No schedule", "available", "", "", "", "", ""});
            return;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("Schedule:") || line.startsWith("----")) {
                    continue;
                }
                
                String[] parts = line.split("\\|");
                if (parts.length >= 9) {
                    scheduleModel.addRow(new Object[]{
                        parts[0],  // Session ID
                        parts[1],  // Session Name
                        parts[4],  // Date
                        parts[6] + " - " + parts[7],  // Time
                        parts[5],  // Venue
                        parts[2],  // Type
                        parts[3]   // Track
                    });
                }
            }
        } catch (Exception e) {
            scheduleModel.addRow(new Object[]{"Error", "loading schedule", "", "", "", "", ""});
        }
    }
    
    private void initRegistrationTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder(" Seminar Registration "));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        // Student ID (read-only)
        idField = new JTextField(currentStudent.getStudentId());
        idField.setEditable(false);
        idField.setBackground(new Color(240, 240, 240));
        
        // Name (read-only)
        nameField = new JTextField(currentStudent.getName());
        nameField.setEditable(false);
        nameField.setBackground(new Color(240, 240, 240));
        
        // Research Title
        titleField = new JTextField(currentStudent.getResearchTitle().equals("None") ? "" : currentStudent.getResearchTitle());
        
        // Abstract
        abstractArea = new JTextArea(currentStudent.getAbstractText().equals("None") ? "" : currentStudent.getAbstractText(), 4, 20);
        abstractArea.setLineWrap(true);
        JScrollPane abstractScroll = new JScrollPane(abstractArea);
        
        // Supervisor
        supervisorField = new JTextField(currentStudent.getSupervisor().equals("None") ? "" : currentStudent.getSupervisor());
        
        // Presentation Type
        typeBox = new JComboBox<>(new String[]{"Oral Presentation", "Poster Presentation"});
        if (!currentStudent.getPresentationType().equals("None")) {
            typeBox.setSelectedItem(currentStudent.getPresentationType());
        }
        
        // Session Selection
        sessionBox = new JComboBox<>();
        loadAvailableSessions();
        
        // Board ID
        boardIdField = new JTextField(currentStudent.getBoardId().equals("None") ? "" : currentStudent.getBoardId());
        
        // File upload section
        filePathLabel = new JLabel("No file uploaded");
        if (!currentStudent.getSubmissionPath().equals("None")) {
            filePathLabel.setText("Current: " + currentStudent.getSubmissionPath());
            uploadedFilePath = currentStudent.getSubmissionPath();
        }
        
        uploadBtn = new JButton("Upload/Change Materials");
        uploadBtn.addActionListener(e -> uploadFile());
        
        submitBtn = new JButton("Submit Registration");
        submitBtn.addActionListener(e -> submitRegistration());
        
        // Add form fields
        addFormField(formPanel, "Student ID:", idField, gbc);
        addFormField(formPanel, "Name:", nameField, gbc);
        addFormField(formPanel, "Research Title:", titleField, gbc);
        addFormField(formPanel, "Abstract:", abstractScroll, gbc);
        addFormField(formPanel, "Supervisor:", supervisorField, gbc);
        addFormField(formPanel, "Presentation Type:", typeBox, gbc);
        addFormField(formPanel, "Select Session:", sessionBox, gbc);
        addFormField(formPanel, "Board ID:", boardIdField, gbc);
        
        // File upload row
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Upload Materials:"), gbc);
        
        gbc.gridx = 1;
        JPanel uploadPanel = new JPanel(new BorderLayout(5, 5));
        uploadPanel.add(filePathLabel, BorderLayout.CENTER);
        uploadPanel.add(uploadBtn, BorderLayout.EAST);
        formPanel.add(uploadPanel, gbc);
        
        // Submit button row
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(submitBtn, gbc);
        
        panel.add(formPanel, BorderLayout.NORTH);
        
        // Add info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(new JLabel("Note: Board ID is required for poster presentations"));
        panel.add(infoPanel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("Register & Upload", panel);
    }
    
    private void addFormField(JPanel panel, String label, JComponent field, GridBagConstraints gbc) {
        gbc.gridx = 0;
        panel.add(new JLabel(label), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
        
        gbc.gridy++;
        gbc.weightx = 0.0;
    }
    
    private void loadAvailableSessions() {
        sessionBox.removeAllItems();
        sessionBox.addItem("-- Select a Session --");
        
        List<String> sessions = UserDatabase.getAllAvailableSessions();
        for (String session : sessions) {
            sessionBox.addItem(session);
        }
        
        // If student already registered for a session, select it
        if (!currentStudent.getSessionId().equals("None")) {
            String currentSession = currentStudent.getSessionId();
            for (int i = 0; i < sessionBox.getItemCount(); i++) {
                String item = sessionBox.getItemAt(i);
                if (item.startsWith(currentSession + " - ")) {
                    sessionBox.setSelectedIndex(i);
                    break;
                }
            }
        }
    }
    
    private void uploadFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Presentation File");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            String fileName = selectedFile.getName();
            
            // Validate file type
            if (!fileName.toLowerCase().endsWith(".pdf") && !fileName.toLowerCase().endsWith(".pptx")) {
                JOptionPane.showMessageDialog(this, 
                    "Only PDF and PPTX files are allowed.",
                    "Invalid File Type",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            uploadedFilePath = selectedFile.getAbsolutePath();
            filePathLabel.setText(fileName);
            
            JOptionPane.showMessageDialog(this,
                "File selected: " + fileName + "\nFile will be uploaded when you submit registration.",
                "File Selected",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void submitRegistration() {
        // Validate session selection
        String selectedSessionItem = (String) sessionBox.getSelectedItem();
        if (selectedSessionItem == null || selectedSessionItem.equals("-- Select a Session --")) {
            JOptionPane.showMessageDialog(this, "Please select a session.");
            return;
        }
        
        // Extract session ID
        String sessionId = selectedSessionItem.split(" - ")[0];
        
        // Validate session exists
        if (!UserDatabase.sessionExists(sessionId)) {
            JOptionPane.showMessageDialog(this, "Selected session no longer exists.");
            loadAvailableSessions();
            return;
        }
        
        // Validate board ID
        if (boardIdField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Board ID.");
            return;
        }
        
        // Create updated student object
        Student updatedStudent = new Student(
            currentStudent.getStudentId(),
            currentStudent.getPassword(),
            currentStudent.getName(),
            titleField.getText().trim(),
            abstractArea.getText().trim(),
            supervisorField.getText().trim(),
            (String) typeBox.getSelectedItem(),
            uploadedFilePath != null ? uploadedFilePath : currentStudent.getSubmissionPath()
        );
        
        updatedStudent.setSessionId(sessionId);
        updatedStudent.setBoardId(boardIdField.getText().trim());
        
        // Validate input
        if (RegistrationController.validateInput(updatedStudent)) {
            // Ask for confirmation
            int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm Registration:\n" +
                "Session: " + selectedSessionItem + "\n" +
                "Board ID: " + boardIdField.getText().trim() + "\n" +
                "File: " + (uploadedFilePath != null ? "Will be uploaded" : "No change"),
                "Confirm Registration",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                RegistrationController.registerStudent(updatedStudent);
                
                // Update current student reference
                currentStudent = updatedStudent;
                
                JOptionPane.showMessageDialog(this,
                    "Registration successful!\n" +
                    "Session: " + sessionId + "\n" +
                    "Board ID: " + boardIdField.getText().trim(),
                    "Registration Complete",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Refresh available sessions
                loadAvailableSessions();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "Please fill in all required fields:\n" +
                "- Research Title\n" +
                "- Abstract\n" +
                "- Supervisor\n" +
                "- Session Selection\n" +
                "- Board ID",
                "Incomplete Form",
                JOptionPane.WARNING_MESSAGE);
        }
    }
}