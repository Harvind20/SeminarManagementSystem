package student;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import misc.Dashboard;
import misc.UserDatabase;

public class StudentDashboard extends Dashboard {

    private CardLayout cardLayout;
    private JPanel mainContainer;

    // View Components
    private JTabbedPane tabbedPane;
    private JTable availableTable, registeredTable, gradesTable;
    private DefaultTableModel availableModel, registeredModel, gradesModel;

    // Form Components
    private JPanel formPanel;
    private JTextField idField, nameField, boardIdField, sessionDisplayField, titleField;
    private JComboBox<String> abstractBox, supervisorBox, typeBox;
    private JButton uploadBtn, saveBtn, cancelBtn;
    private JLabel filePathLabel;
    
    // Data
    private Student currentProfile;
    private List<Student> myRegistrations;
    private String uploadedFilePath;
    private String targetSessionIdForRegistration;

    public StudentDashboard(String studentId) {
        super(studentId, "Student Dashboard - Seminar System");
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }

    @Override
    protected void buildDashboard() {
        currentProfile = UserDatabase.getStudentById(userId);
        if (currentProfile == null) {
            JOptionPane.showMessageDialog(this, "Error: Student not found.");
            dispose(); return;
        }
        
        myRegistrations = UserDatabase.getStudentRegistrations(userId);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        JPanel tabsPanel = new JPanel(new BorderLayout());
        tabbedPane = new JTabbedPane();
        
        initAvailableSeminarsTab();
        initRegisteredSeminarsTab();
        initGradesTab(); 
        
        tabsPanel.add(tabbedPane, BorderLayout.CENTER);

        initFormView();

        mainContainer.add(tabsPanel, "TABS");
        mainContainer.add(formPanel, "FORM");

        contentPanel.add(mainContainer, BorderLayout.CENTER);
    }

