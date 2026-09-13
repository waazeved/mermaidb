package io.github.waazeved.mermaidb.docker

import org.gradle.internal.impldep.com.google.common.annotations.VisibleForTesting

class Docker {

    DockerResult run(List<String> command) {
        String[] cmdArray = command.toArray(new String[0])
        def process = new ProcessBuilder(cmdArray).start()
        def result = new DockerResult()
        def stdout = new StringWriter()
        def stderr = new StringWriter()
        process.waitForProcessOutput(stdout, stderr)
        result.stdout = stdout.toString()
        result.stderr = stderr.toString()
        result.exitCode = process.exitValue()
        return result
    }

    Integer getDynamicPort(String containerName, String internalPort) {
        def processBuilder = new ProcessBuilder('docker', 'port', containerName, internalPort)
        return getDynamicPort(processBuilder)
    }

    @VisibleForTesting
    Integer getDynamicPort(ProcessBuilder processBuilder) {
        def process = processBuilder.start()
        process.waitFor()

        def output = process.inputStream.text.trim()

        if (output.isEmpty()) {
            throw new RuntimeException("Mermaidb: Failed to retrieve dynamic port.")
        }

        return output.split(':').last().trim().toInteger()
    }
}
