package coordinator;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import coordinator.Session.PresentationSlot;
import evaluator.Evaluator;
import misc.Dashboard;
import misc.UserDatabase;
import misc.UserDatabase.StudentGrade;
import student.Student;

public class CoordinatorDashboard extends Dashboard {

    private SessionManager sessionManager;
    private JTabbedPane tabbedPane;

    private JTable sessionsTable;
    private DefaultTableModel sessionsModel;
    
    private JTextField txtId, txtName, txtVenue;
    private JComboBox<String> cbType, cbTrack;
    private JSpinner spDate, spStart, spEnd, spDuration;

    private JComboBox<Session> cbSessionSelector;
    private JTable scheduleTable;
    private DefaultTableModel scheduleModel;
    
    // NEW: Components for Award Nomination tab
    private JTable seminarsTable;
    private DefaultTableModel seminarsModel;
    private JPanel awardMainPanel;
    private CardLayout awardCardLayout;

    public CoordinatorDashboard(String userId) {
        super(userId, "Coordinator Dashboard - FCSIT Seminar System");
        setSize(1100, 700); // Increased height for new tab
        setLocationRelativeTo(null);
    }

    @Override
    protected void buildDashboard() {
        sessionManager = new SessionManager();

        tabbedPane = new JTabbedPane();
        initSessionTab();   
        initScheduleTab();  
        initAwardNominationTab(); // NEW: Award Nomination tab

        contentPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    private void initSessionTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder(" Create New Session "));
        formPanel.setPreferredSize(new Dimension(350, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        txtId = new JTextField();
        txtId.setEditable(false); // Make ID field read-only
        txtId.setBackground(new Color(240, 240, 240));
        txtId.setText(sessionManager.generateSessionId()); // Auto-generate initial ID
        
        txtName = new JTextField();
        txtVenue = new JTextField("DTC Hall");
        
        cbType = new JComboBox<>(new String[]{"Oral Presentation", "Poster Presentation", "Demo Session"});
        cbTrack = new JComboBox<>(new String[]{"AI Track", "Data Science Track", "Cybersecurity Track", "Software Eng. Track", "Game Development Track"});

        spDate = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spDate, "yyyy-MM-dd");
        spDate.setEditor(dateEditor);
        spDate.setValue(new Date()); 

        spStart = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(spStart, "HH:mm");
        spStart.setEditor(startEditor);
        setSpinnerTime(spStart, 9, 0);

        spEnd = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(spEnd, "HH:mm");
        spEnd.setEditor(endEditor);
        setSpinnerTime(spEnd, 12, 0);

        spDuration = new JSpinner(new SpinnerNumberModel(20, 5, 120, 5));

        addFormField(formPanel, "Session ID:", txtId, gbc);
        addFormField(formPanel, "Session Name:", txtName, gbc);
        addFormField(formPanel, "Type:", cbType, gbc);
        addFormField(formPanel, "Track:", cbTrack, gbc);
        addFormField(formPanel, "Date:", spDate, gbc);
        addFormField(formPanel, "Venue:", txtVenue, gbc);
        addFormField(formPanel, "Start Time:", spStart, gbc);
        addFormField(formPanel, "End Time:", spEnd, gbc);
        addFormField(formPanel, "Duration (Mins):", spDuration, gbc);

        JButton btnCreate = new JButton("Create Session");
        btnCreate.setBackground(new Color(60, 120, 180));
        btnCreate.setForeground(Color.WHITE);
        
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        formPanel.add(btnCreate, gbc);

        // RIGHT SIDE: Table
        String[] cols = {"ID", "Name", "Type", "Date", "Venue", "Time"};
        sessionsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        sessionsTable = new JTable(sessionsModel);
        sessionsTable.setRowHeight(25);
        
        refreshSessionTable(); 

        btnCreate.addActionListener(e -> createSessionAction());

        panel.add(formPanel, BorderLayout.WEST);
        panel.add(new JScrollPane(sessionsTable), BorderLayout.CENTER);

        tabbedPane.addTab("Manage Sessions", panel);
    }

    private void addFormField(JPanel p, String label, JComponent field, GridBagConstraints gbc) {
        gbc.gridx = 0; p.add(new JLabel(label), gbc);
        gbc.gridx = 1; p.add(field, gbc);
        gbc.gridy++;
    }

