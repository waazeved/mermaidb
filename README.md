# Mermaidb Gradle Plugin 🧜‍♀️🛢️

[![Gradle Plugin Portal](https://img.shields.io/badge/Gradle%20Plugin%20Portal-v1.0.1-blue.svg)](https://plugins.gradle.org/plugin/io.github.waazeved.mermaidb)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/waazeved/mermaidb)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

**Mermaidb** is a Gradle plugin that automates the generation of Entity-Relationship (ER) diagrams in [Mermaid](https://mermaid.ai/open-source/syntax/entityRelationshipDiagram.html) format directly from your database schema.

It keeps your ER diagrams consistently updated with every schema change, effortlessly.

---

## 🤔 Why Keep ER Diagrams Updated?

Keeping an ER diagram in sync with your database schema is a strategic advantage for modern development teams.

### The "Documentation-as-Code" Advantage

Mermaidb embraces a **Documentation-as-Code** philosophy, which offers significant advantages over traditional database visualization tools that connect directly to a live database:

*   **Versioned and Reviewable**: Because the diagram is a text file (`.mmd`), it lives in your Git repository. Schema changes can be visually reviewed as part of a pull request, just like any other code change.
*   **Enhanced Security**: The diagram is generated from your local migration files within a temporary, isolated environment. You never need to grant a third-party tool access to your staging or production database credentials.
*   **Guaranteed Consistency**: By integrating diagram generation into your development workflow (e.g., via Git hooks), you ensure the documentation is never out of sync with the actual schema.
*   **Ultimate Portability**: The generated `.mmd` file is plain text. It can be rendered by numerous tools, embedded in wikis, or shared easily, without requiring a database connection.

This approach treats your database schema documentation as a first-class citizen of your codebase, making it more reliable, secure, and collaborative.

### For Development Teams & DBAs

*   **Clear Communication**: An up-to-date diagram is a single source of truth, facilitating communication between developers, DBAs, and architects.
*   **Simplified Planning**: When planning new features or schema changes, engineers can copy the diagram's code into an editor like [Mermaid.live](https://mermaid.live), experiment with modifications, and visualize the impact in real-time. This simplifies writing user stories and planning database migrations.
*   **Faster Onboarding**: New team members can quickly understand the database structure, accelerating their integration into the project.

### For AI-Assisted Development

*   **Enhanced AI Context**: When working with AI assistants (like **GitHub Copilot, Google Gemini, or Anthropic Claude**), providing the current ER diagram as context allows the AI to understand your database structure deeply.
*   **Fewer Errors**: With a clear understanding of tables, columns, and relationships, the AI is less likely to make mistakes when generating database queries, migrations, or application code, leading to more accurate and reliable results.

---

## ✨ Features

*   🚀 **Ephemeral Database**: Spins up a temporary database in a Docker container to build the schema safely.
*   🔄 **Liquibase Integration**: Applies your Liquibase migrations to construct the schema.
*   🔍 **Automatic Diagram Generation**: Extracts the schema into a `.mmd` file using the [Mermerd CLI](https://github.com/KarnerTh/mermerd).
*   🧩 **Modular Diagrams**: Intelligently groups tables into smaller, domain-focused diagrams for better readability.
*   ⚡️ **Smart Git Integration**: Optimizes the build by only running when database migration files have changed.
*   ⚙️ **Automatic Git Add**: Automatically stages generated diagrams with `git add` to include them in your next commit.
*   💪 **Force Generation**: Allows forcing the diagram regeneration, bypassing the smart Git check.

---

## ✅ Prerequisites & Compatibility

Before you begin, ensure your development environment meets the following requirements:

*   **Docker**: Must be installed and running, as the plugin relies on it to create ephemeral database containers.
*   **Liquibase**: Your project must use Liquibase to manage database migrations.
*   **Java**: Version 11 or newer.
*   **Gradle**: Version 7.6 or newer.

---

## 🚀 Getting Started

### 1. Apply the Plugin

In your `build.gradle` file, apply the plugin using its ID and desired version. The current version is `1.0.1`.

```groovy
// build.gradle
plugins {
    id 'com.waltsoft.mermaidb' version '1.0.1'
}
```

### 2. Configure the `mermaidb` Extension

Configure the plugin with your project's specific settings.

```groovy
// build.gradle
mermaidb {
    dbType = 'POSTGRESQL'
    dbVersion = '16'
    changeLogFilePath = 'db/changelog/changelog-master.xml'
    outputDirPath = 'docs/diagrams/db'
    autoGitAdd = true
}
```

#### Configuration Options

| Property              | Description                                                                                                                                       | Required | Default Value |
|-----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|----------|---------------|
| `dbType`              | The type of database to use. Supported: `POSTGRESQL`, `ALLOYDB`, `MYSQL`, `SQLSERVER`, `SQLITE`, `MARIADB`, `COCKROACHDB`, `TIDB`.                | **Yes**  | `null`        |
| `changeLogFilePath`   | The relative path to your master Liquibase changelog file.                                                                                        | **Yes**  | `null`        |
| `outputDirPath`       | The directory where the generated `.mmd` diagram files will be saved.                                                                             | **Yes**  | `null`        |
| `dbVersion`           | The Docker image tag for the chosen database (e.g., `16` for `postgres:16`).                                                                      | **Yes*** | `null`        |
| `dbCustomDockerImage` | A custom Docker image for the database (e.g., `postgis/postgis:16-3.4`). Use this for images with extensions like PostGIS. Overrides `dbVersion`. | No       | `null`        |
| `autoGitAdd`          | If `true`, automatically runs `git add` on the generated diagrams.                                                                                | No       | `false`       |
| `uppercaseColumns`    | If `true`, converts all column names in the diagram to uppercase.                                                                                 | No       | `false`       |

*\*`dbVersion` is required if `dbCustomDockerImage` is not provided (and `dbType` is not `SQLITE`).*

### 3. Generate the Diagram

Execute the following command in your terminal:

```bash
./gradlew generateDatabaseDiagram
```

The generated diagrams will be available in the directory specified in `outputDirPath`.

---

## ⚡️ Advanced Usage

### Automating with Git Hooks

To ensure your diagrams are always up-to-date with your schema, you can configure the plugin to run automatically before each commit using a Git pre-commit hook.

This example uses the `com.github.jakemarsden.git-hooks` plugin to trigger the `generateDatabaseDiagram` task.

```groovy
// build.gradle

// Apply the git-hooks plugin
plugins {
    id "com.github.jakemarsden.git-hooks" version "0.0.2"
    id 'com.waltsoft.mermaidb' version '1.0.1'
}

// Add the diagram generation task to your 'check' task
tasks.named('check') {
    dependsOn 'generateDatabaseDiagram'
}

// Configure the pre-commit hook to run 'check'
gitHooks {
    hooks = ['pre-commit': 'check']
}

mermaidb {
    // Your configuration here...
    autoGitAdd = true // Recommended for pre-commit hooks
}
```

With this setup, your diagrams will be regenerated and staged for commit automatically whenever you change your database schema.

### Forcing Generation

To regenerate diagrams regardless of the Git state, use the `-PforceGenerate=true` flag:

```bash
./gradlew generateDatabaseDiagram -PforceGenerate=true
```

---

## 🛠️ For Developers (Contributing)

If you want to contribute to Mermaidb, you can easily test your changes locally.

### How to Test Locally

1.  **Publish to Maven Local**:
    Run the following command to publish the plugin to your local Maven repository:
    ```bash
    ./gradlew publishToMavenLocal
    ```

2.  **Configure the Consumer Project**:
    In the project where you want to test the plugin, add `mavenLocal()` to the `pluginManagement` block in your `settings.gradle` file.

    ```groovy
    // settings.gradle
    pluginManagement {
        repositories {
            mavenLocal() // Add this line
            gradlePluginPortal()
        }
    }
    ```

### Running Tests

To execute all tests, you can run:
```bash
./gradlew test
```

To execute all tests in a specific test class, run:
```bash
./gradlew test --tests SomeTestClass
```

To execute a single specified test in a class, run:
```bash
./gradlew test --tests SomeTestClass.someSpecificMethod
```

### Code Quality Check

To run all tests and verify code quality and style, use the command below:
```bash
./gradlew check
```

---

## 📜 License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
