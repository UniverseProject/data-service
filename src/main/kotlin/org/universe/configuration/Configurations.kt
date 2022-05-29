package org.universe.configuration

import com.uchuhimo.konf.ConfigSpec

object AppConfiguration : ConfigSpec("app") {
    object CacheConfiguration : ConfigSpec("cache") {
        object ClientIdentityConfiguration : ConfigSpec("clientId") {
            val cacheByUUID by optional(true, description = "Allows the get and set from cache using client uuid")
            val cacheByName by optional(false, description = "Allows the get and set from cache using client name")
            val prefixKey by optional("cliId:", description = "Prefix to register a data in cache")
        }
    }
}