package org.universe.utils

import org.universe.data.ClientIdentity
import org.universe.data.MAX_NAME_LENGTH
import org.universe.data.ProfileId
import org.universe.data.ProfileSkin
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