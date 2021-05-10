package network.cow.environment.service.consumer

import io.ktor.http.cio.websocket.*
import network.cow.environment.service.producer.Producer
import java.util.*

/**
 * @author Benedikt Wüller
 */
data class Consumer(
    val id: UUID = UUID.randomUUID(),
    val producer: Producer,
    var session: WebSocketSession? = null
)
