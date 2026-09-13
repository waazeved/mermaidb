package com.waltsoft.mermaidb.docker

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito

@DisplayName("Tests for Docker class")
class DockerTest {

    @Nested
    @DisplayName("Tests for getDynamicPort method")
    class GetDynamicPortTest {

        @Test
        @DisplayName("Should parse and return the correct port on valid output")
        void shouldReturnCorrectPort() {
            final int expectedPort = 32768
            String dockerOutput = "0.0.0.0:${expectedPort}"
            ProcessBuilder pbMock = mockProcessBuilder(dockerOutput)
            Docker docker = new Docker()

            Integer port = docker.getDynamicPort(pbMock)

            Assertions.assertEquals(expectedPort, port, "The parsed port is incorrect")
        }

        @Test
        @DisplayName("Should throw RuntimeException on empty output")
        void shouldThrowOnEmptyOutput() {
            String dockerOutput = ""
            ProcessBuilder pbMock = mockProcessBuilder(dockerOutput)
            Docker docker = new Docker()

            RuntimeException exception = Assertions.assertThrows(RuntimeException.class, {
                docker.getDynamicPort(pbMock)
            }, "Expected a RuntimeException to be thrown for empty output")

            Assertions.assertTrue(exception.getMessage().contains("Failed to retrieve dynamic port"))
        }

        @Test
        @DisplayName("Should handle multi-line output (IPv6) correctly")
        void shouldHandleMultiLineOutput() {
            final int expectedPort = 49153
            String dockerOutput = "0.0.0.0:${expectedPort}\n[::]:${expectedPort}"
            ProcessBuilder pbMock = mockProcessBuilder(dockerOutput)
            Docker docker = new Docker()

            Integer port = docker.getDynamicPort(pbMock)

            Assertions.assertEquals(expectedPort, port, "The parsed port from multi-line output is incorrect")
        }

        @Test
        @DisplayName("Should throw an exception on malformed output")
        void shouldThrowOnMalformedOutput() {
            String dockerOutput = "Error: No such container"
            ProcessBuilder pbMock = mockProcessBuilder(dockerOutput)
            Docker docker = new Docker()

            Assertions.assertThrows(NumberFormatException.class, {
                docker.getDynamicPort(pbMock)
            }, "Expected a NumberFormatException for malformed output")
        }

        private ProcessBuilder mockProcessBuilder(String output) {
            ProcessBuilder processBuilderMock = Mockito.mock(ProcessBuilder.class)
            Process processMock = Mockito.mock(Process.class)
            Mockito.when(processBuilderMock.start()).thenReturn(processMock)
            InputStream inputStream = new ByteArrayInputStream(output.getBytes())
            Mockito.when(processMock.getInputStream()).thenReturn(inputStream)
            return processBuilderMock
        }
    }
}
