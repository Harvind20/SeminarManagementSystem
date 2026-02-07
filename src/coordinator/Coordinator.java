package coordinator;
import evaluator.Evaluator;
import misc.User;
import student.Student;

public class Coordinator extends User {

    public Coordinator(String id, String password, String name) {
        super(id, password, name);
    }

    private Evaluator[] evaluators;
    private Student[] students;

    public Evaluator[] getEvaluators() { return evaluators; }
    public void setEvaluators(Evaluator[] e) { evaluators = e; }
    
    public Student[] getStudents() { return students; }
    public void setStudents(Student[] s) { students = s; }

    public void createSession() {}
    public void assignEvaluators() {}
    public void deleteSessions() {}
    public void modifySession() {}
}