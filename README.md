# data-service

⚠️ **DEPRECATED Another project has been created to continue the system. [New project](https://github.com/Rushyverse/core)** ⚠️

[![Discord](https://img.shields.io/discord/977166213467734056.svg?color=&label=Discord&logo=discord&style=for-the-badge)](https://discord.gg/EQyycAUZtt)

This project allows managing data with the database, cache or other through several services. The goal is that a
developer no longer needs to manipulate data handlers in his program to store and retrieve data. So he uses a set of
interface to do the operations he wants.

## Environment

We have chosen to use [Kotlin](https://kotlinlang.org/) to simplify our codes, learn the language and take advantage of
coroutines for the I/O operations.

[Gradle](https://gradle.org/) is used to manage dependencies because he's the more friendly with Kotlin.

The project is compiled to :

- [Java 8](https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html)

## Installation

You can find the artifact on [Jitpack](https://jitpack.io/#UniverseProject/data-service).
Use the version you prefer by following the tutorial on jitpack and replacing `{version}` bellow.

### Gradle (groovy)

```groovy
repositories {
  maven {
    url "https://jitpack.io"
  }
}
```

---

```groovy
dependencies {
  implementation("com.github.UniverseProject:data-service:{version}")
}
```

### Gradle (kotlin)

```kotlin
repositories {
  maven { url = uri("https://jitpack.io") }
}
```

```kotlin
dependencies {
  implementation("com.github.UniverseProject:data-service:{version}")
}
```

### Maven

```xml
<repositories>
  <repository>
    <id>jitpack</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
      <groupId>com.github.UniverseProject</groupId>
      <artifactId>data-service</artifactId>
      <version>{version}</version>
  </dependency>
</dependencies>
```

## Cache

The [Cache client](src/main/kotlin/io/github/universeproject/dataservice/cache/CacheClient.kt) allows managing connection and
interaction with cache automatically. You can create an instance like that :

```kotlin
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.support.BoundedPoolConfig
import kotlinx.serialization.protobuf.ProtoBuf
import io.github.universeproject.dataservice.cache.CacheClient

suspend fun createCacheClient(): CacheClient {
    return CacheClient {
        uri = RedisURI.create("localhost:6379") // required
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

## Service

Several services are available to interact with the database or the cache.

```kotlin
import io.github.universeproject.MojangAPIImpl
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.support.BoundedPoolConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import io.github.universeproject.dataservice.cache.CacheClient
import io.github.universeproject.dataservice.data.*
import io.github.universeproject.dataservice.supplier.SupplierConfiguration
import io.github.universeproject.dataservice.supplier.database.EntitySupplier
import java.util.*

public suspend fun createCacheClient(): CacheClient {
  return CacheClient {
    uri = RedisURI.create("localhost:6379") // required
    client = RedisClient.create() // optional
    binaryFormat = ProtoBuf { } // optional
    codec = ByteArrayCodec.INSTANCE // optional
    poolConfiguration = BoundedPoolConfig.builder().maxTotal(-1).build() // optional
  }
}

public fun createHttpClient(): HttpClient {
  return HttpClient(CIO) {
    expectSuccess = true
    install(ContentNegotiation) {
      json(Json { ignoreUnknownKeys = true })
    }
  }
}

public suspend fun main() {
  val cacheClient: CacheClient = createCacheClient()
  val uuid = UUID.randomUUID()

  // Create cache service for client identity data
  val clientIdentityCacheService: ClientIdentityCacheService =
    ClientIdentityCacheServiceImpl(client = cacheClient, prefixKey = "c:", cacheByUUID = true, cacheByName = false)

  // Get a client identity by its ID from cache
  println(clientIdentityCacheService.getByUUID(uuid))

  val configuration = SupplierConfiguration(
    mojangAPI = MojangAPIImpl(createHttpClient()),
    clientIdentityCache = clientIdentityCacheService,
    profileSkinCache = ProfileSkinCacheServiceImpl(cacheClient, "skin:"),
    profileIdCache = ProfileIdCacheServiceImpl(client = cacheClient, prefixKey = "profile:"),
  )
  // OR
  // val configuration = SupplierConfiguration(
  //   mojangAPI = MojangAPIImpl(createHttpClient()),
  //   cacheClient = cacheClient,
  // )

  // Common service (database & cache) according to the supplier
  val strategy: EntitySupplier = EntitySupplier.cacheWithCachingDatabaseFallback(configuration)
  val clientIdentityService: ClientIdentityService = ClientIdentityServiceImpl(strategy)
  // Get a client identity by its ID from database and (if found) register it into cache
  println(clientIdentityService.getByUUID(uuid))
}
```

Here a list of service usable :

| Service                                                                                                   | Import                                       |
|-----------------------------------------------------------------------------------------------------------|----------------------------------------------|
| [ClientIdentityServiceImpl](src/main/kotlin/io/github/universeproject/dataservice/data/ClientIdentity.kt) | io.github.universeproject.dataservice.data.* |
| [ProfileIdServiceImpl](src/main/kotlin/io/github/universeproject/dataservice/data/ProfileId.kt)           | io.github.universeproject.dataservice.data.* |
| [ProfileSkinServiceImpl](src/main/kotlin/io/github/universeproject/dataservice/data/ProfileSkin.kt)       | io.github.universeproject.dataservice.data.* |

### Supplier

The suppliers allow defining behavior when you interact with a data.

- [Supplier for database](src/main/kotlin/io/github/universeproject/dataservice/supplier/database)
- [Supplier for http](src/main/kotlin/io/github/universeproject/dataservice/supplier/http)

You can use each type of supplier using the static variable from the
[EntitySupplier database](src/main/kotlin/io/github/universeproject/dataservice/supplier/database/EntitySupplier.kt) and
[EntitySupplier http](src/main/kotlin/io/github/universeproject/dataservice/supplier/http/EntitySupplier.kt).

Using a service, you can change the supplier

```kotlin
import io.github.universeproject.dataservice.data.ClientIdentityService
import io.github.universeproject.dataservice.supplier.SupplierConfiguration
import io.github.universeproject.dataservice.supplier.database.EntitySupplier

// ..
val configuration: SupplierConfiguration = // ..
val clientIdentityService: ClientIdentityService = // ..
// Use exclusively the database    
  clientIdentityService.withStrategy(EntitySupplier.database())
// Use exclusively the database and cache the result
clientIdentityService.withStrategy(EntitySupplier.cachingDatabase(configuration))
// ..
```

## Build

To build the project, you need to use the gradle app ([gradlew.bat](gradlew.bat) for windows
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
