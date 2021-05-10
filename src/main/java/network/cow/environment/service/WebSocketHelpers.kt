package network.cow.environment.service

import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import io.ktor.http.cio.websocket.*
import network.cow.environment.protocol.Payload
import network.cow.environment.protocol.PayloadRegistry

/**
 * @author Benedikt Wüller
 */

suspend fun WebSocketSession.close(code: CloseReason.Codes, message: String) {
    this.close(CloseReason(code, message))
}

suspend fun WebSocketSession.parseFrame(frame: Frame) : Payload? {
    if (frame !is Frame.Text) {
        this.close(CloseReason.Codes.PROTOCOL_ERROR, "The frame is not of type text.")
        return null
    }

    return try {
        val text = frame.readText()
        val json = JsonService.fromJson(text, JsonObject::class.java)
        val type = json["type"].asString
        PayloadRegistry.parsePayload(type, JsonService.toJson(json["payload"]))
    } catch (e: JsonSyntaxException) {
        close(CloseReason.Codes.PROTOCOL_ERROR, "Invalid json body.")
        return null
    }
}
