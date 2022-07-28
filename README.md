# DataService

This project allows managing data with the database, cache or other through several services. The goal is that a
developer no longer needs to manipulate data handlers in his program to store and retrieve data. So he uses a set of
interface to do the operations he wants.

## Environment

We have chosen to use [Kotlin](https://kotlinlang.org/) to simplify our codes, learn the language and take advantage of
coroutines for the I/O operations.

[Gradle](https://gradle.org/) is used to manage dependencies because he's the more friendly with Kotlin.

The project is compiled to [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html), so
you need this version to use the project.

## Prerequisites

- JDK 17 or higher

## Use in your projects

You can find the artifact on [Jitpack](https://jitpack.io/#UniverseProject/DataService).
Use the version you prefer by following the tutorial on jitpack.

For example, if I want the artifact at the commit `0123456789`, I need to declare :

````kotlin
// gradle.kts
repositories {
  maven { url = uri("https://jitpack.io") }
}

dependencies {
  implementation("com.github.UniverseProject:DataService:0123456789")
}
````

### Cache

The [Cache client](src/main/kotlin/org/universe/dataservice/cache/CacheClient.kt) allows managing connection and
interaction with cache automatically. You can create an instance like that :

```kotlin
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.support.BoundedPoolConfig
import kotlinx.serialization.protobuf.ProtoBuf
import org.universe.dataservice.cache.CacheClient

suspend fun createCacheClient(): CacheClient {
    return CacheClient {
        uri = RedisURI.create("my redis url") // required
        client = RedisClient.create() // optional
        binaryFormat = ProtoBuf { } // optional
        codec = ByteArrayCodec.INSTANCE // optional
        poolConfiguration = BoundedPoolConfig.builder().maxTotal(-1).build() // optional
    }
}
```

To get a new connection and begin interaction :

```kotlin
// In suspend function
val cacheClient: CacheClient = createCacheClient()
cacheClient.connect {
    // The connection is opened
    it.set("key", "value")
}
```

**To manage the data in the cache, we recommend using the services**

### Service

Several services are available to interact with the database or the cache.

The default implementation of cache services retrieve
the [Cache client](src/main/kotlin/org/universe/dataservice/cache/CacheClient.kt) using the injection
by [koin](https://github.com/InsertKoinIO/koin). So before use them, you need to register the instance of cache client
in your application.

```kotlin
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.universe.dataservice.cache.CacheClient
import org.universe.dataservice.data.ClientIdentityCacheService
import org.universe.dataservice.data.ClientIdentityCacheServiceImpl
import org.universe.dataservice.data.ClientIdentityService
import org.universe.dataservice.data.ClientIdentityServiceImpl
import org.universe.dataservice.supplier.database.EntitySupplier
import java.util.*


suspend fun main() {
  val cacheClient: CacheClient = createCacheClient()
  startKoin {
    modules(
      module {
        single { cacheClient }
      }
    )
  }

  val uuid = UUID.randomUUID()
  // cache service
  val clientIdentityCacheService: ClientIdentityCacheService =
    ClientIdentityCacheServiceImpl(prefixKey = "c:", cacheByUUID = true, cacheByName = false)
  println(clientIdentityCacheService.getByUUID(uuid))

  // common service (database & cache) according to the supplier
  val clientIdentityService: ClientIdentityService = ClientIdentityServiceImpl(EntitySupplier.cacheWithCachingDatabaseFallback)
  println(clientIdentityService.getByUUID(uuid))
}
```

Here a list of service usable :

| Service                                                                                      | Import                          |
|----------------------------------------------------------------------------------------------|---------------------------------|
| [ClientIdentityServiceImpl](src/main/kotlin/org/universe/dataservice/data/ClientIdentity.kt) | org.universe.dataservice.data.* |
| [ProfileIdServiceImpl](src/main/kotlin/org/universe/dataservice/data/ProfileId.kt)           | org.universe.dataservice.data.* |
| [ProfileSkinServiceImpl](src/main/kotlin/org/universe/dataservice/data/ProfileSkin.kt)       | org.universe.dataservice.data.* |

### Supplier

The suppliers allow defining behavior when you interact with a data.

- [Supplier for database](src/main/kotlin/org/universe/dataservice/supplier/database)
- [Supplier for http](src/main/kotlin/org/universe/dataservice/supplier/http)

You can use each type of supplier using the static variable from the
[EntitySupplier database](src/main/kotlin/org/universe/dataservice/supplier/database/EntitySupplier.kt) and
[EntitySupplier http](src/main/kotlin/org/universe/dataservice/supplier/http/EntitySupplier.kt).

Using a service, you can change the supplier

```kotlin
// Use exclusively the database
clientIdentityService.withStrategy(EntitySupplier.database)

// Use exclusively the database and cache the result
clientIdentityService.withStrategy(EntitySupplier.cachingDatabase)

// ...
```

### Configuration

If you use the default implementation of cache service, it's possible to change some values to set and get information.

| Variables                | Default value | Description                                                                                                                                                                                   |
|--------------------------|---------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| cache.clientId.prefixKey | cliId:        | Define the prefix key to store the [Client identity](src/main/kotlin/org/universe/dataservice/data/ClientIdentity.kt) information.                                                            |
| cache.clientId.useUUID   | true          | Store the [Client identity](src/main/kotlin/org/universe/dataservice/data/ClientIdentity.kt) information using the uuid. *Storage using uuid should not be enabled if name usage is enabled.* |
| cache.clientId.useName   | false         | Store the [Client identity](src/main/kotlin/org/universe/dataservice/data/ClientIdentity.kt) information using the name. *Storage using name should not be enabled if name uuid is enabled.*  |
| cache.skin.prefixKey     | skin:         | Define the prefix key to store the [Profil skin](src/main/kotlin/org/universe/dataservice/data/ProfileSkin.kt) information.                                                                   |
| cache.profilId.prefixKey | profId:       | Define the prefix key to store the [Profil Id](src/main/kotlin/org/universe/dataservice/data/ProfileId.kt) information.                                                                       |

The values are retrieved from the properties or the environment variables.

## Build

To build the project, you need to use the gradle app in the application [gradlew.bat](gradlew.bat) for windows
and [gradlew](gradlew) for linux).
`gradlew` is a wrapper to run gradle command without install it on our computer.

````shell
gradlew build
````

## Test

Some tests in the project require a **docker environment**.

### Windows

- Install [Docker Desktop](https://www.docker.com/products/docker-desktop/).
- You need to enable [WSL](https://docs.microsoft.com/en-us/windows/wsl/install) to give to docker desktop an
  environment with docker.
- Add a linux system for WSL, you can
  take [Ubuntu](https://apps.microsoft.com/store/detail/ubuntu-20044-lts/9MTTCL66CPXJ) or others
- In your linux system, update your packages and install docker.io. For example, with *apt*, the command
  is `apt install docker.io`

Now the application Docker Desktop can connect to the linux system to download and execute docker container

### Linux

- In your linux system, update your packages and install docker.io. For example, with *apt*, the command
  is `apt install docker.io`

When an image will be pulled and executed, this one will be run on your docker directly.

### Run tests

````shell
gradlew test
````
