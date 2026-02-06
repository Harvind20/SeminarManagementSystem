import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserDatabase {

    private static final String STUDENT_FILE = "students.txt";
    private static final String EVALUATOR_FILE = "evaluators.txt";
    private static final String COORDINATOR_FILE = "coordinators.txt";

    public static boolean verifyLogin(String id, String password, String role) {
        String filename = getFileByRole(role);
        if (filename == null) {
            return false;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 2) {
                    if (parts[0].trim().equals(id) && parts[1].trim().equals(password)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading " + filename + ": " + e.getMessage());
        }
        return false;
    }

    public static void registerUser(String id, String password, String role) {
        String filename = getFileByRole(role);
        if (filename == null) {
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true))) {
            String lineToWrite = "";

            switch (role) {
                case "Student":
                    lineToWrite = id + "|" + password + "|New Student|None|None|None|None|None";
                    break;
                case "Evaluator":
                    lineToWrite = id + "|" + password + "|New Evaluator|None";
                    break;
                case "Coordinator":
                    lineToWrite = id + "|" + password + "|New Coordinator";
                    break;
            }

            if (!lineToWrite.isEmpty()) {
                bw.write(lineToWrite);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error registering user: " + e.getMessage());
        }
    }

    public static void saveStudentRegistration(Student updatedStudent) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(STUDENT_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p[0].equals(updatedStudent.getStudentId())) {
                    lines.add(updatedStudent.toFileString());
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(STUDENT_FILE))) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Student getStudentById(String id) {
        try (BufferedReader br = new BufferedReader(new FileReader(STUDENT_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p[0].equals(id)) {
                    String path = (p.length > 7) ? p[7] : "None";
                    return new Student(p[0], p[1], p[2], p[3], p[4], p[5], p[6], path);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(STUDENT_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 7) {
                    String path = (p.length > 7) ? p[7] : "None";
                    list.add(new Student(p[0], p[1], p[2], p[3], p[4], p[5], p[6], path));
                }
            }
        } catch (IOException e) {
        }
        return list;
    }

    public static List<Evaluator> getAllEvaluators() {
        List<Evaluator> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(EVALUATOR_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 4) {
                    list.add(new Evaluator(p[0], p[2], p[3]));
                }
            }
        } catch (IOException e) {
        }
        return list;
    }

    private static String getFileByRole(String role) {
        switch (role) {
            case "Student":
                return STUDENT_FILE;
            case "Evaluator":
                return EVALUATOR_FILE;
            case "Coordinator":
                return COORDINATOR_FILE;
            default:
                return null;
        }
    }
}