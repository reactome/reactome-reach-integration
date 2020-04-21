package org.reactome.reach.covid19;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReachRunner {
	private static final Logger logger = LogManager.getLogger("mainLog");

    public ReachRunner() {
    }

    /**
     * Run the REACH CLI Process.
     *
     * @param reachCodeDir
     * @throws IOException
     * @throws InterruptedException
     */
    public void runReach(Path reachCodeDir) throws IOException, InterruptedException {
        final String sbt = "/usr/local/bin/sbt";
        final String cli = "run-main org.clulab.reach.RunReachCLI";

        Process cliProcess = runCommand(reachCodeDir, sbt, cli);
        logProcess(cliProcess);
        cliProcess.destroy();
    }

    /**
     * Run a command in a given directory.
     *
     * @param dir
     * @param parts
     * @return Process
     * @throws IOException
     * @throws InterruptedException
     */
    private Process runCommand(Path dir, String... parts) throws IOException, InterruptedException {
        List<String> command = new ArrayList<String>();
        for (String part : parts)
            command.add(part);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(dir.toFile());
        Process process = processBuilder.start();
        return process;
    }

    /**
     * Log the output of a running process.
     *
     * @param process
     * @throws IOException
     * @throws InterruptedException
     */
    private void logProcess(Process process) throws IOException, InterruptedException {
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                logger.info(line);
            }

            int exitCode = process.waitFor();
            logger.info("REACH exited with error code: " + exitCode);
        }
    }

}
