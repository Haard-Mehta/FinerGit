package finergit.jdt;

import java.io.IOException;
import java.nio.file.*;

public class App {
    public static void main(String[] args) {

        // cheacking if correct number of arguments are provided
        if (args.length != 2) {
            System.out.println("Usage: java jar <jar-file> <source-repo> <destination-repo>");
            return;
        }

        // defining paths for source and destination repositories
        Path srcRepo = Paths.get(args[0]);  
        Path destRepo = Paths.get(args[1]);

        try {
            // walking through the source repository
            Files.walk(srcRepo)
                .filter(Files::isRegularFile) // filtering regular files
                .forEach(path -> processFile(path, srcRepo, destRepo));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // processing a single file
    private static void processFile(Path filePath, Path srcRepo, Path destRepo) {

        // Compute the relative path and create necessary directories in the destination
        String relativePath = srcRepo.relativize(filePath).toString();
        Path destDir = destRepo.resolve(relativePath).getParent();
        try {
            Files.createDirectories(destDir);

            // processing only the .java files
            if (filePath.toString().endsWith(".java")) {
                RepositoryParser.parseJavaFile(filePath, destDir);
            } else {
                // Copy non-java files as they are
                Path destFile = destRepo.resolve(relativePath);
                Files.copy(filePath, destFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
