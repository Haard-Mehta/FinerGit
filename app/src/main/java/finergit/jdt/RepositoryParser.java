package finergit.jdt;

import org.eclipse.jdt.core.dom.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class RepositoryParser {

    // Method to parse the java files
    public static void parseJavaFile(Path filePath, Path destDir) {
        try {
            // Reading the file content into string
            String content = new String(Files.readAllBytes(filePath));

            // Setting up the AST parser
            ASTParser parser = ASTParser.newParser(AST.JLS8);
            parser.setSource(content.toCharArray());

            // Parsing file contents into AST
            CompilationUnit cu = (CompilationUnit) parser.createAST(null);

            // Extracting the base file name without .java extension
            String fileName = filePath.getFileName().toString();
            String baseName = fileName.substring(0, fileName.length() - 5); // Remove .java extension

            // process and tokenize the class, fields and the methods
            processClass(cu, destDir, baseName, content);
            processFields(cu, destDir, baseName, content);
            processMethods(cu, destDir, baseName, content);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // method to process class declarations and save them in .cjava files
    private static void processClass(CompilationUnit cu, Path destDir, String baseName, String content) {
        try {
            Path classFile = destDir.resolve(baseName + ".cjava");

            // writing the tokenized content into the files
            Files.write(classFile, tokenize(content).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // method to process the field declarations and save them in .fjava files
    private static void processFields(CompilationUnit cu, Path destDir, String baseName, String content) {

        // visit each field declaration in AST
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(FieldDeclaration node) {

                // getting all the variable declarations fragment in the node
                List<VariableDeclarationFragment> fragments = node.fragments();
                for (VariableDeclarationFragment fragment : fragments) {
                    String fieldName = fragment.getName().getIdentifier();
                    String fieldContent = node.toString();
                    try {
                        
                        // creating .fjava file for each field
                        Path fieldFile = destDir.resolve(baseName + "#" + fieldName + ".fjava");

                        // writing the tokenized content to the file
                        Files.write(fieldFile, tokenize(fieldContent).getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return super.visit(node);
            }
        });
    }

    // Method to process method declarations and save them to .mjava files
    private static void processMethods(CompilationUnit cu, Path destDir, String baseName, String content) {

        // visit each method declaration node in AST
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration node) {
                String methodName = node.getName().getIdentifier();
                String methodContent = node.toString();
                try {

                    // creating the .mjava file for each method
                    Path methodFile = destDir.resolve(baseName + "#" + methodName + ".mjava");
                    Files.write(methodFile, tokenize(methodContent).getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return super.visit(node);
            }
        });
    }

    // method to tokenize the source code into individual token in separate lines
    private static String tokenize(String content) {
        StringBuilder tokenizedContent = new StringBuilder();
        char[] characters = content.toCharArray();
        StringBuilder token = new StringBuilder();

        for (char c : characters) {
            if (Character.isWhitespace(c)) {
                if (token.length() > 0) {
                    tokenizedContent.append(token).append("\n");
                    token.setLength(0);
                }
            } else if (isDelimiter(c)) {
                if (token.length() > 0) {
                    tokenizedContent.append(token).append("\n");
                    token.setLength(0);
                }
                tokenizedContent.append(c).append("\n");
            } else {
                token.append(c);
            }
        }
        if (token.length() > 0) {
            tokenizedContent.append(token).append("\n");
        }

        return tokenizedContent.toString();
    }

    // helper method to check if a character is a delimiter
    private static boolean isDelimiter(char c) {
        return c == '(' || c == ')' || c == '{' || c == '}' || c == ';' || c == ',' || c == '.';
    }
}
