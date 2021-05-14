package network.cow.environment.service.database.dao

import network.cow.environment.service.database.table.Consumers
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

/**
 * @author Benedikt Wüller
 */
class Consumer(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Consumer>(Consumers)
    var contextId by Consumers.contextId
    var state by Consumers.state
    var startedAt by Consumers.startedAt
    var stoppedAt by Consumers.stoppedAt
}
