package student;
import javax.swing.*;
import java.io.*;

public class ViewSubmissionForm extends JFrame {

    public ViewSubmissionForm(String studentId) {
        setTitle("My Uploaded Materials");
        setSize(400, 200);

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
                    area.setText(
                        "Student ID: " + data[0] + "\n" +
                        "Presentation Type: " + data[2] + "\n" +
                        "Uploaded File: " + data[3]
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
