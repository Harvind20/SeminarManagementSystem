package evaluator;
import misc.Dashboard;
import evaluator.Evaluation;

import java.awt.*;
import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalTime;
import java.util.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;


import javax.swing.*;

public class EvaluatorDashboard extends Dashboard{
    private JTabbedPane tabbedPane;
    private JScrollPane sessionsTablePane;
    private JTable sessionsTable;
    
    public EvaluatorDashboard(String UserId){
        super(UserId,"Evaluator Dashboard - FCSIT Seminar System");
        setSize(1100,650);
        setLocationRelativeTo(null);
    }

    @Override
    protected void buildDashboard(){
        tabbedPane = new JTabbedPane();
        buildSessionsTablePane();
        contentPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    private void buildSessionsTablePane(){
        String[] columnNames = {"ID", "Name", "Time", "Slots", "View"};
        DefaultTableModel tableModel = new DefaultTableModel(getRowData(),columnNames){
            @Override
            public boolean isCellEditable(int row, int column){
                return (column == 3 && column == 4);
            }
        };
        sessionsTable = new JTable(getRowData(),columnNames);
        TableColumn slotColumn = sessionsTable.getColumnModel().getColumn(3);
        //sessionsTable.setDefaultEditor(Object.class, null);
        sessionsTablePane = new JScrollPane(sessionsTable);
        sessionsTable.setFillsViewportHeight(true);

        sessionsTablePane.setLayout(new ScrollPaneLayout());
        tabbedPane.addTab("View Sessions", sessionsTablePane);
    }

    public String[][] getRowData(){
        File file = new File("./src/saved/sessions.txt");
        String[][] rowData = new String[99][5];
        if (!file.exists()) return rowData;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line; int z = 0;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("----")) {
                    z += 1;
                    continue;
                }
                String[] parts = line.split("\\|");
                if (parts.length >= 9){
                    rowData[z][0] = parts[0];
                    rowData[z][1] = parts[1];
                    rowData[z][2] = parts[6] + " - " + parts[7];
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading sessions: " + e.getMessage());
        }
        return rowData;
    }
}
