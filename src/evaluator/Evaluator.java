package evaluator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Evaluator {
    private String evaluatorId;
    private String name;
    private String expertise;
    private List<String> assignedSessions;

    public Evaluator(String evaluatorId, String name, String expertise, String sessionString) {
        this.evaluatorId = evaluatorId;
        this.name = name;
        this.expertise = expertise;
        this.assignedSessions = new ArrayList<>();
        
        if (sessionString != null && !sessionString.equals("None") && !sessionString.isEmpty()) {
            String[] s = sessionString.split(",");
            this.assignedSessions.addAll(Arrays.asList(s));
        }
    }

    public String getEvaluatorId() { return evaluatorId; }
    public String getName() { return name; }
    public String getExpertise() { return expertise; }
    public List<String> getAssignedSessions() { return assignedSessions; }

    @Override
    public String toString() {
        return name + " (" + evaluatorId + ")";
    }
}