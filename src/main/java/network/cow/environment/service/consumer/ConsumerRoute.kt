package network.cow.environment.service.consumer

import com.google.protobuf.Message
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import network.cow.environment.protocol.Messages
import network.cow.environment.protocol.v1.AudioStartedEvent
import network.cow.environment.protocol.v1.AudioStoppedEvent
import network.cow.environment.service.parseFrame
import network.cow.environment.service.producer.ProducerRegistry
import java.util.*

/**
 * @author Benedikt Wüller
 */

fun Route.consumerWebSocketRoute() {
    webSocket("/consumers/{id}") {
        val id = this.call.parameters["id"] ?: return@webSocket this.call.respond(
            HttpStatusCode.BadRequest,
            "The id is missing."
        )

        val uuid = try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            return@webSocket this.call.respond(HttpStatusCode.BadRequest, "The given id is invalid.")
        }

        if (!ProducerRegistry.exists(uuid)) {
            return@webSocket this.call.respond(HttpStatusCode.BadRequest, "The consumer does not exist.")
        }

        if (ConsumerRegistry.getSession(uuid) != null) {
            return@webSocket this.call.respond(HttpStatusCode.BadRequest, "The consumer is already connected.")
        }

        ConsumerRegistry.connect(uuid, this)
        try {
            for (frame in this.incoming) {
                val message = parseFrame(frame)
                if (!Messages.validate(message)) break
                if (!this.handleConsumerMessage(message)) break
            }
        } finally {
            ConsumerRegistry.disconnect(this)
        }
    }
}

private suspend fun WebSocketServerSession.handleConsumerMessage(message: Message) : Boolean {
    return when (message) {
        is AudioStartedEvent -> this.handleProducerBoundMessage(message.consumerId, message)
        is AudioStoppedEvent -> this.handleProducerBoundMessage(message.consumerId, message)
        else -> false
    }
}

private suspend fun WebSocketServerSession.handleProducerBoundMessage(consumerId: String, message: Message) : Boolean {
    val sessionId = ConsumerRegistry.getSessionId(this) ?: return false
    if (sessionId.toString() != consumerId) return false

    val producer = ProducerRegistry.getConsumerProducer(sessionId) ?: return false
    val session = ProducerRegistry.getSession(producer) ?: return false

    session.send(Messages.toBytes(message))
    return true
}
