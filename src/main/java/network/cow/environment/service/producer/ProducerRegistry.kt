package network.cow.environment.service.producer

import io.ktor.http.cio.websocket.*
import network.cow.environment.service.consumer.ConsumerRegistry

/**
 * @author Benedikt Wüller
 */
object ProducerRegistry {

    private val producers = mutableMapOf<WebSocketSession, Producer>()

    fun addProducer(session: WebSocketSession) {
        this.producers[session] = Producer(session)
    }

    suspend fun removeProducer(session: WebSocketSession) {
        val producer = this.producers.remove(session) ?: return
        producer.consumers.forEach { ConsumerRegistry.unregisterConsumer(it) }
        producer.consumers.clear()
    }

    fun getProducer(session: WebSocketSession) = this.producers[session]!!

}
