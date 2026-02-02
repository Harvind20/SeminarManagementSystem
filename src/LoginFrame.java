import java.awt.*;
import javax.swing.*;
public class LoginFrame extends JFrame {
    private JTextField ID,name;
    private JComboBox<String> role;
    /*private JPanel idFrame,nameFrame,roleFrame,buttonFrame;*/
    private JPanel labelPanel, fieldPanel, buttonPanel, mainPanel;
    private BoxLayout labelLayout;
    private BoxLayout fieldLayout;

    public LoginFrame(){
        ID = new JTextField(16);
        name = new JTextField(16);
        role = new JComboBox<>(new String[]{"Student","Evaluator","Coordinator"});
        JButton confirmation = new JButton("Login");
        labelPanel = new JPanel(); fieldPanel = new JPanel();
        buttonPanel = new JPanel();
        mainPanel = new JPanel();

        labelLayout = new BoxLayout(labelPanel,BoxLayout.Y_AXIS);
        fieldLayout = new BoxLayout(fieldPanel,BoxLayout.Y_AXIS);

        ID.setMaximumSize(ID.getPreferredSize());
        name.setMaximumSize(name.getPreferredSize());
        role.setMaximumSize(role.getPreferredSize());
        
        setTitle("Login");
        setSize(400,450);
        setLayout(new GridLayout(2,1));
        mainPanel.setLayout(new GridLayout(1,2));
        labelPanel.setLayout(labelLayout);
        fieldPanel.setLayout(fieldLayout);
        buttonPanel.setLayout(new FlowLayout(1));
        labelPanel.add(new JLabel("ID:"));
        labelPanel.add(new JLabel("Name:"));
        labelPanel.add(new JLabel("Login As:"));
        fieldPanel.add(ID);
        fieldPanel.add(name);
        fieldPanel.add(role);
        buttonPanel.add(confirmation);

        mainPanel.add(labelPanel); mainPanel.add(fieldPanel); 
        add(mainPanel); add(buttonPanel);
        setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    }

    public void verifyLogin(){}

    public static void main(String[] args){
        new LoginFrame();
    }
}