    private void initAvailableSeminarsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Session ID", "Session Name", "Date", "Time", "Action"};
        availableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 4; }
        };

        availableTable = new JTable(availableModel);
        availableTable.setRowHeight(35);
        availableTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        availableTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));

        panel.add(new JScrollPane(availableTable), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh List");
        refreshBtn.addActionListener(e -> refreshAllData());
        panel.add(refreshBtn, BorderLayout.SOUTH);

        tabbedPane.addTab("Available Seminars", panel);
        loadAvailableSessions();
    }

    private void initRegisteredSeminarsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Session ID", "My Title", "Status", "Edit", "Unregister"};
        registeredModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 3 || column == 4; }
        };

        registeredTable = new JTable(registeredModel);
        registeredTable.setRowHeight(35);
        registeredTable.getColumn("Edit").setCellRenderer(new ButtonRenderer());
        registeredTable.getColumn("Edit").setCellEditor(new EditButtonEditor(new JCheckBox()));
        registeredTable.getColumn("Unregister").setCellRenderer(new ButtonRenderer());
        registeredTable.getColumn("Unregister").setCellEditor(new UnregisterButtonEditor(new JCheckBox()));

        panel.add(new JScrollPane(registeredTable), BorderLayout.CENTER);
        tabbedPane.addTab("My Registrations", panel);
        loadMyRegistration();
    }

    private void initGradesTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Added "Feedback" column
        String[] columns = {"Session ID", "Session Name", "Total Grade", "Awards Earned", "Feedback"};
        gradesModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 4; } 
        };

        gradesTable = new JTable(gradesModel);
        gradesTable.setRowHeight(35);
        
        gradesTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        gradesTable.getColumn("Feedback").setCellRenderer(new ViewFeedbackButtonRenderer());
        gradesTable.getColumn("Feedback").setCellEditor(new ViewFeedbackButtonEditor(new JCheckBox()));

        panel.add(new JScrollPane(gradesTable), BorderLayout.CENTER);
        
        JLabel info = new JLabel("Click 'View Comment' to see detailed rubric scores and evaluator comments.");
        info.setFont(new Font("Arial", Font.ITALIC, 12));
        panel.add(info, BorderLayout.SOUTH);

        tabbedPane.addTab("Grades & Awards", panel);
        loadGradesAndAwards();
    }

    private void refreshAllData() {
        myRegistrations = UserDatabase.getStudentRegistrations(userId);
        loadAvailableSessions();
        loadMyRegistration();
        loadGradesAndAwards();
    }

    private void loadAvailableSessions() {
        availableModel.setRowCount(0);
        File file = new File("saved/sessions.txt"); 
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Schedule:") || line.startsWith("----") || line.trim().isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length >= 8) {
                    String sessionId = p[0];
                    String actionLabel = "Register";
                    for(Student s : myRegistrations) { if(s.getSessionId().equals(sessionId)) { actionLabel = "Registered"; break; } }
                    availableModel.addRow(new Object[]{ sessionId, p[1], p[4], p[6] + "-" + p[7], actionLabel });
                }
            }
        } catch (Exception e) {}
    }

    private void loadMyRegistration() {
        registeredModel.setRowCount(0);
        myRegistrations = UserDatabase.getStudentRegistrations(userId); 
        for(Student s : myRegistrations) {
            registeredModel.addRow(new Object[]{ s.getSessionId(), s.getResearchTitle(), "Confirmed", "Edit", "Unregister" });
        }
    }

    private void loadGradesAndAwards() {
        gradesModel.setRowCount(0);
        for(Student s : myRegistrations) {
            String sessionId = s.getSessionId();
            String[] sessionDetails = UserDatabase.getSessionDetails(sessionId);
            String sessionName = sessionDetails[0];

            double sessionGrade = 0.0;
            boolean hasGrade = false;
            try {
                // Now works because UserDatabase has getEvaluationDetails
                List<UserDatabase.EvaluationDetail> details = UserDatabase.getEvaluationDetails(sessionId, userId);
                if(!details.isEmpty()) {
                    double sum = 0;
                    for(UserDatabase.EvaluationDetail d : details) sum += d.total;
                    sessionGrade = sum / details.size();
                    hasGrade = true;
                }
            } catch(Exception e) {}

            String gradeDisplay = hasGrade ? String.format("%.2f / 40", sessionGrade) : "Pending";
            String awardName = UserDatabase.getAwardForStudent(userId, sessionId);
            String awardDisplay = (awardName != null) ? "🏆 " + awardName : "-";

            gradesModel.addRow(new Object[]{ sessionId, sessionName, gradeDisplay, awardDisplay, "View Comment" });
        }
    }

    private void initFormView() {
        formPanel = new JPanel(new BorderLayout(20, 20));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBorder(BorderFactory.createTitledBorder(" Registration Details "));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        sessionDisplayField = new JTextField(); sessionDisplayField.setEditable(false);
        boardIdField = new JTextField(); boardIdField.setEditable(false);
        titleField = new JTextField();
        String[] abstractOptions = {"-- Select Abstract --", "Artificial Intelligence", "IoT & Smart Systems", "Cybersecurity", "Data Science", "Software Engineering"};
        abstractBox = new JComboBox<>(abstractOptions);
        supervisorBox = new JComboBox<>();
        typeBox = new JComboBox<>(new String[]{"Oral Presentation", "Poster Presentation", "Demo Session"});
        filePathLabel = new JLabel("No file uploaded");
        uploadBtn = new JButton("Upload File");
        uploadBtn.addActionListener(e -> handleFileUpload());

        addFormRow(fieldsPanel, "Session:", sessionDisplayField, gbc, 0);
        addFormRow(fieldsPanel, "Board ID:", boardIdField, gbc, 1);
        addFormRow(fieldsPanel, "Research Title:", titleField, gbc, 2);
        addFormRow(fieldsPanel, "Abstract Category:", abstractBox, gbc, 3);
        addFormRow(fieldsPanel, "Supervisor:", supervisorBox, gbc, 4);
        addFormRow(fieldsPanel, "Presentation:", typeBox, gbc, 5); 
        addFormRow(fieldsPanel, "File:", uploadBtn, gbc, 6);
        gbc.gridx = 1; gbc.gridy = 7; fieldsPanel.add(filePathLabel, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveBtn = new JButton("Submit");
        cancelBtn = new JButton("Cancel");
        saveBtn.addActionListener(e -> submitForm());
        cancelBtn.addActionListener(e -> { refreshAllData(); cardLayout.show(mainContainer, "TABS"); });
        btnPanel.add(cancelBtn); btnPanel.add(saveBtn);

        formPanel.add(fieldsPanel, BorderLayout.CENTER);
        formPanel.add(btnPanel, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel p, String label, JComponent c, GridBagConstraints gbc, int y) {
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; p.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1; p.add(c, gbc);
    }

    private void openRegistrationForm(String sessionId) {
        loadSupervisors();
        targetSessionIdForRegistration = sessionId;
        sessionDisplayField.setText(sessionId);
        boardIdField.setText("BID-" + (new Random().nextInt(900) + 100)); 
        titleField.setText("");
        abstractBox.setSelectedIndex(0);
        supervisorBox.setSelectedIndex(0);
        uploadedFilePath = "None";
        filePathLabel.setText("No file");
        saveBtn.setText("Register");
        cardLayout.show(mainContainer, "FORM");
    }

    private void openEditForm(String sessionId) {
        loadSupervisors();
        targetSessionIdForRegistration = sessionId;
        Student target = null;
        for(Student s : myRegistrations) { if(s.getSessionId().equals(sessionId)) { target = s; break; } }
        if(target != null) {
            sessionDisplayField.setText(sessionId);
            boardIdField.setText(target.getBoardId());
            titleField.setText(target.getResearchTitle());
            typeBox.setSelectedItem(target.getPresentationType());
            setBoxItem(abstractBox, target.getAbstractText());
            setBoxItem(supervisorBox, target.getSupervisor());
            uploadedFilePath = target.getSubmissionPath();
            filePathLabel.setText(uploadedFilePath);
            saveBtn.setText("Save Changes");
            cardLayout.show(mainContainer, "FORM");
        }
    }

    private void submitForm() {
        if (titleField.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Title Required"); return; }
        Student reg = new Student(currentProfile.getStudentId(), currentProfile.getPassword(), currentProfile.getName(), titleField.getText(),          
            (String) abstractBox.getSelectedItem(), (String) supervisorBox.getSelectedItem(), (String) typeBox.getSelectedItem(), uploadedFilePath);
        reg.setSessionId(targetSessionIdForRegistration);
        reg.setBoardId(boardIdField.getText());
        UserDatabase.saveStudentRegistration(reg);
        JOptionPane.showMessageDialog(this, "Saved Successfully!");
        refreshAllData();
        cardLayout.show(mainContainer, "TABS");
    }

    private void unregister(String sessionId) {
        if (JOptionPane.showConfirmDialog(this, "Unregister from " + sessionId + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            UserDatabase.unregisterStudent(userId, sessionId);
            refreshAllData();
        }
    }

    private void setBoxItem(JComboBox<String> box, String val) { for(int i=0; i<box.getItemCount(); i++) if(box.getItemAt(i).equals(val)) box.setSelectedIndex(i); }
    private void loadSupervisors() { supervisorBox.removeAllItems(); supervisorBox.addItem("-- Select --"); File f = new File("saved/evaluators.txt"); try(BufferedReader br = new BufferedReader(new FileReader(f))){ String l; while((l=br.readLine())!=null) { String[] p=l.split("\\|"); if(p.length>2) supervisorBox.addItem(p[2]); } } catch(Exception e){} }
    private void handleFileUpload() { JFileChooser c = new JFileChooser(); if(c.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { uploadedFilePath = c.getSelectedFile().getAbsolutePath(); filePathLabel.setText(c.getSelectedFile().getName()); } }

    private void showFeedbackDialog(String sessionId, String sessionName) {
        JDialog dialog = new JDialog(this, "Detailed Feedback - " + sessionName, true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        // Using the new methods in UserDatabase
        List<UserDatabase.EvaluationDetail> details = UserDatabase.getEvaluationDetails(sessionId, userId);
        
        if (details.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No evaluations submitted yet.");
            return;
        }

        double[] avgScores = new double[4];
        for (UserDatabase.EvaluationDetail d : details) {
            for (int i = 0; i < 4; i++) avgScores[i] += d.scores[i];
        }
        for (int i = 0; i < 4; i++) avgScores[i] /= details.size();

        JPanel summaryPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        summaryPanel.setBorder(BorderFactory.createTitledBorder(" Average Scores by Criteria (Max 10) "));
        String[] criteria = {"Problem Clarity", "Methodology", "Results", "Presentation"};
        
        for (int i = 0; i < 4; i++) {
            JPanel p = new JPanel(new BorderLayout());
            p.add(new JLabel(criteria[i], SwingConstants.CENTER), BorderLayout.NORTH);
            JLabel scoreLbl = new JLabel(String.format("%.2f", avgScores[i]), SwingConstants.CENTER);
            scoreLbl.setFont(new Font("Arial", Font.BOLD, 18));
            scoreLbl.setForeground(new Color(60, 120, 180));
            p.add(scoreLbl, BorderLayout.CENTER);
            summaryPanel.add(p);
        }

        JPanel commentsPanel = new JPanel();
        commentsPanel.setLayout(new BoxLayout(commentsPanel, BoxLayout.Y_AXIS));
        
        for (UserDatabase.EvaluationDetail d : details) {
            JPanel card = new JPanel(new BorderLayout(5, 5));
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createLineBorder(Color.LIGHT_GRAY)
            ));
            
            JLabel nameLbl = new JLabel("Evaluator: " + d.evaluatorName + " (Total: " + d.total + "/40)");
            nameLbl.setFont(new Font("Arial", Font.BOLD, 12));
            
            JTextArea commentText = new JTextArea(d.comment);
            commentText.setLineWrap(true);
            commentText.setWrapStyleWord(true);
            commentText.setEditable(false);
            commentText.setBackground(new Color(245, 245, 245));
            commentText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            card.add(nameLbl, BorderLayout.NORTH);
            card.add(commentText, BorderLayout.CENTER);
            commentsPanel.add(card);
            commentsPanel.add(Box.createVerticalStrut(10));
        }

        dialog.add(summaryPanel, BorderLayout.NORTH);
        dialog.add(new JScrollPane(commentsPanel), BorderLayout.CENTER);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        dialog.add(closeBtn, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    class ViewFeedbackButtonRenderer extends JButton implements TableCellRenderer {
        public ViewFeedbackButtonRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText("View Comment"); 
            setBackground(new Color(100, 149, 237)); setForeground(Color.WHITE);
            return this;
        }
    }
    class ViewFeedbackButtonEditor extends DefaultCellEditor {
        public ViewFeedbackButtonEditor(JCheckBox c) { super(c); }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            JButton b = new JButton("View Comment");
            b.setBackground(new Color(100, 149, 237)); b.setForeground(Color.WHITE);
            b.addActionListener(e -> {
                fireEditingStopped();
                String sessionId = (String) t.getValueAt(r, 0);
                String sessionName = (String) t.getValueAt(r, 1);
                showFeedbackDialog(sessionId, sessionName);
            });
            return b;
        }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText((v==null)?"":v.toString());
            setEnabled(!getText().equals("Registered") && !getText().equals("Locked"));
            return this;
        }
    }
    class ButtonEditor extends DefaultCellEditor {
        public ButtonEditor(JCheckBox c) { super(c); }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            String label = (v==null)?"":v.toString(); JButton b = new JButton(label);
            b.setEnabled(!label.equals("Registered"));
            b.addActionListener(e -> { fireEditingStopped(); if(label.equals("Register")) openRegistrationForm(t.getValueAt(r, 0).toString()); });
            return b;
        }
    }
    class EditButtonEditor extends DefaultCellEditor {
        public EditButtonEditor(JCheckBox c) { super(c); }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            JButton b = new JButton("Edit");
            b.addActionListener(e -> { fireEditingStopped(); openEditForm(t.getValueAt(r, 0).toString()); });
            return b;
        }
    }
    class UnregisterButtonEditor extends DefaultCellEditor {
        public UnregisterButtonEditor(JCheckBox c) { super(c); }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            JButton b = new JButton("Unregister");
            b.addActionListener(e -> { fireEditingStopped(); unregister(t.getValueAt(r, 0).toString()); });
            return b;
        }
    }
}