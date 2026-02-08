package evaluator;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.*;
import misc.Dashboard;

public class EvaluatorDashboard extends Dashboard {

    private JTabbedPane tabbedPane;
    private JTable sessionsTable;

    public EvaluatorDashboard(String userId) {
        super(userId, "Evaluator Dashboard - FCSIT Seminar System");
        setSize(1100, 650);
        setLocationRelativeTo(null);
    }

    @Override
    protected void buildDashboard() {
        tabbedPane = new JTabbedPane();

        buildViewScheduleTab();
        buildSessionsTab();

        contentPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    private void buildViewScheduleTab() {
        JTable scheduleTable = new JTable(getTableModel());
        JScrollPane pane = new JScrollPane(scheduleTable);
        tabbedPane.addTab("View Schedule", pane);
    }

    private void buildSessionsTab() {

        String[] columnNames = {"SessionID", "SessionName", "Time", "Slots", "View"};

        DefaultTableModel model = new DefaultTableModel(getRowData(), columnNames) {
            public boolean isCellEditable(int row, int col) {
                return col == 3 || col == 4;
            }
        };

        sessionsTable = new JTable(model);
        sessionsTable.setRowHeight(30);

        TableColumn slotColumn = sessionsTable.getColumnModel().getColumn(3);
        slotColumn.setCellEditor(new SlotCellEditor());

        sessionsTable.getColumn("View").setCellRenderer(new ButtonRenderer());
        sessionsTable.getColumn("View").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane pane = new JScrollPane(sessionsTable);
        tabbedPane.addTab("My Sessions", pane);
    }

    public String[][] getRowData() {

        File file = new File("./src/saved/sessions.txt");
        String[][] rowData = new String[99][5];

        if (!file.exists()) return rowData;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int z = 0;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("----"))
                    continue;

                String[] parts = line.split("\\|");

                if (parts.length >= 9) {
                    rowData[z][0] = parts[0];
                    rowData[z][1] = parts[1];
                    rowData[z][2] = parts[6] + " - " + parts[7];
                    rowData[z][3] = "Select Slot";
                    rowData[z][4] = "View";
                    z++;
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading sessions: " + e.getMessage());
        }

        return rowData;
    }

    private DefaultTableModel getTableModel() {
        String[] cols = {"SessionID", "SessionName", "Time"};
        return new DefaultTableModel(getRowData(), cols);
    }

    class SlotCellEditor extends DefaultCellEditor {
        private JComboBox<String> combo;
        private int row;

        public SlotCellEditor() {
            super(new JComboBox<>());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.row = row;

            combo = new JComboBox<>();
            String sessionId = (String) table.getValueAt(row, 0);
            loadSlotsFromSession(sessionId, combo);

            combo.addActionListener(e -> {
                Object sel = combo.getSelectedItem();
                if (sel != null) {
                    table.setValueAt(sel.toString(), row, 3);
                }
            });

            return combo;
        }

        @Override
        public Object getCellEditorValue() {
            Object sel = combo.getSelectedItem();
            return sel != null ? sel.toString() : "Select Slot";
        }
    }

    private void loadSlotsFromSession(String sessionId, JComboBox<String> combo) {
        try (BufferedReader br = new BufferedReader(new FileReader("./src/saved/sessions.txt"))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("----"))
                    continue;

                String[] parts = line.split("\\|");

                if (parts[0].equals(sessionId)) {
                    String start = parts[6];
                    String end = parts[7];
                    int increment = Integer.parseInt(parts[8]);

                    generateSlots(start, end, increment, combo);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Slot load error: " + e.getMessage());
        }
    }

    private void generateSlots(String start, String end, int increment, JComboBox<String> combo) {

        String[] sParts = start.split(":");
        String[] eParts = end.split(":");

        int startMin = Integer.parseInt(sParts[0]) * 60 + Integer.parseInt(sParts[1]);
        int endMin = Integer.parseInt(eParts[0]) * 60 + Integer.parseInt(eParts[1]);

        for (int t = startMin; t + increment <= endMin; t += increment) {

            int next = t + increment;

            String slotStart = String.format("%02d:%02d", t / 60, t % 60);
            String slotEnd = String.format("%02d:%02d", next / 60, next % 60);

            combo.addItem(slotStart + "-" + slotEnd);
        }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setText("View");
            setBackground(new Color(60, 120, 200));
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
            button = new JButton("View");

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

                String sessionId = (String) sessionsTable.getValueAt(selectedRow, 0);
                String sessionName = (String) sessionsTable.getValueAt(selectedRow, 1);
                String slotTime = (String) sessionsTable.getValueAt(selectedRow, 3);

                if (slotTime == null || slotTime.equals("Select Slot")) {
                    JOptionPane.showMessageDialog(null, "Please select a slot first.");
                    clicked = false;
                    return "View";
                }

                if (!isEvaluatorAssignedToSlot(sessionId, slotTime, userId)) {
                    JOptionPane.showMessageDialog(null, "You are NOT assigned to this slot.");
                    clicked = false;
                    return "View";
                }

                new GradeStudentsFrame(userId, sessionId, slotTime, sessionName, EvaluatorDashboard.this);
                setVisible(false);
            }

            clicked = false;
            return "View";
        }
    }

    private boolean isEvaluatorAssignedToSlot(String sessionId, String slotTime, String evaluatorId) {

        try (BufferedReader br = new BufferedReader(new FileReader("./src/saved/sessions.txt"))) {
            String line;

            while ((line = br.readLine()) != null) {

                if (line.startsWith(sessionId + "|")) {

                    String[] parts = line.split("\\|");
                    String start = parts[6];
                    int increment = Integer.parseInt(parts[8]);

                    String scheduleLine = br.readLine();
                    if (scheduleLine == null) return false;

                    scheduleLine = scheduleLine.substring(9);
                    String[] slots = scheduleLine.split(",");

                    int slotIndex = getSlotIndex(start, slotTime, increment);

                    for (String slot : slots) {
                        String[] comp = slot.split(":");

                        if (Integer.parseInt(comp[0]) == slotIndex) {
                            String evaluatorIds = comp[2];
                            return evaluatorIds != null && evaluatorIds.contains(evaluatorId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Check error: " + e.getMessage());
        }

        return false;
    }

    private int getSlotIndex(String sessionStart, String selectedSlot, int increment) {

        String[] startParts = sessionStart.split(":");
        int sessionStartMin = Integer.parseInt(startParts[0]) * 60 + Integer.parseInt(startParts[1]);

        String slotStart = selectedSlot.split("-")[0];
        String[] slotParts = slotStart.split(":");
        int slotStartMin = Integer.parseInt(slotParts[0]) * 60 + Integer.parseInt(slotParts[1]);

        return (slotStartMin - sessionStartMin) / increment;
    }
}
