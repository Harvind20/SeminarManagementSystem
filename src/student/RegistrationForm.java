package student;
import java.awt.*;
import javax.swing.*;

import misc.UserDatabase;

public class RegistrationForm extends JFrame {

    private JTextField idField, nameField, titleField, supervisorField;
    private JTextArea abstractArea;
    private JComboBox<String> typeBox, sessionBox;
    private Student currentStudent;

    public RegistrationForm(String userId) {
        this.currentStudent = UserDatabase.getStudentById(userId);

        if (currentStudent == null) {
            JOptionPane.showMessageDialog(this, "Error: Student not found in database.");
            dispose();
            return;
        }

        setTitle("Seminar Registration - " + currentStudent.getName());
        setSize(450, 550); // Increased height for new field
        setLayout(new GridLayout(8, 2, 10, 10)); // Changed to 8 rows
        setLocationRelativeTo(null);

        idField = new JTextField(currentStudent.getStudentId());
        idField.setEditable(false);
        
        nameField = new JTextField(currentStudent.getName());
        nameField.setEditable(false);

        String title = currentStudent.getResearchTitle().equals("None") ? "" : currentStudent.getResearchTitle();
        String supervisor = currentStudent.getSupervisor().equals("None") ? "" : currentStudent.getSupervisor();
        String absText = currentStudent.getAbstractText().equals("None") ? "" : currentStudent.getAbstractText();

        titleField = new JTextField(title);
        supervisorField = new JTextField(supervisor);
        abstractArea = new JTextArea(absText);
        abstractArea.setLineWrap(true);
        typeBox = new JComboBox<>(new String[]{"Oral Presentation", "Poster Presentation"});
        
        // Session selection combo box
        sessionBox = new JComboBox<>();
        loadAvailableSessions();
        sessionBox.setEnabled(true); // Enable for selection

        // If student already has a session, try to select it
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

        JButton submitBtn = new JButton("Submit Registration");

        add(new JLabel("Student ID:")); add(idField);
        add(new JLabel("Name:")); add(nameField);
        add(new JLabel("Research Title:")); add(titleField);
        add(new JLabel("Abstract:")); add(new JScrollPane(abstractArea));
        add(new JLabel("Supervisor Name:")); add(supervisorField);
        add(new JLabel("Presentation Type:")); add(typeBox);
        add(new JLabel("Select Session:")); add(sessionBox);
        add(new JLabel("")); add(submitBtn);

        submitBtn.addActionListener(e -> register());

        setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void loadAvailableSessions() {
        sessionBox.removeAllItems();
        sessionBox.addItem("Select a session...");
        
        java.util.List<String> sessions = UserDatabase.getAllAvailableSessions();
        if (sessions.isEmpty()) {
            sessionBox.addItem("No sessions available");
            sessionBox.setEnabled(false);
        } else {
            for (String session : sessions) {
                sessionBox.addItem(session);
            }
        }
    }

    private void register() {
        // Get selected session
        String selectedSessionItem = (String) sessionBox.getSelectedItem();
        
        if (selectedSessionItem == null || 
            selectedSessionItem.equals("Select a session...") || 
            selectedSessionItem.equals("No sessions available")) {
            JOptionPane.showMessageDialog(this, "Please select a valid session.");
            return;
        }
        
        // Extract session ID from the display string (format: "S1 - Session Name")
        String sessionId = selectedSessionItem.split(" - ")[0];

        // Validate session exists
        if (!UserDatabase.sessionExists(sessionId)) {
            JOptionPane.showMessageDialog(this, "Selected session no longer exists. Please choose another.");
            loadAvailableSessions();
            return;
        }

        Student updatedStudent = new Student(
            currentStudent.getStudentId(),
            currentStudent.getPassword(), 
            currentStudent.getName(),     
            titleField.getText(),
            abstractArea.getText(),
            supervisorField.getText(),
            typeBox.getSelectedItem().toString(),
            currentStudent.getSubmissionPath()
        );
        
        // Set the session ID
        updatedStudent.setSessionId(sessionId);

        if (RegistrationController.validateInput(updatedStudent)) {
            RegistrationController.registerStudent(updatedStudent);
            
            // Ask user if they want to upload file now
            int choice = JOptionPane.showConfirmDialog(this, 
                "Registration Saved!\nSession: " + selectedSessionItem + 
                "\nDo you want to upload your presentation file now?",
                "Upload File", JOptionPane.YES_NO_OPTION);
                
            if (choice == JOptionPane.YES_OPTION) {
                new UploadForm(updatedStudent, this);
            } else {
                dispose();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
        }
    }
}