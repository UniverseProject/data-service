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

This library is not currently published in maven central (or other). But this could be done later.

### Cache

The [Cache client](src/main/kotlin/org/universe/cache/CacheClient.kt) allows managing connection and interaction with
cache automatically. You can create an instance like that :

```kotlin
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.support.BoundedPoolConfig
import kotlinx.serialization.protobuf.ProtoBuf
import org.universe.cache.CacheClient

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

Several services are available to interact with the database or the cache. They are
located [here](src/main/kotlin/org/universe/data).

The default implementation of cache services retrieve
the [Cache client](src/main/kotlin/org/universe/cache/CacheClient.kt) using the injection
by [koin](https://github.com/InsertKoinIO/koin). So before use them, you need to register the instance of cache client
in your application.

```kotlin
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.universe.cache.CacheClient
import org.universe.data.ClientIdentityCacheService
import org.universe.data.ClientIdentityCacheServiceImpl
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
  val clientIdentifyCacheService: ClientIdentityCacheService =
    ClientIdentityCacheServiceImpl(prefixKey = "c:", cacheByUUID = true, cacheByName = false)
  val id = clientIdentifyCacheService.getByUUID(uuid)
  println(id)
}
```

Here a list of service usable :

| Service                                                                               | Import               |
|---------------------------------------------------------------------------------------|----------------------|
| [ClientIdentityCacheServiceImpl](src/main/kotlin/org/universe/data/ClientIdentity.kt) | org.universe.data.*  |
| [ProfileIdServiceImpl](src/main/kotlin/org/universe/data/ProfileId.kt)                | org.universe.data.*  |
| [ProfileSkinServiceImpl](src/main/kotlin/org/universe/data/ProfileSkin.kt)            | org.universe.data.*  |

### Supplier

The suppliers allow defining behavior when you interact with a data.

- [Supplier for database](src/main/kotlin/org/universe/supplier/database)
- [Supplier for http](src/main/kotlin/org/universe/supplier/http)

You can use each type of supplier using the static variable from the 
[EntitySupplier database](src/main/kotlin/org/universe/supplier/database/EntitySupplier.kt) and 
[EntitySupplier http](src/main/kotlin/org/universe/supplier/http/EntitySupplier.kt).

Using a service, you can change the supplier

```kotlin
// Use exclusively the database
clientIdentifyCacheService.withStrategy(EntitySupplier.database)

// Use exclusively the database and cache the result
clientIdentifyCacheService.withStrategy(EntitySupplier.cachingDatabase)

// ...
```

### Configuration

If you use the default implementation of cache service, it's possible to change some values to set and get information.

| Variables                | Default value | Description                                                                                                                                                                       |
|--------------------------|---------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| cache.clientId.prefixKey | cliId:        | Define the prefix key to store the [Client identity](src/main/kotlin/org/universe/data/ClientIdentity.kt) information.                                                            |
| cache.clientId.useUUID   | true          | Store the [Client identity](src/main/kotlin/org/universe/data/ClientIdentity.kt) information using the uuid. *Storage using uuid should not be enabled if name usage is enabled.* |
| cache.clientId.useName   | false         | Store the [Client identity](src/main/kotlin/org/universe/data/ClientIdentity.kt) information using the name. *Storage using name should not be enabled if name uuid is enabled.*  |
| cache.skin.prefixKey     | skin:         | Define the prefix key to store the [Profil skin](src/main/kotlin/org/universe/data/ProfileSkin.kt) information.                                                                   |
| cache.profilId.prefixKey | profId:       | Define the prefix key to store the [Profil Id](src/main/kotlin/org/universe/data/ProfileId.kt) information.                                                                       |

The values are retrieved from the properties or the environment variables.

## Build

To build the project, you need to use the gradle app in the application [gradlew.bat](gradlew.bat) for windows and [gradlew](gradlew) for linux).
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