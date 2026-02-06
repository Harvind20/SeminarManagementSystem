import java.awt.*;
import javax.swing.*;

public class LoginFrame extends JFrame {

    private JTextField ID;
    private JPasswordField password;
    private JComboBox<String> role;

    private JPanel labelPanel, fieldPanel, buttonPanel, mainPanel;

    public LoginFrame() {
        ID = new JTextField(16);
        password = new JPasswordField(16);
        role = new JComboBox<>(new String[]{"Student", "Evaluator", "Coordinator"});

        JButton confirmation = new JButton("Login");
        confirmation.addActionListener(e -> verifyLogin());

        labelPanel = new JPanel();
        fieldPanel = new JPanel();
        buttonPanel = new JPanel();
        mainPanel = new JPanel();

        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        buttonPanel.setLayout(new FlowLayout());

        ID.setMaximumSize(ID.getPreferredSize());
        password.setMaximumSize(password.getPreferredSize());
        role.setMaximumSize(role.getPreferredSize());

        setTitle("FCSIT Seminar System - Login");
        setSize(400, 300);
        setLayout(new GridLayout(2, 1));
        setLocationRelativeTo(null);

        labelPanel.add(new JLabel("ID:"));
        labelPanel.add(new JLabel("Password:"));
        labelPanel.add(new JLabel("Login As:"));

        fieldPanel.add(ID);
        fieldPanel.add(password);
        fieldPanel.add(role);

        mainPanel.setLayout(new GridLayout(1, 2));
        mainPanel.add(labelPanel);
        mainPanel.add(fieldPanel);

        buttonPanel.add(confirmation);

        add(mainPanel);
        add(buttonPanel);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    public void verifyLogin() {
        String id = ID.getText();
        String pw = new String(password.getPassword());
        String selectedRole = role.getSelectedItem().toString();

        if (UserDatabase.verifyLogin(id, pw, selectedRole)) {
            JOptionPane.showMessageDialog(this, "Login Successful");

            switch (selectedRole) {
                case "Student":
                    new StudentDashboard(id);
                    break;
                case "Coordinator":
                    new CoordinatorDashboard(id);
                    break;
                case "Evaluator":
                    JOptionPane.showMessageDialog(this, "Evaluator Dashboard under construction.");
                    break;
            }

            dispose();

        } else {
            JOptionPane.showMessageDialog(this, "Invalid ID or Password. Please try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}