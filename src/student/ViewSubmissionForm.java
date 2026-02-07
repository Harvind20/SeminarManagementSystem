package student;
import javax.swing.*;
import java.io.*;

public class ViewSubmissionForm extends JFrame {

    public ViewSubmissionForm(String studentId) {
        setTitle("My Uploaded Materials");
        setSize(400, 250); // Increased height

        JTextArea area = new JTextArea();
        area.setEditable(false);

        loadSubmission(studentId, area);

        add(new JScrollPane(area));
        setVisible(true);
    }

    private void loadSubmission(String studentId, JTextArea area) {
        try (BufferedReader br = new BufferedReader(
                new FileReader("students.txt"))) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");

                if (data[0].equals(studentId)) {
                    String sessionInfo = (data.length > 8 && !data[8].equals("None")) ? 
                        "Registered Session: " + data[8] : "No session registered";
                    
                    area.setText(
                        "Student ID: " + data[0] + "\n" +
                        "Presentation Type: " + data[2] + "\n" +
                        "Uploaded File: " + data[3] + "\n" +
                        sessionInfo
                    );
                    return;
                }
            }
            area.setText("No submission found.");

        } catch (IOException e) {
            area.setText("Error reading submission.");
        }
    }
}