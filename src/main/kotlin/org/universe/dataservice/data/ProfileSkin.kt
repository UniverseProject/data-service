@file:OptIn(ExperimentalLettuceCoroutinesApi::class, ExperimentalLettuceCoroutinesApi::class)

package org.universe.dataservice.data

import io.github.universeproject.ProfileSkin
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import org.universe.dataservice.cache.CacheClient
import org.universe.dataservice.cache.CacheService
import org.universe.dataservice.supplier.http.EntitySupplier
import org.universe.dataservice.supplier.http.Strategizable

public interface ProfileSkinCacheService {
    /**
     * Get the instance of [ProfileSkin] linked to the [uuid] data.
     * @param uuid UUID of the user.
     * @return The instance stored if found, or null if not found.
     */
    public suspend fun getByUUID(uuid: String): ProfileSkin?

    /**
     * Save the instance into cache using the key defined by the configuration.
     * @param profile Data that will be stored.
     */
    public suspend fun save(profile: ProfileSkin)
}

/**
 * Cache service for [ProfileSkin].
 * @property client Cache client.
 * @property prefixKey Prefix key to identify the data in cache.
 */
public class ProfileSkinCacheServiceImpl(
    private val client: CacheClient,
    prefixKey: String = "skin:"
) : CacheService(prefixKey), ProfileSkinCacheService {

    override suspend fun getByUUID(uuid: String): ProfileSkin? {
        return client.connect {
            val binaryFormat = client.binaryFormat
            val key = encodeKey(binaryFormat, uuid)
            val dataSerial = it.get(key) ?: return null
            decodeFromByteArrayOrNull(binaryFormat, ProfileSkin.serializer(), dataSerial)
        }
    }

    override suspend fun save(profile: ProfileSkin) {
        client.connect {
            val binaryFormat = client.binaryFormat
            val key = encodeKey(binaryFormat, profile.id)
            val data = encodeToByteArray(binaryFormat, ProfileSkin.serializer(), profile)
            it.set(key, data)
        }
    }
}

/**
 * Service to retrieve data about profile.
 */
public interface ProfileSkinService : Strategizable {

    /**
     * Get the skin information of a player from his [ProfileSkin.id].
     * @param uuid Profile's id.
     */
    public suspend fun getByUUID(uuid: String): ProfileSkin?
}

/**
 * Service to retrieve data about client identity.
 * @property supplier Strategy to manage data.
 */
public class ProfileSkinServiceImpl(override val supplier: EntitySupplier) : ProfileSkinService {

    override suspend fun getByUUID(uuid: String): ProfileSkin? = supplier.getSkin(uuid)

    override fun withStrategy(strategy: EntitySupplier): ProfileSkinService = ProfileSkinServiceImpl(strategy)
}