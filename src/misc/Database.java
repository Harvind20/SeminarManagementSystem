package misc;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import student.Student;

public class Database {

    private static final String FILE_NAME = "students.txt";

    // Save student to text file
    public static void saveStudent(Student student) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(FILE_NAME, true))) {

            String record =
                student.getStudentId() + "|" +
                student.getName() + "|" +
                student.getPresentationType() + "|" +
                student.getSubmissionPath() + "|" +
                student.getSessionId(); // Added sessionId

            writer.write(record);
            writer.newLine();

        } catch (IOException e) {
            System.out.println("Error writing to file");
        }
    }
}