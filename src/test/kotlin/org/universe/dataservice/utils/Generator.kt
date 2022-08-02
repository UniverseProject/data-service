package org.universe.dataservice.utils

import io.github.universeproject.ProfileId
import io.github.universeproject.ProfileSkin
import org.universe.dataservice.data.ClientIdentity
import org.universe.dataservice.data.MAX_NAME_LENGTH
import java.util.*

val stringGenerator = generateSequence { UUID.randomUUID().toString() }.distinct().iterator()

fun getRandomString() = stringGenerator.next()

fun createProfileId(): ProfileId {
    return ProfileId(name = getRandomString(), id = getRandomString())
}

fun createProfileSkin(id: ProfileId? = null): ProfileSkin {
    return ProfileSkin(
        id = id?.id ?: getRandomString(),
        name = id?.name ?: getRandomString(),
        properties = emptyList()
    )
}

fun createIdentity(): ClientIdentity {
    return ClientIdentity(uuid = UUID.randomUUID(), name = getRandomString().take(MAX_NAME_LENGTH))
}