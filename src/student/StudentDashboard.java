package student;
import java.awt.*;
import javax.swing.*;

import misc.Dashboard;

public class StudentDashboard extends Dashboard {

    public StudentDashboard(String studentId) {
        super(studentId, "Student Dashboard");
        setSize(400, 300); 
        setLocationRelativeTo(null); 
    }

    @Override
    protected void buildDashboard() {
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton viewScheduleBtn = new JButton("View Seminar Schedule");
        JButton registerBtn = new JButton("Register & Upload Materials");
        JButton viewUploadBtn = new JButton("View Uploaded Materials");

        viewScheduleBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Schedule View Coming Soon"));

        registerBtn.addActionListener(e -> new RegistrationForm(userId));

        viewUploadBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Upload View Coming Soon");
        });

        buttonPanel.add(viewScheduleBtn);
        buttonPanel.add(registerBtn);
        buttonPanel.add(viewUploadBtn);

        contentPanel.add(buttonPanel, BorderLayout.CENTER);
    }
}