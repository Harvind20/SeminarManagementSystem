import java.awt.*;
import javax.swing.*;
public class LoginFrame extends JFrame {
    private JTextField ID,name;
    private JComboBox<String> role;
    private JPanel idFrame,nameFrame,roleFrame,buttonFrame;

    public LoginFrame(){
        ID = new JTextField(16);
        name = new JTextField(16);
        role = new JComboBox<>(new String[]{"Student","Evaluator","Coordinator"});
        JButton confirmation = new JButton("Login");
        idFrame = new JPanel(); nameFrame = new JPanel();
        roleFrame = new JPanel(); buttonFrame = new JPanel();
        
        setTitle("Login");
        setSize(400,450);
        setLayout(new GridLayout(4,1));
        idFrame.setLayout(new FlowLayout());
        nameFrame.setLayout(new FlowLayout());
        roleFrame.setLayout(new FlowLayout());
        buttonFrame.setLayout(new FlowLayout());

        idFrame.add(new JLabel("ID:")); idFrame.add(ID);
        nameFrame.add(new JLabel("Name:")); nameFrame.add(name);
        roleFrame.add(new JLabel("Login as:")); roleFrame.add(role);buttonFrame.add(confirmation);

        add(idFrame);add(nameFrame);add(roleFrame);add(buttonFrame);
        setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    }
    public static void main(String[] args){
        new LoginFrame();
    }
}
