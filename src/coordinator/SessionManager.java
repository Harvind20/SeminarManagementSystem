package coordinator;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import evaluator.Evaluator;
import misc.UserDatabase;
import student.Student;

public class SessionManager {
    private List<Session> sessions;
    private List<Student> allStudents;
    private List<Evaluator> allEvaluators;

    public SessionManager() {
        sessions = new ArrayList<>();
        allStudents = new ArrayList<>();
        allEvaluators = new ArrayList<>();
        
        loadStudents();
        loadEvaluators();
        
        loadSessions();
    }

    public String generateSessionId() {
        loadSessions();
        int maxId = 0;
        
        for (Session session : sessions) {
            try {
                if (session.getSessionId().startsWith("S")) {
                    String numStr = session.getSessionId().substring(1);
                    int idNum = Integer.parseInt(numStr);
                    if (idNum > maxId) {
                        maxId = idNum;
                    }
                }
            } catch (NumberFormatException e) {}
        }
        
        return "S" + (maxId + 1);
    }

    public Session createSession(String id, String name, String type, String track, 
                                 LocalDate date, String venue, LocalTime start, LocalTime end, int duration) {
        Session newSession = new Session(id, name, type, track, date, venue, start, end, duration);
        sessions.add(newSession);
        saveSessions();
        return newSession;
    }

    public void updateSlotAssignment(Session session, int slotIndex, Student s, List<Evaluator> evaluators) {
        if(slotIndex >= 0 && slotIndex < session.getSchedule().size()) {

            Session.PresentationSlot slot = session.getSchedule().get(slotIndex);
            slot.setStudent(s);
            slot.setEvaluators(evaluators);

            saveSessions();

            String timeSlot = slot.getTimeRange();

            updateStudentEvaluatorsAndSlot(s, evaluators, timeSlot);
        }
    }

    private void loadStudents() {
        this.allStudents = UserDatabase.getAllStudents();
    }

    private void loadEvaluators() {
        this.allEvaluators = UserDatabase.getAllEvaluators();
    }

    public List<Student> getStudentsBySession(String sessionId) {
        return UserDatabase.getStudentsBySession(sessionId);
    }

    public void saveSessions() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("./src/saved/sessions.txt"))) {
            for (Session s : sessions) {
                writer.println(s.getSessionId() + "|" + s.getSessionName() + "|" + s.getSessionType() + "|" + s.getSessionTrack() + "|" +
                               s.getDate() + "|" + s.getVenue() + "|" + s.getStartTime() + "|" + s.getEndTime() + "|" + s.getDurationPerStudent());
                
                StringBuilder scheduleStr = new StringBuilder("Schedule:");
                for (int i = 0; i < s.getSchedule().size(); i++) {
                    Session.PresentationSlot slot = s.getSchedule().get(i);
                    
                    String sId = (slot.getStudent() != null) ? slot.getStudent().getStudentId() : "null";
                    
                    String eIds = "null";
                    if (!slot.getEvaluators().isEmpty()) {
                        eIds = slot.getEvaluators().stream()
                                .map(Evaluator::getEvaluatorId)
                                .collect(Collectors.joining("&"));
                    }

                    scheduleStr.append(i).append(":").append(sId).append(":").append(eIds).append(",");
                }
                writer.println(scheduleStr.toString());
                writer.println("----"); 
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadSessions() {
        sessions.clear();
        File file = new File("./src/saved/sessions.txt");
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("----")) continue;

                String[] parts = line.split("\\|");
                if (parts.length >= 9) {
                    Session s = new Session(
                        parts[0],
                        parts[1],
                        parts[2],
                        parts[3],
                        LocalDate.parse(parts[4]),
                        parts[5],
                        LocalTime.parse(parts[6]),
                        LocalTime.parse(parts[7]),
                        Integer.parseInt(parts[8])
                    );

                    String scheduleLine = br.readLine();
                    if (scheduleLine != null && scheduleLine.startsWith("Schedule:")) {
                        parseScheduleLine(s, scheduleLine.substring(9));
                    }

                    sessions.add(s);
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading sessions: " + e.getMessage());
        }
    }

    private void parseScheduleLine(Session session, String data) {
        String[] slots = data.split(",");
        
        for (String slotStr : slots) {
            String[] components = slotStr.split(":");
            if (components.length >= 3) {
                try {
                    int index = Integer.parseInt(components[0]);
                    String studentId = components[1];
                    String evaluatorIds = components[2];

                    if (index < session.getSchedule().size()) {
                        Session.PresentationSlot slot = session.getSchedule().get(index);

                        if (!studentId.equals("null")) {
                            for (Student st : allStudents) {
                                if (st.getStudentId().equals(studentId)) {
                                    slot.setStudent(st);
                                    break;
                                }
                            }
                        }

                        if (!evaluatorIds.equals("null")) {
                            String[] eIds = evaluatorIds.split("&");
                            for (String eId : eIds) {
                                for (Evaluator ev : allEvaluators) {
                                    if (ev.getEvaluatorId().equals(eId)) {
                                        slot.addEvaluator(ev);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {}
            }
        }
    }

    private void updateStudentEvaluatorsAndSlot(Student student, List<Evaluator> evaluators, String timeSlot) {
        File inputFile = new File("./src/saved/students.txt");
        File tempFile = new File("./src/saved/students_temp.txt");

        try (
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            PrintWriter writer = new PrintWriter(new FileWriter(tempFile))
        ) {
            String line;

            String evaluatorStr = "None";
            if (!evaluators.isEmpty()) {
                evaluatorStr = evaluators.stream()
                        .map(Evaluator::getEvaluatorId)
                        .collect(Collectors.joining("&"));
            }

            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|");

                if (data[0].equals(student.getStudentId())) {

                    String[] newData = new String[12];

                    for (int i = 0; i < newData.length; i++) {
                        newData[i] = (i < data.length) ? data[i] : "None";
                    }

                    newData[10] = evaluatorStr;

                    newData[11] = timeSlot;

                    writer.println(String.join("|", newData));
                } else {
                    writer.println(line);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        inputFile.delete();
        tempFile.renameTo(inputFile);
    }

    public List<Session> getAllSessions() { return sessions; }
    public List<Student> getAllStudents() { return allStudents; }
    public List<Evaluator> getAllEvaluators() { return allEvaluators; }
}
