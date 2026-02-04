import javax.swing.*;

public class StudentDashboard extends Dashboard {

    public StudentDashboard(String studentId) {
        super(studentId, "Student Dashboard");
    }

    @Override
    protected void buildDashboard() {

        JButton viewScheduleBtn = new JButton("View Seminar Schedule");
        JButton registerBtn = new JButton("Register & Upload Materials");
        JButton viewUploadBtn = new JButton("View Uploaded Materials");

        viewScheduleBtn.addActionListener(e ->
                new ViewScheduleForm());

        registerBtn.addActionListener(e ->
                new RegistrationForm());

        viewUploadBtn.addActionListener(e ->
                new ViewSubmissionForm(userId));

        mainPanel.add(viewScheduleBtn);
        mainPanel.add(registerBtn);
        mainPanel.add(viewUploadBtn);
    }
}
