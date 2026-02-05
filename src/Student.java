public class Student {
    private String studentId;
    private String name;
    private String researchTitle;
    private String abstractText;
    private String supervisor;
    private String presentationType; 
    private String submissionPath;

    public Student(String studentId, String name, String researchTitle,
                   String abstractText, String supervisor, String presentationType) {
        this.studentId = studentId;
        this.name = name;
        this.researchTitle = researchTitle;
        this.abstractText = abstractText;
        this.supervisor = supervisor;
        this.presentationType = presentationType;
    }

    public void setSubmissionPath(String submissionPath) {
        this.submissionPath = submissionPath;
    }

    public String getSubmissionPath() {
        return submissionPath;
    }

    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getResearchTitle() { return researchTitle; }
    public String getAbstractText() { return abstractText; }    
    public String getSupervisor() { return supervisor; }
    public String getPresentationType() { return presentationType; }

    @Override
    public String toString() {
        return name + " (" + studentId + ")";
    }
}