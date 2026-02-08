package evaluator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import misc.UserDatabase;

public class AssignGradeFrame extends JFrame {
    private String evaluatorId;
    private String sessionId;
    private String slot;
    private String studentId;
    private String studentName;
    private String boardId;
    private GradeStudentsFrame parent;
    
    private JTable rubricsTable;
    private DefaultTableModel rubricsModel;
    private JButton saveButton;

    private boolean alreadyGraded = false;
    
    public AssignGradeFrame(String evaluatorId, String sessionId, String slot, 
                          String studentId, String studentName, String boardId, 
                          GradeStudentsFrame parent) {
        this.evaluatorId = evaluatorId;
        this.sessionId = sessionId;
        this.slot = slot;
        this.studentId = studentId;
        this.studentName = studentName;
        this.boardId = boardId;
        this.parent = parent;
        
        setTitle("Assign Grade - " + studentName + " (" + studentId + ")");
        setSize(800, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Grade Assignment for: " + studentName);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel infoPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        infoPanel.add(new JLabel("Student ID: " + studentId));
        infoPanel.add(new JLabel("Board ID: " + boardId));
        infoPanel.add(new JLabel("Session: " + sessionId));
        infoPanel.add(new JLabel("Time Slot: " + slot));
        infoPanel.add(new JLabel("Evaluator: " + evaluatorId));
        infoPanel.add(new JLabel(""));
        
        headerPanel.add(infoPanel, BorderLayout.CENTER);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] columns = new String[12];
        columns[0] = "Rubric Name";
        for (int i = 1; i <= 10; i++) {
            columns[i] = String.valueOf(i);
        }
        columns[11] = "Selected";
        
        rubricsModel = new DefaultTableModel(columns, 4) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return !alreadyGraded && column >= 1 && column <= 10;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 11) return String.class;
                return column >= 1 && column <= 10 ? Boolean.class : String.class;
            }
        };
        
        String[] rubricNames = {
            "Problem Clarity & Research Question",
            "Methodology & Approach",
            "Results & Analysis",
            "Presentation & Communication"
        };
        
        for (int i = 0; i < 4; i++) {
            rubricsModel.setValueAt(rubricNames[i], i, 0);
            for (int j = 1; j <= 10; j++) {
                rubricsModel.setValueAt(false, i, j);
            }
            rubricsModel.setValueAt("Not selected", i, 11);
        }
        
        rubricsTable = new JTable(rubricsModel);
        rubricsTable.setRowHeight(40);
        
        for (int col = 1; col <= 10; col++) {
            rubricsTable.getColumnModel().getColumn(col).setCellRenderer(new RadioButtonRenderer());
            rubricsTable.getColumnModel().getColumn(col).setCellEditor(new RadioButtonEditor());
        }
        
        rubricsTable.getColumnModel().getColumn(11).setCellRenderer(new SelectedRenderer());
        
        rubricsModel.addTableModelListener(e -> {
            if (e.getColumn() >= 1 && e.getColumn() <= 10) {
                int row = e.getFirstRow();
                Boolean isSelected = (Boolean) rubricsModel.getValueAt(row, e.getColumn());
                if (isSelected != null && isSelected) {
                    rubricsModel.setValueAt("Score: " + e.getColumn(), row, 11);
                }
            }
        });
        
        loadExistingGradeIfAny();
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton backButton = new JButton("Back");
        saveButton = new JButton("Save Grade");
        
        backButton.addActionListener(e -> {
            dispose();
            parent.setVisible(true);
        });
        
        saveButton.addActionListener(e -> saveGrade());
        
        if (alreadyGraded) {
            saveButton.setEnabled(false);
        }
        
        buttonPanel.add(backButton);
        buttonPanel.add(saveButton);
        
        add(headerPanel, BorderLayout.NORTH);
        add(new JScrollPane(rubricsTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    private void loadExistingGradeIfAny() {
        try (BufferedReader br = new BufferedReader(new FileReader("./src/saved/grades.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                
                if (data.length < 9) continue;
                
                String gSession = data[0];
                String gStudent = data[1];
                String gEvaluator = data[2];
                String gSlot = data[3];
                
                if (gSession.equals(sessionId) &&
                    gStudent.equals(studentId) &&
                    gEvaluator.equals(evaluatorId) &&
                    gSlot.equals(slot)) {
                    
                    alreadyGraded = true;
                    
                    for (int i = 0; i < 4; i++) {
                        int score = Integer.parseInt(data[4 + i]);
                        rubricsModel.setValueAt(true, i, score);
                        rubricsModel.setValueAt("Score: " + score, i, 11);
                    }
                    
                    JOptionPane.showMessageDialog(this,
                        "This student has already been graded.\nEditing is disabled.",
                        "Already Graded",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    break;
                }
            }
        } catch (Exception e) {
            
        }
    }
    
    private void saveGrade() {
        int[] rubrics = new int[4];
        int totalGrade = 0;
        boolean allScored = true;
        
        for (int row = 0; row < 4; row++) {
            int score = 0;
            for (int col = 1; col <= 10; col++) {
                Boolean isSelected = (Boolean) rubricsModel.getValueAt(row, col);
                if (isSelected != null && isSelected) {
                    score = col;
                    break;
                }
            }
            
            if (score == 0) {
                allScored = false;
                break;
            }
            
            rubrics[row] = score;
            totalGrade += score;
        }
        
        if (!allScored) {
            JOptionPane.showMessageDialog(this, 
                "Please assign a score (1-10) for all rubric criteria.",
                "Incomplete Grading",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        UserDatabase.saveGrade(sessionId, studentId, evaluatorId, slot, rubrics, totalGrade);
        
        JOptionPane.showMessageDialog(this,
            "Grade saved successfully!\nTotal Grade: " + totalGrade + "/40",
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
        
        dispose();
        parent.setVisible(true);
        parent.refreshStudents();
    }
    
    class RadioButtonRenderer implements javax.swing.table.TableCellRenderer {
        private JRadioButton radioButton = new JRadioButton();
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            radioButton.setSelected(value instanceof Boolean && (Boolean) value);
            radioButton.setHorizontalAlignment(SwingConstants.CENTER);
            radioButton.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return radioButton;
        }
    }
    
    class RadioButtonEditor extends DefaultCellEditor {
        private JRadioButton radioButton;
        private int currentRow;
        private int currentColumn;

        public RadioButtonEditor() {
            super(new JCheckBox());
            radioButton = new JRadioButton();
            radioButton.setHorizontalAlignment(SwingConstants.CENTER);

            radioButton.addActionListener(e -> {
                for (int col = 1; col <= 10; col++) {
                    rubricsModel.setValueAt(false, currentRow, col);
                }
                rubricsModel.setValueAt(true, currentRow, currentColumn);
                fireEditingStopped();
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            currentColumn = column;
            radioButton.setSelected(true);
            return radioButton;
        }

        public Object getCellEditorValue() {
            return true;
        }
    }
    
    class SelectedRenderer extends JLabel implements javax.swing.table.TableCellRenderer {
        public SelectedRenderer() {
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Arial", Font.BOLD, 12));
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            
            if (value != null && value.toString().startsWith("Score: ")) {
                setForeground(new Color(0, 100, 0));
            } else {
                setForeground(Color.RED);
            }
            
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }
}
