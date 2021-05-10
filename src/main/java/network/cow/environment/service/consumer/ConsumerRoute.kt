package network.cow.environment.service.consumer

import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import network.cow.environment.protocol.consumer.*
import network.cow.environment.protocol.service.RegisterConsumerPayload
import network.cow.environment.protocol.service.UnregisterConsumerPayload
import network.cow.environment.service.close
import network.cow.environment.service.parseFrame
import network.cow.environment.service.producer.*
import java.util.*

/**
 * @author Benedikt Wüller
 */

fun Route.consumerWebSocketRoute() {
    webSocket("/consumers/{id}") {
        val id = this.call.parameters["id"] ?: return@webSocket this.call.respond(HttpStatusCode.BadRequest, "The id is missing.")

        val uuid = try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            return@webSocket this.call.respond(HttpStatusCode.BadRequest, "The given id is invalid.")
        }

        val exists = ConsumerRegistry.connectConsumer(uuid, this)
        if (!exists) return@webSocket this.call.respond(HttpStatusCode.BadRequest, "The consumer does not exist.")

        for (frame in incoming) {
            this.handleFrame(frame)
        }
    }
}

private suspend fun WebSocketSession.handleAudioStarted(payload: AudioStartedPayload) {
    println(payload)
}

private suspend fun WebSocketSession.handleAudioStopped(payload: AudioStoppedPayload) {
    println(payload)
}

private suspend fun WebSocketSession.handleFrame(frame: Frame) {
    when (val payload = this.parseFrame(frame) ?: return) {
        is AudioStartedPayload -> this.handleAudioStarted(payload)
        is AudioStoppedPayload -> this.handleAudioStopped(payload)
        else -> this.close(CloseReason.Codes.PROTOCOL_ERROR, "Invalid message payload.")
    }
}
