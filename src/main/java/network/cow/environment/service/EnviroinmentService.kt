package network.cow.environment.service

import com.google.protobuf.Message
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.cow.environment.protocol.Messages
import network.cow.environment.protocol.v1.ConsumerChangedInstanceEvent
import network.cow.environment.service.consumer.ConsumerRegistry
import network.cow.environment.service.consumer.consumerWebSocketRoute
import network.cow.environment.service.producer.ProducerRegistry
import network.cow.environment.service.producer.producerWebSocketRoute
import java.time.Duration
import java.util.*

/**
 * @author Benedikt Wüller
 */

const val UNREGISTER_DELAY = 30L

fun main() {
    val internalPort = System.getenv("ENVIRONMENT_SERVICE_PORT")?.toInt() ?: 35721
    val internalUsername = System.getenv("ENVIRONMENT_SERVICE_USERNAME") ?: "environment"
    val internalPassword = System.getenv("ENVIRONMENT_SERVICE_PASSWORD") ?: "environment"

    CloudEventConsumer.listen(ConsumerChangedInstanceEvent.getDescriptor().fullName, ConsumerChangedInstanceEvent::class.java) {
        val consumerId = UUID.fromString(it.consumerId)
        val session = ConsumerRegistry.getSession(consumerId) ?: return@listen
        GlobalScope.launch {
            ProducerRegistry.getConsumerProducer(consumerId)?.removeConsumer(consumerId, false)
            session.send(Messages.toJsonWithTypePrefix(it))
        }
    }

    embeddedServer(Netty, port = internalPort) {
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(60)
            timeout = Duration.ofSeconds(300)
        }
        install(Authentication) {
            basic("auth-basic-internal") {
                realm = "Access to the '/' path"
                validate { credentials ->
                    if (credentials.name == internalUsername && credentials.password == internalPassword) {
                        UserIdPrincipal(credentials.name)
                    } else {
                        null
                    }
                }
            }
        }
        routing {
            authenticate("auth-basic-internal") {
                producerWebSocketRoute()
            }
            consumerWebSocketRoute()
        }
    }.start(true)
}

fun parseFrame(frame: Frame) : Message {
    return when (frame) {
        is Frame.Binary -> Messages.fromBytes(frame.readBytes())
        is Frame.Text -> Messages.fromJsonWithTypePrefix(frame.readText())
        else -> throw IllegalArgumentException("The frame must be either in binary or text format.")
    }
}
