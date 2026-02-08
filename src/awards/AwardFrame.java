package awards;

import java.awt.*;
import javax.swing.*;

public class AwardFrame extends JFrame {

    private JTextArea resultArea;

    public AwardFrame() {
        setTitle("Awards Module");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        resultArea = new JTextArea();
        resultArea.setEditable(false);

        JButton generateButton = new JButton("Generate Awards");
        generateButton.addActionListener(e -> generateAwards());

        add(new JScrollPane(resultArea), BorderLayout.CENTER);
        add(generateButton, BorderLayout.SOUTH);
    }

    private void generateAwards() {
        AwardCalculator calculator = new AwardCalculator();

        Award bestPoster = calculator.findBestPoster();
        Award bestPresenter = calculator.findBestPresenter();

        String output = "=== AWARD RESULTS ===\n\n";

        if (bestPoster != null) {
            output += "Best Poster:\n" + bestPoster + "\n\n";
        } else {
            output += "Best Poster: No data found\n\n";
        }

        if (bestPresenter != null) {
            output += "Best Presenter:\n" + bestPresenter + "\n";
        } else {
            output += "Best Presenter: No data found\n";
        }

        resultArea.setText(output);
    }

    public static void main(String[] args) {
        new AwardFrame().setVisible(true);
    }
}