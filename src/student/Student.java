package student;
import misc.User;

public class Student extends User {
    private String researchTitle;
    private String abstractText;
    private String supervisor;
    private String presentationType; 
    private String submissionPath;
    private String sessionId;  
    private String boardId;

    public Student(String id, String pwd, String name, String title, String abs, String sv, String type, String path) {
        super(id, pwd, name);
        this.researchTitle = title;
        this.abstractText = abs;
        this.supervisor = sv;
        this.presentationType = type;
        this.submissionPath = path;
        this.sessionId = "None";
        this.boardId = "None";
    }

    public Student(String id, String name, String title, String abs, String sv, String type) {
        super(id, "123", name); 
        this.researchTitle = title;
        this.abstractText = abs;
        this.supervisor = sv;
        this.presentationType = type;
        this.submissionPath = "None";
        this.sessionId = "None";
        this.boardId = "None";
    }

    public void setSubmissionPath(String submissionPath) {
        this.submissionPath = submissionPath;
    }

    public String getSubmissionPath() {
        return submissionPath;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public String getBoardId() {
        return boardId;
    }

    public String toFileString() {
        return id + "|" + password + "|" + name + "|" + researchTitle + "|" + 
               abstractText + "|" + supervisor + "|" + presentationType + "|" + 
               submissionPath + "|" + sessionId + "|" + boardId; 
    }

    public String getStudentId() { return id; }
    public String getResearchTitle() { return researchTitle; }
    public String getAbstractText() { return abstractText; }    
    public String getSupervisor() { return supervisor; }
    public String getPresentationType() { return presentationType; }

    @Override
    public String toString() {
        String sessionInfo = sessionId.equals("None") ? "" : " [Session: " + sessionId + "]";
        String boardInfo = boardId.equals("None") ? "" : " [Board: " + boardId + "]";
        return name + " (" + id + ")" + sessionInfo + boardInfo;
    }
}