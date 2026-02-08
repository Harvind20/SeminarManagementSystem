package misc;

import evaluator.Evaluator;
import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import student.Student;

public class UserDatabase {
    private static final String STUDENT_FILE = "./saved/students.txt";
    private static final String EVALUATOR_FILE = "./saved/evaluators.txt";
    private static final String COORDINATOR_FILE = "./saved/coordinators.txt";
    private static final String SESSIONS_FILE = "./saved/sessions.txt";
    private static final String GRADES_FILE = "./saved/evaluations.csv";
    private static final String AWARDS_FILE = "./saved/awards.txt";

    public static boolean verifyLogin(String id, String password, String role) {
        String filename = getFileByRole(role);
        if (filename == null) return false;

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
        if (filename == null) return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true))) {
            String lineToWrite = "";
            switch (role) {
                case "Student":
                    lineToWrite = id + "|" + password + "|New Student|None|None|None|None|None|None|None";
                    break;
                case "Evaluator":
                    lineToWrite = id + "|" + password + "|New Evaluator|None|";
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

    private static String getFileByRole(String role) {
        switch (role) {
            case "Student": return STUDENT_FILE;
            case "Evaluator": return EVALUATOR_FILE;
            case "Coordinator": return COORDINATOR_FILE;
            default: return null;
        }
    }

    public static void saveStudentRegistration(Student s) {
        List<String> lines = new ArrayList<>();
        boolean updated = false;
        
        try (BufferedReader br = new BufferedReader(new FileReader(STUDENT_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p[0].equals(s.getStudentId())) {
                    String currentSession = (p.length > 8) ? p[8] : "None";
                    if (currentSession.equals(s.getSessionId())) {
                        lines.add(s.toFileString());
                        updated = true;
                    } else if (currentSession.equals("None")) {
                        if (!updated) {
                            lines.add(s.toFileString());
                            updated = true;
                        } else {
                            lines.add(line); 
                        }
                    } else {
                        lines.add(line);
                    }
                } else {
                    lines.add(line);
                }
            }
        } catch (Exception e) {}

        if (!updated) {
            lines.add(s.toFileString());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(STUDENT_FILE))) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void unregisterStudent(String studentId, String sessionId) {
        List<String> lines = new ArrayList<>();
        int studentEntryCount = 0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(STUDENT_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(studentId + "|")) studentEntryCount++;
            }
        } catch (Exception e) {}

        try (BufferedReader br = new BufferedReader(new FileReader(STUDENT_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p[0].equals(studentId) && p.length > 8 && p[8].equals(sessionId)) {
                    if (studentEntryCount <= 1) {
                        Student reset = new Student(p[0], p[1], p[2], "None", "None", "None", "None", "None");
                        reset.setSessionId("None");
                        reset.setBoardId("None");
                        lines.add(reset.toFileString());
                    }
                } else {
                    lines.add(line);
                }
            }
        } catch (Exception e) {}

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(STUDENT_FILE))) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static Student getStudentById(String id) {
        try (BufferedReader br = new BufferedReader(new FileReader(STUDENT_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p[0].equals(id)) {
                    String title = (p.length > 3) ? p[3] : "None";
                    String abs = (p.length > 4) ? p[4] : "None";
                    String sup = (p.length > 5) ? p[5] : "None";
                    String type = (p.length > 6) ? p[6] : "None";
                    String path = (p.length > 7) ? p[7] : "None";
                    String sess = (p.length > 8) ? p[8] : "None";
                    String board = (p.length > 9) ? p[9] : "None";

                    Student s = new Student(p[0], p[1], p[2], title, abs, sup, type, path);
                    s.setSessionId(sess);
                    s.setBoardId(board);
                    return s;
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    }

    public static List<Student> getStudentRegistrations(String studentId) {
        List<Student> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(STUDENT_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p[0].equals(studentId) && p.length > 8 && !p[8].equals("None")) {
                    String title = (p.length > 3) ? p[3] : "None";
                    String abs = (p.length > 4) ? p[4] : "None";
                    String sup = (p.length > 5) ? p[5] : "None";
                    String type = (p.length > 6) ? p[6] : "None";
                    String path = (p.length > 7) ? p[7] : "None";
                    String sess = (p.length > 8) ? p[8] : "None";
                    String board = (p.length > 9) ? p[9] : "None";

                    Student s = new Student(p[0], p[1], p[2], title, abs, sup, type, path);
                    s.setSessionId(sess);
                    s.setBoardId(board);
                    list.add(s);
                }
            }
        } catch (Exception e) {}
        return list;
    }

    public static List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(STUDENT_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 7) {
                    String path = (p.length > 7) ? p[7] : "None";
                    Student s = new Student(p[0], p[1], p[2], p[3], p[4], p[5], p[6], path);
                    if(p.length > 8) s.setSessionId(p[8]);
                    if(p.length > 9) s.setBoardId(p[9]);
                    list.add(s);
                }
            }
        } catch (IOException e) {}
        return list;
    }
    
    public static List<Student> getStudentsBySession(String sessionId) {
        List<Student> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(STUDENT_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 9 && p[8].equals(sessionId)) {
                    String title = (p.length > 3) ? p[3] : "None";
                    String abs = (p.length > 4) ? p[4] : "None";
                    String sup = (p.length > 5) ? p[5] : "None";
                    String type = (p.length > 6) ? p[6] : "None";
                    String path = (p.length > 7) ? p[7] : "None";
                    String sess = (p.length > 8) ? p[8] : "None";
                    String board = (p.length > 9) ? p[9] : "None";

                    Student s = new Student(p[0], p[1], p[2], title, abs, sup, type, path);
                    s.setSessionId(sess);
                    s.setBoardId(board);
                    list.add(s);
                }
            }
        } catch (IOException e) {}
        return list;
    }

    public static Evaluator getEvaluatorById(String id) {
        try (BufferedReader br = new BufferedReader(new FileReader(EVALUATOR_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p[0].equals(id)) {
                    String sessions = (p.length > 4) ? p[4] : ""; 
                    return new Evaluator(p[0], p[2], p[3], sessions);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    public static List<Evaluator> getAllEvaluators() {
        List<Evaluator> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(EVALUATOR_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 4) {
                    String sessions = (p.length > 4) ? p[4] : "";
                    list.add(new Evaluator(p[0], p[2], p[3], sessions));
                }
            }
        } catch (IOException e) {}
        return list;
    }

    public static String[] getSessionDetails(String sessionId) {
        try (BufferedReader br = new BufferedReader(new FileReader(SESSIONS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(line.startsWith("Schedule:") || line.startsWith("----")) continue;
                String[] p = line.split("\\|");
                if (p[0].equals(sessionId)) {
                    String start = p[6].contains(".") ? p[6].substring(0, p[6].indexOf(".")) : p[6];
                    String end = p[7].contains(".") ? p[7].substring(0, p[7].indexOf(".")) : p[7];
                    return new String[]{p[1], start + " - " + end}; 
                }
            }
        } catch (Exception e) {}
        return new String[]{"Unknown", "Unknown"};
    }
    
    public static List<String> getAllAvailableSessions() {
        List<String> sessions = new ArrayList<>();
        File file = new File(SESSIONS_FILE);
        if (!file.exists()) return sessions;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("Schedule:") || line.startsWith("----")) continue;
                String[] p = line.split("\\|");
                if (p.length >= 2) {
                    sessions.add(p[0].trim() + " - " + p[1].trim());
                }
            }
        } catch (IOException e) {}
        return sessions;
    }

    public static boolean sessionExists(String sessionId) {
        for(String s : getAllAvailableSessions()) {
            if(s.startsWith(sessionId + " - ")) return true;
        }
        return false;
    }

    public static String getEvaluatorSlotDetails(String sessionId, String evaluatorId) {
        StringBuilder details = new StringBuilder("<html>");
        try (BufferedReader br = new BufferedReader(new FileReader(SESSIONS_FILE))) {
            String line;
            LocalTime sessionStart = null;
            int duration = 20;

            while ((line = br.readLine()) != null) {
                if (line.startsWith(sessionId + "|")) {
                    String[] p = line.split("\\|");
                    if (p.length >= 9) {
                        try {
                            sessionStart = LocalTime.parse(p[6]);
                            duration = Integer.parseInt(p[8]);
                        } catch (Exception e) {
                            sessionStart = LocalTime.of(9, 0);
                        }
                    }

                    String schedLine = br.readLine();
                    if (schedLine != null && schedLine.startsWith("Schedule:")) {
                        String[] parts = schedLine.substring(9).split(",");
                        for (String part : parts) {
                            String[] data = part.split(":");
                            if (data.length >= 3 && data[2].contains(evaluatorId)) {
                                int slotIndex = Integer.parseInt(data[0]);
                                LocalTime slotStart = sessionStart.plusMinutes((long) slotIndex * duration);
                                LocalTime slotEnd = slotStart.plusMinutes(duration);
                                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
                                details.append("Slot ").append(slotIndex + 1).append(" : ")
                                       .append(slotStart.format(fmt)).append("-")
                                       .append(slotEnd.format(fmt)).append("<br>");
                            }
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        if (details.toString().equals("<html>")) return "No slots assigned";
        return details.append("</html>").toString();
    }

    public static String getStudentSlotTime(String sessionId, String studentId) {
        try (BufferedReader br = new BufferedReader(new FileReader(SESSIONS_FILE))) {
            String line;
            LocalTime sessionStart = null;
            int duration = 20;

            while ((line = br.readLine()) != null) {
                if (line.startsWith(sessionId + "|")) {
                    String[] p = line.split("\\|");
                    try {
                        sessionStart = LocalTime.parse(p[6]);
                        duration = Integer.parseInt(p[8]);
                    } catch (Exception e) {
                        sessionStart = LocalTime.of(9, 0);
                    }

                    String schedLine = br.readLine();
                    if (schedLine != null && schedLine.startsWith("Schedule:")) {
                        String[] parts = schedLine.substring(9).split(",");
                        for (String part : parts) {
                            String[] data = part.split(":");
                            if (data.length >= 2 && data[1].equals(studentId)) {
                                int slotIndex = Integer.parseInt(data[0]);
                                LocalTime slotStart = sessionStart.plusMinutes((long) slotIndex * duration);
                                LocalTime slotEnd = slotStart.plusMinutes(duration);
                                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
                                return "Slot " + (slotIndex + 1) + " (" + slotStart.format(fmt) + "-" + slotEnd.format(fmt) + ")";
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {}
        return "Unassigned";
    }

    public static boolean isEvaluatorAssignedToStudent(String sessionId, String studentId, String evaluatorId) {
        try (BufferedReader br = new BufferedReader(new FileReader(SESSIONS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(sessionId + "|")) {
                    String sched = br.readLine();
                    if (sched != null && sched.startsWith("Schedule:")) {
                        String[] slots = sched.substring(9).split(",");
                        for (String slot : slots) {
                            String[] parts = slot.split(":"); 
                            if (parts.length >= 3 && parts[1].equals(studentId)) {
                                if (parts[2].contains(evaluatorId)) return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {}
        return false;
    }

    public static List<String> getEvaluatorSlots(String sessionId, String evaluatorId) {
        List<String> slots = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(SESSIONS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(sessionId + "|")) {
                    String sched = br.readLine(); 
                    if (sched != null && sched.startsWith("Schedule:")) {
                        String[] parts = sched.substring(9).split(",");
                        for (String part : parts) {
                            String[] details = part.split(":");
                            if (details.length >= 3 && details[2].contains(evaluatorId)) {
                                slots.add("Slot " + (Integer.parseInt(details[0]) + 1));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return slots;
    }

    public static List<Student> getStudentsInSlot(String sessionId, int slotIndex) {
        List<Student> students = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(SESSIONS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(sessionId + "|")) {
                    String schedLine = br.readLine();
                    String[] slots = schedLine.substring(9).split(",");
                    for (String part : slots) {
                        String[] details = part.split(":");
                        if (Integer.parseInt(details[0]) == slotIndex) {
                            String sId = details[1];
                            if (!sId.equals("null")) {
                                Student s = getStudentById(sId);
                                if (s != null) students.add(s);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return students;
    }

    public static boolean hasEvaluatorGraded(String studentId, String evaluatorId) {
        File f = new File(GRADES_FILE);
        if(!f.exists()) return false;
        
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length > 2 && p[1].equals(studentId) && p[2].equals(evaluatorId)) {
                    return true;
                }
            }
        } catch (Exception e) {}
        return false;
    }

    public static int[] getEvaluatorGrades(String studentId, String evaluatorId) {
        File f = new File(GRADES_FILE);
        if(!f.exists()) return null;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length > 7 && p[1].equals(studentId) && p[2].equals(evaluatorId)) {
                    return new int[]{
                        Integer.parseInt(p[3]), Integer.parseInt(p[4]),
                        Integer.parseInt(p[5]), Integer.parseInt(p[6])
                    };
                }
            }
        } catch (Exception e) {}
        return null;
    }

    public static String getEvaluatorComments(String studentId, String evaluatorId) {
        File f = new File(GRADES_FILE);
        if (!f.exists()) return "";
        
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length > 8 && p[1].equals(studentId) && p[2].equals(evaluatorId)) {
                    return p[8];
                }
            }
        } catch (Exception e) {}
        return "";
    }

    public static void saveGrade(String sessionId, String studentId, String evaluatorId, int[] scores, String comments) {
        List<String> allLines = new ArrayList<>();
        int total = Arrays.stream(scores).sum();
        
        String safeComment = (comments == null) ? "" : comments.replace("\n", " ").replace(",", ";");
        
        String newLine = String.format("%s,%s,%s,%d,%d,%d,%d,%d,%s", 
            sessionId, studentId, evaluatorId, scores[0], scores[1], scores[2], scores[3], total, safeComment);
        
        boolean updated = false;
        File f = new File(GRADES_FILE);
        
        if (f.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(",");
                    if (p.length > 2 && p[1].equals(studentId) && p[2].equals(evaluatorId)) {
                        allLines.add(newLine); 
                        updated = true;
                    } else {
                        allLines.add(line);
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        if (!updated) allLines.add(newLine);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(GRADES_FILE))) {
            for (String l : allLines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void saveGrade(String sessionId, String studentId, String evaluatorId, int[] scores) {
        saveGrade(sessionId, studentId, evaluatorId, scores, "");
    }

    public static double[] getStudentAverages(String studentId) {
        double[] sums = new double[5]; 
        int count = 0;
        
        File f = new File(GRADES_FILE);
        if (!f.exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length > 7 && p[1].equals(studentId)) {
                    sums[0] += Integer.parseInt(p[3]); 
                    sums[1] += Integer.parseInt(p[4]); 
                    sums[2] += Integer.parseInt(p[5]); 
                    sums[3] += Integer.parseInt(p[6]); 
                    sums[4] += Integer.parseInt(p[7]); 
                    count++;
                }
            }
        } catch (Exception e) {}

        if (count == 0) return null;

        for (int i = 0; i < 5; i++) sums[i] = sums[i] / count;
        return sums;
    }
    
    public static class EvaluationDetail {
        public String evaluatorName;
        public int[] scores; 
        public int total;
        public String comment;

        public EvaluationDetail(String eName, int[] s, int t, String c) {
            evaluatorName = eName; scores = s; total = t; comment = c;
        }
    }

    public static List<EvaluationDetail> getEvaluationDetails(String sessionId, String studentId) {
        List<EvaluationDetail> list = new ArrayList<>();
        File f = new File(GRADES_FILE);
        if(!f.exists()) return list;

        try(BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if(p.length >= 8 && p[0].equals(sessionId) && p[1].equals(studentId)) {
                    String eId = p[2];
                    String eName = getEvaluatorName(eId);
                    int[] scores = {
                        Integer.parseInt(p[3]), Integer.parseInt(p[4]),
                        Integer.parseInt(p[5]), Integer.parseInt(p[6])
                    };
                    int total = Integer.parseInt(p[7]);
                    String comment = (p.length > 8) ? p[8] : "-";
                    
                    list.add(new EvaluationDetail(eName, scores, total, comment));
                }
            }
        } catch(Exception e) {}
        return list;
    }

    private static String getEvaluatorName(String eId) {
        Evaluator e = getEvaluatorById(eId);
        return (e != null) ? e.getName() : eId;
    }
    
    public static String getCommentsForDisplay(String sessionId, String studentId) {
        StringBuilder sb = new StringBuilder("<html>");
        boolean found = false;
        try (BufferedReader br = new BufferedReader(new FileReader(GRADES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length > 8 && p[0].equals(sessionId) && p[1].equals(studentId)) {
                    String evalId = p[2];
                    String comment = p[8].trim();
                    if (!comment.isEmpty() && !comment.equals("null")) {
                        Evaluator ev = getEvaluatorById(evalId);
                        String eName = (ev != null) ? ev.getName() : evalId;
                        
                        if (found) sb.append("<br><br>"); 
                        sb.append("<b>").append(eName).append(":</b> ").append(comment);
                        found = true;
                    }
                }
            }
        } catch (Exception e) {}
        if (!found) return "-";
        return sb.append("</html>").toString();
    }
    
    public static String getCommentsForCSV(String sessionId, String studentId) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(GRADES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length > 8 && p[0].equals(sessionId) && p[1].equals(studentId)) {
                    String evalId = p[2];
                    String comment = p[8].trim();
                    if (!comment.isEmpty() && !comment.equals("null")) {
                        Evaluator ev = getEvaluatorById(evalId);
                        String eName = (ev != null) ? ev.getName() : evalId;
                        
                        if (sb.length() > 0) sb.append(" | "); 
                        sb.append(eName).append(": ").append(comment);
                    }
                }
            }
        } catch (Exception e) {}
        return sb.length() == 0 ? "None" : sb.toString();
    }

    public static void exportStudentListToCSV(String sessionId) {
        String outFile = "./saved/Final_Report_" + sessionId + ".csv";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))) {
            bw.write("Rank,Student ID,Student Name,Average Grade,Award,Evaluator Comments\n");
            
            List<StudentGrade> list = getStudentsWithGrades(sessionId);
            int rank = 1;
            
            for (StudentGrade sg : list) {
                Student s = sg.getStudent();
                String award = getAwardForStudent(s.getStudentId(), sessionId);
                if (award == null) award = "None";
                
                String comments = getCommentsForCSV(sessionId, s.getStudentId());
                
                comments = "\"" + comments.replace("\"", "\"\"") + "\"";
                
                bw.write(String.format("%d,%s,%s,%s,%s,%s\n",
                    rank++,
                    s.getStudentId(),
                    s.getName(),
                    sg.getFormattedGrade(),
                    award,
                    comments
                ));
            }
            System.out.println("Full Report Exported to: " + outFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveAward(String studentId, String sessionId, String awardType) {
        List<String> lines = new ArrayList<>();
        File file = new File(AWARDS_FILE);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 3) {
                        String sessId = parts[1];
                        String aType = parts[2];
                        if (sessId.equals(sessionId) && aType.equals(awardType)) {
                            continue; 
                        }
                    }
                    lines.add(line); 
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
        String newAward = String.format("%s|%s|%s|%s", studentId, sessionId, awardType, new Date().toString());
        lines.add(newAward);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(AWARDS_FILE))) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static Student getStudentWithAward(String sessionId, String awardType) {
        File file = new File(AWARDS_FILE);
        if (!file.exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    if (parts[1].equals(sessionId) && parts[2].equals(awardType)) {
                        return getStudentById(parts[0]);
                    }
                }
            }
        } catch (IOException e) {}
        return null;
    }

    public static String getAwardForStudent(String studentId, String sessionId) {
        File file = new File(AWARDS_FILE);
        if (!file.exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    if (parts[0].equals(studentId) && parts[1].equals(sessionId)) {
                        return parts[2];
                    }
                }
            }
        } catch (IOException e) {}
        return null;
    }
    
    public static boolean hasAward(String studentId, String sessionId) {
        return getAwardForStudent(studentId, sessionId) != null;
    }

    public static class StudentGrade {
        private Student student;
        private double averageGrade;
        
        public StudentGrade(Student student, double averageGrade) {
            this.student = student;
            this.averageGrade = averageGrade;
        }
        public Student getStudent() { return student; }
        public double getAverageGrade() { return averageGrade; }
        public String getFormattedGrade() {
            return (averageGrade == 0.0) ? "No grades yet" : String.format("%.1f/40", averageGrade);
        }
    }

    public static List<StudentGrade> getStudentsWithGrades(String sessionId) {
        List<StudentGrade> studentGrades = new ArrayList<>();
        for (Student s : getStudentsBySession(sessionId)) {
            double[] avgs = getStudentAverages(s.getStudentId());
            double totalAvg = (avgs != null) ? avgs[4] : 0.0;
            studentGrades.add(new StudentGrade(s, totalAvg));
        }
        studentGrades.sort((sg1, sg2) -> Double.compare(sg2.getAverageGrade(), sg1.getAverageGrade()));
        return studentGrades;
    }
}