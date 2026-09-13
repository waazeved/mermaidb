package io.github.waazeved.mermaidb.extension;

import io.github.waazeved.mermaidb.database.DatabaseType;
import org.gradle.api.GradleException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExtensionValidatorTest {

    @Mock
    private Extension extension;

    private ExtensionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ExtensionValidator(extension);
    }

    @Test
    @DisplayName("Should not throw exception when all properties are valid")
    void shouldNotThrowExceptionWhenAllPropertiesAreValid() {
        Mockito.when(extension.getDbType()).thenReturn(DatabaseType.POSTGRESQL);
        Mockito.when(extension.getDbVersion()).thenReturn("13");
        Mockito.when(extension.getChangeLogFilePath()).thenReturn("/path/to/changelog.sql");
        Mockito.when(extension.getOutputDirPath()).thenReturn("/path/to/output");

        Assertions.assertDoesNotThrow(() -> validator.validate());
    }

    @Test
    @DisplayName("Should not throw exception for SQLite with minimal configuration")
    void shouldNotThrowExceptionForSQLiteWithMinimalConfiguration() {
        Mockito.when(extension.getDbType()).thenReturn(DatabaseType.SQLITE);
        Mockito.when(extension.getChangeLogFilePath()).thenReturn("/path/to/changelog.sql");
        Mockito.when(extension.getOutputDirPath()).thenReturn("/path/to/output");

        Assertions.assertDoesNotThrow(() -> validator.validate());
    }

    @Test
    @DisplayName("Should throw exception when dbType is null")
    void shouldThrowExceptionWhenDbTypeIsNull() {
        Mockito.when(extension.getDbType()).thenReturn(null);
        Assertions.assertThrows(GradleException.class, () -> validator.validate(), "Error (Mermaidb): The 'dbType' field is required.");
    }

    @Test
    @DisplayName("Should throw exception when dbVersion is required but not provided")
    void shouldThrowExceptionWhenDbVersionIsRequiredButNotProvided() {
        Mockito.when(extension.getDbType()).thenReturn(DatabaseType.POSTGRESQL);
        Mockito.when(extension.getDbVersion()).thenReturn(null);
        Mockito.when(extension.getDbCustomDockerImage()).thenReturn(null);
        Assertions.assertThrows(GradleException.class, () -> validator.validate(), "Error (Mermaidb): The 'dbVersion' field is required when not using a custom Docker image.");
    }

    @Test
    @DisplayName("Should throw exception when changeLogFilePath is null")
    void shouldThrowExceptionWhenChangeLogFilePathIsNull() {
        Mockito.when(extension.getDbType()).thenReturn(DatabaseType.SQLITE);
        Mockito.when(extension.getChangeLogFilePath()).thenReturn(null);
        Assertions.assertThrows(GradleException.class, () -> validator.validate(), "Error (Mermaidb): The 'changeLogFilePath' field is required.");
    }

    @Test
    @DisplayName("Should throw exception when outputDirPath is null")
    void shouldThrowExceptionWhenOutputDirPathIsNull() {
        Mockito.when(extension.getDbType()).thenReturn(DatabaseType.SQLITE);
        Mockito.when(extension.getChangeLogFilePath()).thenReturn("/path/to/changelog.sql");
        Mockito.when(extension.getOutputDirPath()).thenReturn(null);
        Assertions.assertThrows(GradleException.class, () -> validator.validate(), "Error (Mermaidb): The 'outputDirPath' field is required.");
    }

    @Test
    @DisplayName("Should not throw exception when using custom docker image without dbVersion")
    void shouldNotThrowExceptionWhenUsingCustomDockerImage() {
        Mockito.when(extension.getDbType()).thenReturn(DatabaseType.POSTGRESQL);
        Mockito.when(extension.getDbCustomDockerImage()).thenReturn("my-custom-postgres:13");
        Mockito.when(extension.getChangeLogFilePath()).thenReturn("/path/to/changelog.sql");
        Mockito.when(extension.getOutputDirPath()).thenReturn("/path/to/output");

        Assertions.assertDoesNotThrow(() -> validator.validate());
    }
}
