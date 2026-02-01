import java.awt.*;
import javax.swing.*;

public class RegistrationForm extends JFrame {

    JTextField idField, nameField, titleField, supervisorField;
    JTextArea abstractArea;
    JComboBox<String> typeBox;

    public RegistrationForm() {
        setTitle("Student Registration");
        setSize(400, 450);
        setLayout(new GridLayout(7, 2));

        idField = new JTextField();
        nameField = new JTextField();
        titleField = new JTextField();
        supervisorField = new JTextField();
        abstractArea = new JTextArea();
        typeBox = new JComboBox<>(new String[]{"Oral", "Poster"});

        JButton submitBtn = new JButton("Register");

        add(new JLabel("Student ID")); add(idField);
        add(new JLabel("Name")); add(nameField);
        add(new JLabel("Research Title")); add(titleField);
        add(new JLabel("Abstract")); add(abstractArea);
        add(new JLabel("Supervisor")); add(supervisorField);
        add(new JLabel("Presentation Type")); add(typeBox);
        add(submitBtn);

        submitBtn.addActionListener(e -> register());

        setVisible(true);
    }

    private void register() {
    Student student = new Student(
        idField.getText(),
        nameField.getText(),
        titleField.getText(),
        abstractArea.getText(),
        supervisorField.getText(),
        typeBox.getSelectedItem().toString()
    );

    
    if (!RegistrationController.validateInput(student)) {
        JOptionPane.showMessageDialog(this, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    
    new UploadForm(student, this);
}

    public static void main(String[] args) {
    new RegistrationForm();
}

}
