package network.cow.environment.service.consumer

import io.ktor.http.cio.websocket.*
import network.cow.environment.protocol.Message
import network.cow.environment.protocol.Payload
import network.cow.environment.service.JsonService
import network.cow.environment.service.producer.Producer
import java.util.*

/**
 * @author Benedikt Wüller
 */
data class Consumer(
    val id: UUID = UUID.randomUUID(),
    val contextId: UUID,
    val producer: Producer,
    var session: WebSocketSession? = null
) {

    val url = (System.getenv("ENVIRONMENT_SERVICE_BASE_URL") ?: "http://localhost") + "/$id"

    suspend fun send(payload: Payload) {
        val message = Message(payload.type, payload)
        this.session?.send(JsonService.toJson(message))
    }
}
