package awards;

public class Award {
    private String studentId;
    private String awardName;
    private int marks;
    private String presentationType;

    public Award(String studentId, String awardName, int marks, String presentationType) {
        this.studentId = studentId;
        this.awardName = awardName;
        this.marks = marks;
        this.presentationType = presentationType;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getAwardName() {
        return awardName;
    }

    public int getMarks() {
        return marks;
    }

    public String getPresentationType() {
        return presentationType;
    }

    @Override
    public String toString() {
        return studentId + " - " + awardName + " (" + marks + ")";
    }
}