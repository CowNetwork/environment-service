package network.cow.environment.service.producer

import com.google.common.collect.HashBiMap
import io.ktor.websocket.*
import java.util.*

object ProducerRegistry {

    private val consumerProducers = mutableMapOf<UUID, Producer>()
    private val producers = HashBiMap.create<WebSocketServerSession, Producer>()

    fun register(session: WebSocketServerSession) {
        producers[session] = Producer()
    }

    fun getProducer(session: WebSocketServerSession) = producers[session]

    fun getSession(producer: Producer) = producers.inverse()[producer]

    fun unregister(session: WebSocketServerSession) {
        val producer = producers.remove(session) ?: return
        producer.scheduleRemovals()
    }

    fun addConsumerProducer(uuid: UUID, producer: Producer) {
        consumerProducers[uuid] = producer
    }

    fun removeConsumerProducer(uuid: UUID) = consumerProducers.remove(uuid)

    fun getConsumerProducer(uuid: UUID) = consumerProducers[uuid]

    fun exists(uuid: UUID) = consumerProducers.containsKey(uuid)

}