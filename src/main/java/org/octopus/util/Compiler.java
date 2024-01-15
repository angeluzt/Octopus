package org.octopus.util;

import org.octopus.analysers.Analyser;
import org.octopus.analysers.LexicalAnalyser;
import org.octopus.analysers.SemanticAnalyser;
import org.octopus.analysers.SyntacticAnalyser;
import org.octopus.structure.Node;
import org.octopus.structure.Token;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.LinkedList;

public class Compiler {

    private static Analyser lexicalAnalyser;
    private static Analyser syntacticAnalyser;

    public void compile() throws Exception {
        String errorPath = System.getProperty("user.dir") + Constants.ERROR_PATH;
        String sourcePath = System.getProperty("user.dir");
        String codePath =  System.getProperty("user.dir") + Constants.CODE_LOCATION_PATH;

        String pathForCompiledCode = System.getProperty("user.dir") + Constants.BINARY_PATH;

        // delete existing oct build path
        deleteCompiledFolderIfExists(pathForCompiledCode);

        // iterate all folders from codePath (code location)
        Files.find(Paths.get(codePath), Integer.MAX_VALUE, (filePath, fileAttr) -> fileAttr.isRegularFile())
                .forEach(filePath -> {
                    String fileContent = null;
                    try {
                        try {

                            if(isSameExtension(filePath.toString(), Constants.OCT_EXTENSION)) {
                                fileContent = readFileAsString(filePath);
                            } else {
                                // Ignore file that do not end with OCT_EXTENSION
                                return;
                            }
                        } catch(IOException ex) {
                            System.out.println("Error while reading file: " + filePath + ", " + ex);
                        }

                        // One instance per file
                        lexicalAnalyser = new LexicalAnalyser();
                        syntacticAnalyser = new SyntacticAnalyser();

                        // TOKENS: generate tokens for current file
                        LinkedList<Token> tokens = (LinkedList<Token>)lexicalAnalyser.analyze(fileContent);

                        // THREE: process tokens and generate syntactic three
                        Node syntacticTree = (Node)syntacticAnalyser.analyze(tokens);

                        // ERRORS
                        boolean isErrorFree = ((SyntacticAnalyser) syntacticAnalyser).getErrorTokens().isEmpty();

                        /* use current file path and sourcePath to extract the relative file path
                            in this way we can use pathForCompiledCode + compiledFileRelativePath
                            and then save the file in build/binary/oct/filePath
                            Example:
                            variables:
                                - pathForCompiledCode = a/b/c/d/build/binary
                                - sourcePath = a/b/c/d
                                - filePath = a/b/c/d/oct/fileName.oct

                            final result to variable compiledFileRelativePath:
                                - /oct/fileName.oct
                            Then we save in finalCompiledFolderName: pathForCompiledCode + compiledFileRelativePath
                                - Result: a/b/c/d/build/binary/oct/fileName.oct

                            Basically, we are replication the same folder structure inside this new build folder
                         */
                        String compiledFileRelativePath = getFinalFileLocationForBuildFile(sourcePath, filePath.toString());
                        String finalCompiledFolderName = pathForCompiledCode + compiledFileRelativePath;
                        if (isErrorFree) {
                            writeThreeInBuildFile(Path.of(finalCompiledFolderName), syntacticTree);
                            new SemanticAnalyser().analyze(syntacticTree);
                        } else {
                            writeErrorsInFile(Path.of(errorPath), filePath, Path.of(compiledFileRelativePath));
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                });
    }

    private boolean isSameExtension(String path, String extension) {

        int indexOfDot = path.toString().lastIndexOf(".");
        String pathExtension = path.toString().substring(indexOfDot + 1);

        if (pathExtension.equals(extension)) {
            return true;
        }

        return false;
    }

    private String getFinalFileLocationForBuildFile(String mainFolderPath, String buildFilePath) {
        String main = mainFolderPath.replace("\\", "/");
        String buildPth = buildFilePath.replace("\\", "/");
        String [] response = buildPth.split(main);
        // Use Paths to create a Path object from the file path

        //System.out.println("File internal path: " + response[1]);

        return response[1].replace("/", "\\");
    }

    private String readFileAsString(Path filePath) throws IOException {
        // Use Paths and Files to read the entire file content as a string
        //Path path = Paths.get(filePath);
        byte[] bytes = Files.readAllBytes(filePath);
        return new String(bytes);
    }

    private void writeErrorsInFile(Path errorPath, Path filePath, Path compiledFileRelativePath) throws IOException {
        StringBuilder code = new StringBuilder();
        createAndGetPath(removeFileNameFromPath(errorPath));

        LinkedList<Token> errors = ((SyntacticAnalyser) syntacticAnalyser).getErrorTokens();
        code.append("->").append("\n");
        code.append("ClassName: " + filePath.getFileName()).append("\n");
        code.append("PathClassName: " + compiledFileRelativePath.getParent()).append("\n");

        errors.stream().forEach(token -> {
            code.append(token.getTokenType().getTokenName()).append(" : ")
                    .append(token.getLexeme()).append(" on line: ").append(token.getLine())
                    .append(", column: " ).append(token.getColumn())
                    .append("\n");
        });

        code.append("<-\n");
        Files.write(errorPath, code.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private void writeThreeInBuildFile(Path filepath, Node syntacticTree) throws IOException {
        StringBuilder stringCollector = new StringBuilder();

        Path finalPath = removeFileNameFromPath(filepath);
        createAndGetPath(finalPath);

        new Three().convertNodeToString(stringCollector, syntacticTree);

        Path filePath = Paths.get(filepath + Constants.THREE_FILE_CODE);

        // Use Paths and Files to create the file and write content
        Files.write(filePath, stringCollector.toString().getBytes());
    }

    private Path removeFileNameFromPath(Path filePath) {
        // extract path
        return filePath.getParent();
    }

    private Path createAndGetPath(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        return path;
    }

    private void deleteCompiledFolderIfExists(String sourcePath) throws IOException {
        Path uri = Path.of(sourcePath).getParent();
        if (Files.exists(uri)) {
            Files.walk(uri)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }
}
