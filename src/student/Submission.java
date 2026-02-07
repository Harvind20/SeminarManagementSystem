package student;
public class Submission {
    private String studentId;
    private String filePath;

    public Submission(String studentId, String filePath) {
        this.studentId = studentId;
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getStudentId() {
        return studentId;
    }
}

