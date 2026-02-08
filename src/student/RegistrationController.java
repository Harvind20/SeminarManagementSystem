package student;
import misc.UserDatabase;
import student.Student;

public class RegistrationController {

    public static boolean validateInput(Student student) {
        if (student.getResearchTitle().isEmpty()) return false;
        if (student.getSupervisor().isEmpty()) return false;
        if (student.getAbstractText().isEmpty()) return false;
        if (student.getSessionId() == null || student.getSessionId().equals("None") || 
            student.getSessionId().isEmpty()) return false;
        if (student.getBoardId() == null || student.getBoardId().isEmpty()) return false;
        return true;
    }

    public static void registerStudent(Student student) {
        if (!UserDatabase.sessionExists(student.getSessionId())) {
            System.out.println("Error: Session " + student.getSessionId() + " does not exist.");
            return;
        }
        UserDatabase.saveStudentRegistration(student);
    }
}