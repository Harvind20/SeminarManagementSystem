package misc;
public class Presentation{
    private String researchTitle;
    private String abstractPara;
    private String presentationType;
    private int[] rubrics;
    private int marks;
    private String comments;

    public String getResearchTitle(){return researchTitle;}
    public void setResearchTitle(String rTitle){researchTitle = rTitle;}
    public String getAbstract(){return abstractPara;}
    public void setAbstract(String a){abstractPara = a;}
    public String getPresentationType(){return presentationType;}
    public void setPresentationType(String pType){presentationType = pType;}
    public void setRubrics(int pClarity, int method, int results, int presentation){
        rubrics[1] = pClarity; rubrics[2] = method; 
        rubrics[3] = results; rubrics[4] = presentation;
    }
    public int[] getRubrics(){return rubrics;}
    public void setMarks(int m){marks = m;}
    public int getMarks(){return marks;}
    public void setComments(String c){comments = c;}
    public String getComments(){return comments;}
}
