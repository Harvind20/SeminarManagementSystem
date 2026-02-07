package evaluator;
public class Evaluator { // Assuming extends User in your full project
    private String evaluatorId;
    private String name;
    private String expertise; // e.g., "AI", "Cybersecurity"

    public Evaluator(String evaluatorId, String name, String expertise) {
        this.evaluatorId = evaluatorId;
        this.name = name;
        this.expertise = expertise;
    }

    public String getEvaluatorId() { return evaluatorId; }
    public String getName() { return name; }
    public String getExpertise() { return expertise; }

    @Override
    public String toString() {
        return name + " (" + expertise + ")";
    }
}