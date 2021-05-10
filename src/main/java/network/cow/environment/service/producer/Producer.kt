package network.cow.environment.service.producer

import io.ktor.http.cio.websocket.*
import network.cow.environment.service.consumer.Consumer

/**
 * @author Benedikt Wüller
 */
data class Producer(
    val session: WebSocketSession,
    val consumers: MutableList<Consumer> = mutableListOf()
)
