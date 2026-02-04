import java.awt.*;
import javax.swing.*;

public class LoginFrame extends JDialog {

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

        setTitle("Login");
        setSize(400, 300);
        setLayout(new GridLayout(2, 1));

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
            }

            dispose();

        } else {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "User not found. Register as " + selectedRole + "?",
                    "Register",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {
                UserDatabase.registerUser(id, pw, selectedRole);
                JOptionPane.showMessageDialog(
                        this,
                        "Registration successful. Please login again."
                );
            }
        }
    }

    public static void main(String[] args) {
        new LoginFrame();
    }
}
