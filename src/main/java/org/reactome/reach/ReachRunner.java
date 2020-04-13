package org.reactome.reach;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ReachRunner {
    public ReachRunner() {
    }
    
    public void runReach(Path reachCodeDir) throws IOException, InterruptedException {
        final String sbt = "/usr/local/bin/sbt";
        final String server = "run-main org.clulab.processors.server.ProcessorServer";
        final String cli = "run-main org.clulab.reach.RunReachCLI";

		// sbt 'run-main org.clulab.processors.server.ProcessorServer'
        Process serverProcess = runCommand(reachCodeDir, sbt, server);

		// sbt 'run-main org.clulab.reach.RunReachCLI' 
        Runnable runabble = () -> {
            try {
                Process cliProcess = runCommand(reachCodeDir, sbt, cli);
                logProcess(cliProcess);
                cliProcess.destroy();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        };
        
        Thread thread = new Thread(runabble);
        thread.start();

        serverProcess.waitFor();
        serverProcess.destroy();
    }

    private Process runCommand(Path dir, String... parts) throws IOException, InterruptedException {
        List<String> command = new ArrayList<String>();
        for (String part : parts)
            command.add(part);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(dir.toFile());
        Process process = processBuilder.start(); 
        return process;
    }

    private void logProcess(Process process) throws IOException, InterruptedException {
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("\nExited with error code : " + exitCode);
        }
    }

}
