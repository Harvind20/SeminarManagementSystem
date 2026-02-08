package evaluator;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import misc.Dashboard;
import misc.UserDatabase;
import student.Student;

public class EvaluatorDashboard extends Dashboard {

    private CardLayout cardLayout;
    private JPanel mainContainer;
    
    // PAGE 1: SEMINAR LIST
    private JTable seminarTable;
    private DefaultTableModel seminarModel;
    
    // PAGE 2: STUDENT LIST
    private JPanel studentListPanel;
    private JTable studentTable;
    private DefaultTableModel studentModel;
    private JLabel studentListLabel;
    private String currentSessionId; 
    
    // PAGE 3: GRADING FORM
    private JPanel gradingPanel;
    private JLabel gradingLabel;
    private JTable rubricTable;
    private DefaultTableModel rubricModel;
    private JTextArea commentsArea; // NEW: Comment Box
    private Student targetStudent;   
    
    private Evaluator currentEvaluator;

    public EvaluatorDashboard(String userId) {
        super(userId, "Evaluator Dashboard");
        setSize(1000, 600);
        setLocationRelativeTo(null);
    }

    @Override
    protected void buildDashboard() {
        currentEvaluator = UserDatabase.getEvaluatorById(userId);
        if(currentEvaluator == null) {
            JOptionPane.showMessageDialog(this, "Error: Profile not found.");
            dispose(); return;
        }

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        initSeminarListPage();
        initStudentListPage();
        initGradingPage();

        contentPanel.add(mainContainer, BorderLayout.CENTER);
    }

