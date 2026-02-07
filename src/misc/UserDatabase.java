package misc;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import coordinator.Session;
import evaluator.Evaluator;
import student.Student;

public class UserDatabase {

    private static final String STUDENT_FILE = "./src/saved/students.txt";
    private static final String EVALUATOR_FILE = "./src/saved/evaluators.txt";
    private static final String COORDINATOR_FILE = "./src/saved/coordinators.txt";
    private static final String SESSIONS_FILE = "./src/saved/sessions.txt";
    private static final String GRADES_FILE = "./src/saved/grades.csv";
    private static final String AWARDS_FILE = "./src/saved/awards.txt";

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
                    lineToWrite = id + "|" + password + "|New Student|None|None|None|None|None|None|None";
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
                    // Updated to handle 10 fields (including boardId)
                    String researchTitle = (p.length > 3 && !p[3].equals("None")) ? p[3] : "None";
                    String abstractText = (p.length > 4 && !p[4].equals("None")) ? p[4] : "None";
                    String supervisor = (p.length > 5 && !p[5].equals("None")) ? p[5] : "None";
                    String presentationType = (p.length > 6 && !p[6].equals("None")) ? p[6] : "None";
                    String submissionPath = (p.length > 7 && !p[7].equals("None")) ? p[7] : "None";
                    String sessionId = (p.length > 8 && !p[8].equals("None")) ? p[8] : "None";
                    String boardId = (p.length > 9 && !p[9].equals("None")) ? p[9] : "None";
                    
