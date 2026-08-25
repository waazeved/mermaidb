# Mermaidb Gradle Plugin 🧜‍♀️

[![Gradle Plugin Portal](https://img.shields.io/badge/Gradle%20Plugin%20Portal-v1.0.0-blue.svg)](https://plugins.gradle.org/plugin/com.waltsoft.mermaidb)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/waazeved/mermaidb)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

**Mermaidb** is a powerful Gradle plugin that automates the generation of Entity-Relationship (ER) diagrams in Mermaid format directly from your database schemas. It manages the entire lifecycle, from setting up an ephemeral database to generating and organizing your diagrams.

---

## 📜 Overview

The plugin streamlines the process of database visualization by:

1.  🚀 **Spinning up an ephemeral database** using a Docker container (e.g., PostgreSQL, MySQL).
2.  🔄 **Applying database migrations** via Liquibase to build the schema.
3.  🔍 **Extracting the schema** into a `.mmd` file using the powerful [Mermerd CLI](https://github.com/mermerd/mermerd).
4.  ✨ **Applying visual and structural enhancements** to the generated diagram.
5.  🗑️ **Tearing down the Docker container** automatically, leaving your environment clean.

This allows you to keep your ER diagrams consistently updated with every schema change, effortlessly.

---

## 📋 Prerequisites

To use the Mermaidb plugin, your development environment must have the following tools installed:

*   **Java & Gradle**: Essential for running the plugin.
*   **Docker**: Required for creating ephemeral database containers and running the Mermerd CLI image.
*   **Git**: Used for the smart pipeline optimization feature.

---

## 🛠️ For Developers: How to Test Locally

If you want to contribute to Mermaidb, you can easily test your changes locally.

1.  **Publish to Maven Local**:
    Run the following command to publish the plugin to your local Maven repository:
    ```bash
    ./gradlew publishToMavenLocal
    ```

2.  **Configure the Consumer Project**:
    In the project where you want to test the plugin, add `mavenLocal()` to the `pluginManagement` block in your `settings.gradle` file. This tells Gradle to look for the plugin in your local repository.

    ```groovy
    // settings.gradle
    pluginManagement {
        repositories {
            mavenLocal() // Add this line
            gradlePluginPortal()
            // other repositories...
        }
    }
    ```

Now you can apply and configure the plugin in your `build.gradle` as usual.

---

## ⚙️ Configuration & Usage

Getting started with Mermaidb is simple.

1.  **Apply and Configure the Plugin**:
    In your `build.gradle` file, apply the plugin and configure the `mermaidb` extension with your project's specific settings.

    ```groovy
    // build.gradle
    plugins {
        id 'com.waltsoft.mermaidb' version '1.0.0' // Use the desired version
    }

    mermaidb {
        // The type of database to use (e.g., 'postgresql', 'mysql')
        dbType = 'postgresql'

        // Path to your Liquibase changelog file
        changeLogFilePath = 'src/main/resources/db/changelog/db.changelog-master.xml'

        // Directory where the generated diagrams will be saved
        outputDirPath = 'docs/diagrams/db'

        // Enable/disable automatic 'git add' for generated diagrams
        autoGitAdd = true
    }
    ```

2.  **Run the Generation Task**:
    Execute the following command in your terminal to generate the diagrams:
    ```bash
    ./gradlew generateDatabaseDiagram
    ```

---

## ⚡️ Smart Git Integration

Mermaidb includes a smart integration with Git to optimize your build pipeline and save valuable time.

### Conditional Execution

To avoid unnecessary work, the plugin performs a `git diff --name-only --cached` check before running. It will only proceed with the diagram generation if it detects that database migration files have been staged (via `git add`).

This means your diagrams are only regenerated when the schema has actually changed.

### Forcing Generation

If you need to regenerate the diagrams regardless of the Git state, you can use the `-PforceGenerate=true` flag:

```bash
./gradlew generateDatabaseDiagram -PforceGenerate=true
```

### Automatic Git Add

When the `autoGitAdd` property is set to `true`, the plugin will automatically stage the generated `.mmd` files for you by running `git add` on the output directory. This helps ensure your diagrams are always included in your next commit.

---

## 🧩 Modular Diagram Generation

Beyond creating a single, monolithic ER diagram, Mermaidb intelligently organizes your schema into smaller, more manageable **Module Diagrams**.

*   **Automatic Grouping**: Using an internal `DiagramModuleGrouper`, the plugin analyzes table names and relationships to group them into logical modules or domains.
*   **Visual Ordering**: The `DiagramTableOrderer` then arranges the tables within each module diagram using a "visual gravity" algorithm, placing central tables in the middle and related ones around them for maximum clarity.
*   **Organized Output**: The final diagrams are saved neatly into subfolders within your specified output directory, making it easy to navigate and find the specific domain you're interested in.

This feature is perfect for large, complex schemas, as it provides both a high-level overview and detailed, domain-specific views of your database architecture.
