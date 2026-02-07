package misc;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FileHandler {

    public static boolean validateFile(File file) {
        return file != null && (file.getName().endsWith(".pdf") || file.getName().endsWith(".pptx"));
    }

    public static String uploadFile(File file) throws IOException {
        File dest = new File("uploads/" + file.getName());
        dest.getParentFile().mkdirs();
        Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return dest.getAbsolutePath();
    }
}
