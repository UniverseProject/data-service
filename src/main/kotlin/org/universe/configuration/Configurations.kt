package org.universe.configuration

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.source.yaml
import org.universe.database.dao.ClientIdentity
import org.universe.model.ProfileId
import org.universe.model.ProfileSkin

/**
 * Configuration for cache.
 */
internal object CacheConfiguration : ConfigSpec("cache") {

    /**
     * Configuration for cache for [ClientIdentity] data.
     */
    internal object ClientIdentityConfiguration : ConfigSpec("clientId") {
        val useUUID by optional(true, description = "Allows the get and set from cache using client uuid")
        val useName by optional(false, description = "Allows the get and set from cache using client name")
        val prefixKey by optional("cliId:", description = "Prefix to register a data in cache")
    }

    /**
     * Configuration for cache for [ProfileSkin] data.
     */
    internal object ProfileSkinConfiguration : ConfigSpec("skin") {
        val prefixKey by optional("skin:", description = "Prefix to register a data in cache")
    }

    /**
     * Configuration for cache for [ProfileId] data.
     */
    internal object ProfileIdConfiguration : ConfigSpec("profilId") {
        val prefixKey by optional("profId:", description = "Prefix to register a data in cache")
    }
}

/**
 * Allows loading and refresh the configuration of the services.
 */
internal object ServiceConfiguration {

    var cacheConfiguration: Config = loadCacheConfiguration()
        private set

    /**
     * Reload all configurations of the services.
     * After the execution of the function, the configurations are updated.
     */
    fun reloadConfigurations() {
        cacheConfiguration = loadCacheConfiguration()
    }

    /**
     * Load the cache configuration linked to the class [CacheConfiguration].
     * @return New configuration loaded.
     */
    private fun loadCacheConfiguration(): Config = Config { addSpec(CacheConfiguration) }
        .from.yaml.file("configuration.yml", optional = true)
        .from.env()
        .from.systemProperties()

}