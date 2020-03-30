package com.silvercar.unleash.repository;

public interface ToggleBackupHandler {
    ToggleCollection read();

    void write(ToggleCollection toggleCollection);
}
