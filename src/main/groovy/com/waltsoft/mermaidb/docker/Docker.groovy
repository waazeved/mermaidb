package com.waltsoft.mermaidb.docker

import com.waltsoft.mermaidb.extension.Extension
import org.gradle.api.Project

class Docker {

    Integer getDynamicPort(String containerName, String internalPort) {
        def process = new ProcessBuilder('docker', 'port', containerName, internalPort).start()
        process.waitFor()

        def output = process.inputStream.text.trim()

        if (output.isEmpty()) {
            throw new RuntimeException("Mermaidb: Failed to retrieve dynamic port for container ${containerName}")
        }

        return output.split(':').last().trim().toInteger()
    }
}
