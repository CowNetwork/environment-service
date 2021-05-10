package network.cow.environment.service.producer

import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import network.cow.environment.protocol.consumer.*
import network.cow.environment.protocol.service.RegisterConsumerPayload
import network.cow.environment.protocol.service.UnregisterConsumerPayload
import network.cow.environment.service.close
import network.cow.environment.service.parseFrame

/**
 * @author Benedikt Wüller
 */

fun Route.producerWebSocketRoute() {
    webSocket("/producers") {
        ProducerRegistry.addProducer(this)
        try {
            for (frame in incoming) {
                this.handleFrame(frame)
            }
        } catch (e: ClosedReceiveChannelException) {
            ProducerRegistry.removeProducer(this)
        }
    }
}

private suspend fun WebSocketSession.handleRegisterConsumer(payload: RegisterConsumerPayload) {
    println(payload)
}

private suspend fun WebSocketSession.handleUnregisterConsumer(payload: UnregisterConsumerPayload) {
    println(payload)
}

private suspend fun WebSocketSession.handleSetPosition(payload: SetPositionPayload) {
    println(payload)
}

private suspend fun WebSocketSession.handlePlayAudio(payload: PlayAudioPayload) {
    println(payload)
}

private suspend fun WebSocketSession.handleUpdateAudio(payload: UpdateAudioPayload) {
    println(payload)
}

private suspend fun WebSocketSession.handleFadeAudio(payload: FadeAudioPayload) {
    println(payload)
}

private suspend fun WebSocketSession.handleStopAudio(payload: StopAudioPayload) {
    println(payload)
}

private suspend fun WebSocketSession.handleFrame(frame: Frame) {
    when (val payload = this.parseFrame(frame) ?: return) {
        is RegisterConsumerPayload -> this.handleRegisterConsumer(payload)
        is UnregisterConsumerPayload -> this.handleUnregisterConsumer(payload)
        is SetPositionPayload -> this.handleSetPosition(payload)
        is PlayAudioPayload -> this.handlePlayAudio(payload)
        is UpdateAudioPayload -> this.handleUpdateAudio(payload)
        is FadeAudioPayload -> this.handleFadeAudio(payload)
        is StopAudioPayload -> this.handleStopAudio(payload)
        else -> this.close(CloseReason.Codes.PROTOCOL_ERROR, "Invalid message payload.")
    }
}
