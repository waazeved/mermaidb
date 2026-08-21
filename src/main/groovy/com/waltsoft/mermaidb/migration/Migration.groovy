package com.waltsoft.mermaidb.migration

import com.waltsoft.mermaidb.extension.Extension
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec

import java.lang.module.Configuration

interface Migration {


    static final int DATABASE_PORT = 54332

    void applyDependencies(Project project, Configuration configuration, Extension extension)

    void configure(JavaExec task, Project project, Extension extension, Configuration migrationRuntime)

    default getDatabasePort(){
        return DATABASE_PORT
    }
}