    private void initSeminarListPage() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(new JLabel("Your Assigned Seminars"), BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Time", "My Assigned Slots", "Action"};
        seminarModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 4; } 
        };
        seminarTable = new JTable(seminarModel);
        seminarTable.setRowHeight(60); 
        seminarTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        seminarTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        seminarTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        seminarTable.getColumnModel().getColumn(3).setPreferredWidth(400);

        for (String sessId : currentEvaluator.getAssignedSessions()) {
            String[] details = UserDatabase.getSessionDetails(sessId);
            String slotDetails = UserDatabase.getEvaluatorSlotDetails(sessId, currentEvaluator.getEvaluatorId());
            seminarModel.addRow(new Object[]{ sessId, details[0], details[1], slotDetails, "View" });
        }

        seminarTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        seminarTable.getColumn("Action").setCellEditor(new ViewButtonEditor(new JCheckBox()));

        panel.add(new JScrollPane(seminarTable), BorderLayout.CENTER);
        mainContainer.add(panel, "SEMINARS");
    }

    private void initStudentListPage() {
        studentListPanel = new JPanel(new BorderLayout(10, 10));
        studentListPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton("Back to Seminars");
        backBtn.addActionListener(e -> cardLayout.show(mainContainer, "SEMINARS"));
        studentListLabel = new JLabel("Students");
        studentListLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        header.add(backBtn);
        header.add(studentListLabel);
        studentListPanel.add(header, BorderLayout.NORTH);

        String[] cols = {"Student ID", "Name", "Time Slot", "Total Grade", "Action"};
        studentModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 4; }
        };
        studentTable = new JTable(studentModel);
        studentTable.setRowHeight(30);
        
        studentTable.getColumn("Action").setCellRenderer(new ActionButtonRenderer());
        studentTable.getColumn("Action").setCellEditor(new ActionButtonEditor(new JCheckBox()));

        studentListPanel.add(new JScrollPane(studentTable), BorderLayout.CENTER);
        mainContainer.add(studentListPanel, "STUDENTS");
    }

    private void loadStudentList(String sessionId) {
        currentSessionId = sessionId;
        studentListLabel.setText("All Registered Students for Session: " + sessionId);
        studentModel.setRowCount(0);
        
        List<Student> students = UserDatabase.getStudentsBySession(sessionId);
        
        for (Student s : students) {
            double[] avgs = UserDatabase.getStudentAverages(s.getStudentId());
            String gradeDisplay = (avgs != null) ? String.format("%.2f", avgs[4]) : "Ungraded";
            String slotTime = UserDatabase.getStudentSlotTime(sessionId, s.getStudentId());

            String actionState = "Unassigned";
            if (UserDatabase.isEvaluatorAssignedToStudent(sessionId, s.getStudentId(), userId)) {
                if (UserDatabase.hasEvaluatorGraded(s.getStudentId(), userId)) {
                    actionState = "Edit";
                } else {
                    actionState = "Grade";
                }
            }

            studentModel.addRow(new Object[]{
                s.getStudentId(), s.getName(), slotTime, gradeDisplay, actionState
            });
        }
    }

    private void initGradingPage() {
        gradingPanel = new JPanel(new BorderLayout(10, 10));
        gradingPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton("Back to List");
        backBtn.addActionListener(e -> {
            loadStudentList(currentSessionId);
            cardLayout.show(mainContainer, "STUDENTS");
        });
        gradingLabel = new JLabel("Grading Student");
        gradingLabel.setFont(new Font("Arial", Font.BOLD, 16));
        header.add(backBtn);
        header.add(gradingLabel);
        gradingPanel.add(header, BorderLayout.NORTH);

        String[] cols = new String[11];
        cols[0] = "Rubric Criteria";
        for(int i=1; i<=10; i++) cols[i] = String.valueOf(i);

        rubricModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col > 0; }
            @Override public Class<?> getColumnClass(int col) { return col == 0 ? String.class : Boolean.class; }
        };
        rubricTable = new JTable(rubricModel);
        rubricTable.setRowHeight(30);
        rubricTable.getColumnModel().getColumn(0).setPreferredWidth(200);

        rubricTable.getModel().addTableModelListener(e -> {
            if (e.getColumn() > 0) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                Boolean checked = (Boolean) rubricModel.getValueAt(row, col);
                if (checked != null && checked) {
                    for (int c = 1; c <= 10; c++) if (c != col) rubricModel.setValueAt(false, row, c);
                }
            }
        });

        gradingPanel.add(new JScrollPane(rubricTable), BorderLayout.CENTER);

        // --- NEW: COMMENTS SECTION ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        commentsArea = new JTextArea(4, 50);
        commentsArea.setLineWrap(true);
        JScrollPane commentScroll = new JScrollPane(commentsArea);
        commentScroll.setBorder(BorderFactory.createTitledBorder("Comments & Feedback"));
        
        JButton saveBtn = new JButton("Save Grades");
        saveBtn.setPreferredSize(new Dimension(150, 40));
        saveBtn.addActionListener(e -> saveGrades());
        
        bottomPanel.add(commentScroll, BorderLayout.CENTER);
        bottomPanel.add(saveBtn, BorderLayout.SOUTH);
        
        gradingPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainContainer.add(gradingPanel, "GRADING");
    }

    private void openGradingForm(String studentId) {
        targetStudent = UserDatabase.getStudentById(studentId);
        gradingLabel.setText("Grading: " + targetStudent.getName() + " (" + targetStudent.getStudentId() + ")");
        
        rubricModel.setRowCount(0);
        String[] criteria = {"Problem Clarity", "Methodology", "Results", "Presentation"};
        
        int[] existingScores = UserDatabase.getEvaluatorGrades(studentId, userId);
        
        // NEW: Load existing comments
        String existingComments = UserDatabase.getEvaluatorComments(studentId, userId);
        commentsArea.setText(existingComments);
        
        for (int r = 0; r < criteria.length; r++) {
            Object[] row = new Object[11];
            row[0] = criteria[r];
            for(int i=1; i<=10; i++) {
                if (existingScores != null && existingScores[r] == i) row[i] = true;
                else row[i] = false;
            }
            rubricModel.addRow(row);
        }
        
        cardLayout.show(mainContainer, "GRADING");
    }

    private void saveGrades() {
        int[] scores = new int[4];
        for (int row = 0; row < 4; row++) {
            int score = 0;
            for (int col = 1; col <= 10; col++) {
                if ((Boolean) rubricModel.getValueAt(row, col)) { score = col; break; }
            }
            if (score == 0) {
                JOptionPane.showMessageDialog(this, "Please score all criteria.");
                return;
            }
            scores[row] = score;
        }
        
        // Save Scores AND Comments
        UserDatabase.saveGrade(currentSessionId, targetStudent.getStudentId(), userId, scores, commentsArea.getText());
        
        JOptionPane.showMessageDialog(this, "Grades Saved!");
        loadStudentList(currentSessionId);
        cardLayout.show(mainContainer, "STUDENTS");
    }

    // ============================================
    // RENDERERS & EDITORS
    // ============================================

    class ViewButtonEditor extends DefaultCellEditor {
        public ViewButtonEditor(JCheckBox c) { super(c); }
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            JButton b = new JButton("View");
            b.addActionListener(e -> {
                fireEditingStopped();
                loadStudentList(table.getValueAt(row, 0).toString());
                cardLayout.show(mainContainer, "STUDENTS");
            });
            return b;
        }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText((v==null)?"":v.toString()); return this;
        }
    }

    class ActionButtonRenderer extends JButton implements TableCellRenderer {
        public ActionButtonRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            String state = (v == null) ? "" : v.toString();
            setText(state);
            setEnabled(!state.equals("Unassigned"));
            
            if(state.equals("Grade")) setBackground(new Color(60, 179, 113)); // Green
            else if(state.equals("Edit")) setBackground(new Color(255, 165, 0)); // Orange
            else setBackground(Color.GRAY);
            
            setForeground(Color.WHITE);
            return this;
        }
    }

    class ActionButtonEditor extends DefaultCellEditor {
        private String label;
        public ActionButtonEditor(JCheckBox c) { super(c); }
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            label = (value == null) ? "" : value.toString();
            JButton b = new JButton(label);
            b.setEnabled(!label.equals("Unassigned"));
            b.addActionListener(e -> {
                fireEditingStopped();
                if(!label.equals("Unassigned")) {
                    openGradingForm(studentTable.getValueAt(studentTable.getSelectedRow(), 0).toString());
                }
            });
            return b;
        }
        public Object getCellEditorValue() { return label; }
    }
}