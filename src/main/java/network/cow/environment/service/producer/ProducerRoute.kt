package network.cow.environment.service.producer

import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import network.cow.environment.protocol.consumer.*
import network.cow.environment.protocol.service.ConsumerRegisteredPayload
import network.cow.environment.protocol.service.RegisterConsumerPayload
import network.cow.environment.protocol.service.UnregisterConsumerPayload
import network.cow.environment.service.close
import network.cow.environment.service.consumer.Consumer
import network.cow.environment.service.consumer.ConsumerRegistry
import network.cow.environment.service.parseFrame

/**
 * @author Benedikt Wüller
 */

fun Route.producerWebSocketRoute() {
    webSocket("/producers") {
        ProducerRegistry.addProducer(this)
        try {
            for (frame in incoming) {
                this.handleFrame(frame)
            }
        } catch (e: ClosedReceiveChannelException) {
            ProducerRegistry.removeProducer(this)
        }
    }
}

private suspend fun WebSocketSession.handleRegisterConsumer(payload: RegisterConsumerPayload) {
    val producer = ProducerRegistry.getProducer(this)
    val consumer = Consumer(producer = producer)
    ConsumerRegistry.registerConsumer(consumer)
    consumer.producer.send(ConsumerRegisteredPayload(payload.contextId, consumer.id, consumer.url))
}

private suspend fun handleUnregisterConsumer(payload: UnregisterConsumerPayload) {
    val consumer = ConsumerRegistry.getConsumer(payload.consumerId)
    ConsumerRegistry.unregisterConsumer(consumer)
}

private suspend fun handleConsumerBoundPayload(payload: ConsumerBoundPayload) {
    val consumer = ConsumerRegistry.getConsumer(payload.consumerId)
    consumer.send(payload)
}

private suspend fun WebSocketSession.handleFrame(frame: Frame) {
    when (val payload = this.parseFrame(frame) ?: return) {
        is RegisterConsumerPayload -> this.handleRegisterConsumer(payload)
        is UnregisterConsumerPayload -> handleUnregisterConsumer(payload)
        is ConsumerBoundPayload -> handleConsumerBoundPayload(payload)
        else -> this.close(CloseReason.Codes.PROTOCOL_ERROR, "Invalid message payload.")
    }
}
