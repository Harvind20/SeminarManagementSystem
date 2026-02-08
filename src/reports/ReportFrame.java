package reports;

import java.awt.*;
import javax.swing.*;

public class ReportFrame extends JFrame {

    private JTextArea reportArea;

    public ReportFrame() {
        setTitle("Reports Module");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        reportArea = new JTextArea();
        reportArea.setEditable(false);

        JButton generateButton = new JButton("Generate Report");
        generateButton.addActionListener(e -> generateReport());

        add(new JScrollPane(reportArea), BorderLayout.CENTER);
        add(generateButton, BorderLayout.SOUTH);
    }

    private void generateReport() {
        ReportGenerator generator = new ReportGenerator();
        Report report = generator.generateSampleReport();

        reportArea.setText(
                "Report ID: " + report.getReportId() + "\n" +
                "Title: " + report.getTitle() + "\n" +
                "Generated Date: " + report.getGeneratedDate() + "\n\n" +
                report.getContent()
        );
    }

    public static void main(String[] args) {
        new ReportFrame().setVisible(true);
    }
}