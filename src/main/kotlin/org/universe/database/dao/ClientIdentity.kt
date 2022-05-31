package org.universe.database.dao

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.universe.serializer.UUIDSerializer
import java.util.*

const val MAX_NAME_LENGTH = 16

object ClientIdentities : Table(ClientIdentity::class.simpleName!!) {
    val uuid = uuid("uuid").uniqueIndex()
    val name = varchar("name", MAX_NAME_LENGTH)
    override val primaryKey = PrimaryKey(uuid)
}

@Serializable
data class ClientIdentity(
    @Serializable(with = UUIDSerializer::class)
    var uuid: UUID,
    var name: String
)