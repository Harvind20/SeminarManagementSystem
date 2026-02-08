package awards;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class AwardCalculator {

    private static final String FILE_PATH = "src/saved/evaluations.txt";

    public Award findBestPoster() {
        String bestStudent = null;
        int highestMarks = -1;

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");

                String studentId = parts[1];
                int totalMarks = Integer.parseInt(parts[6]);
                String type = parts[8];

                if (type.equalsIgnoreCase("Poster") && totalMarks > highestMarks) {
                    highestMarks = totalMarks;
                    bestStudent = studentId;
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading evaluations file.");
        }

        if (bestStudent != null) {
            return new Award(bestStudent, "Best Poster", highestMarks, "Poster");
        }

        return null;
    }

    public Award findBestPresenter() {
        String bestStudent = null;
        int highestMarks = -1;

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");

                String studentId = parts[1];
                int totalMarks = Integer.parseInt(parts[6]);
                String type = parts[8];

                if (type.equalsIgnoreCase("Presentation") && totalMarks > highestMarks) {
                    highestMarks = totalMarks;
                    bestStudent = studentId;
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading evaluations file.");
        }

        if (bestStudent != null) {
            return new Award(bestStudent, "Best Presenter", highestMarks, "Presentation");
        }

        return null;
    }
}