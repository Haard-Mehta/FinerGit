package finergit.jdt;

import org.junit.jupiter.api.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    private static final String SRC_REPO = "src/test/resources/sourceRepo";
    private static final String DEST_REPO = "src/test/resources/destRepo";

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(Paths.get(SRC_REPO));
        Files.createDirectories(Paths.get(DEST_REPO));

        String javaContent = "public class TestClass {\n" +
                             "    private int field;\n" +
                             "    public void method() {}\n" +
                             "}";
        Files.write(Paths.get(SRC_REPO, "TestClass.java"), javaContent.getBytes());
    }

    @AfterEach
    void tearDown() throws IOException {
        deleteDirectory(Paths.get(SRC_REPO).toFile());
        deleteDirectory(Paths.get(DEST_REPO).toFile());
    }

    private void deleteDirectory(File directory) {
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directory.delete();
    }

    @Test
    void testProcessFile() {
        App.main(new String[]{SRC_REPO, DEST_REPO});

        Path classFile = Paths.get(DEST_REPO, "TestClass.cjava");
        Path fieldFile = Paths.get(DEST_REPO, "TestClass#field.fjava");
        Path methodFile = Paths.get(DEST_REPO, "TestClass#method.mjava");

        assertTrue(Files.exists(classFile), "Class file should exist");
        assertTrue(Files.exists(fieldFile), "Field file should exist");
        assertTrue(Files.exists(methodFile), "Method file should exist");

        try {
            String classContent = new String(Files.readAllBytes(classFile));
            assertTrue(classContent.contains("public class TestClass"), "Class file content should match");

            String fieldContent = new String(Files.readAllBytes(fieldFile));
            assertTrue(fieldContent.contains("private int field"), "Field file content should match");

            String methodContent = new String(Files.readAllBytes(methodFile));
            assertTrue(methodContent.contains("public void method"), "Method file content should match");
        } catch (IOException e) {
            fail("Exception should not be thrown");
        }
    }
}
           
