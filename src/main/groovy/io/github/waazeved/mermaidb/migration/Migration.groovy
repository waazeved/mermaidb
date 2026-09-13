package io.github.waazeved.mermaidb.migration

import io.github.waazeved.mermaidb.extension.Extension
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.JavaExec

interface Migration {

    void applyDependencies(Project project, Configuration configuration, Extension extension)

    void configure(JavaExec task, Project project, Extension extension, Configuration migrationRuntime)

}
