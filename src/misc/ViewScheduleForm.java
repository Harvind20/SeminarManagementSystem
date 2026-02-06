package misc;
import javax.swing.*;
import java.io.*;

public class ViewScheduleForm extends JFrame {

    JTextArea area;

    public ViewScheduleForm() {
        setTitle("Seminar Schedule");
        setSize(500, 300);

        area = new JTextArea();
        area.setEditable(false);

        loadSchedule();

        add(new JScrollPane(area));
        setVisible(true);
    }

    private void loadSchedule() {
        try (BufferedReader br = new BufferedReader(
                new FileReader("schedule.txt"))) {

            String line;
            while ((line = br.readLine()) != null) {
                area.append(line + "\n");
            }

        } catch (IOException e) {
            area.setText("No schedule available.");
        }
    }
}
