
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SessionManager {

    private List<Session> sessions;
    private List<Student> allStudents;
    private List<Evaluator> allEvaluators;

    public SessionManager() {
        sessions = new ArrayList<>();
        allStudents = new ArrayList<>();
        allEvaluators = new ArrayList<>();

        // Load data on startup
        loadStudents();
        loadEvaluators();
        loadSessions();
    }

    public Session createSession(String id, String name, String type, String track,
            LocalDate date, String venue, LocalTime start, LocalTime end, int duration) {
        Session newSession = new Session(id, name, type, track, date, venue, start, end, duration);
        sessions.add(newSession);
        saveSessions();
        return newSession;
    }

    public void deleteSession(Session s) {
        sessions.remove(s);
        saveSessions();
    }

    public void updateSlotAssignment(Session session, int slotIndex, Student s, List<Evaluator> evaluators) {
        if (slotIndex >= 0 && slotIndex < session.getSchedule().size()) {
            Session.PresentationSlot slot = session.getSchedule().get(slotIndex);
            slot.setStudent(s);
            slot.setEvaluators(evaluators); // Set the full list
            saveSessions();
        }
    }

    private void loadStudents() {
        allStudents.add(new Student("S001", "Ali", "AI in Health", "Abstract...", "Dr. A", "Oral"));
        allStudents.add(new Student("S002", "Bala", "IoT Farming", "Abstract...", "Dr. B", "Poster"));
        allStudents.add(new Student("S003", "Chong", "Crypto Security", "Abstract...", "Dr. C", "Oral"));
    }

    private void loadEvaluators() {
        allEvaluators.add(new Evaluator("E001", "Dr. Sarah", "AI"));
        allEvaluators.add(new Evaluator("E002", "Prof. James", "Security"));
    }

    public void saveSessions() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("sessions.txt"))) {
            for (Session s : sessions) {
                writer.println(s.getSessionId() + "|" + s.getSessionName() + "|" + s.getSessionType() + "|"
                        + s.getDate() + "|" + s.getStartTime() + "|" + s.getEndTime() + "|" + s.getDurationPerStudent());

                StringBuilder scheduleStr = new StringBuilder("Schedule:");
                for (int i = 0; i < s.getSchedule().size(); i++) {
                    Session.PresentationSlot slot = s.getSchedule().get(i);

                    String sId = (slot.getStudent() != null) ? slot.getStudent().getStudentId() : "null";

                    // Join multiple evaluator IDs with '&'
                    String eIds = "null";
                    if (!slot.getEvaluators().isEmpty()) {
                        eIds = slot.getEvaluators().stream()
                                .map(Evaluator::getEvaluatorId)
                                .collect(Collectors.joining("&"));
                    }

                    // Format: Index:Student:Evaluators
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
// need to add later
    }

    public List<Session> getAllSessions() {
        return sessions;
    }

    public List<Student> getAllStudents() {
        return allStudents;
    }

    public List<Evaluator> getAllEvaluators() {
        return allEvaluators;
    }
}
