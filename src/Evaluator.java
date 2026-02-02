public class Evaluator extends User{
    private int[] rubrics;
    private int marks;
    private String comments;

    public void setRubrics(int pClarity, int method, int results, int presentation){
        rubrics[1] = pClarity; rubrics[2] = method; 
        rubrics[3] = results; rubrics[4] = presentation;
    }
    public int[] getRubrics(){return rubrics;}
    public void setMarks(int m){marks = m;}
    public int getMarks(){return marks;}
    public void setComments(String c){comments = c;}
    public String getComments(){return comments;}
    //implementation unfinished
    public String verifyRubrics(){
        for (int marks : rubrics){
            if (marks >= 1 && marks <= 5){return "Rubrics valid";}
            else {return "Rubrics invalid";}
        }
        return "";
    }
}
