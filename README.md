# Unleash Client SDK for Kotlin 
This is the Unleash Client SDK for Kotlin. This was converted from [unleash-client-java](https://github.com/Unleash/unleash-client-java) and had Java 8 functionality that Android 21+ doesn't support removed.

### Create a new Unleash instance

It is easy to get a new instance of Unleash. In your app you typically *just want one instance of Unleash*, and inject that where you need it. You will typically use a dependency injection frameworks such as Dagger to manage this. 

To create a new instance of Unleash you need to pass in a config object:
```kotlin

val config: UnleashConfig = unleashConfig { 
  appName("java-test")
  instanceId("instance x")
  unleashAPI("http://unleash.herokuapp.com/api/")
}.build()

val unleash = new DefaultUnleash(config)
```

### Awesome feature toggle API

It is really simple to use unleash.

```kotlin
if(unleash.isEnabled("AwesomeFeature")) {
  //do some magic
} else {
  //do old boring stuff
}
```

Calling `unleash.isEnabled("AwesomeFeature")` is the equivalent of calling `unleash.isEnabled("AwesomeFeature", false)`. 
Which means that it will return `false` if it cannot find the named toggle. 

If you want it to default to `true` instead, you can pass `true` as the second argument:

```kotlin
unleash.isEnabled("AwesomeFeature", true)
```

### Activation strategies

The Kotlin client comes with implementations for the built-in activation strategies 
provided by unleash. 

- DefaultStrategy
- UserWithIdStrategy
- GradualRolloutRandomStrategy
- GradualRolloutUserWithIdStrategy
- GradualRolloutSessionIdStrategy
- RemoteAddressStrategy
- ApplicationHostnameStrategy

Read more about the strategies in [activation-strategy.md](https://github.com/Unleash/unleash/blob/master/docs/activation-strategies.md).

#### Custom strategies
You may also specify and implement your own strategy. The specification must be registered in the Unleash UI and 
you must register the strategy implementation when you wire up unleash. 

```kotlin
val s1 = MyAwesomeStrategy()
val s2 = MySuperAwesomeStrategy()
val unleash = DefaultUnleash(config, s1, s2)

```

### Unleash context

In order to use some of the common activation strategies you must provide a [unleash-context](https://github.com/Unleash/unleash/blob/master/docs/unleash-context.md).
This client SDK provides two ways of provide the unleash-context:

#### 1. As part of isEnabled call
This is the simplest and most explicit way of providing the unleash context. 
You just add it as an argument to the `isEnabled` call. 


```kotlin
val context = unleashContext {
  userId("user@mail.com")
}.build()

unleash.isEnabled("someToggle", context)
``` 


#### 2. Via a UnleashContextProvider
This is a bit more advanced approach, where you configure a unleash-context provider. 
By doing this you do not have rebuild or pass the unleash-context object to every 
place you are calling `unleash.isEnabled`. 

The provider typically binds the context to the same thread as the request. 
If you are using Spring the `UnleashContextProvider` will typically be a 
'request scoped' bean. 


```kotlin
val contextProvider = MyAwesomeContextProvider()

val config = unleashConfig {
  appName("java-test")
  instanceId("instance x")
  unleashAPI("http://unleash.herokuapp.com/api/")
  unleashContextProvider(contextProvider)
}.build()

val unleash = DefaultUnleash(config)

// Anywhere in the code unleash will get the unleash context from your registered provider. 
unleash.isEnabled("someToggle")
``` 

### Custom HTTP headers
If you want the client to send custom HTTP Headers with all requests to the Unleash API 
you can define that by setting them via the `UnleashConfig`. 

```kotlin
val config = unleashConfig {
  appName("my-app")
  instanceId("my-instance-1")
  unleashAPI(unleashAPI)
  customHttpHeader("Authorization", "12312Random")
}.build()
```

### Dynamic custom HTTP headers
If you need custom http headers that change during the lifetime of the client, a provider can be defined via the `UnleashConfig`.
```kotlin
class CustomHttpHeadersProviderImpl : CustomHttpHeadersProvider {
  override fun getCustomHeaders(): Map<String, String> {
    val token = "Acquire or refresh token"
    return mutableMapOf(Pair("Authorization", "Bearer $token"))
  }
}
```
```kotlin
val provider = CustomHttpHeadersProviderImpl()

val unleashConfig = unleashConfig { 
  appName("my-app")
  instanceId("my-instance-1")
  unleashAPI(unleashAPI)
  customHttpHeadersProvider(provider)
}.build()
```


### Subscriber API
Sometimes you want to know when Unleash updates internally. This can be achieved by registering a subscriber. An example on how to configure a custom subscriber is shown below. Have a look at [UnleashSubscriber.kt](https://github.com/silvercar/unleash-client-kotlin/blob/master/unleash/src/main/java/com/silvercar/unleash/event/UnleashSubscriber.kt) to get a complete overview of all methods you can override.


```kotlin
val unleashConfig = unleashConfig {
  appName("my-app")
  instanceId("my-instance-1")
  unleashAPI(unleashAPI)
  subscriber(object : UnleashSubscriber { 
    override fun onReady(ready: UnleashReady) {
      System.out.println("Unleash is ready")
    }
    
    override fun togglesFetched(response: FeatureToggleResponse) {
      System.out.println("Fetch toggles with status: " + toggleResponse.getStatus())
    }

    override fun togglesBackedUp(toggleCollection: ToggleCollection) {
      System.out.println("Backup stored.")
    }
  })
}.build()
```

### Options 

- **appName** - Required. Should be a unique name identifying the client application using Unleash. 
- **synchronousFetchOnInitialisation** - Allows the user to specify that the unleash-client should do one synchronous fetch to the `unleash-api` at initialisation. This will slow down the initialisation (the client must wait for a http response). If the `unleash-api` is unavailable the client will silently move on and assume the api will be available later. 

### HTTP Proxy with Authentication
The Unleash Kotlin client uses `HttpURLConnection` as HTTP Client which already recognizes the common JVM proxy settings such as `http.proxyHost` and
`http.proxyPort`. So if you are using a Proxy without authentication, everything works out of the box. However, if you have to use Basic Auth
authentication with your proxy, the related settings such as `http.proxyUser` and `http.proxyPassword` do not get recognized by default. In order
to enable support for basic auth against a http proxy, you can simply enable the following option on the configuration builder:

```kotlin
config = unleashConfig {
  appName("my-app")
  unleashAPI("http://unleash.org")
  enableProxyAuthenticationByJvmProperties()
}.build()
```

## Local backup
By default unleash-client fetches the feature toggles from unleash-server every 10s, and stores the 
result in `unleash-repo.json` which is located in the `java.io.tmpdir` directory. This means that if 
the unleash-server becomes unavailable, the unleash-client will still be able to toggle the features 
based on the values stored in `unleash-repo.json`. As a result of this, the second argument of 
`isEnabled` will be returned in two cases:

* When `unleash-repo.json` does not exists
* When the named feature toggle does not exist in `unleash-repo.json`


## Unit testing
You might want to control the state of the toggles during unit-testing.
Unleash do come with a ```FakeUnleash``` implementation for doing this. 

Some examples on how to use it below:


```kotlin
// example 1: everything on
var fakeUnleash = FakeUnleash()
fakeUnleash.enableAll()

assertTrue(fakeUnleash.isEnabled("unknown"))
assertTrue(fakeUnleash.isEnabled("unknown2"))

// example 2
var fakeUnleash = FakeUnleash()
fakeUnleash.enable("t1", "t2")

assertTrue(fakeUnleash.isEnabled("t1"))
assertTrue(fakeUnleash.isEnabled("t2"))
assertFalse(fakeUnleash.isEnabled("unknown"))

// example 3: variants
var fakeUnleash = FakeUnleash()
fakeUnleash.enable("t1", "t2")
fakeUnleash.setVariant("t1", Variant("a", null, true))

assertEquals(fakeUnleash.getVariant("t1").getName(), "a")
```

See more in [FakeUnleashTest.java](https://github.com/silvercar/unleash-client-kotlin/blob/master/unleash/src/test/java/com/silvercar/unleash/FakeUnleashTest.java)

## Development

Build:
```bash
gradlew clean assemble 
```

