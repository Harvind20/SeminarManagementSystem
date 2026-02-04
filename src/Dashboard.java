import javax.swing.*;
import java.awt.*;

public abstract class Dashboard extends JFrame {

    protected String userId;
    protected JPanel mainPanel;

    public Dashboard(String userId, String title) {
        this.userId = userId;

        setTitle(title);
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(0, 1));

        add(mainPanel);

        // Let subclasses add buttons
        buildDashboard();

        // Common logout button
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        mainPanel.add(logoutBtn);

        setVisible(true);
    }

    // 🔥 Abstraction point
    protected abstract void buildDashboard();
}
