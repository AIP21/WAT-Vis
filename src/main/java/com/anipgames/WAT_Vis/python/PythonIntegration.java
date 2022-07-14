package com.anipgames.WAT_Vis.python;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class PythonIntegration {
    private static final String DIR_PY = new File("src/main/resources/python/").getAbsolutePath();

    /**
     * Run a python file. This is static so to call this function, do PythonIntegration.executePython(NAME OF PYTHON FILE, ARGUMENTS, FILE TO WRITE TO);
     *
     * @param pythonFile  The file name of the python script to execute. These files are contained in the directory "resources/python".
     * @param arguments   The arguments to pass to the python script (in this case, file name).
     * @param writeToFile The extra information to write to a file. This should be used when the information is too large to pass as a single string.
     * @return An array of strings containing the results of the executed python script.
     * @throws Exception If the given python script fails or doesn't exist.
     */
    public static ArrayList<String> executePython(String pythonFile, String arguments, String writeToFile) throws Exception {
        long start = System.currentTimeMillis();

        ProcessBuilder processBuilder = new ProcessBuilder("python", DIR_PY + File.separatorChar + pythonFile, arguments);
        try (PrintWriter pw = new PrintWriter(DIR_PY + File.separatorChar + "data.txt", StandardCharsets.UTF_8)) {
            pw.println(writeToFile);
        } catch (Exception e) {
            System.err.println("Error writing to file for python script");
            e.printStackTrace();
        }

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        ArrayList<String> results = readProcessOutput(process.getInputStream());

        int exitCode = process.waitFor();

        System.out.println("Python code finished in " + (System.currentTimeMillis() - start) + "ms");

        return results;
    }

    /**
     * Run a python file. This is static so to call this function, do PythonIntegration.executePython(NAME OF PYTHON FILE, ARGUMENTS);
     *
     * @param pythonFile The file name of the python script to execute. These files are contained in the directory "resources/python".
     * @param arguments  The arguments to pass to the python script (in this case, file name).
     * @return An array of strings containing the results of the executed python script.
     * @throws Exception If the given python script fails or doesn't exist.
     */
    public static ArrayList<String> executePython(String pythonFile, String arguments) throws Exception {
        long start = System.currentTimeMillis();

        ProcessBuilder processBuilder = new ProcessBuilder("python", DIR_PY + "/" + pythonFile, arguments);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        ArrayList<String> results = readProcessOutput(process.getInputStream());

        int exitCode = process.waitFor();

        System.out.println("Python code finished in " + (System.currentTimeMillis() - start) + "ms");

        return results;
    }

    /**
     * Run a python file. This is static so to call this function, do PythonIntegration.executePython(NAME OF PYTHON FILE);
     *
     * @param pythonFile The file name of the python script to execute. These files are contained in the directory "resources/python".
     * @return An array of strings containing the results of the executed python script.
     * @throws Exception If the given python script fails or doesn't exist.
     */
    public static ArrayList<String> executePython(String pythonFile) throws Exception {
        long start = System.currentTimeMillis();

        ProcessBuilder processBuilder = new ProcessBuilder("python", DIR_PY + "/" + pythonFile);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        ArrayList<String> results = readProcessOutput(process.getInputStream());

        int exitCode = process.waitFor();

        System.out.println("Python code finished in " + (System.currentTimeMillis() - start) + "ms");

        return results;
    }

    private static ArrayList<String> readProcessOutput(InputStream inputStream) throws IOException {
        try (BufferedReader output = new BufferedReader(new InputStreamReader(inputStream))) {
            return (ArrayList<String>) output.lines().collect(Collectors.toList());
        }
    }
}