package coordinator;

import evaluator.Evaluator;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
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
    
    private JTable seminarsTable;
    private DefaultTableModel seminarsModel;
    private JPanel awardMainPanel;

    public CoordinatorDashboard(String userId) {
        super(userId, "Coordinator Dashboard - Seminar System");
        setSize(1300, 800);
        setLocationRelativeTo(null);
    }

    @Override
    protected void buildDashboard() {
        sessionManager = new SessionManager();

        tabbedPane = new JTabbedPane();
        initSessionTab();   
        initScheduleTab();  
        initAwardNominationTab(); 

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
        txtId.setEditable(false);
        txtId.setBackground(new Color(240, 240, 240));
        txtId.setText(sessionManager.generateSessionId());
        
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

        String[] cols = {"ID", "Name", "Type", "Date", "Venue", "Time"};
        sessionsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        sessionsTable = new JTable(sessionsModel);
        sessionsTable.setRowHeight(25);
        
        sessionsTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        
        refreshSessionTable(); 

        btnCreate.addActionListener(e -> createSessionAction());

        panel.add(formPanel, BorderLayout.WEST);
        panel.add(new JScrollPane(sessionsTable), BorderLayout.CENTER);

        tabbedPane.addTab("Manage Sessions", panel);
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

            String newSessionId = sessionManager.generateSessionId();
            
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
            
            txtId.setText(sessionManager.generateSessionId());
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

        List<Student> sessionStudents = sessionManager.getStudentsBySession(selected.getSessionId());
        
        JComboBox<Student> studentBox = new JComboBox<>();
        studentBox.addItem(null);
        
        if (sessionStudents.isEmpty()) {
            studentBox.addItem(new Student("No students", "registered for this session", 
                                            "None", "None", "None", "None") {
                @Override
                public String toString() { return "No students registered for this session"; }
            });
            studentBox.setEnabled(false);
        } else {
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
        
        if (value instanceof Student) {
            Student s = (Student) value;
            if (s.getStudentId().equals("No students")) return;
            
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
    
    private void initAwardNominationTab() {
        awardMainPanel = new JPanel(new CardLayout());
        
        JPanel seminarListPanel = createSeminarListPanel();
        
        awardMainPanel.add(seminarListPanel, "SEMINAR_LIST");
        
        tabbedPane.addTab("Award Nomination", awardMainPanel);
    }
    
    private JPanel createSeminarListPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("Select a Seminar to Nominate Awards");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        String[] columns = {"Session ID", "Session Name", "Date", "Type", "Select"};
        seminarsModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 4; }
            @Override public Class<?> getColumnClass(int column) { return column == 4 ? JButton.class : String.class; }
        };
        
        seminarsTable = new JTable(seminarsModel);
        seminarsTable.setRowHeight(40);
        seminarsTable.getColumnModel().getColumn(0).setPreferredWidth(60); 
        
        refreshSeminarsTable();
        
        seminarsTable.getColumn("Select").setCellRenderer(new ButtonRenderer("View Students"));
        seminarsTable.getColumn("Select").setCellEditor(new ButtonEditor(new JCheckBox()));
        
        panel.add(new JScrollPane(seminarsTable), BorderLayout.CENTER);
        
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
                session.getSessionId(), session.getSessionName(),
                session.getDate().toString(), session.getSessionType(), "View Students"
            });
        }
        if (allSessions.isEmpty()) {
            seminarsModel.addRow(new Object[]{"No seminars", "available", "", "", ""});
        }
    }
    
    private String getAllCommentsForStudent(String sessionId, String studentId) {
        StringBuilder sb = new StringBuilder("<html>");
        boolean found = false;
        try (BufferedReader br = new BufferedReader(new FileReader("./saved/evaluations.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length > 8 && p[0].equals(sessionId) && p[1].equals(studentId)) {
                    String evalId = p[2];
                    String comment = p[8].trim();
                    if (!comment.isEmpty() && !comment.equals("null")) {
                        Evaluator ev = UserDatabase.getEvaluatorById(evalId);
                        String evalName = (ev != null) ? ev.getName() : evalId;
                        
                        if (found) sb.append("<br><br>"); 
                        sb.append("<b>").append(evalName).append(":</b> ").append(comment);
                        found = true;
                    }
                }
            }
        } catch (Exception e) {}
        if (!found) return "-";
        return sb.append("</html>").toString();
    }

    private void showStudentListPanel(String sessionId, String sessionName) {
        JPanel studentListPanel = new JPanel(new BorderLayout(10, 10));
        studentListPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Students in: " + sessionName + " (" + sessionId + ")");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportBtn = new JButton("Export Full CSV");
        exportBtn.setBackground(new Color(0, 102, 204));
        exportBtn.setForeground(Color.WHITE);
        exportBtn.addActionListener(e -> {
            UserDatabase.exportStudentListToCSV(sessionId);
            JOptionPane.showMessageDialog(this, "Exported successfully to ./saved/ folder.");
        });
        
        JButton backButton = new JButton("Back to Seminar List");
        backButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) awardMainPanel.getLayout();
            cl.show(awardMainPanel, "SEMINAR_LIST");
        });
        
        buttonPanel.add(exportBtn);
        buttonPanel.add(backButton);
        titlePanel.add(buttonPanel, BorderLayout.EAST);
        
        studentListPanel.add(titlePanel, BorderLayout.NORTH);
        
        String[] columns = {"Rank", "ID", "Name", "Avg Grade", "Evaluator Comments", "Award Status", "Nominate"};
        
        DefaultTableModel studentsModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) {
                if ("No students".equals(getValueAt(row, 2))) return false;
                return column == 6; 
            }
            @Override public Class<?> getColumnClass(int column) { return column == 6 ? JButton.class : String.class; }
        };
        
        List<StudentGrade> studentGrades = UserDatabase.getStudentsWithGrades(sessionId);
        int rank = 1;
        
        for (StudentGrade sg : studentGrades) {
            Student student = sg.getStudent();
            String awardName = UserDatabase.getAwardForStudent(student.getStudentId(), sessionId);
            String awardDisplay = (awardName != null) ? awardName : "No Award";
            String allComments = getAllCommentsForStudent(sessionId, student.getStudentId());
            
            studentsModel.addRow(new Object[]{
                "#" + rank++, 
                student.getStudentId(),
                student.getName(),
                sg.getFormattedGrade(),
                allComments, 
                awardDisplay,
                "Nominate"
            });
        }
        
        if (studentGrades.isEmpty()) {
            studentsModel.addRow(new Object[]{"-", "-", "No students", "", "", "", ""});
        }
        
        JTable studentsTable = new JTable(studentsModel);
        studentsTable.setRowHeight(80); 
        
        studentsTable.getColumnModel().getColumn(0).setPreferredWidth(35);
        studentsTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        studentsTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        studentsTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        studentsTable.getColumnModel().getColumn(4).setPreferredWidth(450); 
        studentsTable.getColumnModel().getColumn(5).setPreferredWidth(150);
        studentsTable.getColumnModel().getColumn(6).setPreferredWidth(90);
        
        studentsTable.getColumn("Nominate").setCellRenderer(new AwardButtonRenderer());
        studentsTable.getColumn("Nominate").setCellEditor(new AwardButtonEditor(new JCheckBox(), sessionId, sessionName));
        
        studentListPanel.add(new JScrollPane(studentsTable), BorderLayout.CENTER);
        
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(new JLabel("Students sorted by average evaluation grade."));
        studentListPanel.add(infoPanel, BorderLayout.SOUTH);
        
        awardMainPanel.add(studentListPanel, "STUDENT_LIST_" + sessionId);
        CardLayout cl = (CardLayout) awardMainPanel.getLayout();
        cl.show(awardMainPanel, "STUDENT_LIST_" + sessionId);
    }
    
    private void showAwardSelectionDialog(String studentId, String studentName, String sessionId, String sessionName) {
        JDialog awardDialog = new JDialog(this, "Nominate Award", true);
        awardDialog.setSize(400, 300);
        awardDialog.setLocationRelativeTo(this);
        awardDialog.setLayout(new BorderLayout(10, 10));
        
        JPanel headerPanel = new JPanel(new GridLayout(3, 1));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.add(new JLabel("Student: " + studentName + " (" + studentId + ")"));
        headerPanel.add(new JLabel("Session: " + sessionName));
        headerPanel.add(new JLabel("Select Unique Award:"));
        
        JPanel awardPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        awardPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        
        ButtonGroup group = new ButtonGroup();
        JRadioButton rb1 = new JRadioButton("Best Oral Presentation");
        JRadioButton rb2 = new JRadioButton("Best Poster Presentation");
        JRadioButton rb3 = new JRadioButton("People's Choice Award");
        
        String current = UserDatabase.getAwardForStudent(studentId, sessionId);
        if("Best Oral Presentation".equals(current)) rb1.setSelected(true);
        if("Best Poster Presentation".equals(current)) rb2.setSelected(true);
        if("People's Choice Award".equals(current)) rb3.setSelected(true);

        group.add(rb1); group.add(rb2); group.add(rb3);
        awardPanel.add(rb1); awardPanel.add(rb2); awardPanel.add(rb3);
        
        JButton btnSave = new JButton("Save Award");
        btnSave.addActionListener(e -> {
            String selected = null;
            if(rb1.isSelected()) selected = "Best Oral Presentation";
            else if(rb2.isSelected()) selected = "Best Poster Presentation";
            else if(rb3.isSelected()) selected = "People's Choice Award";
            
            if(selected != null) {
                Student currentHolder = UserDatabase.getStudentWithAward(sessionId, selected);
                
                if (currentHolder != null && !currentHolder.getStudentId().equals(studentId)) {
                    int choice = JOptionPane.showConfirmDialog(awardDialog, 
                        "The award '" + selected + "' is currently held by " + currentHolder.getName() + ".\n" +
                        "Do you want to reassign it to " + studentName + "?",
                        "Confirm Reassignment", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    
                    if (choice != JOptionPane.YES_OPTION) return;
                }

                UserDatabase.saveAward(studentId, sessionId, selected);
                showStudentListPanel(sessionId, sessionName); 
                awardDialog.dispose();
                JOptionPane.showMessageDialog(this, "Award Updated!");
            }
        });
        
        awardDialog.add(headerPanel, BorderLayout.NORTH);
        awardDialog.add(awardPanel, BorderLayout.CENTER);
        awardDialog.add(btnSave, BorderLayout.SOUTH);
        awardDialog.setVisible(true);
    }

    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        private String txt; public ButtonRenderer(String t){this.txt=t; setOpaque(true);}
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c){
            setText(txt); 
            if (s) { setBackground(t.getSelectionBackground()); setForeground(t.getSelectionForeground()); }
            else { setBackground(new Color(60, 120, 180)); setForeground(Color.WHITE); }
            return this;
        }
    }
    class ButtonEditor extends DefaultCellEditor {
        private JButton b; private String l; private boolean clicked;
        public ButtonEditor(JCheckBox c){ super(c); b=new JButton(); b.setOpaque(true); b.addActionListener(e->fireEditingStopped()); }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c){
            l=(v==null)?"View":v.toString(); b.setText(l); 
            b.setBackground(new Color(80, 140, 200)); b.setForeground(Color.WHITE); clicked=true; return b;
        }
        public Object getCellEditorValue(){
            if(clicked){ showStudentListPanel(seminarsTable.getValueAt(seminarsTable.getEditingRow(),0).toString(), seminarsTable.getValueAt(seminarsTable.getEditingRow(),1).toString()); }
            clicked=false; return l;
        }
    }
    class AwardButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public AwardButtonRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if ("No students".equals(table.getValueAt(row, 2))) { JLabel empty = new JLabel(); empty.setOpaque(true); empty.setBackground(table.getBackground()); return empty; }
            String awardStatus = (String) table.getValueAt(row, 5); 
            if (!"No Award".equals(awardStatus)) { setText("Change"); setBackground(new Color(255, 165, 0)); } 
            else { setText("Nominate"); setBackground(new Color(60, 180, 120)); }
            setForeground(Color.WHITE); return this;
        }
    }
    class AwardButtonEditor extends DefaultCellEditor {
        private JButton button; private boolean isClicked; private String sessionId, sessionName;
        public AwardButtonEditor(JCheckBox c, String sId, String sName) { super(c); this.sessionId=sId; this.sessionName=sName; button=new JButton(); button.setOpaque(true); button.addActionListener(e->fireEditingStopped()); }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            String stat = (String) t.getValueAt(r, 5);
            button.setText(stat.equals("No Award")?"Nominate":"Change");
            button.setBackground(stat.equals("No Award")?new Color(60, 180, 120):new Color(255, 165, 0));
            button.setForeground(Color.WHITE); isClicked=true; return button;
        }
        public Object getCellEditorValue() {
            if(isClicked) {
                JTable t = (JTable) SwingUtilities.getAncestorOfClass(JTable.class, button);
                if(t!=null) showAwardSelectionDialog((String)t.getValueAt(t.getEditingRow(),1), (String)t.getValueAt(t.getEditingRow(),2), sessionId, sessionName);
            }
            isClicked=false; return "Nominate";
        }
    }
}