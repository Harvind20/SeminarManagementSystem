package evaluator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;

public class GradeStudentsFrame extends JFrame {
    private String evaluatorId;
    private String sessionId;
    private String slot;
    private String sessionName;
    private EvaluatorDashboard parent;
    
    private JTable studentsTable;
    private DefaultTableModel studentsModel;
    
    public GradeStudentsFrame(String evaluatorId, String sessionId, String slot, String sessionName, EvaluatorDashboard parent) {
        this.evaluatorId = evaluatorId;
        this.sessionId = sessionId;
        this.slot = slot;
        this.sessionName = sessionName;
        this.parent = parent;
        
        setTitle("Grade Students - " + sessionName + " (" + slot + ")");
        setSize(900, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Grade Students - " + sessionName);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JButton backButton = new JButton("Back to Dashboard");
        backButton.addActionListener(e -> {
            dispose();
            parent.setVisible(true);
        });
        headerPanel.add(backButton, BorderLayout.EAST);
        
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Session Information"));
        infoPanel.add(new JLabel("Session ID: " + sessionId));
        infoPanel.add(new JLabel("Time Slot: " + slot));
        infoPanel.add(new JLabel("Session: " + sessionName));
        infoPanel.add(new JLabel("Evaluator: " + evaluatorId));
        
        String[] columns = {"Student ID", "Student Name", "Slot", "Board ID", "Total Grade", "Grade"};
        studentsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 5) return JButton.class;
                return String.class;
            }
        };
        
        studentsTable = new JTable(studentsModel);
        studentsTable.setRowHeight(35);
        studentsTable.getColumn("Grade").setCellRenderer(new ButtonRenderer());
        studentsTable.getColumn("Grade").setCellEditor(new ButtonEditor(new JCheckBox()));
        
        loadStudents();
        
        add(headerPanel, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.CENTER);
        add(new JScrollPane(studentsTable), BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    public void loadStudents() {
        studentsModel.setRowCount(0);

        try (BufferedReader br = new BufferedReader(new FileReader("./src/saved/students.txt"))) {

            String line;

            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");

                if (data.length < 12) continue;

                String studentId = data[0];
                String name = data[2];
                String studentSession = data[8];
                String boardId = data[9];
                String evaluatorIds = data[10];
                String studentSlot = data[11];

                boolean correctSession = studentSession.equals(sessionId);

                boolean correctSlot = studentSlot.replace(" ", "")
                        .equals(slot.replace(" ", ""));

                boolean assignedToEvaluator =
                        evaluatorIds.equals(evaluatorId) ||
                        evaluatorIds.startsWith(evaluatorId + "&") ||
                        evaluatorIds.endsWith("&" + evaluatorId) ||
                        evaluatorIds.contains("&" + evaluatorId + "&");

                if (correctSession && correctSlot && assignedToEvaluator) {

                    String totalGrade = getGradeFromCSV(studentId);

                    Object[] rowData = {
                        studentId,
                        name,
                        studentSlot,
                        boardId,
                        totalGrade,
                        "Assign Grade"
                    };

                    studentsModel.addRow(rowData);
                }
            }

        } catch (Exception e) {
            System.out.println("Error loading students: " + e.getMessage());
        }

        if (studentsModel.getRowCount() == 0) {
            studentsModel.addRow(new Object[]{
                "No students assigned",
                "",
                slot,
                "",
                "",
                ""
            });
        }
    }

    private String getGradeFromCSV(String studentId) {
        try (BufferedReader br = new BufferedReader(new FileReader("./src/saved/grades.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 9) continue;

                if (data[0].equals(sessionId) &&
                    data[1].equals(studentId) &&
                    data[2].equals(evaluatorId) &&
                    data[3].equals(slot)) {

                    return data[8] + "/40";
                }
            }
        } catch (Exception e) {}

        return "Ungraded";
    }
    
    public void refreshStudents() {
        loadStudents();
    }
    
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setText("Assign Grade");
            setBackground(new Color(60, 180, 120));
            setForeground(Color.WHITE);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object val,
                boolean sel, boolean focus, int row, int col) {
            return this;
        }
    }
    
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private boolean clicked;
        private int selectedRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Assign Grade");
            button.addActionListener(e -> {
                clicked = true;
                fireEditingStopped();
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int col) {
            selectedRow = row;
            return button;
        }

        public Object getCellEditorValue() {
            if (clicked) {
                String studentId = (String) studentsTable.getValueAt(selectedRow, 0);
                String studentName = (String) studentsTable.getValueAt(selectedRow, 1);
                String boardId = (String) studentsTable.getValueAt(selectedRow, 3);

                new AssignGradeFrame(evaluatorId, sessionId, slot, studentId, studentName, boardId, GradeStudentsFrame.this);
            }
            clicked = false;
            return "Assign Grade";
        }
    }
}
