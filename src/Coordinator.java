public class Coordinator extends User{
    //private Session[] sessions; await class implementation
    private Evaluator[] evaluators;
    private Student[] students;

    //public Session[] getSessions(){return sessions;} await implementation
    public void createSession(){}
    public Evaluator[] getEvaluators(){return evaluators;}
    public void setEvaluators(Evaluator[] e){evaluators = e;}
    public Student[] getStudents(){return students;}
    public void setStudents(Student[] s){students = s;}
    public void assignEvaluators(){}
    public void deleteSessions(){}
    public void modifySession(){}
}
