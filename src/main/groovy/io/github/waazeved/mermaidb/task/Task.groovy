package io.github.waazeved.mermaidb.task

interface Task {
    void register();

    String getName()
}