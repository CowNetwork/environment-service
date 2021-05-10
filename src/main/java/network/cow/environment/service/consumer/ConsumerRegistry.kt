package network.cow.environment.service.consumer

import io.ktor.http.cio.websocket.*
import java.util.*

/**
 * @author Benedikt Wüller
 */
object ConsumerRegistry {

    private val consumers = mutableMapOf<UUID, Consumer>()

    fun registerConsumer(consumer: Consumer) {
        consumers[consumer.id] = consumer
    }

    suspend fun unregisterConsumer(consumer: Consumer) {
        consumers.remove(consumer.id)
        consumer.session?.close(CloseReason(
            CloseReason.Codes.NORMAL,
            "The consumer has been unregistered."
        ))
    }

    fun connectConsumer(id: UUID, session: WebSocketSession) : Boolean {
        val consumer = consumers[id] ?: return false
        consumer.session = session
        return true
    }

    fun disconnectConsumer(id: UUID) {
        val consumer = consumers[id] ?: return
        consumer.session = null
    }

}
