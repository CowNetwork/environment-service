package network.cow.environment.service.producer

import io.ktor.http.cio.websocket.*
import network.cow.environment.protocol.Message
import network.cow.environment.protocol.Payload
import network.cow.environment.service.JsonService
import network.cow.environment.service.consumer.Consumer

/**
 * @author Benedikt Wüller
 */
data class Producer(
    val session: WebSocketSession,
    val consumers: MutableList<Consumer> = mutableListOf()
) {
    suspend fun send(payload: Payload) {
        val message = Message(payload.type, payload)
        this.session.send(JsonService.toJson(message))
    }
}
