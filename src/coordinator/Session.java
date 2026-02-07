package coordinator;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import evaluator.Evaluator;
import student.Student;

public class Session {
    private String sessionId;
    private String sessionName;
    private String sessionType;
    private String sessionTrack; 
    private LocalDate date;
    private String venue;
    private LocalTime startTime;
    private LocalTime endTime;
    private int durationPerStudent; // in minutes

    private List<PresentationSlot> schedule;

    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public Session(String sessionId, String sessionName, String sessionType, String sessionTrack,
                   LocalDate date, String venue, LocalTime startTime, LocalTime endTime, int durationPerStudent) {
        this.sessionId = sessionId;
        this.sessionName = sessionName;
        this.sessionType = sessionType;
        this.sessionTrack = sessionTrack;
        this.date = date;
        this.venue = venue;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationPerStudent = durationPerStudent;
        this.schedule = new ArrayList<>();
        
        generateTimeSlots();
    }

    public void generateTimeSlots() {
        schedule.clear();
        LocalTime current = startTime;
        
        // Loop to create slots until we hit the end time
        while (current.plusMinutes(durationPerStudent).isBefore(endTime) || 
               current.plusMinutes(durationPerStudent).equals(endTime)) {
            
            schedule.add(new PresentationSlot(current, current.plusMinutes(durationPerStudent)));
            current = current.plusMinutes(durationPerStudent);
        }
    }

    public String getSessionId() { return sessionId; }
    public String getSessionName() { return sessionName; }
    public String getSessionType() { return sessionType; }
    public String getSessionTrack() { return sessionTrack; }
    public LocalDate getDate() { return date; }
    public String getVenue() { return venue; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public int getDurationPerStudent() { return durationPerStudent; }
    public List<PresentationSlot> getSchedule() { return schedule; }

    public void setSessionName(String n) { this.sessionName = n; }
    public void setSessionType(String t) { this.sessionType = t; }
    public void setSessionTrack(String t) { this.sessionTrack = t; }
    public void setVenue(String v) { this.venue = v; }
    public void setDate(LocalDate d) { this.date = d; }

    @Override
    public String toString() {
        return sessionId + " - " + sessionName + " (" + date.format(DATE_FMT) + ")";
    }

    public static class PresentationSlot {
        private LocalTime start;
        private LocalTime end;
        private Student assignedStudent;     
        
        private List<Evaluator> assignedEvaluators; 

        public PresentationSlot(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
            this.assignedEvaluators = new ArrayList<>();
        }

        public String getTimeRange() {
            return start.format(TIME_FMT) + " - " + end.format(TIME_FMT);
        }

        public Student getStudent() { return assignedStudent; }
        public void setStudent(Student student) { this.assignedStudent = student; }

        public List<Evaluator> getEvaluators() { return assignedEvaluators; }
        
        public void setEvaluators(List<Evaluator> evaluators) {
            this.assignedEvaluators = evaluators;
        }

        public void addEvaluator(Evaluator e) {
            if (!assignedEvaluators.contains(e)) {
                assignedEvaluators.add(e);
            }
        }

        public String getEvaluatorNames() {
            if (assignedEvaluators.isEmpty()) return "None";
            return assignedEvaluators.stream()
                    .map(Evaluator::getName)
                    .collect(Collectors.joining(", "));
        }
    }
}