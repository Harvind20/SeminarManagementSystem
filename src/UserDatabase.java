import java.io.*;

public class UserDatabase {

    private static String getFileByRole(String role) {
        switch (role) {
            case "Student":
                return "registeredStudents.txt";
            case "Evaluator":
                return "registeredEvaluators.txt";
            case "Coordinator":
                return "registeredCoordinators.txt";
            default:
                return null;
        }
    }

    public static boolean userExists(String id, String role) {
        String file = getFileByRole(role);
        if (file == null) return false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(id + "|")) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    public static boolean verifyLogin(String id, String password, String role) {
        String file = getFileByRole(role);
        if (file == null) return false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data[0].equals(id) && data[1].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    public static void registerUser(String id, String password, String role) {
        String file = getFileByRole(role);
        if (file == null) return;

        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(file, true))) {

            bw.write(id + "|" + password);
            bw.newLine();

        } catch (IOException e) {
            System.out.println("Error saving user");
        }
    }
}
