import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Evaluation {
    private String evaluatorId;
    private String studentId;
    private int[] rubrics; 
    private int totalMarks;
    private String comments;
    private String posterBoardId; 
    private String presentationType; 
    
    public Evaluation(String evaluatorId, String studentId, int problemClarity, int methodology, 
                     int results, int presentation, String comments, String presentationType) {
        this.evaluatorId = evaluatorId;
        this.studentId = studentId;
        this.rubrics = new int[4];
        this.rubrics[0] = problemClarity;
        this.rubrics[1] = methodology;
        this.rubrics[2] = results;
        this.rubrics[3] = presentation;
        this.totalMarks = calculateTotalMarks();
        this.comments = comments;
        this.presentationType = presentationType;
    }
    
    public Evaluation(String evaluatorId, String studentId, String posterBoardId,
                     int problemClarity, int methodology, int results, int presentation, 
                     String comments) {
        this.evaluatorId = evaluatorId;
        this.studentId = studentId;
        this.posterBoardId = posterBoardId;
        this.rubrics = new int[4];
        this.rubrics[0] = problemClarity;
        this.rubrics[1] = methodology;
        this.rubrics[2] = results;
        this.rubrics[3] = presentation;
        this.totalMarks = calculateTotalMarks();
        this.comments = comments;
        this.presentationType = "Poster";
    }
    
    private int calculateTotalMarks() {
        int sum = 0;
        for (int score : rubrics) {
            sum += score;
        }
        return sum;
    }
    
    public void saveToFile() {
        String fileName = "evaluations.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            String record = evaluatorId + "|" + 
                           studentId + "|" + 
                           rubrics[0] + "|" + 
                           rubrics[1] + "|" + 
                           rubrics[2] + "|" + 
                           rubrics[3] + "|" + 
                           totalMarks + "|" + 
                           comments + "|" + 
                           presentationType + "|" + 
                           (posterBoardId != null ? posterBoardId : "N/A");
            
            writer.write(record);
            writer.newLine();
            System.out.println("Evaluation saved successfully.");
            
        } catch (IOException e) {
            System.out.println("Error saving evaluation to file.");
        }
    }
    
    public String getEvaluatorId() { return evaluatorId; }
    public String getStudentId() { return studentId; }
    public int[] getRubrics() { return rubrics; }
    public int getTotalMarks() { return totalMarks; }
    public String getComments() { return comments; }
    public String getPosterBoardId() { return posterBoardId; }
    public String getPresentationType() { return presentationType; }
    
    public void setRubrics(int problemClarity, int methodology, int results, int presentation) {
        this.rubrics[0] = problemClarity;
        this.rubrics[1] = methodology;
        this.rubrics[2] = results;
        this.rubrics[3] = presentation;
        this.totalMarks = calculateTotalMarks();
    }
    
    public void setComments(String comments) { this.comments = comments; }
    public void setPosterBoardId(String posterBoardId) { this.posterBoardId = posterBoardId; }
}