package student;
import java.awt.*;
import javax.swing.*;

import misc.UserDatabase;

public class RegistrationForm extends JFrame {

    private JTextField idField, nameField, titleField, supervisorField;
    private JTextArea abstractArea;
    private JComboBox<String> typeBox;
    private Student currentStudent;

    public RegistrationForm(String userId) {
        this.currentStudent = UserDatabase.getStudentById(userId);

        if (currentStudent == null) {
            JOptionPane.showMessageDialog(this, "Error: Student not found in database.");
            dispose();
            return;
        }

        setTitle("Seminar Registration - " + currentStudent.getName());
        setSize(450, 500);
        setLayout(new GridLayout(7, 2, 10, 10));
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

        JButton submitBtn = new JButton("Submit Registration");

        add(new JLabel("Student ID:")); add(idField);
        add(new JLabel("Name:")); add(nameField);
        add(new JLabel("Research Title:")); add(titleField);
        add(new JLabel("Abstract:")); add(new JScrollPane(abstractArea));
        add(new JLabel("Supervisor Name:")); add(supervisorField);
        add(new JLabel("Presentation Type:")); add(typeBox);
        add(new JLabel("")); add(submitBtn);

        submitBtn.addActionListener(e -> register());

        setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void register() {
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

        if (RegistrationController.validateInput(updatedStudent)) {
            RegistrationController.registerStudent(updatedStudent);
            
            // Ask user if they want to upload file now
            int choice = JOptionPane.showConfirmDialog(this, 
                "Registration Saved! Do you want to upload your presentation file now?",
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