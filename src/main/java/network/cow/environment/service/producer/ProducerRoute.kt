package network.cow.environment.service.producer

import com.google.protobuf.Message
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import network.cow.environment.protocol.Messages
import network.cow.environment.protocol.v1.*
import network.cow.environment.service.*
import network.cow.environment.service.consumer.ConsumerRegistry
import network.cow.environment.service.database.ConsumerState
import network.cow.environment.service.database.DatabaseService
import network.cow.environment.service.database.dao.AudioDefinition
import network.cow.environment.service.database.dao.Consumer
import network.cow.environment.service.database.table.AudioDefinitions
import network.cow.environment.service.database.table.Consumers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.util.*
import network.cow.environment.protocol.v1.AudioDefinition as ProtoAudioDefinition

/**
 * @author Benedikt Wüller
 */

fun Route.producerWebSocketRoute() {
    webSocket("/producers") {
        ProducerRegistry.register(this)
        try {
            for (frame in this.incoming) {
                val message = parseFrame(frame)
                if (!Messages.validate(message)) break
                if (!this.handleProducerMessage(message)) break
            }
        } finally {
            ProducerRegistry.unregister(this)
        }
    }
}

private suspend fun WebSocketServerSession.handleProducerMessage(message: Message) : Boolean {
    return when (message) {
        is RegisterConsumerRequest -> this.handleRegisterConsumerRequest(message)
        is UnregisterConsumerRequest -> this.handleUnregisterConsumerRequest(message)
        is PlayAudioRequest -> handlePlayAudioRequest(message)
        is UpdateAudioRequest -> handleConsumerBoundRequest(message.consumerId, message)
        is FadeAudioRequest -> handleConsumerBoundRequest(message.consumerId, message)
        is StopAudioRequest -> handleConsumerBoundRequest(message.consumerId, message)
        is SetPositionRequest -> handleConsumerBoundRequest(message.consumerId, message)
        else -> false
    }
}

private suspend fun WebSocketServerSession.handleRegisterConsumerRequest(message: RegisterConsumerRequest) : Boolean {
    val producer = ProducerRegistry.getProducer(this) ?: return false

    // Find the latest consumer session within bounds.
    val existingConsumer = transaction(DatabaseService.database) {
        Consumer.find {
            (Consumers.contextId eq message.contextId).and {
                ((Consumers.stoppedAt eq null).or {
                    (DateAdd(Consumers.stoppedAt as Column<DateTime>, longLiteral(UNREGISTER_DELAY)) greaterEq now())
                })
            }
        }.orderBy(Consumers.stoppedAt to SortOrder.DESC).firstOrNull()
    }

    val consumer = when (existingConsumer) {
        null -> {
            transaction(DatabaseService.database) {
                Consumer.new {
                    this.contextId = message.contextId
                    this.startedAt = now()
                }
            }
        }
        else -> existingConsumer
    }

    producer.addConsumer(consumer.id.value)

    handleConsumerBoundRequest(consumer.id.toString(), ConsumerRegisteredEvent.newBuilder()
        .setContextId(consumer.contextId)
        .setConsumerId(consumer.id.toString())
        .setUrl("https://localhost/${consumer.id}") // TODO
        .build())

    if (existingConsumer != null) {
        transaction(DatabaseService.database) {
            existingConsumer.state = ConsumerState.CONNECTED
            existingConsumer.stoppedAt = null
        }

        CloudEventProducer.send(
            KAFKA_TOPIC, ConsumerChangedInstanceEvent.newBuilder()
            .setConsumerId(existingConsumer.id.value.toString())
            .setHost(PUBLIC_HOST)
            .setPort(PUBLIC_PORT)
            .build())
    }

    return true
}

private fun WebSocketServerSession.handleUnregisterConsumerRequest(message: UnregisterConsumerRequest) : Boolean {
    val producer = ProducerRegistry.getProducer(this) ?: return false
    producer.scheduleRemove(UUID.fromString(message.consumerId))
    return true
}

private suspend fun handleConsumerBoundRequest(consumerId: String, message: Message) : Boolean {
    val session = ConsumerRegistry.getSession(UUID.fromString(consumerId)) ?: return false
    session.send(Messages.toJsonWithTypePrefix(message))
    return true
}

private suspend fun handlePlayAudioRequest(message: PlayAudioRequest) : Boolean {
    val builder = PlayAudioRequest.newBuilder(message)

    if (message.hasKey()) {
        builder.clearIdentifier()

        val definition = transaction(DatabaseService.database) {
            AudioDefinition.find { AudioDefinitions.key eq message.key }.firstOrNull()
        } ?: return false

        builder.definition = ProtoAudioDefinition.newBuilder()
            .setKey(definition.key)
            .setName(definition.name)
            .setUrl(definition.url)
            .setSourceUrl(definition.sourceUrl)
            .setReportUrl(definition.reportUrl)
            .setInformation(definition.information)
            .build()
    }

    return handleConsumerBoundRequest(message.consumerId, builder.build())
}
