package network.cow.environment.service.producer

import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import network.cow.environment.protocol.consumer.*
import network.cow.environment.protocol.service.ConsumerRegisteredPayload
import network.cow.environment.protocol.service.RegisterConsumerPayload
import network.cow.environment.protocol.service.UnregisterConsumerPayload
import network.cow.environment.service.close
import network.cow.environment.service.consumer.Consumer
import network.cow.environment.service.consumer.ConsumerRegistry
import network.cow.environment.service.parseFrame
import java.util.UUID

/**
 * @author Benedikt Wüller
 */

//                      ,     ,
//                  ___('-&&&-')__
//                 '.__./     \__.'
//     _     _     _ .-'  6  6 \
//   /` `--'( ('--` `\         |
//  /        ) )      \ \ _   _|
// |        ( (        | (0_._0)
// |         ) )       |/ '---'
// |        ( (        |\_
// |         ) )       |( \,
//  \       ((`       / )__/
//   |     /:))\     |   d
//   |    /:((::\    |
//   |   |:::):::|   |
//   /   \::&&:::/   \
//   \   /;U&::U;\   /
//    | | | u:u | | |
//    | | \     / | |
//    | | _|   | _| |
//    / \""`   `""/ \
//   | __|       | __|
//   `"""`       `"""`

private val UNREGISTER_DELAY = 60 * 1000L

private val registeredContextIds = mutableMapOf<UUID, Consumer>()
private val unregisterJobs = mutableMapOf<UUID, Job>()

fun Route.producerWebSocketRoute() {
    webSocket("/producers") {
        ProducerRegistry.addProducer(this)
        try {
            for (frame in incoming) {
                this.handleFrame(frame)
            }
        } finally {
            ProducerRegistry.removeProducer(this)
        }
    }
}

private suspend fun WebSocketSession.handleRegisterConsumer(payload: RegisterConsumerPayload) {
    val producer = ProducerRegistry.getProducer(this)
    val contextId = payload.contextId
    unregisterJobs[contextId]?.cancelAndJoin()
    val consumer = if (registeredContextIds.contains(contextId)) {
        registeredContextIds[contextId]!!
    } else {
        val consumer = Consumer(producer = producer, contextId = contextId)
        ConsumerRegistry.registerConsumer(consumer)
        consumer
    }
    consumer.producer.send(ConsumerRegisteredPayload(payload.contextId, consumer.id, consumer.url))
}

private suspend fun handleUnregisterConsumer(payload: UnregisterConsumerPayload) {
    val consumer = ConsumerRegistry.getConsumer(payload.consumerId) ?: return
    if (unregisterJobs.containsKey(consumer.contextId)) return
    unregisterJobs[consumer.contextId] = GlobalScope.launch {
        delay(UNREGISTER_DELAY)
        if (!this.isActive) return@launch
        ConsumerRegistry.unregisterConsumer(consumer)
        unregisterJobs.remove(consumer.contextId)
    }
}

private suspend fun handleConsumerBoundPayload(payload: ConsumerBoundPayload) {
    val consumer = ConsumerRegistry.getConsumer(payload.consumerId) ?: return
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
