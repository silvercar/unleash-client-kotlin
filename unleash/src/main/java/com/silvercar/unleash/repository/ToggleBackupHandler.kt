package com.silvercar.unleash.repository

interface ToggleBackupHandler {
  fun read(): ToggleCollection
  fun write(toggleCollection: ToggleCollection)
}
