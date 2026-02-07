package misc;
import java.awt.*;
import javax.swing.*;

public abstract class Dashboard extends JFrame {

    protected String userId;
    protected JPanel contentPanel; 

    public Dashboard(String userId, String title) {
        this.userId = userId;

        setTitle(title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout()); 
        add(contentPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutBtn = new JButton("Logout");
        
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true); 
        });

        footerPanel.add(logoutBtn);
        add(footerPanel, BorderLayout.SOUTH);

        buildDashboard();

        pack();
        setVisible(true);
    }

    protected abstract void buildDashboard();
}