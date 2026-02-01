public class RegistrationController {

    public static boolean validateInput(Student student) {
        return student.getStudentId() != null && !student.getStudentId().isEmpty();
    }

    public static boolean registerStudent(Student student) {
        if (!validateInput(student)) {
            return false;
        }
        Database.saveStudent(student);
        return true;
    }
}
