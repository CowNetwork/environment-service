package network.cow.environment.service.consumer

import io.ktor.http.cio.websocket.*
import network.cow.environment.protocol.service.ConsumerConnectedPayload
import network.cow.environment.protocol.service.ConsumerDisconnectedPayload
import network.cow.environment.protocol.service.ConsumerRegisteredPayload
import java.util.*

/**
 * @author Benedikt Wüller
 */
object ConsumerRegistry {

    private val consumers = mutableMapOf<UUID, Consumer>()
    private val consumerSessions = mutableMapOf<WebSocketSession, UUID>()

    fun registerConsumer(consumer: Consumer) {
        this.consumers[consumer.id] = consumer
    }

    suspend fun unregisterConsumer(consumer: Consumer) {
        consumer.producer.consumers.remove(consumer)
        consumer.session?.close(CloseReason(
            CloseReason.Codes.NORMAL,
            "The consumer has been unregistered."
        ))
        this.disconnectConsumer(consumer.id)
        this.consumers.remove(consumer.id)
    }

    suspend fun connectConsumer(id: UUID, session: WebSocketSession) : Boolean {
        val consumer = this.consumers[id] ?: return false
        consumer.session = session
        consumer.producer.send(ConsumerConnectedPayload(consumer.id))
        this.consumerSessions[session] = id
        return true
    }

    suspend fun disconnectConsumer(id: UUID) {
        val consumer = this.consumers[id] ?: return
        this.consumerSessions.remove(consumer.session)
        consumer.producer.send(ConsumerDisconnectedPayload(consumer.id))
        consumer.session = null
    }

    fun getConsumer(id: UUID) = this.consumers[id]!!

    fun getConsumer(session: WebSocketSession) = this.getConsumer(this.consumerSessions[session]!!)

}
