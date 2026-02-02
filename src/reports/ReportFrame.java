import javax.swing.JFrame;
import javax.swing.JLabel;

public class ReportFrame extends JFrame {

    public ReportFrame() {
        setTitle("Reports Module");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(new JLabel("Reports module loaded successfully", JLabel.CENTER));
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        new ReportFrame().setVisible(true);
    }
}