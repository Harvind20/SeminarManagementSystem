import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class CoordinatorDashboard extends JFrame {

    private SessionManager sessionManager;
    private JTabbedPane tabbedPane;

    private JTable sessionsTable;
    private DefaultTableModel sessionsModel;
    private JTextField txtId, txtName, txtDate, txtStart, txtEnd, txtDuration, txtVenue;
    private JComboBox<String> cbType, cbTrack;

    private JComboBox<Session> cbSessionSelector;
    private JTable scheduleTable;
    private DefaultTableModel scheduleModel;

    public CoordinatorDashboard() {
        sessionManager = new SessionManager();

        // 2. Window Setup
        setTitle("Coordinator Dashboard - FCSIT Seminar System");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 3. Tab System
        tabbedPane = new JTabbedPane();
        initSessionTab();   // Tab 1
        initScheduleTab();  // Tab 2

        add(tabbedPane);
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
        txtName = new JTextField();
        cbType = new JComboBox<>(new String[]{"Oral Presentation", "Poster Presentation", "Demo Session"});
        cbTrack = new JComboBox<>(new String[]{"AI Track", "Data Science Track", "Cybersecurity Track", "Software Eng. Track"});
        txtDate = new JTextField("2026-02-15");
        txtVenue = new JTextField("DTC Hall");
        txtStart = new JTextField("09:00");
        txtEnd = new JTextField("12:00");
        txtDuration = new JTextField("20");

        addFormField(formPanel, "Session ID:", txtId, gbc);
        addFormField(formPanel, "Session Name:", txtName, gbc);
        addFormField(formPanel, "Type:", cbType, gbc);
        addFormField(formPanel, "Track:", cbTrack, gbc);
        addFormField(formPanel, "Date (YYYY-MM-DD):", txtDate, gbc);
        addFormField(formPanel, "Venue:", txtVenue, gbc);
        addFormField(formPanel, "Start Time (HH:MM):", txtStart, gbc);
        addFormField(formPanel, "End Time (HH:MM):", txtEnd, gbc);
        addFormField(formPanel, "Duration (Mins):", txtDuration, gbc);

        JButton btnCreate = new JButton("Create Session");
        btnCreate.setBackground(new Color(60, 120, 180));
        btnCreate.setForeground(Color.WHITE);
        btnCreate.setFocusPainted(false);
        btnCreate.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        formPanel.add(btnCreate, gbc);

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

    private void createSessionAction() {
        try {
            LocalDate d = LocalDate.parse(txtDate.getText());
            LocalTime s = LocalTime.parse(txtStart.getText());
            LocalTime e = LocalTime.parse(txtEnd.getText());
            int dur = Integer.parseInt(txtDuration.getText());

            sessionManager.createSession(
                txtId.getText(), txtName.getText(), cbType.getSelectedItem().toString(),
                cbTrack.getSelectedItem().toString(), d, txtVenue.getText(), s, e, dur
            );

            refreshSessionTable();
            refreshSessionSelector();
            JOptionPane.showMessageDialog(this, "Session created successfully!");
            
            txtId.setText("");
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: Please check Date (YYYY-MM-DD) and Time (HH:MM) formats.");
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
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };
        scheduleTable = new JTable(scheduleModel);
        scheduleTable.setRowHeight(30);

        btnLoad.addActionListener(e -> loadScheduleForSelectedSession());

        scheduleModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 1) {
                updateStudentAssignment(e.getFirstRow());
            }
        });

        scheduleTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && scheduleTable.getSelectedColumn() == 2) {
                    int row = scheduleTable.getSelectedRow();
                    if (row != -1) openMultiEvaluatorDialog(row);
                }
            }
        });

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(scheduleTable), BorderLayout.CENTER);
        
        JLabel infoLabel = new JLabel("  Tip: Double-click 'Assigned Evaluators' to select multiple people.");
        infoLabel.setForeground(Color.DARK_GRAY);
        panel.add(infoLabel, BorderLayout.SOUTH);

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

        JComboBox<Student> studentBox = new JComboBox<>();
        studentBox.addItem(null);
        for (Student s : sessionManager.getAllStudents()) {
            studentBox.addItem(s);
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
    }

    private void updateStudentAssignment(int row) {
        Session selected = (Session) cbSessionSelector.getSelectedItem();
        if (selected == null) return;

        Object value = scheduleModel.getValueAt(row, 1);
        Student s = (value instanceof Student) ? (Student) value : null;

        // Persist change using manager
        List<Evaluator> currentEvaluators = selected.getSchedule().get(row).getEvaluators();
        sessionManager.updateSlotAssignment(selected, row, s, currentEvaluators);
    }

    private void openMultiEvaluatorDialog(int row) {
        Session selected = (Session) cbSessionSelector.getSelectedItem();
        if (selected == null) return;

        Session.PresentationSlot slot = selected.getSchedule().get(row);
        List<Evaluator> currentlyAssigned = slot.getEvaluators();
        List<JCheckBox> checkBoxes = new ArrayList<>();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        
        for (Evaluator ev : sessionManager.getAllEvaluators()) {
            JCheckBox box = new JCheckBox(ev.toString());
            
            if (currentlyAssigned.contains(ev)) {
                box.setSelected(true);
            }
            
            box.putClientProperty("evaluatorObj", ev);
            checkBoxes.add(box);
            panel.add(box);
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setPreferredSize(new Dimension(300, 200));

        int result = JOptionPane.showConfirmDialog(this, scrollPane, 
                "Select Evaluators for " + slot.getTimeRange(), 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

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

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Nimbus not available, using default.");
        }

        SwingUtilities.invokeLater(() -> new CoordinatorDashboard().setVisible(true));
    }
}