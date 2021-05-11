package network.cow.environment.service.consumer

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import network.cow.environment.protocol.PayloadRegistry
import network.cow.environment.protocol.consumer.*
import network.cow.environment.service.JsonService
import network.cow.environment.service.close
import network.cow.environment.service.parseFrame
import network.cow.environment.service.producer.*
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

        val exists = ConsumerRegistry.connectConsumer(uuid, this)
        if (!exists) return@webSocket this.call.respond(HttpStatusCode.BadRequest, "The consumer does not exist or is already connected.")

        try {
            for (frame in incoming) {
                this.handleFrame(frame)
            }
        } finally {
            ConsumerRegistry.disconnectConsumer(uuid)
        }
    }
}

private suspend fun WebSocketSession.handleProducerBoundPayload(payload: ProducerBoundPayload) {
    val consumer = ConsumerRegistry.getConsumer(this)
    if (consumer.id != payload.consumerId) {
        this.close(CloseReason.Codes.PROTOCOL_ERROR, "Invalid consumer id.")
        return
    }
    consumer.producer.send(payload)
}

private suspend fun WebSocketSession.handleFrame(frame: Frame) {
    when (val payload = this.parseFrame(frame) ?: return) {
        is ProducerBoundPayload -> this.handleProducerBoundPayload(payload)
        else -> this.close(CloseReason.Codes.PROTOCOL_ERROR, "Invalid message payload.")
    }
}
