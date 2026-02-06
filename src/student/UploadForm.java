package student;
import java.io.File;
import javax.swing.*;

import misc.Database;
import misc.FileHandler;

    public class UploadForm extends JFrame {

    private Student student;
    private RegistrationForm parent;

    JTextField pathField;
    File selectedFile;

    public UploadForm(Student student, RegistrationForm parent) {
        this.student = student;
        this.parent = parent;

        setTitle("Upload Materials");
        setSize(400, 150);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JButton browseBtn = new JButton("Browse");
        JButton uploadBtn = new JButton("Upload");
        pathField = new JTextField();

        add(pathField);
        add(browseBtn);
        add(uploadBtn);

        browseBtn.addActionListener(e -> browseFile());
        uploadBtn.addActionListener(e -> upload());

        setVisible(true);
    }


    private void browseFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            pathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void upload() {
    try {
        if (!FileHandler.validateFile(selectedFile)) {
            JOptionPane.showMessageDialog(this, "Invalid file", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String savedPath = FileHandler.uploadFile(selectedFile);

        student.setSubmissionPath(savedPath);

        Database.saveStudent(student);

        JOptionPane.showMessageDialog(this, "Registration & Upload Successful");

        // Close both forms
        this.dispose();
        parent.dispose();

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Upload Failed", "Error", JOptionPane.ERROR_MESSAGE);
    }
}


}
