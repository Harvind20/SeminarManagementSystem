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

    public CoordinatorDashboard(String userId) {
        super(userId, "Coordinator Dashboard - FCSIT Seminar System");
        setSize(1100, 650);
        setLocationRelativeTo(null);
    }

    @Override
    protected void buildDashboard() {
        sessionManager = new SessionManager();

        tabbedPane = new JTabbedPane();
        initSessionTab();   
        initScheduleTab();  

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

            sessionManager.createSession(
                txtId.getText(), txtName.getText(), cbType.getSelectedItem().toString(),
                cbTrack.getSelectedItem().toString(), localDate, txtVenue.getText(), 
                startTime, endTime, duration
            );

            refreshSessionTable();
            refreshSessionSelector();
            JOptionPane.showMessageDialog(this, "Session created successfully!");
            
            txtId.setText(""); 
            
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

        List<Evaluator> currentEvaluators = selected.getSchedule().get(row).getEvaluators();
        sessionManager.updateSlotAssignment(selected, row, s, currentEvaluators);
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
}