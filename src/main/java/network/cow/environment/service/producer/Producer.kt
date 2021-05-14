package network.cow.environment.service.producer

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import network.cow.environment.service.UNREGISTER_DELAY
import network.cow.environment.service.consumer.ConsumerRegistry
import network.cow.environment.service.database.ConsumerState
import network.cow.environment.service.database.DatabaseService
import network.cow.environment.service.database.dao.Consumer
import network.cow.environment.service.now
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class Producer {

    private val consumers = mutableSetOf<UUID>()
    private val unregisterJobs = mutableMapOf<UUID, Job>()

    fun addConsumer(id: UUID) {
        ProducerRegistry.addConsumerProducer(id, this)
        this.unregisterJobs.remove(id)?.cancel()
        this.consumers.add(id)

        transaction(DatabaseService.database) {
            val consumer = Consumer.findById(id) ?: return@transaction
            consumer.state = ConsumerState.REGISTERED
            consumer.stoppedAt = null
        }
    }

    suspend fun removeConsumer(id: UUID, unregister: Boolean = true) {
        ProducerRegistry.removeConsumerProducer(id)
        this.unregisterJobs.remove(id)?.cancel()
        this.consumers.remove(id)
        ConsumerRegistry.getSession(id)?.close()

        if (unregister) {
            transaction(DatabaseService.database) {
                val consumer = Consumer.findById(id) ?: return@transaction
                consumer.state = ConsumerState.UNREGISTERED
                consumer.stoppedAt = now()
            }
        }
    }

    fun scheduleRemove(id: UUID) {
        this.unregisterJobs[id] = GlobalScope.launch {
            delay(UNREGISTER_DELAY * 1000L)
            removeConsumer(id)
        }
    }

    fun scheduleRemovals() = this.consumers.forEach(this::scheduleRemove)

}