                    Student student = new Student(p[0], p[1], p[2], researchTitle, abstractText, 
                                                supervisor, presentationType, submissionPath);
                    student.setSessionId(sessionId);
                    student.setBoardId(boardId);
                    return student;
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
                    String sessionId = (p.length > 8) ? p[8] : "None";
                    String boardId = (p.length > 9) ? p[9] : "None";
                    Student student = new Student(p[0], p[1], p[2], p[3], p[4], p[5], p[6], path);
                    student.setSessionId(sessionId);
                    student.setBoardId(boardId);
                    list.add(student);
                }
            }
        } catch (IOException e) {
        }
        return list;
    }

    // Get students registered for a specific session
    public static List<Student> getStudentsBySession(String sessionId) {
        List<Student> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(STUDENT_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 10) {  // Updated to 10 fields
                    String studentSessionId = p[8];
                    if (studentSessionId.equals(sessionId)) {
                        String path = p[7];
                        String boardId = p[9];
                        Student student = new Student(p[0], p[1], p[2], p[3], p[4], p[5], p[6], path);
                        student.setSessionId(studentSessionId);
                        student.setBoardId(boardId);
                        list.add(student);
                    }
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

    // Get all available sessions
    public static List<String> getAllAvailableSessions() {
        List<String> sessions = new ArrayList<>();
        File file = new File(SESSIONS_FILE);
        
        if (!file.exists()) {
            return sessions;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("Schedule:") || line.startsWith("----")) {
                    continue;
                }
                
                String[] parts = line.split("\\|");
                if (parts.length >= 2) {
                    String sessionId = parts[0].trim();
                    String sessionName = parts[1].trim();
                    sessions.add(sessionId + " - " + sessionName);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading sessions file: " + e.getMessage());
        }
        
        return sessions;
    }

    // Validate if a session exists
    public static boolean sessionExists(String sessionId) {
        File file = new File(SESSIONS_FILE);
        
        if (!file.exists()) {
            return false;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("Schedule:") || line.startsWith("----")) {
                    continue;
                }
                
                String[] parts = line.split("\\|");
                if (parts.length >= 1 && parts[0].trim().equals(sessionId)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading sessions file: " + e.getMessage());
        }
        
        return false;
    }

    // Get sessions where evaluator is assigned
    public static List<String> getSessionsForEvaluator(String evaluatorId) {
        List<String> sessions = new ArrayList<>();
        File file = new File(SESSIONS_FILE);
        
        if (!file.exists()) {
            return sessions;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String currentSessionId = null;
            String currentSessionInfo = null;
            
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("----")) {
                    continue;
                }
                
                if (!line.startsWith("Schedule:")) {
                    // This is a session info line
                    String[] parts = line.split("\\|");
                    if (parts.length >= 2) {
                        currentSessionId = parts[0].trim();
                        currentSessionInfo = parts[0] + "|" + parts[1] + "|" + 
                                           (parts.length > 6 ? parts[6] : "") + "|" + 
                                           (parts.length > 7 ? parts[7] : "");
                    }
                } else if (line.startsWith("Schedule:") && currentSessionId != null) {
                    // Check if evaluator is in this session's schedule
                    if (line.contains(evaluatorId)) {
                        sessions.add(currentSessionInfo);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading sessions for evaluator: " + e.getMessage());
        }
        
        return sessions;
    }

    // Get time slots for evaluator in a specific session
    public static List<String> getTimeSlotsForEvaluator(String evaluatorId, String sessionId) {
        List<String> timeSlots = new ArrayList<>();
        File file = new File(SESSIONS_FILE);
        
        if (!file.exists()) {
            return timeSlots;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String currentSessionId = null;
            boolean readingTargetSession = false;
            
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("----")) {
                    readingTargetSession = false;
                    continue;
                }
                
                if (!line.startsWith("Schedule:")) {
                    // This is a session info line
                    String[] parts = line.split("\\|");
                    if (parts.length >= 1) {
                        currentSessionId = parts[0].trim();
                        readingTargetSession = currentSessionId.equals(sessionId);
                    }
                } else if (line.startsWith("Schedule:") && readingTargetSession) {
                    // Parse schedule line
                    String scheduleData = line.substring(9); // Remove "Schedule:"
                    String[] slots = scheduleData.split(",");
                    
                    for (String slot : slots) {
                        if (slot.trim().isEmpty()) continue;
                        
                        String[] components = slot.split(":");
                        if (components.length >= 3) {
                            String slotIndex = components[0];
                            String evaluatorIds = components[2];
                            
                            if (!evaluatorIds.equals("null") && evaluatorIds.contains(evaluatorId)) {
                                // Get time slot info
                                try {
                                    int index = Integer.parseInt(slotIndex);
                                    // Calculate time based on session data
                                    timeSlots.add("Slot " + (index + 1));
                                } catch (NumberFormatException e) {
                                    // Skip invalid slot
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading time slots: " + e.getMessage());
        }
        
        return timeSlots;
    }

    // Save grades to CSV file
    public static void saveGrade(String sessionId, String studentId, String evaluatorId, 
                                String slot, int[] rubrics, int totalGrade) {
        try {
            // Create directory if it doesn't exist
            File gradesDir = new File("./src/saved/");
            if (!gradesDir.exists()) {
                gradesDir.mkdirs();
            }
            
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(GRADES_FILE, true))) {
                String csvLine = String.format("%s,%s,%s,%s,%d,%d,%d,%d,%d",
                    sessionId, studentId, evaluatorId, slot,
                    rubrics[0], rubrics[1], rubrics[2], rubrics[3],
                    totalGrade);
                bw.write(csvLine);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving grade: " + e.getMessage());
        }
    }

    // NEW: Get average grade for a student in a session
    public static double getStudentAverageGrade(String sessionId, String studentId) {
        File file = new File(GRADES_FILE);
        if (!file.exists()) {
            return 0.0;
        }

        List<Integer> grades = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 9) {
                    if (parts[0].equals(sessionId) && parts[1].equals(studentId)) {
                        try {
                            int grade = Integer.parseInt(parts[8]); // totalGrade is at index 8
                            grades.add(grade);
                        } catch (NumberFormatException e) {
                            // Skip invalid grade
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading grades: " + e.getMessage());
        }
        
        if (grades.isEmpty()) {
            return 0.0;
        }
        
        int sum = 0;
        for (int grade : grades) {
            sum += grade;
        }
        return (double) sum / grades.size();
    }

    // NEW: Get all students with their average grades for a session
    public static List<StudentGrade> getStudentsWithGrades(String sessionId) {
        List<StudentGrade> studentGrades = new ArrayList<>();
        
        // Get all students registered for this session
        List<Student> students = getStudentsBySession(sessionId);
        
        for (Student student : students) {
            double avgGrade = getStudentAverageGrade(sessionId, student.getStudentId());
            studentGrades.add(new StudentGrade(student, avgGrade));
        }
        
        // Sort by average grade (highest to lowest)
        studentGrades.sort((sg1, sg2) -> Double.compare(sg2.getAverageGrade(), sg1.getAverageGrade()));
        
        return studentGrades;
    }

    // NEW: Save award nomination
    public static void saveAward(String studentId, String sessionId, String awardType) {
        try {
            File awardsDir = new File("./src/saved/");
            if (!awardsDir.exists()) {
                awardsDir.mkdirs();
            }
            
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(AWARDS_FILE, true))) {
                String awardLine = String.format("%s|%s|%s|%s",
                    studentId, sessionId, awardType, new Date().toString());
                bw.write(awardLine);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving award: " + e.getMessage());
        }
    }

    // NEW: Check if student already has an award for this session
    public static boolean hasAward(String studentId, String sessionId) {
        File file = new File(AWARDS_FILE);
        if (!file.exists()) {
            return false;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    if (parts[0].equals(studentId) && parts[1].equals(sessionId)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading awards: " + e.getMessage());
        }
        
        return false;
    }

    // NEW: Inner class to hold student with grade
    public static class StudentGrade {
        private Student student;
        private double averageGrade;
        
        public StudentGrade(Student student, double averageGrade) {
            this.student = student;
            this.averageGrade = averageGrade;
        }
        
        public Student getStudent() {
            return student;
        }
        
        public double getAverageGrade() {
            return averageGrade;
        }
        
        public String getFormattedGrade() {
            if (averageGrade == 0.0) {
                return "No grades yet";
            }
            return String.format("%.1f/40", averageGrade);
        }
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