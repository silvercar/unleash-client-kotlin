package com.silvercar.unleash.repository

import com.google.gson.JsonParseException
import com.silvercar.unleash.UnleashException
import com.silvercar.unleash.event.EventDispatcher
import com.silvercar.unleash.event.UnleashEvent
import com.silvercar.unleash.event.UnleashSubscriber
import com.silvercar.unleash.util.UnleashConfig
import org.apache.logging.log4j.LogManager
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class ToggleBackupHandlerFile(config: UnleashConfig) : ToggleBackupHandler {
  private val backupFile: String = config.backupFile
  private val eventDispatcher = EventDispatcher(config)

  override fun read(): ToggleCollection {
    LOG.info("Unleash will try to load feature toggle states from temporary backup")
    try {
      FileReader(backupFile).use { reader ->
        return JsonToggleParser().fromJson(BufferedReader(reader)).also {
          eventDispatcher.dispatch(ToggleBackupRead(it))
        }
      }
    } catch (exception: FileNotFoundException) {
      LOG.info(
        "Unleash could not find the backup-file '$backupFile'. This is expected behavior the " +
            "first time unleash runs in a new environment."
      )
    } catch (exception: IOException) {
      eventDispatcher.dispatch(
        UnleashException("Failed to read backup file: $backupFile", exception)
      )
    } catch (exception: IllegalStateException) {
      eventDispatcher.dispatch(
        UnleashException(
          "Failed to read backup file: $backupFile", exception
        )
      )
    } catch (exception: JsonParseException) {
      eventDispatcher.dispatch(
        UnleashException("Failed to read backup file: $backupFile", exception)
      )
    }
    return ToggleCollection(listOf())
  }

  override fun write(toggleCollection: ToggleCollection) {
    try {
      FileWriter(backupFile).use { writer ->
        writer.write(JsonToggleParser().toJsonString(toggleCollection))
        eventDispatcher.dispatch(ToggleBackupWritten(toggleCollection))
      }
    } catch (exception: IOException) {
      eventDispatcher.dispatch(
        UnleashException(
          "Unleash was unable to backup feature toggles to file: $backupFile", exception
        )
      )
    }
  }

  class ToggleBackupRead internal constructor(private val toggleCollection: ToggleCollection) :
    UnleashEvent {
    override fun publishTo(unleashSubscriber: UnleashSubscriber) {
      unleashSubscriber.toggleBackupRestored(toggleCollection)
    }
  }

  class ToggleBackupWritten internal constructor(private val toggleCollection: ToggleCollection) :
    UnleashEvent {
    override fun publishTo(unleashSubscriber: UnleashSubscriber) {
      unleashSubscriber.togglesBackedUp(toggleCollection)
    }
  }

  companion object {
    private val LOG =
      LogManager.getLogger(
        ToggleBackupHandlerFile::class.java
      )
  }
}
