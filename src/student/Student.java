package student;
import misc.User;

public class Student extends User {
    private String researchTitle;
    private String abstractText;
    private String supervisor;
    private String presentationType; 
    private String submissionPath;

    public Student(String id, String pwd, String name, String title, String abs, String sv, String type, String path) {
        super(id, pwd, name);
        this.researchTitle = title;
        this.abstractText = abs;
        this.supervisor = sv;
        this.presentationType = type;
        this.submissionPath = path;
    }

    public Student(String id, String name, String title, String abs, String sv, String type) {
        super(id, "123", name); 
        this.researchTitle = title;
        this.abstractText = abs;
        this.supervisor = sv;
        this.presentationType = type;
        this.submissionPath = "None"; 
    }

    public void setSubmissionPath(String submissionPath) {
        this.submissionPath = submissionPath;
    }

    public String getSubmissionPath() {
        return submissionPath;
    }

    public String toFileString() {
        return id + "|" + password + "|" + name + "|" + researchTitle + "|" + 
               abstractText + "|" + supervisor + "|" + presentationType + "|" + submissionPath;
    }

    public String getStudentId() { return id; }
    public String getResearchTitle() { return researchTitle; }
    public String getAbstractText() { return abstractText; }    
    public String getSupervisor() { return supervisor; }
    public String getPresentationType() { return presentationType; }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}