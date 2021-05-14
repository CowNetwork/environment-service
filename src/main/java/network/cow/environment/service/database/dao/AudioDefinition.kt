package network.cow.environment.service.database.dao

import network.cow.environment.service.database.table.AudioDefinitions
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

/**
 * @author Benedikt Wüller
 */
class AudioDefinition(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AudioDefinition>(AudioDefinitions)
    var key by AudioDefinitions.key
    var name by AudioDefinitions.name
    var url by AudioDefinitions.url
    var sourceUrl by AudioDefinitions.sourceUrl
    var reportUrl by AudioDefinitions.reportUrl
    var information by AudioDefinitions.information
}
