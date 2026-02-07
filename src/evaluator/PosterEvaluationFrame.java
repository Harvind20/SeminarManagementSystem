package evaluator;
import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PosterEvaluationFrame extends JFrame {
    
    private JTextField evaluatorIdField;
    private JTextField studentIdField;
    private JTextField posterBoardIdField;
    private JSpinner problemClaritySpinner;
    private JSpinner methodologySpinner;
    private JSpinner resultsSpinner;
    private JSpinner presentationSpinner;
    private JSpinner visualAppealSpinner; 
    private JSpinner claritySpinner; 
    private JTextArea commentsArea;
    
    public PosterEvaluationFrame() {
        setTitle("Poster Evaluation Form");
        setSize(600, 600);
        setLayout(new GridLayout(11, 2, 10, 10));
        
        evaluatorIdField = new JTextField();
        studentIdField = new JTextField();
        posterBoardIdField = new JTextField();
        
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, 10, 1);
        problemClaritySpinner = new JSpinner(spinnerModel);
        methodologySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        resultsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        presentationSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        visualAppealSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        claritySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        
        commentsArea = new JTextArea(3, 20);
        JScrollPane commentsScroll = new JScrollPane(commentsArea);
        
        JButton submitBtn = new JButton("Submit Poster Evaluation");
        JButton cancelBtn = new JButton("Cancel");
        
        add(new JLabel("Evaluator ID:"));
        add(evaluatorIdField);
        add(new JLabel("Student ID:"));
        add(studentIdField);
        add(new JLabel("Poster Board ID:"));
        add(posterBoardIdField);
        add(new JLabel("Problem Clarity (0-10):"));
        add(problemClaritySpinner);
        add(new JLabel("Methodology (0-10):"));
        add(methodologySpinner);
        add(new JLabel("Results (0-10):"));
        add(resultsSpinner);
        add(new JLabel("Presentation Quality (0-10):"));
        add(presentationSpinner);
        add(new JLabel("Visual Appeal (0-10):"));
        add(visualAppealSpinner);
        add(new JLabel("Clarity of Layout (0-10):"));
        add(claritySpinner);
        add(new JLabel("Comments:"));
        add(commentsScroll);
        add(submitBtn);
        add(cancelBtn);
        
        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitPosterEvaluation();
            }
        });
        
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void submitPosterEvaluation() {
        String evaluatorId = evaluatorIdField.getText().trim();
        String studentId = studentIdField.getText().trim();
        String posterBoardId = posterBoardIdField.getText().trim();
        
        int problemClarity = (int) problemClaritySpinner.getValue();
        int methodology = (int) methodologySpinner.getValue();
        int results = (int) resultsSpinner.getValue();
        int presentation = (int) presentationSpinner.getValue();
        int visualAppeal = (int) visualAppealSpinner.getValue();
        int clarity = (int) claritySpinner.getValue();
        
        String comments = commentsArea.getText().trim();
        
        if (evaluatorId.isEmpty() || studentId.isEmpty() || posterBoardId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!", 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String fullComments = comments + "\n[Poster Specific Scores - Visual Appeal: " + 
                            visualAppeal + ", Clarity: " + clarity + "]";
        
        Evaluation evaluation = new Evaluation(evaluatorId, studentId, posterBoardId,
                                             problemClarity, methodology, results, presentation, 
                                             fullComments);
        
        evaluation.saveToFile();
        
        int totalWithPosterScores = evaluation.getTotalMarks() + visualAppeal + clarity;
        
        JOptionPane.showMessageDialog(this, "Poster evaluation submitted successfully!\n" +
                                     "Standard Rubrics Total: " + evaluation.getTotalMarks() + "\n" +
                                     "With Poster Criteria: " + totalWithPosterScores + "/60", 
                                     "Success", JOptionPane.INFORMATION_MESSAGE);
        
        dispose();
    }
    
    public static void main(String[] args) {
        new PosterEvaluationFrame();
    }
}