package network.cow.environment.service.database.table

import network.cow.environment.service.database.ConsumerState
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.jodatime.datetime

/**
 * @author Benedikt Wüller
 */
object Consumers : UUIDTable("consumers") {
    val contextId = varchar("context_id", 64).index()
    val state = enumerationByName("state", 32, ConsumerState::class).default(ConsumerState.UNREGISTERED).index()
    val startedAt = datetime("started_at")
    val stoppedAt = datetime("stopped_at").nullable()
}
