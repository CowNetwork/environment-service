package network.cow.environment.service.database.table

import org.jetbrains.exposed.dao.id.IntIdTable

/**
 * @author Benedikt Wüller
 */
object AudioDefinitions : IntIdTable() {
    val key = varchar("key", 64).uniqueIndex()
    val name = varchar("name", 64)
    val url = text("url")
    val sourceUrl = text("source_url").nullable()
    val reportUrl = text("report_url").nullable()
    val information = text("information").nullable()
}
