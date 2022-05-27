package org.universe.database.dao

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.universe.serializer.UUIDSerializer
import java.util.*

const val MAX_PSEUDO_LENGTH = 16

object ClientIdentities : Table() {
    val uuid = uuid("uuid").uniqueIndex()
    val name = varchar("name", MAX_PSEUDO_LENGTH)
}

@Serializable
data class ClientIdentity(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    val name: String
)