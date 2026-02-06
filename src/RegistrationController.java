public class RegistrationController {

    public static boolean validateInput(Student student) {
        if (student.getResearchTitle().isEmpty()) return false;
        if (student.getSupervisor().isEmpty()) return false;
        if (student.getAbstractText().isEmpty()) return false;
        return true;
    }

    public static void registerStudent(Student student) {
        // Calls the database to update the text file
        UserDatabase.saveStudentRegistration(student);
    }
}