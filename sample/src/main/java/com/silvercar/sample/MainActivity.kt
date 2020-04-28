package com.silvercar.sample

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.silvercar.unleash.DefaultUnleash
import com.silvercar.unleash.Unleash
import com.silvercar.unleash.event.UnleashReady
import com.silvercar.unleash.event.UnleashSubscriber
import com.silvercar.unleash.repository.FeatureToggleResponse
import com.silvercar.unleash.repository.ToggleCollection
import com.silvercar.unleash.util.unleashConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class MainActivity : AppCompatActivity() {
  private lateinit var unleash: Unleash
  private lateinit var unleashEnabled: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    setSupportActionBar(findViewById(R.id.toolbar))

    supportActionBar?.let { setTitle(R.string.app_name) }

    unleashEnabled = findViewById(R.id.unleashEnabled)

    CoroutineScope(Dispatchers.Main).launch {
      unleash = withContext(Dispatchers.IO) { initUnleash() }
      val isEnabled = unleash.isEnabled("test-android")

      Timber.d("isEnabled just got set to %b", isEnabled)
      unleashEnabled.text = getString(R.string.unleash_test_android_enabled_formatted, isEnabled)
    }
  }

  // TODO: Need to change how Unleash is init because this is very painful to integrate
  private suspend fun initUnleash(): Unleash {
    lateinit var subscriber: UnleashSubscriber

    val unleash: Unleash by lazy {
      DefaultUnleash(unleashConfig {
        appName(application.packageName)
        unleashAPI("https://unleash.silvercar.com/api")
        fetchTogglesInterval(TimeUnit.MINUTES.toSeconds(INTERVAL))
        sendMetricsInterval(TimeUnit.MINUTES.toSeconds(INTERVAL))
        subscriber(subscriber)
      }.build(), EnvironmentStrategy())
    }

    return suspendCancellableCoroutine<Unleash> { coroutine ->
      subscriber = object : UnleashSubscriber {
        override fun onReady(ready: UnleashReady) {
          Timber.d("Unleash is ready")
          coroutine.resume(unleash)
        }

        override fun togglesFetched(response: FeatureToggleResponse) {
          Timber.d("Fetch toggles with status: %s", response.status)
        }

        override fun togglesBackedUp(toggleCollection: ToggleCollection) {
          Timber.d("Backup stored.")
        }
      }

      coroutine.invokeOnCancellation { unleash.shutdown() }

      // init Unleash
      unleash
    }
  }

  private companion object {
    const val INTERVAL: Long = 60
  }
}
