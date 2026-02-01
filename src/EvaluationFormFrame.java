import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EvaluationFormFrame extends JFrame {
    
    private JTextField evaluatorIdField;
    private JTextField studentIdField;
    private JComboBox<String> presentationTypeBox;
    private JSpinner problemClaritySpinner;
    private JSpinner methodologySpinner;
    private JSpinner resultsSpinner;
    private JSpinner presentationSpinner;
    private JTextArea commentsArea;
    
    public EvaluationFormFrame() {
        setTitle("Evaluation Form");
        setSize(500, 500);
        setLayout(new GridLayout(9, 2, 10, 10));
        
        evaluatorIdField = new JTextField();
        studentIdField = new JTextField();
        presentationTypeBox = new JComboBox<>(new String[]{"Oral", "Poster"});
        
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, 10, 1);
        problemClaritySpinner = new JSpinner(spinnerModel);
        methodologySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        resultsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        presentationSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        
        commentsArea = new JTextArea(3, 20);
        JScrollPane commentsScroll = new JScrollPane(commentsArea);
        
        JButton submitBtn = new JButton("Submit Evaluation");
        JButton cancelBtn = new JButton("Cancel");
        
        add(new JLabel("Evaluator ID:"));
        add(evaluatorIdField);
        add(new JLabel("Student ID:"));
        add(studentIdField);
        add(new JLabel("Presentation Type:"));
        add(presentationTypeBox);
        add(new JLabel("Problem Clarity (0-10):"));
        add(problemClaritySpinner);
        add(new JLabel("Methodology (0-10):"));
        add(methodologySpinner);
        add(new JLabel("Results (0-10):"));
        add(resultsSpinner);
        add(new JLabel("Presentation (0-10):"));
        add(presentationSpinner);
        add(new JLabel("Comments:"));
        add(commentsScroll);
        add(submitBtn);
        add(cancelBtn);
        
        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitEvaluation();
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
    
    private void submitEvaluation() {
        String evaluatorId = evaluatorIdField.getText().trim();
        String studentId = studentIdField.getText().trim();
        String presentationType = (String) presentationTypeBox.getSelectedItem();
        
        int problemClarity = (int) problemClaritySpinner.getValue();
        int methodology = (int) methodologySpinner.getValue();
        int results = (int) resultsSpinner.getValue();
        int presentation = (int) presentationSpinner.getValue();
        
        String comments = commentsArea.getText().trim();
        
        if (evaluatorId.isEmpty() || studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Evaluator ID and Student ID are required!", 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Evaluation evaluation;
        if (presentationType.equals("Poster")) {
            String posterBoardId = JOptionPane.showInputDialog(this, "Enter Poster Board ID:");
            if (posterBoardId == null || posterBoardId.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Poster Board ID is required for poster evaluations!", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            evaluation = new Evaluation(evaluatorId, studentId, posterBoardId,
                                       problemClarity, methodology, results, presentation, comments);
        } else {
            evaluation = new Evaluation(evaluatorId, studentId, problemClarity, methodology, 
                                       results, presentation, comments, presentationType);
        }
        
        evaluation.saveToFile();
        
        JOptionPane.showMessageDialog(this, "Evaluation submitted successfully!\nTotal Marks: " + 
                                     evaluation.getTotalMarks(), "Success", JOptionPane.INFORMATION_MESSAGE);
        
        dispose();
    }
    
    public static void main(String[] args) {
        new EvaluationFormFrame();
    }
}