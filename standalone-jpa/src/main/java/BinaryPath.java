import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Paths;

public class BinaryPath {
    public static void main(String[] args) {
        String location = getLocation(BinaryPath.class);
        System.out.println("Location: " + location);

        // Set the variable with the absolute path
        String absolutePath = new File(location).getAbsolutePath();
        System.out.println("Absolute Path: " + absolutePath);
    }
    
    public static String getLocation(Class<?> clazz) {
        try {
            // Get the resource URL of the class file
            String resourcePath = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();

            // Decode URL to handle spaces and special characters
            String decodedPath = URLDecoder.decode(resourcePath, "UTF-8");

            // Create a File object to represent the path
            File file = new File(decodedPath);

            // If it's a directory, return the directory path
            if (file.isDirectory()) {
                return file.getAbsolutePath();
            }

            // If it's a JAR file, return the parent directory path
            if (decodedPath.endsWith(".jar")) {
                return file.getParent();
            }

            // Otherwise, return the path as it is
            return decodedPath;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