    private void setSpinnerTime(JSpinner spinner, int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        spinner.setValue(cal.getTime());
    }

    private void createSessionAction() {
        try {
            // Validate required fields
            if (txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Session Name is required!");
                return;
            }
            
            Date d = (Date) spDate.getValue();
            LocalDate localDate = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            Date s = (Date) spStart.getValue();
            LocalTime startTime = s.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

            Date end = (Date) spEnd.getValue();
            LocalTime endTime = end.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

            int duration = (Integer) spDuration.getValue();

            if (!endTime.isAfter(startTime)) {
                JOptionPane.showMessageDialog(this, "End Time must be after Start Time!");
                return;
            }

            // Generate new session ID
            String newSessionId = sessionManager.generateSessionId();
            
            // Create session with auto-generated ID
            Session createdSession = sessionManager.createSession(
                newSessionId, 
                txtName.getText().trim(), 
                cbType.getSelectedItem().toString(),
                cbTrack.getSelectedItem().toString(), 
                localDate, 
                txtVenue.getText().trim(), 
                startTime, 
                endTime, 
                duration
            );

            refreshSessionTable();
            refreshSessionSelector();
            refreshSeminarsTable(); // Refresh award nomination tab
            
            // Generate new ID for next session
            txtId.setText(sessionManager.generateSessionId());
            
            // Clear other fields but keep ID for next session
            txtName.setText("");
            txtVenue.setText("DTC Hall");
            cbType.setSelectedIndex(0);
            cbTrack.setSelectedIndex(0);
            spDate.setValue(new Date());
            setSpinnerTime(spStart, 9, 0);
            setSpinnerTime(spEnd, 12, 0);
            spDuration.setValue(20);
            
            JOptionPane.showMessageDialog(this, 
                "Session created successfully!\nSession ID: " + newSessionId + 
                "\nSession Name: " + createdSession.getSessionName());
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error creating session: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void refreshSessionTable() {
        sessionsModel.setRowCount(0);
        for (Session s : sessionManager.getAllSessions()) {
            sessionsModel.addRow(new Object[]{
                s.getSessionId(), s.getSessionName(), s.getSessionType(),
                s.getDate(), s.getVenue(), s.getStartTime() + " - " + s.getEndTime()
            });
        }
    }

    private void initScheduleTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Session to Schedule: "));
        
        cbSessionSelector = new JComboBox<>();
        refreshSessionSelector();
        
        JButton btnLoad = new JButton("Load Schedule");
        topPanel.add(cbSessionSelector);
        topPanel.add(btnLoad);

        String[] scheduleCols = {"Time Slot", "Assigned Student", "Assigned Evaluators"};
        scheduleModel = new DefaultTableModel(scheduleCols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 1; }
        };
        scheduleTable = new JTable(scheduleModel);
        scheduleTable.setRowHeight(30);

        btnLoad.addActionListener(e -> loadScheduleForSelectedSession());

        scheduleModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 1)
                updateStudentAssignment(e.getFirstRow());
        });

        scheduleTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && scheduleTable.getSelectedColumn() == 2) {
                    int row = scheduleTable.getSelectedRow();
                    if (row != -1) openMultiEvaluatorDialog(row);
                }
            }
        });

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(scheduleTable), BorderLayout.CENTER);
        panel.add(new JLabel("  Tip: Double-click 'Assigned Evaluators' to select multiple."), BorderLayout.SOUTH);

        tabbedPane.addTab("Session Schedule", panel);
    }

    private void refreshSessionSelector() {
        cbSessionSelector.removeAllItems();
        for (Session s : sessionManager.getAllSessions()) {
            cbSessionSelector.addItem(s);
        }
    }

    private void loadScheduleForSelectedSession() {
        Session selected = (Session) cbSessionSelector.getSelectedItem();
        if (selected == null) return;

        scheduleModel.setRowCount(0);

        // Get students registered for THIS session only
        List<Student> sessionStudents = sessionManager.getStudentsBySession(selected.getSessionId());
        
        JComboBox<Student> studentBox = new JComboBox<>();
        studentBox.addItem(null); // Empty option
        
        if (sessionStudents.isEmpty()) {
            // If no students registered, show a disabled option
            studentBox.addItem(new Student("No students", "registered for this session", 
                                          "None", "None", "None", "None") {
                @Override
                public String toString() {
                    return "No students registered for this session";
                }
            });
            studentBox.setEnabled(false);
        } else {
            // Add students who registered for this session
            for (Student s : sessionStudents) {
                studentBox.addItem(s);
            }
            studentBox.setEnabled(true);
        }
        
        TableColumn studentCol = scheduleTable.getColumnModel().getColumn(1);
        studentCol.setCellEditor(new DefaultCellEditor(studentBox));

        for (Session.PresentationSlot slot : selected.getSchedule()) {
            scheduleModel.addRow(new Object[]{
                slot.getTimeRange(),
                slot.getStudent(),
                slot.getEvaluatorNames()
            });
        }
        
        // Show info about available students
        if (!sessionStudents.isEmpty()) {
            String info = "Note: Showing " + sessionStudents.size() + 
                         " student(s) registered for " + selected.getSessionId();
            JOptionPane.showMessageDialog(this, info, "Session Students", 
                                        JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateStudentAssignment(int row) {
        Session selected = (Session) cbSessionSelector.getSelectedItem();
        if (selected == null) return;

        Object value = scheduleModel.getValueAt(row, 1);
        
        // Don't update if "No students registered for this session" is selected
        if (value instanceof Student) {
            Student s = (Student) value;
            // Check if it's the placeholder student
            if (s.getStudentId().equals("No students")) {
                return;
            }
            
            List<Evaluator> currentEvaluators = selected.getSchedule().get(row).getEvaluators();
            sessionManager.updateSlotAssignment(selected, row, s, currentEvaluators);
        }
    }

    private void openMultiEvaluatorDialog(int row) {
        Session selected = (Session) cbSessionSelector.getSelectedItem();
        if (selected == null) return;
        
        Session.PresentationSlot slot = selected.getSchedule().get(row);
        List<JCheckBox> checkBoxes = new ArrayList<>();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        
        for (Evaluator ev : sessionManager.getAllEvaluators()) {
            JCheckBox box = new JCheckBox(ev.toString());
            if (slot.getEvaluators().contains(ev)) {
                box.setSelected(true);
            }
            box.putClientProperty("evaluatorObj", ev);
            checkBoxes.add(box);
            panel.add(box);
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setPreferredSize(new Dimension(300, 200));

        int result = JOptionPane.showConfirmDialog(this, scrollPane, 
                "Select Evaluators", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            List<Evaluator> newSelection = new ArrayList<>();
            for (JCheckBox box : checkBoxes) {
                if (box.isSelected()) {
                    newSelection.add((Evaluator) box.getClientProperty("evaluatorObj"));
                }
            }
            sessionManager.updateSlotAssignment(selected, row, slot.getStudent(), newSelection);
            scheduleModel.setValueAt(slot.getEvaluatorNames(), row, 2);
        }
    }
    
    // NEW: Initialize Award Nomination tab
    private void initAwardNominationTab() {
        awardMainPanel = new JPanel(new CardLayout());
        
        // Card 1: Seminar List
        JPanel seminarListPanel = createSeminarListPanel();
        
        awardMainPanel.add(seminarListPanel, "SEMINAR_LIST");
        
        tabbedPane.addTab("Award Nomination", awardMainPanel);
    }
    
    private JPanel createSeminarListPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Title
        JLabel titleLabel = new JLabel("Select a Seminar to Nominate Awards");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Create table for seminars
        String[] columns = {"Session ID", "Session Name", "Date", "Type", "Select"};
        seminarsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only Select column is editable
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 4) {
                    return JButton.class;
                }
                return String.class;
            }
        };
        
        seminarsTable = new JTable(seminarsModel);
        seminarsTable.setRowHeight(40);
        
        // Load seminars
        refreshSeminarsTable();
        
        // Add button renderer and editor for Select column
        seminarsTable.getColumn("Select").setCellRenderer(new ButtonRenderer("View Students"));
        seminarsTable.getColumn("Select").setCellEditor(new ButtonEditor(new JCheckBox()));
        
        panel.add(new JScrollPane(seminarsTable), BorderLayout.CENTER);
        
        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(new JLabel("Click 'View Students' to see evaluations and nominate awards"));
        panel.add(infoPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void refreshSeminarsTable() {
        seminarsModel.setRowCount(0);
        
        List<Session> allSessions = sessionManager.getAllSessions();
        for (Session session : allSessions) {
            seminarsModel.addRow(new Object[]{
                session.getSessionId(),
                session.getSessionName(),
                session.getDate().toString(),
                session.getSessionType(),
                "View Students"
            });
        }
        
        if (allSessions.isEmpty()) {
            seminarsModel.addRow(new Object[]{"No seminars", "available", "", "", ""});
        }
    }
    
    // Button Renderer for Select column
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        private String buttonText;
        
        public ButtonRenderer(String text) {
            this.buttonText = text;
            setOpaque(true);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText(buttonText);
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(new Color(60, 120, 180)); // Blue color
                setForeground(Color.WHITE);
            }
            
            return this;
        }
    }
    
    // Button Editor for Select column
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isClicked;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "View Students" : value.toString();
            button.setText(label);
            button.setBackground(new Color(80, 140, 200)); // Darker blue when clicked
            button.setForeground(Color.WHITE);
            isClicked = true;
            return button;
        }
        
        public Object getCellEditorValue() {
            if (isClicked) {
                JTable table = (JTable) SwingUtilities.getAncestorOfClass(JTable.class, button);
                if (table != null) {
                    int row = table.getEditingRow();
                    if (row >= 0) {
                        String sessionId = (String) table.getValueAt(row, 0);
                        String sessionName = (String) table.getValueAt(row, 1);
                        
                        // Switch to student list panel
                        showStudentListPanel(sessionId, sessionName);
                    }
                }
            }
            isClicked = false;
            return label;
        }
    }
    
    private void showStudentListPanel(String sessionId, String sessionName) {
        // Create student list panel
        JPanel studentListPanel = new JPanel(new BorderLayout(10, 10));
        studentListPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Title with back button
        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Students in: " + sessionName + " (" + sessionId + ")");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        JButton backButton = new JButton("Back to Seminar List");
        backButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) awardMainPanel.getLayout();
            cl.show(awardMainPanel, "SEMINAR_LIST");
        });
        titlePanel.add(backButton, BorderLayout.EAST);
        
        studentListPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Create table for students with grades
        String[] columns = {"Rank", "Student ID", "Student Name", "Average Grade", "Award Status", "Nominate Award"};
        DefaultTableModel studentsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only Nominate Award column is editable
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 5) {
                    return JButton.class;
                }
                return String.class;
            }
        };
        
        // Get students with grades for this session
        List<StudentGrade> studentGrades = UserDatabase.getStudentsWithGrades(sessionId);
        
        int rank = 1;
        for (StudentGrade sg : studentGrades) {
            Student student = sg.getStudent();
            String awardStatus = UserDatabase.hasAward(student.getStudentId(), sessionId) ? 
                               "Already Awarded" : "No Award";
            
            studentsModel.addRow(new Object[]{
                "#" + rank++,
                student.getStudentId(),
                student.getName(),
                sg.getFormattedGrade(),
                awardStatus,
                "Nominate"
            });
        }
        
        if (studentGrades.isEmpty()) {
            studentsModel.addRow(new Object[]{"-", "No students", "with evaluations", "", "", ""});
        }
        
        JTable studentsTable = new JTable(studentsModel);
        studentsTable.setRowHeight(40);
        
        // Add button renderer and editor for Nominate column
        studentsTable.getColumn("Nominate Award").setCellRenderer(new AwardButtonRenderer());
        studentsTable.getColumn("Nominate Award").setCellEditor(new AwardButtonEditor(new JCheckBox(), sessionId, sessionName));
        
        studentListPanel.add(new JScrollPane(studentsTable), BorderLayout.CENTER);
        
        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(new JLabel("Students sorted by average evaluation grade (highest to lowest)"));
        studentListPanel.add(infoPanel, BorderLayout.SOUTH);
        
        // Add to card layout
        awardMainPanel.add(studentListPanel, "STUDENT_LIST_" + sessionId);
        
        // Show this panel
        CardLayout cl = (CardLayout) awardMainPanel.getLayout();
        cl.show(awardMainPanel, "STUDENT_LIST_" + sessionId);
    }
    
    // Award Button Renderer
    class AwardButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public AwardButtonRenderer() {
            setOpaque(true);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Nominate");
            
            // Check if student already has award
            String awardStatus = (String) table.getValueAt(row, 4);
            if ("Already Awarded".equals(awardStatus)) {
                setEnabled(false);
                setBackground(Color.GRAY);
                setText("Awarded");
            } else {
                setEnabled(true);
                setBackground(new Color(60, 180, 120)); // Green color
            }
            
            setForeground(Color.WHITE);
            
            return this;
        }
    }
    
    // Award Button Editor
    class AwardButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isClicked;
        private String sessionId;
        private String sessionName;
        
        public AwardButtonEditor(JCheckBox checkBox, String sessionId, String sessionName) {
            super(checkBox);
            this.sessionId = sessionId;
            this.sessionName = sessionName;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "Nominate" : value.toString();
            button.setText(label);
            button.setBackground(new Color(80, 200, 140)); // Darker green when clicked
            button.setForeground(Color.WHITE);
            isClicked = true;
            return button;
        }
        
        public Object getCellEditorValue() {
            if (isClicked) {
                JTable table = (JTable) SwingUtilities.getAncestorOfClass(JTable.class, button);
                if (table != null) {
                    int row = table.getEditingRow();
                    if (row >= 0) {
                        String studentId = (String) table.getValueAt(row, 1);
                        String studentName = (String) table.getValueAt(row, 2);
                        
                        // Show award selection dialog
                        showAwardSelectionDialog(studentId, studentName, sessionId, sessionName, table, row);
                    }
                }
            }
            isClicked = false;
            return label;
        }
    }
    
    private void showAwardSelectionDialog(String studentId, String studentName, 
                                         String sessionId, String sessionName, 
                                         JTable table, int row) {
        // Create award selection dialog
        JDialog awardDialog = new JDialog(this, "Select Award for " + studentName, true);
        awardDialog.setSize(400, 250);
        awardDialog.setLocationRelativeTo(this);
        awardDialog.setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Select Award Type for:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        JLabel studentLabel = new JLabel(studentName + " (" + studentId + ")");
        studentLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        headerPanel.add(studentLabel, BorderLayout.CENTER);
        
        JLabel sessionLabel = new JLabel("Session: " + sessionName);
        sessionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        headerPanel.add(sessionLabel, BorderLayout.SOUTH);
        
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Award options
        JPanel awardPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        awardPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        ButtonGroup awardGroup = new ButtonGroup();
        JRadioButton bestOralButton = new JRadioButton("Best Oral Presentation");
        JRadioButton bestPosterButton = new JRadioButton("Best Poster Presentation");
        JRadioButton peoplesChoiceButton = new JRadioButton("People's Choice Award");
        
        awardGroup.add(bestOralButton);
        awardGroup.add(bestPosterButton);
        awardGroup.add(peoplesChoiceButton);
        
        awardPanel.add(bestOralButton);
        awardPanel.add(bestPosterButton);
        awardPanel.add(peoplesChoiceButton);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton cancelButton = new JButton("Cancel");
        JButton assignButton = new JButton("Assign Award");
        
        cancelButton.addActionListener(e -> awardDialog.dispose());
        assignButton.addActionListener(e -> {
            String selectedAward = null;
            if (bestOralButton.isSelected()) {
                selectedAward = "Best Oral Presentation";
            } else if (bestPosterButton.isSelected()) {
                selectedAward = "Best Poster Presentation";
            } else if (peoplesChoiceButton.isSelected()) {
                selectedAward = "People's Choice Award";
            }
            
            if (selectedAward != null) {
                // Save award
                UserDatabase.saveAward(studentId, sessionId, selectedAward);
                
                // Update table
                table.setValueAt("Already Awarded", row, 4);
                table.setValueAt("Awarded", row, 5);
                
                JOptionPane.showMessageDialog(awardDialog,
                    "Award assigned successfully!\n" +
                    studentName + " received: " + selectedAward,
                    "Award Assigned",
                    JOptionPane.INFORMATION_MESSAGE);
                
                awardDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(awardDialog,
                    "Please select an award type.",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(assignButton);
        
        // Add components to dialog
        awardDialog.add(headerPanel, BorderLayout.NORTH);
        awardDialog.add(awardPanel, BorderLayout.CENTER);
        awardDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        awardDialog.setVisible(true);
    }
}