package com.silvercar.unleash

import com.silvercar.unleash.event.EventDispatcher
import com.silvercar.unleash.event.ToggleEvaluated
import com.silvercar.unleash.metric.UnleashMetricService
import com.silvercar.unleash.repository.FeatureToggleRepository
import com.silvercar.unleash.repository.HttpToggleFetcher
import com.silvercar.unleash.repository.ToggleBackupHandlerFile
import com.silvercar.unleash.repository.ToggleRepository
import com.silvercar.unleash.strategy.ApplicationHostnameStrategy
import com.silvercar.unleash.strategy.ConstraintUtil
import com.silvercar.unleash.strategy.DefaultStrategy
import com.silvercar.unleash.strategy.FlexibleRolloutStrategy
import com.silvercar.unleash.strategy.GradualRolloutRandomStrategy
import com.silvercar.unleash.strategy.GradualRolloutSessionIdStrategy
import com.silvercar.unleash.strategy.GradualRolloutUserIdStrategy
import com.silvercar.unleash.strategy.RandomGenerator
import com.silvercar.unleash.strategy.RemoteAddressStrategy
import com.silvercar.unleash.strategy.Strategy
import com.silvercar.unleash.strategy.UnknownStrategy
import com.silvercar.unleash.strategy.UserWithIdStrategy
import com.silvercar.unleash.util.UnleashConfig
import com.silvercar.unleash.variant.VariantUtil
import java.util.Random

@Suppress("TooManyFunctions") class DefaultUnleash(
  private val config: UnleashConfig,
  private val toggleRepository: ToggleRepository,
  vararg strategies: Strategy
) : Unleash {
  private val metricService: UnleashMetricService
  private val strategyMap: Map<String, Strategy>
  private val contextProvider: UnleashContextProvider
  private val eventDispatcher: EventDispatcher

  @Suppress("SpreadOperator") constructor(
    unleashConfig: UnleashConfig,
    vararg strategies: Strategy
  ) : this(
    unleashConfig,
    defaultToggleRepository(unleashConfig),
    *strategies
  )

  init {
    strategyMap = buildStrategyMap(strategies.toList())
    contextProvider = config.contextProvider
    eventDispatcher = EventDispatcher(config)
    metricService = UnleashMetricService(config, config.scheduledExecutor)
    metricService.register(strategyMap.keys)
  }

  private fun buildStrategyMap(strategies: List<Strategy>): Map<String, Strategy> {
    val map: MutableMap<String, Strategy> = HashMap()
    for (strategy in BUILTIN_STRATEGIES) {
      map[strategy.name] = strategy
    }
    for (strategy in strategies) {
      map[strategy.name] = strategy
    }
    return map
  }

  override val featureToggleNames: List<String>
    get() = toggleRepository.featureNames

  override fun isEnabled(toggleName: String): Boolean {
    return isEnabled(toggleName, false)
  }

  override fun isEnabled(
    toggleName: String,
    defaultSetting: Boolean
  ): Boolean {
    return isEnabled(toggleName, contextProvider.getContext(), defaultSetting)
  }

  override fun isEnabled(
    toggleName: String,
    context: UnleashContext,
    defaultSetting: Boolean
  ): Boolean {
    return isEnabled(toggleName, context) { _: String, _: UnleashContext -> defaultSetting }
  }

  override fun isEnabled(
    toggleName: String,
    fallbackAction: (String, UnleashContext) -> Boolean
  ): Boolean {
    return isEnabled(toggleName, contextProvider.getContext(), fallbackAction)
  }

  override fun isEnabled(
    toggleName: String,
    context: UnleashContext,
    fallbackAction: (String, UnleashContext) -> Boolean
  ): Boolean {
    val enabled = checkEnabled(toggleName, context, fallbackAction)
    count(toggleName, enabled)
    eventDispatcher.dispatch(ToggleEvaluated(toggleName, enabled))
    return enabled
  }

  private fun count(toggleName: String, enabled: Boolean) {
    metricService.count(toggleName, enabled)
  }

  private fun checkEnabled(
    toggleName: String,
    context: UnleashContext,
    fallbackAction: (String, UnleashContext) -> Boolean
  ): Boolean {
    val featureToggle = toggleRepository.getToggle(toggleName)
    val enhancedContext = context.applyStaticFields(config)

    return if (featureToggle == null) {
      fallbackAction.invoke(toggleName, enhancedContext)
    } else if (!featureToggle.isEnabled) {
      false
    } else if (featureToggle.strategies.isEmpty()) {
      true
    } else {
      featureToggle.strategies.any { activationStrategy ->
        val strategy = getStrategy(activationStrategy.name)
        ConstraintUtil().validate(activationStrategy.constraints, enhancedContext)
            && strategy.isEnabled(activationStrategy.getParameters(), enhancedContext)
      }
    }
  }

  override fun getVariant(
    toggleName: String,
    context: UnleashContext
  ): Variant {
    return getVariant(toggleName, context, Variant.DISABLED_VARIANT)
  }

  override fun getVariant(
    toggleName: String,
    context: UnleashContext,
    defaultValue: Variant
  ): Variant {
    val featureToggle = toggleRepository.getToggle(toggleName)
    val enabled = checkEnabled(toggleName, context) { _: String, _: UnleashContext -> false }
    val variant = if (enabled && featureToggle != null) {
      VariantUtil().selectVariant(featureToggle, context, defaultValue)
    } else {
      defaultValue
    }
    metricService.countVariant(toggleName, variant.name)
    return variant
  }

  override fun getVariant(toggleName: String): Variant {
    return getVariant(toggleName, contextProvider.getContext())
  }

  override fun getVariant(
    toggleName: String,
    defaultValue: Variant
  ): Variant {
    return getVariant(toggleName, contextProvider.getContext(), defaultValue)
  }

  fun getFeatureToggleDefinition(toggleName: String): FeatureToggle? {
    return toggleRepository.getToggle(toggleName)
  }

  private fun getStrategy(strategy: String): Strategy {
    return strategyMap[strategy] ?: UNKNOWN_STRATEGY
  }

  override fun shutdown() {
    config.scheduledExecutor.shutdown()
  }

  companion object {
    private val BUILTIN_STRATEGIES = listOf(
      DefaultStrategy(),
      ApplicationHostnameStrategy(),
      GradualRolloutRandomStrategy(Random()),
      GradualRolloutSessionIdStrategy(),
      GradualRolloutUserIdStrategy(),
      RemoteAddressStrategy(),
      UserWithIdStrategy(),
      FlexibleRolloutStrategy(RandomGenerator())
    )
    private val UNKNOWN_STRATEGY = UnknownStrategy()
    private fun defaultToggleRepository(unleashConfig: UnleashConfig): FeatureToggleRepository {
      return FeatureToggleRepository(
        unleashConfig,
        HttpToggleFetcher(unleashConfig),
        ToggleBackupHandlerFile(unleashConfig)
      )
    }
  }
}
